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

import org.pih.warehouse.core.Comment;
import org.pih.warehouse.core.Document;
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import grails.converters.JSON
import org.pih.warehouse.product.Product
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
        def requisition = new Requisition()
        render(view:"edit", model:[requisition:requisition.toJson() as JSON, depots: getDepots()])
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
            def currentInventory = Location.get(session.warehouse.id).inventory
            def picklist = Picklist.findByRequisition(requisition)?: new Picklist()
            def productInventoryItemsMap = [:]
            def productInventoryItems = inventoryService.getInventoryItemsWithQuantity(requisition.requisitionItems?.collect{ it.product}, currentInventory)

            productInventoryItems.keySet().each { product ->
                productInventoryItemsMap[product.id] = productInventoryItems[product].collect{it.toJson()}
            }

            String jsonString = [requisition: requisition.toJson(), productInventoryItemsMap: productInventoryItemsMap, picklist: picklist.toJson()] as JSON
            return [data: jsonString]
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
        def requestInstance = Requisition.get(params.id)
        if (!requestInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.id])}"
            redirect(action: "list")
        }
        else {
            [requestInstance: requestInstance]
        }
    }

    def place = {
        def requestInstance = Requisition.get(params.id)
        if (requestInstance) {

            if (requestInstance?.requisitionItems?.size() > 0) {
                requestInstance.status = RequisitionStatus.PLACED;
                if (!requestInstance.hasErrors() && requestInstance.save(flush: true)) {
                    flash.message = "${warehouse.message(code: 'request.placedWithLocation.message', args: [requestInstance?.description, requestInstance?.origin?.name])}"
                    redirect(action: "show", id: requestInstance.id)
                }
                else {
                    flash.message = "${warehouse.message(code: 'request.errorPlacingRequest.message')}"
                    render(view: "show", model: [requestInstance: requestInstance])
                }
            }
            else {
                flash.message = "${warehouse.message(code: 'request.mustContainAtLeastOneItem.message')}"
                redirect(action: "show", id: requestInstance.id)
            }
        }
        else {
            redirect("show", id: requestInstance?.id)

        }
    }

    def update = {
        def requestInstance = Requisition.get(params.id)
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

    def addComment = {
        def requestInstance = Requisition.get(params?.id)
        if (!requestInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [requestInstance: requestInstance, commentInstance: new Comment()]
        }
    }

    def editComment = {
        def requestInstance = Requisition.get(params?.requisition?.id)
        if (!requestInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.id])}"
            redirect(action: "list")
        }
        else {
            def commentInstance = Comment.get(params?.id)
            if (!commentInstance) {
                flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'comment.label', default: 'Comment'), commentInstance.id])}"
                redirect(action: "show", id: requestInstance?.id)
            }
            render(view: "addComment", model: [requestInstance: requestInstance, commentInstance: commentInstance])
        }
    }

    def deleteComment = {
        def requestInstance = Requisition.get(params.requisition.id)
        if (!requestInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.requisition.id])}"
            redirect(action: "list")
        }
        else {
            def commentInstance = Comment.get(params?.id)
            if (!commentInstance) {
                flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'comment.label', default: 'Comment'), params.id])}"
                redirect(action: "show", id: requestInstance?.id)
            }
            else {
                requestInstance.removeFromComments(commentInstance);
                if (!requestInstance.hasErrors() && requestInstance.save(flush: true)) {
                    flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'request.label', default: 'Request'), requestInstance.id])}"
                    redirect(action: "show", id: requestInstance.id)
                }
                else {
                    render(view: "show", model: [requestInstance: requestInstance])
                }
            }
        }
    }

    def saveComment = {
        log.info(params)

        def requestInstance = Requisition.get(params?.requisition?.id)
        if (requestInstance) {
            def commentInstance = Comment.get(params?.id)
            if (commentInstance) {
                commentInstance.properties = params
                if (!commentInstance.hasErrors() && commentInstance.save(flush: true)) {
                    flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'comment.label', default: 'Comment'), commentInstance.id])}"
                    redirect(action: "show", id: requestInstance.id)
                }
                else {
                    render(view: "addComment", model: [requestInstance: requestInstance, commentInstance: commentInstance])
                }
            }
            else {
                commentInstance = new Comment(params)
                requestInstance.addToComments(commentInstance);
                if (!requestInstance.hasErrors() && requestInstance.save(flush: true)) {
                    flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'request.label', default: 'Request'), requestInstance.id])}"
                    redirect(action: "show", id: requestInstance.id)
                }
                else {
                    render(view: "addComment", model: [requestInstance: requestInstance, commentInstance: commentInstance])
                }
            }
        }
        else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.id])}"
            redirect(action: "list")
        }

    }

    def addDocument = {
        def requestInstance = Requisition.get(params.id)
        if (!requestInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [requestInstance: requestInstance]
        }
    }

    def editDocument = {
        def requestInstance = Requisition.get(params?.requisition?.id)
        if (!requestInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.id])}"
            redirect(action: "list")
        }
        else {
            def documentInstance = Document.get(params?.id)
            if (!documentInstance) {
                flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'document.label', default: 'Document'), documentInstance.id])}"
                redirect(action: "show", id: requestInstance?.id)
            }
            render(view: "addDocument", model: [requestInstance: requestInstance, documentInstance: documentInstance])
        }
    }

    def deleteDocument = {
        def requestInstance = Requisition.get(params.requisition.id)
        if (!requestInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.requisition.id])}"
            redirect(action: "list")
        }
        else {
            def documentInstance = Document.get(params?.id)
            if (!documentInstance) {
                flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'comment.label', default: 'Comment'), params.id])}"
                redirect(action: "show", id: requestInstance?.id)
            }
            else {
                requestInstance.removeFromDocuments(documentInstance);
                if (!requestInstance.hasErrors() && requestInstance.save(flush: true)) {
                    flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'request.label', default: 'Request'), requestInstance.id])}"
                    redirect(action: "show", id: requestInstance.id)
                }
                else {
                    render(view: "show", model: [requestInstance: requestInstance])
                }
            }
        }
    }

    def receive = {
        def requestCommand = requisitionService.getRequest(params.id as int, session.user.id as int)
        if (!requestCommand.requisition) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [requestCommand: requestCommand]
        }
    }

    def saveRequestShipment = { RequisitionCommand command ->
        bindData(command, params)
        def requestInstance = Requisition.get(params?.requisition?.id);
        command.requisition = requestInstance;

        requisitionService.saveRequestShipment(command)

        // If the shipment was saved, let's redirect back to the request received page
        if (!command?.shipment?.hasErrors() && command?.shipment?.id) {
            redirect(controller: "requisition", action: "receive", id: params?.requisition?.id)
        }

        // Otherwise, we want to display the errors, so we need to render the page.
        render(view: "receive", model: [requestCommand: command])
    }

    def addRequestShipment = {
        def requestCommand = requisitionService.getRequest(params.id as int, session.user.id as int)
        int index = Integer.valueOf(params?.index)
        def requestItemToCopy = requestCommand?.requestItems[index]
        if (requestItemToCopy) {
            def requestItemToAdd = new RequisitionItemCommand();
            requestItemToAdd.setPrimary(false)
            requestItemToAdd.setType(requestItemToCopy.type)
            requestItemToAdd.setDescription(requestItemToCopy.description)
            requestItemToAdd.setLotNumber(requestItemToCopy.lotNumber);
            requestItemToAdd.setRequisitionItem(requestItemToCopy.requisitionItem)
            requestItemToAdd.setProductReceived(requestItemToCopy.productReceived)
            requestItemToAdd.setQuantityRequested(requestItemToCopy.quantityRequested)

            requestCommand?.requestItems?.add(index + 1, requestItemToAdd);
        }
        render(view: "receive", model: [requestCommand: requestCommand])
        //redirect(action: "receive")
    }

    def removeRequestShipment = {
        log.info("Remove request shipment " + params)
        def requestCommand = session.requestCommand
        int index = Integer.valueOf(params?.index)
        requestCommand.requestItems.remove(index)

        //render(view: "receive", model: [requestCommand: requestCommand])
        redirect(action: "receive")
    }

    def showPicklist = {
        def requestInstance = Requisition.get(params.id)
        if (!requestInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [requestInstance: requestInstance]
        }
    }


    def fulfill = {
        def requestInstance = Requisition.get(params.id)
        if (!requestInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [requestInstance: requestInstance]
        }
    }


    def fulfillItem = {
        log.info "fulfillItem " + params

        def inventoryItems = [:]
        def requestItem = RequisitionItem.get(params.id)
        if (!requestItem) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.id])}"
        }
        else {
            def warehouse = Location.get(session.warehouse.id);
            if (warehouse.inventory) {
                inventoryItems =
                    inventoryService.getQuantityByInventoryAndProduct(warehouse.inventory, requestItem.product);
            }
            else {
                throw new RuntimeException("Location does not have an associated inventory")
            }
        }
        return [requestItem: requestItem, inventoryItems: inventoryItems]
    }

