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
import org.grails.web.json.JSONObject
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.User
import org.pih.warehouse.importer.CSVUtils
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.StockMovementService
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionSourceType
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.requisition.RequisitionType
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentStatusCode


class StockMovementApiController {

    def dataService
    def outboundStockMovementService
    def stockMovementService
    def stockTransferService

    def list() {
        Location destination = params.destination ? Location.get(params.destination) : null
        Location origin = params.origin ? Location.get(params.origin) : null

        StockMovementDirection direction = params.direction ? params.direction as StockMovementDirection : null
        if (destination == origin || !params.direction) {
            def message = "Direction parameter is required. Origin and destination cannot be the same."
            response.status = 400
            render([errorMessages: [message]] as JSON)
            return
        }

        StockMovement stockMovement = new StockMovement()
        stockMovement.stockMovementDirection = direction
        stockMovement.origin = origin
        stockMovement.destination = destination
        stockMovement.requestedBy = params.requestedBy ? Person.get(params.requestedBy) : null
        stockMovement.createdBy = params.createdBy ? User.get(params.createdBy) : null
        stockMovement.updatedBy = params.updatedBy ? User.get(params.updatedBy) : null
        stockMovement.receiptStatusCodes = params.receiptStatusCode ? params?.list("receiptStatusCode") as ShipmentStatusCode[] : null
        stockMovement.requisitionStatusCodes = params.requisitionStatusCode ? params?.list("requisitionStatusCode") as RequisitionStatus[] : null
        stockMovement.requestType = params.requestType ? params.requestType as RequisitionType : null
        stockMovement.sourceType = params.sourceType ? params.sourceType as RequisitionSourceType : null

        if (params.q) {
            stockMovement.identifier = "%" + params.q + "%"
            stockMovement.name = "%" + params.q + "%"
            stockMovement.description = "%" + params.q + "%"
        }

        if (params.format == 'csv') {
            params.max = null
            params.offset = null
        }

        def stockMovements = stockMovementService.getStockMovements(stockMovement, params)

        if (params.format == 'csv' && stockMovements) {
            def csv = getStockMovementsCsv(stockMovements)
            response.setHeader("Content-disposition", "attachment; filename=\"StockMovements-${new Date().format("yyyyMMdd-hhmmss")}.csv\"")
            render(contentType: "text/csv", text: csv.out.toString())
            return
        }

        render([data: stockMovements, totalCount: stockMovements?.totalCount] as JSON)
    }

    def read() {
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

    def create(StockMovement stockMovement) {
        // Detect whether inbound or outbound stock movement
        def currentLocation = Location.get(session.warehouse.id)
        StockMovement newStockMovement = stockMovementService.createStockMovement(stockMovement)
        response.status = 201
        render([data: newStockMovement] as JSON)
    }

    // TODO Remove it later once all inbound types are shipment
    // and then use endpoint above to create combined shipments
    def createCombinedShipments(StockMovement stockMovement) {
        StockMovement newStockMovement = stockMovementService.createShipmentBasedStockMovement(stockMovement)
        response.status = 201
        render([data: newStockMovement] as JSON)
    }

    /**
     * @deprecated FIXME refactor to avoid using RPC-style endpoints
     */
    def updateRequisition() { // FIXME Bind StockMovement stockMovement
        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)
        bindStockMovement(stockMovement, request.JSON)
        stockMovementService.updateStockMovement(stockMovement)
        forward(action: "read")
    }

