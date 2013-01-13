/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse

import java.text.SimpleDateFormat;

import grails.converters.*;
import org.pih.warehouse.core.Constants;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.core.Tag;
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.core.Location;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.product.ProductGroup;
import org.pih.warehouse.requisition.Requisition;
import org.pih.warehouse.shipping.Container;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.shipping.ShipmentItem;

class JsonController {
	def inventoryService
	def productService
	def localizationService	
	def shipmentService
	
	def findTags = { 
		def searchTerm = "%" + params.term + "%";
		def c = Tag.createCriteria()
		def tags = c.list {
			projections {
				property "tag"
			}
			ilike("tag", searchTerm)
		}
		
		def results = tags.unique().collect { [ value: it, label: it ] }
		render results as JSON;
	}
	
	def findUnitOfMeasures = {
		def searchTerm = "%" + params.term + "%";
		def c = Product.createCriteria()
		def unitOfMeasures = c.list {
			projections {
				property "unitOfMeasure"
			}
			ilike("unitOfMeasure", searchTerm)
		}
		
		def results = unitOfMeasures.unique().collect { [ value: it, label: it ] }
		render results as JSON;
	}
	
	def findManufacturers = {
		println "find manufactures " + params
		def searchTerm = "%" + params.term + "%";
		def c = Product.createCriteria()
		def manufacturers = c.list {
			projections {
				property "manufacturer"
			}
			ilike("manufacturer", searchTerm)
		}
		
		def results = manufacturers.unique().collect { [ value: it, label: it ] }
		render results as JSON;
	}

	def findProductNames = {
		def searchTerm = "%" + params.term + "%";
		def c = Product.createCriteria()
		def productNames = c.list {
			projections {
				property "name"
			}
			ilike("name", searchTerm)
		}
		
		def results = productNames.unique().collect { [ value: it, label: it ] }
		render results as JSON;
	}
	
	def findPrograms = {
		println "find programs " + params
		def searchTerm = params.term + "%";
		def c = Requisition.createCriteria()
		
		def names = c.list {
			projections {
				property "recipientProgram"
			}
			ilike("recipientProgram", searchTerm)
		}
		// Try again 
		if (names.isEmpty()) { 
			searchTerm = "%" + params.term + "%";
			c = Requisition.createCriteria()
			names = c.list {
				projections {
					property "recipientProgram"
				}
				ilike("recipientProgram", searchTerm)
			}
		}
			
		if (names.isEmpty()) { 			
			names = []
			names << params.term 
		}
		
		def results = names.collect { [ value: it, label: it ] }		
		render results as JSON;
	}
	
