/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.order

import grails.validation.ValidationException
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.Person
import org.pih.warehouse.product.Product
import org.springframework.dao.DataIntegrityViolationException

class PurchaseOrderWorkflowController {

    def orderService
    def identifierService

    def index = { redirect(action: "purchaseOrder") }
    def purchaseOrderFlow = {

        start {
            action {
                log.info("Starting order workflow " + params)
                if (params.id) {
                    flow.order = Order.get(params.id)
                } else {
                    def order = new Order()
                    order.orderedBy = Person.get(session.user.id)
                    flow.order = order
                }


                if (params.skipTo) {
                    if (params.skipTo == 'details') return success()
                    else if (params.skipTo == 'items') return showOrderItems()
                }

                return success()
            }
            on("success").to("enterOrderDetails")
            on("showOrderItems").to("showOrderItems")
        }


        enterOrderDetails {
            on("next") {
                log.info "Save order details " + params
                flow.order.properties = params
                try {
                    if (!orderService.saveOrder(flow.order)) {
                        return error()
                    }
                } catch (ValidationException e) {
                    return error()
                }
            }.to("showOrderItems")
            on("showOrderItems").to("showOrderItems")
            on("enterOrderDetails").to("enterOrderDetails")
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
                    flow.order.removeFromOrderItems(orderItem)
                    orderItem.delete()
                }
            }.to("showOrderItems")

            on("editItem") {
                def orderItem = OrderItem.get(params.id)
                if (orderItem) {
                    flow.orderItem = orderItem
                }
            }.to("showOrderItems")

            on("addItem") {
                log.info "adding an item " + params
                if (!flow.order.orderItems) flow.order.orderItems = [] as HashSet

                def product = Product.get(params.product?.id)
                def supplier = Organization.get(params.supplier?.id)
                def orderItem = OrderItem.get(params.orderItem?.id)
                if (orderItem) {
                    orderItem.properties = params
                } else {
                    orderItem = new OrderItem(params)
                }

                orderItem.category = product?.category
                orderItem.description = product?.name
                orderItem.requestedBy = Person.get(session.user.id)

                if (!orderItem.validate() || orderItem.hasErrors()) {
                    flow.orderItem = orderItem
                    return error()
                }

                flow.order.addToOrderItems(orderItem)
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
            on("enterOrderDetails").to("enterOrderDetails")
            on("showOrderItems").to("showOrderItems")
            on("cancel").to("cancel")
            on("finish").to("finish")
            on("error").to("showOrderItems")
        }

        finish {

            action {
                log.info("Finishing workflow, save order object " + flow.order)
                try {

                    if (!orderService.saveOrder(flow.order)) {
                        return error()
                    } else {
                        flash.message = "You have successfully created a new purchase order.  Please select Issue PO "
                        return success()
                    }

                } catch (DataIntegrityViolationException e) {
                    log.info("data integrity exception")
                    return error()
                }
            }
            on("success").to("showOrder")
        }
        cancel {
            redirect(controller: "order", action: "show", params: ["id": flow.order.id ?: ''])
        }
        showOrder {

            redirect(controller: "order", action: "show", params: ["id": flow.order.id ?: ''])
        }

        handleError()
    }
}
