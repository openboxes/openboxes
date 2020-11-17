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

import org.pih.warehouse.core.Location

class OrderAdjustmentTypeController {

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [orderAdjustmentTypes: OrderAdjustmentType.list(params), orderAdjustmentTypesTotal: OrderAdjustmentType.count()]
    }

    def create = {
        def orderAdjustmentType = new OrderAdjustmentType()
        orderAdjustmentType.properties = params
        def location = Location.get(session?.warehouse?.id)
        return [orderAdjustmentType: orderAdjustmentType, locationInstance: location]
    }

    def edit = {
        def location = Location.get(session?.warehouse?.id)
        def orderAdjustmentType = OrderAdjustmentType.get(params.id)
        return [orderAdjustmentType: orderAdjustmentType, locationInstance: location]
    }

    def save = {
        def orderAdjustmentType = new OrderAdjustmentType(params)
        if (orderAdjustmentType.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'orderAdjustmentType.label', default: 'Order Adjustment Type'), orderAdjustmentType.id])}"
            redirect(controller: "orderAdjustmentType", action: "edit", id: orderAdjustmentType?.id)
        } else {
            def location = Location.get(session?.warehouse?.id)
            render(view: "create", model: [orderAdjustmentType: orderAdjustmentType, locationInstance: location])
        }
    }

    def update = {
        def orderAdjustmentType = OrderAdjustmentType.get(params.id)
        if (orderAdjustmentType) {
            orderAdjustmentType.properties = params
            if (!orderAdjustmentType.hasErrors() && orderAdjustmentType.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'orderAdjustmentType.label', default: 'Order Adjustment Type'), orderAdjustmentType.id])}"
                redirect(action: "list")
            } else {
                def location = Location.get(session?.warehouse?.id)
                render(view: "edit", model: [orderAdjustmentType: orderAdjustmentType, locationInstance: location])
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'orderAdjustmentType.label', default: 'Order Adjustment Type'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def orderAdjustmentType = OrderAdjustmentType.get(params.id)
        if (orderAdjustmentType) {
            try {
                orderAdjustmentType.delete(flush: true)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'orderAdjustmentType.label', default: 'Order Adjustment Type'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'orderAdjustmentType.label', default: 'Order Adjustment Type'), params.id])}"
                redirect(action: "list", id: params.id)
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'orderAdjustmentType.label', default: 'Order Adjustment Type'), params.id])}"
            redirect(action: "list")
        }
    }

}
