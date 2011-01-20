package org.pih.warehouse.inventory;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.ListUtils;
import org.pih.warehouse.core.User;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.inventory.TransactionType;
import grails.converters.*

class InventoryItemController {

	def inventoryService;
	
	def show = {
		def itemInstance = InventoryItem.get(params.id)
		//def inventoryItemList = inventoryService.getInventoryItemsByProduct(productInstance)
		def transactionEntryList = TransactionEntry.findAllByInventoryItem(itemInstance)
		[
			itemInstance : itemInstance,
			transactionEntryList : transactionEntryList
		]
	}


	def showRecordInventory = { RecordInventoryCommand cmd -> 
		def commandInstance = inventoryService.getRecordInventoryCommand(cmd, params)
		
		// We need to set the inventory instance in order to save an 'inventory' transaction
		def warehouseInstance = Warehouse.get(session?.warehouse?.id)				
		
		[ commandInstance : commandInstance, inventoryInstance: warehouseInstance.inventory ]
	}
	
	def saveRecordInventory = { RecordInventoryCommand cmd ->
		//flash.message = "Trying to save a record inventory command object";		
		// The cmd.newRow object is not being bound correctly, so we need to use two command objects
		//cmd.recordInventoryRow = recordInventoryRow;

		//def ric = new RecordInventoryCommand()
		//cmd.recordInventoryRow = cmd2
		//bindData(ric, params)
		//bindData(cmd.recordInventoryRows, params)
		//bindData(cmd.recordInventoryRow, params)
						
		if (params.save) { 
			inventoryService.saveRecordInventoryCommand(cmd, params)
			if (!cmd.hasErrors())
				redirect(action: showStockCard, params: ['product.id':cmd.product.id])
		}
		
		render(view: "showRecordInventory", model: [commandInstance:cmd])
	}

	
	

	
	def showTransactions = {
		
		def warehouseInstance = Warehouse.get(session?.warehouse?.id)
		def productInstance = Product.get(params?.product?.id)
		def inventoryInstance = warehouseInstance.inventory
		def inventoryItemList = inventoryService.getInventoryItemsByProduct(productInstance)
		def transactionEntryList = TransactionEntry.findAllByProduct(productInstance)
		def inventoryLotList = InventoryLot.findByProduct(productInstance)
		def inventoryLevelInstance = InventoryLevel.findByProductAndInventory(productInstance, inventoryInstance);
		
		[ 	inventoryInstance: inventoryInstance,
			inventoryLevelInstance: inventoryLevelInstance,
			productInstance: productInstance,
			inventoryItemList: inventoryItemList,
			transactionEntryList: transactionEntryList,
			transactionEntryMap: transactionEntryList.groupBy { it.transaction },
			inventoryLotList: inventoryLotList ]
	}	
	
	def showStockCard = { StockCardCommand cmd ->
		
		def warehouseInstance = Warehouse.get(session?.warehouse?.id)
		def productInstance = Product.get(params?.product?.id)
		def inventoryInstance = warehouseInstance.inventory
		def inventoryItemList = inventoryService.getInventoryItemsByProduct(productInstance)
		def transactionEntryList = TransactionEntry.findAllByProduct(productInstance)
		def inventoryLotList = InventoryLot.findByProduct(productInstance)
		def inventoryLevelInstance = InventoryLevel.findByProductAndInventory(productInstance, inventoryInstance);

		// TODO Eventually, we'll push this to the service 

		if (cmd.startDate)
			transactionEntryList = transactionEntryList.findAll{it.transaction.transactionDate >= cmd.startDate}

		if (cmd.endDate)
			transactionEntryList = transactionEntryList.findAll{it.transaction.transactionDate <= cmd.endDate}
			
		if (cmd.transactionType && cmd.transactionType.id != 0)
			transactionEntryList = transactionEntryList.findAll{it.transaction.transactionType == cmd.transactionType}					
		
			
		[ 	inventoryInstance: inventoryInstance, 
			inventoryLevelInstance: inventoryLevelInstance,
			productInstance: productInstance, 
			inventoryItemList: inventoryItemList, 
			transactionEntryList: transactionEntryList,
			transactionEntryMap: transactionEntryList.groupBy { it.transaction },
			inventoryLotList: inventoryLotList,
			commandInstance: cmd ]
	}
		
