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

import grails.converters.JSON
import grails.plugin.springcache.annotations.CacheFlush
import grails.plugin.springcache.annotations.Cacheable
import groovy.time.TimeCategory
import org.codehaus.groovy.grails.web.json.JSONObject
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Localization
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryStatus
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.jobs.CalculateHistoricalQuantityJob
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductCatalog
import org.pih.warehouse.product.ProductGroup
import org.pih.warehouse.product.ProductPackage
import org.pih.warehouse.product.ProductSupplier
import org.pih.warehouse.reporting.Indicator
import org.pih.warehouse.reporting.TransactionFact
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionItemSortByCode
import org.pih.warehouse.shipping.Container
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.util.LocalizationUtil

import java.text.NumberFormat
import java.text.SimpleDateFormat

class JsonController {

    def dataSource
    def dataService
    def dashboardService
    def inventoryService
    def productService
    def localizationService
    def shipmentService
    def reportService
    def messageSource
    def consoleService
    def userService
    def inventorySnapshotService
    def forecastingService
    def translationService
    def orderService

    def evaluateIndicator = {
        def indicator = Indicator.get(params.id)
        if (indicator) {
            def results = consoleService.eval(indicator.expression, true, request)
            render results.result
        } else {
            render "error"
        }
    }

    def calculateQuantityOnHand = {
        def location = Location.load(params.locationId)
        def products = Product.list(params)
        products.each { product ->
            inventoryService.calculateQuantityOnHand(product, location)
        }
    }


    def addToRequisitionItems = {
        log.info "addToRequisitionItems: ${params} "
        def json
        def requisition = Requisition.get(params?.requisition?.id)
        def product = Product.get(params?.product?.id)
        if (!requisition) {
            response.status = 400
            json = [success: false, errors: ["Unable to find requisition with ID ${params?.requisition?.id}"]]
        } else if (!product) {
            response.status = 400
            json = [success: false, errors: ["Unable to find product with ID ${params?.product?.id}"]]
        } else {
            def quantity = (params.quantity) ? params.int("quantity") : 1
            def orderIndex = (params.orderIndex) ? params.int("orderIndex") : 0
            def requisitionItem = new RequisitionItem()
            if (requisition) {
                requisitionItem.product = product
                requisitionItem.quantity = quantity
                requisitionItem.substitutable = false
                requisitionItem.orderIndex = orderIndex
                requisition.updatedBy = session.user
                requisition.addToRequisitionItems(requisitionItem)
                if (requisition.validate() && requisition.save(flush: true)) {
                    json = [success: true, data: requisitionItem.toStockListDetailsJson()]
                } else {
                    response.status = 400
                    json = [success: false, errors: requisitionItem.errors]
                }
            }
        }
        log.info(json as JSON)
        render json as JSON
    }

    def getRequisitionItems = {
        log.info "getRequisitionItems: ${params} "
        def json
        def requisition = Requisition.get(params?.id)
        if (!requisition) {
            json = [success: false, errors: ["Unable to find requisition with ID ${params?.id}"]]
        } else {
            RequisitionItemSortByCode sortByCode = requisition.sortByCode ?: RequisitionItemSortByCode.SORT_INDEX
            def requisitionItems = requisition."${sortByCode.methodName}"?.collect {
                it.toStockListDetailsJson()
            }
            json = [aaData: requisitionItems]
        }
        log.info(json as JSON)
        render json as JSON
    }

    def updateRequisitionItems = {
        log.info "updateRequisitionItems: ${params} "

        JSONObject jsonObject = request.JSON

        def requisition = Requisition.get(params?.id)
        if (!requisition) {
            response.status = 400
            render([success: false, errors: ["Unable to find requisition with ID ${params?.id}"]] as JSON)
        } else {
            def items = jsonObject.get("items")

            items?.each { item ->
                RequisitionItem requisitionItem = requisition.requisitionItems.find {
                    it.id == item.id
                }

                if (requisitionItem) {
                    requisitionItem.quantity = item.quantity ? new Integer(item.quantity) : null
                    requisitionItem.productPackage = item.productPackageId ? ProductPackage.get(item.productPackageId) : null
                }
            }

            requisition.updatedBy = session.user

            if (requisition.validate() && requisition.save(flush: true)) {
                forward(action: "getRequisitionItems")
            } else {
                response.status = 400
                render([success: false, errors: requisition.errors] as JSON)
            }
        }
    }

    def removeRequisitionItem = {
        log.info "removeRequisitionItem: ${params} "
        def json
        def requisitionItem = RequisitionItem.get(params?.id)
        if (!requisitionItem) {
            response.status = 400
            json = [success: false, errors: ["Unable to find requisition item with ID ${params?.id}"]]
        } else {
            requisitionItem.requisition.removeFromRequisitionItems(requisitionItem)
            requisitionItem.delete()
            json = [success: true]
        }
        log.info(json as JSON)
        render json as JSON
    }

    def getTranslation = {
        def data = translationService.getTranslation(params.text, params.src, params.dest)
        render ([data: data] as JSON)
    }

    def getLocalization = {
        log.info "get localization " + params
        def localization = Localization.get(params.id)
        // Get the localization from the database
        // Create a new localization based on the message source

        if (!localization) {
            localization = new Localization()

            // Get translation from message source
            def message = messageSource.getMessage(params.code, null, params.resolvedMessage, session?.user?.locale ?: "en")
            log.info "get translation for code " + params.code + ", " + session?.user?.locale + " = " + message

            localization.translation = message
            localization.code = params.code
            localization.text = message
            localization.locale = session.user.locale
        }

        // If the translation message is empty, set it equal to the same as the localization text
        if (!localization.translation)
            localization.translation = localization.text

        log.info "localization.toJson() = " + (localization.toJson() as JSON)

        render localization.toJson() as JSON
    }

    def saveLocalization = {
        log.info "Save localization " + params
        def data = request.JSON
        log.info "Data " + data
        log.info "ID: " + data.id
        def locale = session?.user?.locale
        def localization = Localization.get(data?.id?.toString())
        log.info "found localization " + localization
        if (!localization) {
            log.info "Nope.  Looking by code and locale"
            localization = Localization.findByCodeAndLocale(data.code, locale?.toString())
            if (!localization) {
                log.info "Nope. Creating empty localization "
                localization = new Localization()
            }
        }

        localization.text = data.translation
        localization.code = data.code
        localization.locale = locale

        log.info localization.id
        log.info localization.code
        log.info localization.text
        log.info localization.locale
        def jsonResponse = []

        // Attempt to save localization
        if (!localization.hasErrors() && localization.save()) {
            jsonResponse = [success: true, data: localization.toJson()]
        } else {
            jsonResponse = [success: false, errors: localization.errors]
        }
        log.info(jsonResponse as JSON)
        render jsonResponse as JSON
        return true
    }

    def deleteLocalization = {
        log.info "get localization " + params
        // Get the localization from the database
        def jsonResponse = []
        def localization = Localization.get(params.id)
        try {
            if (localization) {
                localization.delete()
                jsonResponse = [success: true, message: "successfully deleted translation"]
            }
        } catch (Exception e) {
            jsonResponse = [success: false, message: e.message]
        }
        render jsonResponse as JSON
    }

    @Cacheable("inventoryBrowserCache")
    def getQuantityToReceive = {
        def product = Product.get(params?.product?.id)
        def location = Location.get(params?.location?.id)
        def quantityToReceive = inventoryService.getQuantityToReceive(location, product)
        render(quantityToReceive ?: "0")
    }

