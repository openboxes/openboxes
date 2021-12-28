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

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.sns.AmazonSNSClientBuilder
import com.amazonaws.services.sns.model.ConfirmSubscriptionRequest
import com.amazonaws.services.sns.model.ConfirmSubscriptionResult
import com.amazonaws.services.sns.model.PublishRequest
import com.amazonaws.services.sns.model.SubscribeRequest
import com.amazonaws.services.sns.model.SubscribeResult
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time.DateUtils
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import util.StringUtil

class NotificationController {

    def productService
    def grailsApplication
    def notificationService


    def publish = {
        String message = request.JSON.toString()
        log.info "Publishing message " + message
//        String TOPIC_ARN = grailsApplication.config.awssdk.sns.product.arn
        String TOPIC_ARN = grailsApplication.config.awssdk.sns.order.arn
        PublishRequest publishRequest = new PublishRequest(TOPIC_ARN, message, "Demo publish")
        def result = notificationService.amazonSnsClient.publish(publishRequest)
        log.info "result::${result?.dump()}"
        render "published"
    }

    def notify = {
        log.info "Notifi Topic: params:${params}"
        JSONObject json = request.getJSON() as JSONObject
        log.info "Notifi Topic: json:${json}"
        if (json.has("Type") && json.getString("Type") == "SubscriptionConfirmation") {
            String token = json.getString("Token")
            try {
                String PRODUCT_TOPIC_ARN = grailsApplication.config.awssdk.sns.product.arn
                ConfirmSubscriptionRequest request = new ConfirmSubscriptionRequest(PRODUCT_TOPIC_ARN, token)
                ConfirmSubscriptionResult result = notificationService.amazonSnsClient.confirmSubscription(request);
                log.info "result::${result?.toString()}"
            } catch (Exception e) {
                System.err.println(e.printStackTrace());
            }
        }
        render "notified"
    }

    def subscribeProduct = {
        String PRODUCT_TOPIC_ARN = grailsApplication.config.awssdk.sns.product.arn
        log.info "Subscribiing topic:${PRODUCT_TOPIC_ARN}"
        try {
            String subscribeUrl = g.createLink(uri: "/api/notifications/products", absolute: true)
            if (grailsApplication.config.openboxes.integration.sns.products.subscribeUrl) {
                subscribeUrl = grailsApplication.config.openboxes.integration.sns.products.subscribeUrl
            }
            log.info "Product subscribeUrl::${subscribeUrl}"
            SubscribeRequest subscribeRequest = new SubscribeRequest(PRODUCT_TOPIC_ARN, "https", subscribeUrl)
            subscribeRequest.returnSubscriptionArn = true
            SubscribeResult result = notificationService.amazonSnsClient.subscribe(subscribeRequest);
            log.info("Subscription ARN is " + result.subscriptionArn);
            render "subscribed ${result}"
        } catch (Exception e) {
            log.error("Error occurred while subscribing to ${PRODUCT_TOPIC_ARN}: " + e.message, e);
        }
        render "subscribed"
    }

    def createProduct = {
        JSONObject json = request.getJSON() as JSONObject
        log.info "json for create Product::${json}"
        if (notificationService.confirmSubscription(json, Constants.ARN_TOPIC_PRODUCT_CREATE_UPDATE_TYPE)){
            render "subscribed"
            return
        }
        String message = json.getString("Message")
        JSONObject productsJson = new JSONObject(message)
        JSONArray products = productsJson.getJSONArray("products")
        for (int i = 0; i < products.length(); i++) {
            JSONObject pJson = products.getJSONObject(i)
            Product productInstance = productService.createFromJson(pJson)
        }
    }

    def subscribeOrder = {
        String ORDER_TOPIC_ARN = grailsApplication.config.awssdk.sns.order.arn?.toString()

        log.info "Subscribiing topic:${ORDER_TOPIC_ARN}"
        try {
            String subscribeUrl = g.createLink(uri: "/api/notifications/orders", absolute: true)
            if (grailsApplication.config.openboxes.integration.sns.orders.subscribeUrl) {
                subscribeUrl = grailsApplication.config.openboxes.integration.sns.orders.subscribeUrl
            }
            log.info "Order subscribeUrl::${subscribeUrl}"
            SubscribeRequest subscribeRequest = new SubscribeRequest(ORDER_TOPIC_ARN, "https", subscribeUrl)
            subscribeRequest.returnSubscriptionArn = true
            SubscribeResult result = notificationService.amazonSnsClient.subscribe(subscribeRequest);
            log.info("Subscription ARN is " + result.subscriptionArn);
        } catch (Exception e) {
            log.error(e.printStackTrace());
        }
        render "subscribed"
    }

