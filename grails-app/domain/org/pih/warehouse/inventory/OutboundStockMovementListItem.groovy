package org.pih.warehouse.inventory

import org.codehaus.groovy.grails.validation.Validateable
import org.pih.warehouse.api.StockMovementType
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionSourceType
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.requisition.RequisitionType
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentStatusCode

@Validateable
class OutboundStockMovementListItem implements Serializable {

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
            "lineItemCount"
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

    Map toJson() {
        return [
                id                  : id,
                name                : name,
                description         : description,
                statusCode          : statusCode?.toString(),
                identifier          : identifier,
                origin              : origin,
                destination         : destination,
                hasManageInventory  : origin?.supports(ActivityCode.MANAGE_INVENTORY),
                stocklist           : [
                        id  : stocklist?.id,
                        name: stocklist?.name
                ],
                replenishmentType   : stocklist?.replenishmentTypeCode,
                dateRequested       : dateRequested?.format("MM/dd/yyyy"),
                requestedBy         : requestedBy,
                lineItemCount       : lineItemCount,
                requestType         : requestType,
                sourceType          : sourceType?.name,
        ]
    }

    Integer getLineItemCount() {
        return requisition ? RequisitionItem.countByRequisition(requisition) : order ? OrderItem.countByOrder(order) : 0
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
