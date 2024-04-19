package util

import org.pih.warehouse.api.StockMovementDirection
import org.pih.warehouse.api.StockMovementType
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Location
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentStatusCode

class StockMovementStatusHelper {

    Order order

    Shipment shipment

    StockMovementType stockMovementType

    Location origin

    Location destination

    RequisitionStatus defaultStatus


    StockMovementDirection getStockMovementDirection(Location currentLocation) {
        if (currentLocation == origin) {
            return StockMovementDirection.OUTBOUND
        }
        if (currentLocation == destination || origin?.isSupplier()) {
            return StockMovementDirection.INBOUND
        }
        return null
    }

    boolean isInbound() {
        Location currentLocation = AuthService.currentLocation
        return getStockMovementDirection(currentLocation) == StockMovementDirection.INBOUND
    }

    boolean isReturn() {
        return stockMovementType == StockMovementType.RETURN_ORDER
    }


    static ShipmentStatusCode getInboundReturnDisplayStatus(Shipment shipment) {
        return shipment?.status?.code ?: ShipmentStatusCode.PENDING
    }

    static RequisitionStatus getOutboundReturnDisplayStatus(Order order) {
        switch (order?.status) {
            case OrderStatus.PENDING:
            case OrderStatus.PLACED:
                return RequisitionStatus.CREATED
            case OrderStatus.APPROVED:
                return RequisitionStatus.PICKING
            case OrderStatus.CANCELED:
                return RequisitionStatus.CANCELED
            case OrderStatus.PARTIALLY_RECEIVED:
            case OrderStatus.RECEIVED:
            case OrderStatus.COMPLETED:
                return RequisitionStatus.ISSUED
            case OrderStatus.REJECTED:
                return RequisitionStatus.CANCELED
            default:
                return RequisitionStatus.PENDING
        }
    }

    def getDisplayStatus() {
        // TODO: Can be extended by another workflows (regular SMs, PO shipments etc)
        if (isReturn()) {
            return isInbound()
                    ? getInboundReturnDisplayStatus(shipment)
                    : getOutboundReturnDisplayStatus(order)
        }
        return defaultStatus
    }
}


