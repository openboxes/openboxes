/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse.integration

import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.api.StockMovementItem
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.shipping.Container

class EventService {

    boolean transactional = true

    def grailsApplication
    def notificationService
    def stockMovementService

    void publishStockMovementStatusEvent(StockMovement stockMovement){
        log.info "Publish status change event " + stockMovement.toJson()
        String TOPIC_ARN = grailsApplication.config.awssdk.sns.order.status
        JSONObject notifyJson = new JSONObject()
        notifyJson.put("id", stockMovement.identifier?:JSONObject.NULL)
        notifyJson.put("locationNumber", stockMovement?.origin?.locationNumber?:JSONObject.NULL)
        notifyJson.put("status", stockMovement.status?:JSONObject.NULL)

        // Add order items
        JSONArray orderItems = new JSONArray()
        stockMovement.lineItems?.each { StockMovementItem lineItem ->
            // Need to do lookup here because the line item is what has been sent in the form submit
            StockMovementItem stockMovementItem = stockMovementService.getStockMovementItem(lineItem.id)

            // We need to get some values from the original (parent) requisition item in the case of modifications and substitutions
            RequisitionItem originalRequisitionItem = stockMovementItem?.requisitionItem?.parentRequisitionItem ?: stockMovementItem?.requisitionItem

            JSONObject orderItem = new JSONObject()
            orderItem.put("id", originalRequisitionItem?.description ?: JSONObject.NULL)
            orderItem.put("acceptedQuantity", stockMovementItem.quantityRequired ?: 0)
            orderItem.put("quantityRequested", originalRequisitionItem?.quantity ?: 0)
            orderItem.put("quantityRevised", stockMovementItem.quantityRevised ?: 0)
            orderItem.put("quantityPicked", stockMovementItem.quantityPicked ?: 0)
            orderItem.put("quantityShipped", stockMovementItem.quantityShipped ?: 0)
            orderItems.add(orderItem)
        }
        notifyJson.put("orderItems", orderItems)

        // Add packages
        JSONArray packages = new JSONArray()
        stockMovement?.shipment?.containers.each { Container container ->
            JSONObject jsonObject = new JSONObject()
            jsonObject.put("identifier", container?.containerNumber?:JSONObject.NULL)
            jsonObject.put("type", container?.containerType?.name?:JSONObject.NULL)
            jsonObject.put("length", container?.length?:JSONObject.NULL)
            jsonObject.put("width", container?.width?:JSONObject.NULL)
            jsonObject.put("height", container?.height?:JSONObject.NULL)
            jsonObject.put("volumeUnits", container?.volumeUnits?:JSONObject.NULL)
            jsonObject.put("weight", container?.weight?:JSONObject.NULL)
            jsonObject.put("weightUnits", container?.weightUnits?:JSONObject.NULL)
            packages.add(jsonObject)
        }
        notifyJson.put("packages", packages)
        log.info "Publishing stock movement status update ${notifyJson.toString(4)}"
        notificationService.publish(TOPIC_ARN, notifyJson.toString(), "Order Status")
    }
}
