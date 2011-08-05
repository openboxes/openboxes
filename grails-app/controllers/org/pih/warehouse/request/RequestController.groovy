package org.pih.warehouse.request

import java.util.Date;

import org.pih.warehouse.core.Comment;
import org.pih.warehouse.core.Document;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.inventory.Warehouse;
import org.pih.warehouse.shipping.DocumentCommand;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.shipping.ShipmentItem;

class RequestController {
	
	
	def requestService
    def inventoryService
	
	static allowedMethods = [save: "POST", update: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }
	
	def menu = { 		
		
		log.info (params);
		
		def list = Request.list();
		def menu = list.groupBy { it.status }  
		
		[menu: menu]
	}
	

    def list = {
        //params.max = Math.min(params.max ? params.int('max') : 10, 100)
        //[requestInstanceList: Request.list(params), requestInstanceTotal: Request.count()]
		
		def location = Location.get(session.warehouse.id)
		def incomingRequests = requestService.getIncomingRequests(location)
		def outgoingRequests = requestService.getOutgoingRequests(location)
		
		[incomingRequests : incomingRequests, outgoingRequests : outgoingRequests]
    }	

	def listRequestItems = { 
		//def requestItems = requestService.getRequestItems()
		def requestItems = RequestItem.getAll().findAll { !it.isComplete() } ;
		
		return [requestItems : requestItems]		
	}
	
	
    def create = {
		redirect(controller: 'createRequestWorkflow', action: 'index');
    }

