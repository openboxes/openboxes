package util

import org.pih.warehouse.api.StockMovementDirection
import org.pih.warehouse.api.StockMovementType
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.StockMovementStatusCode
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.requisition.RequisitionSourceType
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

    boolean isOutbound() {
        Location currentLocation = AuthService.currentLocation
        return getStockMovementDirection(currentLocation) == StockMovementDirection.OUTBOUND
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
                return RequisitionStatus.CREATED
            case OrderStatus.PLACED:
                return RequisitionStatus.CHECKING
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

    def getDisplayStatusListPage() {
        if (isReturn()) {
            return isInbound()
                    ? getInboundReturnDisplayStatus(shipment)
                    : getOutboundReturnDisplayStatus(order)
        }

        if (isInbound()) {
            // Requests
            // FIXME this applies for two cases (stock request dashboard) & (inbound list when we create a request)
            // this if should apply for stock request dashboard but should not for inbound list (inb. list shoul have only shipment statuses)
            if (shipment?.requisition?.sourceType == RequisitionSourceType.ELECTRONIC) {
                // Approval request
                // Mapping statuses for display for the requestor's dashboard
                // We want to display all statuses from approval workflow (approved, rejected and pending approval)
                if (requisitionStatus in RequisitionStatus.listApproval()) {
                    switch(requisitionStatus) {
                        case RequisitionStatus.APPROVED:
                            return StockMovementStatusCode.APPROVED
                        case RequisitionStatus.REJECTED:
                            return StockMovementStatusCode.REJECTED
                        case RequisitionStatus.PENDING_APPROVAL:
                            return StockMovementStatusCode.PENDING_APPROVAL
                    }
                }

                // Regular request
                // We want to map all statuses from depot side to "in progress".
                if (requisitionStatus in RequisitionStatus.listRequestOptions()) {
                    return StockMovementStatusCode.IN_PROGRESS
                }
            }

            return shipment?.status?.code
        }

        if (isOutbound()) {
            return defaultStatus
        }

        return shipment?.status?.code ?: defaultStatus
    }

    def getDisplayStatus() {
        if (isReturn()) {
            return isInbound()
                    ? getInboundReturnDisplayStatus(shipment)
                    : getOutboundReturnDisplayStatus(order)
        }

        if (shipment?.isFromPurchaseOrder) {
            return shipment?.status?.code
        }


        if (requisitionStatus >= RequisitionStatus.ISSUED && shipment?.hasShipped()) {
            return shipment?.status?.code
        }


        return defaultStatus
    }
}
