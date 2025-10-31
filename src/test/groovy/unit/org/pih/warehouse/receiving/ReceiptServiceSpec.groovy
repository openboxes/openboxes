package unit.org.pih.warehouse.receiving

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.pih.warehouse.api.PartialReceipt
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.receiving.ReceiptService
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentService
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class ReceiptServiceSpec extends Specification implements ServiceUnitTest<ReceiptService>, DataTest {

    @Shared
    ShipmentService shipmentService

    void setupSpec() {
        mockDomains(Receipt, Shipment)
    }

    void setup() {
        shipmentService = Mock(ShipmentService) {
            createShipmentEvent(_, _, _, _) >> void
        }
        service.shipmentService = shipmentService
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
}
