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

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.validation.Validateable
import grails.validation.ValidationException
import groovy.time.TimeCategory
import org.apache.commons.collections.FactoryUtils
import org.apache.commons.collections.list.LazyList
import org.apache.commons.lang.StringEscapeUtils
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.DefaultNullableCommand
import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.User
import org.pih.warehouse.importer.CSVUtils
import org.pih.warehouse.importer.InventoryExcelImporter
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.DateUtil
import org.pih.warehouse.core.Location
import org.pih.warehouse.report.InventoryReportCommand
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.commons.CommonsMultipartFile

import java.text.SimpleDateFormat

@Transactional
class InventoryController {

    def dataSource
    def productService
    def dashboardService
    def inventoryService
    def requisitionService
    def inventorySnapshotService
    def productAvailabilityService
    def userService
    def uploadService
    def documentService
    TransactionIdentifierService transactionIdentifierService
    def forecastingService

    static allowedMethods = [show: "GET", search: "POST", download: "GET"]

    def index() {
        redirect(action: "browse")
    }

    def manage(ManageInventoryCommand command) {
        [command: command]
    }

    def cycleCount() {
        render(view: "/common/react")
    }

    def binLocations() {
        Location location = Location.load(session.warehouse.id)
        List binLocations = productAvailabilityService.getQuantityOnHandByBinLocation(location)

        def data = binLocations.collect {
            [
                    it?.inventoryItem?.product?.productCode,
                    it?.inventoryItem?.product?.name,
                    it?.binLocation?.name,
                    it?.inventoryItem?.lotNumber,
                    it?.inventoryItem?.expirationDate ? Constants.EXPIRATION_DATE_FORMATTER.format(it?.inventoryItem?.expirationDate) : null,
                    it?.quantity,
                    it?.quantity,
                    "None"
            ]
        }

        def results = ["aaData": data]
        render(results as JSON)
    }

    def editBinLocation() {
        Product product = Product.findByProductCode(params.productCode)
        Location location = Location.get(session.warehouse.id)
        Location binLocation = Location.findByParentLocationAndName(location, params.binLocation)
        InventoryItem inventoryItem = inventoryService.findInventoryItemByProductAndLotNumber(product, params.lotNumber)
        Integer quantity = inventoryService.getQuantityFromBinLocation(location, binLocation, inventoryItem)
        [location: location, binLocation: binLocation, inventoryItem: inventoryItem, quantity: quantity]
    }

    def saveInventoryChanges(ManageInventoryCommand command) {
        Transaction transaction = new Transaction(params)
        try {
            //transaction.transactionDate = params.transactionDate
            transaction.createdBy = User.load(session.user.id)
            transaction.inventory = Location.load(session.warehouse.id).inventory

            command.entries.each { entry ->
                if (entry?.quantity > 0) {
                    def transactionEntry = new TransactionEntry()
                    transactionEntry.inventoryItem = entry.inventoryItem
                    transactionEntry.product = entry.inventoryItem.product
                    transactionEntry.quantity = entry.quantity
                    transaction.addToTransactionEntries(transactionEntry)
                }
            }

            log.info("size " + transaction?.transactionEntries?.size())

            if (!transaction?.transactionEntries) {
                throw new ValidationException("Transaction entries must not be empty", transaction.errors)
            }

            log.info("validate: " + transaction.validate())

            if (transaction.validate() && transaction.save()) {
                flash.message = "Transaction ${transaction.id} saved"
            } else {
                throw new ValidationException("Transaction errors", transaction.errors)
            }
        } catch (Exception e) {
            command.errors = transaction.errors
            chain(action: "manage", model: [command: command], params: params)
            return
        }

        redirect(action: "manage", params: [tags: params.tags])

    }

    /**
     * Allows a user to browse the inventory for a particular warehouse.
     */
    //@Cacheable("inventoryControllerCache")
    def browse(InventoryCommand command) {
        if (!params.max) params.max = 10
        if (!params.offset) params.offset = 0

        // Set defaults
        command.location = command?.location ?: Location.get(session.warehouse.id)
        def category = params.categoryId ? Category.get(params.categoryId) : productService.getRootCategory()
        command.category = category?.id ? category : null
        command.catalogs = params.catalogs ? command.catalogs : null
        command.tags = params.tags ? command.tags : null
        command.maxResults = params?.max as Integer
        command.offset = params?.offset as Integer
        command.searchResults = productAvailabilityService.searchProducts(command)

        [commandInstance: command]
    }

