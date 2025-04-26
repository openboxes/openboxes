package org.pih.warehouse.inventory

import grails.validation.Validateable
import org.pih.warehouse.api.StockMovementType
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionItemStatus
import org.pih.warehouse.requisition.RequisitionSourceType
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.requisition.RequisitionType
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentStatusCode
import org.pih.warehouse.api.StockMovementStatusContext
import util.StockMovementStatusResolver

class OutboundStockMovementListItem implements Serializable, Validateable {

    String id
    String name
    String identifier
    String description

    Location origin
    Location destination

    Person createdBy
    Person updatedBy

    Date dateCreated
    Date lastUpdated

    Date dateRequested
    Person requestedBy


    ShipmentStatusCode shipmentStatus

    RequisitionStatus status
    Requisition stocklist
    RequisitionType requestType
    RequisitionSourceType sourceType // temporary sourceType field for ELECTRONIC and PAPER types

    StockMovementType stockMovementType

    @Deprecated
    StockMovementStatusCode statusCode

    Requisition requisition
    Shipment shipment
    Order order

    Integer statusSortOrder

    List<ShipmentStatusCode> receiptStatusCodes // For filtering
    List<RequisitionStatus> requisitionStatusCodes // For filtering

    static transients = [
            "receiptStatusCodes",
            "requisitionStatusCodes",
            "isFromOrder",
            "isShipped",
            "isReceived",
            "totalValue",
            "pending",
            "electronicType",
            "lineItemCount",
            "fromReturnOrder"
    ]

    static mapping = {
        version false
        cache usage: "read-only"
        table "stock_movement_list_item"

        statusSortOrder formula: RequisitionStatus.getStatusSortOrderFormula()
    }

    static constraints = {
        id(nullable: true)
        name(nullable: true)
        description(nullable: true)
        origin(nullable: false)
        destination(nullable: false)
        stocklist(nullable: true)
        requestedBy(nullable: false)
        dateRequested(nullable: false)

        shipment(nullable:true)
        requisition(nullable:true)
        order(nullable: true)

        dateCreated(nullable: true)
        lastUpdated(nullable: true)
        requestType(nullable: true)
        sourceType(nullable: true)

        stockMovementType(nullable: true)
        statusCode(nullable: true)

        statusSortOrder(nullable: true)
    }

    @Deprecated
    Map<String, String> getDisplayStatus() {
        StockMovementStatusContext stockMovementContext = new StockMovementStatusContext(
                order: order,
                requisition: requisition,
                shipment: shipment,
                origin: origin,
                destination: destination
        )
        Enum status = StockMovementStatusResolver.getListStatus(stockMovementContext)
        return StockMovementStatusResolver.getStatusMetaData(status)
    }

    Map toJson() {
        return [
                id                  : id,
                name                : name,
                description         : description,
                statusCode          : statusCode?.toString(),
                displayStatus       : displayStatus,
                statusVariant       : status?.variant?.name,
                status              : status.toString(),
                currentStatus       : shipment?.currentStatus?.toString(),
                identifier          : identifier,
                stockMovementType   : stockMovementType.name(),
                origin              : [
                    id                  : origin?.id,
                    name                : origin?.name,
                    locationNumber      : origin?.locationNumber,
                    locationType        : origin?.locationType,
                    locationGroup       : origin?.locationGroup,
                    organizationName    : origin?.organization?.name,
                    organizationCode    : origin?.organization?.code,
                    isDepot             : origin?.isDepot(),
                    supportedActivities : origin?.supportedActivities,
                ],
                destination         : [
                    id                  : destination?.id,
                    name                : destination?.name,
                    locationNumber      : destination?.locationNumber,
                    locationType        : destination?.locationType,
                    locationGroup       : destination?.locationGroup,
                    organizationName    : destination?.organization?.name,
                    organizationCode    : destination?.organization?.code,
                    supportedActivities : destination?.supportedActivities,
                ],
                hasManageInventory  : origin?.supports(ActivityCode.MANAGE_INVENTORY),
                stocklist           : [
                        id  : stocklist?.id,
                        name: stocklist?.name
                ],
                order                : [
                        id                  : order?.id,
                        name                : order?.name,
                        orderNumber         : order?.orderNumber
                ],
                replenishmentType   : stocklist?.replenishmentTypeCode?.name(),
                dateRequested       : dateRequested?.format("MM/dd/yyyy"),
                dateCreated         : dateCreated?.format("MM/dd/yyyy"),
                requestedBy         : requestedBy,
                lineItemCount       : lineItemCount,
                requestType         : requestType?.name(),
                sourceType          : sourceType?.name(),
                isPending           : pending,
                isReturn            : fromReturnOrder,
                isElectronicType    : electronicType,
                isApprovalRequired  : requisition?.approvalRequired,
                shipmentType        : shipment?.shipmentType,
                approvers           : requisition?.approvers?.toList(),

                // Required by mobile app
                expectedShippingDate : shipment?.expectedShippingDate?.format("MM/dd/yyyy HH:mm XXX"),
                requestedDeliveryDate: requisition?.requestedDeliveryDate?.format("MM/dd/yyyy HH:mm XXX"),
                picklist             : requisition?.picklist,
                // Needs to be added to OutboundStockMovementListItem (or moved to the StockMovement :thinking:)
                //receivingLocation: receivingLocation?.toJson(LocationTypeCode.INTERNAL),
                //packingLocation  : packingLocation?.toJson(LocationTypeCode.INTERNAL),
                //loadingLocation  : loadingLocation?.toJson(LocationTypeCode.INTERNAL),
        ]
    }

    Integer getLineItemCount() {
        if (requisition) {
            RequisitionItem[] approvedItems = requisition.requisitionItems.findAll{ it ->
                it.status == RequisitionItemStatus.APPROVED
            }
            return approvedItems.size()
        }
        if (order) {
            return OrderItem.countByOrder(order)
        }
        return 0
    }

    Boolean isFromReturnOrder() {
        return order?.isReturnOrder
    }

    Boolean isPending() {
        return shipment?.currentStatus == ShipmentStatusCode.PENDING
    }

    Boolean isElectronicType() {
        sourceType == RequisitionSourceType.ELECTRONIC
    }

    Boolean hasBeenIssued() {
        return requisition?.status == RequisitionStatus.ISSUED
    }

    Boolean hasBeenShipped() {
        return shipment?.currentStatus == ShipmentStatusCode.SHIPPED
    }

    Boolean hasBeenPartiallyReceived() {
        return shipment?.currentStatus == ShipmentStatusCode.PARTIALLY_RECEIVED
    }

    Boolean hasBeenReceived() {
        return shipment?.currentStatus == ShipmentStatusCode.RECEIVED
    }
}
