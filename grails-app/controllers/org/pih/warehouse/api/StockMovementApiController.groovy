/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.api

import grails.converters.JSON
import org.apache.commons.lang.math.NumberUtils
import org.codehaus.groovy.grails.web.json.JSONObject
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.StockMovementService
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionStatus

class StockMovementApiController {

    StockMovementService stockMovementService
    def dataService

    def list = {
        int max = Math.min(params.max ? params.int('max') : 10, 1000)
        int offset = params.offset ? params.int("offset") : 0
        def stockMovements = params.direction == "INBOUND" ?
                stockMovementService.getInboundStockMovements(max, offset) :
                stockMovementService.getOutboundStockMovements(max, offset)

        stockMovements = stockMovements.collect { StockMovement stockMovement ->
            Map json = stockMovement.toJson()
            def excludes = params.list("exclude")
            if (excludes) {
                excludes.each { exclude ->
                    json.remove(exclude)
                }
            }
            return json
        }
        render([data: stockMovements] as JSON)
    }

    def read = {
        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)
        String stepNumber = params.stepNumber
        def totalCount = stockMovement.lineItems.size()

        // FIXME this should happen in the service
        if (params.stepNumber == "4") {
            totalCount = stockMovementService.getPickPageItems(params.id, null, null).size()
        }
        if (params.stepNumber == "5") {
            totalCount = stockMovementService.getPackPageItems(params.id, null, null).size()
        }
        if (params.stepNumber == "6" && !stockMovement.origin.isSupplier() && stockMovement.origin.supports(ActivityCode.MANAGE_INVENTORY)) {
            totalCount = stockMovementService.getPackPageItems(params.id, null, null).size()
        }

        // FIXME Debugging
        JSONObject jsonObject = new JSONObject(stockMovement.toJson())

