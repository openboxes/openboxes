package unit.org.pih.warehouse.core

import org.pih.warehouse.inventory.StockMovementStatusCode
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentStatus
import org.pih.warehouse.shipping.ShipmentStatusCode
import spock.lang.Specification
import spock.lang.Unroll
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.api.StockMovementStatusContext
import util.StockMovementStatusResolver

@Unroll
class StockMovementStatusResolverSpec extends Specification {

    void "should return requisition status #expected for stock movement returns if shipment is not shipped yet"() {
        given:
        StockMovementStatusContext context = Stub(StockMovementStatusContext) {
            isReturn() >> true
            isInbound() >> false
            isOutbound() >> true
            getOrder() >> new Order(status: orderStatus)
            getShipment() >> Stub(Shipment) {
                hasShipped() >> false
            }
        }

        expect:
        StockMovementStatusResolver.getStatus(context) == expected

        where:
        orderStatus                     ||  expected
        OrderStatus.PENDING             ||  RequisitionStatus.CREATED
        OrderStatus.PLACED              ||  RequisitionStatus.CHECKING
        OrderStatus.APPROVED            ||  RequisitionStatus.PICKING
        OrderStatus.CANCELED            ||  RequisitionStatus.CANCELED
        OrderStatus.REJECTED            ||  RequisitionStatus.CANCELED

    }

    void "should return shipment status #expected for stock movement returns if shipment is shipped"() {
        given:
        StockMovementStatusContext context = Stub(StockMovementStatusContext) {
            isReturn() >> true
            isInbound() >> false
            isOutbound() >> true
            getOrder() >> new Order(status: orderStatus)
            getShipment() >> Stub(Shipment) {
                getStatus() >> new ShipmentStatus(code: shipmentStatus)
                hasShipped() >> true
            }
        }

        expect:
        StockMovementStatusResolver.getStatus(context) == expected

        where:
        orderStatus                     ||  shipmentStatus                          || expected
        OrderStatus.PARTIALLY_RECEIVED  ||  ShipmentStatusCode.PARTIALLY_RECEIVED   || ShipmentStatusCode.PARTIALLY_RECEIVED
        OrderStatus.RECEIVED            ||  ShipmentStatusCode.RECEIVED             || ShipmentStatusCode.RECEIVED
        OrderStatus.COMPLETED           ||  ShipmentStatusCode.SHIPPED              || ShipmentStatusCode.SHIPPED

    }



    void "should return shipment status #expected for stock movements from purchase order"() {
        given:
        StockMovementStatusContext context = Stub(StockMovementStatusContext) {
            isReturn() >> false
            isInbound() >> true
            isOutbound() >> false
            isFromPurchaseOrder() >> true
            getShipment() >> Stub(Shipment) {
                getStatus() >> new ShipmentStatus(code: status)
            }
        }

        expect:
        StockMovementStatusResolver.getStatus(context) == expected

        where:
        status                                  || expected
        ShipmentStatusCode.PENDING              || ShipmentStatusCode.PENDING
        ShipmentStatusCode.SHIPPED              || ShipmentStatusCode.SHIPPED
        ShipmentStatusCode.PARTIALLY_RECEIVED   || ShipmentStatusCode.PARTIALLY_RECEIVED
        ShipmentStatusCode.RECEIVED             || ShipmentStatusCode.RECEIVED
    }

    void "should return requisition status #expected if shipment is not yet shipped"() {
        given:
        StockMovementStatusContext context = Stub(StockMovementStatusContext) {
            isReturn() >> false
            isInbound() >> true
            isOutbound() >> false
            isFromPurchaseOrder() >> false
            getShipment() >> Stub(Shipment) {
                getStatus() >> new ShipmentStatus(code: shipmentStatus)
                hasShipped() >> false
            }
            getRequisition() >> new Requisition(status: requisitionStatus)
        }

        expect:
        StockMovementStatusResolver.getStatus(context) == expected

        where:
        requisitionStatus           ||  shipmentStatus              || expected
        RequisitionStatus.CREATED   ||  ShipmentStatusCode.SHIPPED  || RequisitionStatus.CREATED
        RequisitionStatus.EDITING   ||  ShipmentStatusCode.SHIPPED  || RequisitionStatus.EDITING
        RequisitionStatus.VERIFYING ||  ShipmentStatusCode.SHIPPED  || RequisitionStatus.VERIFYING
        RequisitionStatus.PICKING   ||  ShipmentStatusCode.SHIPPED  || RequisitionStatus.PICKING
        RequisitionStatus.PICKED    ||  ShipmentStatusCode.SHIPPED  || RequisitionStatus.PICKED
    }

    void "should return shipment status #expected if shipment is shipped"() {
        given:
        StockMovementStatusContext context = Stub(StockMovementStatusContext) {
            isReturn() >> false
            isInbound() >> true
            isOutbound() >> false
            isFromPurchaseOrder() >> false
            getShipment() >> Stub(Shipment) {
                getStatus() >> new ShipmentStatus(code: shipmentStatus)
                hasShipped() >> true
            }
            getRequisition() >> new Requisition(status: requisitionStatus)
        }

        expect:
        StockMovementStatusResolver.getStatus(context) == expected

        where:
        requisitionStatus           || shipmentStatus                           || expected
        RequisitionStatus.ISSUED    || ShipmentStatusCode.SHIPPED               || ShipmentStatusCode.SHIPPED
        RequisitionStatus.ISSUED    || ShipmentStatusCode.RECEIVED              || ShipmentStatusCode.RECEIVED
        RequisitionStatus.ISSUED    || ShipmentStatusCode.PARTIALLY_RECEIVED    || ShipmentStatusCode.PARTIALLY_RECEIVED
    }

