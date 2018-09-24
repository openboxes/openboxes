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
import org.codehaus.groovy.grails.web.json.JSONObject
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.StockMovementService
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.RequisitionStatus

import java.text.DateFormat
import java.text.SimpleDateFormat

class StockMovementApiController {

    StockMovementService stockMovementService

    def list = {
        int max = Math.min(params.max ? params.int('max') : 10, 1000)
        int offset = params.offset? params.int("offset") : 0
        def stockMovements = stockMovementService.getStockMovements(max, offset)
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
        render ([data:stockMovements] as JSON)
    }

    def read = {
        StockMovement stockMovement = stockMovementService.getStockMovement(params.id, params.stepNumber)

        // FIXME Debugging
        JSONObject jsonObject = new JSONObject(stockMovement.toJson())

        log.info "read " + jsonObject.toString(4)
        render ([data:stockMovement] as JSON)
    }

    def create = { StockMovement stockMovement ->

        JSONObject jsonObject = request.JSON
        log.info "create " + jsonObject.toString(4)

        stockMovement = stockMovementService.createStockMovement(stockMovement)
        response.status = 201
        render ([data:stockMovement] as JSON)
	}

    def update = { //StockMovement stockMovement ->

        JSONObject jsonObject = request.JSON
        log.info "update: " + jsonObject.toString(4)

        // Bind all other properties to stock movement
        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)
        if (!stockMovement) {
            stockMovement = new StockMovement()
        }

        bindStockMovement(stockMovement, jsonObject)
        stockMovementService.updateStockMovement(stockMovement)

        forward(action: "read")
    }

    def delete = {
        stockMovementService.deleteStockMovement(params.id)
        render status: 204
    }


    def status = {
        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)
        render ([data:stockMovement?.status] as JSON)
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

        Boolean statusOnly =
                jsonObject.containsKey("statusOnly") ? jsonObject.getBoolean("statusOnly") : false

        Boolean clearPicklist =
                jsonObject.containsKey("clearPicklist") ? jsonObject.getBoolean("clearPicklist") : false

        Boolean createPicklist =
                jsonObject.containsKey("createPicklist") ? jsonObject.getBoolean("createPicklist") : false

        RequisitionStatus status =
                jsonObject.containsKey("status") ? jsonObject.status as RequisitionStatus : null

        Boolean rollback =
                jsonObject.containsKey("rollback") ? jsonObject.getBoolean("rollback") : false

        if (status && statusOnly) {
            stockMovementService.updateStatus(params.id, status)
        }
        else {
            if (rollback) {
                stockMovementService.rollbackStockMovement(params.id)
            }

            if (status) {
                switch (status) {
                    case RequisitionStatus.CREATED:
                        break;
                    case RequisitionStatus.EDITING:
                        break;
                    case RequisitionStatus.VERIFYING:
                        break;
                    case RequisitionStatus.PICKING:
                        if (clearPicklist) stockMovementService.clearPicklist(stockMovement)
                        if (createPicklist) stockMovementService.createPicklist(stockMovement)
                        break;
                    case RequisitionStatus.CHECKING:
                        stockMovementService.createOrUpdateShipment(stockMovement, true)
                        break;
                    case RequisitionStatus.PICKED:
                        stockMovementService.createOrUpdateShipment(stockMovement, false)
                        break;
                    case RequisitionStatus.ISSUED:
                        stockMovementService.sendStockMovement(params.id)
                        break;
                    default:
                        throw new IllegalArgumentException("Cannot update status with invalid status ${jsonObject.status}")
                        break;

                }
                // If the dependent actions were updated properly then we can update the
                stockMovementService.updateStatus(params.id, status)
            }
        }
        forward(action: "read")
    }

    /**
     * Bind the date field value to the date object.
     *
     * @param dateObject
     * @param jsonObject
     * @param dateField
     */
    Date parseDate(String date) {
        return date ? Constants.EXPIRATION_DATE_FORMATTER.parse(date) : null
    }

    void bindStockMovement(StockMovement stockMovement, JSONObject jsonObject) {
        // Remove attributes that cause issues in the default grails data binder
        List lineItems = jsonObject.remove("lineItems")

        // Dates aren't bound properly using default JSON binding
        if (jsonObject.containsKey("dateShipped")) {
            stockMovement.dateShipped = parseDate(jsonObject.remove("dateShipped"))
        }

        if (jsonObject.containsKey("dateRequested")) {
            stockMovement.dateRequested = parseDate(jsonObject.remove("dateRequested"))
        }

        // Bind the rest of the JSON attributes to the stock movement object
        log.info "Binding line items: " + lineItems
        bindData(stockMovement, jsonObject)

        // Bind all line items
        if (lineItems) {
            // Need to clear the existing line items so we only process the modified ones
            stockMovement.lineItems.clear()
            bindLineItems(stockMovement, lineItems)
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
        log.info "line items: " + lineItems
        List<StockMovementItem> stockMovementItems = createLineItemsFromJson(stockMovement, lineItems)
        stockMovement.lineItems.addAll(stockMovementItems)
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
            stockMovementItem.palletName = lineItem["palletName"]
            stockMovementItem.boxName = lineItem["boxName"]

            // Inventory item (optional)
            // FIXME Lookup inventory item by product, lot number, expiration date
            stockMovementItem.inventoryItem = lineItem["inventoryItem.id"] ? InventoryItem.load(lineItem["inventoryItem.id"]) : null
            stockMovementItem.lotNumber = lineItem["lotNumber"]
            stockMovementItem.expirationDate = !(lineItem["expirationDate"] == JSONObject.NULL || lineItem["expirationDate"] == null) ?
                    Constants.EXPIRATION_DATE_FORMATTER.parse(lineItem["expirationDate"]) : null

            // Sort order (optional)
            stockMovementItem.sortOrder = lineItem.sortOrder && !lineItem.isNull("sortOrder") ? new Integer(lineItem.sortOrder) : null

            // Actions
            stockMovementItem.delete = lineItem.delete ? Boolean.parseBoolean(lineItem.delete):Boolean.FALSE
            stockMovementItem.revert = lineItem.revert ? Boolean.parseBoolean(lineItem.revert):Boolean.FALSE
            stockMovementItem.cancel = lineItem.cancel ? Boolean.parseBoolean(lineItem.cancel):Boolean.FALSE
            stockMovementItem.substitute = lineItem.substitute ? Boolean.parseBoolean(lineItem.substitute):Boolean.FALSE

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
            List splitLineItems = lineItem.splitLineItems
            if (splitLineItems) {
                stockMovementItem.splitLineItems = createLineItemsFromJson(stockMovement, splitLineItems)
            }

            stockMovementItems.add(stockMovementItem)
        }
        return stockMovementItems
    }

}
