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
				/*
				if (params.skipTo) {
					if (params.skipTo == 'Packing') return enterContainerDetails()
					else if (params.skipTo == 'Details') return enterShipmentDetails()
					else if (params.skipTo == 'Tracking') return enterTrackingDetails()
				}
				*/
				return success()
			}
			on("success").to("enterOrderDetails")

		}
		
		
		enterOrderDetails {
			on("submit") {
				def e = yes()
				flow.order.properties = params
				if(flow.order.hasErrors() || !flow.order.validate()) { 
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
				
				if (params?.product?.id) { 
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
						orderItem.category = category
					}
	
					
				}
				orderItem.requestedBy = Person.get(session.user.id)	
				if (!orderItem.validate()) { 
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
