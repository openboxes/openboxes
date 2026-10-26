package org.pih.warehouse.api.regression.mobile

import org.springframework.beans.factory.annotation.Autowired
import spock.lang.PendingFeature

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.requisition.RequisitionType
import org.pih.warehouse.shipping.Container
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentService
import org.pih.warehouse.shipping.ShipmentType

/**
 * Receiving (inbound list, partial receiving in two steps, receiving bin),
 * containers / LPNs (create, read, details, status) and packing (ready-to-pack
 * list, shipment, pack an item). Runs in Test Mobile DC: inbound shipments
 * come from Main Supplier (Location 2), outbound ones go to it.
 */
class ShippingContractSpec extends MobileContractSpec {

    @Autowired
    AuthService authService

    @Autowired
    ShipmentService shipmentService

    @Override
    String sessionLocationId() {
        return regressionDc().id
    }

    static class Ship {
        String id, number, itemId, containerId, unpackedItemId, packedItemId
        Product product
        InventoryItem lot
    }

    InventoryItem newLot(Product p) {
        return InventoryItem.withNewTransaction {
            new InventoryItem(product: Product.get(p.id), lotNumber: "LOT-${uid()}".toString(), expirationDate: new Date() + 365)
                    .save(flush: true, failOnError: true)
        } as InventoryItem
    }

    /** A SHIPPED inbound shipment from Main Supplier to Test Mobile DC, one item on a pallet. */
    Ship inbound() {
        Location dc = regressionDc()
        Ship s = new Ship(product: newProduct('IN'))
        s.lot = newLot(s.product)
        s.number = "IN-${uid()}".toString()
        Shipment.withNewTransaction {
            authService.withSystemUser {
                Shipment shipment = new Shipment(name: "Inbound ${s.number}", shipmentNumber: s.number,
                        origin: Location.get('2'), destination: Location.get(dc.id),
                        expectedShippingDate: new Date() - 1, expectedDeliveryDate: new Date() + 1,
                        shipmentType: ShipmentType.get('5'))
                Container pallet = shipment.addNewPallet("Pallet ${s.number}")
                InventoryItem lot = InventoryItem.get(s.lot.id)
                ShipmentItem item = new ShipmentItem(product: lot.product, inventoryItem: lot, lotNumber: lot.lotNumber,
                        expirationDate: lot.expirationDate, quantity: 10, container: pallet)
                shipment.addToShipmentItems(item)
                shipment.save(flush: true, failOnError: true)
                shipmentService.createShipmentEvent(shipment, new Date(), EventCode.SHIPPED, shipment.origin)
                shipment.save(flush: true, failOnError: true)
                s.id = shipment.id
                s.itemId = item.id
                s.containerId = pallet.id
            }
        }
        return s
    }

    /** An outbound shipment from Test Mobile DC for a PICKED requisition: one LPN with an item, one item unpacked. */
    Ship outbound() {
        Location dc = regressionDc()
        Ship s = new Ship(product: newProduct('OUT'))
        s.lot = newLot(s.product)
        s.number = "OUT-${uid()}".toString()
        Shipment.withNewTransaction {
            authService.withSystemUser {
                Requisition r = new Requisition(name: "Outbound ${s.number}", requestNumber: s.number,
                        origin: Location.get(dc.id), destination: Location.get('2'), requestedBy: authService.currentUser,
                        dateRequested: new Date(), requestedDeliveryDate: new Date() + 1,
                        status: RequisitionStatus.PICKED, type: RequisitionType.DEFAULT)
                r.addToRequisitionItems(new RequisitionItem(product: Product.get(s.product.id), quantity: 8, quantityApproved: 8, substitutable: false))
                r.save(flush: true, failOnError: true)
                Shipment shipment = new Shipment(name: "Outbound ${s.number}", shipmentNumber: s.number,
                        origin: Location.get(dc.id), destination: Location.get('2'), requisition: r,
                        expectedShippingDate: new Date(), expectedDeliveryDate: new Date() + 2, shipmentType: ShipmentType.get('5'))
                Container lpn = shipment.addNewPallet("LPN-${s.number}-00001")
                lpn.containerNumber = "LPN-${s.number}-00001".toString()
                InventoryItem lot = InventoryItem.get(s.lot.id)
                ShipmentItem unpacked = new ShipmentItem(product: lot.product, inventoryItem: lot, lotNumber: lot.lotNumber, quantity: 5)
                ShipmentItem packed = new ShipmentItem(product: lot.product, inventoryItem: lot, lotNumber: lot.lotNumber, quantity: 3, container: lpn)
                shipment.addToShipmentItems(unpacked)
                shipment.addToShipmentItems(packed)
                shipment.save(flush: true, failOnError: true)
                s.id = shipment.id
                s.containerId = lpn.id
                s.unpackedItemId = unpacked.id
                s.packedItemId = packed.id
            }
        }
        return s
    }

