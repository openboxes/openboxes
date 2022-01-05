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
import org.pih.warehouse.report.NotificationService
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import util.StringUtil

class OrderNotificationController {
    def notificationService
    def grailsApplication

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
        JSONObject json = request.getJSON() as JSONObject
        log.info "Create order JSON:${json}"
        if (notificationService.confirmSubscription(json, Constants.ARN_TOPIC_ORDER_CREATE_TYPE)){
            render "subscribed"
            return
        }
        String message = json.getString("Message")
        JSONObject orderJson = new JSONObject(message)
        log.info "ORDER JSON ${orderJson}"
        Requisition requisition = new Requisition()
        if (orderJson.has("name")) {
            requisition.name = orderJson.getString("name")
        } else {
            requisition.name = "DEFAULT"
        }
        try {
            if (orderJson.has("origin") && orderJson.getString("origin")) {
                JSONObject originJsonObject = orderJson.getJSONObject("origin")
                log.info "Origin location Json:${originJsonObject}"
                Location location = Location.findByLocationNumber(originJsonObject.getString("locationNumber"))
                log.info "Origin location::${location}"
                requisition.origin = location
            }
        } catch (Exception ex) {
            log.error "Error in reading origin from order json:${orderJson}"
        }
        try {
            if (orderJson.has("destination") && orderJson.getString("destination")) {
                JSONObject destinationJsonObject = orderJson.getJSONObject("destination")
                log.info "Destination location Json:${destinationJsonObject}"
                Location location = Location.findByLocationNumber(destinationJsonObject.getString("locationNumber"))
                log.info "Destination location::${location}"
                requisition.destination = location
            }
        } catch (Exception ex) {
            log.error "Error:${ex.printStackTrace()} in reading destination from order json:${orderJson}"
        }
        try {
            if (orderJson.has("requestedBy") && orderJson.getString("requestedBy")) {
                User requestedBy = User.findByUsername(orderJson.getString("requestedBy"))
                if (!requestedBy) {
                    JSONObject userMapping = StringUtil.toJson(grailsApplication.config?.openboxes?.integration?.userMapping ?: null)
                    log.info "UserMapping:${userMapping}"
                    if(userMapping) {
                        String requestedByString = orderJson.getString("requestedBy")
                        if(userMapping.has(requestedByString)) {
                            String username = userMapping.getString(requestedByString)
                            log.info "RequestedByString:${requestedByString}, userMapping[$requestedByString]:${username}"
                            requestedBy = User.findByUsername(username)
                        }
                    }
                }
                requisition.requestedBy = requestedBy
            }
        } catch (Exception ex) {
            log.error "Error:${ex.printStackTrace()} in reading requestedBy from order json:${orderJson}"
        }
        try {
            if (orderJson.has("dateRequested") && orderJson.getString("dateRequested")) {
                Date dateRequested = DateUtils.parseDate(orderJson.getString("dateRequested"), "yyyy-MM-dd")
                requisition.dateRequested = dateRequested
            } else {
                requisition.dateRequested = new Date()
            }
        } catch (Exception ex) {
            log.error "Error:${ex.printStackTrace()} in reading dateRequested from order json:${orderJson}"
            requisition.dateRequested = new Date()
        }
        try {
            if (orderJson.has("requestedDeliveryDate") && orderJson.getString("requestedDeliveryDate")) {
                Date dateRequested = DateUtils.parseDate(orderJson.getString("requestedDeliveryDate"), "yyyy-MM-dd")
                requisition.dateRequested = dateRequested
            } else {
                requisition.requestedDeliveryDate = new Date()
            }
        } catch (Exception ex) {
            log.error "Error in reading dateRequested from order json:${orderJson}"
            requisition.requestedDeliveryDate = new Date()
        }
        Boolean lineItemHasError = false
        JSONArray orderLineItemsArray = orderJson.getJSONArray("orderItems")
        for (int lIndex = 0; lIndex < orderLineItemsArray?.length(); lIndex++) {
            JSONObject lineItem = orderLineItemsArray.getJSONObject(lIndex)
            log.info "Reading LineItem:${lineItem} for Order"
            try {
                RequisitionItem requisitionItem = new RequisitionItem()
                // FIXME Should use a generic approach (e.g. product attribute) for external product IDs
                Product product = Product.findByUpc(lineItem.getString("productId"))
                if (!product) {
                    throw new IllegalArgumentException("Product not found with id:"+lineItem.getString("productId"))
                }
                log.info "product:${product} for lineItem index:${lIndex}"
                Integer quantity = lineItem.getInt("quantity")
                requisitionItem.product = product
                requisitionItem.quantity = quantity
                requisition.addToRequisitionItems(requisitionItem)
            } catch (Exception ex) {
                log.error "Error:${ex.printStackTrace()} in reading Order Line Item:${lineItem}"
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
