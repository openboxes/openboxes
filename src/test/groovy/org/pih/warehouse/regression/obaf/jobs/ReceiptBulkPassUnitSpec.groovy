package org.pih.warehouse.regression.obaf.jobs

import grails.testing.gorm.DataTest
import spock.lang.Specification

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Location
import org.pih.warehouse.receiving.ReceiptService
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentService

/**
 * ReceiptService.receiveInboundShipments: the AutomaticReceiptJob bulk pass for one AUTO_RECEIVING
 * facility. Each shipped inbound shipment is received as the system user and its backorders are
 * queued for reallocation; one failing shipment must not stop the rest.
 */
class ReceiptBulkPassUnitSpec extends Specification implements DataTest {

    ShipmentService shipmentService = Mock()
    AuthService authService = Mock()
    ReceiptService receipts

    Location facility = new Location(name: 'AR')
    Shipment first = new Shipment(name: 'first')
    Shipment second = new Shipment(name: 'second')

    void setup() {
        first.id = 'first'
        second.id = 'second'
        receipts = Spy(ReceiptService)
        receipts.shipmentService = shipmentService
        receipts.authService = authService
        shipmentService.getShippedShipmentsByDestination(facility) >> [first, second]
    }

    void 'each shipped shipment is received as the system user and its backorders are queued'() {
        when:
        receipts.receiveInboundShipments(facility)

        then:
        2 * authService.withSystemUser(_) >> { Closure c -> c.call() }
        1 * receipts.receiveInboundShipment(first) >> null
        1 * receipts.reallocateBackorderedItems('first') >> null
        1 * receipts.receiveInboundShipment(second) >> null
        1 * receipts.reallocateBackorderedItems('second') >> null
    }

    void 'a failing shipment does not stop the next one from being received'() {
        given:
        authService.withSystemUser(_) >> { Closure c -> c.call() }
        receipts.receiveInboundShipment(first) >> { throw new IllegalStateException('cannot receive') }

        when:
        Throwable failure = null
        try {
            receipts.receiveInboundShipments(facility)
        } catch (Throwable t) {
            failure = t
        }

        then:
        failure == null
        0 * receipts.reallocateBackorderedItems('first')
        1 * receipts.receiveInboundShipment(second) >> null
        1 * receipts.reallocateBackorderedItems('second') >> null
    }
}
