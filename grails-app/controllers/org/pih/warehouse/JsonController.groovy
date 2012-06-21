package org.pih.warehouse

import java.text.ParseException;
import java.text.SimpleDateFormat;

import grails.converters.*;

import org.pih.warehouse.core.Constants;
import org.pih.warehouse.core.DialogForm;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.core.UnitOfMeasure;
import org.pih.warehouse.inventory.Inventory;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.inventory.InventoryService;
import org.pih.warehouse.inventory.TransactionEntry;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.shipping.Container;
import org.pih.warehouse.shipping.ShipmentItem;
import org.pih.warehouse.shipping.Shipper;
import org.pih.warehouse.shipping.ShipperService;
import org.pih.warehouse.shipping.Shipment;

class JsonController {

	def inventoryService
	
	def localizationService
	
	def getInventoryItem = { 
		log.info(params)
		
		def inventoryItem = InventoryItem.get(params.id)
		
		render inventoryItem as JSON;
		
	}
	
	def getQuantity = { 
		log.info params
		def quantity = 0
		def location = Location.get(session.warehouse.id);
		def lotNumber = (params.lotNumber) ? (params.lotNumber) : "";
		def product = (params.productId) ? Product.get(params.productId) : null;
		
		def inventoryItem = inventoryService.findInventoryItemByProductAndLotNumber(product, lotNumber);
		if (inventoryItem) { 
			quantity = inventoryService.getQuantity(location?.inventory, inventoryItem)
		}
		log.info "quantity by lotnumber '" + lotNumber + "' and product '" + product + "' = " + quantity;
		render quantity ?: "N/A";
	}
	
	def getContainers = { 
		log.info("params: " + params);
		def containers = []
		def shipment = Shipment.get(params.id)
		if (shipment) { 
			containers = shipment.containers
		}
		else { 
			containers = Container.list();
		}
		log.info("containers: " + containers)
		render containers as JSON
	}
	
	def searchProductByName = {
		println params
		def results = Product.findAllByNameIlike("%" + params.term + "%")
		println "results >>>>>>>>>>>>>>>> " + results
		
		render(template:'searchResults', model:[searchResults:results])
	}
	
	def sortContainers = { 
		
		def container 
		params.get("container[]").eachWithIndex { id, index ->
			container = Container.get(id)
			container.sortOrder = index 
			container.save(flush:true);
			println ("container " + container.name + " saved at index " + index)
		}
		container.shipment.refresh()
				
		render(text: "", contentType: "text/plain")
		
	}
	
	
	def searchAll = { 
		
		def searchTerm = "%" + params.term + "%";
		def c = Product.createCriteria()
		def productNames = c.list {
			projections {
				property "name"
			}
			ilike("name", searchTerm)
		}

				
		def results = productNames.collect { 
			[ id: '0', value: '', type: 'product', label: it ]
		}
		
		log.info(results);
		
		render results as JSON;

	}
	
	
	def findInventoryItems = {
		log.info params
		def inventoryItems = []
		def location = Location.get(session.warehouse.id);
		if (params.term) {
			// Improved the performance of the auto-suggest by moving 
			def tempItems = inventoryService.findInventoryItems(params.term, params.productId)
			
			// Get a map of quantities for all items in inventory
			def quantitiesByInventoryItem = inventoryService.getQuantityForInventory(location?.inventory)
			
			if (tempItems) {
				/*
				items = items.collect() {
					def quantity = quantitiesByInventoryItem[it]
					quantity = (quantity) ?: 0
					[
						id: it.id,
						value: it.lotNumber,
						label:  it.product.name + " --- Item: " + it.lotNumber + " --- Qty: " + quantity + " --- ",
						valueText: it.lotNumber,
						lotNumber: it.lotNumber,
						product: it.product.id,
						productId: it.product.id,
						productName: it.product.name,
						quantity: quantity,
						expirationDate: it.expirationDate
					]
				}*/
				tempItems.each { 
					def quantity = quantitiesByInventoryItem[it]
					quantity = (quantity) ?: 0
					
					def localizedName = localizationService.getLocalizedString(it.product.name)
					
					if (quantity > 0) { 
						inventoryItems << [
							id: it.id,
							value: it.lotNumber,
							label:  localizedName + " --- Item: " + it.lotNumber + " --- Qty: " + quantity + " --- ",
							valueText: it.lotNumber,
							lotNumber: it.lotNumber,
							product: it.product.id,
							productId: it.product.id,
							productName: localizedName,
							quantity: quantity,
							expirationDate: it.expirationDate
						]
					}	
				}
			}
		}
		if (inventoryItems.size() == 0) { 
			def message = "${warehouse.message(code:'inventory.noItemsFound.message', args: [params.term])}"
			inventoryItems << [id: 'null', value: message]			
		}
		else { 
			inventoryItems.sort { it.productName }
		}
		
		render inventoryItems as JSON;
	}
	
	
	def findLotsByName = { 
		log.info params

		// Constrain by product id if the productId param is passed in		
		def items = new TreeSet();
		if (params.term) {
			def searchTerm = "%" + params.term + "%";
			items = InventoryItem.withCriteria {
				and { 
					or {
						ilike("lotNumber", searchTerm)
					}
					// Search within the inventory items for a specific product
					if (params?.productId) { 
						eq("product.id", params.productId)
					}
				}
			}
			
			def warehouse = Location.get(session.warehouse.id);
			def quantitiesByInventoryItem = inventoryService.getQuantityForInventory(warehouse?.inventory)
			
			if (items) {
				items = items.collect() { item ->
					def quantity = quantitiesByInventoryItem[item]
					quantity = (quantity) ?: 0
					
					def localizedName = localizationService.getLocalizedString(it.product.name)
					
					[
						id: item.id,
						value: item.lotNumber,
						label:  localizedName + " --- Item: " + item.lotNumber + " --- Qty: " + quantity + " --- ",
						valueText: item.lotNumber,
						lotNumber: item.lotNumber,
						expirationDate: item.expirationDate
					]
				}
			}
		}
		render items as JSON;
	}
	

