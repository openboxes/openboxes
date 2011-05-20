package org.pih.warehouse.order

import org.pih.warehouse.core.Address;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;
import org.springframework.dao.DataIntegrityViolationException;

class PurchaseOrderWorkflowController {

	def index = { redirect(action:"purchaseOrder") }
	def purchaseOrderFlow = {		
		
		start {
			action {
				log.info("Starting order workflow " + params)
				
				// create a new shipment instance if we don't have one already
				if (!flow.order) {
					flow.order = Order.get(params.id as Long)
				}
				/*
				if (params.skipTo) {
					if (params.skipTo == 'Packing')
						return enterContainerDetails()
					else if (params.skipTo == 'Details')
						return enterShipmentDetails()
					else if (params.skipTo == 'Tracking')
						return enterTrackingDetails()
				}
				*/
				return success()
			}
			on("success").to("enterOrderDetails")

		}
		
		
		enterOrderDetails {
			on("submit") {
				if (!flow.order) {
					def order = new Order(params)				
					flow.order = order
				}
				
				def e = yes()
				if(flow.order.hasErrors() || !flow.order.validate()) return error()
			}.to("showOrderItems")
			//on("return").to "showCart"
			//on(Exception).to("handleError")
		}
		showOrderItems {
			on("back").to("enterOrderDetails")
			on("deleteItem") { 
				log.info params
				def orderItem = OrderItem.get(params.id)
				if (orderItem) { 
					flow.order.removeFromOrderItems(orderItem);
				}
			}.to("showOrderItems")
			
			on("addItem") {
				if(!flow.order.orderItems) flow.order.orderItems = [] as HashSet
				if (params?.product?.id) { 
					def product = Product.get(params?.product?.id)
					if (product) { 
						def orderItem = new OrderItem(params);
						orderItem.description = product.name
						orderItem.category = product.category
						orderItem.requestedBy = Person.get(session.user.id)
						flow.order.addToOrderItems(orderItem);
					}
				}
				else if (params?.category?.id) { 
					def category = Category.get(params?.category?.id)
					if (category) { 
						log.info "params: " + params;
						def orderItem = new OrderItem(params);
						orderItem.category = category
						orderItem.requestedBy = Person.get(session.user.id)
						flow.order.addToOrderItems(orderItem);
					}
				}
				else if (params.description) { 
					def orderItem = new OrderItem(params);
					orderItem.requestedBy = Person.get(session.user.id)
					flow.order.addToOrderItems(orderItem);

				}
				else { 
					flash.message "You must specify a product OR category and description";
				}
				
				if(!flow.order.hasErrors() && flow.order.save(flush:true)) {
				
				}
				
				//flow.order.orderItems = orderItems
			}.to("showOrderItems")
				
			on("searchProducts").to("getProducts")
			on("confirmOrder").to("confirmOrder")
		}
		confirmOrder  {
			on("back").to("showOrderItems")
			on("processOrder").to("processOrder")
		}
		processOrder  {
			action {				
				//def order = new Order();
				def order = flow.order;
				order.orderNumber = new Random().nextInt(9999999)
				//def orderItems = flow.orderItems;
				//orderItems.each { orderItem ->
				//	order.addToOrderItems(orderItem) 
				//}

				try {
					if(!order.hasErrors() && order.save(flush:true)) {
						log.info "NO ERRORS"
						flash.message = "Saved successfully"
						
						log.info "Redirecting to show order page"
						//redirect(controller: "order", action: "show", id: order?.id)
						return success();
						//return success();
					}
					else { 
						log.info "ERRORS"
						//[order : order]
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
			on("success"){ 
				log.info "Success "	
			} .to("displayInvoice")
			
		}
		displayInvoice { 
			on("back").to("showOrderItems")
		}
		handleError()
	}
}
