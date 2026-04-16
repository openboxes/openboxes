package org.pih.warehouse.inventory

import grails.validation.Validateable
import org.pih.warehouse.api.StockMovementStatusContext
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentStatusCode
import org.pih.warehouse.shipping.ShipmentType
import util.StockMovementStatusResolver

class InboundStockMovementListItem implements Serializable, Validateable {

    String id
    String name
    String identifier
    String description

    Location origin
    Location destination

    Person requestedBy
    Person createdBy
    Person updatedBy

    Date dateCreated
    Date lastUpdated
    Date dateRequested

    Shipment shipment
    Requisition requisition
    Requisition stocklist
    Order order

    ShipmentStatusCode currentStatus
    ShipmentType shipmentType

    static mapping = {
        version false
        cache usage: "read-only"
        table "inbound_stock_movement_list_item"
    }

    String getStatus() {
        if (requisition) {
            return requisition.status
        }
        if (shipment) {
            return shipment.status?.code
        }
        if (order) {
            return order.status
        }
        return null
    }

    Map getDisplayStatus() {
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

    Boolean isFromReturnOrder() {
        return order?.isReturnOrder ?: false
    }

    Boolean isPending() {
        return shipment?.currentStatus == ShipmentStatusCode.PENDING
    }

    Boolean isReceived() {
        return shipment?.currentStatus == ShipmentStatusCode.RECEIVED
    }

    Boolean isPartiallyReceived() {
        return shipment?.currentStatus == ShipmentStatusCode.PARTIALLY_RECEIVED
    }

    Integer getLineItemCount() {
        if (requisition) {
            return requisition.requisitionItemCount ?: 0
        }
        if (shipment) {
            return shipment.shipmentItemCount ?: 0
        }
        if (order) {
            return OrderItem.countByOrder(order)
        }

        return 0
    }

    Map toJson() {
        return [
            id                  : id,
            name                : name,
            description         : description,
            shipmentType        : shipment?.shipmentType,
            displayStatus       : displayStatus,
            identifier          : identifier,
            origin              : [
                id     : origin?.id,
                name   : origin?.name,
                isDepot: origin?.isDepot(),
            ],
            destination         : [
                id: destination?.id,
            ],
            order               : [
                id: order?.id,
            ],
            stocklist           : stocklist,
            dateCreated         : dateCreated,
            expectedDeliveryDate: shipment?.expectedDeliveryDate,
            requestedBy         : requestedBy,
            lineItemCount       : lineItemCount,
            isReturn            : fromReturnOrder,
            isReceived          : received,
            isPartiallyReceived : partiallyReceived,
            isPending           : pending,
        ]
    }
}
