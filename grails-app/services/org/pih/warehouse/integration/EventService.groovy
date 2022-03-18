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
import org.pih.warehouse.shipping.Container

class EventService {

    boolean transactional = true

    def notificationService
    def grailsApplication

    void publishStockMovementStatusEvent(StockMovement stockMovement){
        log.info "Publish status change event " + stockMovement.toJson()
        String TOPIC_ARN = grailsApplication.config.awssdk.sns.order.status
        JSONObject notifyJson = new JSONObject()
        notifyJson.put("id", stockMovement.identifier)
        notifyJson.put("locationNumber", stockMovement?.origin?.locationNumber)
        notifyJson.put("status", stockMovement.status)

        // Add order items
        JSONArray orderItems = new JSONArray()
        stockMovement.lineItems?.each { StockMovementItem stockMovementItem ->
            JSONObject orderItem = new JSONObject()
            orderItem.put("id", stockMovementItem.description)
            orderItem.put("acceptedQuantity", stockMovementItem.quantityRequired)
            orderItem.put("quantityRequested", stockMovementItem.quantityRequested)
            orderItem.put("quantityRevised", stockMovementItem.quantityRevised)
            orderItem.put("quantityPicked", stockMovementItem.quantityPicked)
            orderItem.put("quantityShipped", stockMovementItem.quantityShipped)
            orderItems.add(orderItem)
        }
        notifyJson.put("orderItems", orderItems)

        // Add packages
        JSONArray packages = new JSONArray()
        stockMovement?.shipment?.containers.each { Container container ->
            JSONObject jsonObject = new JSONObject()
            jsonObject.put("identifier", container?.containerNumber)
            jsonObject.put("type", container?.containerType?.name)
            jsonObject.put("length", container?.length)
            jsonObject.put("width", container?.width)
            jsonObject.put("height", container?.height)
            jsonObject.put("weight", container?.weight)
            packages.add(jsonObject)
        }
        notifyJson.put("packages", packages)
        log.info "Publishing stock movement status update ${notifyJson.toString(4)}"
        notificationService.publish(TOPIC_ARN, notifyJson.toString(), "Order Status")
    }
}
