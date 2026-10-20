package org.pih.warehouse.api.regression.obaf.receiving

import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.PartialReceipt
import org.pih.warehouse.api.regression.obaf.support.ObafTaskIntegrationSpec
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.receiving.ReceiptService
import org.pih.warehouse.receiving.ReceiptStatusCode
import org.pih.warehouse.shipping.Container
import org.pih.warehouse.shipping.ContainerType
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentStatusCode

/**
 * Receipt automation on a real database: ReceiptService.receiveInboundShipment (the
 * AutomaticReceiptJob single-shipment path and, with forceAutoReceipt, the manual "auto-receive"
 * button), createAutomaticReceipt / findReceivingLocation, and the bulk pass
 * receiveInboundShipments (its error isolation is covered in ReceiptBulkPassUnitSpec).
 *
 * Each receipt here runs with no current user bound, as on the job path once withSystemUser has
 * restored the (absent) job user (see InboundSortationIntegrationSpec for why that matters).
 */
class AutoReceiptIntegrationSpec extends ObafTaskIntegrationSpec {

    @Autowired
    ReceiptService receiptService

    void receive(String shipmentId, boolean force = false) {
        actAs(null)
        try {
            tx { receiptService.receiveInboundShipment(Shipment.get(shipmentId), force) }
        } finally {
            actAs('admin')
        }
    }

    String shipmentStatus(String shipmentId) {
        return tx { Shipment.get(shipmentId).currentStatus?.name() } as String
    }

    /** Receipts of the shipment as [status, [[binPrefix, quantityReceived]...]] in creation order. */
    List<List> receipts(String shipmentId) {
        return tx {
            Shipment.get(shipmentId).receipts.collect { Receipt r ->
                [r.receiptStatusCode.name(), r.receiptItems.collect { [prefixOf(it.binLocation?.name), it.quantityReceived] }.sort { a, b -> (a[0] <=> b[0]) ?: (a[1] <=> b[1]) }]
            }
        } as List<List>
    }

    void 'into the facility sortation bin: the whole shipment is received and RECEIVED'() {
        given:
        String dc = depot('AR-SORT', [ActivityCode.AUTO_RECEIVING])
        bin(dc, 'SORT', [ActivityCode.INBOUND_SORTATION, ActivityCode.RECEIVE_STOCK], false)
        String p = product('AR-SORT')
        String shipment = inboundShipment(dc, [[productId: p, quantity: 4, lotNumber: 'A'], [productId: p, quantity: 6, lotNumber: 'B']])

        when:
        receive(shipment)

        then:
        shipmentStatus(shipment) == 'RECEIVED'
        receipts(shipment) == [['RECEIVED', [['SORT', 4], ['SORT', 6]]]]
        onHand(dc, p) == [SORT: 10]
    }

    void 'no sortation bin: a temporary receiving bin R-<shipment number> is created and used'() {
        given:
        String dc = depot('AR-TMP', [ActivityCode.AUTO_RECEIVING])
        String p = product('AR-TMP')
        String shipment = inboundShipment(dc, [[productId: p, quantity: 3, lotNumber: 'A']])
        String shipmentNumber = tx { Shipment.get(shipment).shipmentNumber } as String

        when:
        receive(shipment)

        then:
        shipmentStatus(shipment) == 'RECEIVED'
        tx { Shipment.get(shipment).receipts.first().receiptItems*.binLocation*.name } == ["R-${shipmentNumber}".toString()]
        tx { Location.findByParentLocationAndName(Location.get(dc), "R-${shipmentNumber}".toString()).locationType.name } == 'Receiving'
    }

    void 'not received: a shipment that has not shipped, or a facility without AUTO_RECEIVING (unless forced)'() {
        given:
        String auto = depot('AR-NS', [ActivityCode.AUTO_RECEIVING])
        String plain = depot('AR-PL')
        String p = product('AR-NS')
        String unshipped = inboundShipment(auto, [[productId: p, quantity: 2, lotNumber: 'A']], false)
        String toPlain = inboundShipment(plain, [[productId: p, quantity: 2, lotNumber: 'A']])

        when:
        receive(unshipped)
        receive(toPlain)

        then:
        receipts(unshipped) == []
        receipts(toPlain) == []
        shipmentStatus(toPlain) == 'SHIPPED'

        when: 'the manual auto-receive button forces it'
        receive(toPlain, true)

        then:
        shipmentStatus(toPlain) == 'RECEIVED'
    }

    void 'a fully received shipment is not received again; a pending receipt blocks the automatic one'() {
        given:
        String dc = depot('AR-ID', [ActivityCode.AUTO_RECEIVING])
        String sort = bin(dc, 'SORT', [ActivityCode.INBOUND_SORTATION, ActivityCode.RECEIVE_STOCK], false)
        String p = product('AR-ID')
        String done = inboundShipment(dc, [[productId: p, quantity: 2, lotNumber: 'A']])
        String pending = inboundShipment(dc, [[productId: p, quantity: 3, lotNumber: 'B']])
        receive(done)
        tx {
            PartialReceipt pr = receiptService.getPartialReceipt(pending, '1')
            pr.partialReceiptItems.each { it.quantityReceiving = 1; it.binLocation = Location.get(sort); it.shouldSave = true }
            receiptService.savePartialReceipt(pr, false)
        }

        when:
        receive(done)
        receive(pending)

        then:
        receipts(done).size() == 1
        receipts(pending) == [['PENDING', [['SORT', 1]]]]
        onHand(dc, p) == [SORT: 2]
    }

