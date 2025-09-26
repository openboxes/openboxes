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
import grails.gorm.transactions.Transactional
import grails.plugins.csv.CSVWriter
import grails.plugins.quartz.GrailsJobClassConstants
import org.apache.commons.lang.StringEscapeUtils
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.api.StockMovementItem
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationService
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.importer.CSVUtils
import org.pih.warehouse.inventory.BinLocationItem
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAvailability
import org.pih.warehouse.report.ChecklistReportCommand
import org.pih.warehouse.report.InventoryReportCommand
import org.pih.warehouse.report.MultiLocationInventoryReportCommand

import org.quartz.JobKey
import org.quartz.impl.StdScheduler
import util.ReportUtil

import java.math.RoundingMode
import java.text.DateFormat
import java.text.SimpleDateFormat

@Transactional
class ReportController {

    def dataSource
    def dataService
    def documentService
    def inventoryService
    def productService
    def reportService
    def inventorySnapshotService
    def productAvailabilityService
    def stockMovementService
    def forecastingService
    def shipmentService
    def orderService
    def userService
    LocationService locationService
    StdScheduler quartzScheduler

    def refreshProductDemand() {
        reportService.refreshProductDemandData()
        render([success: true] as JSON)
    }

    def refreshProductAvailability() {
        productAvailabilityService.refreshProductAvailability(Boolean.TRUE)
        render([success: true] as JSON)
    }

    def refreshTransactionFact() {
        reportService.buildTransactionFact()
        render([success: true] as JSON)
    }

    def refreshConsumptionFact() {
        reportService.buildConsumptionFact()
        render([success: true] as JSON)
    }

    def refreshStockoutFact() {
        reportService.buildStockoutFact()
        render([success: true] as JSON)
    }

    def buildFacts() {
        def startTime = System.currentTimeMillis()
        def results = reportService.buildFacts()
        def responseTime = "${(System.currentTimeMillis() - startTime)} ms"
        render([responseTime: responseTime, results: results, groovyVersion: GroovySystem.version] as JSON)
    }

    def truncateFacts() {
        def startTime = System.currentTimeMillis()
        reportService.truncateFacts()
        def responseTime = "${(System.currentTimeMillis() - startTime)} ms"
        render([responseTime: responseTime] as JSON)
    }

    def buildDimensions() {
        def startTime = System.currentTimeMillis()
        reportService.buildDimensions()
        def responseTime = "${(System.currentTimeMillis() - startTime)} ms"
        render([responseTime: responseTime] as JSON)
    }

    def truncateDimensions() {
        def startTime = System.currentTimeMillis()
        reportService.truncateDimensions()
        def responseTime = "${(System.currentTimeMillis() - startTime)} ms"
        render([responseTime: responseTime] as JSON)
    }

    def binLocationCsvHeader(def binLocation) {
        String csv = ""
        if (binLocation) {
            csv += g.message(code: 'default.status.label') + ","
            csv += g.message(code: 'product.productCode.label') + ","
            csv += g.message(code: 'product.label') + ","
            csv += g.message(code: 'product.productFamily.label') + ","
            csv += g.message(code: 'category.label') + ","
            csv += g.message(code: 'product.formulary.label') + ","
            csv += g.message(code: 'tag.label') + ","
            csv += g.message(code: 'inventoryItem.lotNumber.label') + ","
            csv += g.message(code: 'inventoryItem.expirationDate.label') + ","
            csv += g.message(code: 'location.zone.label') + ","
            csv += g.message(code: 'location.binLocation.label') + ","
            csv += g.message(code: 'default.quantity.label') + ","
            csv += g.message(code: 'default.quantityAvailableToPromise.label') + ","
            csv += g.message(code: 'product.unitCost.label') + ","
            csv += g.message(code: 'product.totalValue.label')
            csv += "\n"
        }
        return csv

    }

