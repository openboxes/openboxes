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
import grails.gorm.PagedResultList
import grails.validation.Validateable
import org.grails.gsp.GroovyPagesTemplateEngine
import org.hibernate.SessionFactory
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.core.localization.MessageLocalizer
import org.pih.warehouse.inventory.OutboundStockMovementService
import org.pih.warehouse.inventory.StockMovementService
import org.pih.warehouse.invoice.Invoice
import org.pih.warehouse.order.Order
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentWorkflow
import org.springframework.beans.BeansException
import org.springframework.web.multipart.MultipartFile
import util.FileUtil
import util.RedirectUtil

import static org.springframework.util.StringUtils.stripFilenameExtension

@Transactional
class DocumentController {

    DocumentService documentService
    def documentTemplateService
    MessageLocalizer messageLocalizer
    def fileService
    def shipmentService
    ZebraService zebraService
    def userService
    SessionFactory sessionFactory
    GroovyPagesTemplateEngine groovyPagesTemplateEngine
    GrailsApplication grailsApplication
    BeanPropertyTemplateService beanPropertyTemplateService
    StockMovementService stockMovementService
    OutboundStockMovementService outboundStockMovementService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    private static final String OCTET_STREAM = "application/octet-stream"

    def index() {
        redirect(action: "list", params: params)
    }

    def list(DocumentFilterCommand command) {

        log.info "params: " + params

        PagedResultList<Document> documentInstanceList = documentService.getDocuments(command)

        [documentInstanceList: documentInstanceList, documentInstanceTotal: documentInstanceList.totalCount]
    }

    def create() {
        def documentInstance = new Document()
        documentInstance.properties = params
        return [documentInstance: documentInstance]
    }