    /**
     * @deprecated FIXME refactor to avoid using RPC-style endpoints
     */
    def updateShipment() { // FIXME Bind StockMovement stockMovement
        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)
        bindStockMovement(stockMovement, request.JSON)
        stockMovementService.updateShipment(stockMovement)
        render status: 200
    }

    /**
     * Deleting Stock Movements (Inbound and Outbound). Because there are 3 options (requisition based, shipment
     * based and order based we have to create a proper StockMovement object. First try to fetch it as outbound type,
     * then if not found as inbound type.
     * */
    def delete() {
        // Pull Outbound Stock movement (Requisition based) or Outbound or Inbound Return (Order based)
        def stockMovement = outboundStockMovementService.getStockMovement(params.id)
        // For inbound stockMovement only
        if (!stockMovement) {
            stockMovement = stockMovementService.getStockMovement(params.id)
        }

        // If still no StockMovement found, then throw 404
        if (!stockMovement) {
            def message = "Stockmovement with id ${params.id} does not exist"
            response.status = 404
            render([errorMessage: message] as JSON)
            return
        }

        if (stockMovement?.order) {
            /**
             * If stock movement has an Order, then treat it as a Return Order
             * and remove it through stockTransferService
             * */
            try {
                stockTransferService.deleteStockTransfer(stockMovement)
            } catch (Exception e) {
                def message = "${g.message(code: 'stockMovement.delete.error.message', default: 'The Stock Movement could not be deleted')}"
                response.status = 400
                render([errorMessage: message] as JSON)
                return
            }

            render status: 204
            return
        } else {
            /**
             * Otherwise treat it as a regular Stock Movement or as a Stock Request
             * */
            // TODO: Tech huddle around this area (if looking into currentLocation in API like that is ok)
            def currentLocation = Location.get(session?.warehouse?.id)
            if (stockMovement.isDeleteOrRollbackAuthorized(currentLocation)) {
                if (stockMovement?.isPending() || !stockMovement?.shipment?.currentStatus) {
                    try {
                        stockMovementService.deleteStockMovement(stockMovement)
                    } catch (Exception e) {
                        def message = "${g.message(code: 'stockMovement.delete.error.message', default: 'The Stock Movement could not be deleted')}"
                        response.status = 400
                        render([errorMessage: message] as JSON)
                        return
                    }
                } else {
                    def message = "You cannot delete a shipment with status ${stockMovement?.shipment?.currentStatus}"
                    response.status = 400
                    render([errorMessage: message] as JSON)
                    return
                }
            }
            else {
                def message = "You are not able to delete stock movement from your location."
                response.status = 400
                render([errorMessage: message] as JSON)
                return
            }
        }

        render status: 204
    }


    def status() {
        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)
        render([data: stockMovement?.status] as JSON)
    }

    def deleteStatus() {
        stockMovementService.rollbackStockMovement(params.id)
        redirect(action: "read", params: params)
    }

    /**
     * Peforms a status update on the stock movement and forwards to the read action.
     */
    def updateStatus() {
        JSONObject jsonObject = request.JSON
        log.info "update status: " + jsonObject.toString(4)
        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)
        stockMovementService.transitionStockMovement(stockMovement, jsonObject)
        render status: 200
    }


    /**
     * @deprecated FIXME refactor to avoid using RPC-style endpoints
     */
    def removeAllItems() {
        Requisition requisition = Requisition.get(params.id)
        Shipment shipment = Shipment.get(params.id)
        if (requisition) {
            stockMovementService.removeRequisitionItems(requisition)
        } else {
            stockMovementService.removeShipmentItems(shipment)
        }
        render status: 204
    }

    /**
     * @deprecated FIXME refactor to avoid using RPC-style endpoints
     */
    def reviseItems() {
        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)
        bindStockMovement(stockMovement, request.JSON)
        // First revise the items
        stockMovementService.reviseItems(stockMovement)
        // Then create missing picklist items and shipment items (previously this part was done in the stockMovementService.reviseItems,
        // but since the stockMovementService is transactional there were issues with not properly refreshed product availability
        // (old values were pulled and validation was failing)
        stockMovementService.createMissingPicklistItems(stockMovement)
        stockMovementService.createMissingShipmentItems(stockMovement)
        render status: 200
    }

    /**
     * @deprecated FIXME refactor to avoid using RPC-style endpoints
     */
    def updateItems() {
        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)
        bindStockMovement(stockMovement, request.JSON)
        stockMovement = stockMovementService.updateItems(stockMovement)
        render([data: stockMovement] as JSON)
    }

    def updateInventoryItems() {
        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)
        bindStockMovement(stockMovement, request.JSON)
        stockMovementService.updateInventoryItems(stockMovement)
        render status: 200
    }

    /**
     * @deprecated FIXME refactor to avoid using RPC-style endpoints
     */
    def updateShipmentItems() {
        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)

        JSONObject jsonObject = request.JSON
        log.debug "revise items: " + jsonObject.toString(4)

        def packPageItems = createPackPageItemsFromJson(stockMovement, jsonObject.packPageItems)

        stockMovementService.updatePackPageItems(packPageItems)

        render([data: stockMovementService.getPackPageItems(stockMovement.id, null, null)] as JSON)
    }

    def updateAdjustedItems() {
        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)
        stockMovementService.updateAdjustedItems(stockMovement, params.adjustedProduct)

        stockMovement = stockMovementService.getStockMovement(params.id)

        render([data: stockMovement] as JSON)
    }

    def createPickList() {
        stockMovementService.createPicklist(params.id)

        render status: 200
    }

    def validatePicklist() {
        stockMovementService.validatePicklist(params.id)

        render status: 200
    }

    def exportPickListItems() {
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

    def importPickListItems(ImportDataCommand command) {

        try {
            StockMovement stockMovement = stockMovementService.getStockMovement(params.id)
            List<PickPageItem> pickPageItems = stockMovementService.getPickPageItems(params.id, null, null )

            def importFile = command.importFile

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

    def getPendingRequisitionDetails() {
        Location origin = Location.get(params.origin.id)
        Product product = Product.get(params.product.id)
        def stockMovementId = params.stockMovementId

        if (!origin || !product) {
            throw new IllegalArgumentException("Both origin location and product are required!")
        }

        def pendingRequisitionDetails = stockMovementService.getPendingRequisitionDetails(origin, product, stockMovementId)
        render([data: pendingRequisitionDetails] as JSON)
    }

    /**
     * Bind the date field value to the date object.
     *
     * @param dateObject
     * @param jsonObject
     * @param dateField
     */
    private Date parseDateRequested(String date) {
        return date ? Constants.EXPIRATION_DATE_FORMATTER.parse(date) : null
    }

    private Date parseDateShipped(String date) {
        return date ? Constants.DELIVERY_DATE_FORMATTER.parse(date) : null
    }

    private void bindStockMovement(StockMovement stockMovement, JSONObject jsonObject) {
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
        if (jsonObject.containsKey("stocklist")) {
            def stocklist = jsonObject.remove("stocklist")
            stockMovement.stocklist = stocklist?.id ? Requisition.get(stocklist?.id) : null
        }

        // Bind the rest of the JSON attributes to the stock movement object
        log.debug "Binding line items: " + lineItems
        bindData(stockMovement, jsonObject)

        // Need to clear the existing line items so we only process the modified ones
        stockMovement.lineItems.clear()

        // Bind all line items
        if (lineItems) {
            log.info "binding lineItems: ${lineItems}"
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
    private void bindLineItems(StockMovement stockMovement, List lineItems) {
        log.debug "line items: " + lineItems
        List<StockMovementItem> stockMovementItems = createLineItemsFromJson(stockMovement, lineItems)
        stockMovement.lineItems.addAll(stockMovementItems)
    }

    private Boolean isNull(Object objectValue) {
        return objectValue == null || objectValue?.equals("")
    }

    private List<StockMovementItem> createLineItemsFromJson(StockMovement stockMovement, List lineItems) {
        List<StockMovementItem> stockMovementItems = new ArrayList<StockMovementItem>()
        lineItems.each { lineItem ->
            StockMovementItem stockMovementItem = new StockMovementItem()
            stockMovementItem.id = !isNull(lineItem["id"]) ? lineItem["id"] : null
            stockMovementItem.stockMovement = stockMovement

            // Required properties
            stockMovementItem.product = lineItem?.product?.id ? Product.load(lineItem?.product?.id) : null
            stockMovementItem.quantityRequested = lineItem.quantityRequested ? new BigDecimal(lineItem.quantityRequested) : null

            // Containers (optional)
            stockMovementItem.palletName = !isNull(lineItem["palletName"]) ? lineItem["palletName"] : null
            stockMovementItem.boxName = !isNull(lineItem["boxName"]) ? lineItem["boxName"] : null

            // Inventory item (optional)
            // FIXME Lookup inventory item by product, lot number, expiration date
            stockMovementItem.inventoryItem = lineItem?.inventoryItem?.id ? InventoryItem.load(lineItem?.inventoryItem?.id) : null
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
            stockMovementItem.newProduct = lineItem?.newProduct?.id ? Product.load(lineItem?.newProduct?.id) : null
            stockMovementItem.newQuantity = lineItem.newQuantity ? new BigDecimal(lineItem.newQuantity) : null

            // When revising quantity you need quantity revised and reason code
            stockMovementItem.quantityRevised = lineItem.quantityRevised ? new BigDecimal(lineItem.quantityRevised) : null
            stockMovementItem.reasonCode = lineItem.reasonCode
            stockMovementItem.comments = lineItem.comments

            // Update recipient
            stockMovementItem.recipient = lineItem?.recipient?.id ? Person.load(lineItem?.recipient?.id) : null

            // Pack page fields
            stockMovementItem.quantityShipped = lineItem.quantityShipped ? new BigDecimal(lineItem.quantityShipped) : null
            stockMovementItem.shipmentItemId = lineItem.shipmentItemId
            stockMovementItem.orderItemId = lineItem.orderItemId

            stockMovementItem.quantityCounted = lineItem.quantityCounted

            List splitLineItems = lineItem.splitLineItems
            if (splitLineItems) {
                stockMovementItem.splitLineItems = createLineItemsFromJson(stockMovement, splitLineItems)
            }

            stockMovementItems.add(stockMovementItem)
        }
        return stockMovementItems
    }


    private List<PackPageItem> createPackPageItemsFromJson(StockMovement stockMovement, List lineItems) {
        List<PackPageItem> packPageItems = new ArrayList<PackPageItem>()
        lineItems.each { lineItem ->
            PackPageItem packPageItem = new PackPageItem()
            packPageItem.recipient = lineItem.recipient?.id ? Person.load(lineItem.recipient?.id) : null
            packPageItem.palletName = lineItem.palletName
            packPageItem.boxName = lineItem.boxName
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

    /**
     * Action to get shipped and not yet received items as csv
     * */
    def shippedItems() {
        if (!params.destination) {
            def message = "Destination parameter cannot be the empty"
            response.status = 400
            render([errorMessages: [message]] as JSON)
            return
        }

        def destination = Location.get(params.destination)

        if (!destination) {
            def message = "Destination does not exist"
            response.status = 404
            render([errorMessage: message] as JSON)
            return
        }

        // Get shipments that are shipped and not fully received
        def statuses = [ShipmentStatusCode.SHIPPED, ShipmentStatusCode.PARTIALLY_RECEIVED]
        def shipments = Shipment.findAllByDestinationAndCurrentStatusInList(destination, statuses)

        def shipmentItems = []
        shipments.each { Shipment shipment ->
            shipment.shipmentItems.findAll { it.quantityRemaining > 0 }.groupBy {
                it.product
            }.each { product, value ->
                shipmentItems << [
                    productCode         : product.productCode,
                    productName         : product.name,
                    quantity            : value.sum { it.quantityRemaining },
                    expectedShippingDate: formatDate(date: shipment.expectedShippingDate, format: "dd-MMM-yy"),
                    expectedDeliveryDate: formatDate(date: shipment.expectedDeliveryDate, format: "dd-MMM-yy"),
                    shipmentNumber      : shipment.shipmentNumber,
                    shipmentName        : shipment.name,
                    origin              : shipment.origin,
                    destination         : shipment.destination,
                ]
            }
        }

        if (shipmentItems) {
            def csv = getShipmentItemsCsv(shipmentItems)
            def date = new Date()
            response.contentType = "text/csv"
            response.setHeader("Content-disposition", "attachment; filename=\"Items shipped not received_${destination.name}_${date.format("yyyyMMdd-hhmmss")}.csv\"")
            render(contentType: "text/csv", text: csv.out.toString())
            return
        } else {
            def message = "No shipment items found"
            response.status = 404
            render([errorMessage: message] as JSON)
            return
        }
    }

    /**
     * Action to get pending requisition items as csv
     * */
    def pendingRequisitionItems() {
        if (!params.origin) {
            def message = "Origin parameter cannot be the empty"
            response.status = 400
            render([errorMessages: [message]] as JSON)
            return
        }

        def origin = Location.get(params.origin)

        if (!origin) {
            def message = "Origin does not exist"
            response.status = 404
            render([errorMessage: message] as JSON)
            return
        }

        def pendingRequisitionItems = stockMovementService.getPendingRequisitionItems(origin)

        if (pendingRequisitionItems) {
            def csv = getPendingRequisitionItemsCsv(pendingRequisitionItems)
            def date = new Date()
            response.contentType = "text/csv"
            response.setHeader("Content-disposition", "attachment; filename=\"PendingShipmentItems-${new Date().format("yyyyMMdd-hhmmss")}.csv\"")
            render(contentType: "text/csv", text: csv.out.toString())
            return
        } else {
            def message = "No pending requisition items found"
            response.status = 404
            render([errorMessage: message] as JSON)
            return
        }
    }

    def getShipmentItemsCsv(List shipmentItems) {
        def csv = CSVUtils.getCSVPrinter()
        csv.printRecord(
            "Code",
            "Product Name",
            "Quantity Incoming",
            "Expected Shipping Date",
            "Expected Delivery Date",
            "Shipment Number",
            "Shipment Name",
            "Origin",
            "Destination",
        )

        shipmentItems.each { shipmentItem ->
            csv.printRecord(
                shipmentItem.productCode,
                shipmentItem.productName,
                shipmentItem.quantity,
                shipmentItem.expectedShippingDate,
                shipmentItem.expectedDeliveryDate,
                shipmentItem.shipmentNumber,
                shipmentItem.shipmentName,
                shipmentItem.origin,
                shipmentItem.destination,
            )
        }

        return csv
    }

    def getPendingRequisitionItemsCsv(List pendingRequisitionItems) {
        def csv = CSVUtils.getCSVPrinter()
        csv.printRecord(
            "Shipment Number",
            "Description",
            "Destination",
            "Status",
            "Product Code",
            "Product",
            "Qty Picked",
        )

        pendingRequisitionItems.each { requisitionItem ->
            def quantityPicked = requisitionItem?.totalQuantityPicked()
            if (quantityPicked) {
                csv.printRecord(
                    requisitionItem?.requisition?.requestNumber,
                    requisitionItem?.requisition?.description ?: '',
                    requisitionItem?.requisition?.destination,
                    requisitionItem?.requisition?.status,
                    requisitionItem?.product?.productCode,
                    requisitionItem?.product?.name,
                    quantityPicked,
                )
            }
        }

        return csv
    }

    def getStockMovementsCsv(List stockMovements) {
        def csv = CSVUtils.getCSVPrinter()
        csv.printRecord(
            "Status",
            "Receipt Status",
            "Identifier",
            "Name",
            "Origin",
            "Destination",
            "Stocklist",
            "Requested by",
            "Date Requested",
            "Date Created",
            "Date Shipped",
        )

        stockMovements?.each { sm ->
            csv.printRecord(
                sm.status,
                sm.shipment?.status,
                sm.identifier,
                sm.description,
                sm.origin?.name ?: "",
                sm.destination?.name ?: "",
                sm.stocklist?.name ?: "",
                sm.requestedBy ?: warehouse.message(code: 'default.none.label'),
                sm.dateRequested.format("MM-dd-yyyy") ?: "",
                sm.requisition?.dateCreated?.format("MM-dd-yyyy") ?: "",
                sm.shipment?.expectedShippingDate?.format("MM-dd-yyyy") ?: "",
            )
        }

        return csv
    }

    def shipmentStatusCodes() {
        def options = ShipmentStatusCode.list()?.collect {
            [
                    id: it.name,
                    value: it.name,
                    label: "${g.message(code: 'enum.ShipmentStatusCode.' + it.name)}",
                    variant: it.variant.name
            ]
        }
        render([data: options] as JSON)
    }

    def requisitionStatusCodes() {
        def options = RequisitionStatus.listOutboundOptions()?.collect {
            [
                    id: it.name(),
                    value: it.name(),
                    label: "${g.message(code: 'enum.RequisitionStatus.' + it.name())}",
                    variant: it.variant.name
            ]
        }
        render([data: options] as JSON)
    }
}
