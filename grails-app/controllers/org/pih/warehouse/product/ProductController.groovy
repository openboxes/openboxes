package org.pih.warehouse.product;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.list.LazyList;
import org.groovydev.SimpleImageBuilder;
import org.junit.runner.Request;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.product.DocumentCommand;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.inventory.InventoryLevel;
import org.pih.warehouse.core.Document;
import org.pih.warehouse.core.DocumentType;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.RoleType;
import org.pih.warehouse.core.User;
import org.springframework.web.multipart.MultipartFile;
import java.awt.Image as AWTImage
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.imageio.ImageIO as IIO
import java.awt.Graphics2D
import java.io.ByteArrayOutputStream;
import com.google.zxing.BarcodeFormat;
import java.net.URLEncoder;

import grails.converters.JSON;

class ProductController {

	def userService;
	def mailService;
	def productService;
	def documentService;
	def inventoryService;
	def barcodeService

	static allowedMethods = [save: "POST", update: "POST"];


	def index = {
		redirect(action: "list", params: params)
	}
	
	/** 
	 * Perform a bulk update of 
	 */
	def batchEdit = { BatchEditCommand cmd ->

		def location = Location.get(session.warehouse.id)

		log.info "Batch edit: " + params
		def categoryInstance = Category.get(params?.category?.id)
		if (categoryInstance) {
			cmd.productInstanceList = Product.findAllByCategory(categoryInstance)
			//cmd.inventoryLevelMap = inventoryService.getInventoryLevels(cmd.productInstanceList, location)
		}
		else {
			//flash.message = "Only displaying first 100 products.  Please select a category."
			//params.max = 25;
			//cmd.productInstanceList = Product.list(params);
			cmd.productInstanceList = []
		}

		cmd.productInstanceList.eachWithIndex { product, index ->
			println product.category
			cmd.categoryInstanceList << product.category;
		}
		cmd.rootCategory = productService.getRootCategory();

		[ commandInstance : cmd, categoryInstance: categoryInstance ]
	}


	def batchSave = { BatchEditCommand cmd ->

		// If there are no products (usually when returning to batchSave after login
		if (!cmd.productInstanceList) {
			redirect(action: 'batchEdit')
		}
		// We needed to hack the category binding in order to make this work.
		// When changing the product.category directly, we received an error
		// from Hibernate stating that we were trying to change the primary key
		// of the category object.
		cmd.categoryInstanceList.eachWithIndex { cat, i ->
			log.info "categoryInstanceList[" + i + "]: " + cat;
			cmd.productInstanceList[i].category = Category.get(cat.id);
		}

		cmd.productInstanceList.eachWithIndex { prod, i ->
			log.info "productInstanceList[" + i + "]: " + prod.category;
			if (!prod.hasErrors() && prod.save()) {
				// saved with no errors
			}
			else {
				// copy the errors from this product on to the overall command object errors
				prod.errors.getAllErrors(). each {
					cmd.errors.reject(it.getCode(), it.getDefaultMessage())
				}
			}
		}

		if (!cmd.hasErrors()) {
			flash.message = "${warehouse.message(code: 'product.allSavedSuccessfully.message')}"
		}
		else {
			// reset the flash message in the case of two submits in a row
			flash.message = null
		}


		cmd.rootCategory = productService.getRootCategory();

		render(view: "batchEdit", model: [commandInstance:cmd]);

	}

	def list = {
		def productInstanceList = []
		def productInstanceTotal = 0;

		params.max = Math.min(params.max ? params.int('max') : 12, 100)

		if (params.q) {
			productInstanceList = Product.findAllByNameLike("%" + params.q + "%", params)
			productInstanceTotal = Product.countByNameLike("%" + params.q + "%", params);
		}
		else {
			productInstanceList = Product.list(params)
			productInstanceTotal = Product.count()
		}

		[productInstanceList: productInstanceList, productInstanceTotal: productInstanceTotal]
	}

	def create = {
		def productInstance = new Product(params)
		render(view: "edit", model: [productInstance : productInstance, rootCategory: productService.getRootCategory()])
	}

