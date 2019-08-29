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

import grails.validation.ValidationException
import org.pih.warehouse.core.*
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionException
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException
import org.springframework.web.multipart.MultipartFile

class CreateShipmentWorkflowController {

    MailService mailService
    def reportService
    def shipmentService
    def inventoryService
    def userService

    def index = {
        log.info "CreateShipmentWorkflowController.index() -> " + params
        flash.type = params.type
        redirect(action: 'createShipment')
    }

    def createShipmentFlow = {

        start {
            action {
                log.info("Starting shipment workflow " + params)
                // create a new shipment instance if we don't have one already
                if (!flow.shipmentInstance) {
                    flow.shipmentInstance = shipmentService.getShipmentInstance(params.id)
                    flow.shipmentWorkflow = shipmentService.getShipmentWorkflow(flow.shipmentInstance)
                }
                if (params.skipTo) {
                    if (params.skipTo == 'Packing')
                        return enterContainerDetails()
                    else if (params.skipTo == 'Details')
                        return enterShipmentDetails()
                    else if (params.skipTo == 'Tracking')
                        return enterTrackingDetails()
                    else if (params.skipTo == 'Picking')
                        return pickShipmentItems()
                    else if (params.skipTo == 'Sending')
                        return sendShipment()
                }
                return success()
            }
            on("success").to("enterShipmentDetails")
            on("enterShipmentDetails").to("enterShipmentDetails")
            on("enterTrackingDetails").to("enterTrackingDetails")
            on("enterContainerDetails").to("enterContainerDetails")
            on("pickShipmentItems").to("pickShipmentItems")
            on("sendShipment").to("sendShipment")
            on("showDetails").to("showDetails")
        }

        enterShipmentDetails {
            on("next") {

                try {
                    flow.shipmentInstance = shipmentService.getShipmentInstance(params.id)
                    bindData(flow.shipmentInstance, params)

                    if (flow.shipmentInstance.hasErrors() || !flow.shipmentInstance.validate()) {
                        return error()
                    } else {
                        shipmentService.saveShipment(flow.shipmentInstance)

                        // set (or reset) the shipment workflow here, since the shipment type may have change
                        flow.shipmentWorkflow = shipmentService.getShipmentWorkflow(flow.shipmentInstance)
                    }

                } catch (ShipmentException e) {
                    flash.message = e.message

                } catch (Exception e) {
                    flash.message = e.message
                }


            }.to("enterTrackingDetails")

            on("save") {
                bindData(flow.shipmentInstance, params)

                if (flow.shipmentInstance.hasErrors() || !flow.shipmentInstance.validate()) {
                    return error()
                } else {
                    shipmentService.saveShipment(flow.shipmentInstance)
                }
            }.to("finish")

            on("cancel").to("finish")

            on("addLocation") {
                flash.addLocation = true
                log.info("locationInstance.hashCode " + flash.locationInstance?.hashCode())
                if (!flash.locationInstance) {
                    flash.locationInstance = new Location()
                }
            }.to("enterShipmentDetails")
            on("saveLocation").to("saveLocationAction")


            // for the top-level links
            on("enterShipmentDetails").to("enterShipmentDetails")
            on("enterTrackingDetails").to("enterTrackingDetails")
            on("enterContainerDetails").to("enterContainerDetails")
            on("pickShipmentItems").to("pickShipmentItems")
            on("reviewShipment").to("reviewShipment")
            on("sendShipment").to("sendShipment")
            on("showDetails").to("showDetails")
        }

        enterTrackingDetails {
            on("back") {
                // TODO: figure out why this isn't working
                // flow.shipmentInstance.properties = params

                // don't need to do validation if just going back
            }.to("enterShipmentDetails")

            on("next") {
                bindData(flow.shipmentInstance, params)

                // need to manually bind the reference numbers and shipper
                bindReferenceNumbers(flow.shipmentInstance, flow.shipmentWorkflow, params)
                bindShipper(flow.shipmentInstance, params)

                if (flow.shipmentInstance.hasErrors() || !flow.shipmentInstance.validate()) {
                    return error()
                } else {
                    shipmentService.saveShipment(flow.shipmentInstance)
                }

            }.to("enterContainerDetails")

            on("save") {
                bindData(flow.shipmentInstance, params)

                // need to manually bind the reference numbers and shipper
                bindReferenceNumbers(flow.shipmentInstance, flow.shipmentWorkflow, params)
                bindShipper(flow.shipmentInstance, params)

                if (flow.shipmentInstance.hasErrors() || !flow.shipmentInstance.validate()) {
                    return error()
                } else {
                    shipmentService.saveShipment(flow.shipmentInstance)
                }

            }.to("finish")

            on("addPerson") {
                flash.addPerson = true
                if (!flash.personInstance) {
                    flash.personInstance = new Person()
                }
            }.to("enterTrackingDetails")

            on("addShipper") {
                flash.addShipper = true
                if (!flash.shipperInstance) {
                    flash.shipperInstance = new Shipper()
                }
            }.to("enterTrackingDetails")


            on("savePerson").to("savePersonAction")
            on("saveShipper").to("saveShipperAction")

            on("cancel").to("finish")

            // for the top-level links
            on("enterShipmentDetails").to("enterShipmentDetails")
            on("enterTrackingDetails").to("enterTrackingDetails")
            on("enterContainerDetails").to("enterContainerDetails")
            on("pickShipmentItems").to("pickShipmentItems")
            on("reviewShipment").to("reviewShipment")
            on("sendShipment").to("sendShipment")
            on("showDetails").to("showDetails")
        }

        enterContainerDetails {

            on("back") {
                // TODO: figure out why this isn't working
                // flow.shipmentInstance.properties = params

                // don't need to do validation if just going back
            }.to("enterTrackingDetails")

            on("enterContainerDetails") {

                log.info("Enter container details " + params)
                def selectedContainer = Container.get(params?.containerId)
                if (params.direction) {
                    def containerList = new ArrayList(flow.shipmentInstance.containers)
                    def sortOrder = selectedContainer ? selectedContainer.sortOrder : -1

                    def index = (sortOrder + Integer.parseInt(params.direction))
                    log.info "current = " + sortOrder + ", nextIndex " + index
                    selectedContainer = containerList.find { it.sortOrder == index }
                }

                [selectedContainer: selectedContainer]
            }.to("enterContainerDetails")


            on("next") {
                try {
                    shipmentService.saveShipment(flow.shipmentInstance)
                } catch (Exception e) {
                    log.error("Error saving shipment: " + e.message, e)
                    flow.shipmentInstance.errors.reject("${g.message(code: 'default.error.message', args: [e.message])}")
                    error()
                }
            }.to("pickShipmentItems")

            on("save") {
                try {
                    shipmentService.saveShipment(flow.shipmentInstance)
                } catch (Exception e) {
                    flash.message = e.message
                }

            }.to("finish")

            on("cancel").to("finish")


            on("sortContainers") {
                log.info("Sort containers" + params)
                try {
                    shipmentService.sortContainers(params.get("container[]"))
                } catch (Exception e) {
                    flash.message = e.message
                }

            }.to("enterContainerDetails")

            on("editContainer") {
                // set the container we will to edit
                flash.containerToEdit = Container.get(params.containerToEditId)
            }.to("enterContainerDetails")

            on("moveContainer") {
                // set the container we will to edit
                def location = Location.get(session.warehouse.id)
                flash.containerToMove = Container.get(params.containerToMoveId)
                def shipments = shipmentService.getOutgoingShipments(location)
                flash.shipments = shipments - flow.shipmentInstance

            }.to("enterContainerDetails")

            on("saveContainer").to("saveContainerAction")

            on("deleteContainer") {
                try {
                    def container = Container.get(params.container.id)
                    def containerName = container?.name
                    shipmentService.deleteContainer(container)
                    flash.message = "Successfully deleted container ${containerName}. Moved all of its items into unpacked items."
                    flow.selectedContainer = null
                } catch (Exception e) {
                    flash.message = e.message
                }
            }.to("enterContainerDetails")

            on("cloneContainer") {
                flash.cloneQuantity = params.cloneQuantity
            }.to("saveContainerAction")

            on("editBox") {
                // set the box we will to edit
                flash.boxToEdit = Container.get(params.boxToEditId)
            }.to("enterContainerDetails")

            on("saveBox").to("saveBoxAction")

            on("deleteBox") {
                try {
                    def box = Container.get(params.box.id)
                    def boxName = box?.name
                    shipmentService.deleteContainer(box)
                    flash.message = "Successfully deleted box ${boxName}. Moved all of its items into unpacked items."
                } catch (Exception e) {
                    flash.message = e.message
                }
            }.to("enterContainerDetails")

            on("cloneBox") {
                flash.cloneQuantity = params.cloneQuantity
            }.to("saveBoxAction")

            on("editItem") {
                // set the item we will to edit
                flash.itemToEdit = ShipmentItem.get(params.itemToEditId)
            }.to("enterContainerDetails")

            on("moveItem") {
                // set the item we will to edit
                flash.itemToMove = ShipmentItem.get(params.itemToMoveId)
            }.to("enterContainerDetails")

            // FIXME Refactor/remove - seems to be used when adding items to an outgoing shipment
            on("saveItem").to("saveItemAction")

            // FIXME Refactor/remove - seems to be used when editing items on an inbound shipment
            on("updateItem") {

                try {
                    log.info "update existing item: " + params

                    // Updating an existing shipment item
                    def shipmentItem = ShipmentItem.get(params.item?.id)

                    // Bind the parameters to the item instance
                    bindData(shipmentItem, params, ['product.name', 'recipient.name'])
                    // blacklisting names so that we don't change product name or recipient name here!

                    shipmentItem.quantity = params.int("quantity")

                    // Update the inventory item associated with this shipment item if the inventory item doesn't exist or the lot number has changed
                    def inventoryItem = InventoryItem.get(params?.inventoryItem?.id)
                    if (!inventoryItem || inventoryItem.lotNumber != params.lotNumber) {
                        inventoryItem = new InventoryItem(params)
                        inventoryItem = inventoryService.findOrCreateInventoryItem(inventoryItem)
                    }

                    // Update the expiration date if the user has changed it
                    Date expirationDate = params.expirationDate ? Date.parse("MM/dd/yyyy", params.expirationDate) : null
                    if (inventoryItem.expirationDate != expirationDate) {
                        inventoryItem.expirationDate = expirationDate
                    }

                    shipmentItem.inventoryItem = inventoryItem

                    if (shipmentService.validateShipmentItem(shipmentItem)) {
                        shipmentService.saveShipmentItem(shipmentItem)
                    }
                }
                catch (ValidationException e) {
                    log.error "Validation exception: " + e.message, e
                    flow.shipmentInstance.errors = e.errors
                    return error()
                }

                catch (RuntimeException e) {
                    log.error("Error saving shipment item ", e)
                    def shipmentInstance = Shipment.read(flow.shipmentInstance.id)
                    flow.shipmentInstance.errors.reject(e.message)
                    return error()
                }

            }.to("enterContainerDetails")

            on("deleteItem") {
                try {
                    ShipmentItem shipmentItem = ShipmentItem.get(params.item.id)
                    shipmentService.deleteShipmentItem(shipmentItem)
                } catch (Exception e) {
                    flash.message = e.message
                }
            }.to("enterContainerDetails")

            on("addContainer") {
                // set the container type to add
                flash.containerTypeToAdd = ContainerType.findById(params.containerTypeToAddId)
            }.to("enterContainerDetails")

            on("addBoxToContainer") {
                // this parameter triggers the "Add Box" dialog for the container to be opened on page reload
                // -1 means we need to assign the id of the newly created container to add box
                flash.addBoxToContainerId = (params.container?.id) ? params.container.id : -1
            }.to("saveContainerAction")

            on("addItemToContainer") {
                // this parameter triggers the "Add Item" dialog for the container to be opened on page reload
                // -1 means we need to assign the id of the new container to add item
                flash.addItemToContainerId = (params.container?.id) ? params.container.id : -1
            }.to("saveContainerAction")

            on("addItemToShipment") {
                // this parameter triggers the "Add Item" dialog for the container to be opened on page reload
                // -1 means we need to assign the id of the new container to add item
                flash.addItemToShipmentId = (params.container?.id) ? params.container.id : -1
            }.to("enterContainerDetails")

            on("addItemToBox") {
                // this parameter triggers the "Add Item" dialog for the container to be opened on page reload
                // -1 means we need to assign the id of the newly created box to to the item
                flash.addItemToContainerId = (params.box?.id) ? params.box.id : -1
            }.to("saveBoxAction")

            on("addAnotherItem") {
                // this parameter triggers the "Add Item" dialog for the container to be opened on page reload
                flash.addItemToContainerId = params.container.id
            }.to("saveItemAction")

            on("addContainers") {
                try {
                    shipmentService.createContainers(params.id, params.containerId, params.containerTypeId, params.containerText)
                    flash.message = "Created containers"
                } catch (ShipmentException e) {
                    flash.message = e.message
                }
            }.to("enterContainerDetails")

            on("addShipmentItem") {
                log.info "add shipment item " + params
                try {
                    shipmentService.addToShipmentItems(params.shipmentId, params.containerId, params?.inventoryItem?.id, params.quantity as int)
                    flash.message = "Added shipment item"

                } catch (ValidationException e) {
                    flow.shipmentInstance = Shipment.read(flow?.shipmentInstance?.id)
                    flow.shipmentInstance.errors = e.errors
                    error()
                } catch (Exception e) {
                    log.warn("Error while adding shipment item to shipment: " + e.message, e)
                    flow.shipmentInstance.errors.reject(e.message)
                    error()
                }

            }.to("enterContainerDetails")


            on("updateShipmentItem") {

                log.info "update shipment item: " + params
                try {
                    def shipmentItem = ShipmentItem.get(params.item?.id)
                    bindData(shipmentItem, params, ['product.name', 'recipient.name'])
                    // blacklisting names so that we don't change product name or recipient name here!
                    if (shipmentService.validateShipmentItem(shipmentItem)) {
                        shipmentService.saveShipmentItem(shipmentItem)
                    }
                    flash.message = "Updated shipment item"

                } catch (ValidationException e) {
                    flow.shipmentInstance.errors = e.errors
                    return error()

                } catch (RuntimeException e) {
                    flow.shipmentInstance.errors.reject(e.message)
                    //flash.message = e.message
                    return error()
                }

            }.to("enterContainerDetails")

            on("deleteContainers") {
                log.info "Delete containers from shipment " + params
                try {

                    def containerIds = params.list("containerId")
                    if (!containerIds) {
                        throw new ShipmentException(message: "You must select at least one container to delete", shipment: Shipment.load(params.id))
                    }
                    shipmentService.deleteContainers(params.id, containerIds, false)
                    flash.message = "Delete selected containers"
                } catch (ShipmentException e) {
                    flash.message = e.message
                }

            }.to("enterContainerDetails")

            on("deleteContainersAndItems") {
                log.info "Delete containers and items from shipment " + params
                try {
                    def containerIds = params.list("containerId")
                    if (!containerIds) {
                        throw new ShipmentException(message: "You must select at least one container to delete", shipment: Shipment.load(params.id))
                    }
                    shipmentService.deleteContainers(params.id, containerIds, true)
                    flash.message = "Delete selected containers and items"
                } catch (ShipmentException e) {
                    flash.message = e.message
                } catch (Exception e) {
                    flash.message = e.message
                }

            }.to("enterContainerDetails")

            on("deleteAllContainersAndItems") {
                log.info "Delete all containers and items from shipment " + params
                try {
                    shipmentService.deleteAllContainers(params.id, true)
                    flash.message = "Successfully deleted all containers and items"
                } catch (Exception e) {
                    log.error("Unable to delete shipment contents: " + e.message, e)
                    flash.message = e.message
                }

            }.to("enterContainerDetails")

            on("importPackingList") {
                log.info "Import packing list into shipment " + params

                try {
                    MultipartFile multipartFile = request.getFile('fileContents')
                    if (multipartFile.empty) {
                        flash.message = "File cannot be empty. Please select a packing list to import."
                        return
                    }

                    if (shipmentService.importPackingList(params.id, multipartFile.inputStream)) {
                        // refresh the shipment instance from database
                        flow.shipmentInstance = shipmentService.getShipmentInstance(params.id)
                        flash.message = "Successfully imported all packing list items. "

                    } else {
                        flash.message = "Failed to import packing list items due to an unknown error."
                    }
                } catch (Exception e) {
                    log.warn("Failed to import packing list due to the following error: " + e.message, e)
                    flash.message = "Failed to import packing list due to the following error: " + e.message
                }

            }.to("enterContainerDetails")

            on("moveShipmentItemToContainer") {
                log.info "Move shipment item to parent container " + params
                try {
                    shipmentService.moveShipmentItemToContainer(params.shipmentItem, params.container)
                    flash.message = "Successfully moved shipment item ${params.shipmentItem} to container ${params.container}"
                } catch (ShipmentException e) {
                    flash.message = e.message
                }

            }.to("enterContainerDetails")

            on("moveContainerToContainer") {
                log.info "Move childContainer item to parent container " + params
                try {
                    shipmentService.moveContainerToContainer(params.childContainer, params.parentContainer)
                    flash.message = "Successfully moved child container ${params.childContainer} to parent container ${params.parentContainer}"
                } catch (ShipmentException e) {
                    flash.message = e.message
                } catch (Exception e) {
                    flash.message = e.message
                }


            }.to("enterContainerDetails")


            on("moveItemToContainer").to("moveItemAction")
            on("moveContainerToShipment").to("moveContainerAction")
            /**
             on("addAnotherBox") {// this parameter triggers the "Add Box" dialog for the container to be opened on page reload
             flash.addBoxToContainerId = params.container.id as Integer}.to("saveBoxAction")
             */

            // for the top-level links
            on("enterShipmentDetails").to("enterShipmentDetails")
            on("enterTrackingDetails").to("enterTrackingDetails")
            on("prepareShipmentItems").to("prepareShipmentItems")
            on("pickShipmentItems").to("pickShipmentItems")
            //on("enterContainerDetails").to("enterContainerDetails")
            on("reviewShipment").to("reviewShipment")
            on("sendShipment").to("sendShipment")
            on("showDetails").to("showDetails")
        }

        pickShipmentItems {
            action {
                log.info "Pick list items"
                flow?.shipmentInstance?.refresh()
            }
            on("error").to "showPicklistItems"
            on(Exception).to "showPicklistItems"
            on("success").to "showPicklistItems"
        }

        showPicklistItems {

            // Needed to add an action state above
            render(view: "pickShipmentItems")

            on("back").to("enterContainerDetails")

            on("next") {

                try {
                    if (shipmentService.validatePicklist(flow?.shipmentInstance)) {
                        flash.message = "${g.message(code: 'shipping.picklistValidated.message')}"
                    }
                } catch (ValidationException e) {
                    log.warn("Validation error: " + e.message, e)
                    flow.shipmentInstance.errors = e.errors
                    error()

                } catch (Exception e) {
                    log.warn("Unexpected error: " + e.message, e)
                    flow.shipmentInstance.errors.reject(e.message)
                    error()
                }


            }.to("sendShipment")

            on("save") {
                shipmentService.saveShipment(flow.shipmentInstance)
            }.to("finish")

            on("nextShipmentItem") {
                log.info "Next shipment item: " + params
                Location location = Location.load(session.warehouse.id)
                def currentShipmentItemId = params.currentShipmentItemId
                ShipmentItem shipmentItem = flow.shipmentInstance?.getNextShipmentItem(currentShipmentItemId)
                List binLocations = inventoryService.getProductQuantityByBinLocation(location, shipmentItem?.product)
                log.info "binLocations: " + binLocations
                [shipmentItemSelected: shipmentItem, binLocationsSelected: binLocations]

            }.to("pickShipmentItems")


            on("clearPicklist") {

                try {
                    log.info "clear picklist: " + params
                    shipmentService.clearPicklist(flow.shipmentInstance)
                    flash.message = "Successfully cleared picklist for shipment ${flow?.shipmentInstance?.shipmentNumber}"
                } catch (ValidationException e) {
                    flow.shipmentInstance?.errors = e.errors
                } catch (Exception e) {
                    flow.shipmentInstance.errors.reject(e.message)
                }

            }.to("pickShipmentItems")


            on("validatePicklist") {
                log.info "Validate picklist " + params
                try {
                    if (shipmentService.validatePicklist(flow?.shipmentInstance)) {
                        flash.message = "${g.message(code: 'shipping.picklistValidated.message')}"
                    }
                } catch (ValidationException e) {
                    log.error("error: " + e.message, e)
                    flow.shipmentInstance.errors = e.errors
                } catch (Exception e) {
                    log.error("error: " + e.message, e)
                    flow.shipmentInstance.errors.reject(e.message)
                }

            }.to("pickShipmentItems")


            on("autoPickShipmentItems") {
                try {
                    log.info "AutoPick: " + params
                    flash.message = "Psych! This feature has not been implemented yet. But imagine how great life will be when it's finished."
                } catch (ShipmentItemException e) {
                    flash.message = e.message
                } catch (Exception e) {
                    flash.message = e.message
                }

            }.to("pickShipmentItems")

            on("deleteShipmentItem") {
                log.info "Delete shipment item " + params
                ShipmentItem nextShipmentItem
                def shipmentItem = ShipmentItem.load(params?.shipmentItem?.id)
                if (shipmentItem) {
                    nextShipmentItem = shipmentItem.shipment.getNextShipmentItem(shipmentItem?.id)
                    shipmentService.deleteShipmentItem(shipmentItem)
                }
                flash.message = "Successfully deleted item ${params?.shipmentItem?.id}"

                [currentShipmentItemId: nextShipmentItem?.id]

            }.to("pickShipmentItems")


            on("splitShipmentItem2") {
                log.info "Split shipment item " + params
                ShipmentItem shipmentItemClone
                def shipmentItem = ShipmentItem.load(params?.shipmentItem?.id)
                if (shipmentItem) {
                    shipmentItemClone = shipmentItem.cloneShipmentItem()
                    shipmentItemClone.quantity = 0
                    shipmentItem.shipment.addToShipmentItems(shipmentItemClone)
                    shipmentItem.shipment.save(flush: true)
                }
                flash.message = "Successfully split item ${params?.shipmentItem?.id}"
                [currentShipmentItemId: shipmentItemClone?.id]

            }.to("pickShipmentItems")


            on("pickShipmentItem") {
                log.info "Save shipment item pick " + params
                ShipmentItem shipmentItemInstance
                try {
                    shipmentItemInstance = ShipmentItem.get(params.shipmentItem.id)

                    if (!params.selection) {
                        flash.message = "${g.message(code: 'shipping.mustPickBinLocation.message', default: 'Please choose a bin location from the list')}"
                        return error()
                    }

                    // Parse the data into bin location and inventory item components
                    String[] selection = params.selection.split(":")
                    String binLocationId = selection[0]
                    String inventoryItemId = selection[1]

                    // Set inventory item
                    log.info "inventoryItemId: " + inventoryItemId
                    InventoryItem inventoryItem = (inventoryItemId) ? InventoryItem.load(inventoryItemId) : null
                    if (!inventoryItem) {
                        shipmentItemInstance.errors.reject("shipmentItem.inventoryItem.required.message", "Inventory item is a required field")
                        throw new ValidationException("Unable to update pick list item", shipmentItemInstance.errors)
                    } else {
                        log.info "Setting inventoryItem " + inventoryItem
                        shipmentItemInstance.inventoryItem = inventoryItem
                    }

                    // Set bin location
                    log.info "binLocationId: " + binLocationId
                    Location binLocation = (binLocationId && !binLocationId.equals("null")) ? Location.load(binLocationId) : null
                    if (binLocation) {
                        log.info "Setting bin location " + binLocation
                        shipmentItemInstance.binLocation = binLocation
                    } else {
                        shipmentItemInstance.binLocation = null
                    }

                    // Set quantity
                    Integer quantity = Integer.parseInt(params.quantity)
                    shipmentItemInstance.quantity = quantity

                    shipmentService.validateShipmentItem(shipmentItemInstance)

                    if (shipmentItemInstance.save(flush: true)) {
                        flash.message = "Successfully picked shipment item. "
                    } else {
                        flash.message = "Failed to edit pick list due to an unknown error."
                    }

                    // Get the next shipment item
                    ShipmentItem nextShipmentItem = shipmentItemInstance.shipment.getNextShipmentItem(shipmentItemInstance?.id)
                    [currentShipmentItemId: nextShipmentItem?.id]

                } catch (ValidationException e) {
                    log.error("Failed to edit pick list due to the following error: " + e.message, e)
                    flow.shipmentInstance = Shipment.read(flow?.shipmentInstance?.id)
                    flow.shipmentInstance.errors = e.errors
                    [currentShipmentItemId: shipmentItemInstance?.id]
                    error()
                }


            }.to("pickShipmentItems")


            on("splitShipmentItem") {
                log.info "Split shipment item " + params

                try {
                    def shipmentItemInstance = ShipmentItem.load(params.shipmentItem.id)

                    def currentQuantity = shipmentItemInstance.quantity
                    def splitQuantity = params.splitQuantity as int
                    def newQuantity = currentQuantity - splitQuantity

                    // Make sure there's no funny business (i.e. entering a split quantity greater than the original quantity)
                    if (newQuantity <= 0 || newQuantity > currentQuantity) {
                        shipmentItemInstance.errors.reject("shipmentItem.invalidQuantity.message", "Quantity is invalid")
                        throw new ValidationException("Unable to update pick list item", shipmentItemInstance.errors)
                    }

                    String[] selection = params.selection.split(":")
                    String binLocationId = selection[0]
                    String inventoryItemId = selection[1]

                    Location binLocation = (binLocationId && !binLocationId.equals("null")) ? Location.load(binLocationId) : null
                    InventoryItem inventoryItem = (inventoryItemId) ? InventoryItem.load(inventoryItemId) : null


                    if (!inventoryItem) {
                        shipmentItemInstance.errors.reject("shipmentItem.inventoryItem.required.message", "Inventory item is a required field")
                        throw new ValidationException("Unable to update pick list item", shipmentItemInstance.errors)
                    }

                    // Update the old shipment item with the new quantity
                    shipmentItemInstance.quantity = newQuantity

                    // Create a new shipment item and update with selected bin location and split quantity
                    def splitItemInstance = shipmentItemInstance.cloneShipmentItem()
                    splitItemInstance.inventoryItem = inventoryItem
                    splitItemInstance.binLocation = binLocation
                    splitItemInstance.quantity = splitQuantity
                    shipmentItemInstance.shipment.addToShipmentItems(splitItemInstance)


                    boolean success = shipmentItemInstance.shipment.save(flush: true)
                    if (success) {
                        flash.message = "Successfully split shipment item. "
                    } else {
                        flash.message = "Failed to edit pick list due to an unknown error."
                    }

                    [currentShipmentItemId: splitItemInstance.id]

                } catch (Exception e) {
                    log.error("Failed to edit pick list due to the following error: " + e.message, e)
                    flash.message = "Failed to edit pick list due to the following error: " + e.message
                }

            }.to("pickShipmentItems")


            on("cancel").to("finish")
            on("success").to("finish")

            // Top-level navigation transitions
            on("enterShipmentDetails").to("enterShipmentDetails")
            on("enterTrackingDetails").to("enterTrackingDetails")
            on("enterContainerDetails").to("enterContainerDetails")
            on("pickShipmentItems").to("pickShipmentItems")
            on("sendShipment").to("sendShipment")
            on("showDetails").to("showDetails")
        }

        sendShipment {

            // All transition buttons on the bottom of the page
            on("back") {

            }.to("pickShipmentItems")

            on("next").to("sendShipmentAction")

            on("save") {
                shipmentService.saveShipment(flow.shipmentInstance)
            }.to("finish")

            on("cancel").to("finish")
            on("success").to("finish")

            // Top-level navigation transitions
            on("enterShipmentDetails").to("enterShipmentDetails")
            on("enterTrackingDetails").to("enterTrackingDetails")
            on("enterContainerDetails").to("enterContainerDetails")
            on("pickShipmentItems").to("pickShipmentItems")
            on("sendShipment").to("sendShipment")
            on("showDetails").to("showDetails")
        }

        sendShipmentAction {
            action { SendShipmentCommand command ->
                println "Send shipment " + params

                flow.command = command

                if (!command.validate()) {
                    log.info "Errors: " + command.errors
                    return error()
                }

                try {
                    shipmentService.validatePicklist(flow?.shipmentInstance)
                } catch (ValidationException e) {
                    flow.shipmentInstance.errors = e.errors
                    error()
                } catch (Exception e) {
                    flow.shipmentInstance.errors.reject(e.message)
                    error()
                }

                Transaction transactionInstance
                User userInstance = User.get(session.user.id)
                Shipment shipmentInstance = Shipment.get(params.id)
                ShipmentWorkflow shipmentWorkflow = shipmentService.getShipmentWorkflow(params.id)

                // This probably shouldn't occur, but we can leave it for now
                if (!shipmentInstance) {
                    flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'shipment.label', default: 'Shipment'), params.id])}"
                    redirect(action: "list", params: [type: params.type])
                } else {
                    // handle a submit
                    if ("POST".equalsIgnoreCase(request.getMethod())) {
                        // create the list of email recipients
                        def emailRecipients = new HashSet()
                        if (params.emailRecipientId) {
                            def recipientIds = params.list("emailRecipientId")
                            recipientIds.each { recipientId ->
                                def recipient = Person.get(recipientId)
                                if (recipient && recipient.email)
                                    emailRecipients.add(recipient)
                            }
                        }

                        try {

                            // validate the picklist
                            shipmentService.validatePicklist(flow?.shipmentInstance)

                            // send the shipment
                            shipmentService.sendShipment(shipmentInstance, command.comments, session.user, session.warehouse,
                                    command.actualShippingDate, command.debitStockOnSend)
                            triggerSendShipmentEmails(shipmentInstance, userInstance, emailRecipients)

                        } catch (ValidationException e) {
                            flow.shipmentInstance.errors = e.errors
                            return error()
                        }
                        catch (ShipmentException e) {
                            flash.message = e.message
                            flow.shipmentInstance = Shipment.get(params.id)
                            flow.shipmentInstance.errors = e.shipment.errors
                            flow.shipmentWorkflow = shipmentService.getShipmentWorkflow(params.id)
                            return error()
                        }
                        catch (TransactionException e) {
                            command.transaction = e.transaction
                            flow.shipmentInstance = Shipment.get(params.id)
                            flow.shipmentWorkflow = shipmentService.getShipmentWorkflow(params.id)
                            return error()
                        }
                        catch (RuntimeException e) {
                            flash.message = e.message
                            flow.shipmentInstance = Shipment.get(params.id)
                            flow.shipmentWorkflow = shipmentService.getShipmentWorkflow(params.id)
                            return error()
                        } catch (Exception e) {
                            flow.shipmentInstance.errors.reject(e.message)
                            return error()
                        }

                        if (!shipmentInstance?.hasErrors() && !transactionInstance?.hasErrors()) {
                            command.shipment = shipmentInstance
                            command.transaction = transactionInstance
                            return success()

                        } else {
                            return error()
                        }
                    }
                }
            }
            on("success").to("finish")
            on("error").to("sendShipment")
        }


        saveContainerAction {
            action {
                log.info("Save container " + params)

                def container

                // fetch the existing container if this is an edit, otherwise add a container to this shipment
                if (params.container?.id) {
                    container = Container.get(params.container?.id)
                } else {
                    def containerType = ContainerType.get(params.containerTypeToAddId)
                    if (!containerType) {
                        throw new Exception("Invaild container type passed to editContainer action.")
                    }

                    container = flow.shipmentInstance.addNewContainer(containerType)
                }

                bindData(container, params)

                log.info("Container recipient " + container.recipient)

                // TODO: make sure that this works properly if there are errors?
                if (container.hasErrors() || !container.validate()) {
                    invalid()
                } else {
                    log.info "# containers: " + flow?.shipmentInstance?.containers?.size()
                    try {

                        shipmentService.saveContainer(container)
                    } catch (HibernateOptimisticLockingFailureException e) {
                        flash.message = e?.cause?.message ?: e?.message
                        invalid()
                    }
                    catch (Exception e) {
                        flash.message = e?.cause?.message ?: e?.message
                        invalid()
                    }
                    // save a reference to this container if we need to clone it
                    if (flash.cloneQuantity) {
                        flash.cloneContainer = container
                    }

                    // assign the id of the container if needed
                    if (flash.addItemToContainerId == -1) {
                        flash.addItemToContainerId = container.id
                    }
                    if (flash.addBoxToContainerId == -1) {
                        flash.addBoxToContainerId = container.id
                    }

                    // used to refocus page with the appropriate container
                    flash.selectedContainer = container

                    valid()
                }
            }


            on("valid").to("cloneContainerAction")
            on("invalid").to("enterContainerDetails")
        }

        cloneContainerAction {
            action {

                // see if we have to make copies of this container
                if (flash.cloneQuantity && flash.cloneContainer) {
                    shipmentService.copyContainer(flash.cloneContainer, flash.cloneQuantity as Integer)
                }

                valid()
            }

            on("valid").to("enterContainerDetails")
        }
        saveLocationAction {
            action {
                def locationInstance
                log.info "saveLocationAction: " + params
                if (flash.locationInstance) {
                    locationInstance.properties = params
                } else {
                    locationInstance = new Location(params)
                }

                def locations = Location.findAll(locationInstance)
                //flash.message
                log.info "saveLocationAction: found " + locations?.size() + " locations"
                if (locations) {
                    flash.message = "${warehouse.message(code: 'location.alreadyExists.message', args: [locationInstance.name])}"
                    invalid()
                } else {

                    if (locationInstance.save(flush: true) && !locationInstance.hasErrors()) {
                        log.info "saved location " + locationInstance + " with id " + locationInstance?.id
                        flash.message = "${warehouse.message(code: 'location.created.message', args: [locationInstance.name])}"
                        valid()
                    } else {
                        log.info "invalid location " + locationInstance.errors
                        flash.message = "${warehouse.message(code: 'location.invalid.message', args: [locationInstance.name])}"
                        flash.addLocation = true
                        flash.locationInstance = locationInstance
                        invalid()
                    }
                }
            }
            on("valid").to("enterShipmentDetails")
            on("invalid").to("enterShipmentDetails")

        }
        savePersonAction {
            action {
                log.info "savePersonAction: " + params

                def personInstance = new Person(params)
                flash.personInstance = personInstance

                def persons = Person.findAll(personInstance)
                flash.message
                log.info "savePersonAction: found " + persons?.size() + " persons"
                if (persons) {
                    flash.message = "${warehouse.message(code: 'person.alreadyExists.message', args: [personInstance.firstName, personInstance.lastName])}"
                    invalid()
                } else {
                    log.info "validate person"
                    if (!personInstance.validate()) {
                        log.info "invalid person " + personInstance.errors
                        flash.message = "${warehouse.message(code: 'person.invalid.message', args: [personInstance.firstName, personInstance.lastName])}"
                        invalid()
                    } else {

                        if (personInstance.save(flush: true) && !personInstance.hasErrors()) {
                            log.info "saved person " + personInstance + " with id " + personInstance?.id
                            flash.message = "${warehouse.message(code: 'person.created.message', args: [personInstance.firstName, personInstance.lastName])}"
                            valid()
                        } else {
                            log.info "invalid person " + personInstance.errors
                            flash.message = "${warehouse.message(code: 'person.invalid.message', args: [personInstance.firstName, personInstance.lastName])}"
                            invalid()
                        }
                    }
                }

            }
            on("valid").to("enterTrackingDetails")
            on("invalid").to("enterTrackingDetails")

        }
        saveShipperAction {
            action {
                log.info "saveShipperAction: " + params

                def shipperInstance = new Shipper(params)
                flash.shipperInstance = shipperInstance

                def shippers = Shipper.findAll(shipperInstance)
                flash.message
                log.info "saveShipperAction: found " + shippers?.size() + " persons"
                if (shippers) {
                    flash.message = "${warehouse.message(code: 'shipper.alreadyExists.message', args: [shipperInstance?.name])}"
                    invalid()
                } else {
                    log.info "validate shipper"
                    if (!shipperInstance.validate()) {
                        log.info "invalid person " + shipperInstance.errors
                        flash.message = "${warehouse.message(code: 'shipper.invalid.message', args: [shipperInstance?.name])}"
                        invalid()
                    } else {

                        if (shipperInstance.save(flush: true) && !shipperInstance.hasErrors()) {
                            log.info "saved shipper " + shipperInstance + " with id " + shipperInstance?.id
                            flash.message = "${warehouse.message(code: 'shipper.created.message', args: [shipperInstance?.name])}"
                            valid()
                        } else {
                            log.info "invalid shipper " + shipperInstance.errors
                            flash.message = "${warehouse.message(code: 'shipper.invalid.message', args: [shipperInstance?.name])}"
                            invalid()
                        }
                    }
                }

            }
            on("valid").to("enterTrackingDetails")
            on("invalid").to("enterTrackingDetails")

        }

        saveBoxAction {
            action {

                // first handle the box
                def box
                def container

                // fetch the existing container if this is an edit, otherwise add a container to this shipment
                if (params.box?.id) {
                    box = Container.get(params.box.id)
                } else {
                    // if not, get the container that we are adding the box to
                    container = Container.get(params.container.id)
                    box = container.addNewContainer(ContainerType.findById(Constants.BOX_CONTAINER_TYPE_ID))
                }

                bindData(box, params)

                log.info("setting recipient ...")
                // If a recipient is not specified, we should specify one
                if (!box?.recipient) {
                    box.recipient = container?.recipient
                }

                // TODO: make sure that this works properly if there are errors?
                if (box.hasErrors() || !box.validate()) {
                    invalid()
                } else {

                    try {
                        shipmentService.saveContainer(box)
                    }
                    catch (Exception e) {
                        flash.message = e.message
                        invalid()
                    }

                    // save a reference to this box if we need to clone it
                    if (flash.cloneQuantity) {
                        flash.cloneContainer = box
                    }

                    // assign the id of the box if needed
                    if (flash.addItemToContainerId == -1) {
                        flash.addItemToContainerId = box.id
                    }

                    // used to refocus page with the appropriate container
                    flash.selectedContainer = box

                }

                valid()
            }

            on("valid").to("cloneContainerAction")
            on("invalid").to("enterContainerDetails")
        }

        moveItemAction {
            action {

                // move an item to another container
                log.info "Move item to container " + params

                def shipment = flow.shipmentInstance
                def item = shipment.shipmentItems.find { it.id == params.item.id }

                if (item) {
                    def destinations = makeDestinationMap(item, params)

                    if (shipmentService.moveItem(item, destinations)) {

                        if (!shipment.hasErrors()) {
                            shipment.save(flush: true)
                        } else {
                            throw new RuntimeException("shipment has errors " + shipment.errors)
                        }

                        valid()
                    } else {
                        invalid()
                    }
                } else {
                    invalid()
                }
            }

            on("valid").to("enterContainerDetails")
            on("invalid").to("enterContainerDetails")
        }
        moveContainerAction {
            action {
                // move an item to another container
                log.info "Move container to another shipment " + params
                def container = Container.get(params.container.id)
                def oldShipment = container.shipment
                log.info "Old shipment " + oldShipment.id
                def newShipment = Shipment.get(params.shipment.id)

                try {
                    shipmentService.moveContainer(container, newShipment)

                    // Shipment in the flow scope does not refresh automatically
                    flow.shipmentInstance.refresh()


                }
                catch (UnsupportedOperationException e) {
                    flash.message = "${warehouse.message(code: 'default.unsupportedOperation.message')}"
                    error()
                }
                log.info "Old shipment " + oldShipment.id
            }
            on("success").to("enterContainerDetails")
            on("error").to("enterContainerDetails")
        }

        saveItemAction {
            action {
                try {
                    log.info "save item action: " + params
                    def shipment = flow?.shipmentInstance
                    def container = Container.get(params?.container?.id)
                    def inventoryItem = InventoryItem.get(params?.inventoryItem?.id)
                    if (!inventoryItem) {
                        inventoryItem = new InventoryItem(params)
                        inventoryItem = inventoryService.findOrCreateInventoryItem(inventoryItem)
                    }

                    // Create a new shipment item
                    // FIXME product: product, lotNumber: params.lotNumber
                    def shipmentItem = new ShipmentItem(
                            shipment: shipment,
                            //product: inventoryItem?.product,
                            //lotNumber: inventoryItem?.lotNumber,
                            //expirationDate: inventoryItem?.expirationDate,
                            container: container,
                            inventoryItem: inventoryItem)

                    // FIXME Property [shipment] of class [class org.pih.warehouse.shipping.ShipmentItem] cannot be null
                    //shipmentItem.shipment = flow.shipmentInstance

                    // Bind the form parameters to the shipment item
                    // blacklisting names so that we don't change product name or recipient name here!
                    bindData(shipmentItem, params, ['product.name', 'recipient.name'])

                    // If a recipient is not specified, the shipment item should inherit the recipient from the parent container
                    if (!shipmentItem?.recipient) {
                        shipmentItem.recipient = container?.recipient
                    }

                    // In case there are errors, we use this flow-scoped variable to display errors to user
                    flow.itemInstance = shipmentItem

                    // Add shipment item if this is an incoming shipment (bypass validation so that we don't check against on-hand quantity)
                    if (shipment?.destination?.id == session?.warehouse?.id || shipmentService.validateShipmentItem(shipmentItem)) {

                        // Add shipment item to shipment
                        shipment.addToShipmentItems(shipmentItem)

                        // Need to validate shipment item before adding it to the shipment
                        shipmentService.saveShipment(shipment)
                        valid()
                    }

                } catch (ShipmentItemException e) {
                    if (!flow.itemInstance) {
                        flow.itemInstance = new ShipmentItem()
                    }
                    flow.itemInstance.errors.reject(e?.message)
                    invalid()
                } catch (HibernateOptimisticLockingFailureException e) {
                    flash.message = e?.cause?.message ?: e?.message
                    invalid()
                } catch (RuntimeException e) {
                    log.error("Error saving shipment item ", e)
                    // Need to instantiate an item instance (if it doesn't exist) so we can add errors to it
                    if (!flow.itemInstance) {
                        flow.itemInstance = new ShipmentItem()
                    }
                    // If there are no errors already (added from the save or
                    // validation method, then we should add the generic error message from the exception)
                    flow.itemInstance.errors.reject(e.message)
                    invalid()
                }

            }
            on("valid").to("enterContainerDetails")
            on("invalid").to("enterContainerDetails")
        }

        showDetails {
            redirect(controller: "shipment", action: "showDetails", params: ["id": flow.shipmentInstance.id ?: ''])
        }

        finish {
            if (flow.shipmentInstance.id) {
                redirect(controller: "shipment", action: "showDetails", params: ["id": flow.shipmentInstance.id ?: ''])
            } else {
                redirect(controller: "shipment", action: "list")
            }
        }
    }


    void bindReferenceNumbers(Shipment shipment, ShipmentWorkflow workflow, Map params) {
        // need to manually bind the reference numbers
        if (!shipment.referenceNumbers) {
            shipment.referenceNumbers = []
        }
        for (ReferenceNumberType type in workflow?.referenceNumberTypes) {

            // find the reference number for this reference number type
            ReferenceNumber referenceNumber = shipment.referenceNumbers.find({
                it.referenceNumberType.id == type.id
            })

            if (params.referenceNumbersInput?."${type.id}") {
                // check to see if this reference value already exists
                if (referenceNumber) {
                    // if it exists, assign the new id
                    referenceNumber.identifier = params.referenceNumbersInput?."${type.id}"
                } else {
                    // otherwise, we need to add a new reference number
                    shipment.referenceNumbers.add(new ReferenceNumber([identifier         : params.referenceNumbersInput?."${type.id}",
                                                                       referenceNumberType: type]))
                }
            } else {
                // if there is no param for this reference number, we need to remove it from the list of reference numbers
                if (referenceNumber) {
                    shipment.referenceNumbers.remove(referenceNumber)
                }
            }
        }
    }

    void bindShipper(Shipment shipment, Map params) {
        // need to manually bind the shipper since it is nested within the "shipmentMethod"
        if (params.shipperInput) {
            if (!shipment.shipmentMethod) {
                // create the new ShipmentMethod object if need be
                shipment.shipmentMethod = new ShipmentMethod()
            }
            shipment.shipmentMethod.shipper = Shipper.get(params.shipperInput.id)
        } else {
            // if there is no input for shipper, we remove the *entire* shipment method
            // TODO: does this delete the underlying shipment method upon saving?
            shipment.shipmentMethod = null
        }
    }

    /**
     *
     * @param shipmentInstance
     * @param userInstance
     * @param recipients
     */
    void triggerSendShipmentEmails(Shipment shipmentInstance, User userInstance, Set<Person> recipients) {
        if (!recipients) recipients = new HashSet<Person>()

        // Add all admins to the email
        def adminList = userService.findUsersByRoleType(RoleType.ROLE_SHIPMENT_NOTIFICATION)
        adminList.each { adminUser ->
            if (adminUser?.email) {
                recipients.add(adminUser)
            }
        }

        // add the current user to the list of email recipients
        if (userInstance) {
            recipients.add(userInstance)
        }


        if (!shipmentInstance.hasErrors()) {

            log.info("Create shipment flow " + createShipmentFlow)
            if (!userInstance) userInstance = User.get(session.user.id)
            def shipmentName = "${shipmentInstance.name}"
            def shipmentType = "${format.metadata(obj: shipmentInstance.shipmentType)}"
            def shipmentDate = "${formatDate(date: shipmentInstance?.actualShippingDate, format: 'MMMMM dd yyyy')}"
            def subject = "${warehouse.message(code: 'shipment.hasBeenShipped.message', args: [shipmentType, shipmentName, shipmentDate])}"
            def body = g.render(template: "/email/shipmentShipped", model: [shipmentInstance: shipmentInstance, userInstance: userInstance])
            def toList = recipients?.collect { it?.email }?.unique()
            log.info("Mailing shipment emails to ${toList} ")

            try {
                mailService.sendHtmlMail(subject, body.toString(), toList)
            } catch (Exception e) {
                log.error "Error triggering send shipment emails " + e.message
            }
        }
    }

    static protected Map makeDestinationMap(ShipmentItem item, Map params) {

        def shipment = item.shipment
        def containerIds = shipment.containers.collect { it.id }
        if (item.container) {
            containerIds.remove(item.container.id)
            containerIds << "0"
        }

        def destinations = [:]
        containerIds.each { containerId ->
            def quantityFromForm = params["quantity-" + containerId]
            def quantity = quantityFromForm ? quantityFromForm as Integer : 0

            if (quantity > 0) {
                destinations[containerId] = quantity
            }
        }
        return destinations
    }

}
