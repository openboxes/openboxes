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

import net.schmizz.sshj.sftp.SFTPException
import org.apache.commons.net.util.Base64
import org.apache.poi.poifs.filesystem.OfficeXmlFileException
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.api.StockMovementItem
import org.pih.warehouse.api.StockMovementType
import org.pih.warehouse.core.Document
import org.pih.warehouse.core.Event
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.importer.InboundStockMovementExcelImporter
import org.pih.warehouse.importer.OutboundStockMovementExcelImporter
import org.pih.warehouse.inventory.StockMovementStatusCode
import org.pih.warehouse.jobs.UploadDeliveryOrdersJob
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductSummary
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.shipping.ShipmentStatusCode
import org.pih.warehouse.util.DateUtil
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest


class MobileController {

    def userService
    def productService
    def inventoryService
    def locationService
    def megamenuService
    def documentService
    def stockMovementService
    def fileTransferService
    def grailsApplication
    def uploadService
    def tmsIntegrationService
    def messageHandlerService
    def xsdValidatorService
    def productAvailabilityService


    def index = {

        Location location = Location.get(session.warehouse.id)
        def productCount = ProductSummary.countByLocation(location)
        def productListUrl = g.createLink(controller: "mobile", action: "productList")

        StockMovement inboundCriteria = new StockMovement(destination: location, stockMovementType: StockMovementType.INBOUND)
        def inboundOrders = stockMovementService.getStockMovements(inboundCriteria, [max:params.max?:10, offset: params.offset?:0])
        def inboundCount = inboundOrders.size()?:0
        def inboundPending = inboundOrders.findAll { !it.isReceived }

        StockMovement outboundCriteria = new StockMovement(origin: location, stockMovementType: StockMovementType.OUTBOUND)
        def outboundOrders = stockMovementService.getStockMovements(outboundCriteria, [max:params.max?:10, offset: params.offset?:0])
        def outboundCount = outboundOrders.size()?:0

        def outboundPending = outboundOrders.findAll { it.stockMovementStatusCode < StockMovementStatusCode.DISPATCHED }
        def readyToBePicked = outboundOrders.findAll{ it.stockMovementStatusCode == StockMovementStatusCode.PICKING }
        def readyToBePacked = outboundOrders.findAll{ it.stockMovementStatusCode == StockMovementStatusCode.PICKED }
        def inTransit = outboundOrders.findAll{ it.isShipped && !it.isReceived }

        def inventorySummary = ProductSummary.createCriteria().list(max: params.max ?: 10, offset: params.offset ?: 0) {
            eq("location", location)
            order("product", "asc")
        }


        [
                indicators: [
                        [name: "Inventory Items", class: "fa fa-box", count: productCount, url: g.createLink(controller: "mobile", action: "productList")],
                        [name: "Inbound Orders", class: "fa fa-shopping-cart", count: inboundCount, url: g.createLink(controller: "mobile", action: "inboundList", params: ['origin.id', location.id])],
                        [name: "Outbound Orders", class: "fa fa-truck", count: outboundCount, url: g.createLink(controller: "mobile", action: "outboundList", params: ['origin.id', location.id])],
                        [name: "Ready to be picked", class: "fa fa-cart-plus", count: readyToBePicked?.size()?:0, url: g.createLink(controller: "mobile", action: "outboundList", params: [status: RequisitionStatus.PICKING])],
                        [name: "Ready to be packed", class: "fa fa-box-open", count: readyToBePacked?.size()?:0, url: g.createLink(controller: "mobile", action: "outboundList", params: [status: RequisitionStatus.PICKED])]
                ],
                inboundPending: inboundPending,
                outboundPending: outboundPending,
                readyToBePicked: readyToBePicked,
                inTransit: inTransit,
                inventorySummary: inventorySummary,
        ]
    }

    def login = {

    }

    def menu = {
        Map menuConfig = grailsApplication.config.openboxes.megamenu
        //User user = User.get(session?.user?.id)
        //Location location = Location.get(session.warehouse?.id)
        //List translatedMenu = megamenuService.buildAndTranslateMenu(menuConfig, user, location)
        [menuConfig:menuConfig]
    }

    def chooseLocation = {
        User user = User.get(session.user.id)
        Location warehouse = Location.get(session.warehouse.id)
        render (view: "/mobile/chooseLocation",
                model: [savedLocations: user.warehouse ? [user.warehouse] : null, loginLocationsMap: locationService.getLoginLocationsMap(user, warehouse)])
    }

