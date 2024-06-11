package org.pih.warehouse.allocation

import grails.validation.Validateable
import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.picklist.PicklistItemCommand
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.RequisitionItem

class AllocationRequest implements Validateable {

    Product product
    Integer quantityRequired
    InventoryItem inventoryItem
    RequisitionItem requisitionItem
    List<AvailableItem> availableItems
    List<AvailableItem> suggestedItems
    PicklistItemCommand picklistItemCommand

    static constraints = {
        product(nullable: false)
        inventoryItem(nullable: true)
        requisitionItem(nullable: true)
        availableItems(nullable: false)
        suggestedItems(nullable: true)
        quantityRequired(nullable: false)
        picklistItemCommand(nullable: false)
    }
}
