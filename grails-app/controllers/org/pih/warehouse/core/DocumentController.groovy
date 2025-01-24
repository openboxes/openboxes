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

import fr.opensagres.xdocreport.converter.ConverterTypeTo
import fr.w3blog.zpl.utils.ZebraUtils
import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.validation.Validateable
import org.apache.http.client.fluent.Request
import org.apache.http.entity.ContentType
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.OutboundStockMovementService
import org.pih.warehouse.inventory.StockMovementService
import org.pih.warehouse.invoice.Invoice
import org.pih.warehouse.order.Order
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentWorkflow
import org.springframework.web.multipart.MultipartFile
import util.FileUtil

import static org.springframework.util.StringUtils.stripFilenameExtension

@Transactional
class DocumentController {

    def documentTemplateService
    def fileService
    def shipmentService
    GrailsApplication grailsApplication
    TemplateService templateService
    StockMovementService stockMovementService
    OutboundStockMovementService outboundStockMovementService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list() {

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

    def create() {
        def documentInstance = new Document()
        documentInstance.properties = params
        return [documentInstance: documentInstance]
    }

    def save() {

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

    def show() {
        def documentInstance = Document.get(params.id)
        if (!documentInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'document.label', default: 'Document'), params.id])}"
            redirect(action: "list")
        } else {
            [documentInstance: documentInstance]
        }
    }

    def edit() {
        def documentInstance = Document.get(params.id)
        if (!documentInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'document.label', default: 'Document'), params.id])}"
            redirect(action: "list")
        } else {
            return [documentInstance: documentInstance]
        }
    }

    def update() {
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

    def delete() {
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
    def uploadDocument(DocumentCommand command) {
        log.info "Uploading document: " + params
        def file = command.fileContents

        log.info "multipart file: " + file.originalFilename + " " + file.contentType + " " + file.size + " "

        def shipmentInstance = Shipment.get(command.shipmentId)
        def orderInstance = Order.get(command.orderId)
        def requestInstance = Requisition.get(command.requestId)
        def productInstance = Product.get(command.productId)
        def invoiceInstance = Invoice.get(command.invoiceId)
        def stockMovement = null
        if (command.stockMovementId) {
            stockMovement = outboundStockMovementService.getStockMovement(command.stockMovementId)
            if (!stockMovement) {
                stockMovement =  stockMovementService.getStockMovement(command.stockMovementId)
            }
        }

        // file must not be empty and must be less than 10MB
        // FIXME The size limit needs to go somewhere
        if (!(file?.size || command.fileUri)) {
            flash.message = "${warehouse.message(code: 'document.documentTooLarge.message')}"
        } else if (file.size < 10 * 1024 * 1000) {
            log.info "Creating new document "
            // Document type with id 9 is "Other" and it's default in case there's no document type chosen
            String typeId = command?.typeId ?: Constants.DEFAULT_DOCUMENT_TYPE_ID;
            DocumentType documentType = DocumentType.get(typeId)

            Document documentInstance = new Document(
                    size: file.size,
                    name: command.name ?: file.originalFilename,
                    filename: file.originalFilename,
                    fileContents: command.fileContents.bytes,
                    fileUri: command.fileUri,
                    contentType: file.contentType,
                    extension: FileUtil.getExtension(file.originalFilename),
                    documentNumber: command.documentNumber,
                    documentType: documentType)

            documentInstance.validate()

            List<DocumentCode> forbiddenDocumentCodes = DocumentCode.templateList()
            if (documentType && forbiddenDocumentCodes.contains(documentType.documentCode)) {
                documentInstance.errors.reject("documentType", "Template types are not allowed for this document upload")
            }

            // Check to see if there are any errors
            if (!documentInstance.hasErrors()) {
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
                } else if (productInstance) {
                    productInstance.addToDocuments(documentInstance).save(flush: true)
                    flash.message = "${warehouse.message(code: 'document.succesfullyUpdatedDocument.message')}"
                } else if (invoiceInstance) {
                    invoiceInstance.addToDocuments(documentInstance).save(flush: true)
                    flash.message = "${warehouse.message(code: 'document.successfullySavedToInvoice.message', args: [invoiceInstance?.name])}"
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
                } else if (productInstance) {
                    redirect(controller: "product", action: "edit", id: productInstance.id)
                    return
                } else if (invoiceInstance) {
                    redirect(controller: "invoice", action: "addDocument", id: invoiceInstance.id,
                            model: [invoiceInstance: invoiceInstance, documentInstance: documentInstance])
                    return
                }
            }
        } else {
            log.info "Document is too large"
            flash.message = "${warehouse.message(code: 'document.documentTooLarge.message')}"
            if (stockMovement) {
                redirect(controller: 'stockMovement', action: 'show', id: stockMovement.id)
                return
            }
            if (shipmentInstance) {
                redirect(controller: 'stockMovement', action: 'show', id: command.shipmentId)
                return
            } else if (orderInstance) {
                redirect(controller: 'order', action: 'show', id: command.orderId)
                return
            } else if (requestInstance) {
                redirect(controller: 'requisition', action: 'show', id: command.requestId)
                return
            } else if (productInstance) {
                redirect(controller: 'product', action: 'edit', id: command.productId)
                return
            } else if (invoiceInstance) {
                redirect(controller: 'invoice', action: 'show', id: command.invoiceId)
                return
            }
        }

        // This is, admittedly, a hack but I wanted to avoid having to add this code to each of
        // these controllers.
        log.info("Redirecting to appropriate show details page")
        if (stockMovement) {
            redirect(controller: 'stockMovement', action: 'show', id: stockMovement.id)
            return
        }
        if (shipmentInstance) {
            redirect(controller: 'stockMovement', action: 'show', id: command.shipmentId)
            return
        } else if (orderInstance) {
            redirect(controller: 'order', action: 'show', id: command.orderId)
            return
        } else if (requestInstance) {
            redirect(controller: 'requisition', action: 'show', id: command.requestId)
            return
        } else if (productInstance) {
            redirect(controller: 'product', action: 'edit', id: command.productId)
            return
        } else if (invoiceInstance) {
            redirect(controller: 'invoice', action: 'show', id: command.invoiceId)
            return
        }
    }

    /**
     * @deprecated
     */
    def upload() {

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
    def download() {
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

    def preview() {
        def documentInstance = Document.get(params.id)
        render(template: "preview", model: [documentInstance: documentInstance])
    }


    def render() {
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

    def renderInvoiceTemplate() {
        Shipment shipmentInstance = Shipment.get(params.shipmentId)

        if (!shipmentInstance) {
            throw new Exception("Unable to locate shipment with ID ${params.id}")
        }

        // If we get the id of document, then render it otherwise find invoice template on shipment workflow
        Document documentInstance
        if (params.id) {
            documentInstance = Document.get(params.id)
        } else {
            ShipmentWorkflow shipmentWorkflow = shipmentService.getShipmentWorkflow(shipmentInstance)
            documentInstance = shipmentWorkflow.documentTemplates?.find {it.documentType?.documentCode == DocumentCode.INVOICE_TEMPLATE}
        }

        if (!documentInstance) {
            throw new Exception("Unable to locate document with type ${DocumentCode.INVOICE_TEMPLATE}. Please add ${DocumentCode.INVOICE_TEMPLATE} document to shipment workflow.")
        }

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
            documentTemplateService.renderInvoiceTemplate(documentInstance, shipmentInstance, outputStream)
            response.setHeader("Content-disposition",
                "attachment; filename=\"${stripFilenameExtension(documentInstance.filename)}-${shipmentInstance.shipmentNumber}.${documentInstance.extension}\"")
            response.setContentType(documentInstance.contentType)
            outputStream.writeTo(response.outputStream)
            response.outputStream.flush()
        } catch (Exception e) {
            log.error("Unable to render document template ${documentInstance.name} for shipment ${shipmentInstance?.id}", e)
            throw e
        }
    }

    def renderRequisitionTemplate = {
        def requisitionInstance = Requisition.get(params.id)
        if (!requisitionInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'requisition.label', default: 'Requisition'), params.id])}"
            redirect(action: "list")
        } else {
            if (!params?.documentTemplate?.id) {
                throw new IllegalArgumentException("documentTemplate.id is required")
            }
            Document documentTemplate = Document.get(params?.documentTemplate?.id)
            if (documentTemplate) {
                try {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
                    ConverterTypeTo targetDocumentType = params.format ? params.format as ConverterTypeTo : null
                    documentTemplateService.renderRequisitionDocumentTemplate(documentTemplate,
                        requisitionInstance, targetDocumentType, outputStream)

                    // Set response headers appropriately
                    if (targetDocumentType) {
                        // Use the appropriate content type and extension of the conversion type
                        // (except XHTML, just render as HTML response)
                        if (targetDocumentType != ConverterTypeTo.XHTML) {
                            response.setHeader("Content-disposition",
                                "attachment; filename=\"${documentTemplate.name}\"-${requisitionInstance.requestNumber}.${targetDocumentType.extension}");
                            response.setContentType(targetDocumentType.mimeType)
                        }
                    }
                    else {
                        // Otherwise write processed document to response using the original
                        // document template's extension and content type
                        response.setHeader("Content-disposition",
                            "attachment; filename=\"${documentTemplate.name}\"-${requisitionInstance.requestNumber}.${documentTemplate.extension}");
                        response.setContentType(documentTemplate.contentType)
                    }
                    outputStream.writeTo(response.outputStream)
                    return
                } catch (Exception e) {
                    log.error("Unable to render document template ${documentTemplate.name} for requisition ${requisitionInstance?.id}", e)
                    throw e;
                }
            }
        }
        [requisitionInstance:requisitionInstance]
    }

    /**
     * Saves changes to document metadata (or, more specifically, saves changes to metadata--type,name,documentNumber--associated with
     * a document without modifying the document itself--the upload method handles this)
     */
    def saveDocument(DocumentCommand command) {
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
            } else if (command.invoiceId) {
                redirect(controller: 'invoice', action: 'show', id: command.invoiceId)
            }
        } else {
            if (command.shipmentId) {
                redirect(controller: "shipment", action: "addDocument", id: command.shipmentId,
                        model: [shipmentInstance: Shipment.get(command.shipmentId), documentInstance: documentInstance])
            } else if (command.orderId) {
                redirect(controller: "order", action: "addDocument", id: command.orderId,
                        model: [orderInstance: Order.get(command.orderId), documentInstance: documentInstance])

            } else if (command.invoiceId) {
                redirect(controller: "invoice", action: "addDocument", id: command.invoiceId,
                        model: [invoiceInstance: Invoice.get(command.invoiceId), documentInstance: documentInstance])

            }
        }
    }

    def printZebraTemplate() {
        Document document = Document.load(params.id)
        Location location = Location.load(session.warehouse.id)
        InventoryItem inventoryItem = InventoryItem.load(params?.inventoryItem?.id)

        Map model = [document: document, inventoryItem: inventoryItem, location: location]
        String renderedContent = templateService.renderTemplate(document, model)

        try {
            if (params.protocol=="usb") {
                String printerName = grailsApplication.config.openboxes.barcode.printer.name
                log.info "Printing ZPL to ${printerName}: ${renderedContent} "
                ZebraUtils.printZpl(renderedContent, printerName)
            }
            else if (params.protocol == "raw") {
                String ipAddress = grailsApplication.config.openboxes.barcode.printer.ipAddress
                Integer port = grailsApplication.config.openboxes.barcode.printer.port
                log.info "Printing ${renderedContent} to ${ipAddress}:${port}"
                ZebraUtils.printZpl(renderedContent, ipAddress, port)
            }
            else {
                throw new IllegalArgumentException("Must specify printing protocol or configure default")
            }

            flash.message = "Label printed successfully"
        }
        catch (Exception e) {
            flash.message = e.message
        }

        redirect(controller: "inventoryItem", action: "showStockCard", id: inventoryItem?.product?.id)
    }

    def buildZebraTemplate() {
        Document document = Document.load(params.id)
        InventoryItem inventoryItem = InventoryItem.load(params.inventoryItem?.id)
        Location location = Location.load(session.warehouse.id)
        Map model = [document: document, inventoryItem: inventoryItem, location: location]
        String renderedContent = templateService.renderTemplate(document, model)
        log.info "renderedContent: ${renderedContent}"
        render(renderedContent)
    }

    def renderZebraTemplate() {
        Document document = Document.load(params.id)
        InventoryItem inventoryItem = InventoryItem.load(params.inventoryItem?.id)
        Location location = Location.load(session.warehouse.id)
        Map model = [document: document, inventoryItem: inventoryItem, location: location]
        String body = templateService.renderTemplate(document, model)

        response.contentType = 'image/png'
        // TODO Move labelary URL to application.yml
        response.outputStream << Request.Post('http://api.labelary.com/v1/printers/8dpmm/labels/4x6/0/')
            .bodyString(body, ContentType.APPLICATION_FORM_URLENCODED)
            .execute()
            .returnContent()
            .asStream()
    }


    def exportZebraTemplate() {
        Document document = Document.load(params.id)
        InventoryItem inventoryItem = InventoryItem.load(params.inventoryItem?.id)
        Location location = Location.load(session.warehouse.id)
        Map model = [document: document, inventoryItem: inventoryItem, location: location]
        String renderedContent = templateService.renderTemplate(document, model)
        // TODO Move labelary URL to application.yml
        String url = "http://labelary.com/viewer.html?zpl=" + renderedContent
        redirect(url: url)
    }

}

/**
 * Command object
 */
class DocumentCommand implements Validateable {
    String name
    String typeId
    String invoiceId
    String orderId
    String productId
    String requestId
    String shipmentId
    String documentNumber
    MultipartFile fileContents
    String fileUri
    String stockMovementId

    static constraints = {
        name(nullable: true)
        fileContents(nullable: true)
        stockMovementId(nullable: true)
    }
}

/**
 * Command object (for bulk upload)
 */
class BulkDocumentCommand extends DocumentCommand {
    List<MultipartFile> filesContents

    static constraints = {
        name(nullable: true)
        fileContents(nullable: true)
        filesContents(nullable: false)
    }
}
