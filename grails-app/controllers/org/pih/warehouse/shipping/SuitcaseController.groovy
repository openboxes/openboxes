package org.pih.warehouse.shipping

import grails.converters.JSON;

import org.pih.warehouse.core.DialogForm;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.Event;
import org.pih.warehouse.core.EventType;
import org.pih.warehouse.core.Comment;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.core.User;
import org.pih.warehouse.product.Product;

class SuitcaseController {
	
	def mailService;
	
	
	def index = {
		redirect(action:'createSuitcase')
		//redirect(action: '')
	}
	
	
	def createSuitcaseFlow = {
		step0 { 
			action {
				flow.shipmentInstance = new Shipment(params);
				ShipmentType shipmentType = ShipmentType.findByName("Suitcase");
				if (!shipmentType)
					throw new Exception("Unable to find 'Suitcase' shipment type")
				flow.shipmentInstance.shipmentType = shipmentType;
				return success();
			}
			on("success").to "step1"
			on("error").to "step1"
		}
		step1 {			
			on("cancel").to "cancel"
			on("back").to "step0"
			on("next") { 
				log.info("params" + params)
				def shipmentInstance = new Shipment(params);

				// This is necessary in order for validation to work
				flow.shipmentInstance = shipmentInstance
				
				// If the shipment does not have any containers, create the default suitcase 
				if (!shipmentInstance?.containers) { 
					def number = (shipmentInstance?.containers) ? shipmentInstance?.containers?.size() : 0;
					def containerType = ContainerType.findByName("Suitcase");
					if (containerType) {
						def suitcase = new Container(name: number+1, containerType: containerType);
						shipmentInstance.addToContainers(suitcase);				
					}
				}
				
				EventType eventType = EventType.findByName("Requested")				
				if (eventType) { 
					def event = new Event(
						eventDate: new Date(), 
						eventType: eventType,
						eventLocation: Location.get(session.warehouse.id)).save(flush:true);
					
					shipmentInstance.addToEvents(event).save(flush:true);
				}

				if (!shipmentInstance.validate(['name', 'origin', 'destination'])) { 
					return error();
				}								
			
				flow.shipmentInstance = shipmentInstance;
				
			}.to "step2"
		}
		step2 { 
			on("back").to "step1"
			on("cancel").to "cancel"
			on("next") { 
				//log.info( "shipment " + flow.shipmentInstance.name)
				def shipmentInstance = Shipment.get(params.id)
				if (!shipmentInstance)
					shipmentInstance = flow.shipmentInstance;
					
				shipmentInstance.properties = params

				if (shipmentInstance.hasErrors() || !shipmentInstance.save(flush:true)) {
					return error();
				}				
						
			}.to "step3"

		}
		step3 { 
			on("back").to "step2"
			on("save") { 
				
				log.info "saving shipment " + params;
				def shipmentInstance = Shipment.get(params.id) 				
				shipmentInstance.properties = params
				
				if (!shipmentInstance.validate()) { 
					return error();
				}
				if (!shipmentInstance.hasErrors() && shipmentInstance.save(flush: true)) {
					flash.message = "${message(code: 'default.updated.message', args: [message(code: 'shipment.label', default: 'Shipment'), params.id])}"
					log.info "saved successfully"
				}
				else { 
					flash.message = "error saving shipment"
					log.info "error saving shipment"
				}
				
				[shipmentInstance: shipmentInstance]				
			}.to "step3"			
			on("addPerson") { 
				log.info("addPerson params" + params);
				flash.message = "New person ${params} added"
				return error();

			}.to "step3"				
			on("addNewItem") { 
				log.info("params " + params);
				def productInstance = new Product(); //Product.findByName("");
				Shipment shipmentInstance = Shipment.get(params.id);
				Container containerInstance = Container.get(params.container.id);
				
				def shipmentItem = new ShipmentItem(product: productInstance, quantity: 1, container: containerInstance);
				
				//containerInstance.addToShipmentItems(shipmentItem).save(flush:true);
				containerInstance.shipment.addToShipmentItems(shipmentItem).save(flush:true);
				[shipmentInstance: shipmentInstance]				
			}.to "step3"				
			on("addItem") { 
				log.info("params " + params);
				def productInstance = new Product(); //Product.findByName("");
				Shipment shipmentInstance = Shipment.get(params.id);
				Container containerInstance = Container.get(params.container.id);
				//containerInstance.addToShipmentItems(shipmentItem);					
				
				def shipmentItem = new ShipmentItem(product: productInstance, quantity: 1, container: containerInstance);
				
				
				containerInstance.shipment.addToShipmentItems(shipmentItem)
				/*if (shipmentInstance.hasErrors() || !shipmentInstance.save(flush:true))
					return error();
				*/
				[shipmentInstance: shipmentInstance]				
			}.to "step3"				
			on("deleteItem") { 
				Shipment shipmentInstance = Shipment.get(params.id);
				Container containerInstance = Container.get(params.container.id);
				def itemInstance = ShipmentItem.get(params.item.id);
				if (itemInstance) {
					itemInstance.delete()
					shipmentInstance.removeFromShipmentItems(itemInstance);	
					//containerInstance.removeFromShipmentItems(itemInstance);				
				}
					
				if (shipmentInstance.hasErrors() || !shipmentInstance.save(flush:true))
					return error();

			}.to "step3"
			on("deleteBox") { 
				def containerInstance = Container.get(params.container.id);
				log.info "container: " + containerInstance
				if (containerInstance) 
					containerInstance.delete();				
				
				//Container containerInstance = Container.get(params.container.id);
				//if (containerInstance?.shipmentItems?.size() > 0) { 
				//	throw new Exception("Cannot delete a container with items");
				//}
				
				Shipment shipmentInstance = Shipment.get(params.id);
				shipmentInstance.removeFromContainers(containerInstance);
				if (shipmentInstance.hasErrors() || !shipmentInstance.save(flush:true))
					return error();
			}.to "step3"
			on("addBox") { 
				def shipmentInstance = flow.shipmentInstance;
				def containerInstance = Container.get(params?.container?.id);				
				def containerType = ContainerType.findByName("Box");
				def number = (shipmentInstance?.containers) ? shipmentInstance?.containers?.size() : 0;
				def box = new Container(name: number, containerType: containerType);
				box.shipment = shipmentInstance;
				containerInstance?.addToContainers(box);				
				if (containerInstance.hasErrors() || !containerInstance.save(flush:true))
					return error();

								//shipmentInstance.addToContainers(box);								
				//if (shipmentInstance.hasErrors() || !shipmentInstance.save(flush:true))
				//	return error();
			}.to "step3"			
			on("cancel").to "cancel"
			on("next") { 
				log.info "fuck you !"
				
			}.to "step4"
		} 
		step4 { 
			on("cancel").to "cancel"
			on("back").to "step3"
			on("next") { 	
				def shipmentInstance = Shipment.get(params.id);
				if (shipmentInstance) { 
					EventType eventType = EventType.findByName("Packed")
					if (eventType) {
						def event = new Event(
							eventDate: new Date(),
							eventType: eventType,
							eventLocation: Location.get(session.warehouse.id)).save(flush:true);					
						shipmentInstance.addToEvents(event).save(flush:true);
					}
				} else {
					log.info("Could not find shipment")
					return error();
				}
									
			}.to "step5"
			on ("reviewLetter") { 				
				// Pass through to 'reviewLetter' action
			}.to "reviewLetter"		
		} 
		reviewLetter { 
			render(view: "suitcaseLetter")			
			on("back").to "step4"
			on("next").to "step4"
			on("refresh").to "reviewLetter"
		}		
		step5 {
			on("cancel").to "cancel"
			on("back").to "step4"
			on("finish") { 	

				def shipmentInstance = Shipment.get(params.id);
				
				//shipmentInstance.properties = params
				if (!shipmentInstance.hasErrors() && shipmentInstance.save(flush: true)) {
					
					EventType eventType = EventType.findByName("Shipped")
					if (eventType) {
						def event = new Event(eventDate: new Date(), eventType: eventType,
							eventLocation: Location.get(session.warehouse.id)).save(flush:true);
						shipmentInstance.addToEvents(event).save(flush:true);
					}
							
					if (params.comment) { 				
						def comment = new Comment(comment: params.comment, sender: session?.user)
						shipmentInstance.addToComments(comment).save(flush:true);
					}				

					def confirmSubject = "Suitcase " + shipmentInstance?.name + " has been shipped";
					def confirmMessage = "This message represents the email body"
					if (session?.user) {
						mailService.sendMail(confirmSubject, confirmMessage, session?.user?.email);
					}

					
					shipmentInstance?.containers?.each {
						it?.shipmentItems?.each {
							def subject = "Suitcase " + shipmentInstance?.name + " contains an item for you!";
							def message = "You should expect to receive " + it.quantity + " units of " + it?.product?.name +
								 " within a few days of " + shipmentInstance?.expectedDeliveryDate;
	
							if (it?.recipient?.email) {
								log.info("Sending email to " + it?.recipient?.email)
								mailService.sendMail(subject, message, it?.recipient?.email);
							}
							
						}
					}
	
					//flash.message = "${message(code: 'default.updated.message', args: [message(code: 'shipment.label', default: 'Shipment'), shipmentInstance.id])}"
					//redirect(action: "showDetails", id: shipmentInstance.id, params: ["containerId" : params.containerId])
				}
				
				//mailService.sendHtmlMail(params.subject, params.htmlMsg, "text alternative message", params.to)								
				
				
				
			}.to "finish"		
		}
		finish { 
						
		}

		
		
		cancel {
			redirect(controller:"dashboard", action:"index")
		}
	}

	
	
