package org.pih.warehouse.order

import java.util.Date;

import org.pih.warehouse.core.Comment;
import org.pih.warehouse.core.Document;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.inventory.Warehouse;
import org.pih.warehouse.shipping.DocumentCommand;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.shipping.ShipmentItem;

class OrderController {
	def orderService
    static allowedMethods = [save: "POST", update: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
		
		System.out.println("Params: " + params);
		
		def suppliers = orderService.getSuppliers().sort();

		def destination = Warehouse.get(session.warehouse.id)
		def origin = params.origin ? Location.get(params.origin) : null
		def status = params.status ? Enum.valueOf(OrderStatus.class, params.status) : null
		def statusStartDate = params.statusStartDate ? Date.parse("MM/dd/yyyy", params.statusStartDate) : null
		def statusEndDate = params.statusEndDate ? Date.parse("MM/dd/yyyy", params.statusEndDate) : null
				
		def orders = orderService.getOrdersPlacedByWarehouse(destination, origin, status, statusStartDate, statusEndDate)
		
		// sort by order date
		orders = orders.sort( { a, b ->
			return b.dateOrdered <=> a.dateOrdered
		} )

		[ orders:orders, origin:origin?.id, destination:destination?.id,
		  status:status, statusStartDate:statusStartDate, statusEndDate:statusEndDate,
		  suppliers : suppliers
		]
    }

	def listOrderItems = { 
		def orderItems = OrderItem.getAll().findAll { !it.isCompletelyFulfilled() } ;		
		return [orderItems : orderItems]		
	}
	
    def create = {
		redirect(controller: 'purchaseOrderWorkflow', action: 'index');
    }

