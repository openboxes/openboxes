/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.reporting

import grails.converters.JSON
import grails.plugin.springcache.annotations.CacheFlush
import org.apache.commons.lang.StringEscapeUtils
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.api.StockMovementItem
import org.pih.warehouse.core.Location
import org.pih.warehouse.importer.CSVUtils
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.report.ChecklistReportCommand
import org.pih.warehouse.report.InventoryReportCommand
import org.pih.warehouse.report.MultiLocationInventoryReportCommand
import org.quartz.JobKey
import org.quartz.impl.StdScheduler

import java.math.RoundingMode
import java.text.DateFormat
import java.text.SimpleDateFormat

class ReportController {

    def dataSource
    def dataService
    def documentService
    def inventoryService
    def productService
    def reportService
    def messageService
    def inventorySnapshotService
    def productAvailabilityService
    def stockMovementService
    def forecastingService
    def shipmentService
    def orderService
    StdScheduler quartzScheduler

    def refreshProductDemand = {
        reportService.refreshProductDemandData()
        render([success: true] as JSON)
    }

    def refreshProductAvailability = {
        productAvailabilityService.refreshProductAvailability(Boolean.TRUE)
        render([success: true] as JSON)
    }

    def refreshTransactionFact = {
        reportService.buildTransactionFact()
        render([success: true] as JSON)
    }

    def refreshConsumptionFact = {
        reportService.buildConsumptionFact()
        render([success: true] as JSON)
    }

    def refreshStockoutFact = {
        reportService.buildStockoutFact()
        render([success: true] as JSON)
    }

    def buildFacts = {
        def startTime = System.currentTimeMillis()
        def results = reportService.buildFacts()
        def responseTime = "${(System.currentTimeMillis() - startTime)} ms"
        render([responseTime: responseTime, results: results, groovyVersion: GroovySystem.version] as JSON)
    }

    def truncateFacts = {
        def startTime = System.currentTimeMillis()
        reportService.truncateFacts()
        def responseTime = "${(System.currentTimeMillis() - startTime)} ms"
        render([responseTime: responseTime] as JSON)
    }

    def buildDimensions = {
        def startTime = System.currentTimeMillis()
        reportService.buildDimensions()
        def responseTime = "${(System.currentTimeMillis() - startTime)} ms"
        render([responseTime: responseTime] as JSON)
    }

    def truncateDimensions = {
        def startTime = System.currentTimeMillis()
        reportService.truncateDimensions()
        def responseTime = "${(System.currentTimeMillis() - startTime)} ms"
        render([responseTime: responseTime] as JSON)
    }

    def exportBinLocation = {
        long startTime = System.currentTimeMillis()
        log.info "Export by bin location " + params
        Location location = Location.get(session.warehouse.id)
        List binLocations = inventoryService.getQuantityByBinLocation(location)
        def products = binLocations.collect { it.product.productCode }.unique()
        data = binLocations.collect {
            [
                productCode: it?.product?.productCode,
                productName: it?.product?.name,
                lotNumber: it?.inventoryItem?.lotNumber,
                expirationDate: CSVUtils.formatDate(it?.inventoryItem?.expirationDate),
                binLocation: it?.binLocation?.name ?: "Default Bin",
                quantity: CSVUtils.formatInteger(it?.quantity),
                unitCost: CSVUtils.formatInteger(it?.unitCost),
                totalValue: CSVUtils.formatInteger(it?.totalValue)
            ]
        }

        if (params.downloadFormat == "csv") {
            def filename = "Bin Locations - ${location.name}.csv"
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
            render(contentType: 'text/csv', text: CSVUtils.dumpMaps(data))
            return
        }

        render([elapsedTime: (System.currentTimeMillis() - startTime), binLocationCount: binLocations.size(), productCount: products.size(), binLocations: binLocations] as JSON)
    }


    def exportDemandReport = {
        long startTime = System.currentTimeMillis()
        Location location = Location.get(session.warehouse.id)
        def data = forecastingService.getDemandDetails(location, null)
        if (params.downloadFormat == "csv") {
            def filename = "Product Demand - ${location.name}.csv"
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
            render(contentType: 'text/csv', text: CSVUtils.dumpMaps(data))
            return
        }
        render([responseTime: (System.currentTimeMillis() - startTime), count: data.size(), data: data] as JSON)
    }


