/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.shipping

import au.com.bytecode.opencsv.CSVWriter
import grails.validation.ValidationException
import groovy.sql.Sql
import org.krysalis.barcode4j.impl.code128.Code128Bean
import org.pih.warehouse.core.*
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionException
import org.pih.warehouse.product.Product
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.receiving.ReceiptItem

class ShipmentController {

    def scaffold = Shipment
    def shipmentService
    def userService
    def reportService
    def inventoryService
    MailService mailService

    def barcode4jService

    def dataSource
    def sessionFactory


    def redirect = {
        redirect(controller: "shipment", action: "showDetails", id: params.id)
    }

    def show = {
        redirect(action: "showDetails", params: ['id': params.id])
    }

    def list = {
        def startTime = System.currentTimeMillis()
        println "Get shipments: " + params

        params.max = Math.min(params.max ? params.int('max') : 100, 10000)

        Calendar calendar = Calendar.instance
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        int firstDayOfMonth = calendar.getActualMinimum(Calendar.DAY_OF_MONTH)
        int lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), firstDayOfMonth)
        def lastUpdatedFromDefault = calendar.getTime()
        def lastUpdatedToDefault = calendar.getTime()

        boolean incoming = params?.type?.toUpperCase() == "INCOMING"
        def origin = incoming ? (params.origin ? Location.get(params.origin) : null) : Location.get(session.warehouse.id)
        def destination = incoming ? Location.get(session.warehouse.id) : (params.destination ? Location.get(params.destination) : null)
        def shipmentType = params.shipmentType ? ShipmentType.get(params.shipmentType) : null
        def statusCode = params.status ? Enum.valueOf(ShipmentStatusCode.class, params.status) : null
        def statusStartDate = params.statusStartDate ? Date.parse("MM/dd/yyyy", params.statusStartDate) : null
        def statusEndDate = params.statusEndDate ? Date.parse("MM/dd/yyyy", params.statusEndDate) : null
        def lastUpdatedFrom = params.lastUpdatedFrom ? Date.parse("MM/dd/yyyy", params.lastUpdatedFrom) : null
        def lastUpdatedTo = params.lastUpdatedTo ? Date.parse("MM/dd/yyyy", params.lastUpdatedTo) : null


        println "lastUpdatedFrom = " + lastUpdatedFrom + " lastUpdatedTo = " + lastUpdatedTo

        def shipments = shipmentService.getShipments(params.terms, shipmentType, origin, destination,
                statusCode, statusStartDate, statusEndDate, lastUpdatedFrom, lastUpdatedTo, params.max)

        println "List shipments: " + (System.currentTimeMillis() - startTime) + " ms"

        [
                shipments      : shipments,
                shipmentType   : shipmentType?.id,
                origin         : origin?.id,
                destination    : destination?.id,
                status         : statusCode?.name,
                lastUpdatedFrom: lastUpdatedFrom,
                lastUpdatedTo  : lastUpdatedTo,
                incoming       : incoming
        ]
    }


    def create = {
        def shipmentInstance = new Shipment()
        shipmentInstance.properties = params

        if (params.type == "incoming") {
            shipmentInstance.destination = session.warehouse
        } else if (params.type == "outgoing") {
            shipmentInstance.origin = session.warehouse
        }
        render(view: "create", model: [shipmentInstance: shipmentInstance,
                                       warehouses      : Location.list(), eventTypes: EventType.list()])
    }

    def save = {
        def shipmentInstance = new Shipment(params)

        if (shipmentInstance.save(flush: true)) {

            // Try to add the initial event
            def eventType = EventType.get(params.eventType.id)
            if (eventType) {
                def shipmentEvent = new Event(eventType: eventType, eventLocation: session.warehouse, eventDate: new Date())
                shipmentInstance.addToEvents(shipmentEvent).save(flush: true)
            }
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'shipment.label', default: 'Shipment'), shipmentInstance.id])}"
            redirect(action: "showDetails", id: shipmentInstance.id)
        } else {
            render(view: "create", model: [shipmentInstance: shipmentInstance,
                                           warehouses      : Location.list(), eventTypes: EventType.list()])
        }
    }

    def update = {
        log.info params

        def shipmentInstance = Shipment.get(params.id)
        if (shipmentInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (shipmentInstance.version > version) {
                    shipmentInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'shipment.label', default: 'Shipment')] as Object[], "Another user has updated this Shipment while you were editing")
                    render(view: "editDetails", model: [shipmentInstance: shipmentInstance])
                    return
                }
            }

            // Bind request parameters
            shipmentInstance.properties = params

            // -- Processing shipment method  -------------------------
            log.info "autocomplete shipment method: " + params
            // Create a new shipment method if one does not exist
            def shipmentMethod = shipmentInstance.shipmentMethod
            if (!shipmentMethod) {
                shipmentMethod = new ShipmentMethod()
            }

            // If there's an ID but no name, it means we want to remove the shipper and shipper service
            if (!params.shipperService.name) {
                shipmentMethod.shipper = null
                shipmentMethod.shipperService = null
            }
            // Otherwise we set the selected accordingly
            else if (params.shipperService.id && params.shipperService.name) {
                def shipperService = ShipperService.get(params.shipperService.id)
                if (shipperService) {
                    shipmentMethod.shipperService = shipperService
                    shipmentMethod.shipper = shipperService.shipper
                }
            }
            // We work with and save the shipmentMethod instance in order to avoid a transient object exception
            // that occurs when setting the destination above and saving the shipment method within the shipment
            shipmentInstance.shipmentMethod = shipmentMethod
            shipmentInstance.shipmentMethod.save(flush: true)

            // -- Processing destination  -------------------------
            // Reset the destination to null
            if (!params.safeDestination.name) {
                shipmentInstance.destination = null
            }
            // Assign a destination if one was selected
            else if (params.safeDestination.id && params.safeDestination.name) {
                def destination = Location.get(params.safeDestination.id)
                if (destination && params.safeDestination.name == destination.name) // if it exists
                    shipmentInstance.destination = destination
            }

            // -- Processing carrier  -------------------------
            // This is necessary because Grails seems to be binding things incorrectly.  If we just let
            // Grails do the binding by itself, it tries to change the ID of the 'carrier' that is already
            // associated with the shipment, rather than changing the 'carrier' object associated with
            // the shipment.

            // Reset the carrier
            if (!params.safeCarrier.name) {
                shipmentInstance.carrier = null
            }
            // else if the person is found and different from the current one, then we use that person
            else if (params.safeCarrier.id && params.safeCarrier.name) {
                def safeCarrier = Person.get(params.safeCarrier.id)
                if (safeCarrier && safeCarrier?.name != shipmentInstance?.carrier?.name)
                    shipmentInstance.carrier = safeCarrier
            }
            // else if only the name is provided, we need to create a new person
            else {
                def safeCarrier = convertStringToPerson(params.safeCarrier.name)
                if (safeCarrier) {
                    safeCarrier.save(flush: true)
                    shipmentInstance.carrier = safeCarrier
                }
            }

            if (!shipmentInstance.hasErrors() && shipmentInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'shipment.label', default: 'Shipment'), shipmentInstance.id])}"
                redirect(action: "showDetails", id: shipmentInstance.id)
            } else {
                render(view: "editDetails", model: [shipmentInstance: shipmentInstance])
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'shipment.label', default: 'Shipment'), params.id])}"
            redirect(action: "list")
        }
    }

    def showDetails = {
        log.info "showDetails " + params
        def shipmentInstance = Shipment.get(params.id)
        if (!shipmentInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'shipment.label', default: 'Shipment'), params.id])}"
            redirect(action: "list", params: [type: params.type])
        } else {

            // Redirect to stock movement details page
            if (shipmentInstance?.requisition && !params?.override) {
                redirect(controller: "stockMovement", action: "show", id: shipmentInstance?.requisition?.id)
                return
            }

            def eventTypes = org.pih.warehouse.core.EventType.list()
            def shipmentWorkflow = shipmentService.getShipmentWorkflow(shipmentInstance)
            [shipmentInstance: shipmentInstance, shipmentWorkflow: shipmentWorkflow, shippingEventTypes: eventTypes]
        }
    }

    def showTransactions = {
        def shipmentInstance = Shipment.get(params.id)
        render(template: "showTransactions", model: [shipmentInstance: shipmentInstance])
    }

    def syncTransactions = {
        def shipmentInstance = Shipment.get(params.id)
        shipmentService.synchronizeTransactions(shipmentInstance)
        redirect(action: "showDetails", id: params.id)
    }

    def editDetails = {
        log.info params
        def shipmentInstance = Shipment.get(params.id)
        if (!shipmentInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'shipment.label', default: 'Shipment'), params.id])}"
            redirect(action: "list", params: [type: params.type])
        } else {
            [shipmentInstance: shipmentInstance]
        }
    }

    def sendShipment = {
        def transactionInstance
        def shipmentInstance = Shipment.get(params.id)
        def shipmentWorkflow = shipmentService.getShipmentWorkflow(params.id)

        if (!shipmentInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'shipment.label', default: 'Shipment'), params.id])}"
            redirect(action: "list", params: [type: params.type])
        } else {
            // handle a submit
            if ("POST".equalsIgnoreCase(request.getMethod())) {
                // make sure a shipping date has been specified and that is not the future
                if (!params.actualShippingDate || Date.parse("MM/dd/yyyy HH:mm XXX", params.actualShippingDate) > new Date()) {
                    flash.message = "${warehouse.message(code: 'shipping.specifyValidShipmentDate.message')}"
                    render(view: "sendShipment", model: [shipmentInstance: shipmentInstance, shipmentWorkflow: shipmentWorkflow])
                    return
                }

                // create the list of email recipients
                def emailRecipients = new HashSet()
                params.emailRecipientId?.each({
                    emailRecipients = emailRecipients + Person.get(it)
                })
                try {
                    // send the shipment
                    shipmentService.sendShipment(shipmentInstance, params.comment, session.user, session.warehouse,
                            Date.parse("MM/dd/yyyy HH:mm XXX", params.actualShippingDate))
                    //triggerSendShipmentEmails(shipmentInstance, userInstance, emailRecipients)
                }
                catch (TransactionException e) {
                    transactionInstance = e.transaction
                    shipmentInstance = Shipment.get(params.id)
                    shipmentWorkflow = shipmentService.getShipmentWorkflow(params.id)
                    render(view: "sendShipment", model: [shipmentInstance: shipmentInstance, shipmentWorkflow: shipmentWorkflow, transactionInstance: transactionInstance])
                    return
                }

                if (!shipmentInstance?.hasErrors() && !transactionInstance?.hasErrors()) {
                    flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'shipment.label', default: 'Shipment'), shipmentInstance.id])}"
                    redirect(action: "showDetails", id: shipmentInstance?.id)
                }
            }

            // populate the model and render the page
            render(view: "sendShipment", model: [shipmentInstance: shipmentInstance, shipmentWorkflow: shipmentWorkflow])
        }
    }

    def refreshCurrentStatus = {
        shipmentService.refreshCurrentStatus(params.id)
        redirect(action: "showDetails", id: params?.id)
    }


    def rollbackLastEvent = {
        def shipmentInstance = Shipment.get(params.id)
        shipmentService.rollbackLastEvent(shipmentInstance)
        redirect(action: "showDetails", id: shipmentInstance?.id)
    }

    def deleteShipment = {
        def shipmentInstance = Shipment.get(params.id)
        if (!shipmentInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'shipment.label', default: 'Shipment'), params.id])}"
            redirect(controller: "dashboard", action: "index")
            return
        } else {
            if ("POST".equalsIgnoreCase(request.getMethod())) {
                shipmentService.deleteShipment(shipmentInstance)

                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'shipment.label', default: 'Shipment'), shipmentInstance.id])}"
                redirect(controller: "dashboard", action: "index")
                return
            }
        }
        [shipmentInstance: shipmentInstance]
    }

    def markAsReceived = {
        def shipmentInstance = Shipment.get(params.id)

        // actually process the receipt
        shipmentService.markAsReceived(shipmentInstance, session.warehouse)
        if (!shipmentInstance.hasErrors()) {
            flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'shipment.label', default: 'Shipment'), shipmentInstance.id])}"
        }
        redirect(controller: "shipment", action: "showDetails", id: shipmentInstance.id)
    }


    def bulkDeleteShipments = {
        def shipmentIds = params.list("shipment.id")

        log.info "Shipment ids: " + shipmentIds
        try {
            shipmentIds.each { shipmentId ->
                Shipment shipment = Shipment.get(shipmentId)
                shipmentService.deleteShipment(shipment)
            }
            flash.message = "Successfully deleted ${shipmentIds?.size()} shipments"

        } catch (Exception e) {
            flash.message = "Error occurred while bulk deleting shipments: " + e.message
        }
        redirect(action: "list", params: [type: params.type, status: params.status])
    }

    def bulkReceiveShipments = {
        def shipmentIds = params.list("shipment.id")
        try {
            shipmentService.receiveShipments(shipmentIds, null, session.user.id, session.warehouse.id, true)
            flash.message = "Successfully received shipments"

        } catch (Exception e) {
            flash.message = "Error occurred while bulk receiving shipments: " + e.message
        }
        redirect(action: "list", params: [type: params.type, status: params.status])
    }


    def bulkMarkAsReceived = {
        def shipmentIds = params.list("shipment.id")
        Location location = Location.load(session.warehouse.id)
        try {
            shipmentIds.each { shipmentId ->
                Shipment shipment = Shipment.load(shipmentId)
                shipmentService.markAsReceived(shipment, location)
            }
            flash.message = "Successfully received shipments"

        } catch (Exception e) {
            flash.message = "Error occurred while bulk receiving shipments: " + e.message
        }
        redirect(action: "list", params: [type: params.type, status: params.status])
    }


    def bulkRollbackShipments = {
        def shipmentIds = params.list("shipment.id")
        try {
            shipmentService.rollbackShipments(shipmentIds)
            flash.message = "Successfully rolled back last event for selected shipments"

        } catch (Exception e) {
            flash.message = "Error occurred while bulk receiving shipments: " + e.message
        }
        redirect(action: "list", params: [type: params.type, status: params.status])
    }

    def downloadLabels = {

        Shipment shipmentInstance = Shipment.get(params.id)
        response.contentType = 'application/pdf'
        response.setHeader("Content-disposition", "attachment; filename=\"barcodes.pdf\"")
        def generator = new Code128Bean()
        generator.height = 10
        generator.fontSize = 3

        def shipmentItems = []

        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        barcode4jService.render(generator, shipmentInstance?.shipmentNumber?.toString(), baos, "image/png")

        shipmentInstance?.shipmentItems.each { shipmentItem ->
            ByteArrayOutputStream baos1 = new ByteArrayOutputStream()
            ByteArrayOutputStream baos2 = new ByteArrayOutputStream()
            barcode4jService.render(generator, shipmentItem?.inventoryItem?.lotNumber.toString(), baos1, "image/png")
            barcode4jService.render(generator, shipmentItem?.inventoryItem?.product?.productCode.toString(), baos2, "image/png")
            shipmentItems << [
                    productCode     : shipmentItem?.inventoryItem?.product?.productCode,
                    productName     : shipmentItem?.inventoryItem?.product?.name,
                    lotNumber       : shipmentItem?.inventoryItem?.lotNumber,
                    lotNumberBytes  : baos1.toByteArray(),
                    productCodeBytes: baos2.toByteArray()]
        }
        renderPdf(template: 'barcodeLabel', model: [shipmentInstance: shipmentInstance, shipmentItems: shipmentItems, shipmentNumberBytes: baos.toByteArray()])
    }

    def showPutawayLocations = {
        def location = Location.get(session.warehouse.id)
        ReceiptItem receiptItem = ReceiptItem.load(params.id)

        Product productInstance = receiptItem.inventoryItem?.product // Product.load(params.id)
        def binLocations = inventoryService.getProductQuantityByBinLocation(location, productInstance)

        render template: "showPutawayLocations", model: [product: productInstance, binLocations: binLocations]
    }


    def splitReceiptItem = {
        ReceiptItem receiptItem1 = ReceiptItem.load(params.id)
        ReceiptItem receiptItem2 = new ReceiptItem(receiptItem1.properties)
        receiptItem2.quantityReceived = 0
        receiptItem1.receipt.addToReceiptItems(receiptItem2)

        Shipment shipment = receiptItem1?.receipt?.shipment
        flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'shipment.label', default: 'Shipment'), shipment.id])}"
        redirect(controller: "shipment", action: "receiveShipment", id: shipment?.id)
    }

    def deleteReceiptItem = {
        ReceiptItem receiptItem = ReceiptItem.load(params.id)
        Shipment shipmentInstance = receiptItem?.receipt?.shipment

        if (receiptItem) {
            // FIXME Prevent delete of the last receipt item for a shipment item (kind of a hack). There should be a
            // way to represent one receipt item as the primary so we don't even show the delete button in the UI.
            if (receiptItem.shipmentItem.receiptItems.size() <= 1) {
                shipmentInstance?.receipt?.errors?.reject("shipping.mustHaveAtLeastOneReceiptItemPerShimentItem")
                render(view: "receiveShipment", model: [shipmentInstance: shipmentInstance, receiptInstance: shipmentInstance.receipt])
                return
            } else {
                shipmentInstance?.receipt.removeFromReceiptItems(receiptItem)
                receiptItem.shipmentItem.removeFromReceiptItems(receiptItem)
                receiptItem.delete()
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'shipment.label', default: 'Shipment'), shipmentInstance.id])}"
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'receiptItem.label', default: 'Receipt Item'), params.id])}"
        }
        redirect(controller: "shipment", action: "receiveShipment", id: shipmentInstance?.id)
    }

    def deleteReceipt = {
        Receipt receiptInstance = Receipt.get(params.id)
        Shipment shipmentInstance = receiptInstance?.shipment
        if (shipmentInstance) {
            shipmentInstance.receipt = null // FIXME This seems absurd
        }
        receiptInstance.delete()
        redirect(controller: "shipment", action: "showDetails", id: shipmentInstance?.id)
    }

    def validateReceipt = {
        Receipt receiptInstance = Receipt.get(params.id)
        Shipment shipmentInstance = receiptInstance?.shipment
        if (shipmentService.validateReceipt(receiptInstance)) {
            flash.message = "Receipt is valid"
        }

        redirect(controller: "shipment", action: "receiveShipment", id: shipmentInstance?.id)
    }


    def receiveShipment = { ReceiveShipmentCommand command ->
        log.info "params: " + params
        def receiptInstance
        def location = Location.get(session.warehouse.id)
        def shipmentInstance = Shipment.get(params.id)
        def userInstance = User.get(session.user.id)

        if (!shipmentInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'shipment.label', default: 'Shipment'), params.id])}"
            redirect(action: "list", params: [type: 'incoming'])
            return
        }

        // Process receive shipment form
        if ("POST".equalsIgnoreCase(request.method)) {

            // FIXME Somewhat unreadable code here ... need to clean it up a bit, but basically we're just checking
            // to see if there's already a receipt on the shipment. If not we create one
            receiptInstance = shipmentInstance.receipt
            if (!receiptInstance) {
                receiptInstance = new Receipt(params)
                shipmentInstance.addToReceipts(receiptInstance)
            } else {
                receiptInstance.properties = params
            }

            // check for errors
            if (receiptInstance.hasErrors() || !receiptInstance.validate()) {
                render(view: "receiveShipment", model: [shipmentInstance: shipmentInstance, receiptInstance: receiptInstance])
                return
            }


            if (params.saveButton == 'receiveShipment') {
                // For now, we'll always credit stock on receipt of shipment
                //def creditStockOnReceipt = params.creditStockOnReceipt=='yes'
                def creditStockOnReceipt = true
                // actually process the receipt
                try {
                    shipmentService.receiveShipment(shipmentInstance.id, params.comment, session?.user?.id, session.warehouse?.id, creditStockOnReceipt)
                    // If there were no errors we can trigger shipment emails to be sent
                    if (!shipmentInstance.hasErrors()) {
                        def recipients = new HashSet()
                        triggerReceiveShipmentEmails(shipmentInstance, userInstance, recipients)
                        flash.message = "${warehouse.message(code: 'default.received.message', args: [warehouse.message(code: 'shipment.label', default: 'Shipment'), shipmentInstance.shipmentNumber ?: shipmentInstance?.id])}"
                        redirect(action: "showDetails", id: shipmentInstance?.id)
                        return
                    }
                } catch (ValidationException e) {
                    shipmentInstance = Shipment.read(params.id)
                    receiptInstance.errors = e.errors
                    render(view: "receiveShipment", model: [shipmentInstance: shipmentInstance, receiptInstance: receiptInstance])
                    return
                }
            } else {

                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'shipment.label', default: 'Shipment'), shipmentInstance.shipmentNumber ?: shipmentInstance?.id])}"
                if (params.saveButton == 'saveAndExit') {
                    redirect(controller: "shipment", action: "showDetails", id: shipmentInstance?.id)
                } else {
                    redirect(controller: "shipment", action: "receiveShipment", id: shipmentInstance?.id, fragment: "tabs-details")
                }
                return

            }
        }

        // Display form
        else {

            if (shipmentInstance?.destination != location) {
                flash.message = "${g.message(code: 'shipping.mustBeLoggedIntoDestinationToReceive.message', args: [shipmentInstance?.destination])}"
            }

            receiptInstance = shipmentService.findOrCreateReceipt(shipmentInstance)

        }

        render(view: "receiveShipment", model: [shipmentInstance: shipmentInstance, receiptInstance: receiptInstance])
    }


    /**
     *
     * @param shipmentInstance
     * @param userInstance
     * @param recipients
     */
    void triggerReceiveShipmentEmails(Shipment shipmentInstance, User userInstance, Set<Person> recipients) {
        if (!userInstance) userInstance = User.get(session.user.id)
        if (!shipmentInstance.hasErrors()) {
            if (!recipients) recipients = new HashSet<Person>()

            // add all admins to the email
            def adminList = userService.findUsersByRoleType(RoleType.ROLE_SHIPMENT_NOTIFICATION)
            adminList.each { adminUser ->
                recipients.add(adminUser)
            }

            // add the current user to the list of email recipients
            if (userInstance?.email) {
                recipients.add(userInstance)
            }

            // add all shipment recipients
            shipmentInstance?.recipients?.each { recipient ->
                recipients.add(recipient)
            }

            def shipmentName = "${shipmentInstance.name}"
            def shipmentType = "${format.metadata(obj: shipmentInstance.shipmentType)}"
            def shipmentDate = "${formatDate(date: shipmentInstance?.actualDeliveryDate, format: 'MMMMM dd yyyy')}"
            def subject = "${warehouse.message(code: 'shipment.hasBeenReceived.message', args: [shipmentType, shipmentName, shipmentDate])}"
            def body = g.render(template: "/email/shipmentReceived", model: [shipmentInstance: shipmentInstance, userInstance: userInstance])
            def toList = recipients?.collect { it?.email }?.unique()
            log.info("Mailing shipment emails to ${toList} with subject ${subject}")

            try {
                mailService.sendHtmlMail(subject, body.toString(), toList)
            } catch (Exception e) {
                log.error "Error triggering receive shipment emails " + e.message
            }
        }
    }

    def renderReceivedEmail = {
        def shipmentInstance = Shipment.get(params.id)
        def userInstance = User.get(session.user.id)
        render(template: "/email/shipmentReceived", model: [shipmentInstance: shipmentInstance, userInstance: userInstance])
    }

    def renderShippedEmail = {
        def shipmentInstance = Shipment.get(params.id)
        def userInstance = User.get(session.user.id)
        render(template: "/email/shipmentShipped", model: [shipmentInstance: shipmentInstance, userInstance: userInstance])
    }


    def showPackingList = {
        def shipmentInstance = Shipment.get(params.id)
        if (!shipmentInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'shipment.label', default: 'Shipment'), params.id])}"
            redirect(action: "list", params: [type: params.type])
        } else {
            [shipmentInstance: shipmentInstance]
        }
    }

    def downloadPackingList = {
        def shipmentInstance = Shipment.get(params.id)
        if (!shipmentInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'shipment.label', default: 'Shipment'), params.id])}"
            redirect(action: "list", params: [type: params.type])
        } else {
            String query = """
				select  
					container.name,  
					container.height, 
					container.width, 
					container.length, 
					container.volume_units, 
					container.weight, 
					container.weight_units,
					shipment_item.quantity,
					product.name,
					shipment_item.serial_number
				from shipment, container, shipment_item, product
				where shipment.id = container.shipment_id
				and shipment_item.container_id = container.id
				and shipment_item.product_id = product.id 
				and shipment.id = ${params.id}"""

            StringWriter sw = new StringWriter()
            CSVWriter writer = new CSVWriter(sw)
            Sql sql = new Sql(sessionFactory.currentSession.connection())

            String[] colArray = new String[6]
            colArray.putAt(0, "unit")
            colArray.putAt(1, "dimensions")
            colArray.putAt(2, "weight")
            colArray.putAt(3, "qty")
            colArray.putAt(4, "item")
            colArray.putAt(5, "serial number")
            writer.writeNext(colArray)
            sql.eachRow(query) { row ->

                def rowArray = new String[6]
                rowArray.putAt(0, row[0])
                rowArray.putAt(1, (row[1]) ? row[1] : "0" + "x" + (row[2]) ? row[2] : "0" + "x" + (row[3]) ? row[3] : "0" + " " + (row[4]) ? row[4] : "")
                rowArray.putAt(2, row[5] + " " + row[6])
                rowArray.putAt(3, row[7])
                rowArray.putAt(4, row[8])
                rowArray.putAt(5, row[9])
                writer.writeNext(rowArray)
            }
            log.info "results: " + sw.toString()
            response.setHeader("Content-disposition", "attachment; filename=\"PackingList.csv\"")
            render(contentType: "text/csv", text: sw.toString())
            sql.close()
        }
    }


    def editContents = {
        def shipmentInstance = Shipment.get(params.id)
        def containerInstance = Container.get(params?.container?.id)

        if (!shipmentInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'shipment.label', default: 'Shipment'), params.id])}"
            redirect(action: "list", params: [type: params.type])
        } else {

            if (!containerInstance && shipmentInstance?.containers) {
                containerInstance = shipmentInstance.containers.iterator().next()
            }
            [shipmentInstance: shipmentInstance, containerInstance: containerInstance]
        }
    }


    def editContainer = {

        log.info params

        def shipmentInstance = Shipment.get(params.shipmentId)
        def containerInstance = Container.get(params.containerId)
        if (containerInstance) {

            containerInstance.properties = params

            Iterator iter = containerInstance.shipmentItems.iterator()
            while (iter.hasNext()) {
                def item = iter.next()
                log.info item.product.name + " " + item.quantity

                if (item.quantity == 0) {
                    item.delete()
                    iter.remove()
                }
            }

            // If the user removed the recipient, we need to make sure that the whole object is removed (not just the ID)
            for (def shipmentItem : containerInstance?.shipmentItems) {
                if (!shipmentItem?.recipient?.id) {
                    log.info("item recipient: " + shipmentItem?.recipient?.id)
                    shipmentItem.recipient = null
                }
            }

            if (!containerInstance.hasErrors() && containerInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'container.label', default: 'Container'), containerInstance.id])}"
                redirect(action: "editContents", id: shipmentInstance.id, params: ["container.id": params.containerId])
            } else {
                flash.message = "${warehouse.message(code: 'shipping.couldNotEditContainer.message')}"
                redirect(action: "showDetails", id: shipmentInstance.id, params: ["containerId": params.containerId])
                //render(view: "edit", model: [containerInstance: containerInstance])
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'container.label', default: 'Container'), params.containerId])}"
            redirect(action: "showDetails", id: shipmentInstance.id, params: ["containerId": params.containerId])
        }
    }


    def copyContainer = {
        def container = Container.get(params.id)
        def shipment = Shipment.get(params.shipmentId)

        if (container && shipment) {
            def numCopies = (params.copies) ? Integer.parseInt(params.copies) : 1
            int index = (shipment?.containers) ? (shipment.containers.size()) : 1

            while (numCopies-- > 0) {
                def containerCopy = new Container(container.properties)
                containerCopy.id = null
                containerCopy.name = "" + (++index)
                containerCopy.containerType = container.containerType
                containerCopy.weight = container.weight
                containerCopy.shipmentItems = null
                containerCopy.save(flush: true)

                container.shipmentItems.each {
                    containerCopy.shipment.addToShipmentItems(shipmentItem).save(flush: true)
                }
                shipment.addToContainers(containerCopy).save(flush: true)
            }
            flash.message = "${warehouse.message(code: 'shipping.copiedContainerSuccessfully.message')}"
        } else {
            flash.message = "${warehouse.message(code: 'shipping.unableToCopyPackage.message')}"
        }

        redirect(action: 'showDetails', id: params.shipmentId)
    }


    def addDocument = {
        log.info params
        def shipmentInstance = Shipment.get(params.id)
        def documentInstance = Document.get(params?.document?.id)
        if (!documentInstance) {
            documentInstance = new Document()
        }
        if (!shipmentInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'shipment.label', default: 'Shipment'), params.id])}"
            redirect(action: "list")
        }
        render(view: "addDocument", model: [shipmentInstance: shipmentInstance, documentInstance: documentInstance])
    }

    def editDocument = {
        def shipmentInstance = Shipment.get(params?.shipmentId)
        def documentInstance = Document.get(params?.documentId)
        if (!shipmentInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'shipment.label', default: 'Shipment'), params.shipmentId])}"
            redirect(action: "list")
        }
        if (!documentInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'document.label', default: 'Document'), params.documentId])}"
            redirect(action: "showDetails", id: shipmentInstance?.id)
        }
        render(view: "addDocument", model: [shipmentInstance: shipmentInstance, documentInstance: documentInstance])
    }


    def addComment = {
        log.debug params
        def shipmentInstance = Shipment.get(params.id)
        render(view: "addComment", model: [shipmentInstance: shipmentInstance, comment: new Comment()])
    }

    /**
     * This action is used to render the form page used to add a
     * new package/container to a shipment.
     */
    def addPackage = {
        def shipmentInstance = Shipment.get(params.id)
        def containerName = (shipmentInstance?.containers) ? String.valueOf(shipmentInstance?.containers?.size() + 1) : "1"
        def containerInstance = new Container(name: containerName)

        render(view: "addPackage", model: [shipmentInstance: shipmentInstance, containerInstance: containerInstance])
    }


    /**
     * This closure is used to process the 'add package' form.
     */
    def savePackage = {

        log.info "params " + params

        def shipmentInstance = Shipment.get(params.shipmentId)
        def parentContainerInstance = Container.get(params?.parentContainer?.id)

        def containerInstance = new Container(params)
        if (containerInstance && shipmentInstance) {
            shipmentInstance.addToContainers(containerInstance)
            if (!shipmentInstance.hasErrors() && shipmentInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'container.label', default: 'Container'), containerInstance.id])}"
                if (parentContainerInstance) {
                    parentContainerInstance.addToContainers(containerInstance).save(flush: true)
                }
                redirect(action: "editContents", id: shipmentInstance.id, params: ["container.id": containerInstance.id])
            } else {
                render(view: "addPackage", model: [shipmentInstance: shipmentInstance, containerInstance: containerInstance])
            }
        } else {
            redirect(action: 'showDetails', id: params.shipmentId)
        }
    }


    def saveComment = {
        def shipmentInstance = Shipment.get(params.shipmentId)
        def recipient = (params.recipientId) ? User.get(params.recipientId) : null
        def comment = new Comment(comment: params.comment, sender: session.user, recipient: recipient)
        if (shipmentInstance) {
            shipmentInstance.addToComments(comment).save(flush: true)
            flash.message = "${warehouse.message(code: 'shipping.addedCommentToShipment.message', args: [params.comment, shipmentInstance.name])}"
        }
        redirect(action: 'showDetails', id: params.shipmentId)
    }


    def editItem = {
        def item = ShipmentItem.get(params.id)
        def container = item.getContainer()
        def shipmentId = container.getShipment().getId()
        if (item) {
            item.quantity = Integer.parseInt(params.quantity)
            item.save()
            flash.message = "${warehouse.message(code: 'shipping.addedCommentToShipment.message', args: [params.id, container.name])}"
            redirect(action: 'editContents', id: shipmentId)
        } else {
            flash.message = "${warehouse.message(code: 'shipping.couldNotEditItemFromContainer.message', args: [params.id])}"
            redirect(action: 'showDetails', id: shipmentId, params: [container.id, container.id])
        }
    }


    def deleteDocument = {
        def document = Document.get(params.id)
        def shipment = Shipment.get(params.shipmentId)
        if (shipment && document) {
            shipment.removeFromDocuments(document).save(flush: true)
            document.delete()
            flash.message = "${warehouse.message(code: 'shipping.deletedDocumentFromShipment.message', args: [params.id])}"
        } else {
            flash.message = "${warehouse.message(code: 'shipping.couldNotRemoveDocumentFromShipment.message', args: [params.id])}"
        }
        redirect(action: 'showDetails', id: params.shipmentId)
    }

    def deleteEvent = {
        def event = Event.get(params.id)
        def shipment = Shipment.get(params.shipmentId)
        if (shipment && event) {   // not allowed to delete a "created" event
            shipment.removeFromEvents(event)
            event.delete()
            shipment.save()
            flash.message = "${warehouse.message(code: 'shipping.deletedEventFromShipment.message', args: [params.id])}"
        } else {
            flash.message = "${warehouse.message(code: 'shipping.couldNotRemoveEventFromShipment.message', args: [params.id])}"
        }
        redirect(action: 'showDetails', id: params.shipmentId)
    }

    def deleteContainer = {
        def container = Container.get(params.id)
        def shipment = Shipment.get(params.shipmentId)

        if (shipment && container) {
            container.delete()
            flash.message = "${warehouse.message(code: 'shipping.deletedContainerFromShipment.message', args: [params.id])}"
        } else {
            flash.message = "${warehouse.message(code: 'shipping.couldNotRemoveContainerFromShipment.message', args: [params.id])}"
        }

        redirect(action: 'showDetails', id: params.shipmentId)
    }

    def deleteItem = {
        def shipmentItem = ShipmentItem.get(params.id)
        def container = shipmentItem.getContainer()
        def shipmentId = container.getShipment().getId()
        if (item) {
            container.removeFromShipmentItems(shipmentItem)
            flash.message = "${warehouse.message(code: 'shipping.deletedShipmentItemFromContainer.message', args: [params.id, container.name])}"
            redirect(action: 'showDetails', id: shipmentId)
        } else {
            flash.message = "${warehouse.message(code: 'shipping.couldNotRemoveItemFromContainer.message', args: [params.id])}"
            redirect(action: 'showDetails', id: shipmentId)
        }
    }

    def deleteComment = {
        def comment = Comment.get(params.id)
        def shipment = Shipment.get(params.shipmentId)
        if (shipment && comment) {
            shipment.removeFromComments(comment).save(flush: true)
            comment.delete()
            flash.message = "${warehouse.message(code: 'shipping.deletedCommentFromShipment.message', args: [comment, params.shipmentId])}"
            redirect(action: 'showDetails', id: params.shipmentId)
        } else {
            flash.message = "${warehouse.message(code: 'shipping.couldNotRemoveCommentFromShipment.message', args: [params.id])}"
            redirect(action: 'showDetails', id: params.shipmentId)
        }
    }


    def editEvent = {
        def eventInstance = Event.get(params.id)
        def shipmentInstance = Shipment.get(params.shipmentId)

        if (!eventInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'shipmentEvent.label', default: 'ShipmentEvent'), params.id])}"
            redirect(action: "showDetails", id: params.shipmentId)
        }

        render(view: "editEvent", model: [shipmentInstance: shipmentInstance, eventInstance: eventInstance])
    }


    def addEvent = {
        def shipmentInstance = Shipment.get(params.id)

        if (!shipmentInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'shipmentEvent.label', default: 'ShipmentEvent'), params.id])}"
            redirect(action: "list")
        }

        def eventInstance = new Event(params)
        render(view: "editEvent", model: [shipmentInstance: shipmentInstance, eventInstance: eventInstance])
    }

    def saveEvent = {
        def shipmentInstance = Shipment.get(params.shipmentId)
        def eventInstance = Event.get(params.eventId) ?: new Event()

        bindData(eventInstance, params)

        // check for errors
        if (eventInstance.hasErrors()) {
            flash.message = "${warehouse.message(code: 'shipping.unableToEditEvent.message', args: [format.metadata(obj: eventInstance?.eventType)])}"
            eventInstance?.errors.allErrors.each {
                log.error it
            }
            render(view: "editEvent", model: [shipmentInstance: shipmentInstance, eventInstance: eventInstance])
        }

        // save (or add) the event
        if (params.eventId) {
            eventInstance.save(flush: true)
        } else {
            shipmentInstance.addToEvents(eventInstance).save(flush: true)
        }

        redirect(action: 'showDetails', id: shipmentInstance.id)
    }

    def addShipmentItem = {
        log.info "parameters: " + params

        [shipmentInstance : Shipment.get(params.id),
         containerInstance: Container.get(params?.containerId),
         itemInstance     : new ShipmentItem()]
    }

    def addReferenceNumber = {
        def referenceNumber = new ReferenceNumber(params)
        def shipment = Shipment.get(params.shipmentId)
        shipment.addToReferenceNumbers(referenceNumber)
        flash.message = "${warehouse.message(code: 'shipping.addedReferenceNumber.message')}"
        redirect(action: 'show', id: params.shipmentId)
    }

    def form = {
        [shipments: Shipment.list()]
    }

    def view = {}

    def generateDocuments = {
        def shipmentInstance = Shipment.get(params.id)
        def shipmentWorkflow = shipmentService.getShipmentWorkflow(shipmentInstance)

        if (shipmentWorkflow.documentTemplate) {
            render(view: "templates/$shipmentWorkflow.documentTemplate", model: [shipmentInstance: shipmentInstance])
        } else {
            // just go back to the show details page if there is no templaet associated with this shipment workflow
            redirect(action: "showDetails", params: ['id': shipmentInstance.id])
        }
    }

    Person convertStringToPerson(String name) {
        def person = new Person()
        if (name) {
            def nameArray = name.split(" ")
            nameArray.each {
                if (it.contains("@")) {
                    person.email = it
                } else if (!person.firstName) {
                    person.firstName = it
                } else if (!person.lastName) {
                    person.lastName = it
                } else {
                    person.lastName += " " + it
                }
            }
        }
        return person
    }


    def addToShipment = {

        // Get product IDs and convert them to String
        def productIds = params.list('product.id')
        productIds = productIds.collect { String.valueOf(it) }

        Location location = Location.get(session.warehouse.id)
        def commandInstance = shipmentService.getAddToShipmentCommand(productIds, location)

        [commandInstance: commandInstance]
    }


    def addToShipmentPost = { ItemListCommand command ->

        println "add to shipment post " + params.shipmentContainerKey

        if (!params?.shipmentContainerKey) {
            command.errors.rejectValue("items", "addToShipment.container.invalid")
            render(view: "addToShipment", model: [commandInstance: command])
            return
        }

        def shipmentContainer = params?.shipmentContainerKey?.split(":")
        if (shipmentContainer) {

            def shipment = Shipment.get(shipmentContainer[0])
            def container = Container.get(shipmentContainer[1])

            if (!shipment) {
                command.errors.rejectValue("items", "addToShipment.container.invalid")
                render(view: "addToShipment", model: [commandInstance: command])
                return
            }

            command.items.each {
                it.shipment = shipment
                it.container = container
            }

            try {
                boolean atLeastOneUpdate = shipmentService.addToShipment(command)
                if (atLeastOneUpdate) {
                    flash.message = "${warehouse.message(code: 'shipping.shipmentItemsHaveBeenAdded.message')}"
                    redirect(controller: "createShipmentWorkflow", action: "createShipment",
                            id: shipment.id, params: ["skipTo": "Packing", "containerId": container?.id])
                    return
                } else {
                    flash.message = "${warehouse.message(code: 'shipping.noShipmentItemsHaveBeenUpdated.message')}"
                }
            } catch (ShipmentItemException e) {
                flash['errors'] = e.shipmentItem.errors
                render(view: "addToShipment", model: [commandInstance: command])
                return
            } catch (ValidationException e) {
                flash['errors'] = e.errors
                render(view: "addToShipment", model: [commandInstance: command])
                return
            }
        }

        redirect(controller: "inventory", action: "browse")
    }


    def exportPackingList = {
        log.info "Export packing list for shipment " + params
        Shipment shipment = Shipment.get(params.id)
        if (!shipment) {
            throw new Exception("Could not locate shipment with ID " + params.id)
        }

        OutputStream outputStream = null
        try {
            // Write the file to the response
            ByteArrayOutputStream baos = new ByteArrayOutputStream()
            response.contentType = "application/vnd.ms-excel"
            response.setHeader 'Content-disposition', "attachment; filename=\"Shipment ${shipment?.shipmentNumber} - Packing List.xls\""
            shipmentService.exportPackingList(params.id, baos)
            response.outputStream << baos.toByteArray()
            response.outputStream.flush()
            return

        } catch (IOException e) {
            flash.message = "Failed to export packing list due to the following error: " + e.message
        } catch (Exception e) {
            log.warn("Failed to export packing list due to the following error: " + e.message, e)
            flash.message = "Failed to export packing list due to the following error: " + e.message
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close()
                } catch (IOException e) {
                    log.error('IOException occurred while closing output stream', e)
                }
            }
        }
        redirect(controller: "shipment", action: "showDetails", id: params.id)
    }

}

class ReceiveShipmentCommand implements Serializable {

    String comments
    Person recipient
    Receipt receipt
    Shipment shipment
    Transaction transaction
    Boolean creditStockOnReceive = true
    Date actualDeliveryDate

    static constraints = {
        receipt(nullable: true)
        shipment(nullable: false, validator: { value, obj -> obj.shipment.hasShipped() && !obj.shipment.wasReceived() })
        transaction(nullable: true)
        recipient(nullable: false)
        comments(nullable: true)
        creditStockOnReceive(nullable: false)
        actualDeliveryDate(nullable: false)
    }

}



