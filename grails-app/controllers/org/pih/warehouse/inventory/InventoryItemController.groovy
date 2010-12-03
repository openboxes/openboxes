package org.pih.warehouse.inventory;

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
	
	def showStockCard = {
		def warehouseInstance = Warehouse.get(session?.warehouse?.id)
		def productInstance = Product.get(params?.product?.id)
		def inventoryInstance = warehouseInstance.inventory
		def inventoryItemList = inventoryService.getInventoryItemsByProduct(productInstance)
		def transactionEntryList = TransactionEntry.findAllByProduct(productInstance)
		def inventoryLotList = InventoryLot.findByProduct(productInstance)
		
		[ inventoryInstance: inventoryInstance, productInstance: productInstance, inventoryItemList: inventoryItemList, transactionEntryList: transactionEntryList,
			inventoryLotList: inventoryLotList ]
	}
	
	
	def create = {
		try { 
			def productInstance = Product.get(params?.product?.id)		
			
			if (productInstance) { 
				def inventoryLots = Collections.nCopies( 5, new InventoryLot(product: productInstance, initialQuantity: 0) )
				
				def inventoryInstance = Inventory.get(params?.inventory?.id)
				//inventoryInstance.inventoryLots = inventoryLots;
				
				def itemInstance = new InventoryItem(product: productInstance)
				
				println "lots: " + inventoryInstance?.inventoryLots?.size()
				
				[itemInstance: itemInstance, inventoryInstance: inventoryInstance, inventoryLots: inventoryLots]
			}
			else { 
				flash.message = "Unable to find the given product"
				redirect(controller: "inventory", action: "browse")
			}
		} catch (Exception e) { 
			log.error("error creating new inventory item", e);
		}
	}

	def save = {
		def itemInstance = new InventoryItem(params)
		def inventoryInstance = Inventory.get(params?.inventory?.id)
		inventoryInstance.properties = params;
		println "lots: " + inventoryInstance?.inventoryLots?.size()
		if (inventoryInstance) { 
			def lotsWithoutQuantity = []
			inventoryInstance?.inventoryLots.each { 
				if (it.initialQuantity <= 0) { 
					lotsWithoutQuantity << it
				}
			}
			inventoryInstance?.inventoryLots.removeAll(lotsWithoutQuantity);
		}
		
		println "lots: " + inventoryInstance?.inventoryLots?.size()
		
		if (!itemInstance.hasErrors() && itemInstance.save(flush:true)) {
			flash.message = "${message(code: 'default.created.message', args: [message(code: 'inventoryItem.label', default: 'Inventory item'), itemInstance.id])}"
			redirect(controller: "inventoryItem", action: "show", id: itemInstance.id)
			//render(view: "create", model: [itemInstance: itemInstance])
		}
		else {
			
			[itemInstance: itemInstance, inventoryInstance: inventoryInstance]			
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
		if (transactionEntry) { 
			transactionEntry.delete();
		}
		redirect(action: "show", id: params.inventoryItem.id)
	}
	
	def addToInventory = {
		def product = Product.get( params.id )
		render "${product.name} was added to inventory"
		//return product as XML		
	}	
	
	def postInventoryItem = {
		def inventory = Inventory.get(params.id)
		def inventoryItem = new InventoryItem(params)
		inventory.addToInventoryItem(inventoryItem)
		if(! inventory.hasErrors() && inventory.save()) {
			render template:'inventoryItemRow', bean:inventoryItem, var:'inventoryItem'
		}
	}
	
	def postInventoryLot = { 
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
		
		if (inventoryItem) {
			def inventoryInstance = Inventory.get(params?.inventory?.id);
			inventoryInstance.removeFromInventoryItems(inventoryItem).save();
			inventoryItem.delete();
		}
		else {
			flash.message = "could not find inventory item"
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
	
	def postTransactionEntry = {				
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
						flash.message = "${message(code: 'default.updated.message', args: [message(code: 'inventory.label', default: 'Inventory item'), itemInstance.id])}"					
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

}
