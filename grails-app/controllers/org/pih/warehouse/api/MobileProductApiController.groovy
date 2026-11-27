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
import org.pih.warehouse.core.LabelPreviewDisabledException
import org.pih.warehouse.core.LabelTemplateException
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

        data.images = documentLinks(product?.images)

        data.defaultImageUrl = data.images ? data.images[0].uri : null

        data.documents = documentLinks(product?.documents)

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

    /**
     * The documents as links for the app. A document whose link is not an http or https URL is left out.
     */
    static List<Map> documentLinks(Collection<Document> documents) {
        return documents?.collect { Document document ->
            [id: document.id, name: document.filename, contentType: document.contentType, uri: document.fileUri ?: document.link]
        }?.findAll { Map link -> isWebUrl(link.uri as String) }
    }

    static boolean isWebUrl(String url) {
        if (!url) {
            return false
        }
        try {
            URI uri = new URI(url)
            return uri.scheme?.toLowerCase() in ['http', 'https'] && util.RedirectUtil.hostOf(uri)
        } catch (URISyntaxException ignored) {
            return false
        }
    }

    def renderLabel() {
        Product product = productService.getProduct(params.id)
        Document document = Document.get(params.documentId)
        if (!document) {
            throw new ObjectNotFoundException(params.documentId, Document.class.simpleName)
        }
        InputStream image
        try {
            image = zebraService.renderDocument(document, [product:product])
        } catch (LabelTemplateException e) {
            renderLabelTemplateError(e)
            return
        } catch (LabelPreviewDisabledException e) {
            response.status = 503
            render([errorCode: 503, errorMessage: e.message] as JSON)
            return
        }
        response.contentType = "image/png"
        response.outputStream << image
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
        } catch (LabelTemplateException e) {
            renderLabelTemplateError(e)
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

        def minLength = grailsApplication.config.openboxes.typeahead.minLength
        if (jsonObject?.value?.size() < minLength) {
            render([data: []] as JSON)
            return
        }

        // Don't split scanned value - treat as single term for barcode/product code matching
        String[] terms = jsonObject.value ? [jsonObject.value] as String[] : null
        List products = productService.searchProducts(terms, [])
        if (!products) {
            // Nothing starts with the value: match the code anywhere, so a part number typed without its
            // manufacturer prefix is found. A value that finds products by prefix keeps exactly those results.
            products = productService.searchProducts(terms, [], false, true)
        }
        render([data: products?.unique()] as JSON)
    }

    /**
     * Update a product identifier (e.g., UPC etc.)
     * Endpoint: PUT /mobile/products/{id}/identifiers
     * Request body to set the identifier:
     * {
     *   "identifier": {
     *     "type": "upc",
     *     "value": "123456789012"
     *   }
     * }
     * To remove it, send "clear": true instead of a value: { "identifier": { "type": "upc", "clear": true } }.
     *
     * 404 unknown product, 401 no session user, 403 not a product manager (checked before the body is read),
     * 400 {errorCode, errorMessage} for a malformed body, a missing or unsupported type, a missing, blank or
     * non-string value, a value together with clear, or a value the product cannot store (e.g. longer than
     * 255 characters); 409 when another product already uses the value.
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

        User user = session?.user
        if (!user) {
            render(status: 401, text: "Active user session required")
            return
        }

        if (!(userService.hasRoleProductManager(user))) {
            render(status: 403, text: "Insufficient privileges")
            return
        }

        def body
        try {
            body = request.JSON
        } catch (Exception e) {
            renderBadRequest("Request body must be JSON")
            return
        }

        try {
            def identifier = body instanceof Map ? body.identifier : null
            if (!(identifier instanceof Map) || !(identifier.type instanceof String) || !identifier.type.trim()) {
                renderBadRequest("Identifier type is required")
                return
            }
            String type = identifier.type.trim().toLowerCase()
            def value = identifier.value
            // Removing a barcode must be asked for explicitly: an empty value is a client error, not a request to remove.
            boolean clear = identifier.clear == true

            if (type != "upc") {
                renderBadRequest("Unsupported identifier type '${type}'")
                return
            }

            if (clear) {
                if (value != null && !(value instanceof String && !value.trim())) {
                    renderBadRequest("Send either an identifier value or clear, not both")
                    return
                }
                product.upc = null
                if (!product.save(flush: true)) {
                    renderSaveFailure(product)
                    return
                }
                render([data: [id: product.id, identifier: [type: type, value: null]]] as JSON)
                return
            }

            if (!(value instanceof String) || !value.trim()) {
                renderBadRequest("Identifier value is required")
                return
            }
            String newValue = value.trim()
            def oldValue = product.upc

            Product upcDuplicate = Product.findByUpc(newValue)
            if (upcDuplicate && upcDuplicate.id != product.id) {
                render(
                        status: 409,
                        text: "Conflict: The UPC '${newValue}' is already assigned to product " +
                                "${upcDuplicate.productCode} – ${upcDuplicate.name}."
                )
                return
            }

            Product productCodeDuplicate = Product.findByProductCode(newValue)
            if (productCodeDuplicate && productCodeDuplicate.id != product.id) {
                render(
                        status: 409,
                        text: "Conflict: The UPC '${newValue}' is already used as a product code " +
                                "by product ${productCodeDuplicate.productCode} – ${productCodeDuplicate.name}."
                )
                return
            }

            product.upc = newValue
            if (!product.save(flush: true)) {
                renderSaveFailure(product)
                return
            }

            sendProductBarcodeUpdateNotification(user, product, oldValue, newValue)

            render([data: [id: product.id, identifier: [type: type, value: newValue]]] as JSON)

        } catch (Exception e) {
            log.error("Error updating identifier for product ${params.id}: ${e.message}", e)
            render(status: 500, text: "Server error while updating product identifier")
        }
    }

    private void renderSaveFailure(Product product) {
        String message = product.errors.hasFieldErrors('upc') ? "Identifier value is not valid" : "Product could not be saved"
        log.info("Product identifier for ${product.id} not saved; invalid fields: ${product.errors.fieldErrors*.field}")
        product.discard()
        renderBadRequest(message)
    }

    private void renderBadRequest(String message) {
        response.status = 400
        render([errorCode: 400, errorMessage: message] as JSON)
    }

    void sendProductBarcodeUpdateNotification(User user, Product product, String oldUpc, String newUpc) {
        try {
            grailsApplication.mainContext.publishEvent(new ProductBarcodeUpdatedEvent(user, product, oldUpc, newUpc))
        } catch (Exception e) {
            log.error("Error while publishing ProductBarcodeUpdatedEvent for product ${product?.id}", e)
        }
    }

    private void renderLabelTemplateError(LabelTemplateException e) {
        log.warn(e.message)
        response.status = 400
        render([errorCode: 400, errorMessage: e.message] as JSON)
    }
}
