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
import util.StockMovementContext
import util.StockMovementStatusHelper

@Unroll
class StockMovementSpec extends Specification {

    void "should return shipment status #expected for inbound stock movement returns"() {
        given:
        StockMovementContext context = new StockMovementContext() {
            @Override
            boolean isReturn() {
                return true
            }
            @Override
            boolean isInbound() {
                return true
            }
            @Override
            boolean isOutbound() {
                return false
            }
        }
        when:
        ShipmentStatusCode shipmentStatusCode = status
        context.shipment = new Shipment() {
            @Override
            ShipmentStatus getStatus() {
                return new ShipmentStatus(code: shipmentStatusCode)
            }
        }

        then:
        StockMovementStatusHelper.getStatus(context) == expected

        where:
        status || expected
        ShipmentStatusCode.PENDING              ||  ShipmentStatusCode.PENDING
        ShipmentStatusCode.SHIPPED              ||  ShipmentStatusCode.SHIPPED
        ShipmentStatusCode.PARTIALLY_RECEIVED   ||  ShipmentStatusCode.PARTIALLY_RECEIVED
        ShipmentStatusCode.RECEIVED             ||  ShipmentStatusCode.RECEIVED
    }

    void "should return requisition status #expected for outbound stock movement returns"() {
        given:
        StockMovementContext context = new StockMovementContext() {
            @Override
            boolean isReturn() {
                return true
            }
            @Override
            boolean isInbound() {
                return false
            }
            @Override
            boolean isOutbound() {
                return true
            }
        }
        when:
        context.order = new Order(status: orderStatus)

        then:
        StockMovementStatusHelper.getStatus(context) == expected

        where:
        orderStatus                     ||  expected
        OrderStatus.PENDING             ||  RequisitionStatus.CREATED
        OrderStatus.PLACED              ||  RequisitionStatus.CREATED
        OrderStatus.APPROVED            ||  RequisitionStatus.PICKING
        OrderStatus.CANCELED            ||  RequisitionStatus.CANCELED
        OrderStatus.PARTIALLY_RECEIVED  ||  RequisitionStatus.ISSUED
        OrderStatus.RECEIVED            ||  RequisitionStatus.ISSUED
        OrderStatus.COMPLETED           ||  RequisitionStatus.ISSUED
        OrderStatus.REJECTED            ||  RequisitionStatus.CANCELED

    }

    void "should return shipment status #expected for stock movements from purchase order"() {
        given:
        StockMovementContext context = new StockMovementContext() {
            @Override
            boolean isReturn() {
                return false
            }
            @Override
            boolean isInbound() {
                return true
            }
            @Override
            boolean isOutbound() {
                return false
            }
            @Override
            Boolean isFromPurchaseOrder() {
                return true
            }
        }

        when:
        ShipmentStatusCode shipmentStatusCode = status
        context.shipment = new Shipment() {
            @Override
            ShipmentStatus getStatus() {
                return new ShipmentStatus(code: shipmentStatusCode)
            }
        }

        then:
        StockMovementStatusHelper.getStatus(context) == expected

        where:
        status                                  || expected
        ShipmentStatusCode.PENDING              || ShipmentStatusCode.PENDING
        ShipmentStatusCode.SHIPPED              || ShipmentStatusCode.SHIPPED
        ShipmentStatusCode.PARTIALLY_RECEIVED   || ShipmentStatusCode.PARTIALLY_RECEIVED
        ShipmentStatusCode.RECEIVED             || ShipmentStatusCode.RECEIVED
    }

    void "should return requisition status #expected if shipment is not yet shipped"() {
        given:
        StockMovementContext context = new StockMovementContext() {
            @Override
            boolean isReturn() {
                return false
            }
            @Override
            boolean isInbound() {
                return true
            }
            @Override
            boolean isOutbound() {
                return false
            }
            @Override
            Boolean isFromPurchaseOrder() {
                return false
            }
        }

        when:
        context.requisition = new Requisition(status: requisitionStatus)
        ShipmentStatusCode shipmentStatusCode = shipmentStatus
        context.shipment = new Shipment() {
            @Override
            ShipmentStatus getStatus() {
                return new ShipmentStatus(code: shipmentStatusCode)
            }
            @Override
            Boolean hasShipped() {
                return true
            }
        }

        then:
        StockMovementStatusHelper.getStatus(context) == expect

        where:
        requisitionStatus           ||  shipmentStatus              || expect
        RequisitionStatus.CREATED   ||  ShipmentStatusCode.SHIPPED  || RequisitionStatus.CREATED
        RequisitionStatus.EDITING   ||  ShipmentStatusCode.SHIPPED  || RequisitionStatus.EDITING
        RequisitionStatus.VERIFYING ||  ShipmentStatusCode.SHIPPED  || RequisitionStatus.VERIFYING
        RequisitionStatus.PICKING   ||  ShipmentStatusCode.SHIPPED  || RequisitionStatus.PICKING
        RequisitionStatus.PICKED    ||  ShipmentStatusCode.SHIPPED  || RequisitionStatus.PICKED
    }