    def exportInventoryReport = {
        println "Export inventory report " + params
        def records = []
        def location = Location.get(session.warehouse.id)
        if (params.list("status")) {
            def data = reportService.calculateQuantityOnHandByProductGroup(location.id)
            params.list('status').collect(records) {
                Map<String, Map> details = data.productGroupDetails[it]
                [
                    Status: details?.status,
                    'Product group': details?.name,
                    'Product codes': details?.productCodes?.join(','),
                    Min: details?.minQuantity,
                    Reorder: details?.reorderQuantity,
                    Max: details?.maxQuantity,
                    QoH: details?.onHandQuantity,
                    Value: details?.totalValue,
                ]
            }
        }

        def filename = "Stock report - " + location.name + ".csv"
        response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
        render(contentType: "text/csv", text: CSVUtils.dumpMaps(records.unique()))
        return
    }

    def showInventoryReport = {}


    def showInventorySamplingReport = {

        def count = (params.n ?: 10).toInteger()
        def location = Location.get(session.warehouse.id)
        def records = []

        try {
            def inventoryItems = inventoryService.getInventorySampling(location, count)
            inventoryItems.collect(records) {
                [
                    'Product Code': it?.product?.productCode,
                    Product: it?.product?.name,
                    'Lot number': it?.lotNumber,
                    'Expiration date': CSVUtils.formatDate(it?.expirationDate),
                    'Bin location': it?.product?.getInventoryLevel(location?.id)?.binLocation,
                    'On hand quantity': null,  // FIXME previous implementation skipped this column
                ]
            }
        } catch (RuntimeException e) {
            log.error(e.message)
        }

        response.setHeader("Content-disposition", "attachment; filename=\"Inventory-sampling-${new Date().format("yyyyMMdd-hhmmss")}.csv\"")
        render(contentType: 'text/csv', text: CSVUtils.dumpMaps(records))

    }


    def showConsumptionReport = {

        def transactions = Transaction.findAllByTransactionDateBetween(new Date() - 10, new Date())

        [transactions: transactions]
    }

    def showTransactionReport = {
        InventoryReportCommand command = new InventoryReportCommand()
        command.location = Location.get(session.warehouse.id)
        command.rootCategory = productService.getRootCategory()

        def triggers = quartzScheduler.getTriggersOfJob(new JobKey("org.pih.warehouse.jobs.RefreshTransactionFactJob"))
        def previousFireTime = triggers*.previousFireTime.max()
        def nextFireTime = triggers*.nextFireTime.max()
        def locationKey = LocationDimension.findByLocationId(command?.location?.id)
        def model = [
                command           : command,
                locationKey       : locationKey,
                transactionCount  : locationKey ? TransactionFact.countByLocationKey(locationKey) : 0,
                productCount      : TransactionFact.countDistinctProducts(locationKey?.locationId).list(),
                minTransactionDate: TransactionFact.minTransactionDate(locationKey?.locationId).list(),
                maxTransactionDate: TransactionFact.maxTransactionDate(locationKey?.locationId).list(),
                previousFireTime  : previousFireTime,
                nextFireTime      : nextFireTime,
        ]

        return model
    }

    def showTransactionReportDialog = {
        def url = createLink(controller: "json", action: "getTransactionReportDetails", params:params)
        render(template: "dataTableDialog", model: [url: url])
    }

    def showShippingReport = { ChecklistReportCommand command ->
        command.rootCategory = productService.getRootCategory()
        if (!command?.hasErrors()) {
            reportService.generateShippingReport(command)
        }
        [command: command]
    }

    def showPaginatedPackingListReport = { ChecklistReportCommand command ->
        command.rootCategory = productService.getRootCategory()
        if (!command?.hasErrors()) {
            reportService.generateShippingReport(command)
        }
        [command: command]
    }

    def printShippingReport = { ChecklistReportCommand command ->
        command.rootCategory = productService.getRootCategory()
        if (!command?.hasErrors()) {
            reportService.generateShippingReport(command)
        }
        [command: command]
    }

    def printPickListReport = { ChecklistReportCommand command ->

        Map binLocations
        if (!command?.hasErrors()) {
            reportService.generateShippingReport(command)
            binLocations = inventoryService.getBinLocations(command.shipment)
        }
        [command: command, binLocations: binLocations]
    }


    def printPaginatedPackingListReport = { ChecklistReportCommand command ->
        try {
            command.rootCategory = productService.getRootCategory()
            if (!command?.hasErrors()) {
                reportService.generateShippingReport(command)
            }
        } catch (Exception e) {
            log.error("error", e)
            e.printStackTrace()
        }
        [command: command]
    }


