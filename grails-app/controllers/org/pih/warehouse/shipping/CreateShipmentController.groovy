package org.pih.warehouse.shipping

import grails.converters.JSON;

import org.pih.warehouse.core.DialogForm;
import org.pih.warehouse.core.Address;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.Event;
import org.pih.warehouse.core.EventType;
import org.pih.warehouse.core.Comment;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.core.User;
import org.pih.warehouse.product.Product;

class CreateShipmentController {
	
	def mailService;
	
	def index = {
		redirect(action:'suitcase')
	}
	
	
	
	def suitcaseFlow = {
		start {
			action {
				log.info("suitcaseFlow started!!!")
				if (!flow.shipmentInstance) { 
					if (params.id) { 
						flow.shipmentInstance = Shipment.get(params.id);
					} 
					else { 
						flow.shipmentInstance = new Shipment();
					}
				}
								
				ShipmentType shipmentType = ShipmentType.findByName("Suitcase");
				if (!shipmentType)
					throw new Exception("Unable to find 'Suitcase' shipment type")
				flow.shipmentInstance.shipmentType = shipmentType;
				return success();
			}
			on("success").to "enterShipmentDetails"
			on("error").to "enterShipmentDetails"
			on(Exception).to "handleError"
		}
		enterShipmentDetails {
			on("cancel").to "cancel"
			on("submit") {
				def shipmentInstance = Shipment.get(params.id)
				if (!shipmentInstance)
					shipmentInstance = new Shipment()
			
				shipmentInstance.properties = params
				flow.shipmentInstance = shipmentInstance
				
				if(shipmentInstance.hasErrors() || !shipmentInstance.validate()) 
					return error()

				// Save the current instance
				shipmentInstance?.save(flush:true);		
				
				// Add a new event to the shipment to mark it as Requested
				EventType eventType = EventType.findByName("Requested")
				if (eventType) {
					boolean exists = Boolean.FALSE;
					// If 'requested' event type already exists, return
					shipmentInstance?.events.each {
						if (it.eventType == eventType) exists = Boolean.TRUE;
					}
					// Avoid duplicate events 
					if (!exists) {
						def event = new Event(
							eventDate: new Date(),
							eventType: eventType,
							eventLocation: Location.get(session.warehouse.id)).save(flush:true);
						shipmentInstance.addToEvents(event).save(flush:true);
					}
				}
				flow.shipmentInstance = shipmentInstance		
				
				//flash.message = "test";
				
			}.to "enterTravelerDetails"
			on("finish").to "finish"
			on("enterShipmentDetails").to "enterShipmentDetails"
			on("enterTravelerDetails").to "enterTravelerDetails"
			on("enterContainerDetails").to "enterContainerDetails"
			on("reviewShipment").to "reviewShipment"
			on("sendShipment").to "sendShipment"
			on("done").to "redirectToShowDetails"
			
			on("return").to "start"			
			on(Exception).to "handleError"
		}
		enterTravelerDetails {
			on("cancel").to "cancel"
			on("back").to "enterShipmentDetails"
			on("submit") { 
				log.info("params = " + params)
				def shipmentInstance = Shipment.get(params.id)
				if (!shipmentInstance) 
					return error();
					
				shipmentInstance.properties = params
				flow.shipmentInstance = shipmentInstance

				
				if(shipmentInstance.hasErrors() || !shipmentInstance.validate())
					return error()


				// Save the current instance
				shipmentInstance?.save(flush:true);
				
				
			}.to "enterContainerDetails"
			on("enterShipmentDetails").to "enterShipmentDetails"
			on("enterTravelerDetails").to "enterTravelerDetails"
			on("enterContainerDetails").to "enterContainerDetails"
			on("reviewShipment").to "reviewShipment"
			on("sendShipment").to "sendShipment"
			on("done").to "redirectToShowDetails"
		}
		enterContainerDetails  {
			on("cancel").to "cancel"
			on("back").to "enterTravelerDetails"
			on("clear").to "clear"
			on("removeContents") { 
				def shipmentInstance = Shipment.get(params.id)
				shipmentInstance.containers.each { 
					it.delete();
				}					
				shipmentInstance.save(flush:true);
				flow.shipmentInstance = shipmentInstance;
				
			}.to "enterContainerDetails"
			on("addSuitcase") {				
				def shipmentInstance = Shipment.get(params.id)
				def sortOrder = (shipmentInstance?.containers)?shipmentInstance?.containers?.size()+1:1
				def name = String.valueOf(sortOrder)
				
				def containerInstance = new Container(
					name: name, 
					containerType:ContainerType.findByName("Suitcase"), 
					shipment: shipmentInstance,
					sortOrder: sortOrder)

				flow.containerInstance = containerInstance
				
				// Errors 
				if(containerInstance.hasErrors() || !containerInstance.validate()) { 
					return error()
				}
				// No errors
				else { 
					
					log.info("shipment: " + shipmentInstance)
					log.info("containers: " + shipmentInstance.containers)
					shipmentInstance?.addToContainers(containerInstance)	
					containerInstance.save(flush:true);
					
					//if(!params.id)	return error()
					//def containers = flow.containers
					//if(!containers) containers = [] as HashSet
					//containers << containerInstance
					//flow.shipmentInstance.containers = containers
					//flow.containers = containers
					//flow.containerInstance = null
					
				}
			}.to "enterContainerDetails"		
			on("removeBox") { 
				def suitcaseInstance = Container.get(params?.suitcase?.id)
				def boxInstance = Container.get(params?.box?.id)
				if (suitcaseInstance && boxInstance) { 
					if (!boxInstance?.shipmentItems) { 
						suitcaseInstance.removeFromContainers(boxInstance).save(flush:true);
						return error();
					}
					else { 
						flash.message = "Sorry, you cannot remove a box that contains items.";
						return error();
					}
				} 
				else { 
					flash.message = "Sorry, you cannot locate the selected suitcase or box.";
					return error();
				}				
				
				
			}.to "enterContainerDetails"
			on("addBox") {
				log.info("add a new box for container: " + params?.suitcase?.id);
				def shipmentInstance = Shipment.get(params.id)
				def suitcaseInstance = Container.get(params?.suitcase?.id)
				log.info("suitcase instance: " + suitcaseInstance);
				
				if (suitcaseInstance) { 
					log.info("boxes: " + suitcaseInstance?.containers)
					def sortOrder = (suitcaseInstance?.containers)?suitcaseInstance?.containers?.size()+1:1
					def name = String.valueOf(sortOrder)
					
					//def name = String.valueOf(suitcaseInstance?.containers()?suitcaseInstance?.containers.size():1);
					def boxInstance = new Container(
						name: name, 
						sortOrder: sortOrder, 
						shipment: shipmentInstance,
						containerType:ContainerType.findByName("Box"));
					
					suitcaseInstance?.addToContainers(boxInstance)
					if (!suitcaseInstance?.save(flush:true)) { 
						return error();
					} 
									
				} else { 
					return error();
				}				
			}.to "enterContainerDetails"
			on("removeItem") {
				log.info("params" + params)
				def shipmentInstance = Shipment.get(params?.shipment?.id);
				def itemInstance = ShipmentItem.get(params?.id);
				if (shipmentInstance) { 
					if (itemInstance) {
						itemInstance.delete();
						shipmentInstance.removeFromShipmentItems(itemInstance).save(flush:true);
					}
				}
				

				
			}.to "enterContainerDetails"
			on("addItem") {
				def shipmentInstance = Shipment.get(params?.shipment?.id);
				def containerInstance = Container.get(params?.container?.id);
				
				if (containerInstance) {
					Product productInstance = Product.get(params?.product?.id);
					if (productInstance) {						
						def itemInstance = new ShipmentItem(
							product: productInstance,
							quantity: params.quantity,
							recipient: Person.get(params.recipient.id),
							lotNumber: params.lotNumber,
							shipment: containerInstance.shipment,
							container: containerInstance);
						
						flow.itemInstance = itemInstance
						
						if(itemInstance.hasErrors() || !itemInstance.validate()) {
							println "Errors: " + itemInstance.errors.each { println it }
							flash.message = "Sorry, there was an error validating item to be added";
							return error()
						}
						
						if (!containerInstance.shipment.addToShipmentItems(itemInstance).save(flush:true)) { 
							log.error("Sorry, unable to add new item to shipment.  Please try again.");
							flash.message = "Unable to add new item to shipment";
						}
					}
					else { 
						flash.message = "Sorry, unable to find the given product.  Please try again.";
					}
				}
				
			}.to "enterContainerDetails"
		
		
			on ("submit").to "reviewShipment"
			on ("enterShipmentDetails").to "enterShipmentDetails"
			on ("enterTravelerDetails").to "enterTravelerDetails"
			on ("enterContainerDetails").to "enterContainerDetails"
			on ("reviewShipment").to "reviewShipment"
			on ("sendShipment").to "sendShipment"
			on ("done").to "redirectToShowDetails"

			
			//on(Exception).to "handleError"
		}
		/*
		addShipmentItems  {
			on("back").to "enterContainerDetails"
			on("addItem") {
				def containerInstance = Container.get(params.container?.id)
				if (containerInstance) { 
					Product productInstance = Product.get(params.product?.id);
					if (productInstance) { 
						
						def itemInstance = new ShipmentItem(
							product: productInstance, 
							quantity: params.quantity, 
							recipient: Person.get(params.recipient.id),
							lotNumber: params.lotNumber, 
							container: containerInstance);

						if(itemInstance.hasErrors() || !itemInstance.validate()) {
							flow.itemInstance = itemInstance
							return error()
						}
						
						containerInstance.shipment.addToShipmentItems(itemInstance).save(flush:true);
						//containerInstance.addToShipmentItems(itemInstance).save(flush:true);
					}					
				}
				flow.itemInstance=null;				
			}.to "addShipmentItems"
			on("submit").to "reviewShipment"
			on(Exception).to "handleError"
			on("clear").to "clear"
		}*/
		reviewShipment  {
			
			on("cancel").to "cancel"
			on("back").to "enterContainerDetails"
			on("reviewLetter").to "reviewLetter"
			on("clear").to "clear"
			on("submit") {
				
				def shipmentInstance = Shipment.get(params.id);
				if (shipmentInstance) { 
					// Add a new event to the shipment to mark it as Requested
					EventType eventType = EventType.findByName("Packed")
					if (eventType) {
						boolean exists = Boolean.FALSE
						// If 'requested' event type already exists, return
						shipmentInstance?.events.each {
							if (it.eventType == eventType) 
								exists = Boolean.TRUE;
						}
						// Avoid duplicate events
						if (!exists) {
							def event = new Event(
								eventDate: new Date(),
								eventType: eventType,
								eventLocation: Location.get(session.warehouse.id)).save(flush:true);
							shipmentInstance.addToEvents(event).save(flush:true);
						}
					}				
				} 
				else {
					return error();
				}				

			}.to "sendShipment"
		
			on("enterShipmentDetails").to "enterShipmentDetails"
			on("enterTravelerDetails").to "enterTravelerDetails"
			on("enterContainerDetails").to "enterContainerDetails"
			on("reviewShipment").to "reviewShipment"
			on("sendShipment").to "sendShipment"
			on("done").to "redirectToShowDetails"
		}
		reviewLetter {
			on("back").to "reviewShipment"
			on("refresh").to "reviewLetter"
		}
		sendShipment  {
			on("cancel").to "cancel"
			on("back").to "reviewShipment"
			on("finish").to "finish"	
			on("enterShipmentDetails").to "enterShipmentDetails"
			on("enterTravelerDetails").to "enterTravelerDetails"
			on("enterContainerDetails").to "enterContainerDetails"
			on("reviewShipment").to "reviewShipment"
			on("sendShipment").to "sendShipment"
			on("done").to "redirectToShowDetails"

			
			
		}
		finish { 
			action {
				def shipmentInstance = Shipment.get(params.id);
				//shipmentInstance.properties = params
				if (!shipmentInstance.hasErrors() && shipmentInstance.save(flush: true)) {
					EventType eventType = EventType.findByName("Shipped")
					if (!eventType) {
						log.error("Event type 'Shipped' does not exist")
					} 
					else { 
						boolean exists = Boolean.FALSE;
						// If 'requested' event type already exists, return
						log.info("exists " + exists)
						shipmentInstance?.events.each {
							if (it.eventType == eventType) 
								exists = Boolean.TRUE;
						}
						// Avoid duplicate events
						if (!exists) {
							log.info ("Event does not exist")
							def event = new Event(eventDate: new Date(), eventType: eventType, 
								eventLocation: Location.get(session.warehouse.id)).save(flush:true);
							shipmentInstance.addToEvents(event).save(flush:true);
						}
					}
					if (params.comment) {
						def comment = new Comment(comment: params.comment, sender: session?.user)
						shipmentInstance.addToComments(comment).save(flush:true);
					}

					// Send an email message to the shipment owner
					if (session?.user) {
						def subject = "Your suitcase shipment " + shipmentInstance?.name + " has been successfully created";
						def message = "You have successfully created a suitcase shipment."
						mailService.sendMail(subject, message, session?.user?.email);
					}
					
					// Send an email message to the shipment traveler
					if (shipmentInstance?.carrier) { 					
						// Send an email message to the shipment owner
						def subject = "A suitcase shipment " + shipmentInstance?.name + " is ready for pickup";
						def message = "The suitcase you will be traveling with is ready for pickup."
						mailService.sendMail(subject, message, shipmentInstance?.carrier?.email);
					}
					
					// Send an email message to the shipment recipient
					if (shipmentInstance?.recipient) { 
						def subject = "A suitcase shipment " + shipmentInstance?.name + " is ready to ship to you";
						def message = "A suitcase that is being sent to you is ready to be shipped."
						mailService.sendMail(subject, message, shipmentInstance?.recipient?.email);
					}
					
					// Send emails to each person receiving shipment 
					shipmentInstance?.allShipmentItems?.each {
						def subject = "An item is being shipped to you as part of shipment " + shipmentInstance?.name;
						def message = "You should expect to receive " + it.quantity + " units of " + it?.product?.name +
							 " within a few days of " + shipmentInstance?.expectedDeliveryDate;

						if (it?.recipient?.email) {
							mailService.sendMail(subject, message, it?.recipient?.email);
						}							
					}
					//flash.message = "${message(code: 'default.updated.message', args: [message(code: 'shipment.label', default: 'Shipment'), shipmentInstance.id])}"
					//redirect(controller: "shipment", action: "showDetails", id: shipmentInstance.id)
				}
			}
			on("error").to "reviewShipment"
			on(Exception).to "reviewShipment"
			on("success").to "complete"
		}
		complete { 
			// renders complete.gsp 		
		}
		handleError()
		cancel {
			redirect(controller:"dashboard", action:"index")
		}
		clear {
			action {
				//flow.clear();
			}
			on("success").to "enterShipmentDetails"
			on(Exception).to "handleError"
		}
		redirectToShowDetails { 	
			//redirect(url: '/shipment/showDetails/${flowScope?.shipmentIntance?.id}')
			redirect(controller:"shipment", action:"showDetails", id: flow?.shipmentInstance?.id)			
		}
	}

	
	
	
	
	
	
	
	
	
	
