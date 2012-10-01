/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.core;

import org.pih.warehouse.inventory.Transaction;
import org.pih.warehouse.order.Order;
import org.pih.warehouse.request.Request;
import org.pih.warehouse.shipping.Shipment;

class LocationController {
	
	def inventoryService
	
	/**
	 * Controllers for managing other locations (besides warehouses)
	 */
	
	def index = { 
		redirect(action: "list")
	}
	
	def list = {
		def locationInstanceList = []
		def locationInstanceTotal = 0;
		
		params.max = Math.min(params.max ? params.int('max') : 10, 100)
		
		if (params.q) {
			locationInstanceList = Location.findAllByNameLike("%" + params.q + "%", params)
			locationInstanceTotal = Location.countByNameLike("%" + params.q + "%", params);
		}
		else {
			locationInstanceList = Location.list(params)
			locationInstanceTotal = Location.count()
		}

		
		[locationInstanceList: locationInstanceList, locationInstanceTotal: locationInstanceTotal]
	}
	
	def show = { 
		def locationInstance = inventoryService.getLocation(params.id)
		if (!locationInstance) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'location.label', default: 'Location'), params.id])}"
			redirect(action: "list")
		}
		else {
			return [locationInstance: locationInstance]
		}
	}
	
	def edit = {
		def locationInstance = inventoryService.getLocation(params.id)
		if (!locationInstance) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'location.label', default: 'Location'), params.id])}"
			redirect(action: "list")
		}
		else {
			return [locationInstance: locationInstance]
		}
	}
	
	def update = {
			def locationInstance = inventoryService.getLocation(params.id)
			
			if (locationInstance) {
				if (params.version) {
					def version = params.version.toLong()
					if (locationInstance.version > version) {
						
						locationInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'location.label', default: 'Location')] as Object[], "Another user has updated this Location while you were editing")
						render(view: "edit", model: [locationInstance: locationInstance])
						return
					}
				}
				
				locationInstance.properties = params
						
				if (!locationInstance.hasErrors()) {
					inventoryService.saveLocation(locationInstance)
					flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'location.label', default: 'Location'), locationInstance.id])}"
					redirect(action: "list", id: locationInstance.id)
				}
				else {
					render(view: "edit", model: [locationInstance: locationInstance])
				}
			}
			else {
				flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'location.label', default: 'Location'), params.id])}"
				redirect(action: "list")
			}
		}
	
		def delete = {
			def locationInstance = Location.get(params.id)
	        if (locationInstance) {	        	
		          try {
		            locationInstance.delete(flush: true)
		            
		            flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'location.label', default: 'Location'), params.id])}"
		            redirect(action: "list")
			      }
			      catch (org.springframework.dao.DataIntegrityViolationException e) {
		            flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'location.label', default: 'Location'), params.id])}"
		            redirect(action: "edit", id: params.id)
			      }
	        }
	        else {
	            flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'location.label', default: 'Location'), params.id])}"
	            redirect(action: "edit", id: params.id)
	        }
		}
		
		
		/**
		* View warehouse logo
		*/
	   def viewLogo = {
		   def warehouseInstance = Location.get(params.id);
		   if (warehouseInstance) {
			   byte[] logo = warehouseInstance.logo
			   if (logo) {
				   response.outputStream << logo
			   }
		   }
	   }
   
   
	   def uploadLogo = {		   
		   def warehouseInstance = Location.get(params.id);
		   if (warehouseInstance) {
			   def logo = request.getFile("logo");
			   if (!logo?.empty && logo.size < 1024*1000) { // not empty AND less than 1MB
				   warehouseInstance.logo = logo.bytes;
				   if (!warehouseInstance.hasErrors()) {
					   inventoryService.save(warehouseInstance)
					   flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'warehouse.label', default: 'Location'), warehouseInstance.id])}"
				   }
				   else {
					   // there were errors, the photo was not saved
				   }
			   }
			   redirect(action: "show", id: warehouseInstance.id)
		   }
		   else {
			   "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'warehouse.label', default: 'Location'), params.id])}"
		   }
	   }
	
	   
	   def deleteTransaction = { 
		   def transaction = Transaction.get(params.id)
		   transaction.delete();
		   flash.message = "Transaction deleted"
		   redirect(action: "show", id: params.location.id);
	   }
	   def deleteShipment = {
		   def shipment = Shipment.get(params.id)
		   shipment.delete();
		   flash.message = "Shipment deleted"
		   redirect(action: "show", id: params.location.id);
	   }
	   def deleteOrder = {
		   def order = Order.get(params.id)
		   order.delete();
		   flash.message = "Order deleted"
		   redirect(action: "show", id: params.location.id);
	   }
	   def deleteRequest = {
		   def requestInstance = Request.get(params.id)
		   requestInstance.delete();
		   flash.message = "Request deleted"
		   redirect(action: "show", id: params.location.id);
	   }
	   def deleteEvent = {
		   def event = Event.get(params.id)
		   event.delete();
		   flash.message = "Event deleted"
		   redirect(action: "show", id: params.location.id);
	   }
	   def deleteUser = {
		   def user = User.get(params.id)
		   user.delete();
		   flash.message = "User deleted"
		   redirect(action: "show", id: params.location.id);
	   }


}
