package org.pih.warehouse.order

import org.pih.warehouse.core.Comment;
import org.pih.warehouse.core.Document;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.shipping.DocumentCommand;

class OrderController {
	def orderService
    static allowedMethods = [save: "POST", update: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        //params.max = Math.min(params.max ? params.int('max') : 10, 100)
        //[orderInstanceList: Order.list(params), orderInstanceTotal: Order.count()]
		
		def location = Location.get(session.warehouse.id)
		def incomingOrders = orderService.getIncomingOrders(location)
		def outgoingOrders = orderService.getOutgoingOrders(location)
		
		[incomingOrders : incomingOrders, outgoingOrders : outgoingOrders]

		
    }

    def create = {
		redirect(controller: 'purchaseOrderWorkflow', action: 'index');
		/*
        def orderInstance = new Order()
        orderInstance.properties = params
        return [orderInstance: orderInstance]
        */
    }

    def save = {
        def orderInstance = new Order(params)
        if (orderInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'order.label', default: 'Order'), orderInstance.id])}"
            redirect(action: "list", id: orderInstance.id)
        }
        else {
            render(view: "create", model: [orderInstance: orderInstance])
        }
    }

    def show = {
        def orderInstance = Order.get(params.id)
        if (!orderInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        }
        else {
            [orderInstance: orderInstance]
        }
    }

    def edit = {
        def orderInstance = Order.get(params.id)
        if (!orderInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [orderInstance: orderInstance]
        }
    }


	
    def update = {
        def orderInstance = Order.get(params.id)
        if (orderInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (orderInstance.version > version) {
                    
                    orderInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'order.label', default: 'Order')] as Object[], "Another user has updated this Order while you were editing")
                    render(view: "edit", model: [orderInstance: orderInstance])
                    return
                }
            }
            orderInstance.properties = params
            if (!orderInstance.hasErrors() && orderInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'order.label', default: 'Order'), orderInstance.id])}"
                redirect(action: "list", id: orderInstance.id)
            }
            else {
                render(view: "edit", model: [orderInstance: orderInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def orderInstance = Order.get(params.id)
        if (orderInstance) {
            try {
                orderInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'order.label', default: 'Order'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'order.label', default: 'Order'), params.id])}"
                redirect(action: "list", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        }
    }
	
	

	
	def addComment = { 
        def orderInstance = Order.get(params?.id)
        if (!orderInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'order.label', default: 'Order'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [orderInstance: orderInstance, commentInstance: new Comment()]
        }
	}
	
	def editComment = {
		def orderInstance = Order.get(params?.order?.id)
		if (!orderInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'order.label', default: 'Order'), params.id])}"
			redirect(action: "list")
		}
		else {
			def commentInstance = Comment.get(params?.id)
			if (!commentInstance) {
				flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'comment.label', default: 'Comment'), commentInstance.id])}"
				redirect(action: "show", id: orderInstance?.id)
			}
			render(view: "addComment", model: [orderInstance: orderInstance, commentInstance: commentInstance])
		}
	}
	
	def deleteComment = { 
		def orderInstance = Order.get(params.order.id)
		if (!orderInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'order.label', default: 'Order'), params.order.id])}"
			redirect(action: "list")
		}
		else {
			def commentInstance = Comment.get(params?.id)
			if (!commentInstance) {
				flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'comment.label', default: 'Comment'), params.id])}"
				redirect(action: "show", id: orderInstance?.id)
			}
			else { 
				orderInstance.removeFromComments(commentInstance);
				if (!orderInstance.hasErrors() && orderInstance.save(flush: true)) {
					flash.message = "${message(code: 'default.updated.message', args: [message(code: 'order.label', default: 'Order'), orderInstance.id])}"
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
					flash.message = "${message(code: 'default.updated.message', args: [message(code: 'comment.label', default: 'Comment'), commentInstance.id])}"
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
					flash.message = "${message(code: 'default.updated.message', args: [message(code: 'order.label', default: 'Order'), orderInstance.id])}"
					redirect(action: "show", id: orderInstance.id)
				}
				else {
					render(view: "addComment", model: [orderInstance: orderInstance, commentInstance:commentInstance])
				}
			}
		}	
		else { 
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'order.label', default: 'Order'), params.id])}"
			redirect(action: "list")
		}
		
	}

	def addDocument = {
		def orderInstance = Order.get(params.id)
		if (!orderInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'order.label', default: 'Order'), params.id])}"
			redirect(action: "list")
		}
		else {
			return [orderInstance: orderInstance]
		}
	}
	
	def editDocument = {
		def orderInstance = Order.get(params?.order?.id)
		if (!orderInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'order.label', default: 'Order'), params.id])}"
			redirect(action: "list")
		}
		else {
			def documentInstance = Document.get(params?.id)
			if (!documentInstance) {
				flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'document.label', default: 'Document'), documentInstance.id])}"
				redirect(action: "show", id: orderInstance?.id)
			}
			render(view: "addDocument", model: [orderInstance: orderInstance, documentInstance: documentInstance])
		}
	}
	
	def deleteDocument = {
		def orderInstance = Order.get(params.order.id)
		if (!orderInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'order.label', default: 'Order'), params.order.id])}"
			redirect(action: "list")
		}
		else {
			def documentInstance = Document.get(params?.id)
			if (!documentInstance) {
				flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'comment.label', default: 'Comment'), params.id])}"
				redirect(action: "show", id: orderInstance?.id)
			}
			else {
				orderInstance.removeFromDocuments(documentInstance);
				if (!orderInstance.hasErrors() && orderInstance.save(flush: true)) {
					flash.message = "${message(code: 'default.updated.message', args: [message(code: 'order.label', default: 'Order'), orderInstance.id])}"
					redirect(action: "show", id: orderInstance.id)
				}
				else {
					render(view: "show", model: [orderInstance: orderInstance])
				}
			}
		}
	}
	
	

	
	
		
}
