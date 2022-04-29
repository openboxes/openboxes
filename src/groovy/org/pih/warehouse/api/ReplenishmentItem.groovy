package org.pih.warehouse.api

import org.codehaus.groovy.grails.validation.Validateable
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Product

@Validateable
class ReplenishmentItem {
    String id
    Product product
    Location replenishmentLocation // origin bin location
    Location location // depot
    Location binLocation // destination bin location
    InventoryItem inventoryItem
    Integer quantity
    Integer quantityInBin // QoH in bin
    Integer minQuantity
    Integer maxQuantity
    Integer totalQuantityOnHand // Total QoH for this product in Depot
    Integer quantityNeeded
    ReplenishmentStatus status = ReplenishmentStatus.PENDING
    Set<PicklistItem> picklistItems = []
    List<AvailableItem> availableItems = []
    Set<SuggestedItem> suggestedItems = []
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
        quantityNeeded(nullable: true)
        picklistItems(nullable: true)
        availableItems(nullable: true)
        suggestedItems(nullable: true)
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
        replenishmentItem.quantityInBin = orderItem.quantity
        replenishmentItem.minQuantity = orderItem.quantity
        replenishmentItem.maxQuantity = orderItem.quantity
        replenishmentItem.totalQuantityOnHand = orderItem.quantity
        replenishmentItem.quantityNeeded = orderItem.quantity
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
            product                         : product,
            inventoryItem                   : inventoryItem,
            lotNumber                       : inventoryItem?.lotNumber,
            expirationDate                  : inventoryItem?.expirationDate?.format("MM/dd/yyyy"),
            "replenishmentLocation.id"      : replenishmentLocation?.id,
            "replenishmentLocation.name"    : replenishmentLocation?.name,
            "replenishmentZone.id"          : replenishmentLocation?.zone?.id,
            "replenishmentZone.name"        : replenishmentLocation?.zone?.name,
            "currentBinLocation.id"         : binLocation?.id,
            "currentBinLocation.name"       : binLocation?.name,
            "currentZone.id"                : binLocation?.zone?.id,
            "currentZone.name"              : binLocation?.zone?.name,
            quantity                        : quantity,
            quantityInBin                   : quantityInBin,
            minQuantity                     : minQuantity,
            maxQuantity                     : maxQuantity,
            totalQuantityOnHand             : totalQuantityOnHand,
            quantityNeeded                  : quantityNeeded,
            status                          : status.name(),
            picklistItems                   : picklistItems.sort { a, b ->
                a.binLocation?.name <=> b.binLocation?.name ?:
                    b.quantity <=> a.quantity
            }.collect { it?.toJson() },
            availableItems                   : availableItems.sort { a, b ->
                a.binLocation?.name <=> b.binLocation?.name
            }.collect { it?.toJson() },
            suggestedItems                   : suggestedItems.sort { a, b ->
                a.binLocation?.name <=> b.binLocation?.name
            }.collect { it?.toJson() }
        ]
    }
}
