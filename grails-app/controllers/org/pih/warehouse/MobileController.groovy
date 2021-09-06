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

import grails.orm.PagedResultList
import org.apache.commons.io.IOUtils
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.api.StockMovementType
import org.pih.warehouse.core.Document
import org.pih.warehouse.core.Event
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.importer.InboundStockMovementExcelImporter
import org.pih.warehouse.importer.OutboundStockMovementExcelImporter
import org.pih.warehouse.integration.AcceptanceStatusEvent
import org.pih.warehouse.integration.DocumentUploadEvent
import org.pih.warehouse.integration.TripExecutionEvent
import org.pih.warehouse.integration.TripNotificationEvent
import org.pih.warehouse.integration.xml.trip.Trip
import org.pih.warehouse.inventory.StockMovementStatusCode
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductSummary
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.integration.xml.acceptancestatus.AcceptanceStatus
import org.pih.warehouse.integration.xml.pod.DocumentUpload
import org.pih.warehouse.integration.xml.execution.Execution
import org.pih.warehouse.shipping.ShipmentStatusCode
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest

import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller
import javax.xml.bind.Unmarshaller

class MobileController {

    def userService
    def productService
    def inventoryService
    def locationService
    def megamenuService
    def stockMovementService
    def fileTransferService
    def grailsApplication
    def uploadService
    def tmsIntegrationService

