package org.pih.warehouse.api.regression.platform.inventory

import spock.lang.PendingFeature

import org.pih.warehouse.inventory.*
import org.grails.web.json.JSONObject
import org.springframework.mock.web.MockMultipartFile

import org.pih.warehouse.api.PackPageItem
import org.pih.warehouse.api.PickPageItem
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.api.StockMovementItem
import org.pih.warehouse.api.regression.platform.common.base.ServiceIntegrationSpec
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.packList.ImportPackListCommand
import org.pih.warehouse.picklist.PicklistImportDataCommand
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.picklist.PicklistItemCommand
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionSourceType
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentStatusCode

/**
 * Outbound (requisition-based) stock movements from a depot holding 5 of lot A in bin A and 3 of lot B with no bin:
 * the items of each workflow step, picks cleared, recreated and imported, packing from a file, and sending, rolling
 * back and deleting a request.
 */
class StockMovementServiceOutboundIntegrationSpec extends ServiceIntegrationSpec {

    StockMovementService stockMovementService
    org.pih.warehouse.core.DashboardService dashboardService

    Location depot
    Location destination
    Location binA
    Product product
    InventoryItem lotA
    InventoryItem lotB

    void setup() {
        depot = newDepot('Outbound depot')
        destination = newDepot('Outbound destination')
        binA = newBin(depot, uniqueName('A'))
        product = newProduct()
        lotA = stock(depot, product, uniqueName('A'), null, 5, binA)
        lotB = stock(depot, product, uniqueName('B'), null, 3)
    }

    /** A request of 5 of the product from the depot; returns its id. */
    private String request(boolean electronic = false) {
        return asAdminAt(depot) {
            StockMovement header = new StockMovement(origin: Location.get(depot.id), destination: Location.get(destination.id),
                    requestedBy: User.findByUsername('admin'), dateRequested: new Date(), description: 'Outbound request',
                    sourceType: electronic ? RequisitionSourceType.ELECTRONIC : RequisitionSourceType.PAPER)
            String id = stockMovementService.createStockMovement(header).id
            StockMovement created = stockMovementService.getStockMovement(id)
            created.lineItems = [new StockMovementItem(product: Product.get(product.id), quantityRequested: 5, sortOrder: 0,
                    stockMovement: created)]
            stockMovementService.updateItems(created, false)
            id
        }
    }

    private void transition(String id, Map json) {
        asAdminAt(depot) {
            stockMovementService.transitionStockMovement(stockMovementService.getStockMovement(id), new JSONObject(json))
        }
    }

    /** A request that has been validated and picked automatically. */
    private String picking(boolean electronic = false) {
        String id = request(electronic)
        transition(id, [status: 'VALIDATED'])
        transition(id, [status: 'PICKING', createPicklist: true])
        return id
    }

    private String firstItemId(String id) {
        return asAdminAt(depot) { Requisition.get(id).requisitionItems.first().id }
    }

    private Map<String, Integer> picks(String id) {
        return asAdminAt(depot) {
            Requisition.get(id).picklist?.picklistItems?.collectEntries { PicklistItem item ->
                [(item.inventoryItem.lotNumber): item.quantity]
            } ?: [:]
        }
    }

