package org.pih.warehouse.inventory

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
	 stockCard: stockCardService.getStockCard(Warehouse.get(params.warehouse),
	 Product.get(params.product), fromDate, toDate)
	 */
	def showInventory = {
		//Date fromDate = params.fromDate ?: new Date()
		//Date toDate = params.toDate ?: new Date()
		def warehouse = Warehouse.get(params.id)
		//Inventory inventory = new Inventory();
		//def inventory = inventoryService.getInventory(warehouseInstance)
		// Create an inventory item for each product in the database
		// TODO There should be a Warehouse->Products association
		Map<Product, Long> inventory = new HashMap<Product, Long>()
		Product.getAll().each { inventory.put(it, 0l) }
		
		// Get all transaction entries
		def entries = TransactionEntry.withCriteria {
			createAlias("transaction", "t")
			eq("t.thisWarehouse", warehouse)
		}
		
		log.debug "transaction entries $entries"
		for (TransactionEntry entry in entries) {
			def quantityNow = inventory.get(entry?.inventoryItem?.product);
			
			log.debug "$quantityNow + $entry?.quantity"	    
			quantityNow += entry.quantity
			
			log.debug "quantity = $quantityNow"
			inventory.put(entry?.inventoryItem?.product, quantityNow)
		}
		
		// HOWTO Do SQL query "Select Product, Count(*) group from transactionEntry by Product""
		/*
		 def tagList = Post.withCriteria {
		 createAlias("user", "u")
		 createAlias("tags", "t")
		 eq("u.userId", "glen")
		 projections {
		 groupProperty("t.name")
		 count("t.id")
		 }
		 }*/
		
		
		// The long way around
		/*
		 def transactionList = inventoryService.getAllTransactions(warehouseInstance);
		 for (transaction in transactionList) {
		 for(transactionEntry in transaction.transactionEntries) {
		 }
		 }
		 */
		
		return [
			warehouseInstance : warehouse,
			inventory : inventory
		]
	}
	
	
	def showTransactions = {
		def warehouseInstance = Warehouse.get(params.id)
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
		params.max = Math.min(params.max ? params.int('max') : 10, 100)
		[warehouseInstanceList: Warehouse.list(params), warehouseInstanceTotal: Warehouse.count()]
	}
	
	def show = {
		def warehouseInstance = inventoryService.getWarehouse(params.id as Long)
		if (!warehouseInstance?.id) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'warehouse.label', default: 'Warehouse'), params.id])}"
			redirect(action: "list")
		}
		else {
			[warehouseInstance: warehouseInstance]
		}
	}
	
	def edit = {
		def warehouseInstance = inventoryService.getWarehouse(params.id as Long)
		if (!warehouseInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'warehouse.label', default: 'Warehouse'), params.id])}"
			redirect(action: "list")
		}
		else {
			return [warehouseInstance: warehouseInstance]
		}
	}
	
	def update = {
		def warehouseInstance = inventoryService.getWarehouse(params.id ? params.id as Long : null)
		
		if (warehouseInstance) {
			if (params.version) {
				def version = params.version.toLong()
				if (warehouseInstance.version > version) {
					
					warehouseInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'warehouse.label', default: 'Warehouse')] as Object[], "Another user has updated this Warehouse while you were editing")
					render(view: "edit", model: [warehouseInstance: warehouseInstance])
					return
				}
			}
			
			warehouseInstance.properties = params
					
			if (!warehouseInstance.hasErrors()) {
				inventoryService.saveWarehouse(warehouseInstance);
				
				// Refresh the current warehouse to make sure the color changes take effect 
				if (session.warehouse.id == warehouseInstance?.id) 
					session.warehouse = Warehouse.get(warehouseInstance?.id);
				
				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'warehouse.label', default: 'Warehouse'), warehouseInstance.id])}"
				redirect(action: "list", id: warehouseInstance.id)
			}
			else {
				render(view: "edit", model: [warehouseInstance: warehouseInstance])
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'warehouse.label', default: 'Warehouse'), params.id])}"
			redirect(action: "list")
		}
	}
	
	def delete = {
		def warehouseInstance = Warehouse.get(params.id)
		if (warehouseInstance) {
			try {
				warehouseInstance.delete(flush: true)
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'warehouse.label', default: 'Warehouse'), params.id])}"
				redirect(action: "list")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'warehouse.label', default: 'Warehouse'), params.id])}"
				redirect(action: "show", id: params.id)
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'warehouse.label', default: 'Warehouse'), params.id])}"
			redirect(action: "list")
		}
	}


	/**
	 * View warehouse logo 
	 */
	def viewLogo = { 
		def warehouseInstance = Warehouse.get(params.id);		
		if (warehouseInstance) { 
			byte[] logo = warehouseInstance.logo 
			if (logo) { 
				response.outputStream << logo
			}
		} 
	} 


	def uploadLogo = { 
		
		def warehouseInstance = Warehouse.get(params.id);		
		if (warehouseInstance) { 
			def logo = request.getFile("logo");
			if (!logo?.empty && logo.size < 1024*1000) { // not empty AND less than 1MB
				warehouseInstance.logo = logo.bytes;			
		        if (!warehouseInstance.hasErrors()) {
		        	inventoryService.save(warehouseInstance)
		            flash.message = "${message(code: 'default.updated.message', args: [message(code: 'warehouse.label', default: 'Warehouse'), warehouseInstance.id])}"
		        }
		        else {
					// there were errors, the photo was not saved
		        }
			}
            redirect(action: "show", id: warehouseInstance.id)
		} 
		else { 
			"${message(code: 'default.not.found.message', args: [message(code: 'warehouse.label', default: 'Warehouse'), params.id])}"
		}
	}

}
