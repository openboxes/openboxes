package org.pih.warehouse.order

import org.pih.warehouse.core.Address;
import org.pih.warehouse.core.Location;
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
				
				flow.suppliers = orderService.getSuppliers();
				// create a new shipment instance if we don't have one already
				if (params.id) {
					flow.order = Order.get(params.id as Long)
				}
				else {
					def order = new Order();
					order.orderNumber = new Random().nextInt(9999999)
					order.orderedBy = Person.get(session.user.id)
					flow.order = order;
				}
				
				
				if (params.skipTo) {
					if (params.skipTo == 'details') return success()
					else if (params.skipTo == 'items') return showOrderItems()
					//else if (params.skipTo == 'confirm') return confirmOrder()
					
				}
				
				return success()
			}
			on("success").to("enterOrderDetails")
			on("showOrderItems").to("showOrderItems")
			//on("confirmOrder").to("confirmOrder")			
		}
		
		
		enterOrderDetails {
			on("next") {
				flow.order.properties = params
				if (!orderService.saveOrder(flow.order)) {
					return error()
				}
			}.to("showOrderItems")
			on("cancel").to("cancel")
			on("finish").to("finish")
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
					orderItem.delete();
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
			
			on("next") {
				log.info "confirm order " + params
				flow.order.properties = params
				
				log.info("order " + flow.order)
			

					
			}.to("finish")

			on("cancel").to("cancel")
			on("finish").to("finish")
			on("error").to("showOrderItems")
		}
		/*
		confirmOrder  {
			on("back").to("showOrderItems")
			on("next").to("finish")
			on("error").to("confirmOrder")
			on(Exception).to("confirmOrder")
		}
		*/
		finish {
			
			action {
				log.info("Finishing workflow, save order object " + flow.order)
				def order = flow.order;

				try {
					
					if (!orderService.saveOrder(flow.order)) {
						return error()
					}
					else { 
						return success()
					} 
					
					/*
					if(!order.hasErrors() && order.save(flush:true)) {
						log.info "success"
						return success();
					}
					else {
						order.errors.allErrors.each { println it; }
						log.info "errors"
						return error()
					}
					*/
				} catch (DataIntegrityViolationException e) {
					log.info ("data integrity exception")
					return error();
				}
			}
			on("success").to("showOrder")
		}
		showOrder { 
			redirect(controller:"order", action : "show", params : [ "id" : flow.order.id ?: '' ])
		}
		
		handleError()
	}
}
