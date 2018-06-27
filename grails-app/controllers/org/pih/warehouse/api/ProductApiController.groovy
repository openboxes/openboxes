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
                def associatedAvailableItems = getAvailableItems(location, productAssociation.associatedProduct)
                [
                        id               : productAssociation.id,
                        type             : productAssociation?.code?.name(),
                        product          : productAssociation.associatedProduct,
                        quantity         : productAssociation.quantity,
                        comments         : productAssociation.comments,
                        availableItems   : associatedAvailableItems
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
        def availableItemsMap = inventoryService.getQuantityByInventoryItemMap(location, [product])
        // Transform all inventory items into available items
        def availableItems = product.inventoryItems.collect {
            def inventoryItemMap = [
                    id: it.id,
                    lotNumber: it.lotNumber,
                    expirationDate: it.expirationDate,
                    "product.id": it.product.id,
                    "product.name": it.product.name
            ]
            inventoryItemMap << [quantity: availableItemsMap[it]]
            inventoryItemMap
        }
        availableItems = availableItems.findAll { it.quantity > 0 }

        return availableItems
    }

}
