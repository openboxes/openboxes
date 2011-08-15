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
import org.pih.warehouse.inventory.Warehouse;
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
	
	def getQuantity = { 
		log.info params
		def quantity = 0
		def warehouse = Warehouse.get(session.warehouse.id);
		def lotNumber = (params.lotNumber) ? (params.lotNumber) : "";
		def product = (params.productId) ? Product.get(params.productId as Integer) : null;
		
		log.info "find by lotnumber '" + lotNumber + "' and product '" + product + "'";
		def item = inventoryService.findInventoryItemByProductAndLotNumber(product, lotNumber);
		if (item) { 
			quantity = inventoryService.getQuantityForInventoryItem(item, warehouse?.inventory)
		}
		render quantity;
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
		def warehouse = Warehouse.get(session.warehouse.id);
		if (params.term) {
			
			// Improved the performance of the auto-suggest by moving 
			def tempItems = inventoryService.findInventoryItems(params.term, params.productId)
			
			// Get a map of quantities for all items in inventory
			def quantitiesByInventoryItem = inventoryService.getQuantityForInventory(warehouse?.inventory)
			
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
		render inventoryItems as JSON;
	}
	
	
	def findLotsByName = { 
		log.info params

		// Constrain by product id if the productId param is passed in
		def productId = null;		
		try { 			
			productId = new Long(params.productId);
		} 
		catch (NumberFormatException e) { /* ignore */ }
		
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
						eq("product.id", productId)
					}
				}
			}
			
			def warehouse = Warehouse.get(session.warehouse.id);
			def quantitiesByInventoryItem = inventoryService.getQuantityForInventory(warehouse?.inventory)
			
			if (items) {
				items = items.collect() {
					def quantity = quantitiesByInventoryItem[it]
					quantity = (quantity) ?: 0
					
					def localizedName = localizationService.getLocalizedString(it.product.name)
					
					[
						value: it.lotNumber,
						label:  localizedName + " --- Item: " + it.lotNumber + " --- Qty: " + quantity + " --- ",
						valueText: it.lotNumber,
						lotNumber: it.lotNumber,
						expirationDate: it.expirationDate,
						id: it.id
					]
				}
			}
			// Add the user-entered lot number to the list 
			//items << [ value: params.term, label: params.term, valueText: params.term, lotNumber: params.term ]
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
					ilike("shipmentNumber", "%" + params.term + "%")
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
							label: shipment.name + " [" + 
							shipment.shipmentNumber + "] - " + shipment?.status,
							valueText: shipment.name, 
							icon: "<img src=\"/warehouse/images/icons/silk/add.png\" />"
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
				/*
				items = items.collect() { shipment ->					
					[
						value: shipment.id,
						label: shipment.name + " [" + shipment.shipmentNumber + "]",
						valueText: shipment.name
					]
					
					
				}
				*/
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
						icon: "none"]
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
							desc: (it?.email) ? it.email : "no email",
						]
					}
				}
				/*
				else {
					def item =  [
						value: null,
						valueText : params.term,
						label: "Add new person '" + params.term + "'?",
						desc: params.term,
						icon: "none"
					];
					items.add(item)
				}
				*/
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		
		}
		render items as JSON;
	}
		
	
	def findInventoryItem = { 
		log.info("params: " + params);
		def product = Product.get(Integer.parseInt(params.productId));
		def inventoryItem = inventoryService.findByProductAndLotNumber(product, params.lotNumber?:null);
		
		// We need to pass the inventory.id param
		//def inventory = Inventory.get(Integer.parseInt(params?.inventory?.id));
		//def quantity = getQuantityForInventoryItem(inventoryItem, inventory);
		def data = [ status: true, inventoryItem: inventoryItem, product: product, quantity: 0 ];
		render data  as JSON
	}
	
	def findProduct = { 
		log.info (params);
		def product = Product.get(params.id)
		def data = []
		if (!product) { 
			data = [ status: false, message: "Error attempting to locate product " + params.id ]
		}
		else { 
			data = [ status: true, product: product]
		}
		render data as JSON;
	}
	
	
	def findProductByName = {
		
		log.debug(params)
		def dateFormat = new SimpleDateFormat(Constants.DEFAULT_MONTH_YEAR_DATE_FORMAT);
		def products = new TreeSet();
		
		if (params.term) {			
			// Match full name
			products = Product.withCriteria { 
				ilike("name", "%" + params.term + "%")
			}
		}
		
		// Add the search term to the list of items returned
		products << [ name: params.term ]
		
		def warehouse = Warehouse.get(params.warehouseId);
		log.info ("warehouse: " + warehouse);
		Map<InventoryItem, Integer> quantityMap = inventoryService.getQuantityForInventory(warehouse?.inventory)		
			
		// Convert from products to json objects 
		if (products) {
			// Make sure items are unique
			products.unique();
			products = products.collect() { product ->
				def productQuantity = 0;
				// We need to check to make sure this is a valid product
				def inventoryItems = []
				if (product.id) { 
					inventoryItems = InventoryItem.findAllByProduct(product);
					inventoryItems = inventoryItems.collect() { inventoryItem ->
						
						// FIXME Getting the quantity from the inventory map does not work at the moment
						def quantity = quantityMap[inventoryItem]?:0;
						productQuantity += quantity;
						
						// Create inventory items object
						if (quantity > 0) { 
							[	
								id: inventoryItem.id?:0, 
								lotNumber: (inventoryItem?.lotNumber)?:"", 
								expirationDate: (inventoryItem?.expirationDate) ? (dateFormat.format(inventoryItem?.expirationDate)) : "never", 
								quantity: quantity
							] 
						}
					}
				}
				
				def localizedName = localizationService.getLocalizedString(product.name)
				
				// Convert product attributes to JSON object attributes
				[	
					product: product,
					quantity: productQuantity,
					value: product.id,
					label: localizedName,
					valueText: localizedName,
					desc: product.description,
					inventoryItems: inventoryItems,
					icon: "none"
				]
			}
		}

		render products as JSON;
	}
	

	def findWarehouseByName = {
		log.info params
		def items = new TreeSet();
		if (params.term) {
			items = Warehouse.withCriteria {
				or {
					ilike("name", "%" +  params.term + "%")
				}
			}
			if (items) {
				items = items.collect() {
					[	value: it.id,
						valueText: it.name,
						label: "<img src=\"/warehouse/warehouse/viewLogo/" + it.id + "\" width=\"24\" height=\"24\" style=\"vertical-align: bottom;\"\"/>&nbsp;" + it.name,
						desc: it.name,
						icon: "<img src=\"/warehouse/warehouse/viewLogo/" + it.id + "\" width=\"24\" height=\"24\" style=\"vertical-align: bottom;\"\"/>"]
				}
			}
			/*
			else {
				def item =  [
					value: 0,
					valueText : params.term,
					label: "Add a new warehouse for '" + params.term + "'?",
					desc: params.term,
					icon: "none"
				];
				items.add(item)
			}*/
		}
		render items as JSON;
	}


	def availableItems = {
		log.debug params;
		def items = null;
		if (params.query) {
			
			//String [] parts = params.query.split(" ");
			
			//items = Product.findAllByNameLike("%${params.query}%", [max:10, offset:0, "ignore-case":true]);
			items = Product.withCriteria {
				or {
					ilike("name", "%${params.query}%")
					ilike("description", "%${params.query}%")
				}
			}
			
			items = items.collect() {
				def localizedName = localizationService.getLocalizedString(it.name)
				[id:it.id, name:localizedName]
			}
		}
		def jsonItems = [result: items]
		render jsonItems as JSON;
	}
		
	
	def availableContacts = {
		def contacts = null;
		if (params.query) {
			contacts = Contact.withCriteria {
				or {
					ilike("name", "%${params.query}%")
					ilike("email", "%${params.query}%")
					ilike("phone", "%${params.query}%")
					ilike("firstName", "%${params.query}%")
					ilike("lastName", "%${params.query}%")
				}
			}
			
			contacts = contacts.collect() {
				[id : it.id, name : it.name]
			}
		}
		def jsonItems = [result: contacts]
		render jsonItems as JSON;
	}

	def availableShipments = {
		log.debug params;
		def items = null;
		if (params.query) {
			items = Shipment.findAllByNameLike("%${params.query}%", [max:10, offset:0, "ignore-case":true]);
			items = items.collect() {
				[id:it.id, name:it.name]
			}
		}
		def jsonItems = [result: items]
		render jsonItems as JSON;
	}
	
	/*
	def savePerson = {	
		log.info("save person" + params)	
		def personInstance = new Person(params)
		if (!personInstance.hasErrors() && personInstance.save(flush: true)) {
			log.info("saved")
			render personInstance as JSON;
		}
		else {
			log.info("errors")
			render("there was an error");
		}
	}*/
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