	def create = {
		def productInstance = Product.get(params?.product?.id)			
		def inventoryInstance = Inventory.get(params?.inventory?.id)
		def itemInstance = new InventoryItem(product: productInstance)				
		def inventoryItems = inventoryService.getInventoryItemsByProduct(productInstance);
		[itemInstance: itemInstance, inventoryInstance: inventoryInstance, inventoryItems: inventoryItems]
	}

	def save = {
		def inventoryItem = new InventoryItem(params)
		def inventoryLot = new InventoryLot(params)		
		def inventoryInstance = Inventory.get(params?.inventory?.id)
		inventoryInstance.properties = params;		

		if (!inventoryItem.hasErrors() && !inventoryLot.hasErrors() && inventoryItem.save() && inventoryLot.save()) { 
			flash.message = "${message(code: 'default.created.message', args: [message(code: 'inventoryItem.label', default: 'Inventory item'), itemInstance.id])}"
			redirect(controller: "inventoryItem", action: "showStockCard", id: itemInstance.id)
		}
		else {
			def inventoryItems = inventoryService.getInventoryItemsByProduct(inventoryItem.product);
			render(view: "create", model: [itemInstance: inventoryItem, inventoryInstance: inventoryInstance, inventoryItems: inventoryItems])
		}
	}
	


	
	
