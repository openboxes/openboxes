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

import grails.converters.JSON
import grails.validation.ValidationException
import org.apache.commons.collections.FactoryUtils
import org.apache.commons.collections.list.LazyList
import org.hibernate.HibernateException
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.picklist.Picklist
import org.pih.warehouse.picklist.PicklistItem
import org.springframework.orm.hibernate3.HibernateSystemException

class RequisitionController {

    def requisitionService
    def inventoryService
	def productService

    static allowedMethods = [save: "POST", update: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {

        def requisition = new Requisition(params)
        requisition.destination = session?.warehouse
        //requisition.origin = Location.get(params?.origin?.id)
        //createdBy = User.get(params?.createdBy?.id)
        //println requisition.status
        def requisitions = []
		//requisitions = requisitionService.getRequisitions(session?.warehouse, origin, createdBy, requisition?.type, requisition?.status, requisition?.commodityClass, params.q, params)
        requisitions = requisitionService.getRequisitions(requisition, params)
        def requisitionsMap = [:]

        // Used to display the counts
        def requisitionsLocal = Requisition.findAllByDestination(session.warehouse)
        println "requisitionsLocal: " + requisitionsLocal.size()

        // Hack to get requisitions that are related to me
        def requisitionsRelatedToMe = requisitionsLocal.findAll { it?.updatedBy?.id == session?.user?.id || it?.createdBy?.id == session?.user?.id || it?.requestedBy?.id == session?.user?.id }
        println "requisitionsRelatedToMe: " + requisitionsRelatedToMe.size()

        requisitionsLocal.groupBy { it.status }.each { k, v ->
            requisitionsMap[k] = v.size()?:0
        }
        requisitionsMap["relatedToMe"] = requisitionsRelatedToMe.size()?:0
        //requisitionsMap["updatedByMe"] = requisitionsLocal.findAll { it.updatedBy == session.user }.size()?:0
        //requisitionsMap["submittedByMe"] = requisitionsLocal.findAll { it.requestedBy == session.user }.size()?:0

        //requisitions = requisitions.sort()
        render(view:"list", model:[requisitions: requisitions, requisitionsMap:requisitionsMap])
    }
	
	def listStock = {
        def requisitions = []
        def destination = Location.get(session.warehouse.id)
		//requisitions = Requisition.findAllByIsTemplate(true)
        requisitions = Requisition.findAllByIsTemplateAndDestination(true, destination)
		render(view:"listStock", model:[requisitions: requisitions])		
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

    def chooseTemplate = {
        println params
        def requisition = new Requisition(status: RequisitionStatus.CREATED)
        requisition.type = params.type as RequisitionType ?: RequisitionType.WARD_STOCK
        //requisition.name = getName(requisition)
        def templates = Requisition.findAllByIsPublishedAndIsTemplate(true,true)

        render(view:"chooseTemplate", model:[requisition:requisition, templates:templates])
    }

    def createStockFromTemplate = {
        def requisition = new Requisition()
        def requisitionTemplate = Requisition.get(params.id)
        if (requisitionTemplate) {

            requisition.type = requisitionTemplate.type
            requisition.origin = requisitionTemplate.origin
            requisition.destination = requisitionTemplate.destination
            requisition.commodityClass = requisitionTemplate.commodityClass
            requisition.createdBy = User.get(session.user.id)
            requisition.dateRequested = new Date()

            requisitionTemplate.requisitionItems.each {
                def requisitionItem = new RequisitionItem()
                requisitionItem.inventoryItem = it.inventoryItem
                requisitionItem.quantity = it.quantity
                requisitionItem.product = it.product
                requisitionItem.productPackage = it.productPackage
                requisition.addToRequisitionItems(requisitionItem)
            }
        }
        else {
            flash.message = "Could not find requisition template"
        }
        render(view:"createStock", model:[requisition:requisition])


    }

    def createNonStock = {
        println params
        def requisition = new Requisition(status: RequisitionStatus.CREATED)
        requisition.requestNumber = requisitionService.getIdentifierService().generateRequisitionIdentifier()
        requisition.type = params.type as RequisitionType ?: RequisitionType.WARD_NON_STOCK
        //requisition.name = getName(requisition)
        render(view:"createNonStock", model:[requisition:requisition])
    }

    def createAdhoc = {
        println params
        def requisition = new Requisition(status: RequisitionStatus.CREATED)
        requisition.requestNumber = requisitionService.getIdentifierService().generateRequisitionIdentifier()
        requisition.type = params.type as RequisitionType ?: RequisitionType.WARD_ADHOC
        //requisition.name = getName(requisition)
        render(view:"createNonStock", model:[requisition:requisition])
    }

    def createDepot = {
        println params
        def requisition = new Requisition(status: RequisitionStatus.CREATED)
        requisition.requestNumber = requisitionService.getIdentifierService().generateRequisitionIdentifier()
        requisition.type = params.type as RequisitionType ?: RequisitionType.DEPOT_TO_DEPOT
        //requisition.name = getName(requisition)
        render(view:"createNonStock", model:[requisition:requisition])
    }


    def saveNonStock = {
        def requisition = new Requisition(params)
        // Need to handle commodity class since it is an enum
        if (params.commodityClass) {
            requisition.commodityClass = params.commodityClass as CommodityClass
        }
        requisition.name = getName(requisition)
        requisition = requisitionService.saveRequisition(requisition)
        if (!requisition.hasErrors()) {
            redirect(controller: "requisition", action: "edit", id: requisition?.id)
        }
        else {
            render(view: "createNonStock", model: [requisition:requisition])
        }
    }

    def saveStock = {
        def requisition = new Requisition(params)


        // Need to handle commodity class since it is an enum
        if (params.commodityClass) {
            requisition.commodityClass = params.commodityClass as CommodityClass
        }
        requisition.name = getName(requisition)

        if (requisitionService.saveRequisition(requisition)) {
            redirect(controller: "requisition", action: "edit", id: requisition?.id)
        }
        else {
            render(view: "createNonStock", model: [requisition:requisition])
        }

    }


	def edit = {
		def requisition = Requisition.get(params.id)
		if(requisition) {

            if (requisition.status < RequisitionStatus.EDITING) {
                requisition.status = RequisitionStatus.EDITING
                requisition.save(flush:true)
            }


            println "Requisition json: " + requisition.toJson()

            return [requisition: requisition];



		}else {
			response.sendError(404)
		}
	}
	
	def editHeader = {
		def requisition = Requisition.get(params.id)
        [requisition: requisition];
	}
	



	def saveHeader = { 
	//	def success = true
		
		def requisition = Requisition.get(params.id)
		
		if(requisition) {
			requisition.properties = params
            requisition.name = getName(requisition)
		    requisition = requisitionService.saveRequisition(requisition)

			if (requisition.hasErrors()) {
				render(view:"editHeader", model:[requisition:requisition])
			    return;
            }
		}
		
		redirect(action: "edit", id: requisition?.id)
		
		
	}
	
	
	def review = {
		def requisition = Requisition.get(params.id)
        if (requisition.status < RequisitionStatus.VERIFYING) {
            requisition.status = RequisitionStatus.VERIFYING
            requisition.save(flush:true)
        }

		
		if (!requisition) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.id])}"
			redirect(action: "list")
		}
		else {

            def location = Location.get(session.warehouse.id)
            def quantityAvailableToPromiseMap = [:]
            def quantityOnHandMap = [:]


            requisition?.requisitionItems?.each { requisitionItem ->
                quantityOnHandMap[requisitionItem?.product?.id] =
                    inventoryService.getQuantityOnHand(location, requisitionItem?.product)
                quantityAvailableToPromiseMap[requisitionItem?.product?.id] =
                    inventoryService.getQuantityAvailableToPromise(location, requisitionItem?.product)
            }

            def requisitionItem = RequisitionItem.get(params?.requisitionItem?.id)
            def quantityOnHand = (requisitionItem)?inventoryService.getQuantityOnHand(location, requisitionItem?.product):0
            def quantityOutgoing = (requisitionItem)?inventoryService.getQuantityToShip(location, requisitionItem?.product):0
     //       def quantityAvailableToPromise = (quantityOnHand - quantityOutgoing)?:0;

			println "Requisition Status: " + requisition.id + " [" + requisition.status + "]"
			return [requisition: requisition,
                    quantityOnHandMap:quantityOnHandMap,
                    quantityAvailableToPromiseMap:quantityAvailableToPromiseMap,
                    selectedRequisitionItem: requisitionItem,
                    quantityOnHand: quantityOnHand,
                    quantityOutgoing:quantityOutgoing]
		}
    }


    def save = {
        def jsonResponse = []
        def requisition = new Requisition()
		try {
            def jsonRequest = request.JSON
            println "Save requisition: " + jsonRequest
            requisition = requisitionService.saveRequisition(jsonRequest, Location.get(session?.warehouse?.id))
            if (!requisition.hasErrors()) {
                jsonResponse = [success: true, data: requisition.toJson()]
            }
            else {
                jsonResponse = [success: false, errors: requisition.errors]
            }
            log.info(jsonResponse as JSON)

        } catch (HibernateException e) {
            println "hibernate exception " + e.message
            jsonResponse = [success: false, errors: requisition?.errors, message: e.message]

        } catch (HibernateSystemException e) {
            println "hibernate system exception " + e.message
            jsonResponse = [success: false, errors: requisition?.errors, message: e.message]

        } catch (Exception e) {
            log.error("Error saving requisition: " + e.message, e)
            log.info ("Errors: " + requisition.errors)
            log.info("Message: " + e.message)
            jsonResponse = [success: false, errors: requisition?.errors, message: e.message]
        }
        render jsonResponse as JSON
    }


	def confirm = {
		def requisition = Requisition.get(params?.id)
		if (requisition) {

            if (requisition.status < RequisitionStatus.CHECKING) {
                requisition.status = RequisitionStatus.CHECKING
                requisition.save(flush:true)
            }

			def currentInventory = Location.get(session.warehouse.id).inventory
	//		def picklist = Picklist.findByRequisition(requisition)
			def productInventoryItemsMap = [:]
			def productInventoryItems = inventoryService.getInventoryItemsWithQuantity(requisition.requisitionItems?.collect{ it.product}, currentInventory)
			productInventoryItems.keySet().each { product ->
				productInventoryItemsMap[product.id] = productInventoryItems[product].collect{it.toJson()}
			}
		}
		[requisition: requisition]
	}

    def saveDetails = {
        def requisition = Requisition.get(params?.id)
        if (requisition) {
            requisition.properties = params
            requisition.save(flush: true)

        }
        redirect(action: params.redirectAction ?: "show", id: requisition.id)
    }




	def pick = {
		println "Pick " + params
		
		def requisition = Requisition.get(params?.id)
		if (requisition) {

            if (requisition.status < RequisitionStatus.PICKING) {
    			requisition.status = RequisitionStatus.PICKING

                // Approve all pending requisition items
                requisition.requisitionItems.each { requisitionItem ->
                    if (requisitionItem.isPending()) {
                        requisitionItem.approveQuantity()
                    }
                }

                requisition.save(flush:true)
            }

						
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
			//if (!requisitionItem) {
				//requisitionItem = requisition.requisitionItems.first()
			//}
			
			//def similarProducts = productService.findSimilarProducts(requisitionItem?.product)
			//def similarProductInventoryItems = inventoryService.getInventoryItemsWithQuantity(similarProducts, currentInventory)
			
			//String jsonString = [requisition: requisition.toJson(), productInventoryItemsMap: productInventoryItemsMap, picklist: picklist.toJson()] as JSON
			//return [data: jsonString, requisitionId: requisition.id, requisition:requisition]
			[requisition:requisition, productInventoryItemsMap: productInventoryItemsMap, 
				picklist: picklist, inventoryLevelMap: inventoryLevelMap, 
				selectedRequisitionItem: requisitionItem]
			
		}
		else { 
			response.sendError(404)
		}
	}
	
	
	def pickNextItem = {
        def nextItem
		def requisition = Requisition.get(params?.id)
		def requisitionItem = RequisitionItem.get(params?.requisitionItem?.id)
        if (!requisitionItem) {
            nextItem = requisition?.requisitionItems?.first()
        }
        else {
		    def currentIndex = requisition?.requisitionItems?.findIndexOf { it == requisitionItem }
		    nextItem = requisition?.requisitionItems[currentIndex+1]?:requisition?.requisitionItems?.first()
        }
		redirect(action: "pick", id: requisition?.id, 
				params: ["requisitionItem.id":nextItem?.id])
	}
	
	def pickPreviousItem = { 
		def requisition = Requisition.get(params?.id)
		def requisitionItem = RequisitionItem.get(params.requisitionItem.id)
        if (!requisitionItem) {
            requisitionItem = requisition?.requisitionItems?.first()
        }
		def lastItem = requisition?.requisitionItems?.size()-1
		def currentIndex = requisition.requisitionItems.findIndexOf { it == requisitionItem }
		def previousItem = requisition?.requisitionItems[currentIndex-1]?:requisition?.requisitionItems[lastItem]
		
		redirect(action: "pick", id: requisition?.id, 
				params: ["requisitionItem.id":previousItem?.id])
		
	}
	

	def picked = {
		def requisition = Requisition.get(params.id)
		if (requisition) {
			requisition.status = RequisitionStatus.PENDING
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
			// def picklist = Picklist.findByRequisition(requisition)
			transaction = requisitionService.issueRequisition(requisition, params.comments)
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

    def rollback = {
        def requisition = Requisition.get(params?.id)
        if (requisition) {
            requisitionService.rollbackRequisition(requisition)
            flash.message = "${warehouse.message(code: 'default.rollback.message', args: [warehouse.message(code: 'requisition.label', default: 'Requisition'), params.id])}"

        }
        flash.message = "Successfully rolled back requisition " + requisition.requestNumber
        redirect(action: "show", id: params.id)

    }


	def undoCancel = {
		def requisition = Requisition.get(params?.id)
		if (requisition) {
			requisitionService.undoCancelRequisition(requisition)
			flash.message = "${warehouse.message(code: 'default.undone.message', args: [warehouse.message(code: 'requisition.label', default: 'Requisition'), params.id])}"
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
		println "print draft " + params 
        def requisition = Requisition.get(params.id)
        def picklist = Picklist.findByRequisition(requisition)
        def location = Location.get(session.warehouse.id)
        //render (view: "printDraft", model:[requisition:requisition, picklist: picklist, location:location])
		[requisition:requisition, picklist: picklist, location:location]
    }


	def addToPicklistItems = { AddToPicklistItemsCommand command ->
		println "Add to picklist items " + params
		def requisition = Requisition.get(params.id)
	//	def requisitionItem = Requisition.get(params.requisitionItem.id)
        println "Requisition " + command?.requisition
		def picklist = Picklist.findByRequisition(command.requisition)
        if (!picklist) {
            picklist = new Picklist();
            picklist.requisition = command.requisition
            if (!picklist.save(flush:true)) {
                throw new ValidationException("Unable to create new picklist", picklist.errors)
            }
        }
        command?.picklistItems.each { picklistItem ->
            def existingPicklistItem  = PicklistItem.get(picklistItem.id)
            if (picklistItem.quantity > 0) {
                if (existingPicklistItem) {
                    existingPicklistItem.quantity = picklistItem.quantity
                    existingPicklistItem.save(flush:true)
                }
                else {
                    println "Adding new item to picklist " + picklistItem?.id + " inventoryItem.id=" + picklistItem?.inventoryItem?.id + " qty=" + picklistItem?.quantity
                    picklist.addToPicklistItems(picklistItem)
                    //picklistItem.save(flush:true)
                    picklist.save(flush:true)
                }
            }
            // Otherwise, if quantity <= 0 then we want to remove this item
            else {
                if (existingPicklistItem) {
                    picklist.removeFromPicklistItems(existingPicklistItem)
                }
            }
        }
		redirect(action:  "pick", id:  command?.requisition?.id)
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

    /**
     * Generate the name of the requisition.
     *
     * @param requisition
     * @return
     */
    def getName(requisition) {
        def commodityClass = (requisition.commodityClass) ? "${warehouse.message(code:'enum.CommodityClass.' + requisition.commodityClass)}" : null
        def requisitionType = (requisition.type) ? "${warehouse.message(code: 'enum.RequisitionType.' + requisition.type)}" : null
        def requisitionName =
            [
                requisitionType,
                requisition.origin,
                requisition.recipientProgram,
                commodityClass,
                "${g:formatDate(date: requisition.dateRequested, format: 'MMM dd yyyy')}"
            ]

        return requisitionName.findAll{ it }.join(" - ")
    }


	
}


class AddToPicklistItemsCommand {
    Requisition requisition
    RequisitionItem requisitionItem
    def picklistItems = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(PicklistItem.class));

    static constraints = {
        requisition(nullable:false)
        requisitionItem(nullable:false)
        picklistItems(nullable:true)

    }


}
