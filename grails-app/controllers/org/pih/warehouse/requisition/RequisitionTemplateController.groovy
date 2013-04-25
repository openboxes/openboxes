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

class RequisitionTemplateController {

    def requisitionService
    def inventoryService
	def productService

    static allowedMethods = [save: "POST", update: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

	def list = {
		def requisitions = []		
		requisitions = Requisition.findAllByIsTemplate(true)
		render(view:"list", model:[requisitions: requisitions])
	}

    def create = {
        println params
		def requisition = new Requisition(status: RequisitionStatus.CREATED)
        requisition.type = params.type as RequisitionType
        requisition.isTemplate = true
		
        [requisition:requisition]
    }

	def edit = {
		def requisition = Requisition.get(params.id)
        if (!requisition) {
            flash.message = "Could not find requisition with ID ${params.id}"
            redirect(action: "list")
        }
        else {
            [requisition: requisition];
        }
	}

	def save = {
        def requisition = new Requisition(params)

        if (!requisition.hasErrors() && requisition.save()) {
            flash.message = "Requisition template has been created"
        }
        else {
            flash.message = "there are errors"
            render(view: "create", model: [requisition:  requisition])
            return;
        }
        redirect(action: "edit", id: requisition.id)
	}

    def update = {
        def requisition = Requisition.get(params.id)
        if (requisition) {
            if (params.version) {
                def version = params.version.toLong()
                if (requisition.version > version) {
                    requisition.errors.rejectValue("version", "default.optimistic.locking.failure", [
                            warehouse.message(code: 'requisition.label', default: 'Requisition')] as Object[],
                            "Another user has updated this requisition while you were editing")
                    render(view: "edit", model: [requisition: requisition])
                    return
                }
            }
            requisition.properties = params
            if (!requisition.hasErrors() && requisition.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'requisition.label', default: 'Requisition'), params.id])}"
                redirect(action:"list")
            }
            else {
                render(view: "edit", model: [requisition: requisition])
            }
        }
        else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'requisition.label', default: 'Requisition'), params.id])}"
            redirect(action: "list")
        }
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

    def removeFromRequisitionItems = {
        def requisition = Requisition.get(params.id)

        if (requisition) {
            def requisitionItem = RequisitionItem.get(params?.requisitionItem?.id)
            if (requisitionItem) {
                requisition.removeFromRequisitionItems(requisitionItem)
                requisition.save()
            }
        }

        redirect(action: "edit", id: requisition.id)
    }

    def copy = {
        def requisition = Requisition.get(params.id)

        if (requisition) {


        }

    }


	private List<Location> getDepots() {
		Location.list().findAll {location -> location.id != session.warehouse.id && location.isWarehouse()}.sort{ it.name }
	}

	private List<Location> getWardsPharmacies() {
		def current = Location.get(session.warehouse.id)
		def locations = []
		if (current) { 
			if(current?.locationGroup == null) {
				locations = Location.list().findAll { location -> location.isWardOrPharmacy() }.sort { it.name }
			} else {
				locations = Location.list().findAll { location -> location.locationGroup?.id == current.locationGroup?.id }.findAll {location -> location.isWardOrPharmacy()}.sort { it.name }
			}
		}				
		return locations
	}

	
}