	/*
	 * 		enterPersonalDetails {
			on("submit") {
				  def p = new Person(params)
				  flow.person = p
				  if(!p.validate())return error()
			}.to "enterShipping"
			on("return").to "showCart"
		 }
 
	 */
	
	
		
	def startWizard = { SuitcaseCommand suitcaseCommand ->
		
		def shipmentType = ShipmentType.findByName("Suitcase");
		suitcaseCommand.shipmentType = shipmentType
		suitcaseCommand.name = "Suitcase shipment";		
		render(view: 'enterName', model: [suitcaseCommand : suitcaseCommand])		
	}
	
	
	def processName = { SuitcaseCommand suitcaseCommand -> 		
		if (request.method == 'POST') {
			if (suitcaseCommand.hasErrors()) {
				render(view: 'enterName', model: [suitcaseCommand : suitcaseCommand])				
			}
			else { 				
				def shipmentInstance = new Shipment()
				shipmentInstance.name = suitcaseCommand.name;
				shipmentInstance.shipmentType = suitcaseCommand.shipmentType;
				//if (shipmentInstance.hasErrorsshipmentInstance.save(flush:true);
			}
		}
		render(view: 'enterName', model: [suitcaseCommand : suitcaseCommand])
	}
		
	
	
	
	
	
	
	
	
