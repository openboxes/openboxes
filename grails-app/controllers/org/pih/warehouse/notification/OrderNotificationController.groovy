/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse.notification

import org.apache.commons.lang3.time.DateUtils
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.api.StockMovementItem
import org.pih.warehouse.api.StockMovementType
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.RequisitionSourceType
import org.pih.warehouse.requisition.RequisitionType

class OrderNotificationController {

    def productService
    def grailsApplication
    def identifierService
    def notificationService
    def stockMovementService

    def publish = {
        String message = request.JSON.toString()
        log.info "Publishing message " + message
        String TOPIC_ARN = grailsApplication.config.awssdk.sns.order.arn
        notificationService.publish(TOPIC_ARN, message,"Demo publish")
        render "published"
    }


    def subscribe = {
        String ORDER_TOPIC_ARN = grailsApplication.config.awssdk.sns.order.arn?.toString()
        log.info "Subscribiing topic:${ORDER_TOPIC_ARN}"
        try {
            String subscribeUrl = g.createLink(uri: "/api/notifications/orders", absolute: true)
            if (grailsApplication.config.openboxes.integration.sns.orders.subscribeUrl) {
                subscribeUrl = grailsApplication.config.openboxes.integration.sns.orders.subscribeUrl
            }
            log.info "Order subscribeUrl::${subscribeUrl}"
            notificationService.subscribeTopic(ORDER_TOPIC_ARN, subscribeUrl)
        } catch (Exception e) {
            log.error(e.printStackTrace());
        }
        render "subscribed"
    }

    def handleMessage = {
        JSONObject json = request.getJSON()
        log.info "handle message ${json.toString(4)}"
        if (notificationService.confirmSubscription(json, Constants.ARN_TOPIC_ORDER_CREATE_TYPE)){
            render "subscribed"
            return
        }
        String message = json.getString("Message")
        JSONObject orderJson = new JSONObject(message)
        log.info "handle order ${orderJson.toString(4)}"
        StockMovement stockMovement = new StockMovement()
        stockMovement.sourceType = RequisitionSourceType.PAPER
        stockMovement.requestType = RequisitionType.DEFAULT
        stockMovement.stockMovementType = StockMovementType.OUTBOUND

        if(orderJson.has("id")){
            stockMovement.identifier = orderJson.getString("id")
        }

        if (orderJson.has("origin") && orderJson.getString("origin")) {
            JSONObject originJsonObject = orderJson.getJSONObject("origin")
            Location origin = Location.findByLocationNumber(originJsonObject.getString("locationNumber"))
            stockMovement.origin = origin
        }

        if (orderJson.has("destination") && orderJson.getString("destination")) {
            JSONObject destinationJsonObject = orderJson.getJSONObject("destination")
            Location destination = Location.findByLocationNumber(destinationJsonObject.getString("locationNumber"))
            stockMovement.destination = destination
        }

        if (orderJson.has("requestedBy") && orderJson.getString("requestedBy")) {
            User requestedBy = User.findByUsername(orderJson.getString("requestedBy"))
            stockMovement.requestedBy = requestedBy
        }

        if (orderJson.has("dateRequested") && orderJson.getString("dateRequested")) {
            Date dateRequested = DateUtils.parseDate(orderJson.getString("dateRequested"), "yyyy-MM-dd")
            stockMovement.dateRequested = dateRequested
        }

        if (orderJson.has("requestedDeliveryDate") && orderJson.getString("requestedDeliveryDate")) {
            Date dateRequested = DateUtils.parseDate(orderJson.getString("requestedDeliveryDate"), "yyyy-MM-dd")
            stockMovement.dateRequested = dateRequested
        }

        stockMovement.name = orderJson.has("name") ? orderJson.getString("name") :
                "${stockMovement?.origin?.locationNumber?.toUpperCase()}-${stockMovement?.destination?.locationNumber?.toUpperCase()}-${stockMovement?.dateRequested?.format("ddMMMyyyy")?.toUpperCase()}"
        stockMovement.description = stockMovement.name

        Boolean lineItemHasError = false
        JSONArray orderLineItemsArray = orderJson.getJSONArray("orderItems")
        for (JSONObject lineItem : orderLineItemsArray) {
            log.info "Reading LineItem:${lineItem} for Order"
            StockMovementItem stockMovementItem = new StockMovementItem()
            Product product = productService.findProductByExternalId(lineItem.getString("productId"))
            if (!product) {
                throw new IllegalArgumentException("Product not found with id:"+lineItem.getString("productId"))
            }
            Integer quantity = lineItem.getInt("quantity")
            stockMovementItem.product = product
            stockMovementItem.quantityRequested = quantity
            stockMovement.lineItems.add(stockMovementItem)
        }
        try {
            stockMovementService.createStockMovement(stockMovement)
        } catch (Exception e) {
            log.error "Error creating stock movement " + e.message, e
        }

        render([status: 200, text: "Order message handled successfully"])
    }
}