    @PendingFeature(reason = 'StockMovementService.groovy:1040 buildEditPageItems queries "in ()" (SQL error) when the edit page has no rows, as for an electronic request on OBAF')
    void 'the items of an electronic request at each step of the workflow'() {
        given:
        String id = picking(true)
        String itemId = firstItemId(id)

        when:
        Map steps = asAdminAt(depot) {
            [
                    plain       : stockMovementService.getStockMovementItems(id, null, 10, 0, false)*.quantityRequested,
                    add         : stockMovementService.getStockMovementItems(id, 2, null, null, false),
                    revise      : stockMovementService.getStockMovementItems(id, 3, 10, 0, false),
                    revisePast  : stockMovementService.getStockMovementItems(id, 3, 10, 5, false),
                    pick        : stockMovementService.getStockMovementItems(id, 4, 10, 0, true).collect { PickPageItem pickPageItem -> pickPageItem.quantityPicked },
                    pack        : stockMovementService.getStockMovementItems(id, 5, null, null, false).size(),
                    send        : stockMovementService.getStockMovementItems(id, 6, 1, 0, false).size(),
                    addItem     : stockMovementService.getStockMovementItem(itemId, 2, false, false),
                    reviseItem  : stockMovementService.getStockMovementItem(itemId, 3, false, false),
                    pickItem    : stockMovementService.getStockMovementItem(itemId, 4, true, true).quantityPicked,
                    packItem    : stockMovementService.getStockMovementItem(itemId, 5, false, false) instanceof PackPageItem,
                    sendItem    : stockMovementService.getStockMovementItem(itemId, 6, false, false) instanceof PackPageItem,
                    plainItem   : stockMovementService.getStockMovementItem(itemId, null, false, false).quantityRequested,
                    substitutes : stockMovementService.getAvailableSubstitutions(Location.get(depot.id), RequisitionItem.get(itemId)),
            ]
        }

        then:
        steps.plain == [5]
        steps.add.size() == 1
        steps.add[0].quantityRequested == 5
        steps.add[0].containsKey('monthlyDemand')
        steps.add[0].quantityOnHand == 0
        steps.revise.size() == 1
        steps.revise[0].quantityRequested == 5
        steps.revise[0].quantityOnHand == 8
        steps.revise[0].containsKey('quantityOnHandRequesting')
        steps.revise[0].containsKey('quantityDemandRequesting')
        steps.revisePast == []
        steps.pick == [5]
        steps.pack == 0
        steps.send == 0
        steps.addItem.quantityRequested == 5
        steps.reviseItem.requisitionItemId == itemId
        steps.reviseItem.containsKey('demandPerReplenishmentPeriod')
        steps.pickItem == 5
        steps.packItem
        steps.sendItem
        steps.plainItem == 5
        steps.substitutes == []
    }

    void 'picks are cleared, recreated and validated'() {
        given:
        String id = picking()

        when:
        Map<String, Integer> automatic = picks(id)
        asAdminAt(depot) { stockMovementService.clearPicklist(id) }
        Map<String, Integer> cleared = picks(id)
        asAdminAt(depot) { stockMovementService.createPicklist(id) }
        Map<String, Integer> recreated = picks(id)
        Boolean valid = asAdminAt(depot) { stockMovementService.validatePicklist(id) }

        then:
        automatic.values().sum() == 5
        cleared == [:]
        recreated == automatic
        valid
    }

    private PicklistImportDataCommand pickImport(String id, List<Map> rows) {
        PicklistImportDataCommand command = new PicklistImportDataCommand()
        command.stockMovement = stockMovementService.getStockMovement(id)
        command.pickPageItems = stockMovementService.getPickPageItems(id, null, null)
        command.location = Location.get(depot.id)
        command.picklistItems = rows.collect { Map row ->
            new PicklistItemCommand(id: firstItemIdInSession(id), code: product.productCode, name: product.name,
                    lotNumber: row.lot, binLocation: row.bin, quantityAsText: row.quantity as String)
        }
        return command
    }

    private String firstItemIdInSession(String id) {
        return Requisition.get(id).requisitionItems.first().id
    }

    void 'a pick list file replaces the automatic picks when it is valid'() {
        given:
        String id = picking()

        when:
        List<String> errors = asAdminAt(depot) {
            PicklistImportDataCommand command = pickImport(id, [[lot: lotA.lotNumber, bin: binA.name, quantity: 2],
                                                                [lot: lotB.lotNumber, bin: 'DEFAULT', quantity: 3]])
            stockMovementService.validatePicklistListImport(command)
            List<String> codes = command.picklistItems*.errors*.allErrors.flatten()*.code
            stockMovementService.importPicklistItems(command)
            codes
        }

        then:
        errors == []
        picks(id) == [(lotA.lotNumber): 2, (lotB.lotNumber): 3]
    }

    void 'a pick list file with an unknown lot, an unknown bin and too much quantity is refused'() {
        given:
        String id = picking()
        Map<String, Integer> automatic = picks(id)

        when:
        List<List<String>> errors = asAdminAt(depot) {
            PicklistImportDataCommand command = pickImport(id, [[lot: 'NO-SUCH-LOT', bin: binA.name, quantity: 5],
                                                                [lot: lotA.lotNumber, bin: 'NO-SUCH-BIN', quantity: 3]])
            stockMovementService.validatePicklistListImport(command)
            stockMovementService.importPicklistItems(command)
            command.picklistItems.collect { it.errors.allErrors*.code }
        }

        then:
        errors[0].contains('importPickCommand.inventoryItem.notFound.error')
        errors[0].contains('importPickCommand.quantity.error')
        errors[1].contains('importPickCommand.binLocation.notFound.error')
        picks(id) == automatic
    }

