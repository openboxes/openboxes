package org.pih.warehouse.receiving

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.pih.warehouse.api.PartialReceipt
import org.pih.warehouse.api.receiving.v2.ReceiptV2Service
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.receiving.ReceiptService
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentService
import org.pih.warehouse.shipping.ShipmentType
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class ReceiptServiceSpec extends Specification implements ServiceUnitTest<ReceiptService>, DataTest {

    @Shared
    ShipmentService shipmentService

    void setupSpec() {
        mockDomains(Receipt, ReceiptV2Marker, Shipment, ShipmentType, Location)
    }

    void setup() {
        shipmentService = Mock(ShipmentService) {
            createShipmentEvent(_, _, _, _) >> void
        }
        service.shipmentService = shipmentService
        // The real service, not a mock: the rollbacks are expected to actually delete the markers.
        service.receiptV2Service = new ReceiptV2Service()
    }

    void 'savePartialReceiptEvent should create RECEIVED event when partial receiving is not supported'() {
        given:
        Location location = Stub(Location) {
            supports(ActivityCode.PARTIAL_RECEIVING) >> false
        }

        Shipment shipment = new Shipment()
        shipment.wasReceived() >> false
        shipment.destination = location

        PartialReceipt partialReceipt = Spy(PartialReceipt)
        partialReceipt.shipment = shipment
        partialReceipt.receipt = new Receipt(actualDeliveryDate: new Date())

        when:
        service.savePartialReceiptEvent(partialReceipt)

        then:
        1 * shipmentService.createShipmentEvent(_, _, EventCode.RECEIVED, _)
        0 * shipmentService.createShipmentEvent(_, _, EventCode.PARTIALLY_RECEIVED, _)
    }

    void 'savePartialReceiptEvent should create RECEIVED event when receiving fully'() {
        given:
        Location location = Stub(Location) {
            supports(ActivityCode.PARTIAL_RECEIVING) >> true
        }

        Shipment shipment = Stub(Shipment) {
            wasReceived() >> false
            isFullyReceived() >> true
            getDestination() >> location
        }

        PartialReceipt partialReceipt = Spy(PartialReceipt)
        partialReceipt.shipment = shipment
        partialReceipt.receipt = new Receipt(actualDeliveryDate: new Date())

        when:
        service.savePartialReceiptEvent(partialReceipt)

        then:
        1 * shipmentService.createShipmentEvent(_, _, EventCode.RECEIVED, _)
        0 * shipmentService.createShipmentEvent(_, _, EventCode.PARTIALLY_RECEIVED, _)
    }

    void 'savePartialReceiptEvent should create PARTIALLY_RECEIVED event when receiving partially'() {
        given:
        Location location = Stub(Location) {
            supports(ActivityCode.PARTIAL_RECEIVING) >> true
        }

        Shipment shipment = Stub(Shipment) {
            wasReceived() >> true
            isFullyReceived() >> false
            wasPartiallyReceived() >> false
            getDestination() >> location
        }

        PartialReceipt partialReceipt = Spy(PartialReceipt)
        partialReceipt.shipment = shipment
        partialReceipt.receipt = new Receipt(actualDeliveryDate: new Date())

        when:
        service.savePartialReceiptEvent(partialReceipt)

        then:
        0 * shipmentService.createShipmentEvent(_, _, EventCode.RECEIVED, _)
        1 * shipmentService.createShipmentEvent(_, _, EventCode.PARTIALLY_RECEIVED, _)
    }

    void 'savePartialReceiptEvent should create RECEIVED event when fully receiving shipment that was partially received'() {
        given:
        Location location = Stub(Location) {
            supports(ActivityCode.PARTIAL_RECEIVING) >> true
        }

        Shipment shipment = Stub(Shipment) {
            wasReceived() >> false
            isFullyReceived() >> true
            wasPartiallyReceived() >> true
            getDestination() >> location
        }

        PartialReceipt partialReceipt = Spy(PartialReceipt)
        partialReceipt.shipment = shipment
        partialReceipt.receipt = new Receipt(actualDeliveryDate: new Date())

        when:
        service.savePartialReceiptEvent(partialReceipt)

        then:
        1 * shipmentService.createShipmentEvent(_, _, EventCode.RECEIVED, _)
        0 * shipmentService.createShipmentEvent(_, _, EventCode.PARTIALLY_RECEIVED, _)
    }

    void 'savePartialReceiptEvent should create no events when was partially received and still is'() {
        given:
        Location location = Stub(Location) {
            supports(ActivityCode.PARTIAL_RECEIVING) >> true
        }

        Shipment shipment = Stub(Shipment) {
            wasReceived() >> false
            isFullyReceived() >> false
            wasPartiallyReceived() >> true
            getDestination() >> location
        }

        PartialReceipt partialReceipt = Spy(PartialReceipt)
        partialReceipt.shipment = shipment
        partialReceipt.receipt = new Receipt(actualDeliveryDate: new Date())

        when:
        service.savePartialReceiptEvent(partialReceipt)

        then:
        0 * shipmentService.createShipmentEvent(_, _, EventCode.RECEIVED, _)
        0 * shipmentService.createShipmentEvent(_, _, EventCode.PARTIALLY_RECEIVED, _)
    }

    void 'rollbackLastReceipt should delete the v2 marker of the receipt it rolls back'() {
        given: 'a received receipt created by the v2 workflow'
        Shipment shipment = buildShipmentWithReceipts(ReceiptStatusCode.RECEIVED)
        Receipt receipt = shipment.receipts.first()
        new ReceiptV2Marker(receipt: receipt).save(failOnError: true, flush: true)

        when:
        service.rollbackLastReceipt(shipment)

        then: 'the marker goes with it - its foreign key would otherwise block the deletion of the receipt'
        assert ReceiptV2Marker.count() == 0
        assert Receipt.count() == 0
    }

    void 'rollbackLastReceipt should roll back a receipt of the old workflow, which carries no marker'() {
        given:
        Shipment shipment = buildShipmentWithReceipts(ReceiptStatusCode.RECEIVED)

        when:
        service.rollbackLastReceipt(shipment)

        then:
        assert Receipt.count() == 0
    }

    void 'rollbackPartialReceipts should delete the v2 marker of every receipt it rolls back'() {
        given: 'a completed and a pending receipt, both created by the v2 workflow'
        Shipment shipment = buildShipmentWithReceipts(ReceiptStatusCode.RECEIVED, ReceiptStatusCode.PENDING)
        shipment.receipts.each { Receipt receipt ->
            new ReceiptV2Marker(receipt: receipt).save(failOnError: true, flush: true)
        }

        when:
        service.rollbackPartialReceipts(shipment)

        then:
        assert ReceiptV2Marker.count() == 0
        assert Receipt.count() == 0
    }

    /**
     * A saved shipment carrying one receipt per given status - the minimal graph the rollbacks walk.
     */
    private static Shipment buildShipmentWithReceipts(ReceiptStatusCode... statusCodes) {
        Shipment shipment = new Shipment(
                name: "Test shipment",
                origin: new Location(name: "Origin"),
                destination: new Location(name: "Destination"),
                expectedShippingDate: new Date() - 7,
                shipmentType: new ShipmentType(name: "Default"),
        )
        // The rollbacks walk this collection, which the domain declares (as a SortedSet) without initializing it.
        shipment.events = new TreeSet()
        statusCodes.each { ReceiptStatusCode statusCode ->
            shipment.addToReceipts(new Receipt(receiptStatusCode: statusCode, actualDeliveryDate: new Date() - 1))
        }
        shipment.save(failOnError: true, flush: true)
        return shipment
    }
}
