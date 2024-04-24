package util

import org.pih.warehouse.api.StockMovementDirection
import org.pih.warehouse.api.StockMovementType
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Location
import org.pih.warehouse.order.Order
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionSourceType
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.shipping.Shipment

class StockMovementContext {
    Order order

    Shipment shipment

    Requisition requisition

    StockMovementDirection getStockMovementDirection(Location currentLocation) {
        if (currentLocation == shipment?.origin) {
            return StockMovementDirection.OUTBOUND
        }
        if (currentLocation == shipment?.destination || shipment?.origin?.isSupplier()) {
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

    boolean isReturn() {
        return order?.orderType?.isReturnOrder()
    }

    Boolean isElectronicType() {
        return requisition?.sourceType == RequisitionSourceType.ELECTRONIC
    }
}
