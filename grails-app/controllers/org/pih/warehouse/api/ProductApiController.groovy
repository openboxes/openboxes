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
        String locationId = params?.location?.id ?: session?.warehouse?.id
        Location location = Location.get(locationId)
        if (!location || productIds.empty) {
            throw new IllegalArgumentException("Must specify a location and at least one product")
        }

        def products = Product.findAllByIdInListAndActive(productIds, true)
        def availableItems = inventoryService.getAvailableItems(location, products)
        render ([data:availableItems] as JSON)
    }


    def availableBins = {
        def productIds = params.list("product.id") + params.list("id")
        String locationId = params?.location?.id ?: session?.warehouse?.id
        Location location = Location.get(locationId)

        if (!location || productIds.empty) {
            throw new IllegalArgumentException("Must specify a location and at least one product")
        }

        def products = Product.findAllByIdInListAndActive(productIds, true)
        def availableBins = inventoryService.getAvailableBinLocations(location, products)
        render ([data:availableBins] as JSON)
    }


    def substitutions = {
        params.type = ProductAssociationTypeCode.SUBSTITUTE
        params.resource = "substitutions"
        forward(action: "associatedProducts")
    }

    def associatedProducts = {
        Product product = Product.get(params.id)
        ProductAssociationTypeCode[] types = params.list("type")
        log.info "Types: " + types
        def productAssociations = ProductAssociation.createCriteria().list {
            eq("product", product)
            'in'("code", types)
        }
        def availableItems = []
        boolean hasEarlierExpiringItems = false
        String locationId = params?.location?.id ?: session?.warehouse?.id
        def location = (locationId) ? Location.get(locationId) : null
        if (location) {
            def products = productAssociations.collect { it.associatedProduct }
            log.info("Location " + location + " products = " + products)

            availableItems = inventoryService.getAvailableItems(location, product)

            productAssociations = productAssociations.collect { productAssociation ->
                def availableProducts = inventoryService.getAvailableProducts(location, productAssociation.associatedProduct)
                def expirationDate = availableProducts.findAll { it.expirationDate != null }.collect {
                    it.expirationDate
                }.min()
                def availableQuantity = availableProducts.collect { it.quantity }.sum()
                return [
                        id               : productAssociation.id,
                        type             : productAssociation?.code?.name(),
                        product          : productAssociation.associatedProduct,
                        conversionFactor : productAssociation.quantity,
                        comments         : productAssociation.comments,
                        minExpirationDate: expirationDate,
                        availableQuantity: availableQuantity
                ]
            }
            Date productExpirationDate = availableItems?.collect { it.inventoryItem.expirationDate }?.min()
            Date otherExpirationDate = productAssociations?.collect { it.minExpirationDate }?.min()
            hasEarlierExpiringItems = productExpirationDate ? productExpirationDate.after(otherExpirationDate) : false
        }

        // This just renames the collection in the JSON so we can match the API called
        // (i.e. resource name is substitutions for /api/products/:id/substitutions)
        params.resource = params.resource ?: "productAssociations"

        render([
                data:
                        [
                                product                : product,
                                availableItems         : availableItems,
                                hasAssociations        : !productAssociations?.empty,
                                hasEarlierExpiringItems: hasEarlierExpiringItems,
                                "${params.resource}"   : productAssociations
                        ]
        ] as JSON)
    }




}
