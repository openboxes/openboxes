package org.pih.warehouse.regression.platform.inventory

import spock.lang.Ignore
import org.pih.warehouse.inventory.*
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.grails.spring.beans.factory.InstanceFactoryBean
import org.springframework.mock.web.MockMultipartFile
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.allocation.AllocationItemRequest
import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.api.StockMovementDirection
import org.pih.warehouse.api.SuggestedItem
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.DocumentCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.core.User
import org.pih.warehouse.core.UserService
import org.pih.warehouse.core.history.HistoryContext
import org.pih.warehouse.core.history.HistoryItem
import org.pih.warehouse.packList.ImportPackListCommand
import org.pih.warehouse.packList.ImportPackListItemCommand
import org.pih.warehouse.picklist.Picklist
import org.pih.warehouse.picklist.PicklistImportDataCommand
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.picklist.PicklistItemCommand
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionSourceType
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.shipping.Container
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentStatusCode

/**
 * The decision rules of the stock movement service that need no database: pick allocation over bins, the checks
 * before a stock movement is synchronized, the messages that disable the edit and receive pages, and the parsing of
 * the pick and pack import files.
 */
class StockMovementServiceRulesSpec extends Specification implements ServiceUnitTest<StockMovementService>, DataTest {

    OutboundStockMovementService outboundStockMovementService = Mock()
    StockMovementHistoryProvider stockMovementHistoryProvider = Mock()
    UserService userService = Mock()

    Location defaultBin = null
    Location bin = new Location(name: 'A-01')
    Location otherBin = new Location(name: 'A-02')
    Location receivingBin = new Location(name: 'R-01', supportedActivities: [ActivityCode.RECEIVE_STOCK.id] as Set)
    Location holdBin = new Location(name: 'H-01', supportedActivities: [ActivityCode.HOLD_STOCK.id] as Set)
    Product product = new Product(productCode: 'P1', name: 'Paracetamol')
    InventoryItem lot = new InventoryItem(product: product, lotNumber: 'L1')

    void setupSpec() {
        mockDomains(Location)
    }

    void setup() {
        service.outboundStockMovementService = outboundStockMovementService
        service.stockMovementHistoryProvider = stockMovementHistoryProvider
        service.userService = userService
        // The disabled messages and document links come from the application tag library; this one echoes the code
        Expando tagLib = new Expando()
        tagLib.message = { Map args -> args.args ? "${args.code}:${args.args.join(',')}".toString() : args.code }
        defineBeans {
            'org.grails.plugins.web.taglib.ApplicationTagLib'(InstanceFactoryBean, tagLib, Expando)
        }
    }

    void cleanup() {
        new AuthService().setCurrentLocation(null)
    }

    private AvailableItem available(Location binLocation, BigDecimal quantity) {
        return new AvailableItem(inventoryItem: lot, binLocation: binLocation, quantityAvailable: quantity, quantityOnHand: quantity)
    }

    @Unroll
    void 'allocation rules keep #expected of #description'() {
        given:
        Map<String, Location> bins = [default: defaultBin, bin: bin, otherBin: otherBin, receiving: receivingBin, hold: holdBin]
        List<AvailableItem> items = binNames.collect { available(bins[it], 10) }

        when:
        List<AvailableItem> kept = service.applyAllocationRulesOnAvailableItems(items, 5)

        then:
        kept*.binLocation*.name == expected.collect { bins[it]?.name }

        where:
        description                                  | binNames                    || expected
        'stock only in the default location'         | ['default']                 || ['default']
        'stock in one bin'                           | ['bin']                     || ['bin']
        'stock in one bin and a hold bin'            | ['bin', 'hold']             || ['bin']
        'stock in two receiving bins'                | ['receiving', 'receiving']  || ['receiving', 'receiving']
        'stock in a receiving bin and the default'   | ['receiving', 'default']    || ['receiving', 'default']
        'stock in two bins (the user must choose)'   | ['bin', 'otherBin']         || []
        'stock in a bin and a receiving bin'         | ['bin', 'receiving']        || []
    }

    // OBAF: the pick validation request is AllocationItemRequest (OBLS-521 gave AllocationRequest to the allocation API)
    private AllocationItemRequest allocationRequest(List<AvailableItem> items, Integer quantityRequired) {
        return new AllocationItemRequest(product: product, inventoryItem: lot, availableItems: items, quantityRequired: quantityRequired,
                picklistItemCommand: new PicklistItemCommand(id: 'item', code: 'P1', lotNumber: 'L1', quantityAsText: "${quantityRequired}"))
    }