	def findShipmentByName = {
		log.info "Find shipment by name " + params;
		def finalItems = []
		
		def items = new TreeSet();
		if (params.term) {
			items = Shipment.withCriteria {
				or {
					ilike("name", "%" + params.term + "%")
					//ilike("shipmentNumber", "%" + params.term + "%")
					destination {
						ilike("name", params.term + "%")
					}
					origin {
						ilike("name", params.term + "%")
					}
				}
			}
				
			if (items) {
				
				items.each { shipment ->
					
					if (!shipment?.hasShipped() && !shipment?.hasArrived()) { 
						finalItems << [ value: shipment.id,
							type: "shipment", 
							label: shipment.name + " [" + shipment?.status + "]",
							valueText: shipment.name, 
							icon: ""
							]
						
						shipment.containers.each { 
							
							def localizedContainerName = localizationService.getLocalizedString(it.containerType.name)
							
							finalItems << [ 
								value: it.id, 
								type: "container", 
								label: "    * " + shipment.name + " > " + localizedContainerName + " " + it.name, 
								valueText: it.shipment.name + " &rsaquo; " + localizedContainerName + " " + it.name 
							]
						}
					}					
				}
			}
		}
		render finalItems as JSON;
	}
	
	
	def findCategoryByName = { 
		log.info params
		def items = new TreeSet();
		if (params.term) {
			items = Category.withCriteria {
				ilike("name", params.term + "%")
			}
			if (items) {
				items = items.collect() {
					
					def localizedName = localizationService.getLocalizedString(it.name)
					
					[	value: localizedName,
						label: localizedName,
						valueText: localizedName,
						desc: localizedName,
						icon: "none" 	]
				}
			}
		}
		render items as JSON;
	}	
	
	
	def findUnitOfMeasureByName = { 
		log.info params
		def items = new TreeSet();
		if (params.term) {
			items = UnitOfMeasure.withCriteria {
				or {
					ilike("name", "%" +  params.term + "%")
				}
			}
			if (items) {
				items = items.collect() {
					
					def localizedName = localizationService.getLocalizedString(it.name)
					
					[	value: it.id,
						label: localizedName,
						valueText: localizedName,
						desc: localizedName,
						icon: "none" 	]
				}
			}
		}
		render items as JSON;
	}

	def findDosageFormByName = {
		log.info params
		def items = new TreeSet();
		if (params.term) {
			items = DosageForm.withCriteria {
				or {
					ilike("name", "%" +  params.term + "%")
				}
			}
			if (items) {
				items = items.collect() {
					[	value: it.id,
						label: it.name,
						valueText: it.name,
						desc: it.name,
						icon: "none" 	]
				}
			}
		}
		render items as JSON;
		
		
	}
	
		
	def findShipperServiceByName = {
		log.info params
		def items = new TreeSet();
		if (params.term) {
			items = ShipperService.withCriteria {
				or {
					ilike("name", "%" +  params.term + "%")					
					ilike("description", "%" +  params.term + "%")
					shipper { 
						ilike("name", "%" +  params.term + "%")
					}
				}
			}
			if (items) {
				items = items.collect() {
					[	value: it.id,
						label: it.shipper.name + " " + it.name,
						valueText: it.shipper.name + " " + it.name,
						desc: it.description,
						icon: "none"]
				}
			}
		}
		render items as JSON;
	}
	
