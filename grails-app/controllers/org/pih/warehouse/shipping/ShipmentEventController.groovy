package org.pih.warehouse.shipping

class ShipmentEventController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [shipmentEventInstanceList: ShipmentEvent.list(params), shipmentEventInstanceTotal: ShipmentEvent.count()]
    }

    def create = {
        def shipmentEventInstance = new ShipmentEvent()
        shipmentEventInstance.properties = params
        return [shipmentEventInstance: shipmentEventInstance]
    }

    def save = {
        def shipmentEventInstance = new ShipmentEvent(params)
        if (shipmentEventInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'shipmentEvent.label', default: 'ShipmentEvent'), shipmentEventInstance.id])}"
            redirect(action: "show", id: shipmentEventInstance.id)
        }
        else {
            render(view: "create", model: [shipmentEventInstance: shipmentEventInstance])
        }
    }

    def show = {
        def shipmentEventInstance = ShipmentEvent.get(params.id)
        if (!shipmentEventInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'shipmentEvent.label', default: 'ShipmentEvent'), params.id])}"
            redirect(action: "list")
        }
        else {
            [shipmentEventInstance: shipmentEventInstance]
        }
    }

    def edit = {
        def shipmentEventInstance = ShipmentEvent.get(params.id)
        if (!shipmentEventInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'shipmentEvent.label', default: 'ShipmentEvent'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [shipmentEventInstance: shipmentEventInstance]
        }
    }

    def update = {
        def shipmentEventInstance = ShipmentEvent.get(params.id)
        if (shipmentEventInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (shipmentEventInstance.version > version) {
                    
                    shipmentEventInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'shipmentEvent.label', default: 'ShipmentEvent')] as Object[], "Another user has updated this ShipmentEvent while you were editing")
                    render(view: "edit", model: [shipmentEventInstance: shipmentEventInstance])
                    return
                }
            }
            shipmentEventInstance.properties = params
            if (!shipmentEventInstance.hasErrors() && shipmentEventInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'shipmentEvent.label', default: 'ShipmentEvent'), shipmentEventInstance.id])}"
                redirect(action: "show", id: shipmentEventInstance.id)
            }
            else {
                render(view: "edit", model: [shipmentEventInstance: shipmentEventInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'shipmentEvent.label', default: 'ShipmentEvent'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def shipmentEventInstance = ShipmentEvent.get(params.id)
        if (shipmentEventInstance) {
            try {
                shipmentEventInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'shipmentEvent.label', default: 'ShipmentEvent'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'shipmentEvent.label', default: 'ShipmentEvent'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'shipmentEvent.label', default: 'ShipmentEvent'), params.id])}"
            redirect(action: "list")
        }
    }
}