    @Cacheable("inventoryBrowserCache")
    def getQuantityToShip = {
        def product = Product.get(params?.product?.id)
        def location = Location.get(params?.location?.id)
        def quantityToShip = inventoryService.getQuantityToShip(location, product)
        render(quantityToShip ?: "0")
    }

    @Cacheable("inventoryBrowserCache")
    def getQuantityOnHand = {
        def product = Product.get(params?.product?.id)
        def location = Location.get(params?.location?.id)
        def quantityOnHand = inventoryService.getQuantityOnHand(location, product)
        render(quantityOnHand ?: "0")
    }
    @Cacheable("inventoryBrowserCache")
    def flushInventoryBrowserCache = {
        redirect(controller: "inventory", action: "browse")
    }

    @Cacheable("dashboardCache")
    def getGenericProductSummary = {
        def startTime = System.currentTimeMillis()
        def location = Location.get(session?.warehouse?.id)
        def genericProductByStatusMap = inventoryService.getGenericProductSummary(location)

        // Convert from map of objects to map of statistics
        genericProductByStatusMap.each { k, v ->
            genericProductByStatusMap[k] = v?.size() ?: 0
        }

        render([elapsedTime              : (System.currentTimeMillis() - startTime),
                totalCount               : genericProductByStatusMap.values().size(),
                genericProductByStatusMap: genericProductByStatusMap] as JSON)

    }

    @Cacheable("dashboardCache")
    def getDashboardAlerts = {
        def location = Location.get(session?.warehouse?.id)
        def dashboardAlerts = dashboardService.getDashboardAlerts(location)
        render dashboardAlerts as JSON
    }

    @Cacheable("dashboardCache")
    def getDashboardExpiryAlerts = {
        def location = Location.get(session?.warehouse?.id)
        def map = dashboardService.getExpirationSummary(location)
        render map as JSON
    }

    @Cacheable("dashboardCache")
    def getTotalStockValue = {
        def location = Location.get(session?.warehouse?.id)
        def result = dashboardService.getTotalStockValue(location)
        def totalValue = g.formatNumber(number: result.totalStockValue)
        def lastUpdated = inventorySnapshotService.getLastUpdatedInventorySnapshotDate()
        if (lastUpdated) {
            lastUpdated = "Last updated " + prettytime.display([date: lastUpdated, showTime: true, capitalize: false]) + "."
        } else {
            lastUpdated = "No data available"
        }
        def data = [
                lastUpdated    : lastUpdated,
                totalStockValue: result.totalStockValue,
                hitCount       : result.hitCount,
                missCount      : result.missCount,
                totalCount     : result.totalCount,
                totalValue     : totalValue]
        render data as JSON
    }

    @Cacheable("dashboardCache")
    def getStockValueByProduct = {
        def location = Location.get(session?.warehouse?.id)
        def result = dashboardService.getTotalStockValue(location)
        def hasRoleFinance = userService.hasRoleFinance(session?.user)

        def stockValueByProduct = []
        result.stockValueByProduct.sort { it.value }.reverseEach { Product product, value ->
            value = g.formatNumber(number: value, format: "#######.00")
            stockValueByProduct << [
                    id         : product.id,
                    productCode: product.productCode,
                    productName: product.name,
                    unitPrice  : hasRoleFinance ? product.pricePerUnit : null,
                    totalValue : hasRoleFinance ? value : null
            ]
        }

        render([aaData: stockValueByProduct] as JSON)
    }


    @CacheFlush("dashboardTotalStockValueCache")
    def refreshTotalStockValue = {
        render([success: true] as JSON)
    }


    def getInventorySnapshots = {

        def location = Location.get(params?.location?.id)
        def results = inventorySnapshotService.findInventorySnapshotByLocation(location)

        def inStockCount = results.findAll {
            it.quantityOnHand > 0 && it.status == InventoryStatus.SUPPORTED
        }.size()
        def lowStockCount = results.findAll {
            it.quantityOnHand > 0 && it.quantityOnHand <= it.minQuantity && it.status == InventoryStatus.SUPPORTED
        }.size()
        def reoderStockCount = results.findAll {
            it.quantityOnHand > it.minQuantity && it.quantityOnHand <= it.reorderQuantity && it.status == InventoryStatus.SUPPORTED
        }.size()
        def overStockCount = results.findAll {
            it.quantityOnHand > it.reorderQuantity && it.quantityOnHand <= it.maxQuantity && it.status == InventoryStatus.SUPPORTED
        }.size()
        def stockOutCount = results.findAll {
            it.quantityOnHand <= 0 && it.status == InventoryStatus.SUPPORTED
        }.size()

        def totalCount = results.size()

        render([
                summary: [
                        totalCount      : totalCount,
                        inStockCount    : inStockCount,
                        lowStockCount   : lowStockCount,
                        reoderStockCount: reoderStockCount,
                        overStockCount  : overStockCount,
                        stockOutCount   : stockOutCount
                ],
                details: [results: results]
        ] as JSON)
    }

    def getQuantityOnHandMap = {
        def startTime = System.currentTimeMillis()
        def results = inventoryService.getQuantityByProductMap(session?.warehouse?.id)

        def elapsedTime = System.currentTimeMillis() - startTime
        render([count: results.size(), elapsedTime: elapsedTime, results: results] as JSON)
    }

    def findProductCodes = {
        def searchTerm = params.term + "%"
        def c = Product.createCriteria()
        def products = c.list {
            eq("active", true)
            or {
                ilike("productCode", searchTerm)
                ilike("name", searchTerm)
            }
        }

        //"id": "Netta rufina", "label": "Red-crested Pochard", "value": "Red-crested Pochard" },
        def results = products.unique().collect {
            [value: it.productCode, label: it.productCode + " " + it.name]
        }
        render results as JSON
    }

    def findTags = {
        def searchTerm = "%" + params.term + "%"
        def c = Tag.createCriteria()
        def tags = c.list {
            projections { property "tag" }
            ilike("tag", searchTerm)
        }

        def results = tags.unique().collect { [value: it, label: it] }
        render results as JSON
    }

    def autoSuggest = {
        log.info "autoSuggest: " + params
        def searchTerm = "%" + params.term + "%"
        def c = Product.createCriteria()
        def results = c.list {
            projections {
                property "${params.field}"
            }
            eq("active", true)
            ilike("${params.field}", searchTerm)
        }
        results = results.unique().collect { [value: it, label: it] }
        render results as JSON
    }

    def autoSuggestProductGroups = {
        log.info "autoSuggest: " + params
        def searchTerms = params.term.split(" ")
        def c = ProductGroup.createCriteria()
        def results = c.list {
            projections {
                property "name"
            }
            and {
                searchTerms.each { searchTerm ->
                    ilike("name", "%" + searchTerm + "%")
                }
            }
        }
        results = results.unique().collect { [value: it, label: it] }
        render results as JSON
    }


    def findProductNames = {
        def searchTerm = "%" + params.term + "%"
        def c = Product.createCriteria()
        def productNames = c.list {
            projections {
                property "name"
            }
            eq("active", true)
            ilike("name", searchTerm)
        }

        def results = productNames.unique().collect { [value: it, label: it] }
        render results as JSON
    }

