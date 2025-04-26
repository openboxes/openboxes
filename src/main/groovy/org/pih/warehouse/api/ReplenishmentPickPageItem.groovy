package org.pih.warehouse.api

import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.picklist.PicklistItem

// Used for the picklist manipulation on the Replenishment Edit Pick modal
class ReplenishmentPickPageItem {

    OrderItem orderItem
    InventoryItem inventoryItem
    Location binLocation

    Set<PicklistItem> picklistItems = []
    List<AvailableItem> availableItems = []
    Set<SuggestedItem> suggestedItems = []

    Map toJson() {
        return [
            pickStatusCode      : statusCode,
            "orderItem.id"      : orderItem?.id,
            "product.name"      : orderItem?.product?.name,
            productCode         : orderItem?.product?.productCode,
            productId           : orderItem?.product?.id,
            product             : orderItem?.product,
            quantityRequested   : orderItem.quantity,
            quantityRequired    : quantityRequired,
            quantityPicked      : quantityPicked,
            quantityAvailable   : quantityAvailable,
            quantityRemaining   : quantityRemaining,
            hasAdjustedInventory: hasAdjustedInventory,
            hasChangedPick      : hasChangedPick,
            availableItems      : availableItems,
            suggestedItems      : suggestedItems,
            picklistItems       : picklistItems,
            recipient           : orderItem?.recipient,
        ]
    }

    Boolean getHasChangedPick() {
        return Boolean.FALSE
    }

    Boolean getHasAdjustedInventory() {
        return Boolean.FALSE
    }


    Integer getQuantityRemaining() {
        Integer quantityRemaining = quantityRequired - quantityPicked
        return quantityRemaining > 0 ? quantityRemaining : 0
    }

    Integer getQuantityRequired() {
        return orderItem?.quantity ?: 0
    }

    Integer getQuantityRequested() {
        orderItem?.quantity ?: 0
    }

    Integer getQuantityPicked() {
        return picklistItems ? picklistItems?.sum { it.quantityPicked } : 0
    }

    Integer getQuantityAvailable() {
        return availableItems ? availableItems?.sum { it.quantityAvailable } : null
    }


    String getStatusCode() {

        if (quantityRequired == quantityPicked && quantityRemaining == 0) {
            return "PICKED"
        } else if (quantityPicked > 0 && quantityRemaining > 0) {
            return "PARTIALLY_PICKED"
        } else {
            return "NOT_PICKED"
        }
    }

    static PickPageItem createFromOrderItem(OrderItem orderItem) {
        return new PickPageItem(orderItem: orderItem)
    }
}