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
import org.pih.warehouse.core.Location
import org.pih.warehouse.report.NotificationService

class EventService {

    boolean transactional = true

    def notificationService
    def grailsApplication

    void publishStockMovementStatusEvent(StockMovement stockMovement, Location origin){
        String TOPIC_ARN = grailsApplication.config.awssdk.sns.order.status
        JSONObject notifyJson = null
        notifyJson = new JSONObject()
        notifyJson.put("id", stockMovement.id)
        notifyJson.put("locationNumber", origin.locationNumber)
        notifyJson.put("status", stockMovement.status)
        JSONArray orderLineItems = new JSONArray()
        stockMovement.lineItems?.each { StockMovementItem stockMovementItem ->
            JSONObject orderItem = new JSONObject()
            orderItem.put("id", stockMovementItem.id)
            orderItem.put("acceptedQuantity", stockMovementItem.quantityPicked)
            orderLineItems.add(orderItem)
        }
        notifyJson.put("orderItems", orderLineItems)
        log.info "notifyJson::${notifyJson}"
        notificationService.publish(TOPIC_ARN, notifyJson.toString(), "Order Status")
    }
}
