package util

import grails.util.Holders
import org.pih.warehouse.api.StockMovementStatusContext
import org.pih.warehouse.inventory.StockMovementStatusCode
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentStatusCode

class StockMovementStatusResolver {

    private static def getReturnViewPageStatus(Order order, Shipment shipment) {
        if (shipment?.hasShipped()) {
            return shipment?.status?.code
        }
        return getOutboundReturnDisplayStatus(order)
    }

    private static RequisitionStatus getOutboundReturnDisplayStatus(Order order) {
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

    private static ShipmentStatusCode getInboundReturnDisplayStatus(Shipment shipment) {
        return shipment?.status?.code ?: ShipmentStatusCode.PENDING
    }

    static Enum getStatus(StockMovementStatusContext context) {
        if (context.isReturn()) {
            return getReturnViewPageStatus(context.order, context.shipment)
        }

        if (context.isFromPurchaseOrder()) {
            return context?.shipment?.status?.code
        }

        if (context.requisition?.status == RequisitionStatus.ISSUED && context?.shipment?.hasShipped()) {
            return context?.shipment?.status?.code
        }

        return context.requisition?.status
    }

    static Enum getListStatus(StockMovementStatusContext context) {
        if (context.isReturn()) {
            return context.isInbound()
                    ? getInboundReturnDisplayStatus(context.shipment)
                    : getOutboundReturnDisplayStatus(context.order)
        }

        if (context.isInbound()) {
            //Request
            if (context.isElectronicType() && context.isCurrentLocationDownstreamConsumer()) {
                // Approval request
                // Mapping statuses for display for the requestor's dashboard
                // We want to display all statuses from approval workflow (approved, rejected and pending approval)
                if (context?.requisition?.status in RequisitionStatus.listApproval()) {
                    switch(context?.requisition?.status) {
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
                if (context?.requisition?.status in RequisitionStatus.listRequestOptions()) {
                    return StockMovementStatusCode.IN_PROGRESS
                }
            }

            return context?.shipment?.status?.code
        }

        if (context.isOutbound()) {
            return context?.requisition?.status
        }

        return context?.requisition?.status
    }

    static def getApplicationTagLib() {
        return Holders.grailsApplication.mainContext.getBean('org.grails.plugins.web.taglib.ApplicationTagLib')
    }

    static Map getStatusMetaData(Enum status) {
        return [
                name: status?.name(),
                label: applicationTagLib.message(code: 'enum.' + status?.getClass()?.getSimpleName() + '.' + status),
                variant: status.hasProperty('variant') ? status?.variant?.name : null,
        ]
    }
}