    def binLocationCsvRow(def binLocation) {
        String csv = ""

        if (binLocation) {
            String defaultBinLocation = g.message(code: 'default.label')
            String expirationDate = g.formatDate(date: binLocation?.inventoryItem?.expirationDate, format: "dd/MMM/yyyy")
            csv += binLocation.status + ","
            csv += StringEscapeUtils.escapeCsv(binLocation?.product?.productCode) + ","
            csv += StringEscapeUtils.escapeCsv(binLocation?.product?.displayNameWithLocaleCode) + ","
            csv += StringEscapeUtils.escapeCsv(binLocation?.product?.productFamily?.name ?: '') + ","
            csv += StringEscapeUtils.escapeCsv(binLocation?.product?.category?.name) + ","
            csv += StringEscapeUtils.escapeCsv(binLocation?.product?.productCatalogsToString()) + ","
            csv += StringEscapeUtils.escapeCsv(binLocation?.product?.tagsToString()) + ","
            csv += StringEscapeUtils.escapeCsv(binLocation?.inventoryItem?.lotNumber) + ","
            csv += StringEscapeUtils.escapeCsv(expirationDate) + ","
            csv += StringEscapeUtils.escapeCsv(binLocation?.binLocation?.zone?.name ?: '') + ","
            csv += StringEscapeUtils.escapeCsv(binLocation?.binLocation?.name ?: defaultBinLocation) + ","
            csv += binLocation.quantity + ","
            csv += binLocation.quantityAvailableToPromise + ","
            csv += binLocation.unitCost + ","
            csv += binLocation.totalValue
            csv += "\n"
        }
        return csv
    }


    def exportBinLocation() {
        long startTime = System.currentTimeMillis()
        log.info "Export by bin location " + params
        Boolean hasRoleFinance = userService.hasRoleFinance(AuthService.getCurrentUser())
        Location location = Location.get(session.warehouse.id)
        List<BinLocationItem> binLocations = inventoryService.getQuantityByBinLocation(location)
        int productCount = binLocations.groupBy { it.product?.productCode }.size()
        List<Map> binLocationsEntries = binLocations.collect {
            BigDecimal unitCost = hasRoleFinance ? (it.product?.pricePerUnit ?: 0.0) : null
            BigDecimal totalValue = hasRoleFinance ? (it.quantity * unitCost) : null
            Product product = Product.findByProductCode(it.product.productCode)
            [
                    productCode   : it.product.productCode,
                    productName   : product.displayNameWithLocaleCode,
                    lotNumber     : it.inventoryItem.lotNumber,
                    expirationDate: it.inventoryItem.expirationDate,
                    binLocation   : it?.binLocation?.name ?: "Default Bin",
                    quantity      : formatNumber(number: it.quantity),
                    unitCost      : hasRoleFinance ? formatNumber(number: unitCost) : null,
                    totalValue    : hasRoleFinance ? formatNumber(number: totalValue) : null,
            ]
        }

        if (params.downloadFormat == "csv") {
            String csv = ReportUtil.getCsvForListOfMapEntries(binLocationsEntries)
            def filename = "Bin Locations - ${location.name}.csv"
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
            render(contentType: "text/csv", text: csv)
            return
        }

        render([elapsedTime: (System.currentTimeMillis() - startTime), binLocationCount: binLocationsEntries.size(), productCount: productCount, binLocations: binLocationsEntries] as JSON)
    }

