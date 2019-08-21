/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.inventory

import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductGroup

class InventoryItemCommand {

    String description
    Category category
    Product product
    ProductGroup productGroup
    InventoryItem inventoryItem
    InventoryLevel inventoryLevel

    // For product groups, we need to keep track of all product-level inventory items
    List<InventoryItemCommand> inventoryItems

    Integer quantityOnHand = 0
    Integer quantityToShip = 0
    Integer quantityToReceive = 0

    static constraints = {
        category(nullable: true)
        product(nullable: true)
        productGroup(nullable: true)
        inventoryItem(nullable: true)
        inventoryLevel(nullable: true)
        inventoryItems(nullable: true)
        quantityOnHand(nullable: true)
        quantityToShip(nullable: true)
        quantityToReceive(nullable: true)
    }

    /**
     * An item is supported if it
     * @return
     */
    Boolean getSupported() {
        return !inventoryLevel?.status || inventoryLevel?.status == org.pih.warehouse.inventory.InventoryStatus.SUPPORTED
    }

    Boolean getNotSupported() {
        return inventoryLevel?.status == org.pih.warehouse.inventory.InventoryStatus.NOT_SUPPORTED ||
                inventoryLevel?.status == org.pih.warehouse.inventory.InventoryStatus.SUPPORTED_NON_INVENTORY
    }


    int hashCode() {
        if (product != null) {
            return product.id.hashCode()
        }
        return super.hashCode()
    }

    boolean equals(Object o) {
        if (o instanceof InventoryItemCommand) {
            InventoryItemCommand that = (InventoryItemCommand) o
            return this.product.id == that.product.id
        }
        return false
    }
}



