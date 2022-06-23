package org.pih.warehouse.api

import com.google.common.base.Enums
import org.codehaus.groovy.grails.validation.Validateable
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.order.OrderType
import org.pih.warehouse.shipping.ReferenceNumber
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentType

@Validateable
class StockTransfer {

    String id
    Location origin
    Location destination
    String description
    String stockTransferNumber
    Date dateCreated
    Person orderedBy
    OrderType type

    Date dateShipped
    Date expectedDeliveryDate
    ShipmentType shipmentType
    String trackingNumber
    String driverName
    String comments

    StockTransferStatus status = StockTransferStatus.PENDING
    List<StockTransferItem> stockTransferItems = []

    List documents

    static constrants = {
        origin(nullable: true)
        destination(nullable: true)
        description(nullable: true)
        stockTransferNumber(nullable: true)
        status(nullable: true)
        stockTransferItems(nullable: true)
        dateCreated(nullable: true)
        orderedBy(nullable: true)
        type(nullable: true)
        dateShipped(nullable: true)
        expectedDeliveryDate(nullable: true)
        shipmentType(nullable: true)
        trackingNumber(nullable: true)
        driverName(nullable: true)
        comments(nullable: true)
        documents(nullable: true)
    }

    static StockTransfer createFromOrder(Order order) {
        StockTransfer stockTransfer = new StockTransfer(
                id: order.id,
                origin: order.origin,
                destination: order.destination,
                description: order.description,
                stockTransferNumber: order.orderNumber,
                status: getStatus(order.status),
                dateCreated: order.dateOrdered,
                orderedBy: order.orderedBy,
                type: order.orderType
        )

        // Add all order items to stock transfer
        order.orderItems.each { orderItem ->
            if (!orderItem.parentOrderItem) {
                stockTransfer.stockTransferItems.add(StockTransferItem.createFromOrderItem(orderItem))
            }
        }

        if (order?.shipments) {
            Shipment shipment = order?.shipments?.first()
            stockTransfer.dateShipped = shipment?.expectedShippingDate
            stockTransfer.expectedDeliveryDate = shipment?.expectedDeliveryDate
            stockTransfer.shipmentType = shipment?.shipmentType
            ReferenceNumber trackingNumber = shipment?.referenceNumbers?.find { ReferenceNumber rn ->
                rn.referenceNumberType.id == Constants.TRACKING_NUMBER_TYPE_ID
            }
            stockTransfer.trackingNumber = trackingNumber?.identifier
            stockTransfer.driverName = shipment?.driverName
            stockTransfer.comments = shipment?.additionalInformation
        }

        return stockTransfer
    }

    static StockTransferStatus getStatus(OrderStatus orderStatus) {
        StockTransferStatus stockTransferStatus = Enums.getIfPresent(StockTransferStatus, orderStatus.name()).orNull()
        return stockTransferStatus ?: StockTransferStatus.PENDING
    }

    Map toJson() {
        return [
                id                  : id,
                description         : description,
                stockTransferNumber : stockTransferNumber,
                status              : status?.name(),
                dateCreated         : dateCreated?.format("MMMM dd, yyyy"),
                "origin.id"         : origin?.id,
                "origin.name"       : origin?.name,
                "destination.id"    : destination?.id,
                "destination.name"  : destination?.name,
                stockTransferItems  : stockTransferItems.sort { a, b ->
                    a.orderIndex <=> b.orderIndex ?:
                        a.product?.productCode <=> b.product?.productCode ?:
                            a.inventoryItem?.lotNumber <=> b.inventoryItem?.lotNumber ?:
                                a.originBinLocation?.zone?.name <=> b.originBinLocation?.zone?.name ?:
                                    a.originBinLocation?.name <=> b.originBinLocation?.name
                }.collect { it?.toJson() },
                orderedBy           : orderedBy?.name,
                type                : type?.code,
                dateShipped         : dateShipped?.format("MM/dd/yyyy") ?: "",
                expectedDeliveryDate: expectedDeliveryDate?.format("MM/dd/yyyy") ?: "",
                shipmentType        : shipmentType ?: "",
                trackingNumber      : trackingNumber ?: "",
                driverName          : driverName ?: "",
                comments            : comments ?: "",
                documents           : documents ?: ""
        ]
    }
}