    def downloadTransactionReport = {
        def baseUri = request.scheme + "://" + request.serverName + ":" + request.serverPort

        // JSESSIONID is required because otherwise the login page is rendered
        def url = baseUri + params.url + ";jsessionid=" + session.getId()
        url += "?print=true"
        url += "&location.id=" + params.location.id
        url += "&category.id=" + params.category.id
        url += "&startDate=" + params.startDate
        url += "&endDate=" + params.endDate
        url += "&showTransferBreakdown=" + params.showTransferBreakdown
        url += "&hideInactiveProducts=" + params.hideInactiveProducts
        url += "&insertPageBreakBetweenCategories=" + params.insertPageBreakBetweenCategories
        url += "&includeChildren=" + params.includeChildren
        url += "&includeEntities=true"

        // Let the browser know what content type to expect
        response.setContentType("application/pdf")

        // Render pdf to the response output stream
        log.info "BaseUri is $baseUri"
        log.info("Session ID: " + session.id)
        log.info "Fetching url $url"
        reportService.generatePdf(url, response.getOutputStream())
    }

    def downloadShippingReport = {
        if (params.format == 'docx') {
            def tempFile = documentService.generateChecklistAsDocx()
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
            response.outputStream << tempFile.readBytes()
        } else if (params.format == 'pdf') {
            def baseUri = request.scheme + "://" + request.serverName + ":" + request.serverPort
            def url = baseUri + params.url + ";jsessionid=" + session.getId()
            url += "?print=true&orientation=portrait"
            url += "&shipment.id=" + params.shipment.id
            url += "&includeEntities=true"
            log.info "Fetching url $url"
            response.setContentType("application/pdf")
            reportService.generatePdf(url, response.getOutputStream())
        } else {
            throw new UnsupportedOperationException("Format '${params.format}' not supported")
        }
    }

    @CacheFlush(["binLocationReportCache", "binLocationSummaryCache"])
    def clearBinLocationCache = {
        flash.message = "Cache have been flushed"
        redirect(action: "showBinLocationReport")
    }


    def showBinLocationReport = {

        log.info "showBinLocationReport " + params
        def startTime = System.currentTimeMillis()
        String locationId = params?.location?.id ?: session?.warehouse?.id
        Location location = Location.get(locationId)

        List statuses = ["inStock", "outOfStock"].collect { status ->
            String messageCode = "binLocationSummary.${status}.label"
            String label = messageService.getMessage(messageCode)
            [status: status, label: label]
        }



        try {
            if (params.downloadAction == "downloadStockReport") {
                def binLocations = productAvailabilityService.getQuantityOnHandByBinLocation(location)

                // Filter on status
                if (params.status) {
                    binLocations = binLocations.findAll { it.status == params.status }
                }

                def output = binLocations.collect { binLocation ->
                    [
                        (g.message(code: 'default.status.label')): binLocation?.status,
                        (g.message(code: 'product.productCode.label')): binLocation?.product?.productCode,
                        (g.message(code: 'product.label')): binLocation?.product?.name,
                        (g.message(code: 'category.label')): binLocation?.product?.category?.name,
                        (g.message(code: 'product.formulary.label')): binLocation?.product?.productCatalogsToString(),
                        (g.message(code: 'tag.label')): binLocation?.product?.tagsToString(),
                        (g.message(code: 'inventoryItem.lotNumber.label')): binLocation?.inventoryItem?.lotNumber,
                        (g.message(code: 'inventoryItem.expirationDate.label')): CSVUtils.formatDate(binLocation?.inventoryItem?.expirationDate),
                        (g.message(code: 'location.zone.label')): binLocation?.binLocation?.zone?.name,
                        (g.message(code: 'location.binLocation.label')): binLocation?.binLocation?.name ?: g.message(code: 'default.label'),
                        (g.message(code: 'default.quantity.label')): binLocation?.quantity,
                        (g.message(code: 'default.quantityAvailableToPromise.label')): binLocation?.quantityAvailableToPromise,
                        (g.message(code: 'product.unitCost.label')): binLocation?.unitCost,
                        (g.message(code: 'product.totalValue.label')): binLocation?.totalValue
                    ]
                }

                def filename = "Bin Location Report - ${location?.name} - ${params.status ?: 'All'}.csv"
                response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
                render(contentType: 'text/csv', text: CSVUtils.dumpMaps(output))
                return
            }
            else if (params.downloadAction == "downloadStockMovement") {

                StockMovement stockMovement = new StockMovement()
                def entries = productAvailabilityService.getQuantityOnHandByBinLocation(location)
                entries.findAll { entry -> entry.quantity > 0 }
                    .groupBy { it.product }
                    .each { k, v ->
                        def quantity = v.sum { it.quantity }
                        stockMovement.lineItems.add(new StockMovementItem(product: k, quantityRequested: quantity))
                    }
                List lineItems = stockMovementService.buildStockMovementItemList(stockMovement)
                response.setHeader('Content-disposition', 'attachment; filename="StockMovementItems-CurrentStock.csv"')
                render(contentType: 'text/csv', text: CSVUtils.dumpMaps(lineItems))
                return
            }
        } catch (Exception e) {
            log.error("Unable to generate bin location report due to error: " + e.message, e)
            flash.message = e.message
        }

        log.info("Show bin location report: " + (System.currentTimeMillis() - startTime) + " ms")
        [
                location   : location,
                elapsedTime: (System.currentTimeMillis() - startTime),
                statuses   : statuses
        ]

    }