    @Unroll
    void 'an allocation request over #description is rejected with #errorCodes'() {
        given:
        Map<String, Location> bins = [default: defaultBin, bin: bin, otherBin: otherBin, receiving: receivingBin, hold: holdBin]
        AllocationItemRequest request = allocationRequest(binNames.collect { available(bins[it], 10) }, quantityRequired)

        when:
        service.validateAllocationRequest(request)

        then:
        request.picklistItemCommand.errors.allErrors*.code == errorCodes

        where:
        description                       | binNames              | quantityRequired || errorCodes
        'one bin with enough stock'       | ['bin']               | 5                || []
        'a bin and the default location'  | ['bin', 'default']    | 5                || ['importPickCommand.availableItems.inMultipleBinLocations']
        'two bins'                        | ['bin', 'otherBin']   | 5                || ['importPickCommand.availableItems.inMultipleLocations']
        'hold bins only'                  | ['hold']              | 5                || ['allocationRequest.availableItems.inHoldLocations']
        'one bin without enough stock'    | ['bin']               | 50               || ['importPickCommand.quantity.insuffientQuantityAvailable']
        'nothing available'               | []                    | 5                || ['importPickCommand.quantity.insuffientQuantityAvailable']
    }

    void 'validateQuantityAvailable needs one item with the whole quantity'() {
        expect:
        service.validateQuantityAvailable([available(bin, 3), available(otherBin, 8)], 8)
        !service.validateQuantityAvailable([available(bin, 3), available(otherBin, 4)], 5)
        !service.validateQuantityAvailable([], 1)
    }

    void 'the quantity available to promise adds back what this pick already holds'() {
        given:
        List<AvailableItem> items = [available(bin, 4)]
        PicklistItem pickedHere = new PicklistItem(inventoryItem: lot, binLocation: bin, quantity: 3)
        PicklistItem pickedElsewhere = new PicklistItem(inventoryItem: lot, binLocation: otherBin, quantity: 2)

        when:
        List<AvailableItem> result = service.calculateQuantityAvailableToPromise(items, [pickedHere, pickedElsewhere])

        then:
        result.size() == 2
        result[0].quantityAvailable == 7
        result[1].binLocation == otherBin
        result[1].quantityAvailable == 0
        result[1].quantityOnHand == 0
    }

    void 'suggested items pick from pickable bins in order until the quantity is met'() {
        when:
        List<SuggestedItem> suggested = service.getSuggestedItems([available(holdBin, 5), available(bin, 4), available(otherBin, 10)], 6)

        then:
        suggested*.binLocation == [bin, otherBin]
        suggested*.quantityPicked == [4, 2]

        and: 'nothing is suggested without available stock'
        service.getSuggestedItems([available(bin, 0)], 6) == []
    }

    private static Requisition requisition(RequisitionStatus status, int picklistItems) {
        Requisition requisition = new Requisition(status: status)
        if (picklistItems >= 0) {
            requisition.picklist = new Picklist(picklistItems: (picklistItems > 0 ? (1..picklistItems).collect { new PicklistItem() } : []) as Set)
        }
        return requisition
    }

    @Unroll
    void 'synchronization is refused when #reason'() {
        when:
        service.isSynchronizationAuthorized(stockMovement)

        then:
        IllegalStateException e = thrown()
        e.message.contains(message)

        where:
        reason                               | stockMovement                                                                                  || message
        'it is not an outbound movement'     | new StockMovement(id: 'sm')                                                                    || 'must be an outbound stock movement'
        'it was not issued'                  | new StockMovement(id: 'sm', requisition: requisition(RequisitionStatus.PICKED, 1))             || 'has not been issued'
        'it has no picklist'                 | new StockMovement(id: 'sm', requisition: requisition(RequisitionStatus.ISSUED, -1))            || 'must have a picklist'
        'its picklist is empty'              | new StockMovement(id: 'sm', requisition: requisition(RequisitionStatus.ISSUED, 0))             || 'must have a picklist'
        'its shipment already has items'     | new StockMovement(id: 'sm', requisition: requisition(RequisitionStatus.ISSUED, 1),
                shipment: new Shipment(shipmentItems: [new ShipmentItem()] as Set))                                                                    || 'must not have any shipment items'
        'its shipment has transaction lines' | new StockMovement(id: 'sm', requisition: requisition(RequisitionStatus.ISSUED, 1),
                shipment: new Shipment(outgoingTransactions: [new Transaction(transactionEntries: [new TransactionEntry()] as Set)] as Set)) || 'must not have any transaction entries'
    }

