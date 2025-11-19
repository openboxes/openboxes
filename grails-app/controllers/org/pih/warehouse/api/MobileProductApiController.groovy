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
import grails.gorm.transactions.Transactional
import grails.core.GrailsApplication
import org.grails.web.json.JSONObject
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.core.Document
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.UserService
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAttribute
import org.pih.warehouse.product.ProductBarcodeUpdatedEvent

class MobileProductApiController extends BaseDomainApiController {

    def zebraService
    def productService
    def documentService
    GrailsApplication grailsApplication
    def productAvailabilityService
    UserService userService

    def read() {
        Product product = productService.getProduct(params.id)
        render ([data:product] as JSON)
    }

    def details() {
        def product = productService.getProduct(params.id)
        def location = Location.get(session.warehouse.id)
        def data = product.toJson()

        data.location = location
        data.upc = product.upc

        List availableItems = productAvailabilityService.getAvailableItems(location, [product.id])
        Integer quantityAvailable = availableItems.sum { it.quantityAvailable?:0 }
        Integer quantityOnHand = availableItems.sum { it.quantityOnHand?:0 }

        data.status = quantityAvailable ? "In Stock" : "Out of Stock"
        data.quantityAvailable = quantityAvailable
        data.quantityOnHand = quantityOnHand
        data.quantityAllocated = 0
        data.quantityOnOrder = 0
        data.unitOfMeasure = product.unitOfMeasure?:"EA"


        data.defaultBarcodeLabelUrl = documentService.getProductBarcodeLabel(product)

        data.images = product?.images?.collect {
            return [ id: it.id, name: it.filename, contentType: it.contentType, uri: it.fileUri?:it?.link ]
        }

        data.defaultImageUrl = data.images ? data.images[0].uri : null

        data.documents = product?.documents?.collect {
            return [ id: it.id, name: it.filename, contentType: it.contentType, uri: it.fileUri?:it?.link ]
        }

        data.attributes = product.attributes.collect { ProductAttribute productAttribute ->
            return [ id: productAttribute.id,
                     code: productAttribute.attribute?.code,
                     name: productAttribute?.attribute?.name,
                     value: productAttribute?.value,
                     unitOfMeasure: productAttribute?.unitOfMeasure?:productAttribute?.attribute?.unitOfMeasureClass?.baseUom?.name
            ]
        }
        data.availableItems = availableItems

        data.inventoryItems = product.inventoryItems
        data.defaultInventoryItem = product.inventoryItems.find { it.lotNumber == null }
        data.productType = product.productType

        render([data: data] as JSON)
    }

    def renderLabel() {
        Product product = productService.getProduct(params.id)
        Document document = Document.get(params.documentId)
        if (!document) {
            throw new ObjectNotFoundException(params.documentId, Document.class.simpleName)
        }
        response.contentType = "image/png"
        response.outputStream << zebraService.renderDocument(document, [product:product])
    }

    def printLabel() {
        try {
            Product product = productService.getProduct(params.id)
            Document document = Document.get(params.documentId)
            if (!document) {
                throw new ObjectNotFoundException(params.documentId, Document.class.simpleName)
            }
            zebraService.printDocument(document, [product:product])

            render([data: "Product label has been printed"] as JSON)
            return
        } catch (Exception e) {
            render([errorCode: 500, cause: e?.class, errorMessage: e?.message] as JSON)
        }
    }

    def list() {

        def minLength = grailsApplication.config.openboxes.typeahead.minLength

        if (params.name && params.name.size() < minLength) {
            render([data: []])
            return
        }

        String[] terms = params?.name?.split(",| ")?.findAll { it } ?: ""
        def products
        if(params.availableItems) {
            products = productService.searchProducts(terms, [])
            def location = Location.get(session.warehouse.id)
            def availableItems = productAvailabilityService.getAvailableBinLocations(location, products*.id).groupBy { it.inventoryItem?.product?.productCode }
            products = []
            availableItems.each { k, v ->
                products += [
                        productCode: k,
                        name: v[0].inventoryItem.product.name,
                        id: v[0].inventoryItem.product.id,
                        product: v[0].inventoryItem.product,
                        quantityAvailable: v.sum { it.quantityAvailable },
                        minExpirationDate: v.findAll { it.inventoryItem.expirationDate != null }.collect {
                            it.inventoryItem?.expirationDate
                        }.min()?.format("MM/dd/yyyy"),
                        color: v[0].inventoryItem.product.color
                ]
            }

            products = products.unique()
        } else {
            products = productService.searchProductDtos(terms)
        }

        render([data: products] as JSON)
    }

    def search() {
        JSONObject jsonObject = request.JSON
        String [] terms = jsonObject.value?.split(" ")
        List products = productService.searchProducts(terms, [])
        render([data: products?.unique()] as JSON)
    }

    /**
     * Update a product identifier (e.g., UPC etc.)
     * Endpoint: PUT /mobile/products/{id}/identifiers
     * Request body:
     * {
     *   "identifier": {
     *     "type": "upc",
     *     "value": "123456789012"
     *   }
     * }
     *
     * Initially supports only 'upc' type but structured for easy expansion.
     */
    @Transactional
    def updateIdentifier() {
        def product = Product.get(params.id)
        if (!product) {
            render(status: 404, text: "Product was not found")
            return
        }

        try {
            def body = request.JSON
            def identifier = body?.identifier
            def type = identifier?.type?.trim()?.toLowerCase()
            def value = identifier?.value?.trim()

            if (!type || !value) {
                render(status: 400, text: "Missing identifier type or value")
                return
            }

            User user = session?.user
            if (!user) {
                render(status: 401, text: "Active user session required")
                return
            }

            if (!(userService.hasRoleProductManager(user) || userService.isUserAdmin(user))) {
                render(status: 403, text: "Insufficient privileges")
                return
            }

            switch (type) {
                case "upc":
                    def oldValue = product.upc
                    def newValue = value

                    Product duplicate = Product.findByUpc(newValue)
                    if (duplicate && duplicate.id != product.id) {
                        render(
                                status: 409,
                                text: "Conflict: '${newValue}' already assigned to another product (Product Code: ${duplicate.productCode})"
                        )
                        return
                    }

                    product.upc = newValue
                    product.save(flush: true)

                    sendProductBarcodeUpdateNotification(product, oldValue, newValue)
                    break

                default:
                    render(status: 400, text: "Unsupported identifier type '${type}'")
                    return
            }

            render([data: [id: product.id, identifier: [type: type, value: value]]] as JSON)

        } catch (Exception e) {
            log.error("Error updating identifier for product ${params.id}: ${e.message}", e)
            render(status: 500, text: "Server error while updating product identifier")
        }
    }

    void sendProductBarcodeUpdateNotification(Product product, String oldUpc, String newUpc) {
        try {
            grailsApplication.mainContext.publishEvent(new ProductBarcodeUpdatedEvent(product, oldUpc, newUpc))
        } catch (Exception e) {
            log.error("Error while publishing ProductBarcodeUpdatedEvent for product ${product?.id}", e)
        }
    }
}
