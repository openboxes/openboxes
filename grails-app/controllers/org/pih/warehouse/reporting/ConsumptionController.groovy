/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 * */
package org.pih.warehouse.reporting

import grails.converters.JSON
import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.util.Holders
import grails.validation.Validateable
import groovy.time.TimeCategory
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.collections.FactoryUtils
import org.apache.commons.collections.list.LazyList
import org.grails.core.DefaultGrailsDomainClass
import org.grails.datastore.mapping.model.PersistentEntity
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.UserService
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionCode
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.order.OrderTypeCode
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductService
import org.pih.warehouse.report.ConsumptionService
import org.pih.warehouse.report.ReportService
import org.pih.warehouse.requisition.Requisition

@Transactional
class ConsumptionController {

    def dataService
    ReportService reportService
    ProductService productService
    InventoryService inventoryService
    ConsumptionService consumptionService
    UserService userService
    def productAvailabilityService

    def show(ShowConsumptionCommand command) {

        if (command.hasErrors()) {
            render(view: "show", model: [command: command])
            return
        }

        String[] defaultTransactionTypeIds = [
                Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID,
                Constants.CONSUMPTION_TRANSACTION_TYPE_ID
        ]

        command.defaultTransactionTypes = defaultTransactionTypeIds.collect {TransactionType.get(it)}
        command.selectedTransactionTypes = command.defaultTransactionTypes
        command.transactionTypes = command.defaultTransactionTypes

        // If any parameters have changed we need to reset filters
        if (command.hasParameterChanged()) {
            command.selectedProperties = []
            command.selectedTags = []
            command.selectedLocations = []
            command.selectedCategories = []

            if (params.format == "csv") {
                params.remove("format")
                flash.message = "Unable to download CSV as parameters have changed. Please try download again."
            }
        }


        // Hack to fix PIMS-2728
        if (command.selectedProperties) {
            if (command.selectedProperties instanceof java.lang.String) {
                command.selectedProperties = [command.selectedProperties]
            }
        }

        def tags = command.selectedTags.collect { it.tag }.asList()
        def products = tags ? inventoryService.getProductsByTags(tags) : null

        // Add an entire day to account for the 24 hour period on the end date
        Date toDate = command.toDate ? command.toDate + 1 : null

        // Set to midnight
        if (toDate) {
            toDate.clearTime()
        }

        // Get all transactions
        command.debits = inventoryService.getDebitsBetweenDates(command.fromLocations,
                command.selectedLocations, command.fromDate, toDate,
                command.selectedTransactionTypes)
        // Get credits for INBOUND RETURNS, selectedLocations = sources, fromLocation = destination inventory
        command.credits = inventoryService.getCreditsBetweenDates(command.selectedLocations, command.fromLocations, command.fromDate, toDate)

        def transactions = []
        transactions.addAll(command.debits)
        transactions.addAll(command.credits?.findAll { it.incomingShipment?.isFromReturnOrder })

        // Sort transaction by date ascending
        transactions = transactions.sort { it.transactionDate }

        // Used within the transaction block to see if we need to add all destinations to command.toLocations
        // which occurs if there are no toLocations selected
        boolean toLocationsEmpty = command.toLocations.empty
        boolean fromLocationsEmpty = command.fromLocations.empty

        // Some transactions don't have a destination (e.g. expired, consumed, etc)
        if (toLocationsEmpty) {
            def debitLocations = transactions.findAll { it.destination != null }.collect {
                it.destination
            }
            def creditLocations = transactions.findAll { it.source != null && it.incomingShipment?.isFromReturnOrder }.collect {
                it.source
            }
            command.toLocations.addAll(debitLocations)
            command.toLocations.addAll(creditLocations)
        }

        // Keep track of all the transaction types (we may want to select a subset of these)
        // FIXME Hard-code transaction types (OBPIH-2059)
        command.transactionTypes = transactions*.transactionType.unique()

        def userHasFinanceRole = userService.hasRoleFinance(session?.user)

        // Iterate over all transactions
        transactions.each { Transaction transaction ->

            // Iterate over all transaction entries
            transaction.transactionEntries.each { TransactionEntry transactionEntry ->
                def product = transactionEntry.inventoryItem.product
                def currentRow = command.rows[product]
                if (!currentRow) {
                    command.rows[product] = new ShowConsumptionRowCommand()
                    command.rows[product].command = command
                    command.rows[product].product = product
                    command.rows[product].pricePerUnit = userHasFinanceRole ? product?.pricePerUnit : 0
                }

                // Keep track of quantity out based on transaction type
                if (transaction.transactionType.id == Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID) {
                    command.rows[product].transferOutQuantity += transactionEntry.quantity
                    command.rows[product].transferOutTransactions << transaction

                    // Initialize transfer out by location map
                    if(transaction.destination && transaction.destination != transaction.source) {
                        def transferOutQuantity = command.rows[product].transferOutMap[transaction.destination]

                        if (!transferOutQuantity) {
                            command.rows[product].transferOutMap[transaction.destination] = 0
                        }

                        command.rows[product].transferOutMap[transaction.destination] += transactionEntry.quantity
                    }

                    def isFromPutawayOrder = transaction?.outgoingShipment?.isFromPutawayOrder
                    def isFromTransferOrder = transaction?.outgoingShipment?.isFromTransferOrder
                    def isInternalTransfer = isFromPutawayOrder || isFromTransferOrder
                    def isFromReturnOrder = transaction?.outgoingShipment?.isFromReturnOrder

                    if (isFromReturnOrder || !isInternalTransfer) {
                        command.rows[product].issuedQuantity += transactionEntry.quantity
                    }
                } else if (transaction.transactionType.id == Constants.EXPIRATION_TRANSACTION_TYPE_ID) {
                    command.rows[product].expiredQuantity += transactionEntry.quantity
                    command.rows[product].expiredTransactions << transaction
                } else if (transaction.transactionType.id == Constants.DAMAGE_TRANSACTION_TYPE_ID) {
                    command.rows[product].damagedQuantity += transactionEntry.quantity
                    command.rows[product].damagedTransactions << transaction
                } else if (transaction.transactionType.id == Constants.TRANSFER_IN_TRANSACTION_TYPE_ID) {
                    command.rows[product].transferInQuantity += transactionEntry.quantity
                    command.rows[product].transferInTransactions << transaction

                    // Initialize transfer out by location map
                    def transferInQuantity = command.rows[product].transferInMap[transaction.source]
                    if (!transferInQuantity) {
                        command.rows[product].transferInMap[transaction.source] = 0
                    }

                    if(transaction?.incomingShipment?.isFromReturnOrder) {
                        command.rows[product].returnedQuantity += transactionEntry.quantity
                    }

                    // Add to the total transfer out per location
                    command.rows[product].transferInMap[transaction.source] += transactionEntry.quantity

                } else if (transaction.transactionType.id == Constants.CONSUMPTION_TRANSACTION_TYPE_ID) {
                    command.rows[product].consumedQuantity += transactionEntry.quantity
                }

                command.rows[product].totalConsumptionQuantity = command.rows[product].issuedQuantity + command.rows[product].consumedQuantity - command.rows[product].returnedQuantity

                String dateKey = transaction.transactionDate.format("yyyy-MM")
                command.selectedDates.add(dateKey)

                // Capture month breakdown for all debits and credits
                if (transaction.transactionType.transactionCode == TransactionCode.DEBIT) {
                    // Add to total transfer out by month (initialize transfer out by month map)
                    def transferOutMonthlyQuantity = command.rows[product].transferOutMonthlyMap[dateKey]
                    if (!transferOutMonthlyQuantity) {
                        command.rows[product].transferOutMonthlyMap[dateKey] = 0
                    }

                    if (transaction.transactionType.id == Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID) {
                        if (transaction?.order?.orderType?.code != Constants.PUTAWAY_ORDER && transaction?.order?.orderType?.code != OrderTypeCode.TRANSFER_ORDER.name()) {
                            command.rows[product].transferOutMonthlyMap[dateKey] += transactionEntry.quantity
                        }
                    } else if (transaction.transactionType.id == Constants.CONSUMPTION_TRANSACTION_TYPE_ID) {
                        command.rows[product].transferOutMonthlyMap[dateKey] += transactionEntry.quantity
                    }

                } else if (transaction.transactionType.transactionCode == TransactionCode.CREDIT) {
                    // Add to total transfer in by month (initialize transfer out by month map)
                    def transferInMonthlyQuantity = command.rows[product].transferInMonthlyMap[dateKey]
                    if (!transferInMonthlyQuantity) {
                        command.rows[product].transferInMonthlyMap[dateKey] = 0
                    }

                    if (transaction.transactionType.id == Constants.TRANSFER_IN_TRANSACTION_TYPE_ID
                            && transaction?.order?.orderType?.code == Constants.RETURN_ORDER) {
                        command.rows[product].transferInMonthlyMap[dateKey] -= transactionEntry.quantity

                    }
                }

                // All transactions
                command.rows[product].transactions << transaction
            }
        }

        // Calculate the on hand quantity for all products returned by the getTransactions() call above
        if (command.fromLocations) {
            products = command.rows.keySet().asList()

            // Filter products by tags
            if (command.selectedTags) {
                def productsToRemove = products.findAll { product ->
                    !command.selectedTags.intersect(product.tags)
                }

                def iterator = command.rows.keySet().iterator()
                while (iterator.hasNext()) {
                    if (productsToRemove.contains(iterator.next())) {
                        iterator.remove()
                    }
                }
            }

            // Filter products by categories
            if (command.selectedCategories) {
                def productsToRemove = products.findAll { product ->
                    !command.selectedCategories.contains(product.category)
                }

                def iterator = command.rows.keySet().iterator()
                while (iterator.hasNext()) {
                    if (productsToRemove.contains(iterator.next())) {
                        iterator.remove()
                    }
                }
            }
            products = command.rows.keySet().asList()

            // Calculate quantity on hand for filtered products
            if (!fromLocationsEmpty && command.includeQuantityOnHand) {
                command.fromLocations.each { location ->
                    if (location.inventory) {
                        def onHandQuantityMap = productAvailabilityService.getCurrentInventory(location)

                        // For each product, add to the onhand quantity map
                        products.each { product ->
                            def onHandQuantity = onHandQuantityMap[product]
                            if (onHandQuantity) {
                                command.rows[product].onHandQuantity += onHandQuantity
                            }
                        }
                    }
                }
            }
        }

        // We want to sort the transaction types and toLocations
        command?.transactionTypes?.unique()?.sort()
        command?.toLocations?.unique()?.sort()

        // If there are no selected locations, we select all of the possible destinations
        if (!command?.selectedLocations) {
            command.selectedLocations = command.toLocations
        }

        if (!command?.selectedTransactionTypes) {
            command.selectedTransactionTypes = command.transactionTypes
        }

        // Export as CSV
        if (params.format == "csv") {

            def csvrows = []
            command.rows.each { key, ShowConsumptionRowCommand row ->
                def valueConsumed = (row?.totalConsumptionQuantity ?: 0) * (row.pricePerUnit ?: 0)

                def csvrow = [
                        'Product code'                                : row.product.productCode ?: '',
                        'Product'                                     : row.product.displayNameWithLocaleCode,
                        'Product family'                              : row.product?.productFamily?.name ?: '',
                        'Category'                                    : row.product?.category?.name,
                        'Formulary'                                   : row.product?.productCatalogsToString(),
                        'Tag'                                         : row.product?.tagsToString(),
                        'Unit Price'                                  : g.formatNumber(number: row.pricePerUnit, format: '###.#', maxFractionDigits: 2) ?: '',
                        'UoM'                                         : row.product.unitOfMeasure ?: '',
                        'Qty Issued'                                  : g.formatNumber(number: row.issuedQuantity, format: '###.#', maxFractionDigits: 1) ?: '',
                        'Qty Consumed'                                : g.formatNumber(number: row.consumedQuantity, format: '###.#', maxFractionDigits: 1) ?: '',
                        'Qty Returned'                                : g.formatNumber(number: row.returnedQuantity, format: '###.#', maxFractionDigits: 1) ?: '',
                        'Total Consumption (Issued+Consumed-Returned)': g.formatNumber(number: row.totalConsumptionQuantity, format: '###.#', maxFractionDigits: 1) ?: '',
                        'Value Consumed'                              : g.formatNumber(number: valueConsumed, format: '###.#', maxFractionDigits: 1),
                        'Average Monthly Consumption'                 : g.formatNumber(number: row.monthlyQuantity, format: '###.#', maxFractionDigits: 4) ?: '',
                        'Quantity on hand'                            : g.formatNumber(number: row.onHandQuantity, format: '###.#', maxFractionDigits: 1) ?: '',
                        'Months remaining'                            : g.formatNumber(number: row.numberOfMonthsRemaining, format: '###.#', maxFractionDigits: 0) ?: '',
                ]

                if (command.selectedProperties) {
                    command.selectedProperties.each { property ->
                        csvrow[property] = row.product."${property}"
                    }
                }

                if (command.includeMonthlyBreakdown) {
                    command.selectedDates.each { date ->
                        csvrow[date.toString()] = row.transferOutMonthlyMap[date] ?: ""
                    }
                }

                if (command.includeLocationBreakdown) {
                    command.selectedLocations.each { location ->
                        csvrow[location.locationNumber ?: location?.name] = row.transferOutMap[location] ?: ""
                    }
                }

                csvrows << csvrow

            }

            csvrows.sort { it["Product code"] }

            def csv = dataService.generateCsv(csvrows)
            response.setHeader("Content-disposition", "attachment; filename=\"Consumption-" +
                    "${!fromLocationsEmpty && command.fromLocations.size() > 1 ? command.fromLocations : command.fromLocations.first()}" +
                    "-${new Date().format("dd MMM yyyy hhmmss")}.csv\"")
            render(contentType: "text/csv", text: csv.toString(), encoding: "UTF-8")
            return
        } else {
            println "Render as HTML " + params

            [command: command]
        }
    }


