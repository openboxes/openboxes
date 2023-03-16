/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse

import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.product.Product


class InventoryTagLib {

    def inventoryService


    def quantityOnHand = { attrs, body ->
        def product = Product.get(attrs.product)
        def location = attrs.location ? Location.get(attrs.location) : Location.get(session.warehouse.id)
        def totalQuantity = inventoryService.getQuantityOnHand(location, product)
        out << "${totalQuantity} ${product.unitOfMeasure ?: g.message(code: 'default.each.label')}"
    }

    def productStatus = { attrs, body ->
        def product = Product.get(attrs.product)
        def location = attrs.location ? Location.get(attrs.location) : Location.get(session.warehouse.id)
        def totalQuantity = inventoryService.getQuantityOnHand(location, product)
        def inventoryLevel = inventoryService.getInventoryLevelByProductAndInventory(product, location.inventory)
        out << render(template: "/taglib/productStatus", model: [product: product, inventoryLevel: inventoryLevel, totalQuantity: totalQuantity])
    }


    def abcClassification = { attrs, body ->
        def location = attrs.location ? Location.get(attrs.location) : Location.get(session.warehouse.id)
        def product = Product.get(attrs.product)
        InventoryLevel inventoryLevel = InventoryLevel.findByProductAndInventory(product, location?.inventory)
        out << "${inventoryLevel?.abcClass ?: product?.abcClass ?: g.message(code: 'default.none.label')}"
    }

}
