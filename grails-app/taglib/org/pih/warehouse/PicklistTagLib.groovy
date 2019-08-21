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

class PicklistTagLib {

    def inventoryService

    def picklistItem = { attrs, body ->

        def location = Location.get(session.warehouse.id)
        attrs.products = []
        if (attrs.requisitionItem.product) {
            attrs.product = attrs.requisitionItem.product
            attrs.inventoryItems = inventoryService.findInventoryItemsByProducts([attrs.product])
            attrs.inventoryItems.each {
                it.quantityOnHand = inventoryService.getQuantity(location.inventory, it)
                it.quantityAvailableToPromise = inventoryService.getQuantityAvailableToPromise(location.inventory, it)
            }
            attrs.inventoryItems = attrs.inventoryItems.findAll { it.quantityOnHand > 0 }
            attrs.inventoryItem = attrs.inventoryItems.find {
                it?.expirationDate?.after(new Date())
            }

        } else if (attrs.requisitionItem.category) {
            attrs.products = attrs.requisitionItem.category.products
            attrs.inventoryItems = inventoryService.findInventoryItemsByProducts(attrs.products)
        } else if (attrs.requisitionItem.productGroup) {
            attrs.products = attrs.requisitionItem.productGroup.products
            attrs.inventoryItems = inventoryService.findInventoryItemsByProducts(attrs.products)
        }


        out << g.render(template: '/taglib/pickRequestItem', model: [attrs: attrs])
    }


    def mapRequestItem = { attrs, body ->
        attrs.products = []
        if (attrs.requisitionItem.product) {
            attrs.product = attrs.requisitionItem.product
            println "product " + attrs.product
            attrs.inventoryItems = inventoryService.findInventoryItemsByProducts([attrs.product])
            attrs.inventoryItem = attrs.inventoryItems.find { it.expirationDate != null }
        } else if (attrs.requisitionItem.category) {
            attrs.products = attrs.requisitionItem.category.products
            println "products " + attrs.products
            attrs.inventoryItems = inventoryService.findInventoryItemsByProducts(attrs.products)
        } else if (attrs.requisitionItem.productGroup) {
            attrs.products = attrs.requisitionItem.productGroup.products
            println "products " + attrs.products
            attrs.inventoryItems = inventoryService.findInventoryItemsByProducts(attrs.products)
        }


        out << g.render(template: '/taglib/mapRequestItem', model: [attrs: attrs])
    }
}