    def showOnOrderReport = {
        if (params.downloadAction == "downloadOnOrderReport") {
            def location = Location.get(session.warehouse.id)
            def items = orderService.getPendingInboundOrderItems(location)
            items += shipmentService.getPendingInboundShipmentItems(location)

            if (items) {
                def records = []
                items.sort { a, b ->
                    a.product?.productCode <=> b.product?.productCode
                }.each {
                    def isOrderItem = it instanceof OrderItem
                    records << [
                        Code: it?.product.productCode,
                        Product: it?.product.name,
                        'Quantity Ordered Not Shipped': isOrderItem ? it?.quantityRemaining * it?.quantityPerUom : '',
                        'Quantity Shipped Not Received': isOrderItem ? '' : it?.quantityRemaining,
                        'PO Number': isOrderItem ? it?.order?.orderNumber : (it?.shipment?.isFromPurchaseOrder ? it?.orderNumber : ''),
                        'PO Description': isOrderItem ? it?.order?.name : (it.shipment.isFromPurchaseOrder ? it.orderName : ''),
                        'Supplier Organization': isOrderItem ? it?.order?.origin?.organization?.name : it.shipment?.origin?.organization?.name,
                        'Supplier Location': isOrderItem ? it?.order?.origin?.name : it?.shipment?.origin?.name,
                        'Supplier Location Group': isOrderItem ? it?.order?.origin?.locationGroup?.name : it.shipment?.origin?.locationGroup?.name,
                        'Estimated Goods Ready Date': isOrderItem ? CSVUtils.formatDate(it?.actualReadyDate) : '',
                        'Shipment Number': isOrderItem ? '' : it?.shipment?.shipmentNumber,
                        'Ship Date': isOrderItem ? '' : CSVUtils.formatDate(it?.shipment?.expectedShippingDate),
                        'Expected Delivery Date': isOrderItem ? '' : CSVUtils.formatDate(it?.shipment?.expectedDeliveryDate),
                        'Shipment Type': isOrderItem ? '' : it?.shipment?.shipmentType?.name,
                    ]
                }

                response.setHeader("Content-disposition", "attachment; filename=\"Detailed-Order-Report-${new Date().format('MM/dd/yyyy')}.csv\"")
                render(contentType: 'text/csv', text: CSVUtils.dumpMaps(records))
            }
        } else if(params.downloadAction == "downloadSummaryOnOrderReport") {
            def location = Location.get(session.warehouse.id)
            def data = reportService.getOnOrderSummary(location)

            if (data) {
                def records = data.sort {
                    it.productCode
                }.collect {
                    [
                        Code: it?.productCode,
                        Product: it?.productName,
                        'Quantity Ordered Not Shipped': it?.qtyOrderedNotShipped,
                        'Quantity Shipped Not Received': it?.qtyShippedNotReceived,
                        'Total On Order': it?.totalOnOrder,
                        'Total On Hand': it?.totalOnHand,
                        'Total On Hand and On Order': it?.totalOnHandAndOnOrder,
                    ]
                }
                response.setHeader('Content-disposition', "attachment; filename=\"Detailed-Order-Report-${new Date().format('MM/dd/yyyy')}.csv\"")
                render(contentType: 'text/csv', text: CSVUtils.dumpMaps(records))
            }
        }
    }