    def createOrder = {
        JSONObject json = request.getJSON() as JSONObject
        log.info "Create order JSON:${json}"
        if (notificationService.confirmSubscription(json, Constants.ARN_TOPIC_ORDER_CREATE_TYPE)){
            render "subscribed"
            return
        }
        String message = json.getString("Message")
        JSONObject oJson = new JSONObject(message)
//        JSONArray orders = productsJson.getJSONArray("orders")
//        for(int i = 0;i<orders.length();i++){
//            JSONObject oJson = orders.getJSONObject(i)
        log.info "ORDER JSON ${oJson}"
        Requisition requisition = new Requisition()
        if (oJson.has("name")) {
            requisition.name = oJson.getString("name")
        } else {
            requisition.name = "DEFAULT"
        }
        try {
            if (oJson.has("origin") && oJson.getString("origin")) {
                JSONObject originJsonObject = oJson.getJSONObject("origin")
                log.info "Origin location Json:${originJsonObject}"
                Location location = Location.findByLocationNumber(originJsonObject.getString("locationNumber"))
                if (!location && grailsApplication.config?.openboxes?.integration?.locationMapping) {
                    log.info "Location Mapping::${grailsApplication.config?.openboxes?.integration?.locationMapping}"
                    location = getLocationByLocationMapping(originJsonObject)
                }
                log.info "Origin location::${location}"
                requisition.origin = location
            }
        } catch (Exception ex) {
            log.error "Error in reading origin from order json:${oJson}"
        }
        try {
            if (oJson.has("destination") && oJson.getString("destination")) {
                JSONObject destinationJsonObject = oJson.getJSONObject("destination")
                log.info "Destination location Json:${destinationJsonObject}"
                Location location = Location.findByLocationNumber(destinationJsonObject.getString("locationNumber"))
                if (!location && grailsApplication.config?.openboxes?.integration?.locationMapping) {
                    log.info "Location Mapping::${grailsApplication.config?.openboxes?.integration?.locationMapping}"
                    location = getLocationByLocationMapping(destinationJsonObject)
                }
                log.info "Destination location::${location}"
                requisition.destination = location
            }
        } catch (Exception ex) {
            log.error "Error:${ex.printStackTrace()} in reading destination from order json:${oJson}"
        }
        try {
            if (oJson.has("requestedBy") && oJson.getString("requestedBy")) {
                User requestedBy = User.findByUsername(oJson.getString("requestedBy"))
                if (!requestedBy) {
                    JSONObject userMapping = toJson(grailsApplication.config?.openboxes?.integration?.userMapping ?: null)
                    log.info "UserMapping:${userMapping}"
                    if(userMapping) {
                        String requestedByString = oJson.getString("requestedBy")
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
            log.error "Error:${ex.printStackTrace()} in reading requestedBy from order json:${oJson}"
        }
        try {
            if (oJson.has("dateRequested") && oJson.getString("dateRequested")) {
                Date dateRequested = DateUtils.parseDate(oJson.getString("dateRequested"), "yyyy-MM-dd")
                requisition.dateRequested = dateRequested
            } else {
                requisition.dateRequested = new Date()
            }
        } catch (Exception ex) {
            log.error "Error:${ex.printStackTrace()} in reading dateRequested from order json:${oJson}"
            requisition.dateRequested = new Date()
        }
        try {
            if (oJson.has("requestedDeliveryDate") && oJson.getString("requestedDeliveryDate")) {
                Date dateRequested = DateUtils.parseDate(oJson.getString("requestedDeliveryDate"), "yyyy-MM-dd")
                requisition.dateRequested = dateRequested
            } else {
                requisition.requestedDeliveryDate = new Date()
            }
        } catch (Exception ex) {
            log.error "Error in reading dateRequested from order json:${oJson}"
            requisition.requestedDeliveryDate = new Date()
        }
        Boolean lineItemHasError = false
        JSONArray orderLineItemsArray = oJson.getJSONArray("orderItems")
        for (int lIndex = 0; lIndex < orderLineItemsArray?.length(); lIndex++) {
            JSONObject lineItem = orderLineItemsArray.getJSONObject(lIndex)
            log.info "Reading LineItem:${lineItem} for Order"
            try {
                RequisitionItem requisitionItem = new RequisitionItem()
                Product product = Product.findByUpc(lineItem.getString("productId"))
                if (!product) {
                    JSONObject productMapping = toJson(grailsApplication.config?.openboxes?.integration?.productMapping ?: null)
                    log.info "ProductMapping:${productMapping}"
                    if(productMapping) {
                        String productId = lineItem.getString("productId")
                        log.info "Reading productId:${productId}"
                        if(productMapping.has(productId)) {
                            product = Product.findByProductCode(productMapping.getString(productId))
                        }
                    }
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
//        }
    }

    JSONObject toJson(String value) {
        try {
            if (value) {
                value = value.trim()
                if(value != "" && value != "null") {
                    JSONObject jsonObject = new JSONObject(value)
                    return jsonObject
                }
            }
        } catch (Exception ex) {
            log.error "Exception:${ex} while parsing ${value} in JSON"
        }

        return null
    }

    Location getLocationByLocationMapping(JSONObject locationJsonObject){
        JSONObject locationMapping = toJson(grailsApplication.config?.openboxes?.integration?.locationMapping ?: null)
        log.info "LocationMapping JSON:${locationMapping}"
        Location location = null
        if(locationMapping) {
            String locationId = locationJsonObject?.getString("id")
            if(locationMapping.has(locationId)) {
                log.info "LocationId:${locationId}, LocationMapping[$locationId]:${locationMapping.getString(locationId)}"
                if (locationMapping.getString(locationId)) {
                    location = Location.findById(locationMapping.getString(locationId))
                }
            }
        }
        return location
    }


}
