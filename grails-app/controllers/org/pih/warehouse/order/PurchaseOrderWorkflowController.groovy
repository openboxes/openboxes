package org.pih.warehouse.order

import org.pih.warehouse.core.Address;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;
import org.springframework.dao.DataIntegrityViolationException;

class PurchaseOrderWorkflowController {

	def index = { redirect(action:"purchaseOrder") }
	def purchaseOrderFlow = {
		enterOrderDetails {
			on("submit") {
				def order = new Order(params)
				
				flow.order = order
				
				def e = yes()
				if(order.hasErrors() || !order.validate()) return error()
			}.to("showOrderItems")
			on("return").to "showCart"
			//on(Exception).to("handleError")
		}
		showOrderItems {
			on("back").to("enterOrderDetails")
			on("addItem") {
				if(!flow.order.orderItems) flow.order.orderItems = [] as HashSet
				if (params?.product?.id) { 
					def product = Product.get(params?.product?.id)
					if (product) { 
						def orderItem = new OrderItem();
						orderItem.product = product;
						orderItem.description = product.name
						orderItem.category = product.category
						orderItem.requestedBy = Person.get(session.user.id)
						orderItem.quantity = 1
						flow.order.addToOrderItems(orderItem);
					}
				}
				else if (params?.category?.id) { 
					def category = Category.get(params?.category?.id)
					if (category) { 
						log.info "params: " + params;
						def orderItem = new OrderItem();
						orderItem.description = params.description
						orderItem.category = category
						orderItem.requestedBy = Person.get(session.user.id)
						orderItem.quantity = 1
						flow.order.addToOrderItems(orderItem);
					}
				}
				else if (params.description) { 
					def orderItem = new OrderItem();
					orderItem.description = params.description
					orderItem.requestedBy = Person.get(session.user.id)
					orderItem.quantity = 1
					flow.order.addToOrderItems(orderItem);

				}
				else { 
					flash.message "You must specify a product OR category and description";
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
