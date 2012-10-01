/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.request

import java.util.Date;

import org.pih.warehouse.core.Comment;
import org.pih.warehouse.core.Document;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.picklist.Picklist;
import org.pih.warehouse.shipping.DocumentCommand;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.shipping.ShipmentItem;

class PicklistController {
	
	def requestService
    def inventoryService
	
	//static allowedMethods = [save: "POST", update: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }
	
	
	def list = { 
		
		[picklists : Picklist.list()]
	}
	
	def show = {
		def picklistInstance = Picklist.get(params.id)
		if (!picklistInstance) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'picklistInstance.label', default: 'Picklist'), params.id])}"
			redirect(action: "list")
		}
		else {
			[picklistInstance: picklistInstance]
		}
	}

	
	/*
	def edit = {
		def requestInstance = Request.get(params.id)
		if (!requestInstance) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.id])}"
			redirect(action: "list")
		}
		else {
			return [requestInstance: requestInstance]
		}
	}
	*/
	
	/*
    def update = {
        def requestInstance = Request.get(params.id)
        if (requestInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (requestInstance.version > version) {
                    
                    requestInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'request.label', default: 'Request')] as Object[], "Another user has updated this Request while you were editing")
                    render(view: "edit", model: [requestInstance: requestInstance])
                    return
                }
            }
            requestInstance.properties = params
            if (!requestInstance.hasErrors() && requestInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'request.label', default: 'Request'), requestInstance.id])}"
                redirect(action: "list", id: requestInstance.id)
            }
            else {
                render(view: "edit", model: [requestInstance: requestInstance])
            }
        }
        else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.id])}"
            redirect(action: "list")
        }
    }
	*/

	
	
    
	
	

	

	
	
		
}
