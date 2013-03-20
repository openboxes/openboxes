/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.requisition

import org.pih.warehouse.core.Location;

class RequisitionItemController {

	def requisitionService
	def inventoryService
	
    //static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [requisitionItemInstanceList: RequisitionItem.list(params), requisitionItemInstanceTotal: RequisitionItem.count()]
    }

    def create = {
        def requisitionItemInstance = new RequisitionItem()
        requisitionItemInstance.properties = params
        return [requisitionItemInstance: requisitionItemInstance]
    }

    def save = {
        def requisitionItemInstance = new RequisitionItem(params)
        if (requisitionItemInstance.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'requisitionItem.label', default: 'RequisitionItem'), requisitionItemInstance.id])}"
            redirect(action: "list", id: requisitionItemInstance.id)
        }
        else {
            render(view: "create", model: [requisitionItemInstance: requisitionItemInstance])
        }
    }

    def show = {
        def requisitionItemInstance = RequisitionItem.get(params.id)
        if (!requisitionItemInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'requisitionItem.label', default: 'RequisitionItem'), params.id])}"
            redirect(action: "list")
        }
        else {
            [requisitionItemInstance: requisitionItemInstance]
        }
    }

    def edit = {
        def requisitionItemInstance = RequisitionItem.get(params.id)
        if (!requisitionItemInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'requisitionItem.label', default: 'RequisitionItem'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [requisitionItemInstance: requisitionItemInstance]
        }
    }

	def change = { 
		def location = Location.get(session.warehouse.id)
		def requisitionItemInstance = RequisitionItem.get(params.id)
		def quantityOnHand = inventoryService.getQuantityOnHand(location, requisitionItemInstance?.product)?:0
		def quantityOutgoing = inventoryService.getQuantityToShip(location, requisitionItemInstance?.product)?:0
		def quantityAvailableToPromise = (quantityOnHand - quantityOutgoing)?:0;
		
		if (!requisitionItemInstance) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'requisitionItem.label', default: 'RequisitionItem'), params.id])}"
			redirect(action: "list")
		}
		else {
			return [requisitionItemInstance: requisitionItemInstance, quantityOnHand: quantityOnHand, quantityAvailableToPromise: quantityAvailableToPromise]
		}
	}
	
    def update = {
        def requisitionItemInstance = RequisitionItem.get(params.id)
        if (requisitionItemInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (requisitionItemInstance.version > version) {
                    
                    requisitionItemInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'requisitionItem.label', default: 'RequisitionItem')] as Object[], "Another user has updated this RequisitionItem while you were editing")
                    render(view: "edit", model: [requisitionItemInstance: requisitionItemInstance])
                    return
                }
            }
            requisitionItemInstance.properties = params
            if (!requisitionItemInstance.hasErrors() && requisitionItemInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'requisitionItem.label', default: 'RequisitionItem'), requisitionItemInstance.id])}"
                //redirect(action: "list", id: requisitionItemInstance.id)
				redirect(controller: "requisition", action: "review", id: requisitionItemInstance?.requisition?.id)
            }
            else {
                render(view: "edit", model: [requisitionItemInstance: requisitionItemInstance])
            }
        }
        else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'requisitionItem.label', default: 'RequisitionItem'), params.id])}"
            //redirect(action: "list")
			redirect(controller: "requisition", action: "review", id: requisitionItemInstance?.requisition?.id)
        }
    }

    def delete = {
		
		println "Delete requisition item " + params
        def requisitionItemInstance = RequisitionItem.get(params.id)
        if (requisitionItemInstance) {
            try {
				def requisition = requisitionItemInstance.requisition
				
				if (requisitionItemInstance.parentRequisitionItem) { 
					requisitionItemInstance.parentRequisitionItem.removeFromRequisitionItems(requisitionItemInstance)
				}
				
    			requisition.removeFromRequisitionItems(requisitionItemInstance)
	            requisitionItemInstance.delete(flush: true)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'requisitionItem.label', default: 'RequisitionItem'), params.id])}"
                //redirect(action: "list")
				redirect(controller: "requisition", action: "review", id: requisition?.id)
			}
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'requisitionItem.label', default: 'RequisitionItem'), params.id])}"
                //redirect(action: "list", id: params.id)
				redirect(controller: "requisition", action: "review", id: requisition?.id)
				
            }
        }
        else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'requisitionItem.label', default: 'RequisitionItem'), params.id])}"
            //redirect(action: "list")
			redirect(controller: "requisition", action: "review", id: requisition?.id)
        }
    }
	
	def cancel = {
		log.info "Cancel requisition item " + params
		
		def requisitionItem = RequisitionItem.get(params.id)
		if (requisitionItem) {
			requisitionItem.properties = params
			//requisitionItem.quantityCanceled = requisitionItem.calculateQuantityRemaining()
			requisitionItem.save(flush:true)
			redirect(controller: "requisition", action: "pick", id: requisitionItem?.requisition?.id , params:['requisitionItem.id':requisitionItem.id])
		}
		else { 
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'requisitionItem.label', default: 'RequisitionItem'), params.id])}"
			redirect(controller: "requisition", action: "list")

		}
    }

	
	def uncancel = {
		def requisitionItem = RequisitionItem.get(params.id)
		if (requisitionItem) {
			requisitionItem.quantityCanceled = 0 
			requisitionItem.cancelComments = null
			requisitionItem.cancelReasonCode = null
			requisitionItem.save(flush:true)
			redirect(controller: "requisition", action: "pick", id: requisitionItem?.requisition?.id, params:['requisitionItem.id':requisitionItem.id])
		}
		else {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'requisitionItem.label', default: 'RequisitionItem'), params.id])}"
			redirect(controller: "requisition", action: "list")

		}
	}
	
	
	

}