    def findPrograms = {
        log.info "find programs " + params
        def searchTerm = params.term + "%"
        def c = Requisition.createCriteria()

        def names = c.list {
            projections {
                property "recipientProgram"
            }
            ilike("recipientProgram", searchTerm)
        }
        // Try again
        if (names.isEmpty()) {
            searchTerm = "%" + params.term + "%"
            c = Requisition.createCriteria()
            names = c.list {
                projections {
                    property "recipientProgram"
                }
                ilike("recipientProgram", searchTerm)
            }
        }

        if (names.isEmpty()) {
            names = []
            names << params.term
        }

        def results = names.collect { [value: it, label: it] }
        render results as JSON
    }


    def getInventoryItem = {
        render InventoryItem.get(params.id).toJson() as JSON
    }

    def getQuantity = {
        log.info params
        def quantity = 0
        def location = Location.get(session.warehouse.id)
        def lotNumber = (params.lotNumber) ? (params.lotNumber) : ""
        def product = (params.productId) ? Product.get(params.productId) : null

        def inventoryItem = inventoryService.findInventoryItemByProductAndLotNumber(product, lotNumber)
        if (inventoryItem) {
            quantity = inventoryService.getQuantity(location?.inventory, inventoryItem)
        }
        log.info "quantity by lotnumber '" + lotNumber + "' and product '" + product + "' = " + quantity
        render quantity ?: "N/A"
    }

    def sortContainers = {
        def container
        params.get("container[]").eachWithIndex { id, index ->
            container = Container.get(id)
            container.sortOrder = index
            container.save(flush: true)
            log.info("container " + container.name + " saved at index " + index)
        }
        container.shipment.save(flush: true)

        render(text: "", contentType: "text/plain")
    }

    def sortRequisitionItems = {
        log.info "sort requisition items " + params

        def requisitionItem
        params.get("requisitionItem[]").eachWithIndex { id, index ->
            requisitionItem = RequisitionItem.get(id)
            requisitionItem.orderIndex = index
            requisitionItem.save(flush: true)
            log.info("requisitionItem " + id + " saved at index " + index)
        }
        requisitionItem.requisition.refresh()
        render(text: "", contentType: "text/plain")
    }

    /**
     * Ajax method for the Record Inventory page.
     */
    def getInventoryItems = {
        log.info params
        def productInstance = Product.get(params?.product?.id)
        def inventoryItemList = inventoryService.getInventoryItemsByProduct(productInstance)
        render inventoryItemList as JSON
    }


    /**
     * Returns inventory items for the given location, lot number, and product.
     */
    def findInventoryItems = {
        log.info params
        long startTime = System.currentTimeMillis()
        def inventoryItems = []
        def location = Location.get(session.warehouse.id)
        if (params.term) {

            // Improved the performance of the auto-suggest by moving
            def tempItems = inventoryService.findInventoryItems(params.term)

            if (tempItems) {
                def maxResults = grailsApplication.config.openboxes.shipping.search.maxResults ?: 1000
                if (tempItems.size() > maxResults) {
                    def message = "${warehouse.message(code: 'inventory.tooManyItemsFound.message', default: 'Found {1} items for term "{0}". Too many items so disabling QoH. Try searching by product code.', args: [params.term, tempItems.size()])}"
                    inventoryItems << [id: 'null', value: message]
                } else {
                    def quantitiesByInventoryItem = [:]
                    quantitiesByInventoryItem = inventoryService.getQuantityByInventoryItemMap(location, tempItems*.product)

                    tempItems.each {
                        def quantity = quantitiesByInventoryItem[it]
                        quantity = (quantity) ?: 0

                        def localizedName = localizationService.getLocalizedString(it.product.name)
                        localizedName = (it.product.productCode ?: " - ") + " " + localizedName
                        inventoryItems = inventoryItems.sort { it.expirationDate }

                        if (quantity > 0) {

                            String description = (it?.lotNumber ?: "${g.message(code: 'default.noLotNumber.label')}") +
                                    " - ${g.message(code: 'default.expires.label')} " + (it?.expirationDate?.format("MMM yyyy") ?: "${g.message(code: 'default.never.label')}") +
                                    " - ${quantity} ${it?.product?.unitOfMeasure ?: 'EA'}"

                            String label = "${localizedName} x ${quantity ?: 0} ${it?.product?.unitOfMeasure ?: 'EA'}"

                            String expirationDate = it?.expirationDate ?
                                    g.formatDate(date: it.expirationDate, format: "MMM yyyy") :
                                    g.message(code: 'default.never.label')

                            inventoryItems << [
                                    id            : it.id,
                                    value         : it.lotNumber,
                                    imageUrl      : "${resource(dir: 'images', file: 'default-product.png')}",
                                    label         : label,
                                    description   : description,
                                    valueText     : it.lotNumber,
                                    lotNumber     : it.lotNumber,
                                    product       : it.product.id,
                                    productId     : it.product.id,
                                    productName   : localizedName,
                                    quantity      : quantity,
                                    expirationDate: expirationDate
                            ]
                        }
                    }

                    def count = inventoryItems.size()
                    def responseTime = System.currentTimeMillis() - startTime
                    inventoryItems.add(0, [id: 'null', value: "Searching for '${params.term}'", description: "Returned ${count} items in ${responseTime} ms"])

                }
            }
        }
        if (inventoryItems.size() == 0) {
            def message = "${warehouse.message(code: 'inventory.noItemsFound.message', args: [params.term])}"
            inventoryItems << [id: 'null', value: message]
        } else {
            inventoryItems = inventoryItems.sort { it.expirationDate }
        }

        render inventoryItems as JSON
    }

    def findLotsByName = {
        log.info params
        // Constrain by product id if the productId param is passed in
        def items = new TreeSet()
        if (params.term) {
            def searchTerm = "%" + params.term + "%"
            items = InventoryItem.withCriteria {
                and {
                    or {
                        ilike("lotNumber", searchTerm)
                    }
                    // Search within the inventory items for a specific product
                    if (params?.productId) {
                        eq("product.id", params.productId)
                    }
                }
            }

            def warehouse = Location.get(session.warehouse.id)
            def quantitiesByInventoryItem = inventoryService.getQuantityForInventory(warehouse?.inventory)

            if (items) {
                items = items.collect() { item ->
                    def quantity = quantitiesByInventoryItem[item]
                    quantity = (quantity) ?: 0

                    def localizedName = localizationService.getLocalizedString(item.product.name)

                    [
                            id            : item.id,
                            value         : item.lotNumber,
                            label         : localizedName + " --- Item: " + item.lotNumber + " --- Qty: " + quantity + " --- ",
                            valueText     : item.lotNumber,
                            lotNumber     : item.lotNumber,
                            expirationDate: item.expirationDate
                    ]
                }
            }
        }
        render items as JSON
    }


    def createPerson = {
        log.info("createPerson" + params)
        def data = [id: null, label: "Unable to create person with name " + params.name]

        def names = params.name.split(" ")
        if (names) {
            Person person
            if (names.length == 1) {
                throw new Exception("Person must have at least two names")
            }
            if (names.length == 2) {
                person = new Person(firstName: names[0], lastName: names[1])
            } else if (names.length == 3) {
                person = new Person(firstName: names[0] + " " + names[1], lastName: names[2])
            } else {
                throw new Exception("Person must have at most three names")
            }

            if (person) {
                person.save(flush: true)
                data = [id: person.id, value: person.name]
            }
        }

        render data as JSON

    }