    def productList = {
        Location location = Location.get(session.warehouse.id)
        def terms = params?.q ? params?.q?.split(" ") : "".split(" ")
        def productSummaries = ProductSummary.createCriteria().list(max: params.max ?: 10, offset: params.offset ?: 0) {
            eq("location", location)
            order("product", "asc")
        }
        [productSummaries:productSummaries]
    }

    def productDetails = {
        Product product = Product.findByIdOrProductCode(params.id, params.id)
        Location location = Location.get(session.warehouse.id)
        def productSummary = ProductSummary.findByProductAndLocation(product, location)
        if (productSummary) {
            [productSummary: productSummary]
        }
        else {
            flash.message = "Product ${product.productCode} is not available in ${location.locationNumber}"
            redirect(action: "productList")
        }
    }

    def inboundList = {
        Location destination = Location.get(session.warehouse.id)
        Location origin = params.origin?Location.get(params.origin.id):null

        StockMovement stockMovement = new StockMovement(origin: origin, destination: destination, stockMovementType: StockMovementType.INBOUND)
        if (params.status) {
            stockMovement.receiptStatusCodes = [params.status as ShipmentStatusCode]
        }
        def stockMovements = stockMovementService.getStockMovements(stockMovement, [max:params.max?:10, offset: params.offset?:0])
        [stockMovements: stockMovements]
    }

    def outboundList = {
        log.info "outboundList params ${params}"
        Location origin = Location.get(params.origin?params.origin.id:session.warehouse.id)

        RequisitionStatus requisitionStatus = params.status ? params.status as RequisitionStatus : null
        StockMovement stockMovement = new StockMovement(
                origin: origin,
                stockMovementType: StockMovementType.OUTBOUND)

        params.max = params.max?:10
        params.offset = params.offset?:0

        if (params.identifier) {
           stockMovement.identifier = "%" + params.identifier + "%"
        }

        if (params.trackingNumber) {
            stockMovement.trackingNumber = "%" + params.trackingNumber + "%"
        }

        if (params.status) {
            RequisitionStatus requisitionStatusCode = params.status as RequisitionStatus
            stockMovement.stockMovementStatusCode = RequisitionStatus.toStockMovementStatus(requisitionStatusCode)
        }

        if (params.destination) {
            Location destination = Location.get(params.destination.id)
            stockMovement.destination = destination
        }

        if (params.deliveryDate) {
            def deliveryDate = Date.parse("yyyy-MM-dd", params.deliveryDate)
            stockMovement.expectedDeliveryDate = deliveryDate
        }

        if (params.requestedDeliveryDateFilter) {
            params.requestedDeliveryDates = DateUtil.parseDateRange(params.requestedDeliveryDateFilter, "dd/MMM/yyyy", " - ")
        }

        def stockMovements = stockMovementService.getStockMovements(stockMovement, params)

        [stockMovements:stockMovements]
    }

    def exportData = {

        try {
            StockMovement stockMovement = stockMovementService.getStockMovement(params.id, false)

            // If there are no lines then we should export a template
            if (!stockMovement.lineItems) {
                stockMovement.lineItems = [new StockMovementItem()]
            }

            def data = stockMovement.lineItems.collect { StockMovementItem stockMovementItem ->
                return [
                        "Origin"              : stockMovement?.origin?.locationNumber,
                        "Dest Venue Code"     : stockMovement?.destination?.locationNumber,
                        "Item Description"    : stockMovementItem.product?.name,
                        "SKU Code"            : stockMovementItem.product?.productCode,
                        "Requested Quantity"  : stockMovementItem.quantityRequested,
                        "Pallet Quantity"     : "",
                        "Pallet Spaces"       : "",
                        "Delivery Date"       : stockMovement?.requestedDeliveryDate,
                        "Load Code"           : stockMovement?.identifier,
                        "Special Instructions": stockMovementItem.requisitionItem?.description,
                ]
            }

            response.contentType = "application/vnd.ms-excel"
            response.setHeader 'Content-disposition', "attachment; filename=\"${stockMovement?.identifier}.xls\""
            documentService.generateExcel(response.outputStream, data)
            response.outputStream.flush()
            return
        } catch (Exception e) {
            flash.message = e.message
        }
        redirect(action: "outboundDetails", id: params.id)
    }