	/*
	def shoppingCartFlow = {
		getBooks {
			action {
				[ bookList:Product.list() ]
			}
			on("success").to "showCatalogue"
			on(Exception).to "handleError"
		}
		showCatalogue {
			on("chooseBook") {
				if(!params.id)return error()
				def items = flow.cartItems
				if(!items) items = [] as HashSet
				items << Product.get(params.id)
				flow.cartItems = items
			}.to "showCart"
		}
		showCart {
			on("checkout").to "enterPersonalDetails"
			on("continueShopping").to "showCatalogue"
		}
		enterPersonalDetails {
			on("submit") {
				def p = new Person(params)
				flow.person = p
				def e = yes()
				if(p.hasErrors() || !p.validate())return error()
			}.to "enterShipping"
			on("return").to "showCart"
			on(Exception).to "handleError"
		}
		enterShipping  {
			on("back").to "enterPersonalDetails"
			on("submit") {
				def a = new Address(params)
				flow.address = a
				if(a.hasErrors() || !a.validate()) return error()
			}.to "enterPayment"
		}
		enterPayment  {
			on("back").to "enterShipping"
			on("submit") {
				def pd = new Container(params)
				flow.paymentDetails = pd
				if(pd.hasErrors() || !pd.validate()) return error()
			}.to "confirmPurchase"
		}
		confirmPurchase  {
			on("back").to "enterPayment"
			on("confirm").to "processPurchaseOrder"
		}
		processPurchaseOrder  {
			action {
				def a =  flow.address
				def p = flow.person
				def pd = flow.paymentDetails
				def cartItems = flow.cartItems
				//def o = new Order(person:p, shippingAddress:a, paymentDetails:pd)
				//o.invoiceNumber = new Random().nextInt(9999999)
				//cartItems.each { o.addToItems(it) }
				//[order:o]
			}
			on("error").to "confirmPurchase"
			on(Exception).to "confirmPurchase"
			on("success").to "displayInvoice"
		}
		displayInvoice()
		handleError()
	}
	*/

}

