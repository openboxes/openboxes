/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 * */
package org.pih.warehouse.requisition

import org.pih.warehouse.core.Location
import grails.converters.JSON
import org.pih.warehouse.picklist.Picklist



class RequisitionController {

    def requisitionService
    def inventoryService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        def requisitions = Requisition.findAll()//Requisition.findAllByStatus(RequisitionStatus.CREATED)
        render(view:"list", model:[requisitions: requisitions])
    }

    def create = {
        def requisition = new Requisition(status: RequisitionStatus.NEW)
        render(view:"edit", model:[requisition:requisition.toJson() as JSON, depots: getDepots(), requisitionId: null])
    }

    def edit = {
        def requisition = Requisition.get(params.id)
        if(requisition) {
           def depots = getDepots()
           String jsonString = requisition.toJson() as JSON
           return [requisition: jsonString, depots: depots, requisitionId: requisition.id];  
        }else
        {
          response.sendError(404)
        }
    }

    private List<Location> getDepots() {
        Location.list().findAll {location -> location.id != session.warehouse.id && location.isWarehouse()}.sort{ it.name }
    }

    def save = {
        def jsonRequest = request.JSON
        def jsonResponse = []
        def requisition = requisitionService.saveRequisition(jsonRequest, Location.get(session.warehouse.id))
        if (!requisition.hasErrors()) {
            jsonResponse = [success: true, data: requisition.toJson()]
        }
        else {
            jsonResponse = [success: false, errors: requisition.errors]
        }
        render jsonResponse as JSON
    }

    def process = {
        def requisition = Requisition.get(params?.id)
        if (requisition) {
            requisition.status = RequisitionStatus.OPEN
            def currentInventory = Location.get(session.warehouse.id).inventory
            def picklist = Picklist.findByRequisition(requisition)?: new Picklist()
            def productInventoryItemsMap = [:]
            def productInventoryItems = inventoryService.getInventoryItemsWithQuantity(requisition.requisitionItems?.collect{ it.product}, currentInventory)
            productInventoryItems.keySet().each { product ->
                productInventoryItemsMap[product.id] = productInventoryItems[product].collect{it.toJson()}
            }

            String jsonString = [requisition: requisition.toJson(), productInventoryItemsMap: productInventoryItemsMap, picklist: picklist.toJson()] as JSON
            return [data: jsonString, requisitionId: requisition.id]
        } else{
            response.sendError(404)
        }
    }

    def cancel = {
        def requisition = Requisition.get(params?.id)
        if (requisition) {
            requisitionService.cancelRequisition(requisition)
            flash.message = "${warehouse.message(code: 'default.cancelled.message', args: [warehouse.message(code: 'requisition.label', default: 'Requisition'), params.id])}"
        }
        redirect(action: "list")
    }

    def show = {
        def requisition = Requisition.get(params.id)
        if (!requisition) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [requisition: requisition]
        }
    }

    def delete = {
        def requisition = Requisition.get(params.id)
        if (requisition) {
            try {
                requisitionService.deleteRequisition(requisition)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'requisition.label', default: 'Requisition'), params.id])}"
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'requisition.label', default: 'Requisition'), params.id])}"
            }
        }
        else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'requisition.label', default: 'Requisition'), params.id])}"
        }
        redirect(action: "list", id:params.id)
    }

    def printDraft = {
        def requisition = Requisition.get(params.id)
        def location = Location.get(session.warehouse.id)
        render(view:"printDraft", model:[requisition:requisition, location:location])
    }
}