    def importData = { ImportDataCommand command ->
        def dataImporter
        if (request instanceof DefaultMultipartHttpServletRequest) {

            List<MultipartFile> xlsFiles = request.getFiles("xlsFile[]")
            if (xlsFiles) {

                try {
                    xlsFiles.each { xlsFile ->

                        command.importFile = xlsFile
                        command.filename = xlsFile.name
                        command.location = Location.get(session.warehouse.id)

                        dataImporter = (params.type == "outbound") ?
                                new OutboundStockMovementExcelImporter(command.filename, xlsFile.inputStream) :
                                new InboundStockMovementExcelImporter(command.filename, xlsFile.inputStream)

                        if (dataImporter) {
                            log.info "Using data importer ${dataImporter.class.name}"
                            command.data = dataImporter.data
                            dataImporter.validateData(command)
                            command.columnMap = dataImporter.columnMap

                            if (command?.data?.isEmpty()) {
                                command.errors.reject("importFile", "${warehouse.message(code: 'inventoryItem.pleaseEnsureDate.message', args: [dataImporter.columnMap?.sheet ?: 'Sheet1', localFile.getAbsolutePath()])}")
                            }

                            if (!command.hasErrors()) {
                                log.info "${command.data.size()} rows of data is about to be imported ..."
                                dataImporter.importData(command)
                                if (!command.hasErrors()) {
                                    flash.message = "${warehouse.message(code: 'inventoryItem.importSuccess.message', args: [xlsFile?.originalFilename])}"
                                }
                            } else {
                                flash.command = command
                            }
                        }
                    }
                } catch (OfficeXmlFileException e) {
                    log.error("An exception occurred while loading Excel file: " + e.message, e)
                    flash.message = "Detected invalid Excel .xlsx format - please import .xls files instead"
                } catch (Exception e) {
                    log.error("An exception occurred while importing data: " + e.message, e)
                    flash.message = "An error occurred while importing data: " + e.message
                }
            }
        }

        if (params.redirectUrl) {
            redirect(url: params.redirectUrl)
        }
        else {
            redirect (action: (params.type == "outbound" ? 'outboundList' : 'inboundList'))
        }

    }

    def inboundDetails = {
        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)

        def events = stockMovement?.shipment?.events?.collect { Event event ->
            [id: event?.id, name: event?.eventType?.toString(), date: event?.eventDate]
        } ?: []

        events << [name: "Order created", date: stockMovement?.dateCreated]
        if (stockMovement?.dateCreated != stockMovement?.lastUpdated) {
            events << [name: "Order updated", date: stockMovement?.lastUpdated]
        }

        events = events.sort { it.date }