    /**
     *
     */
    def save() {
        def warehouseInstance = Location.get(params.warehouse?.id)
        if (!warehouseInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'warehouse.label', default: 'Location'), params.id])}"
            redirect(action: "list")
        } else {
            warehouseInstance.inventory = new Inventory(params)
            //inventoryInstance.warehouse = session.warehouse;
            if (warehouseInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), warehouseInstance.inventory.id])}"
                redirect(action: "browse")
            } else {
                render(view: "create", model: [warehouseInstance: warehouseInstance])
            }
        }
    }

    /**
     *
     */
    def show() {
        def quantityMap = [:]
        def startTime = System.currentTimeMillis()
        def location = Location.get(session.warehouse.id)
        def inventoryInstance = Inventory.get(params.id)
        if (!inventoryInstance) {
            inventoryInstance = location.inventory
        }
        if (!inventoryInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), params.id])}"
            redirect(action: "list")
            return
        }


        def elapsedTime = (System.currentTimeMillis() - startTime)
        log.info("Show current inventory: " + (System.currentTimeMillis() - startTime) + " ms")
        [
                //inventoryMapping: inventoryMapping,
                location   : location,
                elapsedTime: elapsedTime,
                quantityMap: quantityMap
        ]

    }

    def search(QuantityOnHandReportCommand command) {
        def quantityMapByDate = [:]
        def products = []
        def startTime = System.currentTimeMillis()
        def startDate = command.startDate
        def endDate = command.endDate
        if (command.validate()) {
            if (!command?.locations) {
                command.locations = [Location.get(session?.warehouse?.id)]
            }
            if (command.startDate && command.endDate) {
                command.dates = getDatesBetween(startDate, endDate, command.frequency)
            } else if (command.startDate) {
                command?.dates << startDate
            } else if (command.endDate) {
                command?.dates << endDate
            }

            command.locations.each { location ->
                for (date in command?.dates) {
                    println "Get quantity map " + date + " location = " + location
                    def revisedDate = date
                    use(TimeCategory) {
                        revisedDate = revisedDate.plus(1.day)
                    }
                    def quantityMap = inventorySnapshotService.getQuantityOnHandByProduct(location, revisedDate)
                    def existingQuantityMap = quantityMapByDate[date]
                    if (existingQuantityMap) {
                        quantityMapByDate[date] = mergeQuantityMap(existingQuantityMap, quantityMap)
                    } else {
                        quantityMapByDate[date] = quantityMap
                    }

                    if (quantityMapByDate[date]?.size() > products.size()) {
                        products = quantityMapByDate[date].keySet()
                    }

                    println "quantityMap = " + quantityMap?.keySet()?.size() + " results "
                    println "Time " + (System.currentTimeMillis() - startTime) + " ms"
                }
            }

            command.products = products?.sort()
        }

        if (params.button == 'download') {
            if (command.products) {
                def date = new Date()
                response.setHeader("Content-disposition", "attachment; filename=\"Baseline-QoH-${date.format("yyyyMMdd-hhmmss")}.csv\"")
                response.contentType = "text/csv"
                def csv = inventoryService.exportBaselineQoH(command.products, quantityMapByDate)
                println "export products: " + csv
                render(contentType: "text/csv", text:  csv, encoding: "UTF-8")
            } else {
                render(text: 'No products found', status: 404)
            }
            return
        }


        render(view: "show", model: [quantityMapByDate: quantityMapByDate, command: command, elapsedTime: (System.currentTimeMillis() - startTime)])

    }


    def download(QuantityOnHandReportCommand command) {

        println "search " + params
        println "search " + command.location + " " + command.startDate
        def quantityMap = inventoryService.getQuantityOnHandAsOfDate(command.location, command.startDate, command.tags)
        if (quantityMap) {
            def statusMap = dashboardService.getInventoryStatus(command.location)
            def inventoryItems = []

            quantityMap.each { Product product, quantity ->
                def inventoryLevel = product.getInventoryLevel(command.location?.id)
                def quantityAvailableToPromise = inventoryService.getQuantityAvailableToPromise(product, command.location)

                inventoryItems << [
                        status: statusMap[product],
                        product: product,
                        quantity: quantity,
                        quantityAvailableToPromise: quantityAvailableToPromise,
                        inventoryLevel: inventoryLevel
                ]
            }

            def filename = "Stock report - " +
                    (command?.tag ? command?.tag?.tag : "All Products") + " - " +
                    command?.location?.name + " - " +
                    command?.startDate?.format("yyyyMMMdd") + ".csv"
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
            render(contentType: "text/csv", text: getCsvForProductMap(inventoryItems))
            return
        }
        flash.message = "There are no search results available to download - please try again."
        redirect(action: "show")

    }


    def addToInventory() {
        def inventoryInstance = Inventory.get(params.id)
        def productInstance = Product.get(params.product.id)

        if (!productInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'product.label', default: 'Product'), params?.product?.id])}"
            redirect(action: "browse")
        } else {
            def itemInstance = new InventoryItem(product: productInstance)
            if (!itemInstance.hasErrors() && itemInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), inventoryInstance.id])}"
                redirect(action: "browse", id: inventoryInstance.id)
            } else {
                flash.message = "${warehouse.message(code: 'inventory.unableToCreateItem.message')}"
            }
        }
    }


    def edit() {
        def inventoryInstance = Inventory.get(params.id)
        if (!inventoryInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), params.id])}"
            redirect(action: "list")
        } else {
            def productInstanceMap = Product.getAll().groupBy { it.productType }

            return [inventoryInstance: inventoryInstance, productInstanceMap: productInstanceMap]
        }
    }

    def update() {
        def inventoryInstance = Inventory.get(params.id)
        if (inventoryInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (inventoryInstance.version > version) {
                    inventoryInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'inventory.label', default: 'Inventory')] as Object[],
                            "Another user has updated this Inventory while you were editing")
                    render(view: "edit", model: [inventoryInstance: inventoryInstance])
                    return
                }
            }
            inventoryInstance.properties = params
            if (!inventoryInstance.hasErrors() && inventoryInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), inventoryInstance.id])}"
                redirect(action: "browse", id: inventoryInstance.id)
            } else {
                render(view: "edit", model: [inventoryInstance: inventoryInstance])
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete() {
        def inventoryInstance = Inventory.get(params.id)
        if (inventoryInstance) {
            try {
                inventoryInstance.delete(flush: true)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), params.id])}"
            redirect(action: "list")
        }
    }

    def addItem() {
        def inventoryInstance = Inventory.get(params?.inventory?.id)
        def productInstance = Product.get(params?.product?.id)
        def itemInstance = inventoryService.findByProductAndLotNumber(productInstance, params.lotNumber)
        if (itemInstance) {
            flash.message = "${warehouse.message(code: 'default.alreadyExists.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory item'), inventoryInstance.id])}"
            redirect(action: "show", id: inventoryInstance.id)
        } else {
            itemInstance = new InventoryItem(params)
            if (itemInstance.hasErrors() || !itemInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory item'), inventoryInstance.id])}"
                redirect(action: "show", id: inventoryInstance.id)
            } else {
                itemInstance.errors.each { println it }
                flash.message = "${warehouse.message(code: 'default.notUpdated.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory item'), inventoryInstance.id])}"
                render(view: "show", model: [inventoryInstance: inventoryInstance, itemInstance: itemInstance])
            }
        }
    }

    def deleteItem() {
        def itemInstance = InventoryItem.get(params.id)
        if (itemInstance) {
            try {
                itemInstance.delete(flush: true)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'inventoryItem.label', default: 'Inventory item'), params.id])}"
                redirect(action: "show", id: params.inventory.id)
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'inventoryItem.label', default: 'Inventory item'), params.id])}"
                redirect(action: "show", id: params.inventory.id)
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), params.id])}"
            redirect(action: "show", id: params.inventory.id)
        }
    }

    def listDailyTransactions() {
        def dateFormat = new SimpleDateFormat("dd/MM/yyyy")
        def dateSelected = (params.date) ? dateFormat.parse(params.date) : new Date()

        def transactionsByDate = Transaction.list().groupBy {
            DateUtil.clearTime(it?.transactionDate)
        }?.entrySet()?.sort { it.key }?.reverse()

        def transactions = Transaction.findAllByTransactionDate(dateSelected)

        [transactions: transactions, transactionsByDate: transactionsByDate, dateSelected: dateSelected]
    }

    private def determineCategories(params) {
        List<Category> categories = params.list('categories') ?
                Category.findAllByIdInList(params.list('categories')) : []

        // When accessing the page for the first time, the flag should be set to true
        // Initially there no parameter _includeSubcategories, only after running the report manually it is set
        params.includeSubcategories = params.containsKey("_includeSubcategories") ? params.includeSubcategories : true
        if (params.includeSubcategories) {
            categories = inventoryService.getExplodedCategories(categories)
        }
        return categories;
    }

    private def listStock(Map params, String methodName, String fileNamePrefix) {
        def location = Location.get(session.warehouse.id)
        List<Category> categories = this.determineCategories(params)

        def inventoryItems = dashboardService."$methodName"(location, categories)

        if (params.button == "download") {
            def filename = fileNamePrefix + location.name + ".csv"
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
            render(contentType: "text/csv", text: getCsvForProductMap(inventoryItems))
            return
        }

        render(view: "list", model: [availableItems: inventoryItems])
    }

    def list() {
        this.listStock(params, "getInventoryItems", "")
    }

    def listReconditionedStock() {
        this.listStock(params, "getReconditionedStock", "Reconditioned stock - ")
    }

    def listTotalStock() {
        this.listStock(params, "getTotalStock", "Total stock - ")
    }

    def listInStock() {
        this.listStock(params, "getInStock", "In stock - ")
    }

    def listLowStock() {
        this.listStock(params, "getLowStock", "Low stock - ")
    }

    def listReorderStock() {
        this.listStock(params, "getReorderStock", "Reorder stock - ")
    }

    def reorderReport() {
        Location location = Location.get(session.warehouse.id)
        def inventoryItems = dashboardService.getReorderReport(location)

        String filename = "Reorder report - " + location.name + ".csv"

        response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")

        def hasRoleFinance = userService.hasRoleFinance(session.user)

        def csv = ""
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.status.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.productCode.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'category.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.tags.label', default: 'Tags')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.unitOfMeasure.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.minQuantity.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.reorderQuantity.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.maxQuantity.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventory.averageMonthlyDemand.label', default: "Average Monthly Demand")}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventory.quantityAvailable.label', default: 'Quantity Available')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventory.quantityToOrder.label', default: 'Quantity to Order')}" + '"' + ","

        if (hasRoleFinance) {
            csv += '"' + "${warehouse.message(code: 'product.unitCost.label', default: 'Unit Cost')}" + '"' + ","
            csv += '"' + "${warehouse.message(code: 'inventory.expectedReorderCost.label', default: 'Expected Reorder Cost')}" + '"' + ","
        }

        csv += "\n"

        inventoryItems.each { inventoryItem ->
            def product = inventoryItem.product as Product
            def inventoryLevel = inventoryItem.inventoryLevel as InventoryLevel
            def status = inventoryItem.status
            def statusMessage = "${warehouse.message(code: 'enum.InventoryLevelStatusCsv.' + status)}"

            def monthlyDemand = forecastingService.getDemand(location, null, product)?.monthlyDemand ?: 0

            def quantityAvailableToPromise = inventoryItem.quantityAvailableToPromise ?: 0

            def quantityToOrder = inventoryLevel?.maxQuantity == null ? "No Max qty set  - review based on monthly demand" : inventoryLevel.maxQuantity - quantityAvailableToPromise

            csv += '"' + (statusMessage ?: "") + '"' + ","
            csv += '"' + (product.productCode ?: "") + '"' + ","
            csv += StringEscapeUtils.escapeCsv(product?.displayNameWithLocaleCode) + ","
            csv += '"' + (product?.category?.name ?: "") + '"' + ","
            csv += '"' + (product?.tagsToString() ?: "") + '"' + ","
            csv += '"' + (product?.unitOfMeasure ?: "") + '"' + ","
            csv += (inventoryLevel?.minQuantity ?: "") + ","
            csv += (inventoryLevel?.reorderQuantity ?: "") + ","
            csv += (inventoryLevel?.maxQuantity ?: "") + ","
            csv += monthlyDemand + ","
            csv += quantityAvailableToPromise + ","
            csv += quantityToOrder + ","

            if(hasRoleFinance) {
                csv += '"' + (inventoryItem.unitCost ?: "") + '"' + ","
                csv += '"' + (inventoryItem.expectedReorderCost ?: "") + '"' + ","
            }

            csv += "\n"
        }

        render(contentType: "text/csv", text: CSVUtils.prependBomToCsvString(csv))
    }

    def listQuantityOnHandZero() {
        this.listStock(params, "getQuantityOnHandZero", "Out of stock  - all - ")
    }

    def listHealthyStock() {
        this.listStock(params, "getHealthyStock", "Overstock - ")
    }

    def listOverStock() {
        this.listStock(params, "getOverStock", "Overstock - ")
    }

    def listOutOfStock() {
        def location = Location.get(session.warehouse.id)
        List<Category> categories = this.determineCategories(params)

        def inventoryItems = dashboardService.getOutOfStock(location, params.abcClass, categories)

        if (params.button == "download") {
            def filename = "Out of stock - supported - " + location.name + ".csv"
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
            render(contentType: "text/csv", text: getCsvForProductMap(inventoryItems))
            return
        }
        render(view: "list", model: [availableItems: inventoryItems])
    }


    def listExpiredStock(InventoryReportCommand command) {
        command.location = Location.get(session.warehouse.id)
        Boolean withBinLocation = params.boolean("withBinLocation")

        List<InventoryItem> inventoryItems = dashboardService.getExpiredStock(command)
        List<Category> categories = inventoryItems?.collect { it.product.category }?.unique()

        List<Map> data = []
        if (!inventoryItems.isEmpty()) {
            data = withBinLocation
                    ? productAvailabilityService.getAvailableQuantityOnHandByBinLocation(command.location, inventoryItems)
                    : productAvailabilityService.getQuantityOnHandByInventoryItem(command.location, inventoryItems)
                    .collect{ key, val -> [ inventoryItem: key, quantity: val ] }
        }

        if (params.format == "csv") {
            def filename = "Expired stock | " + command.location?.name + ".csv"
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
            render(contentType: "text/csv", text: getCsvForInventoryMap(data, withBinLocation))
            return
        }

        [
                data: data,
                categories: categories,
                command: command,
        ]
    }


    def listExpiringStock(InventoryReportCommand command) {
        command.location = Location.get(session.warehouse.id)
        Boolean withBinLocation = params.boolean("withBinLocation")

        List<InventoryItem> inventoryItems = dashboardService.getExpiringStock(command)
        List<Category> categories = inventoryItems?.collect { it?.product?.category }?.unique().sort {
            it.name
        }

        List<Map> data = []
        if (!inventoryItems?.isEmpty()) {
            data = withBinLocation
                    ? productAvailabilityService.getAvailableQuantityOnHandByBinLocation(command.location, inventoryItems)
                    : productAvailabilityService.getQuantityOnHandByInventoryItem(command.location, inventoryItems)
                    .collect{ key, val -> [ inventoryItem: key, quantity: val ] }
        }

        if (params.format == "csv") {
            def filename = "Expiring stock | " + command.location.name + ".csv"
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
            render(contentType: "text/csv", text: getCsvForInventoryMap(data, withBinLocation))
            return
        }

        [
                data: data,
                categories: categories,
                command: command
        ]
    }

    def exportLatestInventoryDate() {
        println params
        def location = Location.get(session.warehouse.id)

        if (location) {
            def date = new Date()
            response.setHeader("Content-disposition",
                    "attachment; filename=\"MostRecentStockCount-${date.format("yyyyMMdd-hhmmss")}.csv\"")
            response.contentType = "text/csv"
            render dashboardService.exportLatestInventoryDate(location)
        } else {
            //render(text: 'No products found', status: 404)
            response.sendError(404)
        }
    }

    /**
     * Used to create default inventory items.
     * @return
     */
    def createDefaultInventoryItems() {
        def products = inventoryService.findProductsWithoutEmptyLotNumber()
        products.each { product ->
            def inventoryItem = new InventoryItem()
            inventoryItem.product = product
            inventoryItem.lotNumber = null
            inventoryItem.expirationDate = null
            inventoryItem.save()
        }
        redirect(controller: "inventory", action: "showProducts")
    }


    def showProducts() {
        def products = inventoryService.findProductsWithoutEmptyLotNumber()
        [products: products]

    }

    def listTransactions() {

        Location location = Location.get(session.warehouse.id)
        def currentInventory = location.inventory

        Date transactionDateFrom = params.transactionDateFrom ? Date.parse("MM/dd/yyyy", params.transactionDateFrom) : null
        Date transactionDateTo = params.transactionDateTo ? Date.parse("MM/dd/yyyy", params.transactionDateTo) : null

        // we are only showing transactions for the inventory associated with the current warehouse
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        params.sort = params?.sort ?: "dateCreated"
        params.order = params?.order ?: "desc"


        def transactionType = TransactionType.get(params?.transactionType?.id)
        def transactions = Transaction.createCriteria().list(params) {
            and {
                eq("inventory", currentInventory)
                if (transactionType) {
                    eq("transactionType", transactionType)
                }
                if (params.transactionNumber) {
                    ilike("transactionNumber", "%" + params.transactionNumber + "%")
                }
                if (params.transactionDateFrom) {
                    ge("transactionDate", transactionDateFrom)
                }
                if (params.transactionDateTo) {
                    le("transactionDate", transactionDateTo)
                }
            }
            maxResults(params.max)
            order(params.sort, params.order)
        }

        render(view: "listTransactions", model: [transactionInstanceList: transactions,
                                                 transactionCount       : transactions.totalCount, transactionTypeSelected: transactionType])
    }

    def listAllTransactions() {
        redirect(action: "listTransactions")
    }

    def listPendingTransactions() {
        def transactions = Transaction.findAllByConfirmedOrConfirmedIsNull(Boolean.FALSE)
        render(view: "listTransactions", model: [transactionInstanceList: transactions])
    }

    def listConfirmedTransactions() {
        def transactions = Transaction.findAllByConfirmed(Boolean.TRUE)
        render(view: "listTransactions", model: [transactionInstanceList: transactions])
    }


    def deleteTransaction() {
        def transactionInstance = Transaction.get(params.id)

        if (transactionInstance) {
            try {

                inventoryService.deleteTransaction(transactionInstance)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'transaction.label', default: 'Transaction'), params.id])}"
                redirect(action: "listTransactions")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'transaction.label', default: 'Transaction'), params.id])}"
                redirect(action: "editTransaction", id: params.id)
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'transaction.label', default: 'Transaction'), params.id])}"
            redirect(action: "listTransactions")
        }
    }


    def saveTransaction() {
        log.debug "save transaction: " + params
        def transactionInstance = Transaction.get(params.id)
        // def inventoryInstance = Inventory.get(params.inventory.id);

        if (!transactionInstance) {
            transactionInstance = new Transaction()
        }

        transactionInstance.properties = params

        // either save as a local transfer, or a generic transaction
        // (catch any exceptions so that we display "nice" error messages)
        Boolean saved = null
        if (transactionInstance.validate() && !transactionInstance.hasErrors()) {
            try {
                transactionInstance.lastUpdated = new Date()
                saved = transactionInstance.save(flush: true)
            }
            catch (Exception e) {
                log.error("Unable to save transaction ", e)
            }
        }

        if (saved) {
            flash.message = "${warehouse.message(code: 'inventory.transactionSaved.message')}"
            redirect(action: "editTransaction", id: transactionInstance?.id)
        } else {
            flash.message = "${warehouse.message(code: 'inventory.unableToSaveTransaction.message')}"
            flash.errors = transactionInstance.errors
            redirect(action: "editTransaction", id: transactionInstance?.id)
        }
    }

    /**
     * Show the transaction.
     */
    def showTransaction() {
        def transactionInstance = Transaction.get(params.id)
        if (!transactionInstance) {
            flash.message = "${warehouse.message(code: 'inventory.noTransactionWithId.message', args: [params.id])}"
            transactionInstance = new Transaction()
        }

        def model = [transactionInstance: transactionInstance]
        render(view: "showTransaction", model: model)
    }

    /**
     * Show the transaction.
     */
    def showTransactionDialog() {
        def transactionInstance = Transaction.get(params.id)
        if (!transactionInstance) {
            flash.message = "${warehouse.message(code: 'inventory.noTransactionWithId.message', args: [params.id])}"
            transactionInstance = new Transaction()
        }

        def model = [
                transactionInstance : transactionInstance,
                productInstanceMap  : Product.list().groupBy { it.category },
                transactionTypeList : TransactionType.list(),
                locationInstanceList: Location.list(),
                warehouseInstance   : Location.get(session?.warehouse?.id)
        ]

        render(view: "showTransactionDialog", model: model)

    }


    def confirmTransaction() {
        def transactionInstance = Transaction.get(params?.id)
        if (transactionInstance?.confirmed) {
            transactionInstance?.confirmed = Boolean.FALSE
            transactionInstance?.confirmedBy = null
            transactionInstance?.dateConfirmed = null
            flash.message = "${warehouse.message(code: 'inventory.transactionHasBeenUnconfirmed.message')}"
        } else {
            transactionInstance?.confirmed = Boolean.TRUE
            transactionInstance?.confirmedBy = User.get(session?.user?.id)
            transactionInstance?.dateConfirmed = new Date()
            flash.message = "${warehouse.message(code: 'inventory.transactionHasBeenConfirmed.message')}"
        }
        redirect(action: "listTransactions")
    }

    def createInboundTransfer() {
        Location location = Location.get(session.warehouse.id)
        if (!location.supports(ActivityCode.RECEIVE_STOCK)) {
            throw new UnsupportedOperationException("Location ${location.name} does not support receipt transactions")
        }
        params.transactionType = Constants.TRANSFER_IN_TRANSACTION_TYPE_ID
        forward(action: "createTransaction")
    }

    def createOutboundTransfer() {
        Location location = Location.get(session.warehouse.id)
        if (!location.supports(ActivityCode.SEND_STOCK)) {
            throw new UnsupportedOperationException("Location ${location.name} does not support transfer transactions")
        }
        params.transactionType = Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID
        forward(action: "createTransaction")
    }

    def createAdjustment() {
        Location location = Location.get(session.warehouse.id)
        if (!location.supports(ActivityCode.ADJUST_INVENTORY)) {
            throw new UnsupportedOperationException("Location ${location.name} does not support adjustment transactions")
        }
        params.transactionType = Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID
        forward(action: "createTransaction")
    }

    def createConsumed() {
        Location location = Location.get(session.warehouse.id)
        if (!location.supports(ActivityCode.CONSUME_STOCK)) {
            throw new UnsupportedOperationException("Location ${location.name} does not support consumption transactions")
        }
        params.transactionType = Constants.CONSUMPTION_TRANSACTION_TYPE_ID
        forward(action: "createTransaction")
    }

    def createExpired() {
        params.transactionType = Constants.EXPIRATION_TRANSACTION_TYPE_ID
        forward(action: "createTransaction")
    }

    def createDamaged() {
        params.transactionType = Constants.DAMAGE_TRANSACTION_TYPE_ID
        forward(action: "createTransaction")
    }

    def createTransaction() {
        def command = new TransactionCommand()
        def warehouseInstance = Location.get(session?.warehouse?.id)
        def transactionInstance = new Transaction(params)

        def products = []

        // Process productId parameters from inventory browser
        if (params?.product?.id) {
            def productIds = params.list('product.id')
            productIds = productIds.collect { String.valueOf(it) }
            if (productIds) {
                products = Product.getAll(productIds)
                command.productInventoryItems = inventoryService.getInventoryItemsByProducts(warehouseInstance, productIds)

                command.binLocations = inventoryService.getProductQuantityByBinLocation(warehouseInstance, products)
            }
        }
        // If given a list of inventory items, we just return those inventory items
        else if (params?.inventoryItem?.id) {
            def inventoryItemIds = params.list('inventoryItem.id')
            def inventoryItems = inventoryItemIds.collect { InventoryItem.get(String.valueOf(it)) }

            def productIds = inventoryItems.collect { it?.product?.id }
            if (productIds) {
                products = Product.getAll(productIds)
            }
            command.binLocations = inventoryService.getBinLocationsByInventoryItems(warehouseInstance, inventoryItems)
        } else {
            throw new RuntimeException("You must select at least one product or inventory item")
        }

        println "Product inventory items " + command?.productInventoryItems

        command.transactionInstance = transactionInstance
        command.warehouseInstance = warehouseInstance

        command.quantityMap = inventoryService.getQuantityForInventory(warehouseInstance?.inventory, products)

        [command: command]

    }

    /**
     * Save a transaction that sets the current inventory level for stock.
     */
    def saveAdjustmentTransaction(TransactionCommand command) {
        log.info("Saving inventory adjustment " + params)
        log.info "Command: " + command

        def transaction = command?.transactionInstance
        def warehouseInstance = Location.get(session?.warehouse?.id)

        // Quantity cannot be changed to be less than 0
        command.transactionEntries.each {
            if (it.quantity < 0) {
                transaction.errors.rejectValue("transactionEntries", "transactionEntry.quantity.invalid", [it?.inventoryItem?.lotNumber] as Object[], "")
            }
        }

        // Check to see if there are errors, if not save the transaction
        if (!transaction.hasErrors()) {
            try {
                // Add validated transaction entries to the transaction we want to persist
                command.transactionEntries.each {
                    if (it.quantity != 0) {
                        def transactionEntry = new TransactionEntry()
                        transactionEntry.product = it.inventoryItem.product
                        transactionEntry.inventoryItem = it.inventoryItem
                        transactionEntry.binLocation = it.binLocation
                        transactionEntry.quantity = it.quantity
                        transactionEntry.comments = it.comment
                        transactionEntry.reasonCode = it.reasonCode
                        transaction.addToTransactionEntries(transactionEntry)
                    }
                }

                // Validate the transaction object
                if (!transaction.hasErrors() && transaction.validate()) {
                    transaction.save(failOnError: true)
                    flash.message = "Successfully saved transaction"
                    def productId = command.transactionEntries.first()?.inventoryItem?.product?.id
                    redirect(controller: "inventoryItem", action: "showStockCard", id: productId)
                }
            } catch (ValidationException e) {
                log.debug("caught validation exception " + e)
            }
        }

        // After the attempt to save the transaction, there might be errors on the transaction
        if (transaction.hasErrors()) {
            log.debug("has errors" + transaction.errors)

            // Get the list of products that the user selected from the inventory browser
            if (params.product?.id) {
                def productIds = params.list('product.id')
                def products = Product.findAllByIdInList(productIds)
                command.productInventoryItems = inventoryService.getInventoryItemsByProducts(warehouseInstance, productIds)
                command.binLocations = inventoryService.getProductQuantityByBinLocation(warehouseInstance, products)
            }
            // If given a list of inventory items, we just return those inventory items
            else if (params?.inventoryItem?.id) {
                def inventoryItemIds = params.list('inventoryItem.id')
                def inventoryItems = inventoryItemIds.collect {
                    InventoryItem.get(String.valueOf(it))
                }
                command?.productInventoryItems = inventoryItems.groupBy { it.product }
            }

            // Populate the command object and render the form view.
            command.transactionInstance = transaction
            command.warehouseInstance = warehouseInstance

            render(view: "createTransaction", model: [command: command])
        }
    }

    /**
     * Save a transaction that debits stock from the given inventory.
     *
     * TRANSFER_OUT, CONSUMED, DAMAGED, EXPIRED
     */

    //@CacheFlush("inventoryBrowserCache")
    def saveDebitTransaction(TransactionCommand command) {
        log.info("Saving debit transactions " + params)
        log.info("size: " + command?.transactionEntries?.size())

        // Data binding not working properly for nested objects of command objects
        command.transactionInstance = new Transaction(params.transactionInstance)

        // Get the products involved
        def productIds = params.list('product.id').collect { String.valueOf(it) }
        List products = Product.getAll(productIds)

        def transaction = command?.transactionInstance
        transaction.transactionNumber = transactionIdentifierService.generate(transaction)
        def warehouseInstance = Location.get(session?.warehouse?.id)
        def quantityMap = inventoryService.getQuantityForInventory(warehouseInstance?.inventory, products)

        // Quantity cannot be greater than on hand quantity
        command.transactionEntries.each {
            def onHandQuantity = quantityMap[it.inventoryItem]
            if (it.quantity > onHandQuantity) {
                transaction.errors.rejectValue("transactionEntries", "transactionEntry.quantity.invalid", [it?.inventoryItem?.lotNumber] as Object[], "")
            }
        }

        // Check to see if there are errors, if not save the transaction
        if (!transaction?.hasErrors()) {
            try {
                // Add validated transaction entries to the transaction we want to persist
                command.transactionEntries.each {
                    if (it.quantity) {
                        def transactionEntry = new TransactionEntry()
                        transactionEntry.inventoryItem = it.inventoryItem
                        transactionEntry.product = it.product
                        transactionEntry.quantity = it.quantity
                        transactionEntry.binLocation = it.binLocation
                        transaction.addToTransactionEntries(transactionEntry)
                    }
                }

                // Validate the transaction object
                if (!transaction?.hasErrors() && transaction?.validate()) {
                    transaction.save(failOnError: true)
                    flash.message = "Successfully saved transaction"
                    redirect(controller: "inventoryItem", action: "showStockCard", id: productIds[0])
                    return
                }
            } catch (ValidationException e) {
                log.debug("caught validation exception " + e)
            }
        }

        // After the attempt to save the transaction, there might be errors on the transaction
        if (transaction?.hasErrors()) {
            log.debug("has errors" + transaction.errors)

            // Get the list of products that the user selected from the inventory browser
            if (params.product?.id) {
                command.productInventoryItems = inventoryService.getInventoryItemsByProducts(warehouseInstance, productIds)
                command.binLocations = inventoryService.getProductQuantityByBinLocation(warehouseInstance, products)
            }
            // If given a list of inventory items, we just return those inventory items
            else if (params?.inventoryItem?.id) {
                def inventoryItemIds = params.list('inventoryItem.id')
                def inventoryItems = inventoryItemIds.collect {
                    InventoryItem.get(String.valueOf(it))
                }
                command?.productInventoryItems = inventoryItems.groupBy { it.product }
            }

            // Populate the command object and render the form view.
            command.transactionInstance = transaction
            command.warehouseInstance = warehouseInstance
            command.quantityMap = quantityMap

        }
        render(view: "createTransaction", model: [command: command])
    }


    /**
     * Save a transaction that debits stock from the given inventory.
     *
     * TRANSFER_IN
     */
    //@CacheFlush("inventoryBrowserCache")
    def saveCreditTransaction(TransactionCommand command) {

        log.debug("Saving credit transaction: " + params)
        def transactionInstance = command?.transactionInstance
        def warehouseInstance = Location.get(session?.warehouse?.id)

        // Quantity cannot be less than 0 or else it would be in a debit transaction
        command.transactionEntries.each {
            if (it.quantity < 0) {
                transactionInstance.errors.rejectValue("transactionEntries", "transactionEntry.quantity.invalid", [it?.inventoryItem?.lotNumber] as Object[], "")
            }
        }

        // We need to process each transaction entry to make sure that it has a valid inventory item (or we will create one if not)
        command.transactionEntries.each {
            if (!it.inventoryItem) {
                // Find an existing inventory item for the given lot number and product and description
                def inventoryItem = inventoryService.findInventoryItemByProductAndLotNumber(it.product, it.lotNumber)

                // If the inventory item doesn't exist, we create a new one
                if (!inventoryItem) {
                    inventoryItem = new InventoryItem()
                    inventoryItem.lotNumber = it.lotNumber
                    inventoryItem.expirationDate = (it.lotNumber) ? it.expirationDate : null
                    inventoryItem.product = it.product
                    if (inventoryItem.hasErrors() || !inventoryItem.save()) {
                        inventoryItem.errors.allErrors.each { error ->
                            command.errors.reject("inventoryItem.invalid",
                                    [inventoryItem, error.getField(), error.getRejectedValue()] as Object[],
                                    "[${error.getField()} ${error.getRejectedValue()}] - ${error.defaultMessage} ")

                        }
                    }
                }
                it.inventoryItem = inventoryItem
            }
        }

        // Now that all transaction entries in the command have inventory items,
        // we need to create a persistable transaction entry
        command.transactionEntries.each {
            def transactionEntry = new TransactionEntry(inventoryItem: it.inventoryItem,
                    product: it.inventoryItem.product, binLocation: it.binLocation, quantity: it.quantity)
            transactionInstance.addToTransactionEntries(transactionEntry)
        }

        // Check to see if there are errors, if not save the transaction
        if (!transactionInstance.hasErrors()) {
            try {
                // Validate the transaction object
                if (!transactionInstance.hasErrors() && transactionInstance.validate()) {
                    transactionInstance.save(failOnError: true)
                    flash.message = "Successfully saved transaction"
                    //redirect(controller: "inventory", action: "browse")
                    redirect(controller: "inventory", action: "browse")
                }
            } catch (ValidationException e) {
                log.debug("caught validation exception " + e)
            }
        }

        // Should be true if a validation exception was thrown
        if (transactionInstance.hasErrors()) {
            log.debug("has errors" + transactionInstance.errors)

            // Get the list of products that the user selected from the inventory browser
            if (params.product?.id) {
                def productIds = params.list('product.id')
                def products = productIds.collect { String.valueOf(it) }
                command.productInventoryItems = inventoryService.getInventoryItemsByProducts(warehouseInstance, products)
            }
            // If given a list of inventory items, we just return those inventory items
            else if (params?.inventoryItem?.id) {
                def inventoryItemIds = params.list('inventoryItem.id')
                def inventoryItems = inventoryItemIds.collect {
                    InventoryItem.get(String.valueOf(it))
                }
                command?.productInventoryItems = inventoryItems.groupBy { it.product }
            }

            // Populate the command object and render the form view.
            command.warehouseInstance = warehouseInstance

            render(view: "createTransaction", model: [command: command])
        }
    }

    def editTransaction() {
        def startTime = System.currentTimeMillis()
        log.info "edit transaction: " + params
        def transactionInstance = Transaction.get(params?.id)
        if (!transactionInstance) {
            flash.message = "${warehouse.message(code: 'inventory.noTransactionWithId.message', args: [params.id])}"
            redirect(action: "listTransactions")
            return
        }

        def warehouseInstance = Location.get(session?.warehouse?.id)
        def products = transactionInstance?.transactionEntries.collect { it.inventoryItem.product }
        def inventoryItems = InventoryItem.findAllByProductInList(products)
        def model = [
                inventoryItemsMap   : inventoryItems.groupBy { it.product?.id },
                transactionInstance : transactionInstance ?: new Transaction(),
                transactionTypeList : TransactionType.list(),
                locationInstanceList: Location.findAllByParentLocationIsNull(),
                quantityMap         : [:],
                warehouseInstance   : warehouseInstance
        ]

        println "Edit transaction " + (System.currentTimeMillis() - startTime) + " ms"

        render(view: "editTransaction", model: model)

    }


    /**
     * TODO These are the same methods used in the inventory browser.  Need to figure out a better
     * way to handle this (e.g. through a generic ajax call or taglib).
     */
    def removeCategoryFilter() {
        def category = Category.get(params?.categoryId)
        if (category)
            session.inventoryCategoryFilters.remove(category?.id)
        redirect(action: browse)
    }

    def clearAllFilters() {
        session.inventoryCategoryFilters = []
        session.inventorySearchTerms = []
        redirect(action: browse)
    }
    def addCategoryFilter() {
        def category = Category.get(params?.categoryId)
        if (category && !session.inventoryCategoryFilters.contains(category?.id))
            session.inventoryCategoryFilters << category?.id
        redirect(action: browse)
    }
    def narrowCategoryFilter() {
        def category = Category.get(params?.categoryId)
        session.inventoryCategoryFilters = []
        if (category && !session.inventoryCategoryFilters.contains(category?.id))
            session.inventoryCategoryFilters << category?.id
        redirect(action: browse)
    }
    def removeSearchTerm() {
        if (params.searchTerm)
            session.inventorySearchTerms.remove(params.searchTerm)
        redirect(action: browse)
    }


    def upload() {
        def inventoryList = [:]
        if (request.method == "POST") {
            File localFile = null
            MultipartHttpServletRequest mpr = (MultipartHttpServletRequest) request
            CommonsMultipartFile uploadFile = (CommonsMultipartFile) mpr.getFile("file")
            if (!uploadFile?.empty) {
                try {
                    localFile = uploadService.createLocalFile(uploadFile.originalFilename)
                    uploadFile.transferTo(localFile)
                } catch (Exception e) {
                    throw new RuntimeException(e)
                }
            }

            //Iterate through bookList and create/persists your domain instances
            def excelImporter = new InventoryExcelImporter(localFile.absolutePath)
            inventoryList = excelImporter.data
            println inventoryList
        }


        [inventoryList: inventoryList]

    }

    def downloadTemplate() {
        Location location = Location.load(session.warehouse.id)
        List data = productAvailabilityService.getQuantityOnHandByBinLocation(location)
        def rows = []

        if (!data) {
            def row = [
                    'Product code'    : '',
                    'Product name'    : '',
                    'Lot number'      : '',
                    'Expiration date' : '',
                    'Bin location'    : '',
                    'OB QOH'          : '',
                    'Physical QOH'    : '',
                    'Comment'         : '',
            ]

            rows << row
        }

        data.findAll { it.quantity }.each {
            def row = [
                    'Product code'    : it.product?.productCode,
                    'Product name'    : it.product?.name,
                    'Lot number'      : it.inventoryItem?.lotNumber,
                    'Expiration date' : it.inventoryItem?.expirationDate?.format("MM/dd/yyyy"),
                    'Bin location'    : it.binLocation?.name,
                    'OB QOH'          : it.quantity,
                    'Physical QOH'    : '',
                    'Comment'         : '',
            ]

            rows << row
        }
        response.setHeader("Content-disposition", "attachment; filename=\"inventory.xls\"")
        response.setContentType("application/vnd.ms-excel")
        documentService.generateInventoryTemplate(response.outputStream, rows)
        response.outputStream.flush()
    }

    private def mergeQuantityMap(oldQuantityMap, newQuantityMap) {
        oldQuantityMap.each { product, oldQuantity ->
            def newQuantity = newQuantityMap[product] ?: 0
            oldQuantityMap[product] = newQuantity + oldQuantity

        }
        return oldQuantityMap
    }

    private def getDatesBetween(startDate, endDate, frequency) {

        def count = 0
        def dates = []
        if (startDate.before(endDate)) {
            def date = startDate
            def end = endDate
            use(TimeCategory) {
                end = endDate.plus(1.day)
            }
            while (date.before(end)) {
                println "Start date = " + date + " endDate = " + endDate

                dates << date
                if (params.frequency in ['Daily']) {
                    use(TimeCategory) {
                        date = date.plus(1.day)
                    }
                } else if (params.frequency in ['Weekly']) {
                    use(TimeCategory) {
                        date = date.plus(1.week)
                    }
                } else if (params.frequency in ['Monthly']) {
                    use(TimeCategory) {
                        date = date.plus(1.month)
                    }
                } else if (params.frequency in ['Quarterly']) {
                    use(TimeCategory) {
                        date = date.plus(3.month)
                    }
                } else if (params.frequency in ['Annually']) {
                    use(TimeCategory) {
                        date = date.plus(1.year)
                    }
                } else {
                    use(TimeCategory) {
                        date = date.plus(1.day)
                    }

                }
                count++
            }
        }
        return dates
    }

    private def getCsvForInventoryMap(List<Map> data, Boolean includeBinLocation = false) {
        def csv = ""
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.status.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.productCode.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryItem.lotNumber.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryItem.expirationDate.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.productFamily.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'category.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.tags.label', default: 'Tags')}" + '"' + ","
        if (includeBinLocation) {
            csv += '"' + "${warehouse.message(code: 'inventoryLevel.binLocation.label')}" + '"' + ","
        }
        csv += '"' + "${warehouse.message(code: 'product.unitOfMeasure.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.minQuantity.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.reorderQuantity.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.maxQuantity.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.forecastQuantity.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.currentQuantity.label', default: 'Current quantity')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.pricePerUnit.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.totalValue.label')}" + '"'
        csv += "\n"

        def hasRoleFinance = userService.hasRoleFinance(session.user)

        data.each { it ->

            Product product = it.inventoryItem?.product
            InventoryLevel inventoryLevel = product?.getInventoryLevel(session.warehouse.id)
            BigDecimal quantity = it.quantity ?: 0
            BigDecimal totalValue = (product?.pricePerUnit ?: 0) * (quantity)
            String status = inventoryLevel?.statusMessage(quantity as Long)
            if (!status) {
                status = quantity > 0 ? "IN_STOCK" : "STOCK_OUT"
            }
            String statusMessage = "${warehouse.message(code: 'enum.InventoryLevelStatusCsv.' + status, default: status)}"

            csv += '"' + (statusMessage ?: "") + '"' + ","
            csv += '"' + (product.productCode ?: "") + '"' + ","
            csv += StringEscapeUtils.escapeCsv(product?.displayNameWithLocaleCode ?: "") + ","
            csv += StringEscapeUtils.escapeCsv(it.inventoryItem?.lotNumber ?: "") + ","
            csv += '"' + formatDate(date: it.inventoryItem?.expirationDate, format: 'dd/MM/yyyy') + '"' + ","
            csv += '"' + (product?.productFamily?.name ?: "") + '"' + ','
            csv += StringEscapeUtils.escapeCsv(product?.category?.name ?: "") + ","
            csv += '"' + (product?.tagsToString() ?: "") + '"' + ","
            if (includeBinLocation) {
                csv += '"' + (it.binLocation ?: "") + '"' + ","
            }
            csv += '"' + (product?.unitOfMeasure ?: "") + '"' + ","
            csv += (inventoryLevel?.minQuantity ?: "") + ","
            csv += (inventoryLevel?.reorderQuantity ?: "") + ","
            csv += (inventoryLevel?.maxQuantity ?: "") + ","
            csv += (inventoryLevel?.forecastQuantity ?: "") + ","
            csv += '' + quantity + '' + ","
            csv += (hasRoleFinance ? (product?.pricePerUnit ?: "") : "") + ","
            csv += (hasRoleFinance ? (totalValue ?: "") : "")
            csv += "\n"
        }
        return CSVUtils.prependBomToCsvString(csv)
    }

    private def getCsvForProductMap(inventoryItems) {
        def hasRoleFinance = userService.hasRoleFinance(session.user)

        def csv = ""
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.status.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.productCode.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.productFamily.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'category.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.tags.label', default: 'Tags')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.abcClass.label', default: 'ABC Class')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.unitOfMeasure.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.minQuantity.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.reorderQuantity.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.maxQuantity.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.forecastQuantity.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.currentQuantity.label', default: 'Current quantity')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'default.quantityAvailableToPromise.label', default: 'Quantity ATP')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.pricePerUnit.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.totalValue.label')}" + '"'
        csv += "\n"

        inventoryItems.each { inventoryItem ->
            Product product = inventoryItem.product
            def quantity = inventoryItem.quantity
            InventoryLevel inventoryLevel = inventoryItem.inventoryLevel
            def status = inventoryItem.status
            def totalValue = (product?.pricePerUnit ?: 0) * (quantity ?: 0)
            def statusMessage = "${warehouse.message(code: 'enum.InventoryLevelStatusCsv.' + status)}"

            csv += '"' + (statusMessage ?: "") + '"' + ","
            csv += '"' + (product.productCode ?: "") + '"' + ","
            csv += StringEscapeUtils.escapeCsv(product.displayNameWithLocaleCode) + ","
            csv += '"' + (product?.productFamily?.name ?: "") + '"' + ","
            csv += '"' + (product?.category?.getHierarchyAsString(" > ") ?: "") + '"' + ","
            csv += '"' + (product?.tagsToString() ?: "") + '"' + ","
            csv += '"' + (inventoryLevel?.abcClass ?: "") + '"' + ","
            csv += '"' + (product?.unitOfMeasure ?: "") + '"' + ","
            csv += (inventoryLevel?.minQuantity ?: "") + ","
            csv += (inventoryLevel?.reorderQuantity ?: "") + ","
            csv += (inventoryLevel?.maxQuantity ?: "") + ","
            csv += (inventoryLevel?.forecastQuantity ?: "") + ","
            csv += (quantity ?: "0") + ","
            csv += (inventoryItem.quantityAvailableToPromise ?: "0") + ","
            csv += (hasRoleFinance ? (product?.pricePerUnit ?: "") : "") + ","
            csv += (hasRoleFinance ? (totalValue ?: "") : "")
            csv += "\n"
        }
        return CSVUtils.prependBomToCsvString(csv)
    }

}


class QuantityOnHandReportCommand implements Validateable {
    List<Location> locations = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(Location.class))
    List dates = []
    List products = []
    List tags = []
    Tag tag
    Date startDate = new Date()
    Date endDate
    String frequency


    static constraints = {
        locations(nullable: false,
                validator: { value, obj -> value?.size() >= 1 })
        startDate(nullable: false,
                validator: { value, obj -> !obj.endDate || value.before(obj.endDate) })
        endDate(nullable: false)
        frequency(nullable: false, blank: false)
        tag(nullable: true)
    }
}

class ManageInventoryCommand extends DefaultNullableCommand {

    List<ManageInventoryEntryCommand> entries = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(ManageInventoryEntryCommand.class))
    List inventoryItems = []
    List binLocations = []
    String productCodes
    List tags = []
}

class ManageInventoryEntryCommand extends DefaultNullableCommand {
    InventoryItem inventoryItem
    Integer quantity

}