	/*
	def startWizard = { SuitcaseCommand suitcaseCommand ->		
		def shipmentType = ShipmentType.findByName("Suitcase");		
		def shipmentInstance = new Shipment(shipmentType : shipmentType)
		shipmentInstance.name = "Suitcase shipment"		
		
		suitcaseCommand.stepNumber = 1
		suitcaseCommand.shipment = shipmentInstance;
		
		render(view: 'enterName', model: [suitcaseCommand : suitcaseCommand])
	}

	
	def processName = { SuitcaseCommand suitcaseCommand ->			
		if (request.method == 'POST') {
			def shipmentInstance = suitcaseCommand.shipment;			
			if (!shipmentInstance.hasErrors() && shipmentInstance.save(flush:true)) {
				render(view: 'enterDetails', model: [suitcaseCommand : suitcaseCommand])		
				return;
			} 
		}
		render(view: 'enterName', model: [suitcaseCommand : suitcaseCommand])
	}
	*/
	
	def processDetails = { SuitcaseCommand suitaseCommand ->
		def shipmentInstance = Shipment.get(params.id);
		if (request.method == 'POST') { 
			if (!shipmentInstance.hasErrors() && shipmentInstance.save(flush:true)) {
				render(view: 'addContents', model: [suitaseCommand : suitaseCommand])		
			} 
			else { 
				render(view: 'enterDetails', model: [suitaseCommand : suitaseCommand])
			}			
		}

		render(view: 'enterDetails', model: [suitaseCommand : suitaseCommand])	
		
	}