    def index() {
        redirect(action: "list")
    }


    def delete() {
        long startTime = System.currentTimeMillis()
        Integer deletedRecords = consumptionService.deleteConsumptionRecords()
        flash.message = "Deleted ${deletedRecords} consumption records in ${System.currentTimeMillis() - startTime}"
        log.info "Deleted ${deletedRecords} consumption records in ${System.currentTimeMillis() - startTime}"
        redirect(controller: "consumption", action: "list")
    }

    def refresh(ConsumptionCommand command) {
        reportService.buildConsumptionFact()
        redirect(controller: "consumption", action: "list")
    }


    def pivot(ConsumptionCommand command) {

        use(TimeCategory) {
            command.endDate = command?.endDate ?: new Date()
            command.startDate = command?.startDate ?: new Date() - 6.months
        }

        [command: command]
    }

    def list(ConsumptionCommand command) {

        log.info "Params: " + params

        Location location = Location.get(session?.warehouse?.id)

        use(TimeCategory) {
            command.endDate = command?.endDate ?: new Date()
            command.startDate = command?.startDate ?: new Date() - 6.months
        }

        if (command.download) {
            def data = consumptionService.listConsumption(command.location, command.category, command.startDate, command.endDate)
            def crosstab = consumptionService.generateCrossTab(data, command.startDate, command.endDate, null)
            log.info "crosstab " + crosstab
            String csv = dataService.generateCsv(crosstab)
            response.setHeader("Content-disposition", "attachment; filename=Consumption-${location.name}-${new Date().format("dd-MMM-yyyy-hhmmss")}.csv")
            render(contentType: "text/csv", text: csv.toString(), encoding: "UTF-8")
            return
        }

        [command: command]
    }

