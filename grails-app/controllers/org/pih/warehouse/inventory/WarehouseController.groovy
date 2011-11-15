package org.pih.warehouse.inventory

import grails.validation.ValidationException;

import org.pih.warehouse.core.Location;
import org.pih.warehouse.product.Product;

class WarehouseController {
	
	def inventoryService;
	
	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
	//def beforeInterceptor = [action:this.&auth,except:'login']
	
	// defined as a regular method so its private
	def auth() {
		log.debug "checking if user is authenticated $session.user";
		if(!session.user) {
			log.debug "user in not authenticated";
			redirect(controller: "user", action: "login");
			return false
		} else {
			log.debug "user is authenticated";
		}
	}
	
	/*
	 stockCard: stockCardService.getStockCard(Location.get(params.warehouse),
	 Product.get(params.product), fromDate, toDate)
	 */
	def showInventory = {
		//Date fromDate = params.fromDate ?: new Date()
		//Date toDate = params.toDate ?: new Date()
		def warehouse = Location.get(params.id)
		//Inventory inventory = new Inventory();
		//def inventory = inventoryService.getInventory(warehouseInstance)
		// Create an inventory item for each product in the database
		// TODO There should be a Location->Products association
		Map<Product, Long> inventory = new HashMap<Product, Long>()
		Product.getAll().each { inventory.put(it, 0l) }
		
		// Get all transaction entries
		def entries = TransactionEntry.withCriteria {
			createAlias("transaction", "t")
			eq("t.thisLocation", warehouse)
		}
		
		log.debug "transaction entries $entries"
		for (TransactionEntry entry in entries) {
			def quantityNow = inventory.get(entry?.inventoryItem?.product);
			
			log.debug "$quantityNow + $entry?.quantity"	    
			quantityNow += entry.quantity
			
			log.debug "quantity = $quantityNow"
			inventory.put(entry?.inventoryItem?.product, quantityNow)
		}
		
		return [
			warehouseInstance : warehouse,
			inventory : inventory
		]
	}
	
	
	def showTransactions = {
		def warehouseInstance = Location.get(params.id)
		def transactionList = inventoryService.getAllTransactions(warehouseInstance);
		
		return [
			warehouseInstance : warehouseInstance,
			transactions : transactionList
		]
	}
	
	def index = {
		redirect(action: "list", params: params)
	}
	
	def list = {
		params.max = Math.min(params.max ? params.int('max') : 25, 100)
		[warehouseInstanceList: Location.list(params), warehouseInstanceTotal: Location.count()]
	}
	
	def show = {
		def warehouseInstance = inventoryService.getLocation(params.id as Long)
		if (!warehouseInstance?.id) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'warehouse.label', default: 'Location'), params.id])}"
			redirect(action: "list")
		}
		else {
			[warehouseInstance: warehouseInstance]
		}
	}
	
	def edit = {
		def warehouseInstance = inventoryService.getLocation(params.id as Long)
		if (!warehouseInstance) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'warehouse.label', default: 'Location'), params.id])}"
			redirect(action: "list")
		}
		else {
			return [warehouseInstance: warehouseInstance]
		}
	}
	
	def update = {
		
		def warehouseInstance = inventoryService.getLocation(params.id ? params.id as Long : null)
		
		if (warehouseInstance) {
			if (params.version) {
				def version = params.version.toLong()
				if (warehouseInstance.version > version) {					
					warehouseInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'warehouse.label', default: 'Location')] as Object[], "Another user has updated this Location while you were editing")
					render(view: "edit", model: [warehouseInstance: warehouseInstance])
					return
				}
			}
			
			warehouseInstance.properties = params			
			
			if (!warehouseInstance.hasErrors()) {
				
				try { 
					inventoryService.saveLocation(warehouseInstance);
					// Refresh the current warehouse to make sure the color changes take effect 
					if (session.warehouse.id == warehouseInstance?.id) 
						session.warehouse = Location.get(warehouseInstance?.id);					
					flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'warehouse.label', default: 'Location'), warehouseInstance.id])}"
					redirect(action: "list", id: warehouseInstance.id)
				} catch (ValidationException e) { 
					render(view: "edit", model: [warehouseInstance: warehouseInstance])
				}
			}
			else {
				render(view: "edit", model: [warehouseInstance: warehouseInstance])
			}
		}
		else {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'warehouse.label', default: 'Location'), params.id])}"
			redirect(action: "list")
		}
	}
	
	def delete = {
		def warehouseInstance = Location.get(params.id)
		if (warehouseInstance) {
			try {
				warehouseInstance.delete(flush: true)
				flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'warehouse.label', default: 'Location'), params.id])}"
				redirect(action: "list")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'warehouse.label', default: 'Location'), params.id])}"
				redirect(action: "show", id: params.id)
			}
		}
		else {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'warehouse.label', default: 'Location'), params.id])}"
			redirect(action: "list")
		}
	}


	/**
	 * View warehouse logo 
	 */
	def viewLogo = { 
		def warehouseInstance = Location.get(params.id);		
		if (warehouseInstance) { 
			byte[] logo = warehouseInstance.logo 
			if (logo) { 
				response.outputStream << logo
			}
		} 
	} 


	def uploadLogo = { 
		
		def warehouseInstance = Location.get(params.id);		
		if (warehouseInstance) { 
			def logo = request.getFile("logo");
			if (!logo?.empty && logo.size < 1024*1000) { // not empty AND less than 1MB
				warehouseInstance.logo = logo.bytes;			
		        if (!warehouseInstance.hasErrors()) {
		        	inventoryService.save(warehouseInstance)
		            flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'warehouse.label', default: 'Location'), warehouseInstance.id])}"
		        }
		        else {
					// there were errors, the photo was not saved
		        }
			}
            redirect(action: "show", id: warehouseInstance.id)
		} 
		else { 
			"${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'warehouse.label', default: 'Location'), params.id])}"
		}
	}

}
