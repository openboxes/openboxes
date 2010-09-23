package org.pih.warehouse

import grails.converters.*;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.inventory.Warehouse;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.shipping.Shipper;
import org.pih.warehouse.shipping.ShipperService;
import org.pih.warehouse.shipping.Shipment;



class JsonController {

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
							ilike("firstName", "%" + term + "%")
							ilike("lastName", "%" + term + "%")
							ilike("email", "%" + term + "%")
						}
					}
				}
							
				if (items) {
					items.unique();
					items = items.collect() {						
						
						[	value: it.id,
							valueText: (it?.email) ? it.email : it.firstName + " " + it.lastName,
							label:  "" + it.firstName + " " + it.lastName + "&nbsp;&lt;" +  it.email + "&gt;",
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
	
	
	
	def findProductByName = {
		log.info params
		def items = new TreeSet();
		
		if (params.term) {			
			
			// Match full name
			items = Product.withCriteria { 
				ilike("name", "%" + params.term + "%")				
			}
			
			// If no items found, we search by category, product type, name, upc
			if (!items) { 
				def terms = params.term.split(" ")
				for (term in terms) {
					items = Product.withCriteria {
						or {
							ilike("name", "%" + term + "%")
							ilike("upc", "%" + term + "%")
							categories { 
								ilike("name", "%" + term + "%")
							}
							productType { 							
								ilike("name", "%" + term + "%")
							}
						}
					}
				}
			}
		}

		if (!items) { 
			items.add(new Product(name: "<i>No matching products</i>"))
			//items.addAll(Product.getAll());		
		}
		
		
		if (items) {
			// Make sure items are unique
			items.unique();
			items = items.collect() {
				[	value: it.id,
					valueText: it.name,
					label: it.name,
					desc: it.description,
					icon: "none"]
			}
		}
		else {
			def item =  [
				value: null,
				valueText : params.term,
				label: "Add a new product '" + params.term + "'?",
				desc: params.term,
				icon: "none"
			];
			items.add(item)
		}
		render items as JSON;
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
				[id:it.id, name:it.name]
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
	
	
	def savePerson = {		
		def personInstance = new Person(params)
		if (!personInstance.hasErrors() && personInstance.save(flush: true)) {
			render personInstance as JSON;
		}
		else {
			render(view: "createPerson", model: [personInstance : personInstance]);
		}
	}
	
}