    def aggregate(ConsumptionCommand command) {

        String locationId = command?.location?.id ?: session?.warehouse?.id
        Location location = Location.get(locationId)

        use(TimeCategory) {
            command.endDate = command?.endDate ?: new Date()
            command.startDate = command?.startDate ?: new Date() - 6.months
        }


        List<ConsumptionFact> results = consumptionService.listConsumption(location, command?.category, command.startDate, command.endDate)

        results = results.collect {
            [
                    id          : it.id,
                    productCode : it?.productKey?.productCode,
                    productName : it.productKey?.productName,
                    categoryName: it?.productKey?.categoryName,
                    year        : it?.transactionDateKey?.year,
                    month       : it?.transactionDateKey?.month,
                    day         : it?.transactionDateKey?.dayOfMonth,
                    quantity    : it?.quantity,
                    unitCost    : it?.unitCost,
                    unitPrice   : it?.unitPrice
            ]
        }
        render results as JSON
    }

    def product() {
        Product product = Product.get(params.id)
        render(template: "product", model: [product: product])
    }

}


class ShowConsumptionCommand implements Validateable {

    // Map of product to ShowConsumptionRowCommand
    def rows = new TreeMap()

    // Parameters
    Date fromDate
    Date toDate
    List<Location> fromLocations = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(Location.class))

    // State
    String parametersHash

    // Filters
    List<Tag> tags = []
    List<Category> categories = []
    List<Product> products = []
    List<Location> toLocations = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(Location.class))
    List<TransactionType> transactionTypes = []
    List<TransactionType> selectedTransactionTypes = []
    List<TransactionType> defaultTransactionTypes = []
    List<String> selectedDates = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(String.class))
    List<Location> selectedLocations = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(Location.class))
    List<Category> selectedCategories = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(Category.class))
    List<Tag> selectedTags = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(Tag.class))

    Boolean includeLocationBreakdown = Boolean.TRUE
    Boolean includeMonthlyBreakdown = Boolean.TRUE
    Boolean includeQuantityOnHand = Boolean.TRUE

    def selectedProperties = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(String.class))

    // Payload
    Set<Transaction> debits = []
    Set<Transaction> credits = []
    Set<Requisition> requisitions = []
    Set<TransactionEntry> transactionEntries = []
    def productMap = new TreeMap()
    def onHandQuantityMap = new TreeMap()
    def transferOutMap = [:]

    static transients = ["numberOfDays", "numberOfWeeks", "numberOfMonths"]

    static constraints = {
        fromLocations(nullable: false)
        toLocations(nullable: true)
        fromDate(nullable: true)
        toDate(nullable: true)
        parametersHash(nullable: true)
    }

    def getAvailableProperties() {
        Holders.grailsApplication.mappingContext.getPersistentEntity(Product.class.name).persistentPropertyNames
    }

    Boolean hasParameterChanged() {
        String newParametersHash = generateParametersHash()
        return !parametersHash.equals(newParametersHash)
    }

    String generateParametersHash() {
        String parameters = "${fromDate}:${toDate}:${fromLocations}"
        return DigestUtils.md5Hex(parameters.bytes)
    }

    Integer getNumberOfDays() {
        if (toDate && fromDate) {
            return (toDate - fromDate)
        }
        return 0
    }

    Float getNumberOfWeeks() {
        return numberOfDays / 7
    }

    Float getNumberOfMonths() {
        return numberOfDays / 30
    }
}