    // ------------------------------------------------------------------ receiving (inbound.ts)

    @Covers('src/apis/inbound.ts#fetchInboundOrderList')
    void 'GET /shipments?destination= lists the inbound shipment with its containers'() {
        given:
        Ship s = inbound()

        when:
        CallResult r = call('src/apis/inbound.ts#fetchInboundOrderList', [id: regressionDc().id])

        then:
        r.rows { it.id == s.id }.holds()
        (r.data.data as List<Map>)*.id.contains(s.id)
    }

    @Covers('src/apis/inbound.ts#fetchPartialReceiving')
    void 'GET /partialReceiving/{id} answers the receipt the receive screen reads'() {
        given:
        Ship s = inbound()

        when:
        CallResult r = call('src/apis/inbound.ts#fetchPartialReceiving', [id: s.id])

        then:
        r.holds(['data.containers[].shipmentItems[]["binLocation'])
    }

    @Covers('src/apis/inbound.ts#createReceivingBinLocation')
    void 'POST /partialReceiving/{id}/receivingBinLocation makes the receiving bin'() {
        given:
        Ship s = inbound()

        when:
        CallResult r = call('src/apis/inbound.ts#createReceivingBinLocation', [id: s.id])

        then:
        r.holds()
        r.data.data.name.contains(s.number)
    }

    @Covers('src/apis/inbound.ts#submitPartialReceiving')
    void 'POST /partialReceiving/{id} PENDING then the echoed COMPLETED body receives the shipment'() {
        given:
        Ship s = inbound()
        String binId = call('src/apis/inbound.ts#createReceivingBinLocation', [id: s.id]).data.data.id
        Map receipt = call('src/apis/inbound.ts#fetchPartialReceiving', [id: s.id]).data.data as Map
        Map container = (receipt.containers as List<Map>).find { it['container.id'] == s.containerId }
        Map item = (container.shipmentItems as List<Map>)[0]
        Map pending = [receiptId: '', receiptStatus: 'PENDING', shipmentId: s.id,
                       containers: [[container: [id: s.containerId], shipmentItems: [[
                               receiptItemId: '', shipmentItemId: item.shipmentItemId, product: [id: item['product.id']],
                               binLocation: binId, lotNumber: item.lotNumber, expirationDate: item.expirationDate,
                               recipient: item['recipient.id'], quantityReceiving: 10, cancelRemaining: false,
                               quantityOnHand: '', comment: 'received on the handheld', mobile: true, lotStatusCode: '']]]]]

        when:
        CallResult step1 = call('src/apis/inbound.ts#submitPartialReceiving', [id: s.id], pending)
        // The pending receipt is delivered "now", stored to the second: the database can round that up to a second
        // that has not begun yet, and the completed receipt's transaction would then be refused as dated in the
        // future. A handheld is never that fast; wait until the stored date has passed.
        Date delivered = Shipment.withNewTransaction { Shipment.get(s.id).receipts*.actualDeliveryDate.max() }
        long ahead = (delivered?.time ?: 0L) - System.currentTimeMillis()
        if (ahead >= 0) {
            sleep(ahead + 1)
        }
        Map resp = step1.data.data as Map
        Map completed = [isShipmentFromPurchaseOrder: resp.isShipmentFromPurchaseOrder, receiptStatus: 'COMPLETED',
                         containers: (resp.containers as List<Map>).collect { Map c ->
                             c + [container: [id: c['container.id'], name: c['container.name'], type: c['container.type']]]
                         },
                         dateShipped: resp.dateShipped, description: resp.description,
                         destination: [id: resp['destination.id']], origin: [id: resp['origin.id']],
                         receiptId: resp.receiptId, shipmentStatus: resp.shipmentStatus,
                         shipment: [name: resp['shipment.name'], shipmentNumber: resp['shipment.shipmentNumber']],
                         requisition: resp.requisition, recipient: resp.recipient.id]
        CallResult step2 = call('src/apis/inbound.ts#submitPartialReceiving', [id: s.id], completed)

        then:
        step1.holds()
        step2.status == 200
        Shipment.withNewTransaction { Shipment.get(s.id).currentStatus?.name() } == 'RECEIVED'
    }