    def exportDemandReport() {
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

    def exportInventoryReport() {
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

    def showInventoryReport() {}

    def showInventorySamplingReport() {

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

    def showConsumptionReport() {

        def transactions = Transaction.findAllByTransactionDateBetween(new Date() - 10, new Date())

        [transactions: transactions]
    }

    def showTransactionReport() {
        InventoryReportCommand command = new InventoryReportCommand()
        command.location = Location.get(session.warehouse.id)
        command.rootCategory = productService.getRootCategory()

        def triggers = quartzScheduler.getTriggersOfJob(new JobKey("org.pih.warehouse.jobs.RefreshTransactionFactJob", GrailsJobClassConstants.DEFAULT_GROUP))
        def previousFireTime = triggers*.previousFireTime.max()
        def nextFireTime = triggers*.nextFireTime.max()
        def locationKey = LocationDimension.findByLocationId(command?.location?.id)
        def model = [
                command           : command,
                locationKey       : locationKey,
                transactionCount  : locationKey ? TransactionFact.countByLocationKey(locationKey) : 0,
                productCount      : TransactionFact.countDistinctProducts(locationKey?.locationId).get(),
                minTransactionDate: TransactionFact.minTransactionDate(locationKey?.locationId).get(),
                maxTransactionDate: TransactionFact.maxTransactionDate(locationKey?.locationId).get(),
                previousFireTime  : previousFireTime,
                nextFireTime      : nextFireTime,
        ]

        return model
    }

    def showTransactionReportDialog() {
        def url = createLink(controller: "json", action: "getTransactionReportDetails", params:params)
        render(template: "dataTableDialog", model: [url: url])
    }

    def showPaginatedPackingListReport(ChecklistReportCommand command) {
        command.rootCategory = productService.getRootCategory()
        if (!command?.hasErrors()) {
            reportService.generateShippingReport(command)
        }
        [command: command]
    }

    def printShippingReport(ChecklistReportCommand command) {
        command.rootCategory = productService.getRootCategory()
        if (!command?.hasErrors()) {
            reportService.generateShippingReport(command)
        }
        [command: command]
    }

    def printPickListReport(ChecklistReportCommand command) {

        Map binLocations
        if (!command?.hasErrors()) {
            reportService.generateShippingReport(command)
            binLocations = inventoryService.getBinLocations(command.shipment)
        }
        [command: command, binLocations: binLocations]
    }

    def printPaginatedPackingListReport(ChecklistReportCommand command) {
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

    def downloadTransactionReport() {
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

    def downloadShippingReport() {
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

    //@CacheFlush(["binLocationReportCache", "binLocationSummaryCache"])
    def clearBinLocationCache() {
        flash.message = "Cache have been flushed"
        redirect(action: "showBinLocationReport")
    }

    def showLostAndFoundReport() {
        String locationId = params?.location?.id ?: session?.warehouse?.id
        Location facility = Location.get(locationId)

        log.info "Generating Lost & Found Report for location ${locationId}"

        def lostAndFoundLocations = locationService.getLocationsSupportingActivity(ActivityCode.LOST_AND_FOUND)
        def locationIds = lostAndFoundLocations.findAll { it.parentLocation?.id == facility.id }*.id

        List<ProductAvailability> entries = []
        if (locationIds) {
            entries = ProductAvailability.executeQuery("""
                SELECT pa
                FROM ProductAvailability pa
                WHERE pa.location = :facility
                  AND pa.binLocation.id IN (:locationIds)
            """, [facility: facility, locationIds: locationIds])
        }

        log.info "Lost & Found report for facility ${facility?.id} returned ${entries?.size() ?: 0} entries"

        return [location: facility, entries: entries]
    }

    def showBinLocationReport() {

        log.info "showBinLocationReport " + params
        def startTime = System.currentTimeMillis()
        String locationId = params?.location?.id ?: session?.warehouse?.id
        Location location = Location.get(locationId)

        try {
            if (params.downloadAction == "downloadStockReport") {
                def binLocations = productAvailabilityService.getQuantityOnHandByBinLocation(location)

                // Filter on status
                if (params.status) {
                    binLocations = binLocations.findAll { it.status == params.status }
                }

                String csv = ReportUtil.getCsvForListOfMapEntries(binLocations, this.&binLocationCsvHeader, this.&binLocationCsvRow)
                def filename = "Bin Location Report - ${location?.name} - ${params.status ?: 'All'}.csv"
                response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
                render(contentType: "text/csv", text: csv)
                return
            }
            else if (params.downloadAction == "downloadStockMovement") {

                StockMovement stockMovement = new StockMovement()
                def entries = productAvailabilityService.getQuantityOnHandByBinLocation(location)
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
                statuses   : ["inStock", "outOfStock"]
        ]

    }

    def showOnOrderReport() {
        if (params.downloadAction == "downloadOnOrderReport") {
            def location = Location.get(session.warehouse.id)
            def items = orderService.getPendingInboundOrderItems(location)
            items += shipmentService.getPendingInboundShipmentItems(location)

            if (items) {

                def sw = new StringWriter()
                def csv = new CSVWriter(sw, {
                    "Code" { it.productCode }
                    "Product" { it.productName }
                    "Product family" { it.productFamily }
                    "Category" { it.category }
                    "Formulary" { it.productCatalogs }
                    "Quantity Ordered Not Shipped" { it.qtyOrderedNotShipped }
                    "Quantity Shipped Not Received" { it.qtyShippedNotReceived }
                    "PO Number" { it.orderNumber }
                    "Payment Terms" { it.paymentTerm }
                    "PO Description" { it.orderDescription }
                    "Supplier Organization" { it.supplierOrganization }
                    "Supplier Location" { it.supplierLocation }
                    "Supplier Location Group" { it.supplierLocationGroup }
                    "Estimated Goods Ready Date" { it.estimatedGoodsReadyDate }
                    "Shipment Number" { it.shipmentNumber }
                    "Ship Date" { it.shipDate }
                    "Expected Delivery Date" { it.expectedDeliveryDate }
                    "Shipment Type" { it.shipmentType }
                })

                items.sort { a,b ->
                    a.product?.productCode <=> b.product?.productCode
                }.each {
                    def isOrderItem = it instanceof OrderItem
                    csv << [
                            productCode  : it.product?.productCode,
                            productName  : it.product?.displayNameWithLocaleCode,
                            productFamily : it.product?.productFamily?.name ?: '',
                            category      : it.product?.category?.name ?: '',
                            productCatalogs      : it.product?.productCatalogs?.join(", "),
                            qtyOrderedNotShipped : isOrderItem ? it.quantityRemaining * it.quantityPerUom : '',
                            qtyShippedNotReceived : isOrderItem ? '' : it.quantityRemaining,
                            orderNumber  : isOrderItem ? it.order.orderNumber : (it.shipment.isFromPurchaseOrder ? it.orderNumber : ''),
                            paymentTerm  : isOrderItem ? (it.order.paymentTerm?.name ?: '') : (it.shipment.isFromPurchaseOrder ? (it?.paymentTerm ?: '') : ''),
                            orderDescription  : isOrderItem ? it.order.name : (it.shipment.isFromPurchaseOrder ? it.orderName : ''),
                            supplierOrganization  : isOrderItem ? it.order?.origin?.organization?.name : it.shipment?.origin?.organization?.name,
                            supplierLocation  : isOrderItem ? it.order.origin.name : it.shipment.origin.name,
                            supplierLocationGroup  : isOrderItem ? it.order?.origin?.locationGroup?.name : it.shipment?.origin?.locationGroup?.name,
                            estimatedGoodsReadyDate  : isOrderItem ? it.actualReadyDate?.format("MM/dd/yyyy") : '',
                            shipmentNumber  : isOrderItem ? '' : it.shipment.shipmentNumber,
                            shipDate  : isOrderItem ? '' : it.shipment.expectedShippingDate?.format("MM/dd/yyyy"),
                            expectedDeliveryDate  : isOrderItem ? '' : it.shipment.expectedDeliveryDate?.format("MM/dd/yyyy"),
                            shipmentType  : isOrderItem ? '' : it.shipment.shipmentType.name
                    ]
                }

                response.setHeader("Content-disposition", "attachment; filename=\"Detailed-Order-Report-${new Date().format("MM/dd/yyyy")}.csv\"")
                render(contentType: "text/csv", text: CSVUtils.prependBomToCsvString(sw.toString()), encoding: "UTF-8")
            }
        } else if(params.downloadAction == "downloadSummaryOnOrderReport") {
            def location = Location.get(session.warehouse.id)
            def data = reportService.getOnOrderSummary(location)
            if (data) {
                def sw = new StringWriter()
                def csv = new CSVWriter(sw, {
                    "Code" { it.productCode }
                    "Product" { it.displayNameWithLocaleCode }
                    "Product family" { it.productFamily }
                    "Category" { it.category }
                    "Formulary" { it.productCatalogs }
                    "Quantity Ordered Not Shipped" { it.qtyOrderedNotShipped }
                    "Quantity Shipped Not Received" { it.qtyShippedNotReceived }
                    "Total On Order" { it.totalOnOrder }
                    "Total On Hand" { it.totalOnHand }
                    "Total On Hand and On Order" { it.totalOnHandAndOnOrder }
                })

                data = data.sort { it.productCode }
                csv.writeAll(data)
                response.setHeader("Content-disposition", "attachment; filename=\"Detailed-Order-Report-${new Date().format("MM/dd/yyyy")}.csv\"")
                render(contentType: "text/csv", text: CSVUtils.prependBomToCsvString(sw.toString()), encoding: "UTF-8")
            }
        }
    }

    def showInventoryByLocationReport(MultiLocationInventoryReportCommand command) {

        if (!command.validate()) {
            render(view: 'showInventoryByLocationReport', model: [command: command])
            return
        }

        // Include subcategories by default. If user execute report and explicitly chooses
        // to exclude subcategories, then only use the given categories.
        if (command.includeSubcategories) {
            command.categories = inventoryService.getExplodedCategories(command.categories)
        }

        command.entries = productAvailabilityService.getQuantityOnHandByProduct(command.locations, command.categories)

        if (command.isActionDownload) {
            def sw = new StringWriter()

            try {
                if (command.entries) {
                    sw.append("Code").append(",")
                    sw.append("Product").append(",")
                    sw.append("Product Family").append(",")
                    sw.append("Category").append(",")
                    sw.append("Formularies").append(",")
                    sw.append("Tags").append(",")

                    command.locations?.each { location ->
                        String locationName = StringEscapeUtils.escapeCsv(location?.name)
                        sw.append(locationName).append(",")
                    }

                    sw.append("QoH Total").append(",")
                    sw.append("Quantity Available Total")
                    sw.append("\n")

                    command.entries.each { entry ->

                        if (entry.key) {
                            def totalQuantity = entry.value?.values()?.quantityOnHand?.sum()
                            def totalQuantityAvailableToPromise = entry.value?.values()?.quantityAvailableToPromise?.sum()
                            def form = entry.key?.getProductCatalogs()?.collect {
                                it.name
                            }?.join(",")

                            sw.append('"' + (entry.key?.productCode ?: "").toString()?.replace('"', '""') + '"').append(",")
                            sw.append('"' + (entry.key?.displayNameWithLocaleCode ?: "").toString()?.replace('"', '""') + '"').append(",")
                            sw.append('"' + (entry.key?.productFamily?.name ?: "").toString()?.replace('"', '""') + '"').append(",")
                            sw.append('"' + (entry.key?.category?.getHierarchyAsString(" > ") ?: "").toString()?.replace('"', '""') + '"').append(",")
                            sw.append('"' + (form ?: "").toString()?.replace('"', '""') + '"').append(",")
                            sw.append('"' + (entry.key?.tagsToString() ?: "")?.toString()?.replace('"', '""') + '"').append(",")

                            command.locations?.each { location ->
                                sw.append('"' + (entry.value[location?.id] != null ? entry.value[location?.id]?.quantityOnHand?:0 : "").toString() + '"').append(",")
                            }

                            sw.append('"' + (totalQuantity != null ? totalQuantity : "").toString() + '"').append(",")
                            sw.append('"' + (totalQuantityAvailableToPromise != null ? totalQuantityAvailableToPromise : "").toString() + '"')
                            sw.append("\n")
                        }
                    }
                }

            } catch (RuntimeException e) {
                log.error("Unexpected error occurred while generating report ", e.message)
                sw.append(e.message)
            }
            response.setHeader("Content-disposition", "attachment; filename=\"Inventory-by-location-${new Date().format("yyyyMMdd-hhmmss")}.csv\"")
            render(contentType: "text/csv", text: CSVUtils.prependBomToCsvString(sw.toString()), encoding: "UTF-8")
            return
        }

        render(view: 'showInventoryByLocationReport', model: [command: command])
    }

    def showRequestDetailReport() {
        def origin = Location.get(session.warehouse.id)
        params.origin = origin.id
        render(view: 'showRequestDetailReport', params: params)
    }

    def showCycleCountReport() {
        Location location = Location.load(session.warehouse.id)
        List binLocations = inventoryService.getQuantityByBinLocation(location)
        log.info "Returned ${binLocations.size()} bin locations for location ${location}"

        List rows = binLocations.collect { row ->
            // Required in order to avoid lazy initialization exception that occurs because all
            // of the querying / session work that was done above was executed in worker threads
            Product product = Product.load(row?.product?.id)

            def latestInventoryDate = row?.product?.latestInventoryDate(location.id) ?: row?.product.earliestReceivingDate(location.id)
            Map dataRow = params.print ? [
                            "Product code"        : StringEscapeUtils.escapeCsv(row?.product?.productCode),
                            "Product name"        : product.displayNameWithLocaleCode,
                            "Lot number"          : StringEscapeUtils.escapeCsv(row?.inventoryItem.lotNumber ?: ""),
                            "Expiration date"     : row?.inventoryItem.expirationDate ? row?.inventoryItem.expirationDate.format(Constants.EXPIRATION_DATE_FORMAT) : "",
                            "Bin location"        : StringEscapeUtils.escapeCsv(row?.binLocation?.name ?: ""),
                            "OB QOH"              : row?.quantity ?: 0,
                            "Physical QOH"        : "",
                            "Comment"             : "",
                            "Product family"      : product?.productFamily ?: "",
                            "Category"            : StringEscapeUtils.escapeCsv(row?.category?.name ?: ""),
                            "Formularies"         : product.productCatalogs.join(", ") ?: "",
                            "ABC Classification"  : StringEscapeUtils.escapeCsv(row?.product.getAbcClassification(location.id) ?: ""),
                            "Status"              : g.message(code: "binLocationSummary.${row?.status}.label"),
                            "Last Inventory Date" : latestInventoryDate ? latestInventoryDate.format(Constants.EXPIRATION_DATE_FORMAT) : "",
                    ] : [
                            productCode       : StringEscapeUtils.escapeCsv(row?.product?.productCode),
                            productName       : row?.product.name ?: "",
                            productFamily     : product?.productFamily ?: "",
                            category          : StringEscapeUtils.escapeCsv(row?.category?.name ?: ""),
                            formularies       : product.productCatalogs.join(", ") ?: "",
                            lotNumber         : StringEscapeUtils.escapeCsv(row?.inventoryItem.lotNumber ?: ""),
                            expirationDate    : row?.inventoryItem.expirationDate ? row?.inventoryItem.expirationDate.format(Constants.EXPIRATION_DATE_FORMAT) : "",
                            abcClassification : StringEscapeUtils.escapeCsv(row?.product.getAbcClassification(location.id) ?: ""),
                            binLocation       : StringEscapeUtils.escapeCsv(row?.binLocation?.name ?: ""),
                            status            : g.message(code: "binLocationSummary.${row?.status}.label"),
                            lastInventoryDate : latestInventoryDate ? latestInventoryDate.format(Constants.EXPIRATION_DATE_FORMAT) : "",
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

    def showForecastReport() {
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
                        'Name'                            : product.displayNameWithLocaleCode,
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

    def amountOutstandingOnOrdersReport() {
        def hasRoleFinance = userService.hasRoleFinance(session?.user)
        if (!hasRoleFinance) {
            flash.message = "You do not have permission to view financial data"
            return
        }

        def rows = reportService.getAmountOutstandingOnOrders(session?.warehouse?.id)

        def filename = "AmountOutstandingOnOrders-${session?.warehouse?.name}-${new Date().format("dd-MMM-yyyy-hh:mm")}"
        response.contentType = "application/vnd.ms-excel"
        response.setHeader("Content-disposition", "attachment; filename=\"${filename}.xls\"")
        documentService.generateExcel(response.outputStream, rows)
        response.outputStream.flush()
    }
}
