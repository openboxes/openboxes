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
import org.grails.plugins.csv.CSVWriter
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.api.StockMovementItem
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.jobs.RefreshTransactionFactJob
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.report.ChecklistReportCommand
import org.pih.warehouse.report.InventoryReportCommand
import org.pih.warehouse.report.MultiLocationInventoryReportCommand
import org.pih.warehouse.report.ProductReportCommand
import org.quartz.JobKey
import org.quartz.impl.StdScheduler
import util.ReportUtil

class ReportController {

    def dataService
    def documentService
    def inventoryService
    def productService
    def reportService
    def messageService
    def inventorySnapshotService
    def stockMovementService
    def forecastingService
    def shipmentService
    def orderService
    StdScheduler quartzScheduler

    def refreshTransactionFact = {
        reportService.buildDimensions()
        reportService.buildFacts()
        render(success: true)
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

    def binLocationCsvHeader = { binLocation ->
        String csv = ""
        if (binLocation) {
            csv += g.message(code: 'default.status.label') + ","
            csv += g.message(code: 'product.productCode.label') + ","
            csv += g.message(code: 'product.label') + ","
            csv += g.message(code: 'category.label') + ","
            csv += g.message(code: 'product.formulary.label') + ","
            csv += g.message(code: 'tag.label') + ","
            csv += g.message(code: 'inventoryItem.lotNumber.label') + ","
            csv += g.message(code: 'inventoryItem.expirationDate.label') + ","
            csv += g.message(code: 'location.binLocation.label') + ","
            csv += g.message(code: 'default.quantity.label') + ","
            csv += g.message(code: 'product.unitCost.label') + ","
            csv += g.message(code: 'product.totalValue.label')
            csv += "\n"
        }
        return csv

    }

    def binLocationCsvRow = { binLocation ->
        String csv = ""
        if (binLocation) {
            String defaultBinLocation = g.message(code: 'default.label')
            String expirationDate = g.formatDate(date: binLocation?.inventoryItem?.expirationDate, format: "dd/MMM/yyyy")
            csv += binLocation.status + ","
            csv += StringEscapeUtils.escapeCsv(binLocation?.product?.productCode) + ","
            csv += StringEscapeUtils.escapeCsv(binLocation?.product?.name) + ","
            csv += StringEscapeUtils.escapeCsv(binLocation?.product?.category?.name) + ","
            csv += StringEscapeUtils.escapeCsv(binLocation?.product?.productCatalogsToString()) + ","
            csv += StringEscapeUtils.escapeCsv(binLocation?.product?.tagsToString()) + ","
            csv += StringEscapeUtils.escapeCsv(binLocation?.inventoryItem?.lotNumber) + ","
            csv += StringEscapeUtils.escapeCsv(expirationDate) + ","
            csv += StringEscapeUtils.escapeCsv(binLocation?.binLocation?.name ?: defaultBinLocation) + ","
            csv += binLocation.quantity + ","
            csv += binLocation.unitCost + ","
            csv += binLocation.totalValue
            csv += "\n"
        }
        return csv
    }


    def exportBinLocation = {
        long startTime = System.currentTimeMillis()
        log.info "Export by bin location " + params
        Location location = Location.get(session.warehouse.id)
        List binLocations = inventoryService.getQuantityByBinLocation(location)
        def products = binLocations.collect { it.product.productCode }.unique()
        binLocations = binLocations.collect {
            [
                    productCode   : it.product.productCode,
                    productName   : it.product.name,
                    lotNumber     : it.inventoryItem.lotNumber,
                    expirationDate: it.inventoryItem.expirationDate,
                    binLocation   : it?.binLocation?.name ?: "Default Bin",
                    quantity      : formatNumber(number: it.quantity),
                    unitCost      : formatNumber(number: it.unitCost),
                    totalValue     : formatNumber(number: it.totalValue)
            ]
        }

        if (params.downloadFormat == "csv") {
            String csv = ReportUtil.getCsvForListOfMapEntries(binLocations)
            def filename = "Bin Locations - ${location.name}.csv"
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
            render(contentType: "text/csv", text: csv)
            return
        }

        render([elapsedTime: (System.currentTimeMillis() - startTime), binLocationCount: binLocations.size(), productCount: products.size(), binLocations: binLocations] as JSON)
    }


    def exportDemandReport = {
        long startTime = System.currentTimeMillis()
        Location location = Location.get(session.warehouse.id)
        def data = forecastingService.getDemandDetails(location, null)
        if (params.downloadFormat == "csv") {
            String csv = ReportUtil.getCsvForListOfMapEntries(data)
            def filename = "Product Demand - ${location.name}.csv"
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
            render(contentType: "text/csv", text: csv)
            return
        }
        render([responseTime: (System.currentTimeMillis() - startTime), count: data.size(), data: data] as JSON)
    }


    def exportInventoryReport = {
        println "Export inventory report " + params
        def map = []
        def location = Location.get(session.warehouse.id)
        if (params.list("status")) {
            def data = reportService.calculateQuantityOnHandByProductGroup(location.id)
            params.list("status").each {
                println it
                map += data.productGroupDetails[it].values()
            }
            map.unique()
        }

        def filename = "Stock report - " + location.name + ".csv"
        response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
        render(contentType: "text/csv", text: ReportUtil.getCsv(map))
        return
    }

    def showInventoryReport = {}


    def showInventorySamplingReport = {

        def sw = new StringWriter()
        def count = (params.n ?: 10).toInteger()
        def location = Location.get(session.warehouse.id)
        def inventoryItems = []

        try {
            inventoryItems = inventoryService.getInventorySampling(location, count)

            if (inventoryItems) {

                println inventoryItems
                //sw.append(csvrows[0].keySet().join(",")).append("\n")
                sw.append("Product Code").append(",")
                sw.append("Product").append(",")
                sw.append("Lot number").append(",")
                sw.append("Expiration date").append(",")
                sw.append("Bin location").append(",")
                sw.append("On hand quantity").append(",")
                sw.append("\n")
                inventoryItems.each { inventoryItem ->
                    if (inventoryItem) {
                        def inventoryLevel = inventoryItem?.product?.getInventoryLevel(location.id)
                        sw.append('"' + (inventoryItem?.product?.productCode ?: "").toString()?.replace('"', '""') + '"').append(",")
                        sw.append('"' + (inventoryItem?.product?.name ?: "").toString()?.replace('"', '""') + '"').append(",")
                        sw.append('"' + (inventoryItem?.lotNumber ?: "").toString()?.replace('"', '""') + '"').append(",")
                        sw.append('"' + inventoryItem?.expirationDate.toString()?.replace('"', '""') + '"').append(",")
                        sw.append('"' + (inventoryLevel?.binLocation ?: "")?.toString()?.replace('"', '""') + '"').append(",")
                        sw.append("\n")
                    }
                }
            }

        } catch (RuntimeException e) {
            log.error(e.message)
            sw.append(e.message)
        }

        response.setHeader("Content-disposition", "attachment; filename=\"Inventory-sampling-${new Date().format("yyyyMMdd-hhmmss")}.csv\"")
        render(contentType: "text/csv", text: sw.toString(), encoding: "UTF-8")

    }


    def showConsumptionReport = {

        def transactions = Transaction.findAllByTransactionDateBetween(new Date() - 10, new Date())

        [transactions: transactions]
    }


    def showProductReport = { ProductReportCommand command ->
        if (!command?.hasErrors()) {
            reportService.generateProductReport(command)
        }

        [command: command]
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
                transactionCount  : TransactionFact.countByLocationKey(locationKey),
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

    def generateTransactionReport = { InventoryReportCommand command ->
        // We always need to initialize the root category
        command.rootCategory = productService.getRootCategory()
        if (!command?.hasErrors()) {
            reportService.generateTransactionReport(command)
        }
        render(view: 'showTransactionReport', model: [command: command])
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

                def binLocations = inventorySnapshotService.getQuantityOnHandByBinLocation(location)

                // Filter on status
                if (params.status) {
                    binLocations = binLocations.findAll { it.status == params.status }
                }

                String csv = ReportUtil.getCsvForListOfMapEntries(binLocations, binLocationCsvHeader, binLocationCsvRow)
                def filename = "Bin Location Report - ${location?.name} - ${params.status ?: 'All'}.csv"
                response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
                render(contentType: "text/csv", text: csv)
                return
            }
            else if (params.downloadAction == "downloadStockMovement") {

                StockMovement stockMovement = new StockMovement()
                def entries = inventorySnapshotService.getQuantityOnHandByBinLocation(location)
                entries = entries.findAll { entry -> entry.quantity > 0 }
                entries = entries.groupBy { it.product }
                entries.each { k, v ->
                    def quantity = v.sum { it.quantity }
                    stockMovement.lineItems.add(
                            new StockMovementItem(product: k,
                                    quantityRequested: quantity))
                }
                List lineItems = stockMovementService.buildStockMovementItemList(stockMovement)
                String csv = dataService.generateCsv(lineItems)
                response.setHeader("Content-disposition", "attachment; filename=\"StockMovementItems-CurrentStock.csv\"")
                render(contentType: "text/csv", text: csv.toString(), encoding: "UTF-8")
                return;
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

    def showOrderReport = {
        if (params.downloadAction == "downloadOrderReport") {
            def location = Location.get(session.warehouse.id)
            def items = orderService.getPendingInboundOrderItems(location)
            items += shipmentService.getPendingInboundShipmentItems(location)

            if (items) {

                def sw = new StringWriter()
                def csv = new CSVWriter(sw, {
                    "Code" { it.productCode }
                    "Product" { it.productName }
                    "Quantity Ordered Not Shipped" { it.qtyOrderedNotShipped }
                    "Quantity Shipped Not Received" { it.qtyShippedNotReceived }
                    "PO Number" { it.orderNumber }
                    "PO Description" { it.orderDescription }
                    "Supplier Organization" { it.supplierOrganization }
                    "Supplier Location" { it.supplierLocation }
                    "Supplier Location Group" { it.supplierLocationGroup }
                    "Estimated Goods Ready Date" { it.estimatedGoodsReadyDate }
                    "Shipment Number" { it.shipmentNumber }
                    "Ship Date" { it.shipDate }
                    "Shipment Type" { it.shipmentType }
                })

                items.sort { a,b ->
                    a.product.productCode <=> b.product.productCode
                }.each {
                    def isOrderItem = it instanceof OrderItem
                    csv << [
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

                response.setHeader("Content-disposition", "attachment; filename=\"Detailed-Order-Report-${new Date().format("MM/dd/yyyy")}.csv\"")
                render(contentType: "text/csv", text: sw.toString(), encoding: "UTF-8")
            }
        }
    }

    def showInventoryByLocationReport = { MultiLocationInventoryReportCommand command ->
        command.entries = inventorySnapshotService.getQuantityOnHandByProduct(command.locations)

        if (params.button == "download") {
            def sw = new StringWriter()

            try {
                if (command.entries) {
                    sw.append("Code").append(",")
                    sw.append("Product").append(",")
                    sw.append("Category").append(",")
                    sw.append("Formularies").append(",")
                    sw.append("Tags").append(",")

                    command.locations?.each { location ->
                        String locationName = StringEscapeUtils.escapeCsv(location?.name)
                        sw.append(locationName).append(",")
                    }

                    sw.append("QoH Total")
                    sw.append("\n")
                    command.entries.each { entry ->
                        if (entry.key) {
                            def totalQuantity = entry.value?.values()?.sum()
                            def form = entry.key?.getProductCatalogs()?.collect {
                                it.name
                            }?.join(",")

                            sw.append('"' + (entry.key?.productCode ?: "").toString()?.replace('"', '""') + '"').append(",")
                            sw.append('"' + (entry.key?.name ?: "").toString()?.replace('"', '""') + '"').append(",")
                            sw.append('"' + (entry.key?.category?.name ?: "").toString()?.replace('"', '""') + '"').append(",")
                            sw.append('"' + (form ?: "").toString()?.replace('"', '""') + '"').append(",")
                            sw.append('"' + (entry.key?.tagsToString() ?: "")?.toString()?.replace('"', '""') + '"').append(",")

                            command.locations?.each { location ->
                                sw.append('"' + (entry.value[location?.id] != null ? entry.value[location?.id] : "").toString() + '"').append(",")
                            }

                            sw.append('"' + (totalQuantity != null ? totalQuantity : "").toString() + '"')
                            sw.append("\n")
                        }
                    }
                }

            } catch (RuntimeException e) {
                log.error(e.message)
                sw.append(e.message)
            }

            response.setHeader("Content-disposition", "attachment; filename=\"Inventory-by-location-${new Date().format("yyyyMMdd-hhmmss")}.csv\"")
            render(contentType: "text/csv", text: sw.toString(), encoding: "UTF-8")
        }

        render(view: 'showInventoryByLocationReport', model: [command: command])
    }

}