    def findPersonByName = {
        log.info "findPersonByName: " + params
        def items = new TreeSet()
        try {

            def terms = params?.term?.split(" ")
            items = Person.withCriteria {
                eq("active", Boolean.TRUE)
                for (term in terms) {
                    or {
                        ilike("firstName", term + "%")
                        ilike("lastName", term + "%")
                        ilike("email", term + "%")
                    }
                }
                order("firstName", "asc")
                order("lastName", "asc")
            }

            if (items) {
                items = items.collect() {

                    [
                            id         : it.id,
                            label      : it.name,
                            text       : it.name,
                            description: it?.email,
                            value      : it.id,
                            valueText  : it.name,
                            desc       : (it?.email) ? it.email : "",
                    ]
                }?.unique()
            }
            items.add([id: "new", label: 'Create new record for ' + params.term, value: null, valueText: params.term])

        } catch (Exception e) {
            e.printStackTrace()
        }
        log.info "returning ${items?.size()} items: " + items
        render items as JSON


    }

    def findProductByName = {

        log.info("find products by name " + params)
        def dateFormat = new SimpleDateFormat(Constants.SHORT_MONTH_YEAR_DATE_FORMAT)
        def products = new TreeSet()

        if (params.term) {
            def terms = params?.term ? params?.term?.split(" ") : []

            // Get all products that match terms
            products = productService.searchProducts(terms, [])
            products = products.unique()

            if (terms) {
                products = products.sort() {
                    a, b ->
                        (terms.any {
                            a?.productCode?.contains(it) ? a.productCode : null
                        }) <=> (terms.any {
                            b?.productCode?.contains(it) ? b.productCode : null
                        }) ?:
                                (terms.any {
                                    a?.name?.contains(it) ? a.name : null
                                }) <=> (terms.any { b?.name?.contains(it) ? b.name : null })
                }
                products = products.reverse()
            }
        }

        String NEVER = "${warehouse.message(code: 'default.never.label')}"

        boolean skipQuantity = params.boolean("skipQuantity") ?: false
        // Convert from products to json objects
        if (products) {
            // Make sure items are unique
            products = products.collect() { product ->
                def productQuantity = 0
                // We need to check to make sure this is a valid product
                def inventoryItemList = []
                if (product.id && !skipQuantity) {
                    def inventoryItems = InventoryItem.findAllByProduct(product)
                    inventoryItemList = inventoryItems.collect() { inventoryItem ->
                        // FIXME Getting the quantity from the inventory map does not work at the moment
                        def quantity = 0

                        // Create inventory items object
                        [
                                id            : inventoryItem.id ?: 0,
                                lotNumber     : (inventoryItem?.lotNumber) ?: "",
                                expirationDate: (inventoryItem?.expirationDate) ?
                                        (dateFormat.format(inventoryItem?.expirationDate)) :
                                        NEVER,
                                quantity      : quantity
                        ]
                    }

                    // Sort using First-expiry, first out policy
                    inventoryItemList = inventoryItemList.sort { it?.expirationDate }
                }

                def localizedName = localizationService.getLocalizedString(product.name)


                // Convert product attributes to JSON object attributes
                [
                        id            : product?.id,
                        text          : product?.productCode + " " + localizedName,
                        product       : product,
                        category      : product?.category,
                        quantity      : productQuantity,
                        productCode   : product?.productCode,
                        value         : product.id,
                        label         : product?.productCode + " " + localizedName,
                        valueText     : localizedName,
                        desc          : product.description,
                        inventoryItems: inventoryItemList,
                        icon          : "none"
                ]
            }
        }

        if (products.size() == 0) {
            products << [value: null, label: warehouse.message(code: 'product.noProductsFound.message')]
        }

        log.info "Returning " + products.size() + " results for search " + params.term
        render products as JSON
    }

    def findLocations = {
        def locations = Location.createCriteria().list {
            if (params.term) {
                ilike("name", params.term + "%")
            }
            eq("active", Boolean.TRUE)
            isNull("parentLocation")
            order("name", "asc")
        }
        if (params.activityCode) {
            ActivityCode activityCode = params.activityCode as ActivityCode
            locations = locations.findAll { it.supports(activityCode) }
        }
        locations = locations.collect { [id: it.id, text: it.name]}
        render locations as JSON
    }

    def findRequestItems = {

        log.info("find request items by name " + params)

        //def items = new TreeSet();
        def items = []
        if (params.term) {
            // Match full name
            def products = Product.withCriteria {
                ilike("name", "%" + params.term + "%")
            }
            items.addAll(products)

            def productGroups = ProductGroup.withCriteria {
                ilike("name", "%" + params.term + "%")
            }
            productGroups.each { items << [id: it.id, name: it.name, class: it.class] }

            def categories = Category.withCriteria {
                ilike("name", "%" + params.term + "%")
            }
            items.addAll(categories)
        }

        // Convert from products to json objects
        if (items) {
            // Make sure items are unique
            items = items.collect() { item ->
                def type = item.class.simpleName
                def localizedName = localizationService.getLocalizedString(item.name)
                // Convert product attributes to JSON object attributes
                [
                        value    : type + ":" + item.id,
                        type     : type,
                        label    : localizedName + "(" + type + ")",
                        valueText: localizedName,
                ]
            }
        }

        if (items.size() == 0) {
            items << [value: null, label: warehouse.message(code: 'product.noProductsFound.message')]
        }

        log.info "Returning " + items.size() + " results for search " + params.term
        render items as JSON
    }


    def searchProductPackages = {

        log.info "Search product packages " + params
        def location = Location.get(session.warehouse.id)
        def results = productService.searchProductAndProductGroup(params.term)
        if (!results) {
            results = productService.searchProductAndProductGroup(params.term, true)
        }

        def productIds = results.collect { it[0] }
        def products = productService.getProducts(productIds as String[])

        def result = []
        def value = ""
        def productPackageName = ""
        products.each { product ->
            productPackageName = "EA/1"
            value = product?.productCode + " " + product?.name?.trim() + " (" + productPackageName + ")"
            value = value.trim()

            // Add the EACH level items
            result.add([id: product.id, value: value, type: "Product", quantity: null, group: null])
        }
        log.info result
        render result.sort { "${it.group}${it.value}" } as JSON
    }


    def searchProduct = {
        def location = Location.get(session.warehouse.id)
        def results = productService.searchProductAndProductGroup(params.term)
        if (!results) {
            results = productService.searchProductAndProductGroup(params.term, true)
        }

        def productIds = results.collect { it[0] }
        def products = productService.getProducts(productIds as String[])

        def result = []
        def value = ""
        def productPackageName = ""
        products.each { product ->
            productPackageName = "EA/1"
            value = product?.productCode + " " + product?.name?.trim() + " (" + productPackageName + ")"
            value = value.trim()

            // Add the EACH level items
            result.add([id: product.id, value: value, type: "Product", quantity: null, group: null])
        }
        log.info result
        render result.sort { "${it.group}${it.value}" } as JSON
    }


    def searchPersonByName = {
        def items = []
        def terms = params.term?.split(" ")
        terms?.each { term ->
            def result = Person.withCriteria {
                or {
                    ilike("firstName", term + "%")
                    ilike("lastName", term + "%")
                    ilike("email", term + "%")
                }
            }
            items.addAll(result)
        }
        items.unique { it.id }
        def json = items.collect {
            [id: it.id, value: it.name, label: it.name + " " + it.email]
        }
        render json as JSON
    }