	def findShipperByName = {
		log.info params
		def items = new TreeSet();
		if (params.term) {
			items = Shipper.withCriteria {
				or {
					ilike("name", "%" +  params.term + "%")
					ilike("description", "%" +  params.term + "%")
				}
			}
			if (items) {
				items = items.collect() {
					[	value: it.id,
						label: it.name,
						valueText: it.name,
						desc: it.description,
						icon: ""]
				}
			}
		}
		render items as JSON;
	}
	
	def findPersonByName = {
		log.info "findPersonByName: " + params
		def items = new TreeSet();
		try {
			
			if (params.term) {
						
				def terms = params.term.split(" ")				
				for (term in terms) { 						
					items = Person.withCriteria {
						or {
							ilike("firstName", term + "%")
							ilike("lastName", term + "%")
							ilike("email", term + "%")
						}
					}
				}
							
				if (items) {
					items.unique();
					items = items.collect() {						
						
						[	value: it.id,
							valueText: it.name,
							label:  "" + it.firstName + " " + it.lastName + " " +  it.email + " ",
							desc: (it?.email) ? it.email : "",
						]
					}
				}
				else {
					def item =  [
						value: "null",
						valueText : params.term,						
						label: "${warehouse.message(code: 'person.doesNotExist.message', args: [params.term])}",
						desc: params.term,
						icon: ""
					];
					items.add(item)
				}
				
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		
		}
		render items as JSON;
	}
		
	
	def findInventoryItem = { 
		log.info("params: " + params);
		def product = Product.get(Integer.parseInt(params.productId));
		def inventoryItem = inventoryService.findInventoryItemByProductAndLotNumber(product, params.lotNumber?:null);
		
		// We need to pass the inventory.id param
		//def inventory = Inventory.get(Integer.parseInt(params?.inventory?.id));
		//def quantity = getQuantity(inventory, inventoryItem);
		def data = [ status: true, inventoryItem: inventoryItem, product: product, quantity: 0 ];
		render data  as JSON
	}
	
	

	
	def findProduct = { 
		log.info (params);
		def product = Product.get(params.id)
		def data = []
		if (!product) { 
			data = [ status: false, message: "Error attempting to find product with ID " + params.id ]
		}
		else { 
			data = [ status: true, product: product]
		}
		render data as JSON;
	}
	
	
	def findProductByName = {
		
		log.info("find products by name " + params)
		def dateFormat = new SimpleDateFormat(Constants.SHORT_MONTH_YEAR_DATE_FORMAT);
		def products = new TreeSet();
		
		if (params.term) {			
			// Match full name
			products = Product.withCriteria { 
				ilike("name", "%" + params.term + "%")
			}
		}
		
		def location = Location.get(params.warehouseId);
		log.info ("warehouse: " + location);
		def quantityMap = 
			inventoryService.getQuantityForInventory(location?.inventory)		
			
		// FIXME Needed to create a new map with inventory item id as the index 
		// in order to get the quantity below.  For some reason, the inventory item 
		// object was getting toString()'d when used below as a key and therefore
		// the keys were mismatched and the quantity was always null.
		def idQuantityMap = [:]
		quantityMap.keySet().each { 
			idQuantityMap[it.id] = quantityMap[it]
		}
		
		// Convert from products to json objects 
		if (products) {
			// Make sure items are unique
			products.unique();
			products = products.collect() { product ->
				def productQuantity = 0;
				// We need to check to make sure this is a valid product
				def inventoryItemList = []
				if (product.id) { 
					def inventoryItems = InventoryItem.findAllByProduct(product);
					inventoryItemList = inventoryItems.collect() { inventoryItem ->
						// FIXME Getting the quantity from the inventory map does not work at the moment
						def quantity = idQuantityMap[inventoryItem.id]?:0;
						
						// Create inventory items object
						//if (quantity > 0) { 
							[	
								id: inventoryItem.id?:0, 
								lotNumber: (inventoryItem?.lotNumber)?:"", 
								expirationDate: (inventoryItem?.expirationDate) ? 
									(dateFormat.format(inventoryItem?.expirationDate)) : 
									"${warehouse.message(code: 'default.never.label')}", 
								quantity: quantity
							] 
						//}
					}
					
					// Sort using First-expiry, first out policy
					inventoryItemList = inventoryItemList.sort { it?.expirationDate }
				}
				
				def localizedName = localizationService.getLocalizedString(product.name)
				
				
				// Convert product attributes to JSON object attributes
				[	
					product: product,
					category: product?.category,
					quantity: productQuantity,
					value: product.id,
					label: localizedName,
					valueText: localizedName,
					desc: product.description,
					inventoryItems: inventoryItemList,
					icon: "none"
				]
			}
		}
		
		if (products.size() == 0) { 
			products << [ value: null, label: warehouse.message(code:'product.noProductsFound.message')]
		}

		log.info "Returning " + products.size() + " results for search " + params.term
		render products as JSON;
	}
	

	

		
	

	def savePerson = {
		log.info("save person" + params)	
		def personInstance = new Person(params)
		personInstance.save(flush: true)	
		render prepareDialogForm(personInstance) as JSON
	}

	def moveItemToContainer = { 
		log.info params
		
		def itemInstance = ShipmentItem.get(params.item);
		def containerInstance = Container.get(params.container);
		
		if (itemInstance && containerInstance) { 
			log.info "move item " + itemInstance + " from " + itemInstance?.container + " to " + containerInstance
			itemInstance.container = containerInstance;
			itemInstance.save();
		}		
		render itemInstance as JSON
	}
	
	
	def saveBox = {
		log.info("save box " + params)
		def shipmentInstance = Shipment.get(params["shipmentId"]);
		def containerInstance = new Container(params);
		if (containerInstance) {
			log.info("add container");
			shipmentInstance.addToContainers(containerInstance).save(flush:true);
			log.info("added container");
		}
		else {
			log.info("could not add container")
		}
		render prepareDialogForm(containerInstance) as JSON
	}
	
	def saveItem = {
		log.info("save item" + params)		
		//def itemInstance = null;
		def itemInstance = new ShipmentItem(params);	
		def shipmentInstance = Shipment.get(params["shipmentId"]);
		
		if (shipmentInstance) { 
			log.info("shipment is not null")
			shipmentInstance.expectedDeliveryDate = new Date();
			shipmentInstance.save(flush:true);
		}
		
		def containerInstance = Container.get(params["containerId"]);
		def productInstance = Product.get(params["productId"])
		def recipientInstance = Person.get(params["recipientId"]);
		def quantity = (params.quantity) ? Integer.parseInt(params.quantity.trim()) : 1;		
		if (containerInstance) { 
			itemInstance = new ShipmentItem(product: productInstance, quantity: quantity, 
				recipient: recipientInstance, lotNumber: params.lotNumber);
			
			if (itemInstance) { 
				log.info("add item to container");
				containerInstance.shipment.addToShipmentItems(itemInstance).save(flush:true);
				shipmentInstance.save(flush:true);
				log.info("added item to container");
			}
			else { 
				log.info("could not create shipment item");
			}
		} 
		else { 
			log.info("could not find container")
		}
		
		
		// Create a new unverified product
		/*
		if (!productInstance) {
			productInstance = new Product(name: params.selectedItem.name);
			if (!productInstance.hasErrors() && productInstance.save(flush: true)) {
				flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'container.label', default: 'Product'), product.id])}"
				log.info("saved product")
			}
			else {
				log.info("error saving product")
				// Encountered an error with saving the product
				//redirect(action: "editContents", id: shipment.id, params: ["container.id": container?.id])
				return;
			}
		}*/
		
		
		
		
		/*
		// Add item to container if product doesn't already exist
		if (containerInstance) {
			def oldQuantity = 0;
			def newQuantity = 0;
			boolean found = false;
			containerInstance.shipmentItems.each {
				if (it.product == productInstance) {
					oldQuantity = it.quantity;
					it.quantity += quantity;
					newQuantity = it.quantity;
					it.save();
					found = true;
				}
			}
			if (!found) {
				itemInstance = new ShipmentItem(product: productInstance, quantity: quantity, recipient: recipientInstance);
				containerInstance.shipment.addToShipmentItems(itemInstance).save(flush:true);
			}
			else {
				flash.message = "Modified quantity of existing shipment item ${productInstance.name} from ${oldQuantity} to ${newQuantity}"
			}
		}*/
		
		render prepareDialogForm(itemInstance) as JSON
	}

	
	def prepareDialogForm(domainInstance) {
		//def g = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib')
		def dialogForm = new DialogForm()
		if (domainInstance.hasErrors()) {
			g.eachError(bean: domainInstance) {
				log.info("error: " + it.field + it)
				dialogForm.errors."${it.field}" = g.message(error: it)
			}
			dialogForm.success = false
			dialogForm.message = "There was an error"
		} else {
			dialogForm.success = true
			dialogForm.message = "Success"
		}
		dialogForm.domainInstance = domainInstance
		return dialogForm
	}
	
	
	def deleteTransactionEntry = { 
		log.info "delete transaction entry " + params;
		def transactionEntry = TransactionEntry.get(params.id);
		if (transactionEntry) { 
			def transaction = transactionEntry.transaction;
			transaction.removeFromTransactionEntries(transactionEntry);
			transactionEntry.delete();
			def result = [ success: true, transactionEntry: transactionEntry ]
			render result as JSON
		}
		else { 
			response.setStatus(200);
			response.setContentType('text/plain')
			response.outputStream << 'Sorry, there was an error while attempting to delete this transaction entry. Please try again or contact your system administrator.' 
		}
		
	}
	
	
	
}