    def save() {

        log.info "Params " + params
        // The file and what describes it come only from the uploaded file below
        Document documentInstance = new Document()
        bindData(documentInstance, params, [exclude: ['fileContents', 'filename', 'extension', 'contentType', 'documentType']])
        documentInstance.documentType = requestedDocumentType()
        if (!canChangeDataExport(documentInstance.documentType)) {
            refuseDataExportChange(null)
            return
        }

        MultipartFile file = request.getFile("fileContents")
        // file must not be empty and must be less than 10MB
        if (!file || file?.isEmpty()) {
            flash.message = "${warehouse.message(code: 'document.documentCannotBeEmpty.message')}"
            render(view: "create", model: [documentInstance: documentInstance])
            return
        }
        // Templates and data exports are created here with their own file types; other documents get the upload check
        else if (!isTemplateType(documentInstance.documentType) && !isAllowedUpload(file)) {
            flash.message = uploadNotAllowedMessage()
            render(view: "create", model: [documentInstance: documentInstance])
            return
        }
        // FIXME The size limit should be configurable
        else if (file.size >= 10 * 1024 * 1000) {
            flash.message = "${warehouse.message(code: 'document.documentTooLarge.message')}"
            render(view: "create", model: [documentInstance: documentInstance])
            return
        }
        else {

            documentInstance.name = file.originalFilename
            documentInstance.filename = file.originalFilename
            documentInstance.fileContents = file.bytes
            documentInstance.extension = FileUtil.getExtension(file.originalFilename)
            documentInstance.contentType = file.contentType
            // A template file of a type uploads do not accept is stored as plain bytes, never as a page or an image
            if (!Document.isAllowedFile(file.originalFilename, file.contentType)) {
                documentInstance.contentType = OCTET_STREAM
            }
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

            DocumentType requestedType = requestedDocumentType()
            if (!canChangeDataExport(documentInstance.documentType, requestedType)) {
                refuseDataExportChange(documentInstance)
                return
            }
            // The form edits the extension and content type as text: a change must give a file type uploads accept
            String extension = (params.containsKey('extension') ? params.extension : documentInstance.extension) ?: null
            String contentType = (params.containsKey('contentType') ? params.contentType : documentInstance.contentType) ?: null
            boolean fileTypeChanged = extension != (documentInstance.extension ?: null) || contentType != (documentInstance.contentType ?: null)
            if (fileTypeChanged && !Document.isAllowedFile("file.${extension}", contentType)) {
                flash.message = uploadNotAllowedMessage()
                redirect(action: "edit", id: documentInstance.id)
                return
            }

            // The file itself is replaced only through upload, which checks it. The type is chosen by its id only:
            // the document type itself is not changed from here
            bindData(documentInstance, params, [exclude: ['fileContents', 'documentType']])
            if (params.containsKey('documentType.id')) {
                documentInstance.documentType = requestedType
            }

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
            flash.message = "${warehouse.message(code: 'document.documentCannotBeEmpty.message')}"
        // Validate the file type only if it's an actual file upload (if it has size), not if it's only an URI (stored as a link)
        } else if (file && file.size && !isAllowedUpload(file)) {
            flash.message = uploadNotAllowedMessage()
        } else if (!file || file.size < 10 * 1024 * 1000) {
            log.info "Creating new document "
            // Document type with id 9 is "Other" and it's default in case there's no document type chosen
            String typeId = command?.typeId ?: Constants.DEFAULT_DOCUMENT_TYPE_ID;
            DocumentType documentType = DocumentType.get(typeId)

            Document documentInstance = new Document(
                    size: file?.size,
                    name: command.name ?: file?.originalFilename,
                    filename: file?.originalFilename,
                    fileContents: command.fileContents?.bytes,
                    fileUri: command.fileUri,
                    contentType: file?.contentType,
                    extension: file?.originalFilename ? FileUtil.getExtension(file.originalFilename) : null,
                    documentNumber: command.documentNumber,
                    documentType: documentType)

            documentInstance.validate()

            if (isTypeNotForRecords(documentType)) {
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

            if (!canChangeDataExport(documentInstance.documentType)) {
                refuseDataExportChange(documentInstance)
                return
            }

            MultipartFile multipartFile = request.getFile("fileContents")
            if (!multipartFile || multipartFile.empty) {
                flash.message = "${g.message(code: 'file')}"
            } else if (!isAllowedReplacement(documentInstance, multipartFile)) {
                flash.message = keepsStoredFileType(documentInstance) ?
                        messageLocalizer.localize('document.replacementTypeNotAllowed.message', [storedExtension(documentInstance)] as Object[]) :
                        uploadNotAllowedMessage()
                redirect(action: "edit", id: documentInstance.id)
                return
            } else {
                boolean keepFileType = keepsStoredFileType(documentInstance)

                // Only change the name if it was never modified from the original filename
                if (documentInstance.filename == documentInstance.name) {
                    documentInstance.name = multipartFile.originalFilename
                }

                documentInstance.filename = multipartFile.originalFilename
                documentInstance.fileContents = multipartFile.bytes
                // A template keeps its stored extension and content type; a content type uploads do not accept
                // for that extension becomes plain bytes
                if (keepFileType) {
                    if (!Document.isAllowedFile("file.${storedExtension(documentInstance)}", documentInstance.contentType)) {
                        documentInstance.contentType = OCTET_STREAM
                    }
                } else {
                    documentInstance.extension = FileUtil.getExtension(multipartFile.originalFilename)
                    documentInstance.contentType = multipartFile.contentType
                    // A template file of a type uploads do not accept is stored as plain bytes
                    if (isTemplateType(documentInstance.documentType) &&
                            !Document.isAllowedFile(multipartFile.originalFilename, multipartFile.contentType)) {
                        documentInstance.contentType = OCTET_STREAM
                    }
                }
            }

            if (!documentInstance.hasErrors() && documentInstance.save(flush: true)) {

                // Required in order to refresh the GSP template cache (primarily for barcode label templates)
                groovyPagesTemplateEngine.clearPageCache()

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
        if (documentInstance?.documentType?.documentCode == DocumentCode.DATA_EXPORT) {
            render(controller: "dataExport", action: "render", id: documentInstance.id)
            return
        }
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

        // Refuse what uploadDocument refuses before anything changes on the document
        MultipartFile multipartFile = command.fileContents
        if (multipartFile && !multipartFile.empty && !isAllowedUpload(multipartFile)) {
            flash.message = uploadNotAllowedMessage()
            redirectToAddDocument(command, documentInstance)
            return
        }
        DocumentType requestedType = DocumentType.get(command.typeId)
        if (isTypeNotForRecords(documentInstance.documentType) || isTypeNotForRecords(requestedType)) {
            flash.message = messageLocalizer.localize('document.templateTypeNotAllowed.message')
            redirectToAddDocument(command, documentInstance)
            return
        }

        // bind the command object to the document object ignoring the shipmentId and fileContents params (which can't change after creation)
        //bindData(documentInstance, command, ['shipmentId','orderId'])
        // manually update the document type
        documentInstance.name = command.name
        documentInstance.documentNumber = command.documentNumber
        documentInstance.documentType = requestedType

        // If a new file is passed we should update all of the read-only properties
        if (multipartFile && !multipartFile.empty) {
            documentInstance.name = command.name ?: multipartFile.originalFilename
            documentInstance.filename = multipartFile.originalFilename
            documentInstance.fileContents = multipartFile.bytes
            documentInstance.extension = FileUtil.getExtension(multipartFile.originalFilename)
            documentInstance.contentType = multipartFile.contentType
        }

        if (!documentInstance.hasErrors()) {
            flash.message = "${warehouse.message(code: 'document.succesfullyUpdatedDocument.message')}"
            if (command.shipmentId) {
                redirect(controller: 'shipment', action: 'showDetails', id: command.shipmentId)
            } else if (command.orderId) {
                redirect(controller: 'order', action: 'show', id: command.orderId)
            } else if (command.invoiceId) {
                redirect(controller: 'invoice', action: 'show', id: command.invoiceId)
            } else if (command.requestId) {
                redirect(controller: 'requisition', action: 'show', id: command.requestId)
            }
        } else {
            redirectToAddDocument(command, documentInstance)
        }
    }

    private void redirectToAddDocument(DocumentCommand command, Document documentInstance) {
        if (command.shipmentId) {
            redirect(controller: "shipment", action: "addDocument", id: command.shipmentId,
                    model: [shipmentInstance: Shipment.get(command.shipmentId), documentInstance: documentInstance])
        } else if (command.orderId) {
            redirect(controller: "order", action: "addDocument", id: command.orderId,
                    model: [orderInstance: Order.get(command.orderId), documentInstance: documentInstance])
        } else if (command.invoiceId) {
            redirect(controller: "invoice", action: "addDocument", id: command.invoiceId,
                    model: [invoiceInstance: Invoice.get(command.invoiceId), documentInstance: documentInstance])
        } else if (command.requestId) {
            redirect(controller: "requisition", action: "show", id: command.requestId)
        } else {
            redirect(action: "edit", id: documentInstance.id)
        }
    }

    /**
     * The file check every path that stores an uploaded file applies (the same as uploadDocument): the file's
     * name, type and content must agree on a type documents accept.
     */
    private static boolean isAllowedUpload(MultipartFile file) {
        return Document.isAllowedUpload(file)
    }

    private String uploadNotAllowedMessage() {
        return messageLocalizer.localize('document.uploadNotAllowed.message', [Document.allowedExtensions().join(', ')] as Object[])
    }

    /**
     * Templates and data exports are kept up to date by replacing their file on the document page, and their files
     * (label templates, queries) are not of the types an upload accepts. A replacement for one of them must have
     * the same extension as the stored file, and the stored extension and content type are kept.
     */
    private static boolean keepsStoredFileType(Document document) {
        return isTemplateType(document.documentType) && storedExtension(document)
    }

    private static boolean isAllowedReplacement(Document document, MultipartFile file) {
        if (keepsStoredFileType(document)) {
            String extension = storedExtension(document)
            boolean sameExtension = file.originalFilename?.contains('.') &&
                    FileUtil.getExtension(file.originalFilename).equalsIgnoreCase(extension)
            // A file type uploads accept (a .docx template, say) also gets the content check, with the stored type
            if (sameExtension && Document.isAllowedFile("file.${extension}", document.contentType)) {
                return Document.isAllowedFile(file.originalFilename, document.contentType, file.inputStream)
            }
            return sameExtension
        }
        // A template with no known file type takes any file; it is stored as plain bytes unless uploads accept it
        return isTemplateType(document.documentType) || isAllowedUpload(file)
    }

    /** The document type chosen on the document form (documentType.id), if any. */
    private DocumentType requestedDocumentType() {
        String id = params['documentType.id']
        return id ? DocumentType.get(id) : null
    }

    /** The stored file's extension, from the document or else from its filename. */
    private static String storedExtension(Document document) {
        if (document.extension) {
            return document.extension
        }
        return document.filename?.contains('.') ? FileUtil.getExtension(document.filename) : null
    }

    /** Templates and data exports: documents whose content the application interprets. */
    private static boolean isTemplateType(DocumentType documentType) {
        DocumentCode documentCode = documentType?.documentCode
        return documentCode && (DocumentCode.templateList().contains(documentCode) || documentCode == DocumentCode.DATA_EXPORT)
    }

    /** Types a document attached to a shipment, order, request, product or invoice cannot have. */
    private static boolean isTypeNotForRecords(DocumentType documentType) {
        return isTemplateType(documentType)
    }

    /** Data export queries run as SQL: creating or changing one needs a superuser, as dataExport/update does. */
    private boolean canChangeDataExport(DocumentType... documentTypes) {
        boolean isDataExport = documentTypes.any { it?.documentCode == DocumentCode.DATA_EXPORT }
        return !isDataExport || userService.isSuperuser(session.user)
    }

    private void refuseDataExportChange(Document documentInstance) {
        flash.message = messageLocalizer.localize('document.dataExportNotAllowed.message')
        if (documentInstance?.id) {
            redirect(action: "edit", id: documentInstance.id)
        } else {
            redirect(action: "list")
        }
    }

    def printZebraTemplate() {
        Document document = loadZebraTemplate(params.id)
        Product product = Product.get(params?.product?.id)
        Location facility = Location.get(session.warehouse.id)
        Location location = Location.get(params?.location?.id)
        InventoryItem inventoryItem = InventoryItem.get(params?.inventoryItem?.id)

        Map context = [document: document, inventoryItem: inventoryItem, product: inventoryItem?.product?:product, facility: facility, location: location]
        String renderedContent
        try {
            renderedContent = renderLabelTemplate(document, context)
        } catch (LabelTemplateException e) {
            log.warn(e.message)
            flash.message = e.message
            RedirectUtil.redirect(this, [location: location, product: product])
            return
        }

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

        RedirectUtil.redirect(this, [location: location, product: product])
    }

    def buildZebraTemplate() {
        Document document = loadZebraTemplate(params.id)
        Product product = Product.get(params?.product?.id)
        InventoryItem inventoryItem = InventoryItem.get(params?.inventoryItem?.id)
        Location facility = Location.get(session.warehouse.id)
        Location location = Location.get(params?.location?.id)
        Map model = [document: document, inventoryItem: inventoryItem, product: product?:inventoryItem?.product, facility: facility, location: location]
        String renderedContent
        try {
            renderedContent = renderLabelTemplate(document, model)
        } catch (LabelTemplateException e) {
            renderLabelTemplateError(e)
            return
        }
        log.info "renderedContent: ${renderedContent}"
        response.setHeader("Content-disposition", "attachment; filename=\"${product?.productCode?:location?.locationNumber}.zpl\"")
        render(contentType: "text/csv", text: renderedContent, encoding: "UTF-8")
    }

    def renderZebraTemplate() {
        Document document = loadZebraTemplate(params.id)
        Product product = Product.get(params?.product?.id)
        InventoryItem inventoryItem = InventoryItem.get(params?.inventoryItem?.id)
        Location facility = Location.get(session.warehouse.id)
        Location location = Location.get(params?.location?.id)
        Map model = [document: document, inventoryItem: inventoryItem, product: product?:inventoryItem?.product, facility: facility, location: location]
        String body
        try {
            body = renderLabelTemplate(document, model)
        } catch (LabelTemplateException e) {
            renderLabelTemplateError(e)
            return
        }

        InputStream image
        try {
            image = zebraService.renderPreview(body)
        } catch (LabelPreviewDisabledException e) {
            renderLabelPreviewDisabled(e)
            return
        }
        response.contentType = 'image/png'
        response.outputStream << image
    }


    def exportZebraTemplate() {
        Document document = loadZebraTemplate(params.id)
        Product product = Product.get(params?.product?.id)
        InventoryItem inventoryItem = InventoryItem.load(params?.inventoryItem?.id)
        Location facility = Location.get(session.warehouse.id)
        Location location = Location.get(params?.location?.id)
        Map model = [document: document, inventoryItem: inventoryItem, product: product?:inventoryItem?.product, facility: facility, location: location]
        String renderedContent
        try {
            renderedContent = renderLabelTemplate(document, model)
        } catch (LabelTemplateException e) {
            renderLabelTemplateError(e)
            return
        }
        String url
        try {
            url = zebraService.getViewerUrl(renderedContent)
        } catch (LabelPreviewDisabledException e) {
            renderLabelPreviewDisabled(e)
            return
        }
        redirect(url: url)
    }

    private String renderLabelTemplate(Document document, Map model) {
        try {
            return beanPropertyTemplateService.renderTemplate(document, model)
        } catch (IllegalArgumentException | BeansException e) {
            throw new LabelTemplateException(document, e)
        }
    }

    private void renderLabelTemplateError(LabelTemplateException e) {
        log.warn(e.message)
        response.status = 400
        render(contentType: "text/plain", text: e.message, encoding: "UTF-8")
    }

    private void renderLabelPreviewDisabled(LabelPreviewDisabledException e) {
        response.status = 503
        render(contentType: "text/plain", text: e.message, encoding: "UTF-8")
    }

    private Document loadZebraTemplate(Serializable id) {
        Document document = Document.get(id)
        if (document == null) {
            throw new ObjectNotFoundException(id, Document.name)
        }
        if (document.documentType?.documentCode != DocumentCode.ZEBRA_TEMPLATE) {
            throw new IllegalArgumentException("Only documents of type ZEBRA_TEMPLATE can be rendered as Zebra templates")
        }
        return document
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