    // ------------------------------------------------------------------ containers / LPN (lpn.ts, packing.ts)

    @Covers('src/apis/lpn.ts#saveAndUpdateLpn')
    void 'POST /containers/ with the create LPN body adds a pallet to the shipment'() {
        given:
        Ship s = outbound()
        String name = "LPN-${uid()}".toString()

        when:
        CallResult r = call('src/apis/lpn.ts#saveAndUpdateLpn', [:],
                [name: name, containerNumber: name, 'containerType.id': '2', 'shipment.id': s.id])

        then:
        r.holds()
        Container.withNewTransaction { Container.get(r.data.data.id as String)?.shipment?.id } == s.id
    }

    @Covers(['src/apis/lpn.ts#fetchContainer', 'src/apis/packing.ts#getContainerDetails'])
    void 'GET /generic/container/{id} answers the LPN with its items'() {
        given:
        Ship s = outbound()

        when:
        CallResult lpn = call('src/apis/lpn.ts#fetchContainer', [id: s.containerId])
        CallResult packing = call('src/apis/packing.ts#getContainerDetails', [id: s.containerId])

        then:
        lpn.holds()
        packing.holds()
    }

    @Covers('src/apis/lpn.ts#getAllContainers')
    void 'GET /containers lists containers (inherited list action)'() {
        given:
        Ship s = outbound()

        when:
        CallResult r = call('src/apis/lpn.ts#getAllContainers')

        then:
        r.rows { it.id == s.containerId }.holds()
        (r.data.data as List<Map>)*.id.contains(s.containerId)
    }

    @Covers('src/apis/lpn.ts#getContainerDetail')
    void 'GET /containers/{id}/details answers the LPN with its label URL'() {
        given:
        ensureLabelTemplates()
        Ship s = outbound()

        when:
        CallResult r = call('src/apis/lpn.ts#getContainerDetail', [id: s.containerId])

        then:
        r.holds()
    }

    @Covers('src/apis/lpn.ts#updateContainerStatus')
    void 'POST /containers/{id}/status with the LPN detail and loading bodies'() {
        given:
        Ship s = outbound()

        when:
        CallResult packed = call('src/apis/lpn.ts#updateContainerStatus', [id: s.containerId], [status: 'PACKED'])
        CallResult loaded = call('src/apis/lpn.ts#updateContainerStatus', [id: s.containerId], [status: 'LOADED', loadingLocation: 'DOCK-1'])

        then:
        packed.holds()
        loaded.holds()
        Container.withNewTransaction { Container.get(s.containerId).containerStatus?.name() } == 'LOADED'
    }

    @Covers('src/apis/packing.ts#getContainerType')
    void 'GET generic/containerType answers the container types (saga wired, never dispatched)'() {
        when:
        CallResult r = call('src/apis/packing.ts#getContainerType')

        then:
        r.holds()
    }

    // ------------------------------------------------------------------ packing (packing.ts)

