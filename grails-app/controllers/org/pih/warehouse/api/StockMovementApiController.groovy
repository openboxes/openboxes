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
import org.grails.web.json.JSONObject
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.DateUtil
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.DocumentService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationService
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.StockMovementParamsCommand
import org.pih.warehouse.core.User
import org.pih.warehouse.core.UserService
import org.pih.warehouse.importer.CSVUtils
import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.OutboundStockMovementService
import org.pih.warehouse.inventory.StockMovementService
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionSourceType
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.requisition.RequisitionType
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentService
import org.pih.warehouse.shipping.ShipmentStatusCode
import org.pih.warehouse.stockTransfer.StockTransferService


class StockMovementApiController {

    OutboundStockMovementService outboundStockMovementService
    StockMovementService stockMovementService
    StockTransferService stockTransferService
    UserService userService
    DocumentService documentService
    LocationService locationService
    ShipmentService shipmentService

    def list() {
        Location destination = params.destination ? Location.get(params.destination) : null
        Location origin = params.origin ? Location.get(params.origin) : null
        params.isRequestApprover = userService.isUserInAllRoles(session?.user?.id, [RoleType.ROLE_REQUISITION_APPROVER], origin?.id)

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
        stockMovement.approvers = params.approver ? User.getAll(params.list("approver"))?.findAll{ it } : null

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

    def read(StockMovementParamsCommand command) {
        StockMovement stockMovement = stockMovementService.getStockMovement(command.id)
        Integer totalCount = stockMovement.lineItems.size()

        // FIXME this should happen in the service
        switch (OutboundWorkflowState.fromStepNumber(command.stepNumber)) {
            case OutboundWorkflowState.PICK_ITEMS:
                if (command.refreshPicklistItems) {
                    stockMovementService.allocatePicklistItems(stockMovement.requisition.requisitionItems?.asList())
                }
                totalCount = stockMovementService.getPickPageItems(command.id, null, null).size()
                break
            case OutboundWorkflowState.PACK_ITEMS:
                totalCount = stockMovementService.getPackPageItems(command.id, null, null).size()
                break
            case OutboundWorkflowState.SEND_SHIPMENT:
                if (!stockMovement.origin.isSupplier() && stockMovement.origin.supports(ActivityCode.MANAGE_INVENTORY)) {
                    totalCount = stockMovementService.getPackPageItems(command.id, null, null).size()
                }
                break
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
        redirect(action: "read", params: params)
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
                // If a shipment has items with invoice quantity greater than 0,
                // it means there is a connected invoice, and we cannot delete stock movement
                if (stockMovement?.shipment?.hasInvoicedItem()) {
                    String message = g.message(
                            code: 'stockMovement.delete.error.message',
                            default: 'The Stock Movement could not be deleted'
                    )
                    response.status = 400
                    render([errorMessage: message] as JSON)
                    return
                }

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

    private void bindStockMovement(StockMovement stockMovement, JSONObject jsonObject) {
        // Remove attributes that cause issues in the default grails data binder
        List lineItems = jsonObject.remove("lineItems")
        List packPageItems = jsonObject.remove("packPageItems")

        // Dates aren't bound properly using default JSON binding
        if (jsonObject.containsKey("dateShipped")) {
            stockMovement.dateShipped = DateUtil.asDate(jsonObject.remove("dateShipped") as String)
        }

        if (jsonObject.containsKey("dateRequested")) {
            stockMovement.dateRequested = DateUtil.asDate(jsonObject.remove("dateRequested") as String)
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
            stockMovementItem.expirationDate = DateUtil.asDate(lineItem["expirationDate"] as String)

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
        // Location for checking if approval is required
        Location currentLocation = Location.get(session.warehouse.id)
        // Indicator deciding if we should get statuses for request or for normal outbound
        Boolean isElectronicType = params.get("sourceType") == RequisitionSourceType.ELECTRONIC.name()

        List<String> statuses = getOutboundRequisitionStatusCodes(currentLocation.isApprovalRequired(), isElectronicType).collect(RequisitionStatus.mapToOption)

        render([data: statuses] as JSON)
    }

    // Function for getting appropriate filter options based on current list and supporting requests approval
    List<RequisitionStatus> getOutboundRequisitionStatusCodes(Boolean isApprovalRequired, Boolean isElectronicType) {
        // If a location doesn't have approval required, return listOutboundOptions when we are on outbound list
        // but if we are on the requests list return listRequestsOptions
        if (!isApprovalRequired) {
            return isElectronicType ? RequisitionStatus.listRequestOptions() : RequisitionStatus.listOutboundOptions()
        }
        // If request approval is required, check what type of list it is and return appropriate statuses
        if (isElectronicType) {
            boolean isRequestApprover = userService.isUserInAllRoles(session?.user?.id, [RoleType.ROLE_REQUISITION_APPROVER], session.warehouse?.id)
            if (isRequestApprover) {
                return RequisitionStatus.listRequestOptionsWhenApprovalRequired()
            }

            // If request approval is required, but the user is not Approver user
            return RequisitionStatus.listRequestOptionsWhenNonApprover()
        }

        return RequisitionStatus.listOutboundOptionsWhenApprovalRequired()
    }

    def rollbackApproval = {
        String stockMovementId = params.get("id")
        try {
            stockMovementService.rollbackApproval(stockMovementId)
        } catch (Exception e) {
            log.error("Unable to rollback stock movement with ID ${stockMovementId}: " + e.message)
            String errorMessage = g.message(
                    code: "request.rollbackApproval.error.message",
                    default: "Unable to rollback approval: ${e.message}",
                    args: [e.message]
            )
            response.status = 500
            render([errorMessage: errorMessage] as JSON)
        }
        render(status: 200)
    }

    def downloadPackingListTemplate() {
        try {
            String filename = "completedPackingList.xls"
            File file = documentService.findFile("templates/" + filename)
            response.setHeader('Content-disposition', "attachment; filename=\"${filename}\"")
            response.outputStream << file.bytes
            response.outputStream.flush()
        } catch (FileNotFoundException e) {
            render status: 404
        }
    }

    def packingLocation() {
        JSONObject jsonObject = request.JSON
        String packingLocationId = jsonObject["packingLocation.id"]
        Location packingLocation = packingLocationId ? locationService.getLocation(packingLocationId) : null
        if (!packingLocation) {
            throw new ObjectNotFoundException(packingLocationId, Location.class)
        }

        if (!packingLocation.supports(ActivityCode.PACK_STOCK)) {
            throw new IllegalArgumentException("Location ${packingLocation?.name} should be an internal location with supported activities that include ${ActivityCode.PACK_STOCK}")
        }

        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)
        if (!stockMovement) {
            throw new ObjectNotFoundException(params.id, StockMovement.class)
        }

        // If the packing location is already set and doesn't match the given packing location
        if (stockMovement?.packingLocation && stockMovement?.packingLocation != packingLocation) {
            throw new IllegalArgumentException("Stock movement ${stockMovement.identifier} is already assigned to packing location ${stockMovement?.packingLocation?.name}")
        }

        // Setting packing location
        stockMovement.shipment.setPackingScheduled(packingLocation, new Date(), User.load(session.user.id))

        shipmentService.saveShipment(stockMovement.shipment)

        render status: 200
    }
}
