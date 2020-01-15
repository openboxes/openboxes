/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.product

import com.google.zxing.BarcodeFormat
import grails.converters.JSON
import grails.validation.ValidationException
import org.apache.commons.io.FilenameUtils
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.hibernate.Criteria
import org.pih.warehouse.core.Document
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.MailService
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.Synonym
import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.UploadService
import org.pih.warehouse.core.User
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryLevel
import org.springframework.web.servlet.support.RequestContextUtils as RCU

import javax.activation.MimetypesFileTypeMap

class ProductController {

    def dataService
    def userService
    MailService mailService
    def productService
    def documentService
    def inventoryService
    def barcodeService
    UploadService uploadService

    static allowedMethods = [save: "POST", update: "POST"]


    def index = {
        redirect(action: "list", params: params)
    }

    def redirect = {
        log.info("Redirecting to product " + params.id)
        redirect(controller: "inventoryItem", action: "showStockCard", id: params.id)
    }

    /**
     * Perform a bulk update of
     */
    def batchEdit = { BatchEditCommand cmd ->
        def startTime = System.currentTimeMillis()
        //	def location = Location.get(session.warehouse.id)
        def category = Category.get(params.categoryId)
        def tagIds = params.list("tagId")

        log.info "Batch edit: " + params

        if (category || tagIds)
            cmd.productInstanceList = productService.getProducts(category, tagIds, params)

        cmd.productInstanceList.eachWithIndex { product, index ->
            println product.category
            cmd.categoryInstanceList << product.category
        }
        cmd.rootCategory = productService.getRootCategory()

        println "batch edit products: " + (System.currentTimeMillis() - startTime) + " ms"

        [commandInstance: cmd, products: cmd.productInstanceList ?: [], categoryInstance: category]
    }

    def batchEditProperties = {
        def startTime = System.currentTimeMillis()

        println "batch edit products: " + (System.currentTimeMillis() - startTime) + " ms"

        [products: product]
    }


    def batchSave = { BatchEditCommand cmd ->

        println "Batch save " + cmd

        // If there are no products (usually when returning to batchSave after login
        if (!cmd.productInstanceList) {
            redirect(action: 'batchEdit')
        }
        // We needed to hack the category binding in order to make this work.
        // When changing the product.category directly, we received an error
        // from Hibernate stating that we were trying to change the primary key
        // of the category object.
        cmd.categoryInstanceList.eachWithIndex { cat, i ->
            log.info "categoryInstanceList[" + i + "]: " + cat
            cmd.productInstanceList[i].category = Category.get(cat.id)
        }

        cmd.productInstanceList.eachWithIndex { product, i ->
            log.info "productInstanceList[" + i + "]: " + product.category
            if (!product.hasErrors() && product.save()) {
                // saved with no errors
            } else {
                // copy the errors from this product on to the overall command object errors
                product.errors.getAllErrors().each {
                    cmd.errors.reject(it.getCode(), it.getDefaultMessage())
                }
            }
        }

        if (!cmd.hasErrors()) {
            flash.message = "${warehouse.message(code: 'product.allSavedSuccessfully.message')}"
            chain(controller: "product", action: "batchEdit", params: params)
        } else {

            def category = Category.get(params.categoryId)
            def tagIds = params.list("tagId")
            def products = productService.getProducts(category, tagIds, params)

            render(view: "batchEdit", model: [commandInstance: cmd, products: products])
        }

        println "flash " + flash.message
        println "params " + params
    }

    def list = {
        def productInstanceList = []
        def productInstanceTotal = 0

        params.max = Math.min(params.max ? params.int('max') : 10, 100)


        boolean includeInactive = params.boolean('includeInactive') ?: false
        def category = params.categoryId ? Category.load(params.categoryId) : null
        def tags = params.tagId ? Tag.getAll(params.list("tagId")) : []
        def catalogs = params.catalogId ? ProductCatalog.getAll(params.list("catalogId")) : []
        params.name = params.q
        params.description = params.q
        params.brandName = params.q
        params.manufacturer = params.q
        params.manufacturerCode = params.q
        params.vendor = params.q
        params.vendorCode = params.q
        params.productCode = params.q
        params.unitOfMeasure = params.q

        // If we specify a format (e.g. csv) we probably want to download everything
        if (params.format) {
            params.max = -1
        }

        productInstanceList = productService.getProducts(category, catalogs, tags, includeInactive, params)

        if (params.format) {
            def date = new Date()
            response.setHeader("Content-disposition",
                    "attachment; filename=\"Products-${date.format("yyyyMMdd-hhmmss")}.csv\"")
            response.contentType = "text/csv"
            def csv = productService.exportProducts(productInstanceList)
            render csv
            return
        }

        [productInstanceList: productInstanceList, productInstanceTotal: productInstanceList.totalCount]
    }