	def edit = {
		def itemInstance = InventoryItem.get(params.id)
		def inventoryInstance = Inventory.get(params?.inventory?.id)
		if (!itemInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'inventoryItem.label', default: 'Inventory item'), params.id])}"
			redirect(action: "show", id: itemInstance.id)
		}
		else {
			return [itemInstance: itemInstance]
		}
	}
	
	def editWarningLevels = {
		def itemInstance = InventoryItem.get(params.id)
		def inventoryInstance = Inventory.get(params?.inventory?.id)
		if (!itemInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'inventoryItem.label', default: 'Inventory item'), params.id])}"
			redirect(action: "show", id: itemInstance.id)
		}
		else {
			return [itemInstance: itemInstance]
		}
	}
	
	def update = {		
		def itemInstance = InventoryItem.get(params.id)
		def inventoryInstance = Inventory.get(params.inventory.id)
		if (itemInstance) {
			if (params.version) {
				def version = params.version.toLong()
				if (itemInstance.version > version) {
					itemInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'inventoryItem.label', default: 'Inventory Item')] as Object[], "Another user has updated this inventory item while you were editing")
					render(view: "show", model: [itemInstance: itemInstance])
					return
				}
			}
			itemInstance.properties = params
			if (!itemInstance.hasErrors() && itemInstance.save(flush: true)) {
				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'inventoryItem.label', default: 'Inventory item'), itemInstance.id])}"
				redirect(controller: "inventoryItem", action: "show", id: itemInstance.id)
			}
			else {
				def transactionEntryList = TransactionEntry.findAllByInventoryItem(itemInstance)
				render(view: "show", model: [itemInstance: itemInstance, transactionEntryList: transactionEntryList])
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'inventoryItem.label', default: 'Inventory item'), params.id])}"
			redirect(action: "show", id: itemInstance.id)
		}
	}
	
	
	
	
	def deleteTransactionEntry = { 
		def transactionEntry = TransactionEntry.get(params.id)
		def productInstance 
		if (transactionEntry) {
			productInstance = transactionEntry.product 
			transactionEntry.delete();
		}
		redirect(action: 'showStockCard', params: ['product.id':productInstance?.id])
	}
	
	def addToInventory = {
		def product = Product.get( params.id )
		render "${product.name} was added to inventory"
		//return product as XML		
	}	
	
	
	def saveInventoryLevel = {
		// Get existing inventory level
		def inventoryLevelInstance = InventoryLevel.get(params.id)		
		def productInstance = Product.get(params?.product?.id)
		//def inventoryInstance = Inventory.get(params?.inventory?.id);

		if (inventoryLevelInstance) { 
			inventoryLevelInstance.properties = params;
		}
		else { 
			inventoryLevelInstance = new InventoryLevel(params);
		}
		
		if (!inventoryLevelInstance.hasErrors() && inventoryLevelInstance.save()) { 
			
		}
		else { 
			flash.message = "error saving inventory levels<br/>" 
			inventoryLevelInstance.errors.allErrors.each { 
				flash.message += it + "<br/>";
			}
		}
		redirect(action: 'showStockCard', params: ['product.id':productInstance?.id])
	}
	
		
	def saveInventoryItem = {
		def inventory = Inventory.get(params.id)
		def inventoryItem = new InventoryItem(params)
		inventory.addToInventoryItem(inventoryItem)
		if(! inventory.hasErrors() && inventory.save()) {
			render template:'inventoryItemRow', bean:inventoryItem, var:'inventoryItem'
		}
	}
	
	def saveInventoryLot = { 
		log.info params
		def productInstance = Product.get(params?.product?.id)
		
		if (productInstance) { 
			Inventory.withTransaction { status ->			
				def inventoryInstance = Inventory.get(params?.inventory?.id);		
				def warehouseInstance = Warehouse.get(session?.warehouse?.id);
				def createdBy = User.get(session?.user?.id);
				
				def inventoryLot = new InventoryLot(params)
				def inventoryItem = new InventoryItem(params);
				def transactionInstance = new Transaction(params);
				def transactionEntry = new TransactionEntry(params);
				
				if (!inventoryItem.validate()) { 
					flash.message = "Inventory item is not valid"	
				}
				
				if (!inventoryLot.validate()) { 
					flash.message = "Inventory lot is not valid"
				}
				
				if (inventoryInstance) { 
					inventoryInstance.addToInventoryItems(inventoryItem);					
					inventoryInstance.addToInventoryLots(inventoryLot);
					
					if (!inventoryInstance.validate()) { 
						flash.message = "Inventory is not valid"			
					}
					if (inventoryInstance.save(flush:true)) { 
						flash.message = "A lot was created successfully"
						
					} else { 
						flash.message = "A lot was not created"
						inventoryInstance.errors.allErrors.each { 
							println it
						}
					}
					
					/*
					transactionInstance.transactionType = TransactionType.findByName("Inventory");
					if (!transactionInstance?.transactionType) {						
						throw new Exception("errors.inventory.TransactionTypeNotFound")
					}
					else { 
						transactionInstance.source = warehouseInstance;
						transactionInstance.destination = warehouseInstance;
						transactionInstance.transactionDate = new Date();
						transactionInstance.createdBy = createdBy;						
						if (!transactionInstance.validate()) { 
							flash.message = "Transaction is not valid";
						}
						inventoryItem = InventoryItem.findByProductAndLotNumber(productInstance, params.lotNumber);
						transactionEntry.inventoryItem = inventoryItem;
						transactionInstance.addToTransactionEntries(transactionEntry).save(flush:true)
					}
					*/						
				}
				else { 
					flash.message = "Inventory with ID ${params.inventory.id} does not exist"
				}		
			}
		}
		else { 
			flash.message = "Unable to locate product with product ID ${params.product.id}"
		}
		redirect(action: 'showStockCard', params: ['product.id':productInstance?.id])
	}
	
	def deleteInventoryLot = { 
		def inventoryLot = InventoryLot.get(params.id);
		def productInstance = inventoryLot?.product;
		
		if (inventoryLot) { 
			def inventoryInstance = Inventory.get(params?.inventory?.id);
			if (inventoryInstance.removeFromInventoryLots(inventoryLot).save(flush:true)) {
				inventoryLot.delete();
			}
			else { 
				flash.message = "could not remove inventory lot"
			}
		}
		else { 
			flash.message = "could not find inventory lot"
		}
		redirect(action: 'showStockCard', params: ['product.id':productInstance?.id])
		
	}

	def deleteInventoryItem = {
		def inventoryItem = InventoryItem.get(params.id);
		def productInstance = inventoryItem?.product;
		def inventoryInstance = Inventory.get(inventoryItem?.inventory?.id);
		
		if (inventoryItem && inventoryInstance) {
			inventoryInstance.removeFromInventoryItems(inventoryItem).save();
			inventoryItem.delete();
		}		
		else {
			inventoryItem.errors.reject("inventoryItem.error", "Could not delete inventory item")
			params.put("product.id", productInstance?.id);
			params.put("inventory.id", inventoryInstance?.id);
			log.info "Params " + params;
			chain(action: "create", model: [inventoryItem: inventoryItem], params: params)
			return;
		}
		redirect(action: 'showStockCard', params: ['product.id':productInstance?.id])
		
	}

		
	/*
	def addTransactionEntry = { 		
		def itemInstance = InventoryItem.get(params?.inventoryItem?.id);		
		if (!itemInstance) { 
			def productInstance = Product.get(params?.product?.id);
			if (productInstance && params.lotNumber) { 
				itemInstance = InventoryItem.findByProductAndLotNumber(productInstance, params.lotNumber)
				if (!itemInstance) { 
					itemInstance = new InventoryItem(product: productInstance, lotNumber: params.lotNumber, 
						inventoryItemType: InventoryItemType.NON_SERIALIZED, active: Boolean.TRUE);					
					itemInstance.save();
				}
			}
			else { 
				
				// error - need to specify product.id and lotNumber

			}			
		}
		else { 		
			
		}
	}
	*/	
	
	
	def saveTransactionEntry = {			
		def productInstance = Product.get(params?.product?.id)				
		if (!productInstance) {
			flash.message = "${message(code: 'default.notfound.message', args: [message(code: 'product.label', default: 'Product'), productInstance.id])}"
			redirect(action: "showStockCard", id: productInstance?.id)
		}
		else { 
			def inventoryItem = InventoryItem.findByProductAndLotNumber(productInstance, params.lotNumber?:null)
			if (!inventoryItem) { 
				flash.message = "${message(code: 'default.notfound.message', args: [message(code: 'inventoryItem.label', default: 'Inventory item'), params.lotNumber])}"
			} 
			else {  
				def transactionInstance = new Transaction(params)
				def transactionEntry = new TransactionEntry(params)
				
				// If we're transferring stock to another location OR consuming stock, 
				// then we need to make sure the quantity is negative
				if (transactionInstance?.destination?.id != session?.warehouse?.id 
					|| transactionInstance?.transactionType?.name == 'Consumption') { 
					if (transactionEntry.quantity > 0) { 
						transactionEntry.quantity = -transactionEntry.quantity;
					}
				}
				
				transactionEntry.inventoryItem = inventoryItem;
				if (!transactionEntry.hasErrors() &&
					transactionInstance.addToTransactionEntries(transactionEntry).save(flush:true)) {
					flash.message = "Saved transaction entry"
				} 
				else {
					transactionInstance.errors.each { println it }
					transactionEntry.errors.each { println it }
					flash.message = "Unable to save transaction entry"
				}
				
				/*
				if (!transactionInstance.hasErrors() && transactionInstance.save(flush:true) {
					def transactionEntry = new TransactionEntry(params)
					transactionEntry.inventoryItem = inventoryItem;					
					if (!transactionEntry.hasErrors() && 
						transactionInstance.addToTransactionEntries(transactionEntry).save(flush:true)) {
						transactionEntry.errors.each { println it }
						flash.message = "Unable to save transaction entry"
					} else { 
						flash.message = "${message(code: 'default.saved.message', args: [message(code: 'inventory.label', default: 'Inventory item'), itemInstance.id])}"					
					}
				}
				else { 
					transactionInstance.errors.each { println it }
					flash.message = "Unable to save transaction"
				}	
				*/
			}
		}
		redirect(action: "showStockCard",  params: ['product.id':productInstance?.id])		
	}
	
	/*
	def recordInventory = {
		log.info "Record inventory: " + params;
		def warehouseInstance = Warehouse.get(session?.warehouse?.id)
		def productInstance = Product.get(params?.productId)
				
		// Populate the model with the following data
		def inventoryInstance = warehouseInstance.inventory
		def inventoryItemList = inventoryService.getInventoryItemsByProduct(productInstance)
		def transactionEntryList = TransactionEntry.findAllByProduct(productInstance)
		def inventoryLotList = InventoryLot.findByProduct(productInstance)
		def inventoryLevelInstance = InventoryLevel.findByProductAndInventory(productInstance, inventoryInstance);
		
		[ 	inventoryInstance: inventoryInstance,
			inventoryLevelInstance: inventoryLevelInstance,
			productInstance: productInstance,
			inventoryItemList: inventoryItemList,
			transactionEntryList: transactionEntryList,
			inventoryLotList: inventoryLotList ]
	}

	def saveInventoryItems = {
		log.info "Save inventory items: " + params;
		def inventoryItem = new InventoryItem(params);
		def inventoryLot = new InventoryLot(params);
		def productInstance = Product.get(params.productId);
		def createdBy = User.get(session?.user?.id);
		
		if (params.quantity <= 0) {
			inventoryItem.errors.rejectValue('quantity', 'inventoryItem.quantity.required',
				[params.quantity] as Object[], 'Quantity is required and must be greater than 0');
		}
		inventoryItem.product = productInstance;
		// Look up lotOrSerialNumber to make sure it doesn't already exist.
		def itemsFound = InventoryItem.findByLotNumber(params.lotNumber);
		if (itemsFound) {
			// Add an error to the model object
			inventoryItem.errors.rejectValue('lotNumber', 'inventoryItem.lotNumber.alreadyExists',
				[params.lotNumber] as Object[], 'Inventory item already exists');
		}
		else {
			def transaction = new Transaction(params);
			transaction.transactionType = TransactionType.get(7);
			transaction.source = Warehouse.get(session.warehouse.id);
			
			def transactionEntry = new TransactionEntry(params);
			transactionEntry.inventoryItem = inventoryItem;
			transactionEntry.product = productInstance;
			transactionEntry.lotNumber = params.lotNumber;
			transactionEntry.quantity = params.quantity;
			transaction.addToTransactionEntries(transactionEntry);
			if (transaction.hasErrors()) {
				inventoryItem.errors = transaction.errors
			}
			else if (transactionEntry.hasErrors()) {
				inventoryItem.errors = transactionEntry.errors
			}
			else {
				if (!inventoryItem.hasErrors() && inventoryItem.save()) {
					flash.message = "Saved inventory item successfully";
					if (!transaction.hasErrors() && transaction.save()) {
						flash.message = "Saved inventory item and transaction successfully";
					}
				}
			}
		}
		
		// Redirect to the record inventory action
		chain(action: recordInventory, model:[productInstance: productInstance,
			inventoryItem: inventoryItem, inventoryLot: inventoryLot], params: params);
	}
	*/
	

}


class StockCardCommand { 
	
	Date startDate = new Date() - 30;		// defaults to today - 30d
	Date endDate = new Date();				// defaults to today
	TransactionType transactionType
	
	static constraints = { 
		startDate(nullable:true)
		endDate(nullable:true)
		transactionType(nullable:true)
	}
	
}
