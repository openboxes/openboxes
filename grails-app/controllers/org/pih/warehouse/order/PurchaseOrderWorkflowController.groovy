package org.pih.warehouse.order

import org.pih.warehouse.core.Address;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;
import org.springframework.dao.DataIntegrityViolationException;

class PurchaseOrderWorkflowController {

	def orderService;
	
	def index = { redirect(action:"purchaseOrder") }
	def purchaseOrderFlow = {		
		
		start {
			action {
				log.info("Starting order workflow " + params)
				
				// create a new shipment instance if we don't have one already
				if (params.id) {
					flow.order = Order.get(params.id as Long)
				}
				else {
					def order = new Order();
					order.orderNumber = new Random().nextInt(9999999)
					flow.order = order;
				}
				
				
				if (params.skipTo) {
					if (params.skipTo == 'details') return success()
					else if (params.skipTo == 'items') return showOrderItems()
					else if (params.skipTo == 'confirm') return confirmOrder()
					
				}
				
				return success()
			}
			on("success").to("enterOrderDetails")
			on("showOrderItems").to("showOrderItems")
			on("confirmOrder").to("confirmOrder")
			
			
		}
		
		
		enterOrderDetails {
			on("submit") {
				def e = yes()
				flow.order.properties = params
				if (!orderService.saveOrder(flow.order)) {
					return error()
				}
			}.to("showOrderItems")
		}
		showOrderItems {
			on("back") { 
				log.info "saving items " + params
				flow.order.properties = params
				if (!orderService.saveOrder(flow.order)) {
					return error()
				}

			}.to("enterOrderDetails")
			
			on("deleteItem") { 
				log.info "deleting an item " + params
				def orderItem = OrderItem.get(params.id)
				if (orderItem) { 
					flow.order.removeFromOrderItems(orderItem);
				}
			}.to("showOrderItems")
			
			
			on("addItem") {
				log.info "adding an item " + params
				if(!flow.order.orderItems) flow.order.orderItems = [] as HashSet
				
				def orderItem = new OrderItem(params);
				orderItem.requestedBy = Person.get(session.user.id)
				
				if (params?.product?.id && params?.category?.id) { 
					log.info("error with product and category")
					orderItem.errors.rejectValue("product.id", "Please choose a product OR a category OR enter a description")
					flow.orderItem = orderItem
					return error()
				}				
				else if (params?.product?.id) { 
					def product = Product.get(params?.product?.id)
					if (product) { 
						orderItem.description = product.name
						orderItem.category = product.category
					}
				}
				else if (params?.category?.id) { 
					def category = Category.get(params?.category?.id) 
					if (category) {
						orderItem.description = category.name
						//orderItem.category = category
					}
				}
				
				if (!orderItem.validate() || orderItem.hasErrors()) { 
					
					flow.orderItem = orderItem
					return error();
				}
				flow.order.addToOrderItems(orderItem);
				if (!orderService.saveOrder(flow.order)) {
					return error()
				}
				flow.orderItem = null
				
			}.to("showOrderItems")
			
			on("confirmOrder") {
				log.info "confirm order " + params
				flow.order.properties = params
				
				log.info("order item " + flow.order)
				if (!orderService.saveOrder(flow.order)) {
					return error()
				}

					
			}.to("confirmOrder")

			on("error").to("showOrderItems")
		}
		confirmOrder  {
			on("back").to("showOrderItems")
			on("processOrder").to("processOrder")
		}
		processOrder  {
			action {				
				def order = flow.order;

				try {
					if(!order.hasErrors() && order.save(flush:true)) {
						return success();
					}
					else { 
						order.errors.allErrors.each { println it; }
						return error()
					}
				} catch (DataIntegrityViolationException e) { 
					log.info ("data integrity exception")
					return error();
				}
			}
			on("error") { 
				log.info "error"
			}.to("confirmOrder")
			on(Exception).to("confirmOrder")
			on("success").to("showOrder")
			
		}
		showOrder {
			redirect(controller:"order", action : "show", params : [ "id" : flow.order.id ?: '' ])
		}
		
		handleError()
	}
}
