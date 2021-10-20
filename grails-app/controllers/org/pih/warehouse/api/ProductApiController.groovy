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

import fr.w3blog.zpl.utils.ZebraUtils
import grails.converters.JSON
import groovyx.net.http.HTTPBuilder
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.core.Document
import org.pih.warehouse.core.DocumentCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAssociation
import org.pih.warehouse.product.ProductAssociationTypeCode
import org.pih.warehouse.product.ProductAttribute
import org.pih.warehouse.product.ProductAvailability

class ProductApiController extends BaseDomainApiController {

    def productService
    def templateService
    def inventoryService
    def forecastingService
    def grailsApplication
    def productAvailabilityService

    def read = {
        Product product = productService.getProduct(params.id)
        render ([data:product] as JSON)
    }

    def details = {
        def product = productService.getProduct(params.id)
        def location = Location.get(session.warehouse.id)
        def data = product.toJson()
        data.location = location

        List<AvailableItem> availableItems = inventoryService.getAvailableItems(location, product)
        Integer quantityAvailable = availableItems.collect { AvailableItem availableItem -> availableItem.quantityAvailable?:0 }.sum()
        Integer quantityOnHand = availableItems.collect { AvailableItem availableItem -> availableItem.quantityOnHand?:0 }.sum()

        data.status = quantityAvailable ? "In Stock" : "Out of Stock"
        data.quantityAvailableToPromise = quantityAvailable
        data.quantityOnHand = quantityOnHand
        data.quantityAllocated = 0
        data.quantityOnOrder = 0
        data.unitOfMeasure = product.unitOfMeasure?:"EA"

        ApplicationHolder.application.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib')
        List<Document> barcodeTemplates = Document.findAllByDocumentCode(DocumentCode.ZEBRA_TEMPLATE)
        data.barcodeLabels = barcodeTemplates.collect { Document template ->
            String url = String.format("/api/products/%s/labels/%s", product.id, template.id)
            [id: template.id, name: template.name, url: g.createLink(uri: url, absolute: true)]
        }

        data.images = product?.images?.collect {
            return [ id: it.id, name: it.filename, contentType: it.contentType, fileUri: it.fileUri?:it?.link ]
        }
        data.documents = product?.documents?.collect {
            return [ id: it.id, name: it.filename, contentType: it.contentType, fileUri: it.fileUri?:it?.link ]
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
        render([data: data] as JSON)
    }

    def renderLabel = {
        Product product = productService.getProduct(params.id)
        Document document = Document.get(params.documentId)
        if (!document) {
            throw new ObjectNotFoundException(params.documentId, Document.class.simpleName)
        }
        Map model = [product: product]
        String body = templateService.renderTemplate(document, model)
        String renderApiUrl = grailsApplication.config.openboxes.barcode.labelaryApi.url
        def http = new HTTPBuilder(renderApiUrl)
        def html = http.post(body: body)
        response.contentType = "image/png"
        response.outputStream << html
    }

    def printLabel = {
        try {
            Product product = productService.getProduct(params.id)

            // FIXME OBKN-98 Temporarily default to first barcode template if documentId is not provided
            Document document = (params.documentId) ?
                    Document.get(params.documentId) :
                    Document.findAllByDocumentCode(DocumentCode.ZEBRA_TEMPLATE).first()
            if (!document) {
                throw new ObjectNotFoundException(params.documentId, Document.class.simpleName)
            }
            Map model = [product: product]
            String renderedContent = templateService.renderTemplate(document, model)
            String ipAddress = grailsApplication.config.openboxes.barcode.printer.ipAddress
            Integer port = grailsApplication.config.openboxes.barcode.printer.port
            log.info "Printing ${renderedContent} to ${ipAddress}:${port}"
            ZebraUtils.printZpl(renderedContent, ipAddress, port)
            render([data: "Product label has been printed to ${ipAddress}:${port}"] as JSON)
            return
        } catch (Exception e) {
            render([errorCode: 500, cause: e?.class, errorMessage: e?.message] as JSON)
        }
    }

    def demand = {
        def product = Product.get(params.id)
        def location = Location.get(session.warehouse.id)
        def data = [:]
        data.location = location
        data.product = product
        data.demand = forecastingService.getDemand(location, product)

        render([data: data] as JSON)
    }

    def demandSummary = {
        def product = Product.get(params.id)
        def location = Location.get(session.warehouse.id)
        def data = forecastingService.getDemandSummary(location, product)
        render([data: data] as JSON)
    }

    def productSummary = {
        def product = Product.load(params.id)
        def location = Location.load(session.warehouse.id)
        def quantityOnHand = ProductAvailability.findAllByProductAndLocation(product, location).sum { it.quantityOnHand }
        render([data: [product:[id: product.id], location: [id: location.id], quantityOnHand: quantityOnHand]] as JSON)
    }

    def productAvailability = {
        def product = Product.load(params.id)
        def location = Location.load(session.warehouse.id)
        def data = ProductAvailability.findAllByProductAndLocation(product, location)
        render([data: data] as JSON)
    }

    def list = {

        def minLength = grailsApplication.config.openboxes.typeahead.minLength

        if (params.name && params.name.size() < minLength) {
            render([data: []])
            return
        }

        String[] terms = params?.name?.split(",| ")?.findAll { it }
        def products
        if(params.availableItems) {
            products = productService.searchProducts(terms, [])
            def location = Location.get(session.warehouse.id)
            def availableItems = productAvailabilityService.getAvailableBinLocations(location, products).groupBy { it.inventoryItem?.product?.productCode }
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

    def availableItems = {
        def productIds = params.list("product.id") + params.list("id")
        String locationId = params?.location?.id ?: session?.warehouse?.id
        Location location = Location.get(locationId)
        if (!location || productIds.empty) {
            throw new IllegalArgumentException("Must specify a location and at least one product")
        }

        def products = Product.findAllByIdInListAndActive(productIds, true)
        def availableItems = inventoryService.getAvailableBinLocations(location, products)
        render([data: availableItems] as JSON)
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
        render([data: availableBins] as JSON)
    }


    def substitutions = {
        params.type = ProductAssociationTypeCode.SUBSTITUTE
        params.resource = "substitutions"
        forward(action: "associatedProducts")
    }

    def associatedProducts = {
        Product product = Product.get(params.id)
        ProductAssociationTypeCode[] types = params.list("type")
        log.debug "Types: " + types
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
            log.debug("Location " + location + " products = " + products)

            availableItems = inventoryService.getAvailableItems(location, product)

            productAssociations = productAssociations.collect { productAssociation ->
                def availableProducts = inventoryService.getAvailableProducts(location, productAssociation.associatedProduct)
                def expirationDate = availableProducts.findAll {
                    it.expirationDate != null
                }.collect {
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
            Date productExpirationDate = availableItems?.collect {
                it.inventoryItem.expirationDate
            }?.min()
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

    def withCatalogs = {
        Product product = Product.get(params.id)

        render([data: [
                id         : product.id,
                name       : product.name,
                productCode: product.productCode,
                catalogs   : product.getProductCatalogs()?.collect {
                    [
                            id  : it.id,
                            name: it.name
                    ]
                }
        ]] as JSON)
    }

    def productAvailabilityAndDemand = {
        Product product = Product.get(params.id)
        Location location = Location.get(params.locationId)
        def quantityOnHand = productAvailabilityService.getQuantityOnHand(product, location)
        def quantityAvailable = inventoryService.getQuantityAvailableToPromise(product, location)
        def demand = forecastingService.getDemand(location, product)
        render([monthlyDemand: demand.monthlyDemand, quantityOnHand: quantityOnHand, quantityAvailable: quantityAvailable] as JSON)
    }

    def search = {
        def jsonObject = request.JSON
        List<Product> products = productService.findProducts([jsonObject?.value])
        render([data: products] as JSON)
    }

}