    def showInventoryByLocationReport = { MultiLocationInventoryReportCommand command ->
        command.entries = productAvailabilityService.getQuantityOnHandByProduct(command.locations)
        def csv = CSVUtils.getCSVPrinter()

        if (params.button == "download") {
            try {
                if (command.entries) {
                    def csvHeader = [
                        'Code',
                        'Product',
                        'Category',
                        'Formularies',
                        'Tags'
                    ]

                    command.locations?.collect(csvHeader) { it?.name }
                    csvHeader.addAll(
                        'QoH Total',
                        'Quantity Available Total'
                    )

                    csv.printRecord(csvHeader)

                    command.entries.each { k, v ->
                        def csvRow = [
                            k?.productCode,
                            k?.name,
                            k?.category?.name,
                            k?.getProductCatalogs()?.collect { it.name }?.join(','),
                            k?.tagsToString()
                        ]

                        command.locations?.collect(csvRow) { v[it?.id]?.quantityOnHand ?: 0 }
                        csvRow.addAll(
                            v?.values()?.quantityOnHand?.sum(),
                            v?.values()?.quantityAvailableToPromise?.sum()
                        )
                        csv.printRecord(csvRow)
                    }
                }

            } catch (RuntimeException e) {
                log.error(e.message)
            }

            response.setHeader('Content-disposition', "attachment; filename=\"Inventory-by-location-${new Date().format('yyyyMMdd-hhmmss')}.csv\"")
            render(contentType: 'text/csv', text: csv.out.toString())
        }

        render(view: 'showInventoryByLocationReport', model: [command: command])
    }

    def showRequestDetailReport = {
        def origin = Location.get(session.warehouse.id)
        params.origin = origin.id
        render(view: 'showRequestDetailReport', params: params)
    }

