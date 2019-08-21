/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.shipping

import org.pih.warehouse.core.Document
import org.pih.warehouse.core.DocumentCode
import org.pih.warehouse.core.DocumentType

class ShipmentWorkflowController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [shipmentWorkflowInstanceList: ShipmentWorkflow.list(params), shipmentWorkflowInstanceTotal: ShipmentWorkflow.count()]
    }

    def create = {
        def shipmentWorkflowInstance = new ShipmentWorkflow()
        shipmentWorkflowInstance.properties = params
        return [shipmentWorkflowInstance: shipmentWorkflowInstance, documentTemplates: documentTemplates]
    }

    def save = {
        def shipmentWorkflowInstance = new ShipmentWorkflow(params)
        if (shipmentWorkflowInstance.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'shipmentWorkflow.label', default: 'ShipmentWorkflow'), shipmentWorkflowInstance.id])}"
            redirect(action: "list", id: shipmentWorkflowInstance.id)
        } else {
            render(view: "create", model: [shipmentWorkflowInstance: shipmentWorkflowInstance])
        }
    }

    def show = {
        def shipmentWorkflowInstance = ShipmentWorkflow.get(params.id)
        if (!shipmentWorkflowInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'shipmentWorkflow.label', default: 'ShipmentWorkflow'), params.id])}"
            redirect(action: "list")
        } else {
            [shipmentWorkflowInstance: shipmentWorkflowInstance]
        }
    }

    def edit = {
        def shipmentWorkflowInstance = ShipmentWorkflow.get(params.id)
        if (!shipmentWorkflowInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'shipmentWorkflow.label', default: 'ShipmentWorkflow'), params.id])}"
            redirect(action: "list")
        } else {
            return [shipmentWorkflowInstance: shipmentWorkflowInstance, documentTemplates: documentTemplates]
        }
    }

    def update = {
        def shipmentWorkflowInstance = ShipmentWorkflow.get(params.id)
        if (shipmentWorkflowInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (shipmentWorkflowInstance.version > version) {

                    shipmentWorkflowInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'shipmentWorkflow.label', default: 'ShipmentWorkflow')] as Object[], "Another user has updated this ShipmentWorkflow while you were editing")
                    render(view: "edit", model: [shipmentWorkflowInstance: shipmentWorkflowInstance, documentTemplates: documentTemplates])
                    return
                }
            }
            shipmentWorkflowInstance.properties = params
            if (!shipmentWorkflowInstance.hasErrors() && shipmentWorkflowInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'shipmentWorkflow.label', default: 'ShipmentWorkflow'), shipmentWorkflowInstance.id])}"
                redirect(action: "list", id: shipmentWorkflowInstance.id)
            } else {
                render(view: "edit", model: [shipmentWorkflowInstance: shipmentWorkflowInstance, documentTemplates: documentTemplates])
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'shipmentWorkflow.label', default: 'ShipmentWorkflow'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def shipmentWorkflowInstance = ShipmentWorkflow.get(params.id)
        if (shipmentWorkflowInstance) {
            try {
                shipmentWorkflowInstance.delete(flush: true)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'shipmentWorkflow.label', default: 'ShipmentWorkflow'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'shipmentWorkflow.label', default: 'ShipmentWorkflow'), params.id])}"
                redirect(action: "list", id: params.id)
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'shipmentWorkflow.label', default: 'ShipmentWorkflow'), params.id])}"
            redirect(action: "list")
        }
    }


    List getDocumentTemplates() {
        def documentTypes = DocumentType.findAllByDocumentCode(DocumentCode.SHIPPING_TEMPLATE)
        Document.findAllByDocumentTypeInList(documentTypes)
    }
}