    def globalSearch = {

        def minLength = grailsApplication.config.openboxes.typeahead.minLength
        if (params.term && params.term.size() < minLength) {
            render([:] as JSON)
            return
        }

        def terms = params.term?.split(" ")

        // FIXME Should replace this with an elasticsearch implementation
        // Get all products that match terms
        def products = productService.searchProducts(terms, [])

        products = products.unique()

        // Only calculate quantities if there are products - otherwise this will calculate quantities for all products in the system
        def location = Location.get(session.warehouse.id)
        def quantityMap = inventorySnapshotService.getQuantityOnHandByProduct(location)

        if (terms) {
            products = products.sort() {
                a, b ->
                    (terms.any {
                        a?.productCode?.contains(it) ? a.productCode : null
                    }) <=> (terms.any { b?.productCode.contains(it) ? b.productCode : null }) ?:
                            (terms.any { a?.name.contains(it) ? a.name : null }) <=> (terms.any {
                                b?.name.contains(it) ? b.name : null
                            })
            }
            products = products.reverse()
        }

        def items = []
        items.addAll(products)
        items.unique { it.id }
        def json = items.collect { Product product ->
            def quantity = quantityMap[product] ?: 0
            quantity = " [" + quantity + " " + (product?.unitOfMeasure ?: "EA") + "]"
            def type = product.class.simpleName.toLowerCase()
            [
                    id   : product.id,
                    type : product.class,
                    url  : request.contextPath + "/" + type + "/redirect/" + product.id,
                    value: product.name,
                    label: product.productCode + " " + product.name + " " + quantity,
                    color: product.color
            ]
        }
        render json as JSON
    }

    @CacheFlush("quantityOnHandCache")
    def flushQuantityOnHandCache = {
        redirect(controller: "inventory", action: "analyze")
    }

    @Cacheable("quantityOnHandCache")
    def calculateQuantityOnHandByProduct = {

        log.info "Calculating quantity on hand by product ..." + params

        def items = []
        def startTime = System.currentTimeMillis()
        def location = Location.get(session.warehouse.id)
        def quantityMap = inventoryService.getQuantityByProductMap(session.warehouse.id)
        def inventoryStatusMap = dashboardService.getInventoryStatusAndLevel(location)
        quantityMap.each { Product product, value ->
            def inventoryLevel = inventoryStatusMap[product]?.inventoryLevel
            def status = inventoryStatusMap[product]?.inventoryStatus
            def quantity = inventoryStatusMap[product]?.quantity ?: 0

            items << [
                    id             : product.id,
                    name           : product.name,
                    status         : status,
                    productCode    : product.productCode,
                    genericProduct : product?.genericProduct?.name ?: "Empty",
                    inventoryLevel : inventoryLevel,
                    minQuantity    : inventoryLevel?.minQuantity ?: 0,
                    maxQuantity    : inventoryLevel?.maxQuantity ?: 0,
                    reorderQuantity: inventoryLevel?.reorderQuantity ?: 0,
                    unitOfMeasure  : product.unitOfMeasure,
                    unitPrice      : product.pricePerUnit ?: 0,
                    onHandQuantity : value ?: 0.0,
                    totalValue     : (product.pricePerUnit ?: 0) * (value ?: 0)
            ]
        }


        def elapsedTime = (System.currentTimeMillis() - startTime) / 1000

        def inStockCount = items.findAll { it.status == "IN_STOCK" }.size()
        def reorderStockCount = items.findAll { it.status == "REORDER" }.size()
        def lowStockCount = items.findAll { it.status == "LOW_STOCK" }.size()
        def outOfStockCount = items.findAll { it.status == "STOCK_OUT" }.size()
        def overStockCount = items.findAll { it.status == "OVERSTOCK" }.size()

        def totalValue = items.sum { it.totalValue }
        def data = [
                totalValue       : totalValue,
                items            : items,
                elapsedTime      : elapsedTime,
                allStockCount    : items.size(),
                inStockCount     : inStockCount,
                reorderStockCount: reorderStockCount,
                lowStockCount    : lowStockCount,
                outOfStockCount  : outOfStockCount,
                overStockCount   : overStockCount
        ]

        log.info "Elapsed time " + elapsedTime + " s"
        render text: "${params.callback}(${data as JSON})", contentType: "application/javascript", encoding: "UTF-8"
    }

    /**
     * Analytics > Inventory Browser > Data Table
     */
    def getQuantityOnHandByProductGroup = {
        def startTime = System.currentTimeMillis()
        log.info "getQuantityOnHandByProductGroup " + params
        def aaData = new HashSet() //data.productGroupDetails.ALL.values()
        if (params["status[]"]) {
            def data = reportService.calculateQuantityOnHandByProductGroup(params.location.id)
            params["status[]"].split(",").each {
                log.info "Add entries from data.productGroupDetails[${it}]"
                def entry = data.productGroupDetails[it]
                if (entry) {
                    aaData += entry.values()
                }
            }
        }

        def totalValue = 0
        totalValue = aaData.sum { it.totalValue ?: 0 }
        NumberFormat numberFormat = NumberFormat.getNumberInstance()
        String currencyCode = grailsApplication.config.openboxes.locale.defaultCurrencyCode ?: "USD"
        numberFormat.currency = Currency.getInstance(currencyCode)
        numberFormat.maximumFractionDigits = 2
        numberFormat.minimumFractionDigits = 2
        def totalValueFormatted = numberFormat.format(totalValue ?: 0)
        log.info "totalValue = " + totalValueFormatted

        render(["aaData"        : aaData,
                "processingTime": "Took " + (System.currentTimeMillis() - startTime) + " ms to process",
                totalValue      : totalValue, totalValueFormatted: totalValueFormatted] as JSON)
    }

    def getSummaryByProductGroup = {
        log.info "getSummaryByProductGroup " + params
        def data = reportService.calculateQuantityOnHandByProductGroup(params.location.id)

        render(data.productGroupSummary as JSON)
    }

    def mostRecentQuantityOnHand = {
        def product = Product.get(params.id)
        def location = Location.get(session?.warehouse?.id)
        def object = inventoryService.getMostRecentQuantityOnHand(product, location)
        render([mostRecentQuantityOnHand: object] as JSON)
    }


    def mostRecentQuantityOnHandByLocation = {
        def location = Location.get(session?.warehouse?.id)
        def results = inventoryService.getMostRecentQuantityOnHand(location)
        render([results: results] as JSON)
    }

    def quantityMap = {
        def location = Location.get(session?.warehouse?.id)
        def quantityMap = inventoryService.getQuantityMap(location)
        render([quantityMap: quantityMap] as JSON)
    }


    def distinctProducts = {
        def location = Location.get(session?.warehouse?.id)
        def products = inventoryService.getDistinctProducts(location)
        render([products: null] as JSON)
    }

    def scanBarcode = {
        log.info "Scan barcode: " + params

        def url
        def type
        def barcode = params.barcode

        def product = Product.findByProductCode(barcode)
        if (product) {
            url = g.createLink(controller: "inventoryItem", action: "showStockCard", id: product.id, absolute: true)
            type = "stock card"
        } else {
            def requisition = Requisition.findByRequestNumber(barcode)
            if (requisition) {
                url = g.createLink(controller: "requisition", action: "show", id: requisition.id, absolute: true)
                type = "requisition"
            } else {
                def shipment = Shipment.findByShipmentNumber(barcode)
                if (shipment) {
                    url = g.createLink(controller: "shipment", action: "showDetails", id: shipment.id, absolute: true)
                    type = "shipment"
                } else {
                    def purchaseOrder = Order.findByOrderNumber(barcode)
                    if (purchaseOrder) {
                        url = g.createLink(controller: "purchaseOrder", action: "show", id: purchaseOrder.id, absolute: true)
                        type = "purchase order"
                    }
                }
            }
        }
        render([url: url, type: type, barcode: barcode] as JSON)
    }


