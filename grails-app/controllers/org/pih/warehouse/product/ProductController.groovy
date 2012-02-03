package org.pih.warehouse.product;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.list.LazyList;
import org.junit.runner.Request;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.core.Location;

import grails.converters.JSON;

class ProductController {

	def inventoryService;
	def productService;

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
		
        params.max = Math.min(params.max ? params.int('max') : 15, 100)
		
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
				productInstance.getAttributes().add(new ProductAttribute(new ProductAttribute(["attribute":it,"value":attVal])))
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
			redirect(controller: "inventoryItem", action: "recordInventory", params: ['product.id':productInstance.id, 'inventory.id': inventoryInstance?.id])
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
        if (!productInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'product.label', default: 'Product'), params.id])}"
            redirect(controller: "inventoryItem", action: "browse")
        }
        else {
            return [productInstance: productInstance, rootCategory: productService.getRootCategory()]
        }
    }
	
    def update = {
		
		log.info "update called with params " + params
        def productInstance = Product.get(params.id)
		log.info " " + productInstance.class.simpleName
		
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
			 
			//update my indexes
			//productInstance.categories.eachWithIndex(){cat, i ->
			//	cat.index = i
			//}
			
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
                render(view: "edit", model: [productInstance: productInstance, rootCategory: productService.getRootCategory()])
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
	        	  // first we need to delete any inventory items associated with this transaction
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
	

	
	
}