    def showCycleCountReport = {
        Location location = Location.load(session.warehouse.id)
        List binLocations = inventoryService.getQuantityByBinLocation(location)
        log.info "Returned ${binLocations.size()} bin locations for location ${location}"
        String dateFormat = grailsApplication.config.openboxes.expirationDate.format

        List rows = binLocations.collect { row ->

            // Required in order to avoid lazy initialization exception that occurs because all
            // of the querying / session work that was done above was executed in worker threads
            Product product = Product.load(row?.product?.id)

            def latestInventoryDate = row?.product?.latestInventoryDate(location.id) ?: row?.product.earliestReceivingDate(location.id)
            Map dataRow = params.print ? [
                            "Product code"        : StringEscapeUtils.escapeCsv(row?.product?.productCode),
                            "Product name"        : row?.product.name ?: "",
                            "Lot number"          : StringEscapeUtils.escapeCsv(row?.inventoryItem.lotNumber ?: ""),
                            "Expiration date"     : row?.inventoryItem.expirationDate ? row?.inventoryItem.expirationDate.format(dateFormat) : "",
                            "Bin location"        : StringEscapeUtils.escapeCsv(row?.binLocation?.name ?: ""),
                            "OB QOH"              : row?.quantity ?: 0,
                            "Physical QOH"        : "",
                            "Comment"             : "",
                            "Generic product"     : row?.genericProduct?.name ?: "",
                            "Category"            : StringEscapeUtils.escapeCsv(row?.category?.name ?: ""),
                            "Formularies"         : product.productCatalogs.join(", ") ?: "",
                            "ABC Classification"  : StringEscapeUtils.escapeCsv(row?.product.getAbcClassification(location.id) ?: ""),
                            "Status"              : g.message(code: "binLocationSummary.${row?.status}.label"),
                            "Last Inventory Date" : latestInventoryDate ? latestInventoryDate.format(dateFormat) : "",
                    ] : [
                            productCode       : StringEscapeUtils.escapeCsv(row?.product?.productCode),
                            productName       : row?.product.name ?: "",
                            genericProduct    : row?.genericProduct?.name ?: "",
                            category          : StringEscapeUtils.escapeCsv(row?.category?.name ?: ""),
                            formularies       : product.productCatalogs.join(", ") ?: "",
                            lotNumber         : StringEscapeUtils.escapeCsv(row?.inventoryItem.lotNumber ?: ""),
                            expirationDate    : row?.inventoryItem.expirationDate ? row?.inventoryItem.expirationDate.format(dateFormat) : "",
                            abcClassification : StringEscapeUtils.escapeCsv(row?.product.getAbcClassification(location.id) ?: ""),
                            binLocation       : StringEscapeUtils.escapeCsv(row?.binLocation?.name ?: ""),
                            status            : g.message(code: "binLocationSummary.${row?.status}.label"),
                            lastInventoryDate : latestInventoryDate ? latestInventoryDate.format(dateFormat) : "",
                            quantityOnHand    : row?.quantity ?: 0,
                    ]

            return dataRow
        }

        if (params.print) {
            def filename = "CycleCountReport-${location.name}-${new Date().format("dd MMM yyyy hhmmss")}"
            response.contentType = "application/vnd.ms-excel"
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}.xls\"")
            documentService.generateInventoryTemplate(response.outputStream, rows)
            return
        }

        render(view: "showCycleCountReport", model: [rows: rows])
    }

    def showForecastReport = {
        def origin = Location.get(session.warehouse.id)
        params.origin = origin.name

        // Export as XLS
        if (params.format == "text/csv" && params.print) {
            params.originId = session.warehouse.id
            DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy")
            if (params.startDate && params.endDate) {
                params.startDate = dateFormat.parse(params.startDate)
                params.endDate = dateFormat.parse(params.endDate)
            } else {
                Integer demandPeriod = grailsApplication.config.openboxes.forecasting.demandPeriod ?: 365
                Date today = new Date()
                params.startDate = today - demandPeriod
                params.endDate = today
            }
            def data = reportService.getForecastReport(params)

            def rows = []
            data.collect { item ->
                def product = Product.get(item.productId)
                def inventoryLevel
                if (!params.replenishmentPeriodDays || !params.leadTimeDays) {
                    inventoryLevel = InventoryLevel.findByProduct(product)
                }
                Integer replenishmentPeriodDays = params.replenishmentPeriodDays ? params.replenishmentPeriodDays.toInteger() : inventoryLevel && inventoryLevel.replenishmentPeriodDays ? inventoryLevel.replenishmentPeriodDays : 365
                Integer leadTimeDays = params.leadTimeDays ? params.leadTimeDays.toInteger() : inventoryLevel && inventoryLevel.expectedLeadTimeDays ? inventoryLevel.expectedLeadTimeDays : 365
                def productExpiry = forecastingService.getProductExpiry(origin, replenishmentPeriodDays + leadTimeDays, item.productId)
                def quantityOnOrder = item?.totalOnOrder ?: 0
                def quantityOnHand = item?.totalOnHand ?: productAvailabilityService.getQuantityOnHand(product, origin)
                def quantityExpiring = productExpiry.collect {it.quantity_on_hand}.sum() ?: 0
                def quantityAvailable = quantityOnOrder + quantityOnHand - quantityExpiring ?: 0
                def averageDemand = item.averageMonthlyDemand ? item.averageMonthlyDemand?.setScale(1, RoundingMode.HALF_UP) : 0
                def monthsOfStock = ((replenishmentPeriodDays + leadTimeDays) / 30).setScale(1, RoundingMode.HALF_UP) ?: 0
                def quantityNeeded = averageDemand * monthsOfStock ?: 0
                def quantityToOrder = BigDecimal.ZERO.max(quantityNeeded - quantityAvailable)
                def unitPrice = product?.pricePerUnit ?: 0

                def printRow = [
                        'Product code'                    : product.productCode ?: '',
                        'Name'                            : product.name,
                        'Order Period (Days)'             : replenishmentPeriodDays,
                        'Lead Time (Days)'                : leadTimeDays,
                        'Qty On Order'                    : quantityOnOrder,
                        'Qty On Hand'                     : quantityOnHand,
                        'Qty Expiring within time period' : quantityExpiring,
                        'Qty Available'                   : quantityAvailable,
                        'Average Demand/Month'            : averageDemand,
                        'Months of Stock Needed (order period + lead time in months)'  : monthsOfStock,
                        'Qty Needed'                      : quantityNeeded,
                        'Qty to Order'                    : quantityToOrder,
                        'Estimated Cost'                  : unitPrice * quantityToOrder,
                ]

                rows << printRow
            }

            rows.sort { it["Product code"] }
            if (rows.size() > 0) {
                def filename = "ForecastReport-${new Date().format("dd MMM yyyy hhmmss")}"
                response.contentType = "application/vnd.ms-excel"
                response.setHeader("Content-disposition", "attachment; filename=\"${filename}.xls\"")
                documentService.generateExcel(response.outputStream, rows)
                response.outputStream.flush()
            } else {
                log.info("Unable to generate forecast report due to lack of data")
                flash.message = "Unable to generate forecast report due to lack of data"
            }
        }

        render(view: 'showForecastReport', params: params)
    }
}