    def getInventorySnapshotDetails = { InventorySnapshotCommand command ->

        // Get the most recent inventory snapshot data for data table
        if (!command.date) {
            command.date = inventorySnapshotService.getMostRecentInventorySnapshotDate()
        }

        def data = inventorySnapshotService.getInventorySnapshots(command.product, command.location, command.date)
        def defaultLabel = "${g.message(code: 'default.label')}"
        def defaultExpirationDate = "${g.message(code: 'default.never.label')}"
        data = data.collect {
            [
                    date          : it.date?.format(Constants.DEFAULT_DATE_FORMAT),
                    productCode   : it?.productCode,
                    lotNumber     : it?.lotNumber ?: defaultLabel,
                    expirationDate: it.inventoryItem?.expirationDate?.format(Constants.EXPIRATION_DATE_FORMAT) ?: defaultExpirationDate,
                    binLocation   : it.binLocationName ?: defaultLabel,
                    quantityOnHand: it.quantityOnHand
            ]
        }
        render([data: data] as JSON)
    }

    def getQuantityOnHandByMonth = {
        Location location = Location.get(params.location.id)
        Product product = Product.get(params.product.id)

        // Determine date range for inventory snapshot graph
        Integer numMonths = (params.numMonths as int) ?: 12
        Date startDate = new Date()
        Date endDate = inventorySnapshotService.getMostRecentInventorySnapshotDate()?:new Date()
        use(TimeCategory) { startDate = startDate - numMonths.months }

        // Retrieve and transform data for time-series graph
        def data = inventorySnapshotService.getQuantityOnHandBetweenDates(product, location, startDate, endDate)
        data = data.collect {
            [
                    it[0].time, // time in milliseconds
                    it[3]       // quantity on hand
            ]
        }

        render([data: data] as JSON)
    }

    @Cacheable("dashboardCache")
    def getFastMovers = {
        def dateFormat = new SimpleDateFormat("MM/dd/yyyy")
        def date = new Date()
        if (params.date) {
            date = dateFormat.parse(params.date)
            date.clearTime()
        }
        def location = Location.get(params?.location?.id ?: session?.warehouse?.id)
        def data = dashboardService.getFastMovers(location, date, params.max as int)

        render([aaData: data?.results ?: []] as JSON)
    }

    def getOrderItem = {
        def orderItem = OrderItem.get(params.id)
        render([id: orderItem.id, product: orderItem.product, order: orderItem.order, quantity: orderItem.quantity, unitPrice: orderItem.unitPrice] as JSON)
    }


    def enableCalculateHistoricalQuantityJob = {
        CalculateHistoricalQuantityJob.enabled = true
        render([message: "CalculateHistoricalQuantityJob has been ${CalculateHistoricalQuantityJob.enabled ? 'enabled' : 'disabled'}"] as JSON)
    }

    def disableCalculateHistoricalQuantityJob = {
        CalculateHistoricalQuantityJob.enabled = false
        render([message: "CalculateHistoricalQuantityJob has been ${CalculateHistoricalQuantityJob.enabled ? 'enabled' : 'disabled'}"] as JSON)
    }

    def statusCalculateHistoricalQuantityJob = {
        render "${CalculateHistoricalQuantityJob.enabled ? 'enabled' : 'disabled'}"
    }

    @Cacheable("dashboardCache")
    def getBinLocationSummary = {
        String locationId = params?.location?.id ?: session?.warehouse?.id
        Location location = Location.get(locationId)
        def binLocations = inventorySnapshotService.getQuantityOnHandByBinLocation(location)

        def data = inventoryService.getBinLocationSummary(binLocations)
        render(data as JSON)
    }

    def getBinLocationReport = {
        log.info "binLocationReport: " + params
        String locationId = params?.location?.id ?: session?.warehouse?.id
        Location location = Location.get(locationId)
        def data = inventorySnapshotService.getQuantityOnHandByBinLocation(location)

        if (params.status) {
            data = data.findAll { it.status == params.status }
        }

        def hasRoleFinance = userService.hasRoleFinance(session?.user)

        // Flatten the data to make it easier to display
        data = data.collect {
            def quantity = it?.quantity ?: 0
            def unitCost = hasRoleFinance ? (it?.product?.pricePerUnit ?: 0.0) : null
            def totalValue = hasRoleFinance ? g.formatNumber(number: quantity * unitCost) : null
            [
                    id            : it.product?.id,
                    status        : g.message(code: "binLocationSummary.${it.status}.label"),
                    productCode   : it.product?.productCode,
                    productName   : it?.product?.name,
                    productGroup  : it?.product?.genericProduct?.name,
                    category      : it?.product?.category?.name,
                    lotNumber     : it?.inventoryItem?.lotNumber,
                    expirationDate: g.formatDate(date: it?.inventoryItem?.expirationDate, format: "dd/MMM/yyyy"),
                    unitOfMeasure : it?.product?.unitOfMeasure,
                    binLocation   : it?.binLocation?.name,
                    quantity      : quantity,
                    unitCost      : unitCost,
                    totalValue    : totalValue
            ]
        }
        render(["aaData": data] as JSON)
    }

    def getDetailedOrderReport = {
        def location = Location.get(session.warehouse.id)
        def items = orderService.getPendingInboundOrderItems(location)
        items += shipmentService.getPendingInboundShipmentItems(location)

        def data = items.collect {
            def isOrderItem = it instanceof OrderItem
            [
                    productCode  : it.product.productCode,
                    productName  : it.product.name,
                    qtyOrderedNotShipped : isOrderItem ? it.quantityRemaining : '',
                    qtyShippedNotReceived : isOrderItem ? '' : it.quantityRemaining,
                    orderNumber  : isOrderItem ? it.order.orderNumber : (it.shipment.isFromPurchaseOrder ? it.orderNumber : ''),
                    orderDescription  : isOrderItem ? it.order.name : (it.shipment.isFromPurchaseOrder ? it.orderName : ''),
                    supplierOrganization  : isOrderItem ? it.order?.origin?.organization?.name : it.shipment?.origin?.organization?.name,
                    supplierLocation  : isOrderItem ? it.order.origin.name : it.shipment.origin.name,
                    supplierLocationGroup  : isOrderItem ? it.order?.origin?.locationGroup?.name : it.shipment?.origin?.locationGroup?.name,
                    estimatedGoodsReadyDate  : isOrderItem ? it.estimatedReadyDate?.format("MM/dd/yyyy") : '',
                    shipmentNumber  : isOrderItem ? '' : it.shipment.shipmentNumber,
                    shipDate  : isOrderItem ? '' : it.shipment.expectedShippingDate?.format("MM/dd/yyyy"),
                    shipmentType  : isOrderItem ? '' : it.shipment.shipmentType.name
            ]
        }
        render(["aaData": data] as JSON)
    }