	def findRxNormDisplayNames = { 
		println params
		def results = []
		try {
			def url = new URL("http://rxnav.nlm.nih.gov/REST/displaynames")
			def connection = url.openConnection()
			if (connection.responseCode == 200) {
				def xml = connection.content.text
				def list = new XmlParser(false, true).parseText(xml)
				for (item in list.displayTermsList.term) {
					//println "item: " + item
					if (item.text().startsWith(params.term)) { 
						results << item.text()
					}
					
				}
				
				if (results.size() > 10) {
					def remaining = results.size() - 10
					results = results.subList(0,10)
					results << "There are " + remaining + " more items"
				}

			}
		} catch (Exception e) {
			log.error("Error trying to get products from NDC API ", e);
			throw e
		}
		render results as JSON;
	}
	
	
	def getInventoryItem = { 
		render InventoryItem.get(params.id).toJson() as JSON;
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
	
	/**
	 * Ajax method for the Record Inventory page.
	 */
	def getInventoryItems = {
		log.info params
		def productInstance = Product.get(params?.product?.id);
		def inventoryItemList = inventoryService.getInventoryItemsByProduct(productInstance)
		render inventoryItemList as JSON;
	}

	
	/**
	 * Returns inventory items for the given location, lot number, and product.
	 */
	def findInventoryItems = {
		log.info params
		def inventoryItems = []
		def location = Location.get(session.warehouse.id);
		if (params.term) {
			// Improved the performance of the auto-suggest by moving 
			def tempItems = inventoryService.findInventoryItems(params.term)
			
			// Get a map of quantities for all items in inventory
			def quantitiesByInventoryItem = inventoryService.getQuantityForInventory(location?.inventory)
			
			if (tempItems) {

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

	def findRequestItems = {
		
		log.info("find request items by name " + params)
		
		//def items = new TreeSet();
		def items = []
		if (params.term) {
			// Match full name
			def products = Product.withCriteria {
				ilike("name", "%" + params.term + "%")
			}
			items.addAll(products)

			def productGroups = ProductGroup.withCriteria {
				ilike("description", "%" + params.term + "%")
			}
			productGroups.each { items << [id: it.id, name: it.description, class: it.class] }
			//items.addAll(productGroups)

			
			def categories = Category.withCriteria { 
				ilike("name", "%" + params.term + "%")
			}
			items.addAll(categories)
		}
		
		// Convert from products to json objects
		if (items) {
			// Make sure items are unique
			//items.unique();
			items = items.collect() { item ->
				def type = item.class.simpleName
				def localizedName = localizationService.getLocalizedString(item.name)
				// Convert product attributes to JSON object attributes
				[
					value: type + ":" + item.id,
					type: type,
					label: localizedName + "(" + type + ")",
					valueText: localizedName,
				]
			}
		}
		
		if (items.size() == 0) {
			items << [ value: null, label: warehouse.message(code:'product.noProductsFound.message')]
			//items << [value: null, label: params.term]
		}

		log.info "Returning " + items.size() + " results for search " + params.term
		render items as JSON;
	}

	def moveShipmentItemToContainer = {
		log.info "Move shipment item to container: " + params		
		def shipmentItem = ShipmentItem.get(params.shipmentItem);
		def container = Container.get(params.container);
		
		if (shipmentItem) { 
			log.info "Move item " + shipmentItem + " from " + shipmentItem?.container + " to " + container
			shipmentItem.container = container;
			shipmentItem.save(flush:true);
		}
		render shipmentItem as JSON
	}

  
  def searchProduct = {
      def location = Location.get(session.warehouse.id);
      def products = productService.searchProductAndProductGroup(params.term)
      def productIds = products.collect{ it[0]}
      def quantities = inventoryService.getQuantityForProducts(location.inventory, productIds)
      def result = []  
      
      products.each{ productData ->
        if(productData[3] && !result.any{it.id == productData[3] && it.type == "ProductGroup"})
          result.add([id: productData[3], value: productData[2], type:"ProductGroup", group: ""])
        result.add([id: productData[0], value: productData[1], type:"Product", quantity: quantities[productData[0]], group: productData[2] ?: ""])
      }
      //println result
      render result.sort{"${it.group}${it.value}"} as JSON
    }


	def searchPersonByName = {
		def items = []
		def terms = params.term?.split(" ")
		terms?.each{ term ->
			def result = Person.withCriteria {
				or {
					ilike("firstName", term + "%")
					ilike("lastName", term + "%")
					ilike("email", term + "%")
				}
			}
			items.addAll(result)
		}
		items.unique{ it.id }
		def json = items.collect{
			[id: it.id, value: it.name, label: it.name+ " " + it.email]
		}
		render json as JSON
	}

  
	def globalSearch = {
		def items = []
		def terms = params.term?.split(" ")
		terms?.each{ term ->
			def personResults = Person.withCriteria {
				or {
					ilike("firstName", term + "%")
					ilike("lastName", term + "%")
					ilike("email", term + "%")
				}
			}
			items.addAll(personResults)
			
			def productResults = inventoryService.getProductsByTermsAndCategories(terms, [], 25, 0)
			items.addAll(productResults)
			
			def shipmentResults = Shipment.withCriteria {
				or {
					ilike("name", term + "%")
				}
			}
			items.addAll(shipmentResults)
		}
		
		items.unique{ it.id }
		def json = items.collect{
			def type = it.class.simpleName.toLowerCase()
			[id: it.id, type: it.class, url: request.contextPath + "/" + type  + "/redirect/" + it.id, 
				value: it.name  , label: it.name + " [" + type + "]" ]
		}
		render json as JSON
	}
}