    def save = {
        def requestInstance = new Request(params)
        if (requestInstance.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'request.label', default: 'Request'), requestInstance.id])}"
            redirect(action: "list", id: requestInstance.id)
        }
        else {
            render(view: "create", model: [requestInstance: requestInstance])
        }
    }
	
    def show = {
        def requestInstance = Request.get(params.id)
        if (!requestInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.id])}"
            redirect(action: "list")
        }
        else {
            [requestInstance: requestInstance]
        }
    }

    def edit = {
        def requestInstance = Request.get(params.id)
        if (!requestInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [requestInstance: requestInstance]
        }
    }

	def place = { 
		def requestInstance = Request.get(params.id)
		if (requestInstance) {
			
			if (requestInstance?.requestItems?.size() > 0) { 
				requestInstance.status = RequestStatus.REQUESTED;
				if (!requestInstance.hasErrors() && requestInstance.save(flush: true)) {
					flash.message = "Request '${requestInstance?.description}' has been placed with warehouse ${requestInstance?.origin?.name}"
					redirect(action: "show", id: requestInstance.id)
				}
				else {
					flash.message = "There was an error while placing your request."
					render(view: "show", model: [requestInstance: requestInstance])
				}
			}
			else { 
				flash.message = "An request must contain at least one item before it can be placed with a vendor."
				redirect(action: "show", id: requestInstance.id)
			}
		}
		else { 
			redirect("show", id: requestInstance?.id)
			
		}
	}


	
	
    def update = {
        def requestInstance = Request.get(params.id)
        if (requestInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (requestInstance.version > version) {
                    
                    requestInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'request.label', default: 'Request')] as Object[], "Another user has updated this Request while you were editing")
                    render(view: "edit", model: [requestInstance: requestInstance])
                    return
                }
            }
            requestInstance.properties = params
            if (!requestInstance.hasErrors() && requestInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'request.label', default: 'Request'), requestInstance.id])}"
                redirect(action: "list", id: requestInstance.id)
            }
            else {
                render(view: "edit", model: [requestInstance: requestInstance])
            }
        }
        else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.id])}"
            redirect(action: "list")
        }
    }


	
	
    def delete = {
        def requestInstance = Request.get(params.id)
        if (requestInstance) {
            try {
                requestInstance.delete(flush: true)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.id])}"
                redirect(action: "list", id: params.id)
            }
        }
        else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.id])}"
            redirect(action: "list")
        }
    }
	
	

	
	def addComment = { 
        def requestInstance = Request.get(params?.id)
        if (!requestInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [requestInstance: requestInstance, commentInstance: new Comment()]
        }
	}
	
	def editComment = {
		def requestInstance = Request.get(params?.request?.id)
		if (!requestInstance) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.id])}"
			redirect(action: "list")
		}
		else {
			def commentInstance = Comment.get(params?.id)
			if (!commentInstance) {
				flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'comment.label', default: 'Comment'), commentInstance.id])}"
				redirect(action: "show", id: requestInstance?.id)
			}
			render(view: "addComment", model: [requestInstance: requestInstance, commentInstance: commentInstance])
		}
	}
	
	def deleteComment = { 
		def requestInstance = Request.get(params.request.id)
		if (!requestInstance) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.request.id])}"
			redirect(action: "list")
		}
		else {
			def commentInstance = Comment.get(params?.id)
			if (!commentInstance) {
				flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'comment.label', default: 'Comment'), params.id])}"
				redirect(action: "show", id: requestInstance?.id)
			}
			else { 
				requestInstance.removeFromComments(commentInstance);
				if (!requestInstance.hasErrors() && requestInstance.save(flush: true)) {
					flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'request.label', default: 'Request'), requestInstance.id])}"
					redirect(action: "show", id: requestInstance.id)
				}
				else {
					render(view: "show", model: [requestInstance: requestInstance])
				}
			}
		}		
	}
	
	def saveComment = { 
		log.info(params)
		
		def requestInstance = Request.get(params?.request?.id)
		if (requestInstance) { 
			def commentInstance = Comment.get(params?.id)
			if (commentInstance) { 
				commentInstance.properties = params
				if (!commentInstance.hasErrors() && commentInstance.save(flush: true)) {
					flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'comment.label', default: 'Comment'), commentInstance.id])}"
					redirect(action: "show", id: requestInstance.id)
				}
				else {
					render(view: "addComment", model: [requestInstance: requestInstance, commentInstance: commentInstance])
				}
			}
			else { 
				commentInstance = new Comment(params)
				requestInstance.addToComments(commentInstance);
				if (!requestInstance.hasErrors() && requestInstance.save(flush: true)) {
					flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'request.label', default: 'Request'), requestInstance.id])}"
					redirect(action: "show", id: requestInstance.id)
				}
				else {
					render(view: "addComment", model: [requestInstance: requestInstance, commentInstance:commentInstance])
				}
			}
		}	
		else { 
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.id])}"
			redirect(action: "list")
		}
		
	}

	def addDocument = {
		def requestInstance = Request.get(params.id)
		if (!requestInstance) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.id])}"
			redirect(action: "list")
		}
		else {
			return [requestInstance: requestInstance]
		}
	}
	
	def editDocument = {
		def requestInstance = Request.get(params?.request?.id)
		if (!requestInstance) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.id])}"
			redirect(action: "list")
		}
		else {
			def documentInstance = Document.get(params?.id)
			if (!documentInstance) {
				flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'document.label', default: 'Document'), documentInstance.id])}"
				redirect(action: "show", id: requestInstance?.id)
			}
			render(view: "addDocument", model: [requestInstance: requestInstance, documentInstance: documentInstance])
		}
	}
	
	def deleteDocument = {
		def requestInstance = Request.get(params.request.id)
		if (!requestInstance) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.request.id])}"
			redirect(action: "list")
		}
		else {
			def documentInstance = Document.get(params?.id)
			if (!documentInstance) {
				flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'comment.label', default: 'Comment'), params.id])}"
				redirect(action: "show", id: requestInstance?.id)
			}
			else {
				requestInstance.removeFromDocuments(documentInstance);
				if (!requestInstance.hasErrors() && requestInstance.save(flush: true)) {
					flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'request.label', default: 'Request'), requestInstance.id])}"
					redirect(action: "show", id: requestInstance.id)
				}
				else {
					render(view: "show", model: [requestInstance: requestInstance])
				}
			}
		}
	}
	
	def receive = {		
		def requestCommand = requestService.getRequest(params.id as int, session.user.id as int)
		if (!requestCommand.request) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.id])}"
			redirect(action: "list")
		}
		else { 
			return [requestCommand: requestCommand]
		}
	}
	
	def saveRequestShipment = { RequestCommand command ->	
		bindData(command, params)		
		def requestInstance = Request.get(params?.request?.id);
		command.request = requestInstance;
		
		requestService.saveRequestShipment(command)
		
		// If the shipment was saved, let's redirect back to the request received page
		if (!command?.shipment?.hasErrors() && command?.shipment?.id) {
			redirect(controller: "request", action: "receive", id: params?.request?.id)
		}
		
		// Otherwise, we want to display the errors, so we need to render the page.
		render(view: "receive", model: [requestCommand: command])
	}

	def addRequestShipment = {  
		def requestCommand = requestService.getRequest(params.id as int, session.user.id as int)
		int index = Integer.valueOf(params?.index)
		def requestItemToCopy = requestCommand?.requestItems[index]
		if (requestItemToCopy) { 
			def requestItemToAdd = new RequestItemCommand();
			requestItemToAdd.setPrimary(false)
			requestItemToAdd.setType(requestItemToCopy.type)
			requestItemToAdd.setDescription(requestItemToCopy.description)
			requestItemToAdd.setLotNumber(requestItemToCopy.lotNumber);
			requestItemToAdd.setRequestItem(requestItemToCopy.requestItem)
			requestItemToAdd.setProductReceived(requestItemToCopy.productReceived)
			requestItemToAdd.setQuantityRequested(requestItemToCopy.quantityRequested)
			
			requestCommand?.requestItems?.add(index+1, requestItemToAdd);
		}
		render(view: "receive", model: [requestCommand: requestCommand])
		//redirect(action: "receive")
	} 
	
	def removeRequestShipment = { 
		log.info("Remove request shipment " + params)
		def requestCommand = session.requestCommand
		int index = Integer.valueOf(params?.index)
		requestCommand.requestItems.remove(index)

		//render(view: "receive", model: [requestCommand: requestCommand])
		redirect(action: "receive")
	}
	
	def showPicklist = { 
		def requestInstance = Request.get(params.id)
		if (!requestInstance) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.id])}"
			redirect(action: "list")
		}
		else {
			return [requestInstance: requestInstance]
		}
	}
		

	def fulfill = {
		def requestInstance = Request.get(params.id)
		if (!requestInstance) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.id])}"
			redirect(action: "list")
		}
		else {
			return [requestInstance: requestInstance]
		}
	}
	
	
	def fulfillItem = {
		log.info "fulfillItem " + params
		
		def inventoryItems = [:] 
		def requestItem = RequestItem.get(params.id)
		if (!requestItem) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.id])}"
		}
		else {
			def warehouse = Warehouse.get(session.warehouse.id);
			if (warehouse.inventory) { 
				inventoryItems =
					inventoryService.getQuantityByInventoryAndProduct(warehouse.inventory, requestItem.product);				
			}
			else { 
				throw new RuntimeException("Warehouse does not have an associated inventory")
			}
		}
		return [requestItem: requestItem, inventoryItems: inventoryItems]
	}
	


		
	def addRequestItemToShipment = { 
		
		def requestInstance = Request.get(params?.id)
		def requestItem = RequestItem.get(params?.requestItem?.id)
		def shipmentInstance = Shipment.get(params?.shipment?.id)
		
		if (requestItem) { 
			def shipmentItem = new ShipmentItem(requestItem.properties)
			shipmentInstance.addToShipmentItems(shipmentItem);
			if (!shipmentInstance.hasErrors() && shipmentInstance?.save(flush:true)) { 
				
				def requestShipment = RequestShipment.link(requestItem, shipmentItem);
				/*
				if (!requestShipment.hasErrors() && requestShipment.save(flush:true)) { 
					flash.message = "success"
				}
				else { 
					flash.message = "request shipment error(s)"
					render(view: "fulfill", model: [requestShipment: requestShipment, requestItemInstance: requestItem, shipmentInstance: shipmentInstance])
					return;
				}*/
			}
			else { 
				flash.message = "shipment item error(s)"
				render(view: "fulfill", model: [requestItemInstance: requestItem, shipmentInstance: shipmentInstance])
				return;
			}
		}
		
		redirect(action: "fulfill", id: requestInstance?.id)
		
	}

	
	def fulfillPost = {
		def requestInstance = Request.get(params.id)
		if (requestInstance) {
			
			if (requestInstance?.requestItems?.size() > 0) {
				requestInstance.status = RequestStatus.FULFILLED;
				if (!requestInstance.hasErrors() && requestInstance.save(flush: true)) {
					flash.message = "Request '${requestInstance?.description}' has been placed with warehouse ${requestInstance?.origin?.name}"
					redirect(action: "show", id: requestInstance.id)
				}
				else {
					flash.message = "There was an error while placing your request."
					render(view: "show", model: [requestInstance: requestInstance])
				}
			}
			else {
				flash.message = "An request must contain at least one item before it can be placed with a vendor."
				redirect(action: "show", id: requestInstance.id)
			}
		}
		else {
			redirect("show", id: requestInstance?.id)
			
		}
				
	}

	
	
		
}
