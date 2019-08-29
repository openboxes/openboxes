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

import org.pih.warehouse.core.Location

class ShipmentItemController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def inventoryService
    def shipmentService


    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [shipmentItemInstanceList: ShipmentItem.list(params), shipmentItemInstanceTotal: ShipmentItem.count()]
    }

    def create = {
        def shipmentItemInstance = new ShipmentItem()
        shipmentItemInstance.properties = params
        return [shipmentItemInstance: shipmentItemInstance]
    }

    def save = {
        def shipmentItemInstance = new ShipmentItem(params)
        if (shipmentItemInstance.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'shipmentItem.label', default: 'ShipmentItem'), shipmentItemInstance.id])}"
            redirect(action: "list", id: shipmentItemInstance.id)
        } else {
            render(view: "create", model: [shipmentItemInstance: shipmentItemInstance])
        }
    }

    def show = {
        def shipmentItemInstance = ShipmentItem.get(params.id)
        if (!shipmentItemInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'shipmentItem.label', default: 'ShipmentItem'), params.id])}"
            redirect(action: "list")
        } else {
            [shipmentItemInstance: shipmentItemInstance]
        }
    }

    def edit = {
        def shipmentItemInstance = ShipmentItem.get(params.id)
        if (!shipmentItemInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'shipmentItem.label', default: 'ShipmentItem'), params.id])}"
            redirect(action: "list")
        } else {
            [shipmentItemInstance: shipmentItemInstance]
        }
    }

    def update = {
        def shipmentItemInstance = ShipmentItem.get(params.id)
        if (shipmentItemInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (shipmentItemInstance.version > version) {

                    shipmentItemInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'shipmentItem.label', default: 'ShipmentItem')] as Object[], "Another user has updated this ShipmentItem while you were editing")
                    render(view: "edit", model: [shipmentItemInstance: shipmentItemInstance])
                    return
                }
            }
            shipmentItemInstance.properties = params
            if (!shipmentItemInstance.hasErrors() && shipmentItemInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'shipmentItem.label', default: 'ShipmentItem'), shipmentItemInstance.id])}"
                redirect(action: "list", id: shipmentItemInstance.id)
            } else {
                render(view: "edit", model: [shipmentItemInstance: shipmentItemInstance])
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'shipmentItem.label', default: 'ShipmentItem'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def shipmentItemInstance = ShipmentItem.get(params.id)
        if (shipmentItemInstance) {
            try {
                shipmentItemInstance.delete(flush: true)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'shipmentItem.label', default: 'ShipmentItem'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'shipmentItem.label', default: 'ShipmentItem'), params.id])}"
                redirect(action: "list", id: params.id)
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'shipmentItem.label', default: 'ShipmentItem'), params.id])}"
            redirect(action: "list")
        }
    }

    def pick = {
        def shipmentItem = ShipmentItem.get(params.id)
        if (!shipmentItem) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'shipmentItem.label', default: 'ShipmentItem'), params.id])}"
        } else {
            Location location = Location.load(session.warehouse.id)
            List binLocations = inventoryService.getProductQuantityByBinLocation(location, shipmentItem.product)
            List binLocationSelected = binLocations.findAll {
                it?.binLocation == shipmentItem?.binLocation && it.inventoryItem == shipmentItem?.inventoryItem
            }
            [shipmentItem: shipmentItem, binLocations: binLocations, binLocationSelected: binLocationSelected]
        }

    }

    def split = {
        log.info "Split " + params
        def shipmentItemInstance = ShipmentItem.get(params.id)
        if (!shipmentItemInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'shipmentItem.label', default: 'ShipmentItem'), params.id])}"
        } else {
            Location location = Location.load(session.warehouse.id)
            List binLocations = inventoryService.getProductQuantityByBinLocation(location, shipmentItemInstance.product)

            [shipmentItemInstance: shipmentItemInstance, binLocations: binLocations]
        }
    }


    def updatePicklistItem = {

        def shipmentItemInstance = ShipmentItem.get(params.id)
        if (shipmentItemInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (shipmentItemInstance.version > version) {

                    shipmentItemInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'shipmentItem.label', default: 'ShipmentItem')] as Object[], "Another user has updated this ShipmentItem while you were editing")
                    render(view: "pick", model: [shipmentItemInstance: shipmentItemInstance])
                    return
                }
            }
            if (!shipmentItemInstance.hasErrors() && shipmentItemInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'shipmentItem.label', default: 'ShipmentItem'), shipmentItemInstance.id])}"
                redirect(action: "pick", id: shipmentItemInstance.id)
            } else {
                render(view: "pick", model: [shipmentItemInstance: shipmentItemInstance])
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'shipmentItem.label', default: 'ShipmentItem'), params.id])}"
            redirect(action: "pick")
        }

    }


}
