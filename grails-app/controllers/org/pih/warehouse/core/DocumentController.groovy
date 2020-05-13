/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.core

import groovyx.net.http.HTTPBuilder
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.order.Order
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment
import org.springframework.web.multipart.MultipartFile
import util.FileUtil
import org.pih.warehouse.core.Constants

class DocumentController {

    def fileService
    def grailsApplication
    def templateService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {

        log.info "params: " + params
        params.max = Math.min(params.max ? params.int('max') : 10, 100)

        def documentType = DocumentType.get(params?.documentType?.id)

        def documentInstanceList = []
        def documentInstanceTotal = 0

        if (params.q || documentType) {
            def q = "%" + params.q + "%"
            def queryClosure = {
                ilike("name", q)
                if (documentType) {
                    eq("documentType", documentType)
                }
                maxResults(params.max)
            }

            documentInstanceList = Document.createCriteria().list(queryClosure)
            documentInstanceTotal = Document.createCriteria().count(queryClosure)
        } else {
            log.info "show all: " + params
            documentInstanceList = Document.list(params)
            documentInstanceTotal = Document.count()
        }

        [documentInstanceList: documentInstanceList, documentInstanceTotal: documentInstanceTotal]
    }

    def create = {
        def documentInstance = new Document()
        documentInstance.properties = params
        return [documentInstance: documentInstance]
    }