    def save = {
        def orderInstance = new Order(params)
        if (orderInstance.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'order.label', default: 'Order'), orderInstance.id])}"
            redirect(action: "list", id: orderInstance.id)
        }
        else {
            render(view: "create", model: [orderInstance: orderInstance])
        }
    }
	
    def show = {
        def orderInstance = Order.get(params.id)
        if (!orderInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        }
        else {
            [orderInstance: orderInstance]
        }
    }

    def edit = {
        def orderInstance = Order.get(params.id)
        if (!orderInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [orderInstance: orderInstance]
        }
    }

	def placeOrder = { 
		def orderInstance = Order.get(params.id)
		if (orderInstance) {
			
			if (orderInstance?.orderItems?.size() > 0) { 
				orderInstance.status = OrderStatus.PLACED;
				if (!orderInstance.hasErrors() && orderInstance.save(flush: true)) {
					flash.message = "${warehouse.message(code: 'order.orderHasBeenPlacedWithVendor.message', args: [orderInstance?.description, orderInstance?.origin?.name])}"
					redirect(action: "show", id: orderInstance.id)
				}
				else {
					flash.message = "${warehouse.message(code: 'order.errorPlacingOrder.message')}"
					render(view: "show", model: [orderInstance: orderInstance])
				}
			}
			else { 
				flash.message = "${warehouse.message(code: 'order.order.mustContainAtLeastOneItem.message.message')}"
				redirect(action: "show", id: orderInstance.id)
			}
		}
		else { 
			redirect("show", id: orderInstance?.id)
			
		}
				
	}

	
    def update = {
        def orderInstance = Order.get(params.id)
        if (orderInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (orderInstance.version > version) {
                    
                    orderInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'order.label', default: 'Order')] as Object[], "Another user has updated this Order while you were editing")
                    render(view: "edit", model: [orderInstance: orderInstance])
                    return
                }
            }
            orderInstance.properties = params
            if (!orderInstance.hasErrors() && orderInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'order.label', default: 'Order'), orderInstance.id])}"
                redirect(action: "list", id: orderInstance.id)
            }
            else {
                render(view: "edit", model: [orderInstance: orderInstance])
            }
        }
        else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        }
    }


	
	
    def delete = {
        def orderInstance = Order.get(params.id)
        if (orderInstance) {
            try {
                orderInstance.delete(flush: true)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
                redirect(action: "list", id: params.id)
            }
        }
        else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        }
    }
	
	

	
	def addComment = { 
        def orderInstance = Order.get(params?.id)
        if (!orderInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [orderInstance: orderInstance, commentInstance: new Comment()]
        }
	}
	
	def editComment = {
		def orderInstance = Order.get(params?.order?.id)
		if (!orderInstance) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
			redirect(action: "list")
		}
		else {
			def commentInstance = Comment.get(params?.id)
			if (!commentInstance) {
				flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'comment.label', default: 'Comment'), commentInstance.id])}"
				redirect(action: "show", id: orderInstance?.id)
			}
			render(view: "addComment", model: [orderInstance: orderInstance, commentInstance: commentInstance])
		}
	}
	
	def deleteComment = { 
		def orderInstance = Order.get(params.order.id)
		if (!orderInstance) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.order.id])}"
			redirect(action: "list")
		}
		else {
			def commentInstance = Comment.get(params?.id)
			if (!commentInstance) {
				flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'comment.label', default: 'Comment'), params.id])}"
				redirect(action: "show", id: orderInstance?.id)
			}
			else { 
				orderInstance.removeFromComments(commentInstance);
				if (!orderInstance.hasErrors() && orderInstance.save(flush: true)) {
					flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'order.label', default: 'Order'), orderInstance.id])}"
					redirect(action: "show", id: orderInstance.id)
				}
				else {
					render(view: "show", model: [orderInstance: orderInstance])
				}
			}
		}		
	}
	
	def saveComment = { 
		log.info(params)
		
		def orderInstance = Order.get(params?.order?.id)
		if (orderInstance) { 
			def commentInstance = Comment.get(params?.id)
			if (commentInstance) { 
				commentInstance.properties = params
				if (!commentInstance.hasErrors() && commentInstance.save(flush: true)) {
					flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'comment.label', default: 'Comment'), commentInstance.id])}"
					redirect(action: "show", id: orderInstance.id)
				}
				else {
					render(view: "addComment", model: [orderInstance: orderInstance, commentInstance: commentInstance])
				}
			}
			else { 
				commentInstance = new Comment(params)
				orderInstance.addToComments(commentInstance);
				if (!orderInstance.hasErrors() && orderInstance.save(flush: true)) {
					flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'order.label', default: 'Order'), orderInstance.id])}"
					redirect(action: "show", id: orderInstance.id)
				}
				else {
					render(view: "addComment", model: [orderInstance: orderInstance, commentInstance:commentInstance])
				}
			}
		}	
		else { 
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
			redirect(action: "list")
		}
		
	}

	def addDocument = {
		def orderInstance = Order.get(params.id)
		if (!orderInstance) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
			redirect(action: "list")
		}
		else {
			return [orderInstance: orderInstance]
		}
	}
	
	def editDocument = {
		def orderInstance = Order.get(params?.order?.id)
		if (!orderInstance) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
			redirect(action: "list")
		}
		else {
			def documentInstance = Document.get(params?.id)
			if (!documentInstance) {
				flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'document.label', default: 'Document'), documentInstance.id])}"
				redirect(action: "show", id: orderInstance?.id)
			}
			render(view: "addDocument", model: [orderInstance: orderInstance, documentInstance: documentInstance])
		}
	}
	
	def deleteDocument = {
		def orderInstance = Order.get(params.order.id)
		if (!orderInstance) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.order.id])}"
			redirect(action: "list")
		}
		else {
			def documentInstance = Document.get(params?.id)
			if (!documentInstance) {
				flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'comment.label', default: 'Comment'), params.id])}"
				redirect(action: "show", id: orderInstance?.id)
			}
			else {
				orderInstance.removeFromDocuments(documentInstance);
				if (!orderInstance.hasErrors() && orderInstance.save(flush: true)) {
					flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'order.label', default: 'Order'), orderInstance.id])}"
					redirect(action: "show", id: orderInstance.id)
				}
				else {
					render(view: "show", model: [orderInstance: orderInstance])
				}
			}
		}
	}
	
	def receive = {		
		def orderCommand = orderService.getOrder(params.id as int, session.user.id as int)
		if (!orderCommand.order) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
			redirect(action: "list")
		}
		else { 
			return [orderCommand: orderCommand]
		}
	}
	
	def saveOrderShipment = { OrderCommand command ->	
		bindData(command, params)		
		def orderInstance = Order.get(params?.order?.id);
		command.order = orderInstance;
		
		orderService.saveOrderShipment(command)
		
		// If the shipment was saved, let's redirect back to the order received page
		if (!command?.shipment?.hasErrors() && command?.shipment?.id) {
			redirect(controller: "order", action: "receive", id: params?.order?.id)
		}
		
		// Otherwise, we want to display the errors, so we need to render the page.
		render(view: "receive", model: [orderCommand: command])
	}

	def addOrderShipment = {  
		def orderCommand = orderService.getOrder(params.id as int, session.user.id as int)
		int index = Integer.valueOf(params?.index)
		def orderItemToCopy = orderCommand?.orderItems[index]
		if (orderItemToCopy) { 
			def orderItemToAdd = new OrderItemCommand();
			orderItemToAdd.setPrimary(false)
			orderItemToAdd.setType(orderItemToCopy.type)
			orderItemToAdd.setDescription(orderItemToCopy.description)
			orderItemToAdd.setLotNumber(orderItemToCopy.lotNumber);
			orderItemToAdd.setOrderItem(orderItemToCopy.orderItem)
			orderItemToAdd.setProductReceived(orderItemToCopy.productReceived)
			orderItemToAdd.setQuantityOrdered(orderItemToCopy.quantityOrdered)
			
			orderCommand?.orderItems?.add(index+1, orderItemToAdd);
		}
		render(view: "receive", model: [orderCommand: orderCommand])
		//redirect(action: "receive")
	} 
	
	def removeOrderShipment = { 
		log.info("Remove order shipment " + params)
		def orderCommand = session.orderCommand
		int index = Integer.valueOf(params?.index)
		orderCommand.orderItems.remove(index)

		//render(view: "receive", model: [orderCommand: orderCommand])
		redirect(action: "receive")
	}
		

	def fulfill = {
		def orderInstance = Order.get(params.id)
		if (!orderInstance) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
			redirect(action: "list")
		}
		else {
			return [orderInstance: orderInstance]
		}
	}

	def addOrderItemToShipment = { 
		
		def orderInstance = Order.get(params?.id)
		def orderItem = OrderItem.get(params?.orderItem?.id)
		def shipmentInstance = Shipment.get(params?.shipment?.id)
		
		if (orderItem) { 
			def shipmentItem = new ShipmentItem(orderItem.properties)
			shipmentInstance.addToShipmentItems(shipmentItem);
			if (!shipmentInstance.hasErrors() && shipmentInstance?.save(flush:true)) { 
				
				def orderShipment = OrderShipment.link(orderItem, shipmentItem);
				/*
				if (!orderShipment.hasErrors() && orderShipment.save(flush:true)) { 
					flash.message = "success"
				}
				else { 
					flash.message = "order shipment error(s)"
					render(view: "fulfill", model: [orderShipment: orderShipment, orderItemInstance: orderItem, shipmentInstance: shipmentInstance])
					return;
				}*/
			}
			else { 
				flash.message = "${warehouse.message(code: 'order.shipmentItemErrors.message')}"
				render(view: "fulfill", model: [orderItemInstance: orderItem, shipmentInstance: shipmentInstance])
				return;
			}
		}
		
		redirect(action: "fulfill", id: orderInstance?.id)
		
	}

	
	
		
}