    void "should return shipment status #expected for list items on inbound stock movement returns"() {
        given:
        StockMovementStatusContext context = Stub(StockMovementStatusContext) {
            isReturn() >> true
            isInbound() >> true
            isOutbound() >> false
            getShipment() >> Stub(Shipment) {
                getStatus() >> new ShipmentStatus(code: status)
            }
        }

        expect:
        StockMovementStatusResolver.getListStatus(context) == expected

        where:
        status                                  || expected
        ShipmentStatusCode.PENDING              || ShipmentStatusCode.PENDING
        ShipmentStatusCode.SHIPPED              || ShipmentStatusCode.SHIPPED
        ShipmentStatusCode.PARTIALLY_RECEIVED   || ShipmentStatusCode.PARTIALLY_RECEIVED
        ShipmentStatusCode.RECEIVED             || ShipmentStatusCode.RECEIVED
    }

    void "should return shipment status #expected for list items on outbound stock movement returns"() {
        given:
        StockMovementStatusContext context = Stub(StockMovementStatusContext) {
            isReturn() >> true
            isInbound() >> false
            isOutbound() >> true
            getOrder() >> new Order(status: orderStatus)
        }

        expect:
        StockMovementStatusResolver.getListStatus(context) == expected

        where:
        orderStatus                     || expected
        OrderStatus.PENDING             || RequisitionStatus.CREATED
        OrderStatus.PLACED              || RequisitionStatus.CHECKING
        OrderStatus.APPROVED            || RequisitionStatus.PICKING
        OrderStatus.CANCELED            || RequisitionStatus.CANCELED
        OrderStatus.CANCELED            || RequisitionStatus.CANCELED
        OrderStatus.PARTIALLY_RECEIVED  || RequisitionStatus.ISSUED
        OrderStatus.RECEIVED            || RequisitionStatus.ISSUED
        OrderStatus.COMPLETED           || RequisitionStatus.ISSUED
        OrderStatus.REJECTED            || RequisitionStatus.CANCELED
    }

    void "should return shipment status #expected for list items on inbound stock requests"() {
        given:
        StockMovementStatusContext context = Stub(StockMovementStatusContext) {
            isReturn() >> false
            isInbound() >> true
            isOutbound() >> false
            isElectronicType() >> true
            isCurrentLocationDownstreamConsumer() >> false
            getShipment() >> Stub(Shipment) {
                getStatus() >> new ShipmentStatus(code: ShipmentStatusCode.SHIPPED)
            }
            getRequisition() >> new Requisition(status: status)
        }

        expect:
        StockMovementStatusResolver.getListStatus(context) == expected

        where:
        status                              || expected
        RequisitionStatus.APPROVED          || ShipmentStatusCode.SHIPPED
        RequisitionStatus.REJECTED          || ShipmentStatusCode.SHIPPED
        RequisitionStatus.VERIFYING         || ShipmentStatusCode.SHIPPED
        RequisitionStatus.PENDING_APPROVAL  || ShipmentStatusCode.SHIPPED
    }

    void "should return approval status #expected for list items on inbound stock requests in a downstream consumer location"() {
        given:
        StockMovementStatusContext context = Stub(StockMovementStatusContext) {
            isReturn() >> false
            isInbound() >> true
            isOutbound() >> false
            isElectronicType() >> true
            isCurrentLocationDownstreamConsumer() >> true
            getShipment() >> Stub(Shipment) {
                getStatus() >> new ShipmentStatus(code: ShipmentStatusCode.SHIPPED)
            }
            getRequisition() >> new Requisition(status: status)
        }

        expect:
        StockMovementStatusResolver.getListStatus(context) == expected

        where:
        status                              || expected
        RequisitionStatus.APPROVED          || StockMovementStatusCode.APPROVED
        RequisitionStatus.REJECTED          || StockMovementStatusCode.REJECTED
        RequisitionStatus.PENDING_APPROVAL  || StockMovementStatusCode.PENDING_APPROVAL
    }

    void "should return status #expected for any non-approval list items on inbound stock requests in a downstream consumer location"() {
        given:
        StockMovementStatusContext context = Stub(StockMovementStatusContext) {
            isReturn() >> false
            isInbound() >> true
            isOutbound() >> false
            isElectronicType() >> true
            isCurrentLocationDownstreamConsumer() >> true
            getShipment() >> Stub(Shipment) {
                getStatus() >> new ShipmentStatus(code: ShipmentStatusCode.SHIPPED)
            }
            getRequisition() >> new Requisition(status: status)
        }

        expect:
        StockMovementStatusResolver.getListStatus(context) == expected

        where:
        status                                 || expected
        RequisitionStatus.VERIFYING            || StockMovementStatusCode.IN_PROGRESS
        RequisitionStatus.PICKING              || StockMovementStatusCode.IN_PROGRESS
        RequisitionStatus.PICKED               || StockMovementStatusCode.IN_PROGRESS
        RequisitionStatus.CHECKING             || StockMovementStatusCode.IN_PROGRESS
    }
}