        [stockMovement:stockMovement, events:events]
    }

    def inboundDelete = {
        stockMovementService.deleteStockMovement(params.id)
        redirect(action: "inboundList")
    }


    def outboundDetails = {
        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)

        // Get the on hand quantity for all products in the given stock movement
        Location location = Location.get(session.warehouse.id)
        List<Product> products = stockMovement.lineItems.collect { it.product }
        Map<Product, Integer> quantityOnHand = [:]
        if (!products?.empty) {
            Map tempQuantityOnHand = productAvailabilityService.getQuantityOnHandByProduct(location, products)
            // FIXME For some reason we cannot retrieve value from map using product object
            tempQuantityOnHand.each { product, value ->
                quantityOnHand.put(product.id, value)
            }
        }
        // Move to service layer
        def events = stockMovement?.shipment?.events?.collect { Event event ->
            [id: event?.id, name: event?.eventType?.toString(), date: event?.eventDate]
        } ?: []
        events << [name: "Order created", date: stockMovement?.requisition?.dateCreated]
        if (stockMovement?.requisition?.dateCreated != stockMovement?.requisition?.lastUpdated) {
            events << [name: "Order updated", date: stockMovement?.requisition?.lastUpdated]
        }
        events = events.sort { it.date }

        [stockMovement:stockMovement, events:events, quantityOnHand:quantityOnHand]
    }

    def outboundDownload = {
        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)
        Object deliveryOrder = tmsIntegrationService.createDeliveryOrder(stockMovement)
        String serializedOrder = tmsIntegrationService.serialize(deliveryOrder, org.pih.warehouse.integration.xml.order.Order.class)
        response.outputStream << serializedOrder

        response.setHeader "Content-disposition", "attachment;filename=\"CreateDeliveryOrder-${stockMovement?.identifier}.xml\""
        response.contentType = "application/xml"
        response.outputStream.flush()
    }

    def outboundUpload = {
        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)
        tmsIntegrationService.uploadDeliveryOrder(stockMovement)
        flash.message = "Message uploaded successfully"
        redirect(action: "outboundDetails", id: params.id)
    }

    def outboundDelete = {
        stockMovementService.deleteStockMovement(params.id)
        redirect(action: "outboundList")
    }

    def messageList = {
        String directory = grailsApplication.config.openboxes.integration.ftp.inbound.directory
        List<String> subdirectories = grailsApplication.config.openboxes.integration.ftp.inbound.subdirectories
        log.info "subdirectories: " + subdirectories
        def messages = fileTransferService.listMessages(directory, subdirectories)

        [messages:messages]
    }

    def messageListProcess = {
        tmsIntegrationService.handleMessages()

        redirect(action: "messageList")
    }

    def messageDetails = {
        def content = fileTransferService.retrieveMessage(params.path)
        if (params.path.endsWith(".xml")) {
            render text: content, contentType: 'text/xml', encoding: 'UTF-8'
        }
        else if (params.path.endsWith(".log")) {
            render text: "<pre>" + content + "</pre>"
        }
        else {
            render text: content
        }
        return
    }

    def messageDelete = {
        fileTransferService.deleteMessage(params.path)
        flash.message = "Message ${params.path} deleted successfully"
        redirect(action: "messageList")
    }

    def messageUpload = {
        def messageFile = request.getFile('messageFile')
        if(messageFile) {
            String fileName = messageFile.originalFilename
            File file = new File ("/tmp/${fileName}")
            messageFile.transferTo(file)
            try {
                String directory = grailsApplication.config.openboxes.integration.ftp.inbound.directory
                String subdirectory = params.subdirectory
                String destination = "${directory}/${subdirectory}/${file.name}"
                fileTransferService.storeMessage(file, destination)
                flash.message = "File ${fileName} transferred successfully"

            } catch (SFTPException e) {
                log.error "Unable to upload message due to error ${e.statusCode}: " + e.message, e
                flash.message = "File ${fileName} not transferred due to error: " + e.message

            }
        }
        redirect(action: "messageList")
    }

    def messageValidate = {
        try {
            String xmlContents = fileTransferService.retrieveMessage(params.path)
            tmsIntegrationService.validateMessage(xmlContents, Boolean.TRUE)
            flash.message = "Message has been validated"
        }
        catch (Exception e) {
            log.error("Message not validated due to error: " + e.message, e)
            flash.message = "Message not validated due to error: " + e.message?:e?.cause?.message
        }
        redirect(action: "messageList")
    }

    def messageProcess = {
        try {
            String xmlContents = fileTransferService.retrieveMessage(params.path)
            tmsIntegrationService.validateMessage(xmlContents)
            Object messageObject = tmsIntegrationService.deserialize(xmlContents)
            tmsIntegrationService.handleMessage(messageObject)
            flash.message = "Message ${params.path} has been processed successfully"

            Boolean archiveOnSuccess = grailsApplication.config.openboxes.integration.ftp.archiveOnSuccess
            if (archiveOnSuccess) {
                tmsIntegrationService.archiveMessage(params.path)
            }
        }
        catch (Exception e) {
            log.error("Message was not processed due to error: " + e.message, e)
            flash.message = "Message ${params.path} was not processed due to error: " + e.message
            tmsIntegrationService.failMessage(params.path, e)
        }
        redirect(action: "messageList")
    }

    def messageArchive = {
        try {
            tmsIntegrationService.archiveMessage(params.path)
            flash.message = "Message ${params.path} has been archived successfully"
        }
        catch (Exception e) {
            log.error("Message was not processed due to error: " + e.message, e)
            flash.message = "Message ${params.path} was not processed due to error: " + e.message
            tmsIntegrationService.failMessage(params.path, e)
        }
        redirect(action: "messageList")
    }


    def uploadDeliveryOrders = {
        log.info "upload delivery orders " + params
        UploadDeliveryOrdersJob.triggerNow([requestedDeliveryDate:params.requestedDeliveryDate])
        flash.message = "Successfully triggered upload delivery orders job"
        redirect(action: "outboundList")
    }


    def documentDownload = {
        Document document = Document.get(params.id)

        byte[] fileContents = document.fileContents
        if (Base64.isArrayByteBase64(fileContents)) {
            InputStream inputStream = new ByteArrayInputStream(fileContents)
            String contentType = URLConnection.guessContentTypeFromStream(inputStream)
            if (!contentType) {
                contentType = URLConnection.guessContentTypeFromName(document.name)
            }
            fileContents = Base64.decodeBase64(fileContents)

            response.setHeader "Content-disposition", "attachment;filename=\"${document.name}\""
            response.contentType = contentType
            response.outputStream << fileContents
            response.outputStream.flush()
            return;
        }
        response.setHeader "Content-disposition", "attachment;filename=\"${document.name}\""
        response.contentType = document.contentType
        response.outputStream << document.fileContents
        response.outputStream.flush()
    }
}
