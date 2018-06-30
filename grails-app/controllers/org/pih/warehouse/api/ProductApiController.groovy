/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.api

import grails.converters.JSON
import grails.validation.ValidationException
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAssociation
import org.pih.warehouse.product.ProductAssociationTypeCode

class ProductApiController extends BaseDomainApiController {

    def productService
    def inventoryService

    def list = {
        def products = productService.getProducts(null, null, false, params)
		render ([data:products] as JSON)
	}

    def availableItems = {
        def productIds = params.list("product.id") + params.list("id")
        Location location = Location.get(params.location.id)

        if (!location || productIds.empty) {
            throw new IllegalArgumentException("Must specify a location and at least one product")
        }

        def products = Product.findAllByIdInListAndActive(productIds, true)
        def availableItems = getAvailableItems(location, products)
        render ([data:availableItems] as JSON)
    }

    def associatedProducts = {
        Product product = Product.get(params.id)
        ProductAssociationTypeCode [] types = params.list("type")
        log.info "Types: " + types
        def productAssociations = ProductAssociation.createCriteria().list {
            eq("product", product)
            'in'("code", types)
        }
        def availableItems = []
        def location = (params?.location?.id) ? Location.get(params.location.id) : null
        if (location) {
            def products = productAssociations.collect { it.associatedProduct }
            log.info ("Location " + location + " products = " + products)

            availableItems = getAvailableItems(location, product)

            productAssociations = productAssociations.collect { productAssociation ->
                def availableProducts = getAvailableProducts(location, productAssociation.associatedProduct)
                def expirationDate = availableProducts.findAll { it.expirationDate != null }.collect { it.expirationDate }.min()
                def availableQuantity = availableProducts.collect { it.quantity }.sum()
                return [
                        id               : productAssociation.id,
                        type             : productAssociation?.code?.name(),
                        product          : productAssociation.associatedProduct,
                        conversionFactor : productAssociation.quantity,
                        comments         : productAssociation.comments,
                        minExpirationDate   : expirationDate,
                        availableQuantity   : availableQuantity
                ]
            }
        }



        render ([data:[
                product: product,
                availableItems: availableItems,
                hasAssociations: !productAssociations?.empty,
                hasEarlierExpiringItems: false,
                productAssociations: productAssociations]] as JSON)
    }

    def getAvailableItems(Location location, Product product) {
        return getAvailableItems(location, [product])
    }


    def getAvailableItems(Location location, List products) {
        def availableItemsMap = inventoryService.getQuantityByInventoryItemMap(location, products)

        def inventoryItems = products.collect { it.inventoryItems }.flatten()
        log.info "inventory items: " + inventoryItems
        def availableItems = inventoryItems.collect {
            return [
                    inventoryItem: it,
                    quantity: availableItemsMap[it]
            ]
        }
        availableItems = availableItems.findAll { it.quantity > 0 }

        return availableItems
    }

    def getAvailableProducts(Location location, Product product) {
        return getAvailableProducts(location, [product])
    }

    def getAvailableProducts(Location location, List products) {
        def availableItemsMap = inventoryService.getQuantityByInventoryItemMap(location, products)

        def inventoryItems = products.collect { it.inventoryItems }.flatten()
        log.info "inventory items: " + inventoryItems
        def availableItems = inventoryItems.collect { InventoryItem inventoryItem ->
            return [
                    "inventoryItem.id": inventoryItem.id,
                    lotNumber: inventoryItem.lotNumber,
                    expirationDate: inventoryItem.expirationDate,
                    quantity: availableItemsMap[inventoryItem]
            ]
        }
        availableItems = availableItems.findAll { it.quantity > 0 }
        return availableItems
    }

}