	def addContents = { 
		def shipmentInstance = Shipment.get(params.id);
		if (shipmentInstance.hasErrors()) {
			render(view: 'enterDetails', model: [shipmentInstance : shipmentInstance])
		}
		
		shipmentInstance.stepNumber++;
		render(view: 'reviewContents', model: [shipmentInstance : shipmentInstance])	
	}
		
	def reviewContents = { 
		def shipmentInstance = Shipment.get(params.id);
		
		if (shipmentInstance.hasErrors()) {
			render(view: 'reviewContents', model: [shipmentInstance : shipmentInstance])
		}
		
		shipmentInstance.stepNumber++;
		render(view: 'sendShipment', model: [shipmentInstance : shipmentInstance])
	}
	
	def sendShipment = { SuitcaseCommand suitcaseCommand -> 
		def shipment = Shipment.get(params.id);
		
		if (suitcaseCommand.hasErrors()) {
			render(view: 'sendShipment', model: [suitcaseCommand : suitcaseCommand])
		}
		
		suitcaseCommand.stepNumber++;
		redirect(controller: 'suitcase', action: 'startWizard');
	}
	
	
	def something = { SuitcaseCommand suitcaseCommand -> 
				
		if(suitcaseCommand.hasErrors()) {
			// render form with errors 
		} 
		else { 		
			// Enter name and shipment type
			if (suitcaseCommand.stepNumber == 1) { 	
				session["suitcase"].shipment.name = suitcaseCommand.name;
				session["suitcase"].shipment.shipmentType = suitcaseCommand.shipmentType;
				
				suitcaseCommand.stepNumber++;	
			}
			
			// Enter details
			else if (suitcaseCommand.stepNumber == 2) { 
				// Process step 2
				
				// Prepare for adding contents
				suitcaseCommand.suitcase = new Container(containerType:ContainerType.findByName("Suitcase"));
				suitcaseCommand.suitcase.shipmentItems = new ArrayList<ShipmentItem>();
				suitcaseCommand.suitcase.shipmentItems.add(new ShipmentItem(product: new Product(name: "Item 1")));
				suitcaseCommand.suitcase.shipmentItems.add(new ShipmentItem(product: new Product(name: "Item 2")))
				suitcaseCommand.suitcase.shipmentItems.add(new ShipmentItem(product: new Product(name: "Item 3")))
				suitcaseCommand.suitcase.shipmentItems.add(new ShipmentItem(product: new Product(name: "Item 4")))

				suitcaseCommand.stepNumber++;					
			}
			
			// Add contents
			else if (suitcaseCommand.stepNumber == 3) {
				suitcaseCommand.stepNumber++;
				
			}
			
			// Review contents
			else if (suitcaseCommand.stepNumber == 4) {
				suitcaseCommand.stepNumber++;
			}
			
			else if (suitcaseCommand.stepNumber == 5) { 
				
				def suitcase = session[""]	
			}
		}
			
		[suitcaseCommand : suitcaseCommand, eventTypes : EventType.getAll()]
	}

	
	def editContents = { SuitcaseCommand suitcaseCommand ->
	
	}

	
	def saveContents = { SuitcaseCommand suitcaseCommand -> 
		log.info ("saveContents: " + params);
	
		render(viewName: "create", model: [suitcaseCommand: suitcaseCommand]);
	
	}
	
}



class SuitcaseCommand {
	Integer id;
	String name
	User createdBy
	Integer stepNumber;
	Person traveler;
	Location origin;
	Location destination;
	ShipmentType shipmentType;
	Container suitcase;
	Date expectedShippingDate;
	Date expectedDeliveryDate;
	String flightNumber;
	Float totalValue;
	Integer suitcaseCount = 1;
	
	
	Shipment shipment;
	
	
	static constraints = {
		name(blank:false)
		//createdBy(nullable:false)
		//shipmentType(nullable:false)
	}
}