    def getTransactionReport = { TransactionReportCommand command ->

        Date startDate = command.startDate
        Date endDate = command.endDate + 1
        Location location = command.location ?: Location.get(session.warehouse.id)

        Category category = command.category
        List<Tag> tagList = []
        List<ProductCatalog> catalogList = []

        if (params.tags) {
            params.tags.split(",").each { tagId ->
                Tag tag = Tag.findById(tagId)
                if (tag) {
                    tagList << tag
                }
            }
        }

        if (params.catalogs) {
            params.catalogs.split(",").each { catalogId ->
                ProductCatalog catalog = ProductCatalog.findById(catalogId)
                if (catalog) {
                    catalogList << catalog
                }
            }
        }

        if (!category) {
            category = productService.getRootCategory()
        }

        List<Category> categories = []
        Boolean includeCategoryChildren = params.includeCategoryChildren
        if (includeCategoryChildren) {
            categories = category.children
            categories << category
        } else {
            categories << category
        }

        // FIXME Command validation not working so we're doing it manually
        if (!startDate || !endDate || !location) {
            throw new IllegalArgumentException("All parameter fields are required")
        }

        if (!startDate.before(endDate)) {
            throw new IllegalArgumentException("Start date must occur before end date")
        }

        if (endDate.after(new Date()+1)) {
            throw new IllegalArgumentException("End date must occur on or before today")
        }

        if (command.refreshBalances) {
            log.info "Refreshing inventory snapshot for ${startDate} and location ${location}"
            inventorySnapshotService.populateInventorySnapshots(startDate, command.location)
            log.info "Refreshing inventory snapshot for ${endDate} and location ${location}"
            inventorySnapshotService.populateInventorySnapshots(endDate, command.location)
        }

        def data = (params.format == "text/csv") ?
                inventorySnapshotService.getTransactionReportDetails(location, categories, tagList, catalogList, startDate, endDate) :
                inventorySnapshotService.getTransactionReportSummary(location, categories, tagList, catalogList, startDate, endDate)

        if (params.format == "text/csv") {
            String csv = dataService.generateCsv(data)
            response.setHeader("Content-disposition", "attachment; filename=\"Transaction-Report-${startDate.format("yyyyMMdd")}-${endDate.format("yyyyMMdd")}.csv\"")
            render(contentType: "text/csv", text: csv.toString(), encoding: "UTF-8")
            return
        }
        render(["aaData": data] as JSON)
    }

    def getTransactionReportDetails = { TransactionReportCommand command ->
        String locationId = params?.location?.id ?: session?.warehouse?.id
        Location location = Location.get(locationId)
        Product product = Product.findByProductCode(params.productCode)
        Date startDate = command.startDate
        Date endDate = command.endDate + 1

        def balanceOpeningBinLocations = inventorySnapshotService.getQuantityOnHandByBinLocation(location, startDate, [product])
        def balanceClosingBinLocations = inventorySnapshotService.getQuantityOnHandByBinLocation(location, endDate, [product])

        def transactionsByTransactionCode = TransactionFact.createCriteria().list {
            projections {
                productKey {
                    property("productCode")
                }
                transactionDateKey {
                    property("date")
                }
                transactionTypeKey {
                    property("transactionCode")
                    property("transactionTypeName")
                }
                property("quantity")
            }

            transactionDateKey {
                between("date", startDate, endDate)
            }
            locationKey {
                eq("locationId", location.id)
            }
            productKey {
                eq("productCode", product.productCode)
            }

            transactionDateKey {
                order("date")
            }
        }


        def balanceOpening = balanceOpeningBinLocations.quantity?.sum() ?: 0
        def balanceClosing = balanceClosingBinLocations.quantity?.sum() ?: 0
        def balanceRunning = balanceOpening
        transactionsByTransactionCode = transactionsByTransactionCode.collect {
            def quantity = it[4]
            def transactionCode = it[2]

            if (transactionCode == "PRODUCT_INVENTORY") {
                balanceRunning = quantity
                quantity = null
            } else if (transactionCode == "DEBIT") {
                balanceRunning -= quantity
            } else if (transactionCode == "CREDIT") {
                balanceRunning += quantity
            }

            [
                    transactionDate    : it[1]?.format("dd MMM yyyy"),
                    transactionTime    : it[1]?.format("HH:mm:ss"),
                    transactionCode    : transactionCode,
                    transactionTypeName: LocalizationUtil.getLocalizedString(it[3], session.locale),
                    quantity           : quantity,
                    balance            : balanceRunning,
            ]
        }

        log.info("balanceOpening: " + balanceOpening)

        transactionsByTransactionCode.add(0, [
                transactionDate    : startDate?.format("dd MMM yyyy"),
                transactionTime    : startDate?.format("HH:mm:ss"),
                transactionCode    : "BALANCE_OPENING",
                transactionTypeName: "Opening Balance",
                quantity           : null,
                balance            : balanceOpening])

        transactionsByTransactionCode.add([
                transactionDate    : endDate?.format("dd MMM yyyy"),
                transactionTime    : endDate?.format("HH:mm:ss"),
                transactionCode    : "BALANCE_CLOSING",
                transactionTypeName: "Closing Balance",
                quantity           : null,
                balance            : balanceClosing])

        log.info "transactionsByTransactionCode: " + transactionsByTransactionCode

        // Flatten the data to make it easier to display
        def data = transactionsByTransactionCode

        render(["aaData": data] as JSON)
    }


    def getShipmentsWithInvalidStatus = {
        def shipments = shipmentService.shipmentsWithInvalidStatus
        render([count: shipments.size(), shipments: shipments] as JSON)
    }

    def fixShipmentsWithInvalidStatus = {
        def count = shipmentService.fixShipmentsWithInvalidStatus()
        render([count: count] as JSON)
    }