    void 'a picked request is packed from a file, and a packed line is split'() {
        given:
        String id = picking()
        transition(id, [status: 'PICKED'])

        when:
        List<Map> template = asAdminAt(depot) { stockMovementService.buildPackTemplateLineItems(id) }
        String shipmentItemId = template[0].values()[0]
        String csv = 'Id,Code,Name,Bin,Lot,Expiry,Quantity,UoM,Recipient,Pallet,Box\n' +
                "${shipmentItemId},${product.productCode},x,,,,5,EA,,Pallet 1,Box 1\n" +
                'no-such-item,x,x,,,,1,EA,Nobody At All,,Box 9\n'
        List<String> errors = asAdminAt(depot) {
            ImportPackListCommand command = new ImportPackListCommand()
            command.importFile = new MockMultipartFile('file', 'pack.csv', 'text/csv', csv.bytes)
            command.location = Location.get(depot.id)
            stockMovementService.processPackListImport(command, id)
        }
        Map packed = asAdminAt(depot) {
            ShipmentItem item = ShipmentItem.get(shipmentItemId)
            [box: item.container?.name, pallet: item.container?.parentContainer?.name]
        }
        List<Integer> quantitiesBefore = asAdminAt(depot) {
            Shipment.findByRequisition(Requisition.get(id)).shipmentItems*.quantity
        }
        List<Integer> split = asAdminAt(depot) {
            PackPageItem line = new PackPageItem(shipmentItemId: shipmentItemId, splitLineItems: [
                    new PackPageItem(quantityShipped: 1, palletName: 'Pallet 2'),
                    new PackPageItem(quantityShipped: 1, palletName: 'Pallet 3', boxName: 'Box 3')])
            stockMovementService.updatePackPageItems([line])
            Shipment.findByRequisition(Requisition.get(id)).shipmentItems*.quantity
        }
        Integer firstPage = asAdminAt(depot) { stockMovementService.getPackPageItems(id, 1, 0).size() }

        then:
        template.size() == quantitiesBefore.size()
        errors.size() == 2
        errors.every { it.startsWith('Row 2') }
        errors.every { !it.contains('org.pih') && !it.contains('{0}') }
        packed == [box: 'Box 1', pallet: 'Pallet 1']
        quantitiesBefore.sum() == 5
        split.size() == quantitiesBefore.size() + 1
        split.count { it == 1 } >= 2
        firstPage == 1
    }

    void 'pending requests of a product are listed for other requests'() {
        given:
        String id = picking()
        transition(id, [status: 'PICKED'])

        when:
        List pending = asAdminAt(depot) {
            stockMovementService.getPendingRequisitionDetails(Location.get(depot.id), Product.get(product.id), 'another-request')
        }
        List pendingForItself = asAdminAt(depot) {
            stockMovementService.getPendingRequisitionDetails(Location.get(depot.id), Product.get(product.id), id)
        }
        List pendingItems = asAdminAt(depot) { stockMovementService.getPendingRequisitionItems(Location.get(depot.id))*.id }
        Map fastMovers = asAdminAt(depot) { dashboardService.getFastMovers(Location.get(depot.id), new Date() + 1, 5) }

        then:
        pending.size() == 1
        pending[0].quantityRequested == 5
        pending[0].quantityPicked == 5
        pending[0].'destination.name' == destination.name
        pendingForItself == []
        pendingItems == [firstItemId(id)]
        fastMovers.results*.id == [product.id]
        fastMovers.results[0].quantityRequested == 5
        fastMovers.results[0].quantityOnHand == 8
    }

    void 'a status-only transition moves the request to the matching requisition status'() {
        given:
        String id = request()

        when:
        transition(id, [status: 'VALIDATED', statusOnly: true])

        then:
        asAdminAt(depot) { Requisition.get(id).status } == RequisitionStatus.VERIFYING
    }