	def save = {
		def productInstance = new Product();
		productInstance.properties = params

		log.info("Categories " + productInstance?.categories);

		Attribute.list().each() {
			String attVal = params["productAttributes." + it.id + ".value"];
			if (attVal == "_other" || attVal == null || attVal == '') {
				attVal = params["productAttributes." + it.id + ".otherValue"];
			}
			if (attVal) {
				productInstance.getAttributes().add(new ProductAttribute(["attribute":it,"value":attVal]))
			}
		}

		// find the phones that are marked for deletion
		def _toBeDeleted = productInstance.categories.findAll {(it?.deleted || (it == null))}

		log.info("toBeDeleted: " + _toBeDeleted )

		// if there are phones to be deleted remove them all
		if (_toBeDeleted) {
			productInstance.categories.removeAll(_toBeDeleted)
		}

		def warehouseInstance = Location.get(session.warehouse.id);
		def inventoryInstance = warehouseInstance?.inventory;

		if (!productInstance.hasErrors() && productInstance.save(flush: true)) {
			flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'product.label', default: 'Product'), format.product(product:productInstance)])}"
			sendProductCreatedEmail(productInstance)
			redirect(controller: "inventoryItem", action: "showRecordInventory", params: ['productInstance.id':productInstance.id, 'inventoryInstance.id': inventoryInstance?.id])
			//redirect(controller: "inventoryItem", action: "showStockCard", id: productInstance?.id, params:params)
		}
		else {
			render(view: "edit", model: [productInstance: productInstance, rootCategory: productService.getRootCategory()])
		}
	}

	def show = {
		def productInstance = Product.get(params.id)
		if (!productInstance) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'product.label', default: 'Product'), params.id])}"
			redirect(controller: "inventoryItem", action: "browse")
		}
		else {
			[productInstance: productInstance]
		}
	}

	def edit = {
		def productInstance = Product.get(params.id)
		def location = Location.get(session?.warehouse?.id);
		if (!productInstance) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'product.label', default: 'Product'), params.id])}"
			redirect(controller: "inventoryItem", action: "browse")
		}
		else {
			
			def inventoryLevelInstance = InventoryLevel.findByProductAndInventory(productInstance, location.inventory)
			if (!inventoryLevelInstance) {
				inventoryLevelInstance = new InventoryLevel();
			}
	
			
			return [productInstance: productInstance, rootCategory: productService.getRootCategory(), inventoryInstance: location.inventory, inventoryLevelInstance:inventoryLevelInstance]
		}
	}

	def update = {
		log.info "Update called with params " + params
		def productInstance = Product.get(params.id)

		if (productInstance) {
			if (params.version) {
				def version = params.version.toLong()
				if (productInstance.version > version) {
                    productInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'product.label', default: 'Product')] as Object[], "Another user has updated this Product while you were editing")
					render(view: "edit", model: [productInstance: productInstance])
					return
				}
			}
			productInstance.properties = params

			Map existingAtts = new HashMap();
			productInstance.attributes.each() {
				existingAtts.put(it.attribute.id, it)
			}

			Attribute.list().each() {
				String attVal = params["productAttributes." + it.id + ".value"]
				if (attVal == "_other" || attVal == null || attVal == '') {
					attVal = params["productAttributes." + it.id + ".otherValue"]
				}
				ProductAttribute existing = existingAtts.get(it.id)
				if (attVal != null && attVal != '') {
					if (!existing) {
						existing = new ProductAttribute(["attribute":it])
						productInstance.attributes.add(existing)
					}
					existing.value = attVal;
				}
				else {
					productInstance.attributes.remove(existing)
				}
			}

			log.info("Categories " + productInstance?.categories);

			// find the phones that are marked for deletion
			def _toBeDeleted = productInstance.categories.findAll {(it?.deleted || (it == null))}

			log.info("toBeDeleted: " + _toBeDeleted )

			// if there are phones to be deleted remove them all
			if (_toBeDeleted) {
				productInstance.categories.removeAll(_toBeDeleted)
			}

			/*
			 productInstance?.categories?.clear();		
			 params.each {
			 println ("category: " + it.key +  " starts with category_ " + it.key.startsWith("category_"))
			 if (it.key.startsWith("category_")) {
			 def category = Category.get((it.key - "category_"));
			 log.info "adding " + category?.name
			 productInstance.addToCategories(category)
			 }
			 }
			 */
			if (!productInstance.hasErrors() && productInstance.save(flush: true)) {
				flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'product.label', default: 'Product'), format.product(product:productInstance)])}"
				redirect(controller: "inventoryItem", action: "showStockCard", id: productInstance?.id, params:params)
			}
			else {
				
				def location = Location.get(session?.warehouse?.id);
				def inventoryLevelInstance = InventoryLevel.findByProductAndInventory(productInstance, location.inventory)
				if (!inventoryLevelInstance) {
					inventoryLevelInstance = new InventoryLevel();
				}
		
				
				render(view: "edit", model: [productInstance: productInstance, 
					rootCategory: productService.getRootCategory(),
					inventoryInstance: location.inventory, 
					inventoryLevelInstance:inventoryLevelInstance])
			}
		}
		else {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'product.label', default: 'Product'), params.id])}"
			redirect(controller: "inventoryItem", action: "browse")
		}
	}

	def delete = {
		def productInstance = Product.get(params.id)
		if (productInstance && !productInstance.hasAssociatedTransactionEntriesOrShipmentItems()) {
			try {
				// first we need to delete any inventory items associated with this product
				def items = InventoryItem.findAllByProduct(productInstance)
				items.each { it.delete(flush:true) }

				// now delete the actual product
				productInstance.delete(flush: true)

				flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'product.label', default: 'Product'), params.id])}"
				redirect(controller: "product", action: "list")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'product.label', default: 'Product'), params.id])}"
				redirect(action: "edit", id: params.id)
			}
		}
		else {
			flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'product.label', default: 'Product'), params.id])}"
			redirect(action: "edit", id: params.id)
		}
	}
	
	
	def removePackage = { 
		def packageInstance = ProductPackage.get(params.id)
		def productInstance = packageInstance.product
		log.info "" + packageInstance.product
		productInstance.removeFromPackages(packageInstance)
		packageInstance.delete()
		productInstance.save();
		flash.message = "Product package has been deleted"
		redirect(action: "edit", id: productInstance.id)
	}

	def savePackage = { 
		
		println "savePackage: " + params
		def productInstance = Product.get(params.id)		
		def packageInstance = new ProductPackage(params)
		productInstance.addToPackages(packageInstance)
		
		if (!productInstance.hasErrors() && productInstance.save(flush: true) ) {
			flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'package.label', default: 'Product'), packageInstance.name])}"			
			redirect(action: "edit", id: productInstance?.id)
		}
		else {
			def location = Location.get(session.warehouse.id)
			def inventoryLevelInstance = InventoryLevel.findByProductAndInventory(productInstance, location.inventory)
			if (!inventoryLevelInstance) {
				inventoryLevelInstance = new InventoryLevel();
			}
			
			render(view: "edit", model: [productInstance: productInstance, inventoryLevelInstance: inventoryLevelInstance, packageInstance: packageInstance, rootCategory: productService.getRootCategory()])
		}
	}
	
	
	/**
	 * 
	 */
	def importDependencies = {
		/*
		 if (session.dosageForms) {
		 session.dosageForms.unique().each() {
		 new DosageForm(code: it, name: it).save(flush:true)
		 }
		 session.dosageForms = null
		 }*/

		redirect(controller: "product", action: "importProducts")
	}


	/**
	 *
	 * @param userInstance
	 * @return
	 */	
   def sendProductCreatedEmail(Product product) {
		def adminList = []
		try {
			adminList = userService.findUsersByRoleType(RoleType.ROLE_ADMIN).collect { it.email }
			if (adminList) {
			   println adminList.class
				def subject = "${warehouse.message(code:'email.productCreated.message',args:[product?.name,product?.createdBy?.name])}";
				def body = "${g.render(template:'/email/productCreated',model:[product:product])}"
				mailService.sendHtmlMail(subject, body.toString(), adminList);
				flash.message = "${warehouse.message(code:'email.sent.message',args:[adminList])}"
			}
			else {
			}
		}
		catch (Exception e) {
		   log.error("Error sending 'Product Created' email")
			flash.message = "${warehouse.message(code:'email.notSent.message',args:[adminList])}: ${e.message}"
		}
	}



	def search = { 
		log.info "search " + params
		if (params.q) { 
			def products = productService.findProducts(URLEncoder.encode(params.q))
			[ products : products ]
		}
	}
	
	
	def barcode = { 
		BarcodeFormat format = BarcodeFormat.valueOf(params.format)
		File file = File.createTempFile("barcode-", ".png")
		barcodeService.renderImageToFile(file, params.data, 100, 50, format)
		response.contentType = "image/png"
		response.outputStream << file.bytes
		file.delete()
	}	   
   
   
	/**
	 * Upload a document to a product.
	 */
	def upload = { DocumentCommand command ->
		log.info "Uploading document: " + params
		def file = command.fileContents;

		// HACK - for some reason the Product in document command is not getting bound
		command.product = Product.get(params.product.id)

		log.info "multipart file: " + file?.originalFilename + " " + file?.contentType + " " + file?.size + " "
		log.info "product " + command.product
		// file must not be empty and must be less than 10MB
		// FIXME The size limit needs to go somewhere
		if (!file || file?.isEmpty()) {
			flash.message = "${warehouse.message(code: 'document.documentCannotBeEmpty.message')}"
		}
		else if (file.size < 10*1024*1000) {
			log.info "Creating new document ";
			Document documentInstance = new Document(
					size: file.size,
					name: file.originalFilename,
					filename: file.originalFilename,
					fileContents: file.bytes,
					contentType: file.contentType,
					documentNumber: command.documentNumber,
					documentType:  command.documentType)

			if (!command?.product) {
				log.info "Cannot add document " + documentInstance + "  because product does not exist";
				flash.message = "${warehouse.message(code: 'document.productDoesNotExist.message')}"
				redirect(controller: "product", action: "list")
				return
			}
			else {

				// Check to see if there are any errors
				if (documentInstance.validate() && !documentInstance.hasErrors()) {
					log.info "Saving document " + documentInstance;
					command.product.addToDocuments(documentInstance).save(flush:true)
					flash.message = "${warehouse.message(code: 'document.successfullySavedToProduct.message', args: [command?.product?.name])}"
				}
				// If there are errors, we need to redisplay the document form
				else {
					log.info "Document did not save " + documentInstance.errors;
					flash.message = "${warehouse.message(code: 'document.cannotSave.message', args: [documentInstance.errors])}"
					redirect(controller: "product", action: "edit", id: command?.product?.id,
							model: [productInstance: command?.product, documentInstance : documentInstance])
					return;
				}
			}
		}
		else {
			log.info "Document is too large"
			flash.message = "${warehouse.message(code: 'document.documentTooLarge.message')}"
		}

		// This is, admittedly, a hack but I wanted to avoid having to add this code to each of
		// these controllers.
		log.info ("Redirecting to appropriate show details page " + command?.product?.id)
		redirect(controller: 'product', action: 'edit', id: command?.product?.id)
	}
	
	
	def deleteDocument = {
		def productInstance = Product.get(params.product.id)
		if (!productInstance) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'product.label', default: 'Product'), params.product.id])}"
			redirect(action: "list")
		}
		else {
			def documentInstance = Document.get(params?.id)
			if (!documentInstance) {
				flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'document.label', default: 'Document'), params.id])}"
				redirect(action: "edit", id: productInstance?.id)
			}
			else {
				productInstance.removeFromDocuments(documentInstance);
				documentInstance?.delete();
				if (!productInstance.hasErrors() && productInstance.save(flush: true)) {
					flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'product.label', default: 'Product'), productInstance.id])}"
					redirect(action: "edit", id: productInstance?.id)
				}
				else {
					render(view: "edit", model: [productInstance: productInstance])
				}
			}
		}
	}
	
	
	def upnDatabase = { 
		
		def file = new File("/home/jmiranda/Dropbox/OpenBoxes/Product Databases/HIBCC/UPNDownload.txt")
		def count=0, MAXSIZE=100000
		def rows = []
		try {
			def line = ""
			file.withReader { reader ->
				while ((line = reader.readLine()) != null) {
					//rows << line[0..19].trim()					
					rows << [
						line: line,
						upn: line[0..19].trim(),
						supplier:  line[20..54].trim(),
						division: line[55..89].trim(),
						tradeName: line[90..124].trim(),
						description: line[125..204].trim(),
						uom: line[205..206].trim(),
						qty: line[207..214].trim(),
						partno: line[215..234].trim(),
						saleable: line[235..235].trim(),
						upnQualifierCode: line[236..237].trim(),
						srcCode: line[238..239].trim(),
						trackingRequired: line[240..240].trim(),
						upnCreateDate: line[241..248].trim(),
						upnEditDate: line[249..256].trim(),
						statusCode: line[257..258].trim(),
						actionCode: line[259..260].trim(),
						reference: line[261..280].trim(),
						referenceQualifierCode: line[281..282].trim()
					]
					
					//if (++count > MAXSIZE) throw new RuntimeException('File too large!')
				}
			}
			
			
		} catch (RuntimeException e) {
			log.error(e.message)
			//render "error " + e.message + "<br/>" + rows			
		}

		[rows:rows];
		//render rows;
		
		//assert names[0].first == 'JOHN'
		//assert names[1].age == 456
	}
	
	/**
	* View user's profile photo
	*/
   def viewImage = {
	   def documentInstance = Document.get(params.id);
	   if (documentInstance) {
		   documentService.scaleImage(documentInstance, response.outputStream, '300px', '300px')
		   /*
		   println "resize image " + params.width + " " + params.height
	   	   println params
		   byte[] bytes = documentInstance.fileContents
		   println documentInstance.contentType
		   resize(bytes, response.outputStream, params.width as int, params.height as int)
		   //response.outputStream << bytes
		   */
	   }
	   else {
		   "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'document.label'), params.id])}"
	   }
   }

   
   def viewThumbnail = {
	   def documentInstance = Document.get(params.id);
	   if (documentInstance) {
		   documentService.scaleImage(documentInstance, response.outputStream, '100px', '100px')
		   //byte[] bytes = documentInstance.fileContents
		   //resize(bytes, response.outputStream, width, height)
	   }
	   else {
		   "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'document.label'), params.id])}"
	   }
   }

   /**
   static scale = { out, 
	   def b = new SimpleImageBuilder()
	   def result = b.render(width:'100px',height:'100px') {
		  fill(color:'ffffff')
		  def small
		  image(file:'image.jpg') {
			small = fit(width:100,height:100)
		  }
		  draw(align:'center', image:small)
		  fit(width:50,height:50) {
			//save(file:'thumbnail-50p.png', format:'png')
		  }
		  fit(width:20,height:20) {
			//save(file:'thumbnail-20p.png', format:'png')
		  }
	   }
	   
   }   
   */
	
   
   
   
   static resize = { bytes, out, maxW, maxH ->
	   AWTImage ai = new ImageIcon(bytes).image
	   int width = ai.getWidth( null )
	   int height = ai.getHeight( null )
   
	   //def limits = 300..2000
	   //assert limits.contains( width ) && limits.contains( height ) : 'Picture is either too small or too big!'
   
	   float aspectRatio = width / height
	   float requiredAspectRatio = maxW / maxH
   
	   int dstW = 0
	   int dstH = 0
	   if (requiredAspectRatio < aspectRatio) {
		   dstW = maxW
		   dstH = Math.round( maxW / aspectRatio)
	   } else {
		   dstH = maxH
		   dstW = Math.round(maxH * aspectRatio)
	   }
   
	   BufferedImage bi = new BufferedImage(dstW, dstH, BufferedImage.TYPE_INT_RGB)
	   Graphics2D g2d = bi.createGraphics()
	   g2d.drawImage(ai, 0, 0, dstW, dstH, null, null)
   
	   IIO.write( bi, 'JPEG', out )
   }
		
}