    def index = {

        Location location = Location.get(session.warehouse.id)
        def productCount = ProductSummary.countByLocation(location)
        def productListUrl = g.createLink(controller: "mobile", action: "productList")

        StockMovement stockMovement = new StockMovement(destination: location,
                stockMovementType: StockMovementType.INBOUND, stockMovementStatusCode: StockMovementStatusCode.PENDING)

        def orderCount = stockMovementService.getStockMovements(stockMovement, [max:params.max?:10, offset: params.offset?:0]).size()

        /*def orderCount = Shipment.createCriteria().count {
            eq("destination", location)
        }*/

        def requisitionCount = Requisition.createCriteria().count {
            eq("origin", location)
        }

        def messages = fileTransferService.listMessages()
        def messageCount = messages ? messages?.size() :0

        def readyToBePicked = stockMovement.findAll{ it.stockMovementStatusCode < StockMovementStatusCode.PICKED }
        def readyToBePickedCount = readyToBePicked.size()
        def status =  'READY_TO_BE_PICKED'
        [
                data: [
                        [name: "Inventory", class: "fa fa-box", count: productCount, url: g.createLink(controller: "mobile", action: "productList")],
                        [name: "Inbound Orders", class: "fa fa-shopping-cart", count: orderCount, url: g.createLink(controller: "mobile", action: "inboundList", params: ['origin.id', location.id])],
                        [name: "Outbound Orders", class: "fa fa-truck", count: requisitionCount, url: g.createLink(controller: "mobile", action: "outboundList", params: ['origin.id', location.id])],
                        [name: "Messages", class: "fa fa-envelope", count: messageCount, url: g.createLink(controller: "mobile", action: "messageList", params: ['origin.id', location.id])],
                        [name: "Ready to be Picked", class: "fa fa-truck-pickup", count: readyToBePickedCount, url: g.createLink(controller: "mobile", action: "outboundList", params: [status: status])],
                ]
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
        Location destination = Location.get(params.origin?params.origin.id:session.warehouse.id)
        StockMovement stockMovement = new StockMovement(destination: destination,
                stockMovementType: StockMovementType.INBOUND, stockMovementStatusCode: StockMovementStatusCode.PENDING)
        if(params.status == "IN_TRANSIT") {
            params.receiptStatusCode = ["SHIPPED"]
            stockMovement = new StockMovement(
                    destination: destination,
                    stockMovementType: StockMovementType.INBOUND,
                    receiptStatusCodes: params.list("receiptStatusCode") as ShipmentStatusCode[]
            )
        }

        def stockMovements = stockMovementService.getStockMovements(stockMovement, [max:params.max?:10, offset: params.offset?:0])

        if ( params.status == "READY_TO_BE_PICKED") {
            def tempStockMovements = stockMovements.findAll{ it.stockMovementStatusCode < StockMovementStatusCode.PICKED }
            stockMovements = new PagedResultList(tempStockMovements, tempStockMovements.size())
        }
        [stockMovements: stockMovements]
    }

    def outboundList = {
        log.info "outboundList params ${params}"
        Location origin = Location.get(params.origin?params.origin.id:session.warehouse.id)
        StockMovement stockMovement = new StockMovement(origin: origin, stockMovementType: StockMovementType.OUTBOUND, stockMovementStatusCode: StockMovementStatusCode.PENDING)
        if(params.status == "IN_TRANSIT") {
            params.receiptStatusCode = ["SHIPPED"]
            stockMovement = new StockMovement(
                    origin: origin,
                    stockMovementType: StockMovementType.OUTBOUND,
                    receiptStatusCodes: params.list("receiptStatusCode") as ShipmentStatusCode[]
            )
        }
        def stockMovements = stockMovementService.getStockMovements(stockMovement, [max:params.max?:10, offset: params.offset?:0])

        if(params.status == "READY_TO_BE_PICKED") {
            def tempStockMovements = stockMovements.findAll{ it.stockMovementStatusCode < StockMovementStatusCode.PICKED }
            stockMovements = new PagedResultList(tempStockMovements, tempStockMovements.size())
        }

        [stockMovements:stockMovements]
    }

    def importData = { ImportDataCommand command ->

        log.info params
        log.info command.location
        log.info session
        File localFile = null
        if (request instanceof DefaultMultipartHttpServletRequest) {
            def uploadFile = request.getFile("xlsFile")
            if (!uploadFile.empty) {
                try {
                    localFile = uploadService.createLocalFile(uploadFile.originalFilename)
                    uploadFile.transferTo(localFile)
                    session.localFile = localFile
                } catch (Exception e) {
                    flash.message = "Unable to upload file due to exception: " + e.message
                    return
                }
            } else {
                flash.message = "${warehouse.message(code: 'inventoryItem.emptyFile.message')}"
            }
        } else {
            localFile = session.localFile
        }
        def dataImporter
        if (localFile) {
            command.importFile = localFile
            command.filename = localFile.getAbsolutePath()
            command.location = Location.get(session.warehouse.id)

            if (params.type == "outbound")
                dataImporter = new OutboundStockMovementExcelImporter(command.filename)
            else
                dataImporter = new InboundStockMovementExcelImporter(command.filename)

            if(dataImporter) {
                println "Using data importer ${dataImporter.class.name}"
                command.data = dataImporter.data
                dataImporter.validateData(command)
                command.columnMap = dataImporter.columnMap
            }

            if (command?.data?.isEmpty()) {
                command.errors.reject("importFile", "${warehouse.message(code: 'inventoryItem.pleaseEnsureDate.message', args: [dataImporter.columnMap?.sheet?:'Sheet1', localFile.getAbsolutePath()])}")
            }

            if (!command.hasErrors() ) {
                println "data is about to be imported ..."
                try {
                    dataImporter.importData(command)
                }catch (Exception e) {
                    command.errors.reject(e.message)
                }
                if (!command.errors.hasErrors()) {
                    flash.message = "${warehouse.message(code: 'inventoryItem.importSuccess.message', args: [localFile.getAbsolutePath()])}"
                    redirect(action: "outboundList")
                    return
                }
                log.info "There were errors: " + command.errors
            }else {
                log.info "There are some errors ${command.errors}"
                flash.message = "There are some validation errors"
            }
        }
        redirect action: 'outboundList'
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

    def outboundDetails = {
        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)

        // Move to service layer
        def events = stockMovement?.shipment?.events?.collect { Event event ->
            [id: event?.id, name: event?.eventType?.toString(), date: event?.eventDate]
        } ?: []
        events << [name: "Order created", date: stockMovement?.requisition?.dateCreated]
        if (stockMovement?.requisition?.dateCreated != stockMovement?.requisition?.lastUpdated) {
            events << [name: "Order updated", date: stockMovement?.requisition?.lastUpdated]
        }
        events = events.sort { it.date }

        // Generate QR Code link for stock movement
        def qrCodeLink = "${createLink(controller: "mobile", action: "outboundDetails", id: stockMovement.id, absolute: true)}"

        [stockMovement:stockMovement, qrCodeLink: qrCodeLink, events:events]
    }

    def outboundDownload = {

        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)
        Object deliveryOrder = tmsIntegrationService.createDeliveryOrder(stockMovement)

        JAXBContext jaxbContext = JAXBContext.newInstance(org.pih.warehouse.integration.xml.order.Order.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(deliveryOrder, response.outputStream);
        response.setHeader "Content-disposition", "attachment;filename=\"CreateDeliveryOrder-${stockMovement?.identifier}.xml\""
        response.contentType = "application/xml"
        response.outputStream.flush()
    }

    def outboundDelete = {
        stockMovementService.deleteStockMovement(params.id)
        redirect(action: "outboundList")
    }

    def messageList = {
        def messages = fileTransferService.listMessages()
        [messages:messages]
    }

    def messageDetails = {
        def content = fileTransferService.retrieveMessage(params.filename)
        //def xml = new XML(content)
        render text: content, contentType: 'text/xml', encoding: 'UTF-8'
        return
    }

    def messageUpload = {
        def messageFile = request.getFile('messageFile')
        if(messageFile) {
            String fileName = messageFile.originalFilename
            File file = new File ("/tmp/${fileName}")
            messageFile.transferTo(file)
            try {
                fileTransferService.storeMessage(file)
                flash.message = "File ${fileName} transferred successfully"

            } catch (Exception e) {
                log.error "Unable to upload message due to error: " + e.message, e
                flash.message = "File ${fileName} not transferred due to error: " + e.message

            }
        }
        redirect(action: "messageList")
    }

    def messageProcess = {

        // Retrive XML file
        String xmlContents = fileTransferService.retrieveMessage(params.filename)

        // Convert XML message to message object
        JAXBContext jaxbContext = JAXBContext.newInstance("org.pih.warehouse.integration.xml.acceptancestatus:org.pih.warehouse.integration.xml.execution:org.pih.warehouse.integration.xml.pod:org.pih.warehouse.integration.xml.trip");
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        InputStream inputStream = IOUtils.toInputStream(xmlContents)
        Object messageObject = unmarshaller.unmarshal(inputStream)

        // Publish message to event bus
        if (messageObject instanceof DocumentUpload) {
            grailsApplication.mainContext.publishEvent(new DocumentUploadEvent(messageObject))
        }
        else if (messageObject instanceof AcceptanceStatus) {
            grailsApplication.mainContext.publishEvent(new AcceptanceStatusEvent(messageObject))
        }
        else if (messageObject instanceof Execution) {
            grailsApplication.mainContext.publishEvent(new TripExecutionEvent(messageObject))
        }
        else if (messageObject instanceof Trip) {
            grailsApplication.mainContext.publishEvent(new TripNotificationEvent(messageObject))
        }

        flash.message = "Message has been processed"
        redirect(action: "messageList")

    }

    def documentDownload = {
        Document document = Document.get(params.id)
        response.contentType = document.contentType
        response.outputStream << document.fileContents
        response.outputStream.flush()
    }
}
