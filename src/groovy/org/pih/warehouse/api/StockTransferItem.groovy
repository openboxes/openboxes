package org.pih.warehouse.api

import org.codehaus.groovy.grails.validation.Validateable
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAvailability

@Validateable
class StockTransferItem {
    String id
    Product product
    Location location
    Location originBinLocation
    Location destinationBinLocation
    InventoryItem inventoryItem
    Integer quantityOnHand
    Integer quantity
    StockTransferStatus status = StockTransferStatus.PENDING
    List<StockTransferItem> splitItems = []
    Boolean delete = Boolean.FALSE

    static constraints = {
        id(nullable: true)
        product(nullable: true)
        location(nullable: true)
        originBinLocation(nullable: true)
        destinationBinLocation(nullable: true)
        inventoryItem(nullable: true)
        quantityOnHand(nullable: true)
        quantity(nullable: true)
        splitItems(nullable: true)
    }

    static StockTransferItem createFromOrderItem(OrderItem orderItem) {
        StockTransferItem stockTransferItem = new StockTransferItem()
        stockTransferItem.id = orderItem.id
        stockTransferItem.product = orderItem.product
        stockTransferItem.inventoryItem = orderItem.inventoryItem
        stockTransferItem.location = orderItem?.order?.origin
        stockTransferItem.originBinLocation = orderItem.originBinLocation
        stockTransferItem.destinationBinLocation = orderItem.destinationBinLocation
        stockTransferItem.quantity = orderItem.quantity
        stockTransferItem.quantityOnHand = orderItem.quantity
        stockTransferItem.status = getItemStatus(orderItem.orderItemStatusCode)

        orderItem.orderItems?.each { item ->
            stockTransferItem.splitItems.add(createFromOrderItem(item))
        }

        return stockTransferItem
    }

    static StockTransferItem createFromProductAvailability(ProductAvailability productAvailability) {
        StockTransferItem stockTransferItem = new StockTransferItem()
        stockTransferItem.product = productAvailability.product
        stockTransferItem.inventoryItem = productAvailability.inventoryItem
        stockTransferItem.location = productAvailability.location
        stockTransferItem.originBinLocation = productAvailability.binLocation
        stockTransferItem.destinationBinLocation = productAvailability.binLocation
        stockTransferItem.quantity = productAvailability.quantityOnHand
        stockTransferItem.quantityOnHand = productAvailability.quantityOnHand

        return stockTransferItem
    }

    static StockTransferStatus getItemStatus(OrderItemStatusCode orderItemStatusCode) {
        switch (orderItemStatusCode) {
            case OrderItemStatusCode.COMPLETED:
                return StockTransferStatus.COMPLETED
            case OrderItemStatusCode.CANCELED:
                return StockTransferStatus.CANCELED
            default:
                return StockTransferStatus.PENDING
        }
    }

    Map toJson() {
        return [
                id                              : id,
                "product.id"                    : product?.id,
                "product.productCode"           : product?.productCode,
                "product.name"                  : product?.name,
                "inventoryItem.id"              : inventoryItem?.id,
                "lotNumber"                     : inventoryItem?.lotNumber,
                "expirationDate"                : inventoryItem?.expirationDate?.format("MM/dd/yyyy"),
                "originBinLocation.id"          : originBinLocation?.id,
                "originBinLocation.name"        : originBinLocation?.name,
                "originZone"                    : originBinLocation?.zone?.name,
                "destinationBinLocation.id"     : destinationBinLocation?.id,
                "destinationBinLocation.name"   : destinationBinLocation?.name,
                "destinationZone.id"            : destinationBinLocation?.zone?.id,
                "destinationZone.name"          : destinationBinLocation?.zone?.name,
                quantity                        : quantity,
                quantityOnHand                  : quantityOnHand,
                status                          : status.name(),
                splitItems                      : splitItems.collect { it?.toJson() }
        ]
    }
}