//
//	def addRequestItemToShipment = {
//
//		def requestInstance = Request.get(params?.id)
//		def requestItem = RequestItem.get(params?.requestItem?.id)
//		def shipmentInstance = Shipment.get(params?.shipment?.id)
//
//		if (requestItem) {
//			def shipmentItem = new ShipmentItem(requestItem.properties)
//			shipmentInstance.addToShipmentItems(shipmentItem);
//			if (!shipmentInstance.hasErrors() && shipmentInstance?.save(flush:true)) {
//
//				def requestShipment = RequestShipment.link(requestItem, shipmentItem);
//				/*
//				if (!requestShipment.hasErrors() && requestShipment.save(flush:true)) {
//					flash.message = "success"
//				}
//				else {
//					flash.message = "request shipment error(s)"
//					render(view: "fulfill", model: [requestShipment: requestShipment, requestItemInstance: requestItem, shipmentInstance: shipmentInstance])
//					return;
//				}*/
//			}
//			else {
//				flash.message = "${warehouse.message(code: 'request.shipmentItemsError.label')}"
//				render(view: "fulfill", model: [requestItemInstance: requestItem, shipmentInstance: shipmentInstance])
//				return;
//			}
//		}
//
//		redirect(action: "fulfill", id: requestInstance?.id)
//
//	}


    def fulfillPost = {
        def requestInstance = Requisition.get(params.id)
        if (requestInstance) {

            if (requestInstance?.requisitionItems?.size() > 0) {
                requestInstance.status = RequisitionStatus.FULFILLED;
                if (!requestInstance.hasErrors() && requestInstance.save(flush: true)) {
                    flash.message = "${warehouse.message(code: 'request.placedWithLocation.message', args: [requestInstance?.description, requestInstance?.origin?.name])}"
                    redirect(action: "show", id: requestInstance.id)
                }
                else {
                    flash.message = "${warehouse.message(code: 'request.errorPlacingRequest.message')}"
                    render(view: "show", model: [requestInstance: requestInstance])
                }
            }
            else {
                flash.message = "${warehouse.message(code: 'request.mustContainAtLeastOneItem.message')}"
                redirect(action: "show", id: requestInstance.id)
            }
        }
        else {
            redirect("show", id: requestInstance?.id)

        }

    }


}
