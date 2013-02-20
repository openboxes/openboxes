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
import org.pih.warehouse.inventory.InventoryItem;

import grails.converters.JSON
import grails.validation.ValidationException;

import org.pih.warehouse.picklist.Picklist
import org.pih.warehouse.picklist.PicklistItem;



class RequisitionController {

    def requisitionService
    def inventoryService
	def productService

    static allowedMethods = [save: "POST", update: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        def requisitions = Requisition.findAllByDestination(session.warehouse)
        render(view:"list", model:[requisitions: requisitions])
    }

    def create = {
        println params
		def requisition = new Requisition(status: RequisitionStatus.CREATED)
        requisition.type = params.type as RequisitionType
        def locations
        if (requisition.isWardRequisition()) {
            locations = getWardsPharmacies()
        } else {
            locations = getDepots()
        }
		
		if (!locations) { 
			requisition.errors.rejectValue("origin", "requisition.origin.error")
		}
		
        render(view:"edit", model:[requisition:requisition, locations: locations])
    }

	def edit = {
		def requisition = Requisition.get(params.id)
		if(requisition) {
			def locations
			if (requisition.isWardRequisition()) {
				locations = getWardsPharmacies()
			} else {
				locations = getDepots()
			}
			
			if (!locations) {
				requisition.errors.rejectValue("origin", "requisition.origin.error")
			}
	
			
			return [requisition: requisition, locations: locations];
		}else {
			response.sendError(404)
		}
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
        log.info(jsonResponse as JSON)
        render jsonResponse as JSON
    }

	def confirm = {
		def requisition = Requisition.get(params?.id)
		if (requisition) {
			def currentInventory = Location.get(session.warehouse.id).inventory
			def picklist = Picklist.findByRequisition(requisition)
			def productInventoryItemsMap = [:]
			def productInventoryItems = inventoryService.getInventoryItemsWithQuantity(requisition.requisitionItems?.collect{ it.product}, currentInventory)
			productInventoryItems.keySet().each { product ->
				productInventoryItemsMap[product.id] = productInventoryItems[product].collect{it.toJson()}
			}
		}
		
		[requisition: requisition]
	}
	
	def substitute = { 
		println "substitute " + params
		def requisitionItem = RequisitionItem.get(params.requisitionItem.id)
		def requisition = Requisition.get(params.id)
		def picklist = Picklist.findByRequisition(requisition)
		
		def inventoryItem = InventoryItem.get(params.inventoryItem.id)		
		if (!inventoryItem) { 
			flash.message = "Could not find inventory item with lot number '${params.lotNumber}'" 
		}
		else { 
			def picklistItem = new PicklistItem()
			picklistItem.inventoryItem = inventoryItem
			picklistItem.requisitionItem = requisitionItem
			picklistItem.quantity = Integer.valueOf(params.quantity)
			picklist.addToPicklistItems(picklistItem);
			picklist.save(flush:true)
		}

		chain(action: "pick", id: requisition.id, params: ['requisitionItem.id':requisitionItem.id])
	}

	def pick = {
		println "Pick " + params
		
		def requisition = Requisition.get(params?.id)
		if (requisition) {
			
			requisition.status = RequisitionStatus.PICKING
			requisition.save(flush:true)
						
			def currentInventory = Location.get(session.warehouse.id).inventory
			def picklist = Picklist.findByRequisition(requisition)

			if (!picklist) {
				picklist = new Picklist();
				picklist.requisition = requisition
				if (!picklist.save(flush:true)) {
					throw new ValidationException("Unable to create new picklist", picklist.errors)
				}
			}
			
			def productInventoryItemsMap = [:]
			def productInventoryItems = inventoryService.getInventoryItemsWithQuantity(requisition.requisitionItems?.collect{ it.product}, currentInventory)
			productInventoryItems.keySet().each { product ->
				productInventoryItemsMap[product.id] = productInventoryItems[product]//.collect{it.toJson()}
			}

			def location = Location.get(session.warehouse.id)
			def inventoryLevelMap = [:]
			productInventoryItems.keySet().each { product ->				
				inventoryLevelMap[product] = product.getInventoryLevel(location.id)
			}
			
			
			def requisitionItem = RequisitionItem.get(params?.requisitionItem?.id)
			if (!requisitionItem) {
				requisitionItem = requisition.requisitionItems.first()
			}
			
			def similarProducts = productService.findSimilarProducts(requisitionItem?.product)
			
			def similarProductInventoryItems = inventoryService.getInventoryItemsWithQuantity(similarProducts, currentInventory)
			
			//String jsonString = [requisition: requisition.toJson(), productInventoryItemsMap: productInventoryItemsMap, picklist: picklist.toJson()] as JSON
			//return [data: jsonString, requisitionId: requisition.id, requisition:requisition]
			[requisition:requisition, productInventoryItemsMap: productInventoryItemsMap, 
				picklist: picklist, inventoryLevelMap: inventoryLevelMap, 
				selectedRequisitionItem: requisitionItem, similarProducts: similarProducts, similarProductInventoryItems: similarProductInventoryItems]
			
		}
		else { 
			response.sendError(404)
		}
	}