    void 'synchronization is allowed for an issued movement with a picklist and an empty shipment'() {
        expect:
        service.isSynchronizationAuthorized(new StockMovement(id: 'sm', requisition: requisition(RequisitionStatus.ISSUED, 2),
                shipment: new Shipment(shipmentItems: [] as Set)))
    }

    private static Location location(String id, LocationTypeCode typeCode) {
        Location location = new Location(name: id, locationType: new LocationType(name: typeCode.name(), locationTypeCode: typeCode))
        location.id = id
        return location
    }

    @Unroll
    void 'the disabled message for #description is #expected'() {
        given:
        Location depot = location('depot', LocationTypeCode.DEPOT)
        Location otherDepot = location('other', LocationTypeCode.DEPOT)
        Location ward = location('ward', LocationTypeCode.WARD)
        Map<String, Location> locations = [depot: depot, other: otherDepot, ward: ward]
        StockMovement stockMovement = new StockMovement(identifier: 'SM1', origin: locations[origin], destination: locations[destination],
                shipment: status ? new Shipment(currentStatus: status) : null,
                requisition: requisitionStatus ? new Requisition(status: requisitionStatus, sourceType: RequisitionSourceType.PAPER) : null,
                isFromOrder: fromOrder)

        expect:
        service.getDisabledMessage(stockMovement, locations[current], editing) == expected

        where:
        description                                    | origin  | destination | current | status                                | requisitionStatus          | fromOrder | editing || expected
        'editing a received movement'                  | 'depot' | 'other'     | 'other' | ShipmentStatusCode.RECEIVED           | null                       | false     | true    || 'stockMovement.cantEditReceived.message'
        'a pending movement from another depot'        | 'other' | 'depot'     | 'depot' | ShipmentStatusCode.PENDING            | null                       | false     | false   || 'stockMovement.isDifferentOrigin.message'
        'editing at neither end of a ward request'     | 'ward'  | 'other'     | 'depot' | ShipmentStatusCode.SHIPPED            | null                       | false     | true    || 'stockMovement.isDifferentOrigin.message'
        'a received movement'                          | 'depot' | 'other'     | 'other' | ShipmentStatusCode.RECEIVED           | null                       | false     | false   || 'stockMovement.hasAlreadyBeenReceived.message:SM1'
        'a movement that has not shipped'              | 'depot' | 'other'     | 'depot' | ShipmentStatusCode.PENDING            | null                       | false     | false   || 'stockMovement.hasNotBeenShipped.message:SM1'
        'a shipped movement seen from elsewhere'       | 'depot' | 'other'     | 'depot' | ShipmentStatusCode.SHIPPED            | null                       | false     | false   || 'stockMovement.isDifferentLocation.message'
        'a shipped request that was not issued'        | 'depot' | 'other'     | 'other' | ShipmentStatusCode.SHIPPED            | RequisitionStatus.PICKED   | false     | false   || 'stockMovement.hasNotBeenIssued.message:SM1'
        'a partially received issued request'          | 'depot' | 'other'     | 'other' | ShipmentStatusCode.PARTIALLY_RECEIVED | RequisitionStatus.ISSUED   | false     | false   || null
        'a shipment from an order'                     | 'depot' | 'other'     | 'other' | ShipmentStatusCode.SHIPPED            | null                       | true      | false   || null
    }

    void 'the direction of a stock movement depends on the current location'() {
        given:
        Location depot = new Location(name: 'Depot')
        depot.save(validate: false, flush: true)
        Location clinic = location('clinic', LocationTypeCode.WARD)
        new AuthService().setCurrentLocation(depot)

        expect:
        service.getStockMovementDirection(new OutboundStockMovement()) == StockMovementDirection.OUTBOUND
        service.getStockMovementDirection(new StockMovement(origin: depot, destination: clinic)) == StockMovementDirection.OUTBOUND
        service.getStockMovementDirection(new StockMovement(origin: clinic, destination: depot)) == StockMovementDirection.INBOUND
        service.getStockMovementDirection(new StockMovement(origin: clinic, destination: location('ward', LocationTypeCode.WARD))) == null
        service.getStockMovementDirection('not a stock movement') == null
    }

    void 'stock movement lists need a direction; outbound lists come from the outbound service'() {
        given:
        StockMovement outbound = new StockMovement(stockMovementDirection: StockMovementDirection.OUTBOUND)
        Map params = [max: 10]

        when:
        def result = service.getStockMovements(outbound, params)

        then:
        1 * outboundStockMovementService.getStockMovements(outbound, { it.includeStockMovementItems == false }) >> ['listed']
        result == ['listed']

        when:
        service.getStockMovements(new StockMovement(), [:])

        then:
        IllegalArgumentException e = thrown()
        e.message == 'Missing stock movement direction parameter'
    }