    void 'a request is checked, sent, rolled back and deleted'() {
        given:
        String id = picking()
        transition(id, [status: 'PICKED'])

        when:
        transition(id, [status: 'CHECKING'])
        // MySQL rounds fractional seconds when it stores the expected shipping date, so a date set to "now" can read
        // back up to half a second in the future and sending in that same second is refused as a future ship date
        Thread.sleep(1100)
        transition(id, [status: 'DISPATCHED'])
        Map sent = asAdminAt(depot) {
            [requisition: Requisition.get(id).status, shipment: Shipment.findByRequisition(Requisition.get(id)).currentStatus]
        }

        then:
        sent.requisition == RequisitionStatus.ISSUED
        sent.shipment == ShipmentStatusCode.SHIPPED

        when:
        asAdminAt(depot) { stockMovementService.isSynchronizationAuthorized(stockMovementService.getStockMovement(id)) }

        then:
        IllegalStateException e = thrown()
        e.message.contains('must not have any shipment items')

        when:
        transition(id, [rollback: true])
        Map rolledBack = asAdminAt(depot) {
            [requisition: Requisition.get(id).status, shipment: Shipment.findByRequisition(Requisition.get(id)).currentStatus]
        }
        asAdminAt(depot) { stockMovementService.deleteStockMovement(id) }

        then:
        rolledBack.shipment == ShipmentStatusCode.PENDING
        rolledBack.requisition != RequisitionStatus.ISSUED
        asAdminAt(depot) { Requisition.get(id) } == null
    }

    void 'an item changed while picking gets new picks'() {
        given:
        String id = picking()

        when:
        asAdminAt(depot) {
            StockMovement stockMovement = stockMovementService.getStockMovement(id)
            stockMovement.lineItems[0].quantityRequested = 4
            stockMovementService.updateItems(stockMovement, false)
        }

        then:
        picks(id).values().sum() == 4
    }

    void 'a picked item is canceled, then reverted with new picks'() {
        given:
        String id = picking()

        when:
        Map canceled = asAdminAt(depot) {
            StockMovementItem item = stockMovementService.getStockMovement(id).lineItems[0]
            item.reasonCode = 'STOCKOUT'
            item.comments = 'Not in stock'
            StockMovementItem result = stockMovementService.cancelItem(item)
            [quantityCanceled: RequisitionItem.get(item.id).quantityCanceled, status: result.statusCode]
        }
        Map<String, Integer> afterCancel = picks(id)
        asAdminAt(depot) {
            stockMovementService.revertItemAndCreateMissingPicklist(stockMovementService.getStockMovement(id).lineItems[0])
        }

        then: 'OBAF: StockMovementService.cancelItem leaves an item untouched once picking started (it logs a manual review)'
        canceled.quantityCanceled == null
        afterCancel.values().sum() == 5
        picks(id).values().sum() == 5
    }

    void 'the shipment of a picked request is updated, and the request follows'() {
        given:
        String id = picking()
        transition(id, [status: 'PICKED'])

        when:
        asAdminAt(depot) {
            StockMovement stockMovement = stockMovementService.getStockMovement(id)
            stockMovement.description = 'Renamed request'
            stockMovement.driverName = 'Driver'
            stockMovement.trackingNumber = 'TRACK-9'
            stockMovement.comments = 'Fragile'
            stockMovementService.updateShipment(stockMovement)
        }
        Map result = asAdminAt(depot) {
            Shipment shipment = Shipment.findByRequisition(Requisition.get(id))
            StockMovement stockMovement = stockMovementService.getStockMovement(id)
            [
                    driver      : shipment.driverName,
                    description : shipment.description,
                    information : shipment.additionalInformation,
                    request     : Requisition.get(id).description,
                    tracking    : stockMovement.trackingNumber,
                    receiptItems: stockMovementService.getStockMovementReceiptItems(stockMovement),
            ]
        }

        then:
        result.driver == 'Driver'
        result.description == 'Renamed request'
        result.information == 'Fragile'
        result.request == 'Renamed request'
        result.tracking == 'TRACK-9'
        !result.receiptItems
    }

    void 'a picked request without shipped stock is synchronized into a sent shipment'() {
        given:
        String id = picking()
        transition(id, [status: 'PICKED'])

        when:
        asAdminAt(depot) { stockMovementService.synchronizeStockMovement(id, new Date() - 1) }
        Map result = asAdminAt(depot) {
            Shipment shipment = Shipment.findByRequisition(Requisition.get(id))
            [status: shipment.currentStatus, transactions: shipment.outgoingTransactions?.size()]
        }

        then:
        result.status == ShipmentStatusCode.SHIPPED
        result.transactions == 1
    }

    void 'approving or rejecting needs a request that is pending approval'() {
        given:
        String id = request()

        when:
        transition(id, [status: status])

        then:
        IllegalArgumentException e = thrown()
        e.message.contains('not pending approval')

        where:
        status << ['APPROVED', 'REJECTED']
    }
}