    def create = {
        def startTime = System.currentTimeMillis()
        def productInstance = new Product(params)
        def rootCategory = productService.getRootCategory()

        println "Create product: " + (System.currentTimeMillis() - startTime) + " ms"

		render(view: "edit", model: [productInstance : productInstance, rootCategory: rootCategory])
        println "After render create.gsp for product: " + (System.currentTimeMillis() - startTime) + " ms"
    }

    def save = {
        println "Save product: " + params


        def productInstance = new Product()
        productInstance.properties = params

        updateTags(productInstance, params)

        // Need to validate here FIRST otherwise we'll run into an uncaught transient property exception
        // when the session is closed.
        if (!productInstance?.id || productInstance.validate()) {
            if (!productInstance.productCode) {
                productInstance.productCode = productService.generateProductIdentifier()
            }
        }

        if (!productInstance.hasErrors() && productInstance.save(flush: true)) {
            log.info("saved product " + productInstance.errors)
            def warehouseInstance = Location.get(session.warehouse.id)
            def inventoryInstance = warehouseInstance?.inventory
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'product.label', default: 'Product'), format.product(product: productInstance)])}"
            sendProductCreatedNotification(productInstance)
        }

        render(view: "edit", model: [productInstance: productInstance, rootCategory: productService.getRootCategory()])
    }


    def show = {}


    def edit = {

        def productInstance = Product.get(params.id)
        def location = Location.get(session?.warehouse?.id)
        if (!productInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'product.label', default: 'Product'), params.id])}"
            redirect(controller: "inventory", action: "browse")
        } else {
            def inventoryLevelInstance = InventoryLevel.findByProductAndInventory(productInstance, location.inventory)
            if (!inventoryLevelInstance) {
                inventoryLevelInstance = new InventoryLevel()
            }
			[productInstance: productInstance, inventoryInstance: location.inventory, inventoryLevelInstance:inventoryLevelInstance]
        }
    }


    def productSuppliers = {

        def productInstance = Product.get(params.id)
        if (!productInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'product.label', default: 'Product'), params.id])}"
            redirect(controller: "inventory", action: "browse")
        } else {
            render(template: "productSuppliers", model: [productInstance: productInstance])
        }
    }

    def productSubstitutions = {
        def productInstance = Product.get(params.id)
        if (!productInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'product.label', default: 'Product'), params.id])}"
            redirect(controller: "inventory", action: "browse")
        } else {
            render(template: "productSubstitutions", model: [productInstance: productInstance])
        }
    }


    def update = {
        log.info "Update called with params " + params
        def productInstance = Product.get(params.id)

        if (productInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (productInstance.version > version) {
                    productInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [
                            warehouse.message(code: 'product.label', default: 'Product')] as Object[], "Another user has updated this product while you were editing")
                    render(view: "edit", model: [productInstance: productInstance])
                    return
                }
            }
            productInstance.properties = params

            try {
                updateTags(productInstance, params)
                updateAttributes(productInstance, params)

                log.info("Categories " + productInstance?.categories)

                // find the categories that are marked for deletion
                def _toBeDeleted = productInstance.categories.findAll {
                    (it?.deleted || (it == null))
                }

                log.info("toBeDeleted: " + _toBeDeleted)

                // if there are categories to be deleted remove them all
                if (_toBeDeleted) {
                    productInstance.categories.removeAll(_toBeDeleted)
                }

                // Need to validate here FIRST otherwise we'll run into an uncaught transient property exception
                // when the session is closed.
                if (productInstance.validate()) {
                    if (!productInstance.productCode) {
                        productInstance.productCode = productService.generateProductIdentifier()
                    }
                }


                if (!productInstance.hasErrors() && productInstance.save(failOnError: true, flush: true)) {
                    flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'product.label', default: 'Product'), format.product(product: productInstance)])}"
                    //redirect(controller: "inventoryItem", action: "showStockCard", id: productInstance?.id)
                    redirect(controller: "product", action: "edit", id: productInstance?.id)
                } else {
                    render(view: "edit", model: [productInstance: productInstance])
                }

            } catch (ValidationException e) {
                log.error("Validation error: " + e.message, e)
                // Clear attributes to prevent transient object exception
                productInstance.attributes.clear()
                productInstance = Product.read(params.id)
                productInstance.errors = e.errors
                render view: "edit", model: [productInstance:productInstance]
                return
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'product.label', default: 'Product'), params.id])}"
            redirect(controller: "inventoryItem", action: "browse")


        }
    }

    def updateTags(productInstance, params) {
        // Process product tags
        try {

            def tagList = []
            if (params.tagsToBeAdded) {
                params.tagsToBeAdded.split(",").each { tagText ->
                    Tag tag = Tag.findByTag(tagText)
                    if (!tag) tag = new Tag(tag: tagText)
                    tagList << tag
                }
            }
            println "product.tags: " + productInstance.tags
            println "tagsToBeAdded: " + params.tagsToBeAdded
            println "tags to be persisted " + tagList
            productInstance?.tags?.clear()
            tagList.each { tag ->
                productInstance?.addToTags(tag)
            }
            productInstance?.save()
            println "product.tags: " + productInstance.tags

        } catch (Exception e) {
            log.error("Error occurred: " + e.message)
        }
    }


    def updateAttributes(Product productInstance, Map params) {
        Map existingAtts = new HashMap()
        productInstance.attributes.each() {
            existingAtts.put(it.attribute.id, it)
        }

        // Process attributes
        Attribute.findAllByActive(true).each() {

            String value = params["productAttributes." + it.id + ".value"]
            if (value == "_other" || value == null || value == '') {
                value = params["productAttributes." + it.id + ".otherValue"]
            }

            log.info("Process attribute " + it.name + " = " + value + ", required = ${it.required}, active = ${it.active}")

            if (it.active && it.required && !value) {
                productInstance.errors.rejectValue("attributes", "product.attribute.required",
                        [] as Object[],
                        "Product attribute ${it.name} is required")
                throw new ValidationException("Attribute required", productInstance.errors)
            }

            ProductAttribute existingAttribute = existingAtts.get(it.id)
            if (value) {
                if (!existingAttribute) {
                    existingAttribute = new ProductAttribute("attribute": it, value: value)
                    productInstance.addToAttributes(existingAttribute)
                    productInstance.save()
                } else {
                    existingAttribute.value = value
                    existingAttribute.save()
                }
            } else {
                if (existingAttribute?.attribute?.active) {
                    log.info("removing attribute ${existingAttribute.attribute.name}")
                    productInstance.removeFromAttributes(existingAttribute)
                    existingAttribute.delete()
                    productInstance.save()
                }
            }
        }
    }


    def delete = {
        def productInstance = Product.get(params.id)
        if (productInstance && !productInstance.hasAssociatedTransactionEntriesOrShipmentItems()) {
            try {
                // first we need to delete any inventory items associated with this product
                def items = InventoryItem.findAllByProduct(productInstance)
                items.each {
                    it.delete(flush: true)
                }

                // now delete the actual product
                productInstance.delete(flush: true)

                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'product.label', default: 'Product'), params.id])}"
                redirect(controller: "product", action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'product.label', default: 'Product'), params.id])}"
                redirect(action: "edit", id: params.id)
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'product.label', default: 'Product'), params.id])}"
            redirect(action: "edit", id: params.id)
        }
    }


    def deleteProducts = {
        println "Delete products: " + params
        def productIds = request.getParameterValues("product.id")

        def products = productService.getProducts(productIds)
        if (products) {
            products.each { product ->
                product.delete()
            }
        }
        redirect(controller: "inventory", action: "browse")
    }


    def removePackage = {
        def packageInstance = ProductPackage.get(params.id)
        def productInstance = packageInstance.product
        log.info "" + packageInstance.product
        productInstance.removeFromPackages(packageInstance)
        packageInstance.delete()
        productInstance.save()
        flash.message = "Product package has been deleted"
        redirect(action: "edit", id: productInstance.id)
    }

    def savePackage = {

        println "savePackage: " + params
        def productInstance = Product.get(params.product.id)

        def packageInstance = ProductPackage.get(params.id)
        if (!packageInstance) {
            packageInstance = new ProductPackage(params)
            productInstance.addToPackages(packageInstance)
        } else {
            packageInstance.properties = params
        }

        if (!productInstance.hasErrors() && productInstance.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'package.label', default: 'Product'), packageInstance.name])}"
            redirect(action: "edit", id: productInstance?.id)
        } else {
            def location = Location.get(session.warehouse.id)
			def inventoryLevelInstance = InventoryLevel.findByProductAndInventory(productInstance, location.inventory)
            if (!inventoryLevelInstance) {
                inventoryLevelInstance = new InventoryLevel()
            }

			render(view: "edit", model: [productInstance: productInstance, inventoryLevelInstance: inventoryLevelInstance, packageInstance: packageInstance, rootCategory: productService.getRootCategory()])
        }
    }


    /**
     *
     */
    def importDependencies = {
        redirect(controller: "product", action: "importProducts")
    }


    /**
     * @param userInstance
     * @return
     */
    def sendProductCreatedNotification(Product productInstance) {
        try {
            def recipientList = userService.findUsersByRoleType(RoleType.ROLE_PRODUCT_NOTIFICATION).collect {
                it.email
            }
            if (recipientList) {
                def subject = "${warehouse.message(code: 'email.productCreated.message', args: [productInstance?.name, productInstance?.createdBy?.name])}"
                def body = "${g.render(template: '/email/productCreated', model: [productInstance: productInstance])}"
                mailService.sendHtmlMail(subject, body.toString(), recipientList)
            }
        }
        catch (Exception e) {
            log.error("Error sending product notification email: " + e.message, e)
        }
    }


    def search = {
        log.info "search " + params
        if (params.q) {
            def products = productService.findProducts(URLEncoder.encode(params.q))
            [products: products]
        }
    }

    def barcode = {
        BarcodeFormat format = BarcodeFormat.valueOf(params.format)
        File file = File.createTempFile("barcode-", ".png")
        barcodeService.renderImageToFile(file, params.data, (params.width ?: 125) as int, (params.height ?: 50) as int, format)
        response.contentType = "image/png"
        response.outputStream << file.bytes
        file.delete()
    }


    /**
     * Upload a document to a product.
     */
    def upload = { DocumentCommand command ->
        log.info "Uploading document: " + params

        // HACK - for some reason the Product in document command is not getting bound
        command.product = Product.get(params.product.id)

        if (params.url) {

            Document documentInstance
            try {
                def filename = params.url.tokenize("/")[-1]
                def fileOutputStream = new FileOutputStream(filename)
                def out = new BufferedOutputStream(fileOutputStream)
                out << new URL(params.url).openStream()
                out.close()

                File file = new File(filename)
                def contentType = new MimetypesFileTypeMap().getContentType(file)

                documentInstance = new Document(
                        size: file.size(),
                        name: file.name,
                        filename: file.name,
                        fileContents: file.bytes,
                        contentType: contentType)

                if (documentInstance?.validate() && !documentInstance?.hasErrors()) {
                    log.info "Saving document " + documentInstance
                    command.product.addToDocuments(documentInstance).save(flush: true)
                    flash.message = "${warehouse.message(code: 'document.successfullySavedToProduct.message', args: [command?.product?.name])}"
                }
                // If there are errors, we need to redisplay the document form
                else {
                    log.info "Document did not save " + documentInstance.errors
                    flash.message = "${warehouse.message(code: 'document.cannotSave.message', args: [documentInstance.errors])}"
                    redirect(controller: "product", action: "edit", id: command?.product?.id,
                            model: [productInstance: command?.product, documentInstance: documentInstance])
                    return
                }

            } catch (IOException e) {
                flash.message = "An error occurred while uploading image: " + e.message
                redirect(controller: "product", action: "edit", id: command?.product?.id, model: [productInstance: command?.product])
                return
            }


        } else {
            def file = command.fileContents
            // file must not be empty and must be less than 10MB
            // FIXME The size limit needs to go somewhere
            if (!file || file?.isEmpty()) {
                flash.message = "${warehouse.message(code: 'document.documentCannotBeEmpty.message')}"
            } else if (file.size < 10 * 1024 * 1000) {
                log.info "Creating new document "
                Document documentInstance = new Document(
                        size: file.size,
                        name: file.originalFilename,
                        filename: file.originalFilename,
                        fileContents: file.bytes,
                        contentType: file.contentType,
                        documentNumber: command.documentNumber,
                        documentType: command.documentType)

                if (!command?.product) {
                    log.info "Cannot add document " + documentInstance + "  because product does not exist"
                    flash.message = "${warehouse.message(code: 'document.productDoesNotExist.message')}"
                    redirect(controller: "product", action: "list")
                    return
                } else {

                    // Check to see if there are any errors
                    if (documentInstance.validate() && !documentInstance.hasErrors()) {
                        log.info "Saving document " + documentInstance
                        command.product.addToDocuments(documentInstance).save(flush: true)
                        flash.message = "${warehouse.message(code: 'document.successfullySavedToProduct.message', args: [command?.product?.name])}"
                    }
                    // If there are errors, we need to redisplay the document form
                    else {
                        log.info "Document did not save " + documentInstance.errors
                        flash.message = "${warehouse.message(code: 'document.cannotSave.message', args: [documentInstance.errors])}"
                        redirect(controller: "product", action: "edit", id: command?.product?.id,
                                model: [productInstance: command?.product, documentInstance: documentInstance])
                        return
                    }
                }
            } else {
                log.info "Document is too large"
                flash.message = "${warehouse.message(code: 'document.documentTooLarge.message')}"
            }
        }

        // This is, admittedly, a hack but I wanted to avoid having to add this code to each of
        // these controllers.
        log.info("Redirecting to appropriate show details page " + command?.product?.id)
        redirect(controller: 'product', action: 'edit', id: command?.product?.id)
    }


    def deleteDocument = {
        def productInstance = Product.get(params.product.id)
        if (!productInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'product.label', default: 'Product'), params.product.id])}"
            redirect(action: "list")
        } else {
            def documentInstance = Document.get(params?.id)
            if (!documentInstance) {
                flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'document.label', default: 'Document'), params.id])}"
                redirect(action: "edit", id: productInstance?.id)
            } else {
                productInstance.removeFromDocuments(documentInstance)
                documentInstance?.delete()
                if (!productInstance.hasErrors() && productInstance.save(flush: true)) {
                    flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'product.label', default: 'Product'), productInstance.id])}"
                    redirect(action: "edit", id: productInstance?.id)
                } else {
                    render(view: "edit", model: [productInstance: productInstance])
                }
            }
        }
    }


    def upnDatabase = {

        def file = new File("/home/jmiranda/Dropbox/OpenBoxes/Product Databases/HIBCC/UPNDownload.txt")
        def rows = []
        try {
            def line = ""
            file.withReader { reader ->
                while ((line = reader.readLine()) != null) {
                    rows << [
                            line                  : line,
                            upn                   : line[0..19].trim(),
                            supplier              : line[20..54].trim(),
                            division              : line[55..89].trim(),
                            tradeName             : line[90..124].trim(),
                            description           : line[125..204].trim(),
                            uom                   : line[205..206].trim(),
                            qty                   : line[207..214].trim(),
                            partno                : line[215..234].trim(),
                            saleable              : line[235..235].trim(),
                            upnQualifierCode      : line[236..237].trim(),
                            srcCode               : line[238..239].trim(),
                            trackingRequired      : line[240..240].trim(),
                            upnCreateDate         : line[241..248].trim(),
                            upnEditDate           : line[249..256].trim(),
                            statusCode            : line[257..258].trim(),
                            actionCode            : line[259..260].trim(),
                            reference             : line[261..280].trim(),
                            referenceQualifierCode: line[281..282].trim()
                    ]
                }
            }


        } catch (RuntimeException e) {
            log.error(e.message)
        }

        [rows: rows]
    }


    def renderImage = {
        def documentInstance = Document.get(params.id)
        if (documentInstance) {
            response.outputStream << documentInstance.fileContents
        } else {
            response.sendError(404)
        }
    }

    def downloadDocument = {
        log.info "viewImage: " + params
        def documentInstance = Document.get(params.id)
        if (documentInstance) {
            response.setHeader "Content-disposition", "attachment;filename=\"${documentInstance.filename}\""
            response.contentType = documentInstance.contentType
            response.outputStream << documentInstance.fileContents
            response.outputStream.flush()
        }
    }

    /**
     * View document
     */
    def viewImage = {
        log.info "viewImage: " + params
        def documentInstance = Document.get(params.id)
        if (documentInstance) {
            if (documentInstance.isImage()) {
                documentService.scaleImage(documentInstance, response.outputStream, '300px', '300px')
            } else {
                // Strip out the most common mime type tree names
                def documentType = documentInstance.contentType.minus("application/").minus("image/").minus("text/")
                def servletContext = ServletContextHolder.servletContext
                def imageContent = servletContext.getResource("/images/icons/${documentType}.png")
                if (!imageContent) {
                    imageContent = servletContext.getResource('/images/icons/silk/page.png')
                }
                response.contentType = 'image/png'
                response.outputStream << imageContent.bytes
                response.outputStream.flush()


            }
        } else {
            response.sendError(404)
        }
    }


    def viewThumbnail = {
        log.info "viewThumbnail: " + params
        def documentInstance = Document.get(params.id)
        if (documentInstance) {
            if (documentInstance.isImage()) {
                documentService.scaleImage(documentInstance, response.outputStream, '100px', '100px')
            } else {
                // Strip out the most common mime type tree names
                def documentType = documentInstance.contentType.minus("application/").minus("image/").minus("text/")
                def servletContext = ServletContextHolder.servletContext
                def imageContent = servletContext.getResource("/images/icons/${documentType}.png")
                if (!imageContent) {
                    imageContent = servletContext.getResource('/images/icons/silk/page.png')
                }
                response.contentType = 'image/png'
                response.outputStream << imageContent.bytes
                response.outputStream.flush()

            }
        } else {
            response.sendError(404)
        }
    }

    /**
     * Export all products identified by the product.id parameter.
     *
     * @params product.id
     */
    def exportProducts = {
        println "export products: " + params
        def productIds = params.list('product.id')
        println "Product IDs: " + productIds
        def products = productService.getProducts(productIds.toArray())
        if (products) {
            def date = new Date()
            def csv = productService.exportProducts(products)
            response.setHeader("Content-disposition",
                    "attachment; filename=\"Products-${date.format("yyyyMMdd-hhmmss")}.csv\"")
            response.contentType = "text/csv"
            println "export products: " + csv
            render csv
        } else {
            response.sendError(404)
        }
    }

    /**
     * Export all products as CSV
     */
    def exportAsCsv = {

        boolean includeAttributes = params.boolean("includeAttributes")?:false
        def products = Product.findAllByActive(true, [fetch:[attributes:"eager", tags:"eager"]])
        if (products) {
            String csv = productService.exportProducts(products, includeAttributes)
            response.setHeader("Content-disposition",
                    "attachment; filename=\"Products-${new Date().format("yyyyMMdd-hhmmss")}.csv\"")
            response.contentType = "text/csv"
            render csv
        } else {
            render(text: 'No products found', status: 404)
        }
    }

    /**
     * Renders form to begin the import process
     */
    def importAsCsv = {}

    /**
     * Upload CSV file
     */
    def uploadCsv = { ImportDataCommand command ->

        log.info "uploadCsv " + params

        def columns
        def localFile
        def uploadFile = command?.importFile

        def existingProductsMap = [:]
        def tag = ""

        if (request.method == "POST") {

            // Step 1: Upload file
            if (uploadFile && !uploadFile?.empty) {

                def contentTypes = ['application/vnd.ms-excel', 'text/plain', 'text/csv', 'text/tsv']
                println "Content type: " + uploadFile.contentType
                println "Validate: " + contentTypes.contains(uploadFile.contentType)

                try {

                    // Upload file
                    localFile = uploadService.createLocalFile(uploadFile.originalFilename)
                    uploadFile?.transferTo(localFile)
                    session.localFile = localFile

                    // Get CSV content
                    def csv = localFile.getText()
                    columns = productService.getColumns(csv)
                    println "CSV " + csv

                    // Create default tag based on base filename
                    tag = FilenameUtils.getBaseName(command?.importFile?.originalFilename)

                    command.products = productService.validateProducts(csv)

                    flash.message = "Uploaded file ${uploadFile?.originalFilename} to ${localFile.absolutePath}"
                } catch (RuntimeException e) {
                    log.error("An error occurred while uploading product import CSV " + e.message, e)
                    command.errors.reject(e.message)
                }
                catch (FileNotFoundException e) {
                    log.error("File not found exception occurred while uploading product import CSV " + e.message, e)
                    command.errors.reject("File '${localFile.absolutePath}' could not be uploaded.  This is most likely due to a file permission error.  Make sure that the 'uploads' directory exists and has the proper read/write permissions.")

                }
                catch (Exception e) {
                    log.error("Exception occurred while uploading product import CSV " + e.message, e)
                    command.errors.reject("Unknown error: " + e.message)

                }
            } else {
                command.errors.reject("${warehouse.message(code: 'import.emptyFile.message', default: 'File is empty')}")
            }
        }

        render(view: 'importAsCsv', model: [command: command, columns: columns, tag: tag])
    }

    /**
     * Perform import of CSV
     */
    def importCsv = { ImportDataCommand command ->

        log.info "import " + params

        // Step 2: Import data from file
        def tags = []
        def columns = []

        if (params.importNow && session.localFile) {
            try {
                def csv = session.localFile.getText()

                // Get columns
                columns = productService.getColumns(csv)

                // Split tags
                tags = params?.tagsToBeAdded?.split(",") as List

                // Import products
                command.products = productService.validateProducts(csv)


                productService.importProducts(command.products, tags)
                flash.message = "All ${command?.products?.size()} product(s) were imported successfully."
                redirect(controller: "product", action: "importAsCsv", params: [tag: tags[0]])
            } catch (ValidationException e) {
                command.errors = e.errors
            }

        }
        render(view: 'importAsCsv', model: [command: command, tags: tags, columns: columns, productsHaveBeenImported: true])
    }


    /**
     * Add a product group to existing product
     *
     * @return
     */
    def addProductGroupToProduct = {
        println "addProductGroupToProduct() " + params
        def product = Product.get(params.id)
        if (product) {
            def productGroup = ProductGroup.findByName(params.productGroup)
            if (!productGroup) {
                productGroup = new ProductGroup(name: params.productGroup, category: product.category)
            }
            product.addToProductGroups(productGroup)
            product.save(failOnError: true)
        }
        render(template: 'productGroups', model: [product: product, productGroups: product.productGroups])
    }


    def addProductComponent = {
        Product assemblyProduct = productService.addProductComponent(params.assemblyProduct.id, params.componentProduct.id, params.quantity as BigDecimal, params.unitOfMeasure)
        render(template: 'productComponents', model: [productInstance: assemblyProduct])
    }

    def deleteProductComponent = {

        def productInstance
        def productComponent = ProductComponent.get(params.id)
        if (productComponent) {
            productInstance = productComponent.assemblyProduct
            productComponent.assemblyProduct.removeFromProductComponents(productComponent)
            productComponent.delete()
        }
        render(template: 'productComponents', model: [productInstance: productInstance])
    }

    /**
     * Delete product group from database
     */
    def removeFromProductGroups = {
        println "removeFromProductGroup() " + params

        def product = Product.get(params.productId)
        if (product) {
            def productGroup = ProductGroup.get(params.id)
            product.removeFromProductGroups(productGroup)
            product.save(flush: true)
        } else {
            response.status = 404
        }
        render(template: 'productGroups', model: [product: product, productGroups: product?.productGroups])
    }


    /**
     * Delete product group from database
     */
    def deleteProductGroup = {
        println "deleteProductGroup() " + params

        def product = Product.get(params.productId)
        if (product) {
            def productGroup = ProductGroup.get(params.id)
            def productIds = productGroup?.products?.collect { it.id }
            productIds.each { productId ->
                def productGroupProduct = Product.get(productId)
                productGroup.removeFromProducts(productGroupProduct)
            }
            productGroup.delete(flush: true)
            product.save(flush: true)
        } else {
            response.status = 404
        }
        render(template: 'productGroups', model: [product: product, productGroups: product?.productGroups])
    }


    /**
     * Add a synonym to existing product
     *
     * @return
     */
    def addSynonymToProduct = {
        println "addSynonymToProduct() " + params
        def product = Product.get(params.id)
        if (product) {
            product.addToSynonyms(new Synonym(name: params.synonym, locale: RCU.getLocale(request)))
            product.save(flush: true, failOnError: true)
        }
        render(template: 'synonyms', model: [product: product, synonyms: product.synonyms])
    }

    /**
     * Delete synonym from database
     */
    def deleteSynonym = {
        println "deleteSynonym() " + params

        def product = Product.get(params.productId)
        if (product) {
            def synonym = Synonym.get(params.id)
            product.removeFromSynonyms(synonym)
            synonym.delete()
            product.save(flush: true)
        } else {
            response.status = 404
        }
        render(template: 'synonyms', model: [product: product, synonyms: product?.synonyms])
    }


    def renderCreatedEmail = {
        def productInstance = Product.get(params.id)
        def userInstance = User.get(session.user.id)
        render(template: "/email/productCreated", model: [productInstance: productInstance, userInstance: userInstance])
    }

    def addToProductCatalog = { ProductCatalogCommand command ->
        log.info("Add product ${command.product} to ${command.productCatalog}" + params)
        def product = command.product
        def productCatalog = command.productCatalog
        if (product && productCatalog) {
            if (!productCatalog.productCatalogItems.contains(product)) {
                productCatalog.addToProductCatalogItems(new ProductCatalogItem(product: product))
                productCatalog.save()
            }
        }
        redirect(action: "productCatalogs", id: command.product.id)
    }

    def includesProduct = {
        def product = Product.get(params.id)

        render([products: ProductCatalog.includesProduct(product).listDistinct()] as JSON)
    }

    def removeFromProductCatalog = {
        log.info("params: " + params)
        def product = Product.get(params.id)
        def productCatalog = ProductCatalog.get(params.productCatalog.id)
        if (productCatalog && product) {
            log.info("product: " + product)
            log.info("productCatalog: " + productCatalog)
            def list = productCatalog.productCatalogItems.findAll { it.product = product }
            list.toArray().each { productCatalogItem ->
                productCatalog.removeFromProductCatalogItems(productCatalogItem)
                productCatalogItem.delete()
                productCatalog.save()
            }
        }
        redirect(action: "productCatalogs", id: product.id)
    }


    def productCatalogs = {
        def product = Product.get(params.id)

        def productCatalogs = ProductCatalogItem.createCriteria().list {
            projections {
                property("productCatalog")
            }
            eq("product", product)
            resultTransformer Criteria.DISTINCT_ROOT_ENTITY
        }

        log.info "productCatalogs: " + productCatalogs

        render template: "productCatalogs", model: [productCatalogs: productCatalogs, product: product]
    }

    def removeFromProductAssociations = {
        String productId
        def productAssociation = ProductAssociation.get(params.id)
        if (productAssociation) {
            productId = productAssociation.product.id
            productAssociation.delete()
            redirect(action: "productSubstitutions", id: productId)
        } else {
            response.status = 404
        }
    }


    def createProductSnapshot = {

        Product product = Product.get(params.id)
        Location location = Location.get(session.warehouse.id)

        inventoryService.createStockSnapshot(location, product)

        flash.message = "Successfully created stock snapshot for product ${product.productCode} ${product?.name}"

        redirect(controller: "inventoryItem", action: "showStockCard", id: params.id)
    }


}