    void 'a partly received shipment: only what remains is received'() {
        given:
        String dc = depot('AR-PART', [ActivityCode.AUTO_RECEIVING, ActivityCode.PARTIAL_RECEIVING])
        String sort = bin(dc, 'SORT', [ActivityCode.INBOUND_SORTATION, ActivityCode.RECEIVE_STOCK], false)
        String p = product('AR-PART')
        String shipment = inboundShipment(dc, [[productId: p, quantity: 10, lotNumber: 'A']])
        tx {
            PartialReceipt pr = receiptService.getPartialReceipt(shipment, '1')
            pr.partialReceiptItems.each { it.quantityReceiving = 4; it.binLocation = Location.get(sort); it.shouldSave = true }
            receiptService.saveAndCompletePartialReceipt(pr)
        }
        assert shipmentStatus(shipment) == 'PARTIALLY_RECEIVED'

        when:
        receive(shipment)

        then:
        shipmentStatus(shipment) == 'RECEIVED'
        receipts(shipment) == [['RECEIVED', [['SORT', 4]]], ['RECEIVED', [['SORT', 6]]]]
        onHand(dc, p) == [SORT: 10]
    }

    void 'items packed in an LPN are received once, into the sortation bin'() {
        given:
        String dc = depot('AR-LPN', [ActivityCode.AUTO_RECEIVING])
        bin(dc, 'SORT', [ActivityCode.INBOUND_SORTATION, ActivityCode.RECEIVE_STOCK], false)
        String p = product('AR-LPN')
        String shipment = inboundShipment(dc, [[productId: p, quantity: 5, lotNumber: 'A'], [productId: p, quantity: 2, lotNumber: 'B']])
        tx {
            Shipment s = Shipment.get(shipment)
            Container c = new Container(name: 'LPN-1', containerNumber: 'LPN-1', containerType: ContainerType.get(Constants.PALLET_CONTAINER_TYPE_ID))
            s.addToContainers(c)
            s.save(flush: true, failOnError: true)
            ShipmentItem packed = s.shipmentItems.find { it.quantity == 5 }
            packed.container = c
            s.save(flush: true, failOnError: true)
        }

        when:
        receive(shipment)

        then:
        shipmentStatus(shipment) == 'RECEIVED'
        receipts(shipment) == [['RECEIVED', [['SORT', 2], ['SORT', 5]]]]
        onHand(dc, p) == [SORT: 7]
    }

    void 'CROSS_DOCKING facility: an unknown backorder reference stops the receipt and is logged on the shipment'() {
        given:
        String dc = depot('AR-XD', [ActivityCode.AUTO_RECEIVING, ActivityCode.CROSS_DOCKING])
        bin(dc, 'SORT', [ActivityCode.INBOUND_SORTATION, ActivityCode.RECEIVE_STOCK], false)
        String p = product('AR-XD')
        String shipment = inboundShipment(dc, [[productId: p, quantity: 5, lotNumber: 'A', backorderReference: 'NO-SUCH-BACKORDER']])

        when:
        receive(shipment)
        receive(shipment)

        then: 'logged once, however often it is retried'
        receipts(shipment) == []
        shipmentStatus(shipment) == 'SHIPPED'
        tx { Shipment.get(shipment).comments*.comment } == ['Backorder NO-SUCH-BACKORDER not found']
    }

    void 'bulk pass: every shipped inbound shipment to the facility is received; unshipped and already received ones are left alone'() {
        given:
        String dc = depot('AR-BULK', [ActivityCode.AUTO_RECEIVING])
        bin(dc, 'SORT', [ActivityCode.INBOUND_SORTATION, ActivityCode.RECEIVE_STOCK], false)
        String p = product('AR-BULK')
        String a = inboundShipment(dc, [[productId: p, quantity: 1, lotNumber: 'A']])
        String b = inboundShipment(dc, [[productId: p, quantity: 2, lotNumber: 'A']])
        String notShipped = inboundShipment(dc, [[productId: p, quantity: 4, lotNumber: 'A']], false)
        String received = inboundShipment(dc, [[productId: p, quantity: 8, lotNumber: 'A']])
        receive(received)

        when:
        tx { receiptService.receiveInboundShipments(Location.get(dc)) }

        then:
        [a, b, notShipped, received].collect { shipmentStatus(it) } == ['RECEIVED', 'RECEIVED', 'PENDING', 'RECEIVED']
        receipts(received).size() == 1
        onHand(dc, p) == [SORT: 11]
    }
}
