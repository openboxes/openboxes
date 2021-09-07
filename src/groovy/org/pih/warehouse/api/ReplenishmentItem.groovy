package org.pih.warehouse.api

import org.codehaus.groovy.grails.validation.Validateable
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAvailability

@Validateable
class ReplenishmentItem {
    String id
    Product product
    Location replenishmentLocation  // origin
    Location location // destination depot
    Location binLocation // destination
    InventoryItem inventoryItem
    Integer quantity
    Integer quantityInBin // QoH in bin
    Integer minQuantity
    Integer maxQuantity
    Integer totalQuantityOnHand // Total QoH for this product in Depot
    ReplenishmentStatus status = ReplenishmentStatus.PENDING
    List<ReplenishmentItem> picklistItems = [] // Extracted from order <-> picklist association
    Boolean delete = Boolean.FALSE

    static constraints = {
        id(nullable: true)
        product(nullable: true)
        location(nullable: true)
        replenishmentLocation (nullable: true)
        binLocation(nullable: true)
        inventoryItem(nullable: true)
        quantity(nullable: true)
        quantityInBin(nullable: true)
        minQuantity(nullable: true)
        maxQuantity(nullable: true)
        totalQuantityOnHand(nullable: true)
        picklistItems(nullable: true)
    }

    static ReplenishmentItem createFromOrderItem(OrderItem orderItem) {
        ReplenishmentItem replenishmentItem = new ReplenishmentItem()
        replenishmentItem.id = orderItem.id
        replenishmentItem.product = orderItem.product
        replenishmentItem.inventoryItem = orderItem.inventoryItem
        replenishmentItem.location = orderItem?.order?.origin
        replenishmentItem.replenishmentLocation  = orderItem.originBinLocation
        replenishmentItem.binLocation = orderItem.destinationBinLocation
        replenishmentItem.quantity = orderItem.quantity
        replenishmentItem.quantityInBin =
        replenishmentItem.minQuantity = orderItem.quantity
        replenishmentItem.maxQuantity = orderItem.quantity
        replenishmentItem.totalQuantityOnHand = orderItem.quantity
        replenishmentItem.status = getItemStatus(orderItem.orderItemStatusCode)

        orderItem?.picklistItems?.each { PicklistItem picklistItem ->
            replenishmentItem.picklistItems.add(createFromPicklistItem(picklistItem))
        }

        return replenishmentItem
    }


    static ReplenishmentItem createFromPicklistItem(PicklistItem picklistItem) {
        ReplenishmentItem replenishmentItem = new ReplenishmentItem()
        replenishmentItem.id = picklistItem.id
        replenishmentItem.product = picklistItem?.inventoryItem?.product
        replenishmentItem.inventoryItem = picklistItem?.inventoryItem
        replenishmentItem.location = picklistItem?.orderItem?.order?.origin
        replenishmentItem.replenishmentLocation  = picklistItem?.binLocation
        replenishmentItem.binLocation = picklistItem?.orderItem?.destinationBinLocation
        replenishmentItem.quantity = picklistItem?.quantity
        replenishmentItem.status = getItemStatus(OrderItemStatusCode.COMPLETED)

        return replenishmentItem
    }

    static ReplenishmentStatus getItemStatus(OrderItemStatusCode orderItemStatusCode) {
        switch (orderItemStatusCode) {
            case OrderItemStatusCode.COMPLETED:
                return ReplenishmentStatus.COMPLETED
            case OrderItemStatusCode.CANCELED:
                return ReplenishmentStatus.CANCELED
            default:
                return ReplenishmentStatus.PENDING
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
            "replenishmentLocation.id"      : replenishmentLocation?.id,
            "replenishmentLocation.name"    : replenishmentLocation?.name,
            "replenishmentZone"             : replenishmentLocation?.zone?.name,
            "binLocation.id"                : binLocation?.id,
            "binLocation.name"              : binLocation?.name,
            "zone.id"                       : binLocation?.zone?.id,
            "zone.name"                     : binLocation?.zone?.name,
            quantity                        : quantity,
            quantityInBin                   : quantityInBin,
            minQuantity                     : minQuantity,
            maxQuantity                     : maxQuantity,
            totalQuantityOnHand             : totalQuantityOnHand,
            status                          : status.name(),
            picklistItems                   : picklistItems.sort { a, b ->
                a.binLocation?.name <=> b.binLocation?.name ?:
                    b.quantity <=> a.quantity
            }.collect { it?.toJson() }
        ]
    }
}