        log.debug "read " + jsonObject.toString(4)
        render([data: stockMovement, totalCount: totalCount] as JSON)
    }

    def create = { StockMovement stockMovement ->
        // Detect whether inbound or outbound stock movement
        def currentLocation = Location.get(session.warehouse.id)
        StockMovementType stockMovementType = stockMovement.origin.equals(currentLocation) ?
                StockMovementType.OUTBOUND : stockMovement.destination.equals(currentLocation) ?
                        StockMovementType.INBOUND : null

        stockMovement.stockMovementType = stockMovementType
        StockMovement newStockMovement = stockMovementService.createStockMovement(stockMovement)
        response.status = 201
        render([data: newStockMovement] as JSON)
    }

    /**
     * @deprecated FIXME refactor to avoid using RPC-style endpoints
     */
    def updateRequisition = { //StockMovement stockMovement ->
        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)
        bindStockMovement(stockMovement, request.JSON)
        stockMovementService.updateStockMovement(stockMovement)
        forward(action: "read")
    }

    /**
     * @deprecated FIXME refactor to avoid using RPC-style endpoints
     */
    def updateShipment = { //StockMovement stockMovement ->
        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)
        bindStockMovement(stockMovement, request.JSON)
        stockMovementService.updateShipment(stockMovement)
        render status: 200
    }

    def delete = {
        stockMovementService.deleteStockMovement(params.id)
        render status: 204
    }


    def status = {
        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)
        render([data: stockMovement?.status] as JSON)
    }

    def deleteStatus = {
        stockMovementService.rollbackStockMovement(params.id)
        forward(action: "read")
    }

    /**
     * Peforms a status update on the stock movement and forwards to the read action.
     */
    def updateStatus = {
        JSONObject jsonObject = request.JSON
        log.info "update status: " + jsonObject.toString(4)
        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)
        stockMovementService.transitionStockMovement(stockMovement, jsonObject)
        render status: 200
    }


    /**
     * @deprecated FIXME refactor to avoid using RPC-style endpoints
     */
    def removeAllItems = {
        Requisition requisition = Requisition.get(params.id)
        stockMovementService.removeRequisitionItems(requisition)
        render status: 204
    }

    /**
     * @deprecated FIXME refactor to avoid using RPC-style endpoints
     */
    def reviseItems = {
        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)
        bindStockMovement(stockMovement, request.JSON)
        List<EditPageItem> revisedItems = stockMovementService.reviseItems(stockMovement)
        render([data: revisedItems] as JSON)
    }

    /**
     * @deprecated FIXME refactor to avoid using RPC-style endpoints
     */
    def updateItems = {
        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)
        bindStockMovement(stockMovement, request.JSON)
        stockMovement = stockMovementService.updateItems(stockMovement)
        render([data: stockMovement] as JSON)
    }

    /**
     * @deprecated FIXME refactor to avoid using RPC-style endpoints
     */
    def updateShipmentItems = {
        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)

        JSONObject jsonObject = request.JSON
        log.debug "revise items: " + jsonObject.toString(4)

        def packPageItems = createPackPageItemsFromJson(stockMovement, jsonObject.packPageItems)

        stockMovementService.updatePackPageItems(packPageItems)

        render([data: stockMovementService.getPackPageItems(stockMovement.id, null, null)] as JSON)
    }

    def updateAdjustedItems = {
        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)
        stockMovementService.updateAdjustedItems(stockMovement, params.adjustedProduct)

        stockMovement = stockMovementService.getStockMovement(params.id)

        render([data: stockMovement] as JSON)
    }

    def exportPickListItems = {
        List<PickPageItem> pickPageItems = stockMovementService.getPickPageItems(params.id, null, null )
        List<PicklistItem> picklistItems = pickPageItems.inject([]) { result, pickPageItem ->
            result.addAll(pickPageItem.picklistItems)
            result
        }
        // We need to create at least one row to ensure an empty template
        if (picklistItems?.empty) {
            picklistItems.add(new PicklistItem())
        }

        def lineItems = picklistItems.collect {
            [
                    "${g.message(code: 'default.id.label')}": it?.requisitionItem?.id ?: "",
                    "${g.message(code: 'product.productCode.label')}": it?.requisitionItem?.product?.productCode ?: "",
                    "${g.message(code: 'product.name.label')}": it?.requisitionItem?.product?.name ?: "",
                    "${g.message(code: 'inventoryItem.lotNumber.label')}": it?.inventoryItem?.lotNumber ?: "",
                    "${g.message(code: 'inventoryItem.expirationDate.label')}": it?.inventoryItem?.expirationDate ? it.inventoryItem.expirationDate.format(Constants.EXPIRATION_DATE_FORMAT) : "",
                    "${g.message(code: 'inventoryItem.binLocation.label')}": it?.binLocation?.name ?: "",
                    "${g.message(code: 'default.quantity.label')}": it?.quantity ?: "",
            ]
        }
        String csv = dataService.generateCsv(lineItems)
        response.setHeader("Content-disposition", "attachment; filename=\"StockMovementItems-${params.id}.csv\"")
        render(contentType: "text/csv", text: csv.toString(), encoding: "UTF-8")
    }

    def importPickListItems = { ImportDataCommand command ->

        try {
            StockMovement stockMovement = stockMovementService.getStockMovement(params.id)
            List<PickPageItem> pickPageItems = stockMovementService.getPickPageItems(params.id, null, null )

            def importFile = command.importFile
            if (importFile.isEmpty()) {
                throw new IllegalArgumentException("File cannot be empty")
            }

            if (importFile.fileItem.contentType != "text/csv") {
                throw new IllegalArgumentException("File must be in CSV format")
            }

            String csv = new String(importFile.bytes)
            def settings = [separatorChar: ',', skipLines: 1]
            csv.toCsvReader(settings).eachLine { tokens ->
                String requisitionItemId = tokens[0]
                String lotNumber = tokens[3] ?: null
                String expirationDate = tokens[4] ?: null
                String binLocation = tokens[5] ?: null
                Integer quantityPicked = tokens[6] ? tokens[6].toInteger() : null

                if (!requisitionItemId || quantityPicked == null) {
                    throw new IllegalArgumentException("Requisition item id and quantity picked are required")
                }

                if (lotNumber?.contains("E") && NumberUtils.isNumber(lotNumber)) {
                    throw new IllegalArgumentException("Lot numbers must not be specified in scientific notation. " +
                            "Please reformat field with Lot Number: \"${lotNumber}\" to a number format")
                }

                PickPageItem pickPageItem = pickPageItems.find {
                    it.requisitionItem?.id == requisitionItemId
                }

                if (!pickPageItem) {
                    throw new IllegalArgumentException("Requisition item id: ${requisitionItemId} not found")
                }

                // FIXME Should find bin location by name and parent and inventory item by lot number and expiration date
                // and compare object equality (or at least PK equality) rather than comparing various components
                AvailableItem availableItem = pickPageItem.availableItems?.find {
                    (binLocation ? it.binLocation?.name == binLocation : !it.binLocation) && lotNumber == (it.inventoryItem?.lotNumber ?: null) &&
                            expirationDate == (it?.inventoryItem?.expirationDate ? it.inventoryItem.expirationDate.format(Constants.EXPIRATION_DATE_FORMAT) : null)
                }

                if (!availableItem) {
                    throw new IllegalArgumentException("There is no item available with lot: ${lotNumber ?: ""}, expiration date: ${tokens[2] ?: ""} and bin: ${binLocation ?: ""}")
                }

                RequisitionItem requisitionItem = pickPageItem.requisitionItem?.modificationItem ?: pickPageItem.requisitionItem

                pickPageItem.picklistItems.each {
                    if (it.id) {
                        it.quantity = 0
                    }
                }
                pickPageItem.picklistItems.add(new PicklistItem(
                        requisitionItem: requisitionItem,
                        inventoryItem: availableItem.inventoryItem,
                        binLocation: availableItem.binLocation,
                        quantity: quantityPicked,
                        sortOrder: pickPageItem.sortOrder
                ))
            }

            stockMovementService.createOrUpdatePicklistItem(stockMovement, pickPageItems)

        } catch (Exception e) {
            // FIXME The global error handler does not return JSON for multipart uploads
            log.warn("Error occurred while importing CSV: " + e.message, e)
            response.status = 500
            render([errorCode: 500, errorMessage: e?.message ?: "An unknown error occurred during import"] as JSON)
            return
        }

        render([data: "Data will be imported successfully"] as JSON)
    }

    /**
     * Bind the date field value to the date object.
     *
     * @param dateObject
     * @param jsonObject
     * @param dateField
     */
    Date parseDateRequested(String date) {
        return date ? Constants.EXPIRATION_DATE_FORMATTER.parse(date) : null
    }

    Date parseDateShipped(String date) {
        return date ? Constants.DELIVERY_DATE_FORMATTER.parse(date) : null
    }

    void bindStockMovement(StockMovement stockMovement, JSONObject jsonObject) {
        // Remove attributes that cause issues in the default grails data binder
        List lineItems = jsonObject.remove("lineItems")
        List packPageItems = jsonObject.remove("packPageItems")

        // Dates aren't bound properly using default JSON binding
        if (jsonObject.containsKey("dateShipped")) {
            stockMovement.dateShipped = parseDateShipped(jsonObject.remove("dateShipped"))
        }

        if (jsonObject.containsKey("dateRequested")) {
            stockMovement.dateRequested = parseDateRequested(jsonObject.remove("dateRequested"))
        }

        // If the stocklist.id key is present and empty, then we need to remove the stocklist from the stock movement
        if (jsonObject.containsKey("stocklist.id")) {
            String stocklistId = jsonObject.remove("stocklist.id")
            stockMovement.stocklist = (stocklistId) ? Requisition.get(stocklistId) : null
        }

        // Bind the rest of the JSON attributes to the stock movement object
        log.debug "Binding line items: " + lineItems
        bindData(stockMovement, jsonObject)

        // Need to clear the existing line items so we only process the modified ones
        stockMovement.lineItems.clear()

        // Bind all line items
        if (lineItems) {
            log.info "lineItems: " + lineItems
            bindLineItems(stockMovement, lineItems)
        }

        if (packPageItems) {
            createPackPageItemsFromJson(stockMovement, packPageItems)
        }
    }


    /**
     * Bind the given line items (JSONArray) to StockMovementItem objects and add them to the given
     * StockMovement object.
     *
     * NOTE: THis method was necessary because the default data binder for Grails command objects
     * does not seem to handle nested objects very well.
     *
     * FIXME Refactor data binding
     *
     * @param stockMovement
     * @param lineItems
     */
    void bindLineItems(StockMovement stockMovement, List lineItems) {
        log.debug "line items: " + lineItems
        List<StockMovementItem> stockMovementItems = createLineItemsFromJson(stockMovement, lineItems)
        stockMovement.lineItems.addAll(stockMovementItems)
    }

    Boolean isNull(Object objectValue) {
        return objectValue == JSONObject.NULL || objectValue == null || objectValue?.equals("")
    }

    List<StockMovementItem> createLineItemsFromJson(StockMovement stockMovement, List lineItems) {
        List<StockMovementItem> stockMovementItems = new ArrayList<StockMovementItem>()
        lineItems.each { lineItem ->
            StockMovementItem stockMovementItem = new StockMovementItem()
            stockMovementItem.id = lineItem.id
            stockMovementItem.stockMovement = stockMovement

            // Required properties
            stockMovementItem.product = lineItem["product.id"] ? Product.load(lineItem["product.id"]) : null
            stockMovementItem.quantityRequested = lineItem.quantityRequested ? new BigDecimal(lineItem.quantityRequested) : null

            // Containers (optional)
            stockMovementItem.palletName = !isNull(lineItem["palletName"]) ? lineItem["palletName"] : null
            stockMovementItem.boxName = !isNull(lineItem["boxName"]) ? lineItem["boxName"] : null

            // Inventory item (optional)
            // FIXME Lookup inventory item by product, lot number, expiration date
            stockMovementItem.inventoryItem = lineItem["inventoryItem.id"] ? InventoryItem.load(lineItem["inventoryItem.id"]) : null
            stockMovementItem.lotNumber = lineItem["lotNumber"]
            stockMovementItem.expirationDate = (!isNull(lineItem["expirationDate"])) ?
                    Constants.EXPIRATION_DATE_FORMATTER.parse(lineItem["expirationDate"]) : null

            // Sort order (optional)
            stockMovementItem.sortOrder = lineItem.sortOrder && !lineItem.isNull("sortOrder") ? new Integer(lineItem.sortOrder) : null

            // Actions
            stockMovementItem.delete = lineItem.delete ? Boolean.valueOf(lineItem.delete) : Boolean.FALSE
            stockMovementItem.revert = lineItem.revert ? Boolean.valueOf(lineItem.revert) : Boolean.FALSE
            stockMovementItem.cancel = lineItem.cancel ? Boolean.valueOf(lineItem.cancel) : Boolean.FALSE
            stockMovementItem.substitute = lineItem.substitute ? Boolean.valueOf(lineItem.substitute) : Boolean.FALSE

            // When substituting a product, we need to include the new product, quantity and reason code
            stockMovementItem.newProduct = lineItem["newProduct.id"] ? Product.load(lineItem["newProduct.id"]) : null
            stockMovementItem.newQuantity = lineItem.newQuantity ? new BigDecimal(lineItem.newQuantity) : null

            // When revising quantity you need quantity revised and reason code
            stockMovementItem.quantityRevised = lineItem.quantityRevised ? new BigDecimal(lineItem.quantityRevised) : null
            stockMovementItem.reasonCode = lineItem.reasonCode
            stockMovementItem.comments = lineItem.comments

            // Update recipient
            stockMovementItem.recipient = lineItem["recipient.id"] ? Person.load(lineItem["recipient.id"]) : null

            // Pack page fields
            stockMovementItem.quantityShipped = lineItem.quantityShipped ? new BigDecimal(lineItem.quantityShipped) : null
            stockMovementItem.shipmentItemId = lineItem.shipmentItemId
            stockMovementItem.orderItemId = lineItem.orderItemId

            List splitLineItems = lineItem.splitLineItems
            if (splitLineItems) {
                stockMovementItem.splitLineItems = createLineItemsFromJson(stockMovement, splitLineItems)
            }

            stockMovementItems.add(stockMovementItem)
        }
        return stockMovementItems
    }


    List<PackPageItem> createPackPageItemsFromJson(StockMovement stockMovement, List lineItems) {
        List<PackPageItem> packPageItems = new ArrayList<PackPageItem>()
        lineItems.each { lineItem ->
            PackPageItem packPageItem = new PackPageItem()
            packPageItem.recipient = lineItem["recipient.id"] ? Person.load(lineItem["recipient.id"]) : null
            packPageItem.palletName = lineItem["palletName"]
            packPageItem.boxName = lineItem["boxName"]
            packPageItem.quantityShipped = lineItem.quantityShipped ? new BigDecimal(lineItem.quantityShipped) : null
            packPageItem.shipmentItemId = lineItem.shipmentItemId

            List splitLineItems = lineItem.splitLineItems
            if (splitLineItems) {
                packPageItem.splitLineItems = createPackPageItemsFromJson(stockMovement, splitLineItems)
            }

            packPageItems.add(packPageItem)
        }
        return packPageItems
    }
}