    @Cacheable("dashboardCache")
    def getDashboardActivity = {

        List activityList = []
        def currentUser = User.get(session?.user?.id)
        def location = Location.get(session.warehouse.id)
        def daysToInclude = params.daysToInclude ? Integer.parseInt(params.daysToInclude) : 7

        // Find recent requisition activity
        def requisitions = Requisition.executeQuery("""select distinct r from Requisition r where (r.isTemplate = false or r.isTemplate is null) and r.lastUpdated >= :lastUpdated and (r.origin = :origin or r.destination = :destination)""",
                ['lastUpdated': new Date() - daysToInclude, 'origin': location, 'destination': location])
        requisitions.each {
            def link = "${createLink(controller: 'requisition', action: 'show', id: it.id)}"
            def user = (it.dateCreated == it.lastUpdated) ? it?.createdBy : it?.updatedBy
            def activityType = (it.dateCreated == it.lastUpdated) ? "dashboard.activity.created.label" : "dashboard.activity.updated.label"
            def username = user?.name ?: "${warehouse.message(code: 'default.nobody.label', default: 'nobody')}"
            activityType = "${warehouse.message(code: activityType)}"
            activityList << [
                    type       : "basket",
                    label      : "${warehouse.message(code: 'dashboard.activity.requisition.label', args: [link, it.name, activityType, username])}",
                    url        : link,
                    dateCreated: it.dateCreated,
                    lastUpdated: it.lastUpdated,
                    requisition: it]
        }

        // Add recent shipments
        def shipments = Shipment.executeQuery("select distinct s from Shipment s where s.lastUpdated >= :lastUpdated and \
			(s.origin = :origin or s.destination = :destination)", ['lastUpdated': new Date() - daysToInclude, 'origin': location, 'destination': location])
        shipments.each {
            def link = "${createLink(controller: 'shipment', action: 'showDetails', id: it.id)}"
            def activityType = (it.dateCreated == it.lastUpdated) ? "dashboard.activity.created.label" : "dashboard.activity.updated.label"
            activityType = "${warehouse.message(code: activityType)}"
            activityList << [
                    type       : "lorry",
                    label      : "${warehouse.message(code: 'dashboard.activity.shipment.label', args: [link, it.name, activityType])}",
                    url        : link,
                    dateCreated: it.dateCreated,
                    lastUpdated: it.lastUpdated,
                    shipment   : it]
        }
        def shippedShipments = Shipment.executeQuery("SELECT s FROM Shipment s JOIN s.events e WHERE e.eventDate >= :eventDate and e.eventType.eventCode = 'SHIPPED'", ['eventDate': new Date() - daysToInclude])
        shippedShipments.each {
            def link = "${createLink(controller: 'shipment', action: 'showDetails', id: it.id)}"
            def activityType = "dashboard.activity.shipped.label"
            activityType = "${warehouse.message(code: activityType, args: [link, it.name, activityType, it.destination.name])}"
            activityList << [
                    type       : "lorry_go",
                    label      : activityType,
                    url        : link,
                    dateCreated: it.dateCreated,
                    lastUpdated: it.lastUpdated,
                    shipment   : it]
        }
        def receivedShipment = Shipment.executeQuery("SELECT s FROM Shipment s JOIN s.events e WHERE e.eventDate >= :eventDate and e.eventType.eventCode = 'RECEIVED'", ['eventDate': new Date() - daysToInclude])
        receivedShipment.each {
            def link = "${createLink(controller: 'shipment', action: 'showDetails', id: it.id)}"
            def activityType = "dashboard.activity.received.label"
            activityType = "${warehouse.message(code: activityType, args: [link, it.name, activityType, it.origin.name])}"
            activityList << [
                    type       : "lorry_stop",
                    label      : activityType,
                    url        : link,
                    dateCreated: it.dateCreated,
                    lastUpdated: it.lastUpdated,
                    shipment   : it]
        }

        def products = Product.executeQuery("select distinct p from Product p where p.lastUpdated >= :lastUpdated", ['lastUpdated': new Date() - daysToInclude])
        products.each {
            def link = "${createLink(controller: 'inventoryItem', action: 'showStockCard', params: ['product.id': it.id])}"
            def user = (it.dateCreated == it.lastUpdated) ? it?.createdBy : it.updatedBy
            def activityType = (it.dateCreated == it.lastUpdated) ? "dashboard.activity.created.label" : "dashboard.activity.updated.label"
            activityType = "${warehouse.message(code: activityType)}"
            def username = user?.name ?: "${warehouse.message(code: 'default.nobody.label', default: 'nobody')}"
            activityList << [
                    type       : "package",
                    label      : "${warehouse.message(code: 'dashboard.activity.product.label', args: [link, it.name, activityType, username])}",
                    url        : link,
                    dateCreated: it.dateCreated,
                    lastUpdated: it.lastUpdated,
                    product    : it]
        }

        // If the current location has an inventory, add recent transactions associated with that location to the activity list
        if (location?.inventory) {
            def transactions = Transaction.executeQuery("select distinct t from Transaction t where t.lastUpdated >= :lastUpdated and \
				t.inventory = :inventory", ['lastUpdated': new Date() - daysToInclude, 'inventory': location?.inventory])

            transactions.each {
                def link = "${createLink(controller: 'inventory', action: 'showTransaction', id: it.id)}"
                def user = (it.dateCreated == it.lastUpdated) ? it?.createdBy : it?.updatedBy
                def activityType = (it.dateCreated == it.lastUpdated) ? "dashboard.activity.created.label" : "dashboard.activity.updated.label"
                activityType = "${warehouse.message(code: activityType)}"
                def label = LocalizationUtil.getLocalizedString(it)
                def username = user?.name ?: "${warehouse.message(code: 'default.nobody.label', default: 'nobody')}"
                activityList << [
                        type       : "arrow_switch_bluegreen",
                        label      : "${warehouse.message(code: 'dashboard.activity.transaction.label', args: [link, label, activityType, username])}",
                        url        : link,
                        dateCreated: it.dateCreated,
                        lastUpdated: it.lastUpdated,
                        transaction: it]
            }
        }

        def users = User.executeQuery("select distinct u from User u where u.lastUpdated >= :lastUpdated", ['lastUpdated': new Date() - daysToInclude], [max: 10])
        users.each {
            def link = "${createLink(controller: 'user', action: 'show', id: it.id)}"
            def activityType = (it.dateCreated == it.lastUpdated) ? "dashboard.activity.created.label" : "dashboard.activity.updated.label"
            if (it.lastUpdated == it.lastLoginDate) {
                activityType = "dashboard.activity.loggedIn.label"
            }
            activityType = "${warehouse.message(code: activityType)}"
            activityList << [
                    type       : "user",
                    label      : "${warehouse.message(code: 'dashboard.activity.user.label', args: [link, it?.name, activityType])}",
                    url        : link,
                    dateCreated: it.dateCreated,
                    lastUpdated: it.lastUpdated,
                    user       : it]
        }

        // Sort list by date updated
        activityList = activityList.sort { it.lastUpdated }.reverse()

        // Transform activity list
        activityList = activityList.collect {
            [type: it.type, label: it.label, lastUpdated: it.lastUpdated?.format('MMM d hh:mma')]
        }


        render([aaData: activityList] as JSON)
    }

    def getProductDemandDetails = {
        Product product = Product.get(params.id)
        Location location = Location.get(session.warehouse.id)
        render([aaData: forecastingService.getDemandDetails(location, product)] as JSON)
    }

    def getProductDemandSummary = {
        Product product = Product.get(params.id)
        Location location = Location.get(session.warehouse.id)
        render([aaData: forecastingService.getDemandSummary(location, product)] as JSON)
    }


    def getForecastingData = {
        Product product = Product.get(params.id)
        Location location = Location.get(session.warehouse.id)
        def demandData = forecastingService.getDemand(location, product)
        render demandData as JSON
    }

    def productChanged = {
        Product product = Product.get(params.productId)
        Organization supplier = Organization.get(params.supplierId)
        List productSuppliers = []
        if (product && supplier) {
            productSuppliers = ProductSupplier.findAllByProductAndSupplier(product, supplier)
        }
        productSuppliers = productSuppliers.collect {[
            id: it.id,
            code: it.code,
            supplierCode: it.supplierCode,
            text: it.code,
            manufacturerCode: it.manufacturerCode,
            manufacturer: it.manufacturer?.id,
        ]}

        render([productSupplierOptions: productSuppliers] as JSON)
    }

    def productSupplierChanged = {
        ProductSupplier productSupplier = ProductSupplier.findById(params.productSupplierId)
        ProductPackage productPackage = productSupplier?.defaultProductPackage
        render([
                unitPrice: productPackage?.price ? g.formatNumber(number: productPackage?.price) : null,
                supplierCode: productSupplier?.supplierCode,
                manufacturer: productSupplier?.manufacturer,
                manufacturerCode: productSupplier?.manufacturerCode,
                minOrderQuantity: productSupplier?.minOrderQuantity,
                quantityPerUom: productPackage?.quantity,
                unitOfMeasure: productPackage?.uom,
        ] as JSON)
    }
}


class InventorySnapshotCommand {

    Date date
    Location location
    Product product
    InventoryItem inventoryItem
    Location binLocation
    BigDecimal quantity


}

class TransactionReportCommand {
    Date startDate
    Date endDate
    Location location
    List<TransactionType> transactionTypes
    Category category
    Boolean refreshBalances = Boolean.FALSE
}