    void 'an inbound status other than issued cannot be set'() {
        when:
        service.transitionStockMovement(new StockMovement(id: 'sm'), new org.grails.web.json.JSONObject([status: 'PENDING']))

        then:
        thrown(UnsupportedOperationException)
    }

    void 'the history includes rollbacks, and the latest item is its first'() {
        given:
        StockMovement stockMovement = new StockMovement(id: 'sm')
        HistoryItem latest = new HistoryItem()
        HistoryItem earlier = new HistoryItem()

        when:
        List<HistoryItem> history = service.getHistory(stockMovement)
        HistoryItem latestItem = service.getLatestHistoryItem(stockMovement)
        HistoryItem none = service.getLatestHistoryItem(stockMovement)

        then:
        1 * stockMovementHistoryProvider.getHistory(stockMovement, { HistoryContext c -> c.includeRolledBackEvents && c.limit == null }) >> [latest, earlier]
        1 * stockMovementHistoryProvider.getHistory(stockMovement, { HistoryContext c -> c.limit == 1 }) >> [latest]
        1 * stockMovementHistoryProvider.getHistory(stockMovement, { HistoryContext c -> c.limit == 1 }) >> []
        history == [latest, earlier]
        latestItem.is(latest)
        none == null
    }

    void 'the pack page shows the pallet and the box of a shipment item'() {
        given:
        Container pallet = new Container(name: 'Pallet 1')
        Container box = new Container(name: 'Box 1', parentContainer: pallet)

        expect:
        service.buildPackPageItem(new ShipmentItem(container: box)).with { [palletName, boxName] } == ['Pallet 1', 'Box 1']
        service.buildPackPageItem(new ShipmentItem(container: pallet)).with { [palletName, boxName] } == ['Pallet 1', '']
        service.buildPackPageItem(new ShipmentItem()).with { [palletName, boxName] } == ['', '']
    }

    void 'a box cannot be packed without a pallet'() {
        when:
        service.createOrUpdateContainer(new Shipment(), null, 'Box 1')

        then:
        IllegalArgumentException e = thrown()
        e.message.contains('A box must be contained within a pallet')

        expect:
        service.createOrUpdateContainer(new Shipment(), null, null) == null
    }

    void 'the pack list import reads the id, pack levels and recipient of each row'() {
        given:
        String csv = 'Id,Code,Name,Bin,Lot,Expiry,Qty,UoM,Recipient,Pallet,Box\n' +
                'item-1,P1,Paracetamol,A-01,L1,01/2030,5,EA,Jane Doe,Pallet 1,Box 1\n' +
                'item-2,P2,Gauze,,,,3,EA,,Pallet 2,\n'
        ImportPackListCommand command = new ImportPackListCommand(importFile: new MockMultipartFile('file', 'pack.csv', 'text/csv', csv.bytes))

        when:
        List<ImportPackListItemCommand> rows = service.parsePackCsvTemplateImport(command)

        then:
        rows*.id == ['item-1', 'item-2']
        rows*.packLevel1 == ['Pallet 1', 'Pallet 2']
        rows[0].packLevel2 == 'Box 1'
        rows[0].recipient == 'Jane Doe'
        !rows[1].packLevel2
    }

    void 'a pick list import with semicolons is read with the right separator'() {
        given:
        String csv = 'Id;Code;Name;Lot;Expiry;Bin;Quantity\n' +
                'item-1;P1;Paracetamol;L1;01/01/2030;A-01;5\n' +
                'item-1;P1;Paracetamol;L2;;;2\n'
        PicklistImportDataCommand command = new PicklistImportDataCommand(importFile: new MockMultipartFile('file', 'pick.csv', 'text/csv', csv.bytes))

        when:
        List<PicklistItemCommand> rows = service.parsePickCsvTemplateImport(command)

        then:
        rows*.id == ['item-1', 'item-1']
        rows*.lotNumber == ['L1', 'L2']
        rows[0].binLocation == 'A-01'
        rows[0].expirationDate == new Date('01/01/2030')
        rows[1].expirationDate == null
        rows*.quantity == [5, 2]
    }

    void 'an import without a file fails with a parse error'() {
        when:
        service.parsePackCsvTemplateImport(new ImportPackListCommand())

        then:
        RuntimeException packError = thrown()
        packError.message.startsWith('Error parsing pack template CSV')

        when:
        service.parsePickCsvTemplateImport(new PicklistImportDataCommand())

        then:
        RuntimeException pickError = thrown()
        pickError.message.startsWith('Error parsing order item CSV')
    }
}
