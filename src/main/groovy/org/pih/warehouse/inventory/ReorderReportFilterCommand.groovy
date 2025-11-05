package org.pih.warehouse.inventory

import grails.validation.Validateable
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Tag
import org.pih.warehouse.product.Category

class ReorderReportFilterCommand implements Validateable {
    List<Location> additionalLocations
    InventoryLevelStatus inventoryLevelStatus = InventoryLevelStatus.ALL_PRODUCTS
    ExpirationFilter expiration = ExpirationFilter.SUBTRACT_EXPIRED_STOCK
    List<Category> categories
    List<Tag> tags

    static constraints = {
        additionalLocations(nullable: true, validator: { List<Location> locations ->
            if (!locations.every { it.supports(ActivityCode.MANAGE_INVENTORY) }) {
                return ['invalid.locations']
            }
        })
        inventoryLevelStatus(nullable: true)
        expiration(nullable: true)
        categories(nullable: true)
        tags(nullable: true)
    }
}