    @Covers('src/apis/packing.ts#getShipmentsReadyToBePacked')
    void 'GET /shipments?origin&shipmentStatusCode&requisitionStatus lists the shipment to pack'() {
        given:
        Ship s = outbound()

        when:
        CallResult r = call('src/apis/packing.ts#getShipmentsReadyToBePacked', [locationId: regressionDc().id, shipmentStatusCode: 'PENDING'])

        then:
        r.rows { it.id == s.id }.holds()
        (r.data.data as List<Map>)*.id.contains(s.id)
    }

    @Covers('src/apis/packing.ts#getShipmentOrigin')
    void 'GET /shipments/?origin= lists the outbound shipments'() {
        given:
        Ship s = outbound()

        when:
        CallResult r = call('src/apis/packing.ts#getShipmentOrigin', [id: regressionDc().id])

        then:
        r.rows { it.id == s.id }.holds()
        (r.data.data as List<Map>)*.id.contains(s.id)
    }

    @Covers('src/apis/packing.ts#getShipment')
    void 'GET /shipments/{id} answers the shipment the pack screen reads (all but the known gaps)'() {
        given:
        Ship s = outbound()

        when:
        CallResult r = call('src/apis/packing.ts#getShipment', [id: s.id])

        then:
        r.holds([], ['data.availableContainers', 'data.containers[].shipmentItems[].quantityRemaining'])
    }

    @Covers('src/apis/packing.ts#getShipment')
    @PendingFeature(reason = 'APP/SERVER MISMATCH: src/screens/ShipItemDetails/index.tsx maps data.availableContainers unguarded and seeds the pack quantity from containers[].shipmentItems[].quantityRemaining; OBAF Shipment.toJson (and the BootStrap ShipmentItem marshaller) send neither, so the pack screen throws a TypeError and would default quantityToPack to 0')
    void 'GET /shipments/{id} sends availableContainers and the item quantityRemaining'() {
        given:
        Ship s = outbound()

        when:
        CallResult r = call('src/apis/packing.ts#getShipment', [id: s.id])

        then:
        r.holds()
    }

    @Covers('src/apis/packing.ts#submitShipmentItems')
    void 'POST /shipments/{shipmentItemId} PACK moves the item into an empty LPN'() {
        given:
        Ship s = outbound()
        String name = "LPN-${uid()}".toString()
        String lpnId = call('src/apis/lpn.ts#saveAndUpdateLpn', [:],
                [name: name, containerNumber: name, 'containerType.id': '2', 'shipment.id': s.id]).data.data.id

        when:
        CallResult r = call('src/apis/packing.ts#submitShipmentItems', [id: s.unpackedItemId],
                [action: 'PACK', 'container.id': lpnId, quantityToPack: 5])

        then:
        r.holds()
        ShipmentItem.withNewTransaction { ShipmentItem.get(s.unpackedItemId)?.container?.id } == lpnId
    }

    @Covers('src/apis/packing.ts#submitShipmentItems')
    @PendingFeature(reason = 'SERVER DEFECT: packing an item into an LPN that already holds the same lot (src/screens/ShipItemDetails, POST /shipments/{shipmentItemId} PACK) answers 200 but ShipmentService.packItem merges the quantity into the SOURCE item, deletes the LPN item and never sets the container, so the stock stays unpacked')
    void 'POST /shipments/{shipmentItemId} PACK into an LPN holding the same lot ends up in that LPN'() {
        given:
        Ship s = outbound()

        when:
        CallResult r = call('src/apis/packing.ts#submitShipmentItems', [id: s.unpackedItemId],
                [action: 'PACK', 'container.id': s.containerId, quantityToPack: 5])
        List<Map> inLpn = ShipmentItem.withNewTransaction {
            ShipmentItem.findAllByContainer(Container.get(s.containerId)).collect { [id: it.id, quantity: it.quantity] }
        } as List<Map>

        then:
        r.holds()
        inLpn*.quantity.sum() == 8
    }
}