    void "should return shipment status #expected if shipment is shipped"() {
        given:
        StockMovementContext context = new StockMovementContext() {
            @Override
            boolean isReturn() {
                return false
            }
            @Override
            boolean isInbound() {
                return true
            }
            @Override
            boolean isOutbound() {
                return false
            }
            @Override
            Boolean isFromPurchaseOrder() {
                return false
            }
        }

        when:
        context.requisition = new Requisition(status: requisitionStatus)
        ShipmentStatusCode shipmentStatusCode = shipmentStatus
        context.shipment = new Shipment() {
            @Override
            ShipmentStatus getStatus() {
                return new ShipmentStatus(code: shipmentStatusCode)
            }
            @Override
            Boolean hasShipped() {
                return true
            }
        }

        then:
        StockMovementStatusHelper.getStatus(context) == expect

        where:
        requisitionStatus           || shipmentStatus                           || expect
        RequisitionStatus.ISSUED    || ShipmentStatusCode.SHIPPED               || ShipmentStatusCode.SHIPPED
        RequisitionStatus.ISSUED    || ShipmentStatusCode.RECEIVED              || ShipmentStatusCode.RECEIVED
        RequisitionStatus.ISSUED    || ShipmentStatusCode.PARTIALLY_RECEIVED    || ShipmentStatusCode.PARTIALLY_RECEIVED
    }

    void "should return shipment status #expected for list items on inbound stock movement returns"() {
        given:
        StockMovementContext context = new StockMovementContext() {
            @Override
            boolean isReturn() {
                return true
            }
            @Override
            boolean isInbound() {
                return true
            }
            @Override
            boolean isOutbound() {
                return false
            }
        }

        when:
        ShipmentStatusCode shipmentStatusCode = status
        context.shipment = new Shipment() {
            @Override
            ShipmentStatus getStatus() {
                return new ShipmentStatus(code: shipmentStatusCode)
            }
        }

        then:
        StockMovementStatusHelper.getListStatus(context) == expected

        where:
        status                                  || expected
        ShipmentStatusCode.PENDING              || ShipmentStatusCode.PENDING
        ShipmentStatusCode.SHIPPED              || ShipmentStatusCode.SHIPPED
        ShipmentStatusCode.PARTIALLY_RECEIVED   || ShipmentStatusCode.PARTIALLY_RECEIVED
        ShipmentStatusCode.RECEIVED             || ShipmentStatusCode.RECEIVED
    }

    void "should return shipment status #expected for list items on outbound stock movement returns"() {
        given:
        StockMovementContext context = new StockMovementContext() {
            @Override
            boolean isReturn() {
                return true
            }
            @Override
            boolean isInbound() {
                return false
            }
            @Override
            boolean isOutbound() {
                return true
            }
        }
        when:
        context.order = new Order(status: status)

        then:
        StockMovementStatusHelper.getListStatus(context) == expected

        where:
        status                          || expected
        OrderStatus.PENDING             || RequisitionStatus.CREATED
        OrderStatus.PLACED              || RequisitionStatus.CREATED
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
        StockMovementContext context = new StockMovementContext() {
            @Override
            Boolean isElectronicType() {
                return true
            }
            @Override
            boolean isInbound() {
                return true
            }
            @Override
            boolean isOutbound() {
                return false
            }
            @Override
            boolean isReturn() {
                return false
            }
            @Override
            boolean isCurrentLocationDownstreamConsumer() {
                return false
            }
        }
        context.shipment = new Shipment() {
            @Override
            ShipmentStatus getStatus() {
                return new ShipmentStatus(code: ShipmentStatusCode.SHIPPED)
            }
        }
        when:
        context.requisition = new Requisition(status: status)

        then:
        StockMovementStatusHelper.getListStatus(context) == expected

        where:
        status                              || expected
        RequisitionStatus.APPROVED          || ShipmentStatusCode.SHIPPED
        RequisitionStatus.REJECTED          || ShipmentStatusCode.SHIPPED
        RequisitionStatus.VERIFYING         || ShipmentStatusCode.SHIPPED
        RequisitionStatus.PENDING_APPROVAL  || ShipmentStatusCode.SHIPPED
    }

    void "should return approval status #expected for list items on inbound stock requests in a downstream consumer location"() {
        given:
        StockMovementContext context = new StockMovementContext() {
            @Override
            Boolean isElectronicType() {
                return true
            }
            @Override
            boolean isInbound() {
                return true
            }
            @Override
            boolean isOutbound() {
                return false
            }
            @Override
            boolean isReturn() {
                return false
            }
            @Override
            boolean isCurrentLocationDownstreamConsumer() {
                return true
            }
        }

        when:
        context.requisition = new Requisition(status: status)

        then:
        StockMovementStatusHelper.getListStatus(context) == expected

        where:
        status                              || expected
        RequisitionStatus.APPROVED          || StockMovementStatusCode.APPROVED
        RequisitionStatus.REJECTED          || StockMovementStatusCode.REJECTED
        RequisitionStatus.PENDING_APPROVAL  || StockMovementStatusCode.PENDING_APPROVAL
    }

    void "should return status #expected for any non-approval list items on inbound stock requests in a downstream consumer location"() {
        given:
        StockMovementContext context = new StockMovementContext() {
            @Override
            Boolean isElectronicType() {
                return true
            }
            @Override
            boolean isInbound() {
                return true
            }
            @Override
            boolean isOutbound() {
                return false
            }
            @Override
            boolean isReturn() {
                return false
            }
            @Override
            boolean isCurrentLocationDownstreamConsumer() {
                return true
            }
        }

        when:
        context.requisition = new Requisition(status: status)

        then:
        StockMovementStatusHelper.getListStatus(context) == expected

        where:
        status                                 || expected
        RequisitionStatus.VERIFYING            || StockMovementStatusCode.IN_PROGRESS
        RequisitionStatus.PICKING              || StockMovementStatusCode.IN_PROGRESS
        RequisitionStatus.PICKED               || StockMovementStatusCode.IN_PROGRESS
        RequisitionStatus.CHECKING             || StockMovementStatusCode.IN_PROGRESS
    }
}
