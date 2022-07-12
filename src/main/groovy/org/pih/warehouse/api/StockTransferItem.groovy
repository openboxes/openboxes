package org.pih.warehouse.api

import org.codehaus.groovy.grails.validation.Validateable
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAvailability

@Validateable
class StockTransferItem {
    String id
    String productAvailabilityId
    Product product
    Location location
    Location originBinLocation
    Location destinationBinLocation
    InventoryItem inventoryItem
    Integer quantityOnHand
    Integer quantityNotPicked
    Integer quantity
    StockTransferStatus status = StockTransferStatus.PENDING
    List<StockTransferItem> splitItems = []
    Set<PicklistItem> picklistItems = []
    Boolean delete = Boolean.FALSE
    Person recipient

    Integer orderIndex = 0

    static constraints = {
        id(nullable: true)
        product(nullable: true)
        location(nullable: true)
        originBinLocation(nullable: true)
        destinationBinLocation(nullable: true)
        inventoryItem(nullable: true)
        quantityOnHand(nullable: true)
        quantityNotPicked(nullable: true)
        quantity(nullable: true)
        splitItems(nullable: true)
        picklistItems(nullable: true)
        recipient(nullable: true)
        orderIndex(nullable: true)
    }

    static StockTransferItem createFromOrderItem(OrderItem orderItem) {
        StockTransferItem stockTransferItem = new StockTransferItem()
        stockTransferItem.id = orderItem.id
        stockTransferItem.product = orderItem.product
        stockTransferItem.inventoryItem = orderItem.inventoryItem
        stockTransferItem.location = orderItem?.order?.origin
        stockTransferItem.originBinLocation = orderItem.originBinLocation
        stockTransferItem.destinationBinLocation = orderItem.destinationBinLocation
        stockTransferItem.recipient = orderItem.recipient
        stockTransferItem.quantity = orderItem.quantity
        // Temporarily set to quantity, should be pulled from PA
        stockTransferItem.quantityOnHand = orderItem.quantity
        stockTransferItem.quantityNotPicked = orderItem.quantity
        stockTransferItem.status = getItemStatus(orderItem.orderItemStatusCode)
        stockTransferItem.orderIndex = orderItem.orderIndex

        orderItem.orderItems?.each { item ->
            stockTransferItem.splitItems.add(createFromOrderItem(item))
        }

        orderItem?.picklistItems?.each { PicklistItem picklistItem ->
            stockTransferItem.picklistItems.add(createFromPicklistItem(picklistItem))
        }

        return stockTransferItem
    }

    static StockTransferItem createFromProductAvailability(ProductAvailability productAvailability) {
        StockTransferItem stockTransferItem = new StockTransferItem()
        stockTransferItem.productAvailabilityId = productAvailability.id
        stockTransferItem.product = productAvailability.product
        stockTransferItem.inventoryItem = productAvailability.inventoryItem
        stockTransferItem.location = productAvailability.location
        stockTransferItem.originBinLocation = productAvailability.binLocation
        stockTransferItem.destinationBinLocation = productAvailability.binLocation
        stockTransferItem.quantity = productAvailability.quantityOnHand
        stockTransferItem.quantityOnHand = productAvailability.quantityOnHand
        stockTransferItem.quantityNotPicked = productAvailability.quantityNotPicked > 0 ? productAvailability.quantityNotPicked : 0

        return stockTransferItem
    }

    static StockTransferItem createFromPicklistItem(PicklistItem picklistItem) {
        StockTransferItem stockTransferItem = new StockTransferItem()
        stockTransferItem.id = picklistItem.id
        stockTransferItem.product = picklistItem?.inventoryItem?.product
        stockTransferItem.inventoryItem = picklistItem?.inventoryItem
        stockTransferItem.location = picklistItem?.orderItem?.order?.origin
        stockTransferItem.originBinLocation  = picklistItem?.binLocation
        stockTransferItem.quantity = picklistItem?.quantity
        stockTransferItem.status = getItemStatus(OrderItemStatusCode.COMPLETED)

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
                productAvailabilityId           : productAvailabilityId,
                "product.id"                    : product?.id,
                "product.productCode"           : product?.productCode,
                "product.name"                  : product?.name,
                "product.handlingIcons"         : product?.handlingIcons,
                "inventoryItem.id"              : inventoryItem?.id,
                "lotNumber"                     : inventoryItem?.lotNumber,
                "expirationDate"                : inventoryItem?.expirationDate?.format("MM/dd/yyyy"),
                "recalled"                      : inventoryItem?.isRecalled(),
                "originBinLocation.id"          : originBinLocation?.id,
                "originBinLocation.name"        : originBinLocation?.name,
                "originZone"                    : originBinLocation?.zone?.name,
                "onHold"                        : originBinLocation?.isOnHold(),
                "destinationBinLocation.id"     : destinationBinLocation?.id,
                "destinationBinLocation.name"   : destinationBinLocation?.name,
                "destinationZone.id"            : destinationBinLocation?.zone?.id,
                "destinationZone.name"          : destinationBinLocation?.zone?.name,
                quantity                        : quantity,
                quantityOnHand                  : quantityOnHand,
                quantityNotPicked               : quantityNotPicked,
                status                          : status.name(),
                recipient                       : recipient,
                splitItems                      : splitItems.sort { a, b ->
                    a.destinationBinLocation?.name <=> b.destinationBinLocation?.name ?:
                        b.quantity <=> a.quantity
                }.collect { it?.toJson() },
                picklistItems                   : picklistItems.sort { a, b ->
                    a.binLocation?.name <=> b.binLocation?.name ?:
                            b.quantity <=> a.quantity
                }.collect { it?.toJson() },
                sortOrder                       : orderIndex
        ]
    }
}
