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
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionSourceType
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.requisition.RequisitionType

class OrderNotificationController {

    def grailsApplication
    def identifierService
    def notificationService
    def productService

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
        Requisition requisition = new Requisition()

        requisition.status = RequisitionStatus.CREATED
        requisition.type = RequisitionType.DEFAULT
        requisition.sourceType = RequisitionSourceType.PAPER
        requisition.requestNumber = identifierService.generateRequisitionIdentifier()

        if (orderJson.has("origin") && orderJson.getString("origin")) {
            JSONObject originJsonObject = orderJson.getJSONObject("origin")
            Location origin = Location.findByLocationNumber(originJsonObject.getString("locationNumber"))
            requisition.origin = origin
        }

        if (orderJson.has("destination") && orderJson.getString("destination")) {
            JSONObject destinationJsonObject = orderJson.getJSONObject("destination")
            Location destination = Location.findByLocationNumber(destinationJsonObject.getString("locationNumber"))
            requisition.destination = destination
        }

        if (orderJson.has("requestedBy") && orderJson.getString("requestedBy")) {
            User requestedBy = User.findByUsername(orderJson.getString("requestedBy"))
            requisition.requestedBy = requestedBy
        }

        if (orderJson.has("dateRequested") && orderJson.getString("dateRequested")) {
            Date dateRequested = DateUtils.parseDate(orderJson.getString("dateRequested"), "yyyy-MM-dd")
            requisition.dateRequested = dateRequested
        }

        if (orderJson.has("requestedDeliveryDate") && orderJson.getString("requestedDeliveryDate")) {
            Date dateRequested = DateUtils.parseDate(orderJson.getString("requestedDeliveryDate"), "yyyy-MM-dd")
            requisition.dateRequested = dateRequested
        }

        requisition.name = orderJson.has("name") ? orderJson.getString("name") :
                "${requisition?.origin?.locationNumber?.toUpperCase()}-${requisition?.destination?.locationNumber?.toUpperCase()}-${requisition?.dateRequested?.format("ddMMMyyyy")?.toUpperCase()}"
        requisition.description = requisition.name

        Boolean lineItemHasError = false
        JSONArray orderLineItemsArray = orderJson.getJSONArray("orderItems")
        for (int lIndex = 0; lIndex < orderLineItemsArray?.length(); lIndex++) {
            JSONObject lineItem = orderLineItemsArray.getJSONObject(lIndex)
            log.info "Reading LineItem:${lineItem} for Order"
            try {
                RequisitionItem requisitionItem = new RequisitionItem()
                Product product = productService.findProductByExternalId(lineItem.getString("productId"))
                if (!product) {
                    throw new IllegalArgumentException("Product not found with id:"+lineItem.getString("productId"))
                }
                Integer quantity = lineItem.getInt("quantity")
                requisitionItem.product = product
                requisitionItem.quantity = quantity
                requisitionItem.quantityApproved = quantity
                requisition.addToRequisitionItems(requisitionItem)
            } catch (Exception e) {
                log.error "Error ${lineItem}", e
                lineItemHasError = true
            }
        }
        try {
            if (!lineItemHasError) {
                if (requisition.validate() && !requisition.hasErrors()) {
                    requisition.save(flush: true, failOnError: true)
                } else {
                    requisition?.errors?.allErrors?.each {
                        log.error("ERR:${it}")
                    }
                }
            }
        } catch (Exception ex) {
            log.error "Error:${ex.printStackTrace()} in saving ORDER:${ex?.message}"
        }

    }
}
