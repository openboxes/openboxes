package org.pih.warehouse.api

import org.pih.warehouse.api.StockMovementDirection
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Location
import org.pih.warehouse.order.Order
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionSourceType
import org.pih.warehouse.shipping.Shipment

class StockMovementStatusContext {
    Order order

    Shipment shipment

    Requisition requisition

    Location origin

    Location destination

    Shipment getShipment() {
        return shipment
    }

    Order getOrder() {
        return order
    }

    Requisition getRequisition() {
        return requisition
    }

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

    boolean isOutbound() {
        Location currentLocation = AuthService.currentLocation
        return getStockMovementDirection(currentLocation) == StockMovementDirection.OUTBOUND
    }

    boolean isCurrentLocationDownstreamConsumer() {
        Location currentLocation = AuthService.currentLocation
        return currentLocation.isDownstreamConsumer()
    }

    boolean isReturn() {
        return order?.orderType?.isReturnOrder()
    }

    Boolean isFromPurchaseOrder() {
        return shipment?.isFromPurchaseOrder
    }

    Boolean isElectronicType() {
        return requisition?.sourceType == RequisitionSourceType.ELECTRONIC
    }
}