class ShowConsumptionRowCommand implements Validateable {

    Product product
    ShowConsumptionCommand command
    InventoryLevel inventoryLevel

    Double pricePerUnit = 0

    Integer onHandQuantity = 0
    Integer transferInQuantity = 0
    Integer transferOutQuantity = 0
    Integer expiredQuantity = 0
    Integer damagedQuantity = 0
    Integer otherQuantity = 0
    Integer debitQuantity = 0

    Integer issuedQuantity = 0
    Integer consumedQuantity = 0
    Integer returnedQuantity = 0
    Integer totalConsumptionQuantity = 0

    Set<Transaction> transferOutTransactions = []
    Set<Transaction> expiredTransactions = []
    Set<Transaction> damagedTransactions = []
    Set<Transaction> transactions = []
    Set<Transaction> transferInTransactions = []
    Set<Transaction> otherTransactions = []

    // Location breakdown
    Map<Location, Integer> transferInMap = new TreeMap<Location, Integer>()
    Map<Location, Integer> transferOutMap = new TreeMap<Location, Integer>()

    // Monthly breakdown
    Map<String, Integer> transferInMonthlyMap = new TreeMap<String, Integer>()
    Map<String, Integer> transferOutMonthlyMap = new TreeMap<String, Integer>()

    static constraints = {

    }

    Integer getTransferBalance() {
        transferOutQuantity + expiredQuantity + damagedQuantity + otherQuantity
    }

    Float getMonthlyQuantity() {
        totalConsumptionQuantity / command.numberOfDays * 30
    }

    Float getWeeklyQuantity() {
        transferBalance / command.numberOfWeeks
    }

    Float getDailyQuantity() {
        transferBalance / command.numberOfDays
    }

    Float getNumberOfMonthsRemaining() {
        if (getMonthlyQuantity() > 0) {
            return onHandQuantity / getMonthlyQuantity()
        } else {
            return 0.0
        }
    }

    String transferOutLocations(List<Location> locations) {
        String transferOutLocations = ""
        if (locations) {
            locations.each { location ->
                transferOutLocations += transferOutMap[location] ?: '0' + ","
            }
        }
        return transferOutLocations
    }
}

class ConsumptionCommand implements Validateable {

    Category category
    Location location
    String groupBy
    Date startDate
    Date endDate

    Boolean aggregate = Boolean.FALSE
    Boolean download = Boolean.FALSE

    static constraints = {
        category(nullable: true)
        location(nullable: true)
        startDate(nullable: true)
        endDate(nullable: true)
        groupBy(nullable: true)

    }
}