	def picked = {		
		def requisition = Requisition.get(params.id)
		if (requisition) {
			requisition.status = RequisitionStatus.PICKED
			requisition.save(flush:true)
		}
		redirect(controller: "requisition", action: "confirm", id: requisition.id)				
	}
		
		
    def process = {
        def requisition = Requisition.get(params?.id)
        if (requisition) {
            def currentInventory = Location.get(session.warehouse.id).inventory
            def picklist = Picklist.findByRequisition(requisition)?: new Picklist()
            def productInventoryItemsMap = [:]
            def productInventoryItems = inventoryService.getInventoryItemsWithQuantity(requisition.requisitionItems?.collect{ it.product}, currentInventory)
            productInventoryItems.keySet().each { product ->
                productInventoryItemsMap[product.id] = productInventoryItems[product].collect{it.toJson()}
            }
			
			
			
            String jsonString = [requisition: requisition.toJson(), productInventoryItemsMap: productInventoryItemsMap, picklist: picklist.toJson()] as JSON
            return [data: jsonString, requisitionId: requisition.id, requisition:requisition]
        } else{
            response.sendError(404)
        }
    }

	def transfer = { 
		def requisition = Requisition.get(params.id)
		def picklist = Picklist.findByRequisition(requisition)
		
		//requisitionService.transferStock(requisition)
		
		[requisition:requisition, picklist:picklist]
	}
	
	def complete = { 
		def transaction 
		try { 
			def requisition = Requisition.get(params.id)
			def picklist = Picklist.findByRequisition(requisition)
			transaction = requisitionService.completeInventoryTransfer(requisition, params.comments)
		} 
		catch (ValidationException e) { 
			//flash.message = e.message 
			def requisition = Requisition.read(params.id)
			def picklist = Picklist.findByRequisition(requisition)
			requisition.errors = e.errors
			render(view: "transfer", model:[requisition:requisition,picklist:picklist])
			return
		}
		flash.message = "Successfully saved outbound transaction with ID " + transaction.transactionNumber		
		redirect(action: "show", id: params.id)
	}
	
    def cancel = {
        def requisition = Requisition.get(params?.id)
        if (requisition) {
            requisitionService.cancelRequisition(requisition)
            flash.message = "${warehouse.message(code: 'default.cancelled.message', args: [warehouse.message(code: 'requisition.label', default: 'Requisition'), params.id])}"
        }
        redirect(action: "list")
    }

	def uncancel = {
		def requisition = Requisition.get(params?.id)
		if (requisition) {
			requisitionService.uncancelRequisition(requisition)
			flash.message = "${warehouse.message(code: 'default.uncancelled.message', args: [warehouse.message(code: 'requisition.label', default: 'Requisition'), params.id])}"
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
			println requisition.status 
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
		println "print draft " + params 
        def requisition = Requisition.get(params.id)
        def picklist = Picklist.findByRequisition(requisition)
        def location = Location.get(session.warehouse.id)
        //render (view: "printDraft", model:[requisition:requisition, picklist: picklist, location:location])
		[requisition:requisition, picklist: picklist, location:location]
    }
	
	def printDeliveryNote = {
		def requisition = Requisition.get(params.id)
		def picklist = Picklist.findByRequisition(requisition)
		def location = Location.get(session.warehouse.id)
		[requisition:requisition, picklist: picklist, location:location]
	}

	def addToPicklistItems = { 
		println "Add to picklist items " + params
		def requisition = Requisition.get(params.id)
		def requisitionItem = Requisition.get(params.requisitionItem.id)
		def picklist = Picklist.findByRequisition(requisition)

		
		if (!picklist) {
			picklist = new Picklist();
			picklist.requisition = requisition
			if (!picklist.save(flush:true)) {
				throw new ValidationException("Unable to create new picklist", picklist.errors)
			}
		}
		def tmpPicklist = new Picklist()
		bindData(tmpPicklist, params)
		//bindData(picklistItems, params.picklistItems)
		tmpPicklist.picklistItems.each { picklistItem ->
			if (picklistItem.quantity > 0) { 
				println "Save to picklist " + picklistItem.id + " " + picklistItem.inventoryItem + " " + picklistItem.quantity
				def existingPicklistItem
				if (picklistItem.id) { 
					existingPicklistItem = PicklistItem.get(picklistItem.id)
					existingPicklistItem.quantity = picklistItem.quantity
					existingPicklistItem.save(flush:true)
				} 
				else { 					
					picklist.addToPicklistItems(picklistItem)
					picklistItem.save(flush:true)
					picklist.save(flush:true)
					println "saved picklist " + picklist.id + picklist.errors
					println picklist.picklistItems
				}
				println picklistItem.id + " " + picklistItem.inventoryItem + " " + picklistItem.quantity
			}
		}
		
		//def picklistItem = new PicklistItem()
		//requisitionItem = requisitionItem
		//inventoryItem = 
		//quantity
		
		chain(action: "pick", id: requisition.id)
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
