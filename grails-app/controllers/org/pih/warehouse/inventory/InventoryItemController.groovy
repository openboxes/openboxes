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
import grails.validation.ValidationException
import org.pih.warehouse.api.StockMovementType
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.User
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductException
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionItemStatus
import org.pih.warehouse.shipping.Container
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentItemException
import util.ConfigHelper

import java.text.DateFormat
import java.text.SimpleDateFormat

class InventoryItemController {

    def dataService
    def inventoryService
    def shipmentService
    def requisitionService
    def orderService
    def forecastingService
    def grailsApplication


    def index = {
        redirect(controller: "inventory", action: "browse")
    }
    /**
     *
     */
    def show = {
        def itemInstance = InventoryItem.get(params.id)
        def transactionEntryList = TransactionEntry.findAllByInventoryItem(itemInstance)
        [
                itemInstance        : itemInstance,
                transactionEntryList: transactionEntryList
        ]
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
     * Displays the stock card for a product
     */
    def showStockCard = { StockCardCommand cmd ->

        try {
            // add the current warehouse to the command object which prevents location from being spoofed
            cmd.warehouse = Location.get(session?.warehouse?.id)

            // now populate the rest of the commmand object
            inventoryService.getStockCardCommand(cmd, params)

            [commandInstance: cmd]
        } catch (ProductException e) {
            flash.message = e.message
            redirect(controller: "dashboard", action: "index")
        }
    }

    def showCurrentStock = { StockCardCommand cmd ->
        def startTime = System.currentTimeMillis()
        cmd.warehouse = Location.get(session?.warehouse?.id)
        def commandInstance = inventoryService.getStockCardCommand(cmd, params)
        commandInstance.product = Product.get(params.id)
        log.info "${controllerName}.${actionName}: " + (System.currentTimeMillis() - startTime) + " ms"
        log.info "Product " + commandInstance.product
        render(template: "showCurrentStock", model: [commandInstance: commandInstance])
    }


    def showCurrentStockAllLocations = { StockCardCommand cmd ->
        def startTime = System.currentTimeMillis()
        User currentUser = User.get(session.user.id)
        Location currentLocation = Location.get(session?.warehouse?.id)
        cmd.warehouse = currentLocation
        def commandInstance = inventoryService.getStockCardCommand(cmd, params)
        def quantityMap = inventoryService.getCurrentStockAllLocations(commandInstance?.product, currentLocation, currentUser)
        def targetUri = "/inventoryItem/showStockCard/${commandInstance?.product?.id}"
        log.info "${controllerName}.${actionName}: " + (System.currentTimeMillis() - startTime) + " ms"

        render(template: "showCurrentStockAllLocations", model: [commandInstance: commandInstance, quantityMap: quantityMap, targetUri: targetUri])
    }

    def showAlternativeProducts = { StockCardCommand cmd ->
        def startTime = System.currentTimeMillis()
        def product = Product.get(params.id)
        def location = Location.get(session?.warehouse?.id)

        def products = product.alternativeProducts() as List
        log.info "Products " + products
        def quantityMap = [:]
        if (!products.isEmpty()) {
            quantityMap = inventoryService.getQuantityByProductMap(location, products)
        }
        def totalQuantity = quantityMap.values().sum() ?: 0

        log.info "${controllerName}.${actionName}: " + (System.currentTimeMillis() - startTime) + " ms"

        render(template: "showProductGroups", model: [product: product, totalQuantity: totalQuantity, quantityMap: quantityMap])
    }


    def showStockHistory = { StockCardCommand cmd ->
        def startTime = System.currentTimeMillis()
        // add the current warehouse to the command object
        cmd.warehouse = Location.get(session?.warehouse?.id)

        // now populate the rest of the commmand object
        def commandInstance = inventoryService.getStockCardCommand(cmd, params)

        def stockHistoryList = []


        int totalDebit = 0, totalCredit = 0, totalBalance = 0, totalCount = 0
        def balance = [:]
        def count = [:]
        def transactionMap = commandInstance?.getTransactionLogMap(false)
        def previousTransaction = null


        transactionMap.each { Transaction transaction, List transactionEntries ->

            // For PRODUCT INVENTORY transactions we just need to clear the balance completely and start over
            if (transaction?.transactionType?.transactionCode == org.pih.warehouse.inventory.TransactionCode.PRODUCT_INVENTORY) {
                balance = [:]
                count = [:]
                totalCredit = 0
                totalDebit = 0
            }

            transactionEntries.eachWithIndex { TransactionEntry transactionEntry, i ->

                boolean isBaseline = false
                String index = (transactionEntry.binLocation?.name ?: "DefaultBin") + "-" + (transactionEntry?.inventoryItem?.lotNumber ?: "DefaultLot")

                if (!balance[index]) {
                    balance[index] = 0
                    count[index] = 0
                }

                if (transaction?.transactionType?.transactionCode == org.pih.warehouse.inventory.TransactionCode.DEBIT) {
                    balance[index] -= transactionEntry?.quantity
                    totalDebit += transactionEntry?.quantity
                } else if (transaction?.transactionType?.transactionCode == org.pih.warehouse.inventory.TransactionCode.CREDIT) {
                    balance[index] += transactionEntry?.quantity
                    totalCredit += transactionEntry?.quantity
                } else if (transaction?.transactionType?.transactionCode == org.pih.warehouse.inventory.TransactionCode.INVENTORY) {
                    balance[index] = transactionEntry?.quantity
                    count[index] = transactionEntry?.quantity
                } else if (transaction?.transactionType?.transactionCode == org.pih.warehouse.inventory.TransactionCode.PRODUCT_INVENTORY) {
                    balance[index] += transactionEntry?.quantity
                    count[index] += transactionEntry?.quantity
                }

                if (transaction?.transactionType?.transactionCode == org.pih.warehouse.inventory.TransactionCode.PRODUCT_INVENTORY && i == 0) {
                    isBaseline = true
                }

                boolean isCredit = (transaction?.transactionType?.transactionCode == org.pih.warehouse.inventory.TransactionCode.CREDIT && transactionEntry?.quantity >= 0) ||
                        (transaction?.transactionType?.transactionCode == org.pih.warehouse.inventory.TransactionCode.DEBIT && transactionEntry.quantity < 0)

                boolean isDebit = (transaction?.transactionType?.transactionCode == org.pih.warehouse.inventory.TransactionCode.DEBIT && transactionEntry?.quantity > 0) ||
                        (transaction?.transactionType?.transactionCode == org.pih.warehouse.inventory.TransactionCode.CREDIT && transactionEntry.quantity < 0)

                // Normalize quantity (inventory transactions were all converted to CREDIT so some may have negative quantity)
                def quantity = (transactionEntry.quantity > 0) ? transactionEntry.quantity : -transactionEntry.quantity

                String transactionYear = (transaction.transactionDate.year + 1900).toString()
                String transactionMonth = (transaction.transactionDate.month).toString()

                stockHistoryList << [
                        transactionYear  : transactionYear,
                        transactionMonth : transactionMonth,
                        transactionDate  : transaction.transactionDate,
                        transactionCode  : transaction?.transactionType?.transactionCode,
                        transaction      : transaction,
                        shipment         : null,
                        requisition      : null,
                        binLocation      : transactionEntry.binLocation,
                        inventoryItem    : transactionEntry.inventoryItem,
                        comments         : transactionEntry.comments,
                        quantity         : quantity,
                        isDebit          : isDebit,
                        isCredit         : isCredit,
                        balance          : balance.values().sum(),
                        showDetails      : (i == 0),
                        isBaseline       : isBaseline,
                        isSameTransaction: (previousTransaction?.id == transaction?.id),
                ]
                previousTransaction = transaction
            }

            totalBalance = balance.values().sum()
            totalCount = count.values().sum()
        }


        log.info "${controllerName}.${actionName}: " + (System.currentTimeMillis() - startTime) + " ms"

        if (params.print) {
            render(template: "printStockHistory", model: [commandInstance: commandInstance, stockHistoryList: stockHistoryList,
                                                          totalBalance   : totalBalance, totalCount: totalCount, totalCredit: totalCredit, totalDebit: totalDebit])
        } else {
            stockHistoryList = stockHistoryList.groupBy({ it.transactionYear })
            def groupedStockHistoryList = [:]
            stockHistoryList.each { year, history ->
                history = history.groupBy { it.transactionMonth }
                groupedStockHistoryList.get(year, [:]) << history
            }
            render(template: "showStockHistory", model: [commandInstance: commandInstance, stockHistoryList: groupedStockHistoryList,
                                                          totalBalance   : totalBalance, totalCount: totalCount, totalCredit: totalCredit, totalDebit: totalDebit])
        }
    }

    def showSuppliers = {

        def productInstance = Product.get(params.id)


        render(template: "showSuppliers", model: [productInstance: productInstance])
    }


    def showPending = {

        Product product = Product.get(params.id)
        Location location = Location.get(session?.warehouse?.id)
        StockMovementType stockMovementType = params.type as StockMovementType
        def itemsMap, requisitionItems, shipmentItems, orderItems = []

        if (!stockMovementType) {
            throw new IllegalArgumentException("Stock movement type is required")
        }

        Location origin = stockMovementType == StockMovementType.INBOUND ? null : location
        Location destination = stockMovementType == StockMovementType.OUTBOUND ? null : location

        if (origin) {
            requisitionItems = requisitionService.getPendingRequisitionItems(origin, product)
            itemsMap = requisitionItems.groupBy { it.requisition }
        } else if (destination) {
            shipmentItems = shipmentService.getPendingInboundShipmentItems(destination, product)
            itemsMap = shipmentItems.sort { it.shipment.currentStatus }.groupBy { it.shipment }
            orderItems = orderService.getPendingInboundOrderItems(destination, product)
            itemsMap += orderItems.groupBy { it.order }
        }

        log.info "itemsMap: " + itemsMap
        if (itemsMap) {
            itemsMap.keySet().each {
                def quantityRequested = it instanceof Requisition ? itemsMap[it].sum() { RequisitionItem requisitionItem -> requisitionItem.quantity } : 0
                def quantityRequired = it instanceof Requisition ? itemsMap[it].sum() { RequisitionItem requisitionItem -> requisitionItem.calculateQuantityRequired() } : 0
                def quantityPicked =  it instanceof Requisition ? itemsMap[it].sum() { RequisitionItem requisitionItem -> requisitionItem.calculateQuantityPicked() } : 0
                def quantityRemaining = it instanceof Shipment ? itemsMap[it].sum() { ShipmentItem shipmentItem -> shipmentItem.quantityRemaining } : 0
                def quantityPurchased = it instanceof Order ? itemsMap[it].sum() { OrderItem orderItem -> orderItem.quantityRemaining } : 0
                def type = it instanceof Order ? "Purchase Order" : "Stock Movement"
                def quantityMap = [
                        quantityRequested: quantityRequested,
                        quantityRequired : quantityRequired,
                        quantityPicked   : quantityPicked,
                        quantityRemaining: quantityRemaining,
                        quantityPurchased: quantityPurchased,
                        type             : type
                ]
                itemsMap.put(it, quantityMap)
            }
        }

        render(template: "showPendingStock", model: [product: product, itemsMap: itemsMap])
    }

    def showConsumption = { StockCardCommand cmd ->

        // add the current warehouse to the command object
        cmd.warehouse = Location.get(session?.warehouse?.id)

        def reasonCodes = params.list("reasonCode")
        if (reasonCodes.empty) {
            reasonCodes = ConfigHelper.listValue(grailsApplication.config.openboxes.stockCard.consumption.reasonCodes)
            reasonCodes = reasonCodes.collect { it.toString() }
        }

        // now populate the rest of the commmand object
        def commandInstance = inventoryService.getStockCardCommand(cmd, params)
        def requisitionItems = requisitionService.getIssuedRequisitionItems(commandInstance?.warehouse, commandInstance?.product, cmd.startDate, cmd.endDate, reasonCodes)

        // Calculate the number of days between first and last requisition
        Date firstDateRequested = requisitionItems.collect { it.requisition.dateRequested }.min()
        Date lastDateRequested = requisitionItems.collect { it.requisition.dateRequested }.max()

        // Get quantity issued by request
        def transactionEntries = requisitionService.getIssuedTransactionEntries(commandInstance?.warehouse, commandInstance?.product, cmd.startDate, cmd.endDate)
        transactionEntries = transactionEntries.collect { TransactionEntry transactionEntry ->
            // Retrieved through shipment since the transaction cannot be trusted to have a reliable link yet (see OBPIH-2447)
            Requisition requisition = transactionEntry.transaction?.outgoingShipment?.requisition
            [
                    requestNumber : requisition?.requestNumber,
                    dateRequested : requisition?.dateRequested,
                    dateIssued    : transactionEntry?.transaction?.transactionDate,
                    quantityIssued: transactionEntry?.quantity,
            ]
        }

        DateFormat monthFormat = new SimpleDateFormat("MMM yyyy")
        monthFormat.timeZone = TimeZone.default

        requisitionItems = requisitionItems.collect {
            def requestNumber = it?.requisition?.requestNumber

            def transactionEntriesByRequest = transactionEntries.findAll { te -> te.requestNumber == requestNumber }
            def quantityIssued = transactionEntriesByRequest.collect { it.quantityIssued }.sum()
            def dateIssued = transactionEntriesByRequest.collect { it.dateIssued }.min()

            def quantityApproved = it?.quantityApproved ?: 0
            def quantityPicked = it?.calculateQuantityPicked() ?: 0
            if (it.status in [RequisitionItemStatus.CHANGED, RequisitionItemStatus.SUBSTITUTED]) {
                quantityApproved = it?.requisitionItems?.collect { it.quantityApproved }.sum()
                quantityPicked = it?.requisitionItems?.collect {
                    it.calculateQuantityPicked()
                }.sum()
            }
            [
                    status           : it?.status,
                    productCode      : it?.product?.productCode,
                    productName      : it?.product?.name,
                    origin           : it?.requisition?.origin?.name,
                    requisitionId    : it?.requisition?.id,
                    requestNumber    : it?.requisition?.requestNumber,
                    requestStatus    : it?.requisition?.status,
                    destination      : it?.requisition?.destination?.name,
                    lotNumber        : it?.inventoryItem?.lotNumber,
                    expirationDate   : it?.inventoryItem?.expirationDate,
                    dateIssued       : dateIssued,
                    monthIssued      : dateIssued ? monthFormat.format(dateIssued) : null,
                    dateRequested    : it?.requisition?.dateRequested,
                    monthRequested   : monthFormat.format(it?.requisition?.dateRequested),
                    quantityRequested: it?.quantity ?: 0,
                    quantityCanceled : it?.quantityCanceled ?: 0,
                    quantityApproved : quantityApproved,
                    quantityRequired : it?.calculateQuantityRequired() ?: 0,
                    quantityPicked   : quantityPicked,
                    quantityIssued   : quantityIssued,
                    reasonCode       : it?.cancelReasonCode,
                    comments         : it?.comment
            ]
        }

        // Create list of months to display including all months between first and last requested date
        def monthKeys = (firstDateRequested..lastDateRequested).collect {
            monthFormat.format(it)
        }.unique()

        // Remove the current month
        def currentMonthKey = monthFormat.format(new Date())
        monthKeys.remove(currentMonthKey)

        // By default, only display the last 6 months
        if (!cmd.startDate && !cmd.endDate) {
            monthKeys = monthKeys.subList(Math.max(monthKeys.size() - 6, 0), monthKeys.size());
        }
        render(template: "showConsumption",
                model: [commandInstance : commandInstance,
                        requisitionItems: requisitionItems,
                        reasonCodes     : reasonCodes,
                        numberOfMonths  : monthKeys?.size() ?: 1,
                        monthKeys       : monthKeys])
    }

    def showProductDemand = {
        Product product = Product.get(params.id)
        Location location = Location.get(session.warehouse.id)
        if (params.format == 'csv') {
            def data = forecastingService.getDemandDetails(location, product)
            def csv = dataService.generateCsv(data)
            def filename = "Product Demand ${product.productCode} ${location.name}.csv"
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
            render(contentType: "text/csv", text: csv)

            return
        }

        render(template: "showProductDemand", model: [product: product])
    }


    def showInventorySnapshot = {
        def location = Location.get(session.warehouse.id)
        def product = Product.get(params.id)
        def inventorySnapshots = InventorySnapshot.findAllByProductAndLocation(product, location)
        render(template: "showInventorySnapshot", model: [inventorySnapshots: inventorySnapshots, product: product])

    }


    /**
     * Displays the stock card for a product
     */
    def showLotNumbers = { StockCardCommand cmd ->
        // add the current warehouse to the command object
        cmd.warehouse = Location.get(session?.warehouse?.id)

        // now populate the rest of the commmand object
        def commandInstance = inventoryService.getStockCardCommand(cmd, params)


        [commandInstance: commandInstance]
    }

    /**
     * Displays the stock card for a product
     */
    def showTransactionLog = { StockCardCommand cmd ->
        // add the current warehouse to the command object
        cmd.warehouse = Location.get(session?.warehouse?.id)

        // now populate the rest of the commmand object
        def commandInstance = inventoryService.getStockCardCommand(cmd, params)

        [commandInstance: commandInstance]
    }


    /**
     * Displays the stock card for a product
     */
    def showGraph = { StockCardCommand cmd ->
        // add the current warehouse to the command object
        cmd.warehouse = Location.get(session?.warehouse?.id)

        // now populate the rest of the commmand object
        def commandInstance = inventoryService.getStockCardCommand(cmd, params)

        log.info("Inventory item list: " + commandInstance?.inventoryItemList)
        [commandInstance: commandInstance]
    }

    /**
     * Display the Record Inventory form for the product
     */
    def showRecordInventory = { RecordInventoryCommand commandInstance ->

        def locationInstance = Location.get(session?.warehouse?.id)

        // We need to set the inventory instance in order to save an 'inventory' transaction
        if (!commandInstance.inventory) {
            commandInstance.inventory = locationInstance?.inventory
        }
        inventoryService.populateRecordInventoryCommand(commandInstance, params)

        Product productInstance = commandInstance.product
        List transactionEntryList = inventoryService.getTransactionEntriesByInventoryAndProduct(commandInstance?.inventory, [productInstance])

        // Get the inventory warning level for the given product and inventory
        commandInstance.inventoryLevel = InventoryLevel.findByProductAndInventory(productInstance, commandInstance?.inventory)

        // Compute the total quantity for the given product
        commandInstance.totalQuantity = inventoryService.getQuantityByProductMap(transactionEntryList)[productInstance] ?: 0

        // FIXME Use this method instead of getQuantityByProductMap
        // NEED to add tests before we introduce this change
        //commandInstance.totalQuantity = inventoryService.getQuantityOnHand(locationInstance, productInstance)

        Map<Product, List<InventoryItem>> inventoryItems = inventoryService.getInventoryItemsWithQuantity([productInstance], commandInstance.inventory)
        def result = []
        inventoryItems.keySet().each { product ->
            result = inventoryItems[product].collect { ((InventoryItem) it).toJson() }
        }
        String jsonString = [product: productInstance.toJson(), inventoryItems: result] as JSON
        log.info "record inventory " + jsonString

        [commandInstance: commandInstance, product: jsonString]
    }

    def saveRecordInventory = { RecordInventoryCommand commandInstance ->
        log.info("Before saving record inventory " + params)
        inventoryService.saveRecordInventoryCommand(commandInstance, params)
        if (!commandInstance.hasErrors()) {
            redirect(action: "showStockCard", params: ['product.id': commandInstance.product.id])
            return
        }

        log.info("User chose to validate or there are errors")
        def warehouseInstance = Location.get(session?.warehouse?.id)

        commandInstance.inventory = warehouseInstance?.inventory
        commandInstance.inventoryLevel = InventoryLevel.findByProductAndInventory(commandInstance?.product, commandInstance?.inventory)

        Product productInstance = commandInstance.product
        List transactionEntryList = inventoryService.getTransactionEntriesByInventoryAndProduct(commandInstance?.inventory, [productInstance])

        // Get the inventory warning level for the given product and inventory
        commandInstance.inventoryLevel = InventoryLevel.findByProductAndInventory(commandInstance?.product, commandInstance?.inventory)

        commandInstance.totalQuantity = inventoryService.getQuantityByProductMap(transactionEntryList)[productInstance] ?: 0

        log.info "commandInstance.recordInventoryRows: "
        commandInstance?.recordInventoryRows.each {
            log.info "it ${it?.id}:${it?.lotNumber}:${it?.oldQuantity}:${it?.newQuantity}"
        }

        render(view: "showRecordInventory", model: [commandInstance: commandInstance])
    }

    def showTransactions = {

        def warehouseInstance = Location.get(session?.warehouse?.id)
        def productInstance = Product.get(params?.product?.id)
        def inventoryInstance = warehouseInstance.inventory
        def inventoryItemList = inventoryService.getInventoryItemsByProductAndInventory(productInstance, inventoryInstance)
        def transactionEntryList = TransactionEntry.findAllByProductAndInventory(productInstance, inventoryInstance)
        def inventoryLevelInstance = InventoryLevel.findByProductAndInventory(productInstance, inventoryInstance)

        [inventoryInstance     : inventoryInstance,
         inventoryLevelInstance: inventoryLevelInstance,
         productInstance       : productInstance,
         inventoryItemList     : inventoryItemList,
         transactionEntryList  : transactionEntryList,
         transactionEntryMap   : transactionEntryList.groupBy { it.transaction }]
    }


    def createInventoryItem = {

        flash.message = "${warehouse.message(code: 'inventoryItem.temporaryCreateInventoryItem.message')}"

        def productInstance = Product.get(params?.product?.id)
        def inventoryInstance = Inventory.get(params?.inventory?.id)
        def itemInstance = new InventoryItem(product: productInstance)
        def inventoryLevelInstance = inventoryService.getInventoryLevelByProductAndInventory(productInstance, inventoryInstance)
        def inventoryItems = inventoryService.getInventoryItemsByProduct(productInstance)
        [itemInstance: itemInstance, inventoryInstance: inventoryInstance, inventoryItems: inventoryItems, inventoryLevelInstance: inventoryLevelInstance, totalQuantity: totalQuantity]
    }

    def saveInventoryItem = {
        log.info "save inventory item " + params
        def productInstance = Product.get(params.product.id)
        def inventoryInstance = Inventory.get(params.inventory.id)
        def inventoryItem = new InventoryItem(params)
        def inventoryItems = inventoryService.getInventoryItemsByProduct(inventoryItem.product)
        inventoryInstance.properties = params

        def transactionInstance = new Transaction(params)
        def transactionEntry = new TransactionEntry(params)
        if (!transactionEntry.quantity) {
            transactionEntry.errors.rejectValue("quantity", 'transactionEntry.quantity.invalid')
        }

        if (transactionEntry.hasErrors()) {
            inventoryItem.errors = transactionEntry.errors
        }
        if (transactionInstance.hasErrors()) {
            inventoryItem.errors = transactionInstance.errors
        }


        // TODO Move all of this logic into the service layer in order to take advantage of Hibernate/Spring transactions
        if (!inventoryItem.hasErrors() && inventoryItem.save()) {
            // Need to create a transaction if we want the inventory item
            // to show up in the stock card
            transactionInstance.transactionDate = new Date()
            transactionInstance.transactionType = TransactionType.get(Constants.INVENTORY_TRANSACTION_TYPE_ID)
            def warehouseInstance = Location.get(session.warehouse.id)
            transactionInstance.source = warehouseInstance
            transactionInstance.inventory = warehouseInstance.inventory

            transactionEntry.inventoryItem = inventoryItem
            transactionInstance.addToTransactionEntries(transactionEntry)

            transactionInstance.save()
            flash.message = "${warehouse.message(code: 'inventoryItem.savedItemWithinNewTransaction.message', args: [inventoryItem.id, transactionInstance.id])}"

        } else {
            render(view: "createInventoryItem", model: [itemInstance: inventoryItem, inventoryInstance: inventoryInstance, inventoryItems: inventoryItems])
            return
        }


        // If all else fails, return to the show stock card page
        redirect(action: 'showStockCard', id: productInstance?.id)
    }


    def edit = {
        def itemInstance = InventoryItem.get(params.id)
        if (!itemInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventoryItem.label', default: 'Inventory item'), params.id])}"
            redirect(action: "show", id: itemInstance.id)
        } else {
            return [itemInstance: itemInstance]
        }
    }

    def editInventoryLevel = {

        def productInstance = Product.get(params?.product?.id)
        def inventoryInstance = Inventory.get(params?.inventory?.id)
        if (!inventoryInstance) {
            def warehouse = Location.get(session?.warehouse?.id)
            inventoryInstance = warehouse.inventory
        }

        def inventoryLevelInstance = InventoryLevel.findByProductAndInventory(productInstance, inventoryInstance)
        if (!inventoryLevelInstance) {
            inventoryLevelInstance = new InventoryLevel()
        }

        [productInstance: productInstance, inventoryInstance: inventoryInstance, inventoryLevelInstance: inventoryLevelInstance]
    }

    def updateInventoryLevel = {

        log.info("update inventory level " + params)

        def productInstance = Product.get(params?.product?.id)
        def inventoryInstance = Inventory.get(params?.inventory?.id)
        def inventoryLevelInstance = InventoryLevel.get(params.id)

        if (inventoryLevelInstance) {
            inventoryLevelInstance.properties = params
        } else {
            inventoryLevelInstance = new InventoryLevel(params)
        }

        if (!inventoryLevelInstance.hasErrors() && inventoryLevelInstance.save()) {
            log.info("save inventory level ")
            flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'inventoryLevel.label', default: 'Inventory level')])}"
        } else {
            log.info("render with errors")
            render(view: "updateInventoryLevel", model:
                    [productInstance: productInstance, inventoryInstance: inventoryInstance, inventoryLevelInstance: inventoryLevelInstance])
            return
        }

        redirect(controller: "inventoryItem", action: "showStockCard", id: productInstance?.id)
    }


    /**
     * Handles form submission from Show Stock Card > Adjust Stock dialog.
     */
    def adjustStock = { AdjustStockCommand command ->
        InventoryItem inventoryItem = command.inventoryItem
        try {
            inventoryService.adjustStock(command)
            if (!command.hasErrors()) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'inventoryItem.label', default: 'Inventory item'), inventoryItem.id])}"
            }
        } catch (ValidationException e) {
            command.errors = e.errors
        }

        if (params.redirectUri) {
            redirect(uri: params.redirectUri)
            return
        }


        chain(controller: "inventoryItem", action: "showStockCard",
                id: inventoryItem?.product?.id, params: ['inventoryItem.id': inventoryItem?.id], model: [command: command])
    }

    def showDialog = {
        def location = Location.get(session.warehouse.id)
        def inventoryItem = InventoryItem.get(params.id)
        def binLocation = Location.get(params.binLocation)
        def quantityAvailable = inventoryService.getQuantityFromBinLocation(location, binLocation, inventoryItem)

        render(template: params.template, model: [location         : location,
                                                  binLocation      : binLocation,
                                                  inventoryItem    : inventoryItem,
                                                  quantityAvailable: quantityAvailable])
    }

    def refreshBinLocation = {
        log.info "params: " + params
        render g.selectBinLocationByLocation(params)
    }


    def transferStock = { TransferStockCommand command ->
        log.info "Transfer stock " + params
        log.info "Command " + command

        InventoryItem inventoryItem = command.inventoryItem

        if (inventoryItem) {

            Transaction transaction
            try {
                transaction = inventoryService.transferStock(command)

                if (!transaction.hasErrors()) {
                    flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'inventoryItem.label', default: 'Inventory item'), inventoryItem.id])}"
                } else {
                    chain(controller: "inventoryItem", action: "showStockCard", id: inventoryItem?.product?.id, model: [transaction: transaction])
                    return
                }

            } catch (Exception e) {
                log.error("Error transferring stock " + e.message, e)
                flash.transaction = transaction
                chain(controller: "inventoryItem", action: "showStockCard", id: inventoryItem?.product?.id, model: [transaction: transaction, itemInstance: inventoryItem])
                return
            }
            log.info("transaction " + transaction + " " + transaction?.id)


        }
        redirect(controller: "inventoryItem", action: "showStockCard", id: inventoryItem?.product?.id, params: ['inventoryItem.id': inventoryItem?.id])
    }


    def update = {

        log.info "Params " + params
        def itemInstance = InventoryItem.get(params.id)
        def productInstance = Product.get(params?.product?.id)
        def date = grailsApplication.config.openboxes.expirationDate.minValue
        if (itemInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (itemInstance.version > version) {
                    itemInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'inventoryItem.label', default: 'Inventory Item')] as Object[], "Another user has updated this inventory item while you were editing")
                    //render(view: "show", model: [itemInstance: itemInstance])
                    redirect(controller: "inventoryItem", action: "showStockCard", id: productInstance?.id)
                    return
                }
            }
            itemInstance.properties = params

            // FIXME Temporary hack to handle a changed values for these two fields
            itemInstance.lotNumber = params?.lotNumber

            if (!itemInstance.hasErrors() && itemInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'inventoryItem.label', default: 'Inventory item'), itemInstance.id])}"
            } else {
                flash.error = "${warehouse.message(code: 'default.not.updated.message', args: [warehouse.message(code: 'inventoryItem.label', default: 'Inventory item'), itemInstance.id])}"
                log.info "There were errors trying to save inventory item " + itemInstance?.errors
                if (params.expirationDate < date) {
                    flash.error = "This date is invalid. Please enter a date after ${date.getYear()+1900}."
                    redirect(controller: "inventoryItem", action: "showLotNumbers", id: productInstance?.id)
                    return
                }
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventoryItem.label', default: 'Inventory item'), params.id])}"
        }
        redirect(controller: "inventoryItem", action: "showStockCard", id: productInstance?.id)
    }


    def deleteTransactionEntry = {
        def transactionEntry = TransactionEntry.get(params.id)
        def productInstance
        if (transactionEntry) {
            productInstance = transactionEntry.inventoryItem.product
            transactionEntry.delete()
        }
        redirect(action: 'showStockCard', params: ['product.id': productInstance?.id])
    }

    def addToInventory = {
        def product = Product.get(params.id)
        render warehouse.message(code: 'inventoryItem.productAddedToInventory.message', args: [product.name])
        //return product as XML
    }
    /**
     * Add a shipment item to a shipment
     */
    def addToShipment = {
        log.info "params" + params
        def shipmentInstance = null
        def containerInstance = null
        def productInstance = Product.get(params?.product?.id)
        def personInstance = Person.get(params?.recipient?.id)
        def binLocation = Location.get(params?.binLocation?.id)
        def inventoryItem = InventoryItem.get(params?.inventoryItem?.id)

        def shipmentContainer = params.shipmentContainer?.split(":")

        shipmentInstance = Shipment.get(shipmentContainer[0])
        containerInstance = Container.get(shipmentContainer[1])

        log.info("shipment " + shipmentInstance)
        log.info("container " + containerInstance)


        def shipmentItem = new ShipmentItem(
                product: productInstance,
                binLocation: binLocation,
                lotNumber: inventoryItem.lotNumber ?: '',
                expirationDate: inventoryItem?.expirationDate,
                inventoryItem: inventoryItem,
                quantity: params.quantity,
                recipient: personInstance,
                shipment: shipmentInstance,
                container: containerInstance)

        try {

            shipmentService.validateShipmentItem(shipmentItem)

            if (shipmentItem.hasErrors() || !shipmentItem.validate()) {
                flash.message = "${warehouse.message(code: 'inventoryItem.errorValidatingItem.message')}\n"
                shipmentItem.errors.each { flash.message += it }

            }

            if (!shipmentItem.hasErrors()) {
                if (!shipmentInstance.addToShipmentItems(shipmentItem).save()) {
                    log.error("Sorry, unable to add new item to shipment.  Please try again.")
                    flash.message = "${warehouse.message(code: 'inventoryItem.unableToAddItemToShipment.message')}"
                } else {
                    def productDescription = format.product(product: productInstance) + (inventoryItem?.lotNumber) ? " #" + inventoryItem?.lotNumber : ""
                    flash.message = "${warehouse.message(code: 'inventoryItem.addedItemToShipment.message', args: [productDescription, shipmentInstance?.name])}"
                }
            }

        } catch (ShipmentItemException e) {
            flash['errors'] = e.shipmentItem.errors
        } catch (ValidationException e) {
            flash['errors'] = e.errors
        }

        redirect(action: "showStockCard", params: ['product.id': productInstance?.id])
    }


    def saveInventoryLevel = {
        // Get existing inventory level
        def inventoryLevelInstance = InventoryLevel.get(params.id)
        def productInstance = Product.get(params?.product?.id)

        if (inventoryLevelInstance) {
            inventoryLevelInstance.properties = params
        } else {
            inventoryLevelInstance = new InventoryLevel(params)
        }

        if (!inventoryLevelInstance.hasErrors() && inventoryLevelInstance.save()) {

        } else {
            flash.message = "${warehouse.message(code: 'inventoryItem.errorSavingInventoryLevels.message')}<br/>"
            inventoryLevelInstance.errors.allErrors.each {
                flash.message += it + "<br/>"
            }
        }
        redirect(action: 'showStockCard', params: ['product.id': productInstance?.id])
    }

    def create = {
        def inventoryItem = new InventoryItem(params)
        if (InventoryItem && inventoryItem.save()) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'inventoryItem.label'), params.id])}"
        } else {
            flash.message = "${warehouse.message(code: 'default.not.created.message', args: [warehouse.message(code: 'inventoryItem.label')])}"
        }
        redirect(action: 'showLotNumbers', params: ['product.id': inventoryItem?.product?.id])
    }


    def delete = {
        def inventoryItem = InventoryItem.get(params.id)
        if (inventoryItem) {
            try {
                inventoryItem.delete(flush: true)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'inventoryItem.label', default: 'Attribute'), params.id])}"
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'inventoryItem.label', default: 'Attribute'), params.id])}"
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventoryItem.label', default: 'Attribute'), params.id])}"
        }

        redirect(action: 'showLotNumbers', params: ['product.id': inventoryItem?.product?.id])
    }

    def deleteInventoryItem = {
        def inventoryItem = InventoryItem.get(params.id)
        def productInstance = inventoryItem?.product
        def inventoryInstance = Inventory.get(inventoryItem?.inventory?.id)

        if (inventoryItem && inventoryInstance) {
            inventoryInstance.removeFromInventoryItems(inventoryItem).save()
            inventoryItem.delete()
        } else {
            inventoryItem.errors.reject("inventoryItem.error", "Could not delete inventory item")
            params.put("product.id", productInstance?.id)
            params.put("inventory.id", inventoryInstance?.id)
            log.info "Params " + params
            chain(action: "createInventoryItem", model: [inventoryItem: inventoryItem], params: params)
            return
        }
        redirect(action: 'showStockCard', params: ['product.id': productInstance?.id])

    }


    def saveTransactionEntry = {
        def productInstance = Product.get(params?.product?.id)
        if (!productInstance) {
            flash.message = "${warehouse.message(code: 'default.notfound.message', args: [warehouse.message(code: 'product.label', default: 'Product'), productInstance.id])}"
            redirect(action: "showStockCard", id: productInstance?.id)
        } else {
            def inventoryItem = inventoryService.findByProductAndLotNumber(productInstance, params.lotNumber ?: null)
            if (!inventoryItem) {
                flash.message = "${warehouse.message(code: 'default.notfound.message', args: [warehouse.message(code: 'inventoryItem.label', default: 'Inventory item'), params.lotNumber])}"
            } else {
                def transactionInstance = new Transaction(params)
                def transactionEntry = new TransactionEntry(params)

                // If we're transferring stock to another location OR consuming stock,
                // then we need to make sure the quantity is negative
                if (transactionInstance?.destination?.id != session?.warehouse?.id
                        || transactionInstance?.transactionType?.name == 'Consumption') {
                    if (transactionEntry.quantity > 0) {
                        transactionEntry.quantity = -transactionEntry.quantity
                    }
                }

                transactionEntry.inventoryItem = inventoryItem
                if (!transactionEntry.hasErrors() &&
                        transactionInstance.addToTransactionEntries(transactionEntry).save(flush: true)) {
                    flash.message = "${warehouse.message(code: 'inventoryItem.savedTransactionEntry.label')}"
                } else {
                    transactionInstance.errors.each { log.info it }
                    transactionEntry.errors.each { log.info it }
                    flash.message = "${warehouse.message(code: 'inventoryItem.inventoryItem.unableToSaveTransactionEntry.message.label')}"
                }
            }
        }
        redirect(action: "showStockCard", params: ['product.id': productInstance?.id])
    }
}