    def save = {

        log.info "Params " + params
        def documentInstance = new Document(params)

        def file = request.getFile("fileContents")
        // file must not be empty and must be less than 10MB
        if (!file || file?.isEmpty()) {
            flash.message = "${warehouse.message(code: 'document.documentCannotBeEmpty.message')}"
        }
        // FIXME The size limit should be configurable
        else if (file.size < 10 * 1024 * 1000) {

            documentInstance.name = file.originalFilename
            documentInstance.filename = file.originalFilename
            documentInstance.fileContents = file.bytes
            documentInstance.extension = FileUtil.getExtension(file.originalFilename)
            documentInstance.contentType = file.contentType
        }

        if (documentInstance.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'document.label', default: 'Document'), documentInstance.id])}"
            redirect(action: "edit", id: documentInstance.id)
        } else {
            render(view: "create", model: [documentInstance: documentInstance])
        }
    }

    def show = {
        def documentInstance = Document.get(params.id)
        if (!documentInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'document.label', default: 'Document'), params.id])}"
            redirect(action: "list")
        } else {
            [documentInstance: documentInstance]
        }
    }

    def edit = {
        def documentInstance = Document.get(params.id)
        if (!documentInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'document.label', default: 'Document'), params.id])}"
            redirect(action: "list")
        } else {
            return [documentInstance: documentInstance]
        }
    }

    def update = {
        log.info "Update " + params


        def documentInstance = Document.get(params.id)
        if (documentInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (documentInstance.version > version) {
                    documentInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'document.label', default: 'Document')] as Object[], "Another user has updated this Document while you were editing")
                    render(view: "edit", model: [documentInstance: documentInstance])
                    return
                }
            }

            documentInstance.properties = params

            if (!documentInstance.hasErrors() && documentInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'document.label', default: 'Document'), documentInstance.id])}"
                redirect(action: "list", id: documentInstance.id)
            } else {
                render(view: "edit", model: [documentInstance: documentInstance])
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'document.label', default: 'Document'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def documentInstance = Document.get(params.id)
        if (documentInstance) {
            try {
                documentInstance.delete(flush: true)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'document.label', default: 'Document'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'document.label', default: 'Document'), params.id])}"
                redirect(action: "list", id: params.id)
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'document.label', default: 'Document'), params.id])}"
            redirect(action: "list")
        }
    }

    /**
     * Upload a document to the server
     */
    def uploadDocument = { DocumentCommand command ->
        log.info "Uploading document: " + params
        def file = command.fileContents

        log.info "multipart file: " + file.originalFilename + " " + file.contentType + " " + file.size + " "

        def shipmentInstance = Shipment.get(command.shipmentId)
        def orderInstance = Order.get(command.orderId)
        def requestInstance = Requisition.get(command.requestId)

        // file must not be empty and must be less than 10MB
        // FIXME The size limit needs to go somewhere
        if (file?.isEmpty()) {
            flash.message = "${warehouse.message(code: 'document.documentTooLarge.message')}"
        } else if (file.size < 10 * 1024 * 1000) {
            log.info "Creating new document "
            // Document type with id 9 is "Other" and it's default in case there's no document type chosen
            def typeId = command?.typeId ?: Constants.DEFAULT_DOCUMENT_TYPE_ID;
            Document documentInstance = new Document(
                    size: file.size,
                    name: command.name ?: file.originalFilename,
                    filename: file.originalFilename,
                    fileContents: command.fileContents.bytes,
                    contentType: file.contentType,
                    extension: FileUtil.getExtension(file.originalFilename),
                    documentNumber: command.documentNumber,
                    documentType: DocumentType.get(typeId))

            // Check to see if there are any errors
            if (documentInstance.validate() && !documentInstance.hasErrors()) {
                log.info "Saving document " + documentInstance
                if (shipmentInstance) {
                    shipmentInstance.addToDocuments(documentInstance).save(flush: true)
                    flash.message = "${warehouse.message(code: 'document.successfullySavedToShipment.message', args: [shipmentInstance?.name])}"
                } else if (orderInstance) {
                    orderInstance.addToDocuments(documentInstance).save(flush: true)
                    flash.message = "${warehouse.message(code: 'document.successfullySavedToOrder.message', args: [orderInstance?.name])}"
                } else if (requestInstance) {
                    requestInstance.addToDocuments(documentInstance).save(flush: true)
                    flash.message = "${warehouse.message(code: 'document.successfullySavedToRequest.message', args: [requestInstance?.description])}"
                }
            }
            // If there are errors, we need to redisplay the document form
            else {
                log.info "Document did not save " + documentInstance.errors
                flash.message = "${warehouse.message(code: 'document.cannotSave.message', args: [documentInstance.errors])}"
                if (shipmentInstance) {
                    redirect(controller: "stockMovement", action: "addDocument", id: shipmentInstance.id,
                            model: [shipmentInstance: shipmentInstance, documentInstance: documentInstance])
                    return
                } else if (orderInstance) {
                    redirect(controller: "order", action: "addDocument", id: orderInstance.id,
                            model: [orderInstance: orderInstance, documentInstance: documentInstance])
                    return
                } else if (requestInstance) {
                    redirect(controller: "requisition", action: "addDocument", id: requestInstance.id,
                            model: [requestInstance: requestInstance, documentInstance: documentInstance])
                    return

                }
            }
        } else {
            log.info "Document is too large"
            flash.message = "${warehouse.message(code: 'document.documentTooLarge.message')}"
            if (shipmentInstance) {
                redirect(controller: 'shipment', action: 'showDetails', id: command.shipmentId)
                return
            } else if (orderInstance) {
                redirect(controller: 'order', action: 'show', id: command.orderId)
                return
            } else if (requestInstance) {
                redirect(controller: 'requisition', action: 'show', id: command.requestId)
                return

            }
        }

        // This is, admittedly, a hack but I wanted to avoid having to add this code to each of
        // these controllers.
        log.info("Redirecting to appropriate show details page")
        if (shipmentInstance) {
            redirect(controller: 'shipment', action: 'showDetails', id: command.shipmentId)
            return
        } else if (orderInstance) {
            redirect(controller: 'order', action: 'show', id: command.orderId)
            return
        } else if (requestInstance) {
            redirect(controller: 'requisition', action: 'show', id: command.requestId)
            return

        }

    }

    /**
     * @deprecated
     */
    def upload = {

        log.info "Upload " + params

        def documentInstance = Document.get(params.id)
        if (documentInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (documentInstance.version > version) {
                    documentInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'document.label', default: 'Document')] as Object[], "Another user has updated this Document while you were editing")
                    render(view: "edit", model: [documentInstance: documentInstance])
                    return
                }
            }

            def file = request.getFile("fileContents")
            if (file?.empty) {
                flash.message = "${g.message(code: 'file')}"
            } else {

                // Only change the name if it was never modified from the original filename
                if (documentInstance.filename == documentInstance.name) {
                    documentInstance.name = file.originalFilename
                }

                documentInstance.filename = file.originalFilename
                documentInstance.fileContents = file.bytes
                documentInstance.extension = FileUtil.getExtension(file.originalFilename)
                documentInstance.contentType = file.contentType
            }

            if (!documentInstance.hasErrors() && documentInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'document.label', default: 'Document'), documentInstance.id])}"
                redirect(action: "edit", id: documentInstance.id)
            } else {
                render(view: "edit", model: [documentInstance: documentInstance])
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'document.label', default: 'Document'), params.id])}"
            redirect(action: "list")
        }
    }

    /**
     * Allow user to download the file associated with the given id.
     */
    def download = {
        log.debug "Download file with id = ${params.id}"
        def documentInstance = Document.get(params.id)
        if (!documentInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'document.label', default: 'Document'), params.id])}"
            redirect(controller: "shipment", action: "showDetails", id: document.getShipment().getId())
        } else {
            if (!params.boolean("inline")) {
                response.setHeader "Content-disposition", "attachment;filename=\"${documentInstance.filename}\""
            }
            response.contentType = documentInstance.contentType
            response.outputStream << documentInstance.fileContents
            response.outputStream.flush()
        }
    }

    def preview = {
        def documentInstance = Document.get(params.id)
        render(template: "preview", model: [documentInstance: documentInstance])
    }


    def render = {
        def documentInstance = Document.get(params.id)
        Shipment shipmentInstance = Shipment.get(params.shipmentId)
        if (!documentInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'document.label', default: 'Document'), params.id])}"
            redirect(controller: "shipment", action: "showDetails", id: shipmentInstance.id)
        } else {
            if (documentInstance?.documentType?.documentCode != DocumentCode.SHIPPING_TEMPLATE) {
                throw new IllegalArgumentException("Document render action only supports documents with document code ${DocumentCode.SHIPPING_TEMPLATE}")
            }

            // FIXME Move this into the service layer and try to pass back a BAOS
            try {
                File tempFile = fileService.renderShippingTemplate(documentInstance, shipmentInstance)
                def filename = "${documentInstance.name}-${shipmentInstance?.name?.trim()}.docx"
                response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
                response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                response.outputStream << tempFile.readBytes()
                response.outputStream.flush()
            } catch(Exception e) {
                log.error("Unable to render file due to error: " + e.message, e)
                flash.message = "${e.message}"
                redirect(action: "edit", id: params.id)
            }
        }
    }

    /**
     * Saves changes to document metadata (or, more specifically, saves changes to metadata--type,name,documentNumber--associated with
     * a document without modifying the document itself--the upload method handles this)
     */
    def saveDocument = { DocumentCommand command ->
        // fetch the existing document
        Document documentInstance = Document.get(params.documentId)
        if (!documentInstance) {
            // this should never happen, so fail hard
            throw new RuntimeException("Unable to retrieve document " + params.documentId)
        }

        // bind the command object to the document object ignoring the shipmentId and fileContents params (which can't change after creation)
        //bindData(documentInstance, command, ['shipmentId','orderId'])
        // manually update the document type
        documentInstance.name = command.name
        documentInstance.documentNumber = command.documentNumber
        documentInstance.documentType = DocumentType.get(command.typeId)

        // If a new file is passed we should update all of the read-only properties
        def file = command.fileContents

        if (file && !file.empty) {
            documentInstance.name = command.name ?: file.originalFilename
            documentInstance.filename = file.originalFilename
            documentInstance.fileContents = file.bytes
            documentInstance.extension = FileUtil.getExtension(file.originalFilename)
            documentInstance.contentType = file.contentType
        }

        if (!documentInstance.hasErrors()) {
            flash.message = "${warehouse.message(code: 'document.succesfullyUpdatedDocument.message')}"
            if (command.shipmentId) {
                redirect(controller: 'shipment', action: 'showDetails', id: command.shipmentId)
            } else if (command.orderId) {
                redirect(controller: 'order', action: 'show', id: command.orderId)
            }
        } else {
            if (command.shipmentId) {
                redirect(controller: "shipment", action: "addDocument", id: command.shipmentId,
                        model: [shipmentInstance: Shipment.get(command.shipmentId), documentInstance: documentInstance])
            } else if (command.orderId) {
                redirect(controller: "order", action: "addDocument", id: command.orderId,
                        model: [orderInstance: Order.get(command.orderId), documentInstance: documentInstance])

            }
        }
    }

    def printZebraTemplate = {
        Document document = Document.load(params.id)
        Location location = Location.load(session.warehouse.id)
        InventoryItem inventoryItem = InventoryItem.load(params?.inventoryItem?.id)

        String lptPort = grailsApplication.config.openboxes.linePrinterTerminal.port

        Map model = [document: document, inventoryItem: inventoryItem, location: location]
        String renderedContent = templateService.renderTemplate(document, model)

        try {
            FileOutputStream os = new FileOutputStream(lptPort)
            PrintStream ps = new PrintStream(os)
            ps.println(renderedContent)
            ps.print("\f")
            ps.close()
            flash.message = "Label printed successfully"
        }
        catch (Exception e) {
            flash.message = e.message
        }

        redirect(controller: "inventoryItem", action: "showStockCard", id: inventoryItem?.id)
    }

    def buildZebraTemplate = {
        Document document = Document.load(params.id)
        InventoryItem inventoryItem = InventoryItem.load(params.inventoryItem?.id)
        Location location = Location.load(session.warehouse.id)
        Map model = [document: document, inventoryItem: inventoryItem, location: location]
        String renderedContent = templateService.renderTemplate(document, model)
        log.info "renderedContent: ${renderedContent}"
        render(renderedContent)
    }

    def renderZebraTemplate = {

        Document document = Document.load(params.id)
        InventoryItem inventoryItem = InventoryItem.load(params.inventoryItem?.id)
        Location location = Location.load(session.warehouse.id)
        Map model = [document: document, inventoryItem: inventoryItem, location: location]
        String body = templateService.renderTemplate(document, model)
        String contentType = "image/png"

        def http = new HTTPBuilder("http://api.labelary.com/v1/printers/8dpmm/labels/4x6/0/".toString())
        def html = http.post(body: body)

        response.contentType = contentType
        response.outputStream << html
        response.flush()
    }


    def exportZebraTemplate = {
        Document document = Document.load(params.id)
        InventoryItem inventoryItem = InventoryItem.load(params.inventoryItem?.id)
        Location location = Location.load(session.warehouse.id)
        Map model = [document: document, inventoryItem: inventoryItem, location: location]
        String renderedContent = templateService.renderTemplate(document, model)
        String url = "http://labelary.com/viewer.html?zpl=" + renderedContent
        redirect(url: url)
    }
}

/**
 * Command object
 */
class DocumentCommand {
    String name
    String typeId
    String orderId
    String productId
    String requestId
    String shipmentId
    String documentNumber
    MultipartFile fileContents

    static constraints = {
        name(nullable: true)
        fileContents(nullable: false)
    }

}

