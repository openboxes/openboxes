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
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.User

class PurchaseOrderController {


    def orderService
    def identifierService


    def index = { redirect(action: "create") }


    def create = {
        Location currentLocation = Location.get(session.warehouse.id)
        User user = User.get(session.user.id)
        Boolean isCentralPurchasingEnabled = currentLocation.supports(ActivityCode.ENABLE_CENTRAL_PURCHASING)
        Order order = orderService.createNewPurchaseOrder(currentLocation, user, isCentralPurchasingEnabled)
        render(template: "enterOrderDetails", model: [order: order, isCentralPurchasingEnabled: isCentralPurchasingEnabled])
    }


    def edit = {
        Order order = Order.get(params?.id)
        Location currentLocation = Location.get(session.warehouse.id)
        def isCentralPurchasingEnabled = currentLocation.supports(ActivityCode.ENABLE_CENTRAL_PURCHASING)
        if (order) {
            render(template: "enterOrderDetails", model: [order: order, isCentralPurchasingEnabled: isCentralPurchasingEnabled])
        } else {
            redirect(action: "create")
        }
    }


    def saveOrderDetails = {
        def order
        if (params.order?.id) {
            order = Order.get(params.order.id)
        } else {
            order = new Order()
        }

        if (order.orderItems && order.origin.id != params.origin.id) {
            order.errors.reject("purchaseOrder.supplierError.label", "Cannot change the supplier for a PO with item lines.")
            render(template: "enterOrderDetails", model: [order: order])
            return
        } else {
            order.properties = params
            try {
                if (!orderService.saveOrder(order)) {
                    order.errors.reject("purchaseOrder.saveError.label", "Can't save order due to an error.")
                }
            } catch (ValidationException e) {
                order.errors = e.errors
            }
        }

        if (order.hasErrors()) {
            render(template: "enterOrderDetails", model: [order: order])
            return
        }

        redirect(action: addItems, id: order.id)
    }


    def addItems = {
        Order order = Order.get(params?.id)
        def currentLocation = Location.get(session.warehouse.id)
        def isAccountingRequired = currentLocation?.isAccountingRequired()
        if (order) {
            render(template: "showOrderItems", model: [order: order, isAccountingRequired: isAccountingRequired])
        } else {
            redirect(action: "create")
        }
    }
}
