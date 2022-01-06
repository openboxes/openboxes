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

import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import org.pih.warehouse.core.Constants
import org.pih.warehouse.product.Product

class ProductNotificationController {
    def notificationService
    def productService
    def grailsApplication

    def publish = {
        String message = request.JSON.toString()
        log.info "Publishing message " + message
        String TOPIC_ARN = grailsApplication.config.awssdk.sns.product.arn
        notificationService.publish(TOPIC_ARN, message,"Demo publish")
        render "published"
    }


    def subscribe = {
        String PRODUCT_TOPIC_ARN = grailsApplication.config.awssdk.sns.product.arn
        log.info "Subscribiing topic:${PRODUCT_TOPIC_ARN}"
        try {
            String subscribeUrl = g.createLink(uri: "/api/notifications/products", absolute: true)
            if (grailsApplication.config.openboxes.integration.sns.products.subscribeUrl) {
                subscribeUrl = grailsApplication.config.openboxes.integration.sns.products.subscribeUrl
            }
            log.info "Product subscribeUrl::${subscribeUrl}"
            notificationService.subscribeTopic(PRODUCT_TOPIC_ARN, subscribeUrl)
            render "subscribed ${result}"
        } catch (Exception e) {
            log.error("Error occurred while subscribing to ${PRODUCT_TOPIC_ARN}: " + e.message, e);
        }
        render "subscribed"
    }

    def handleMessage = {
        JSONObject json = request.getJSON()
        log.info "json for create Product::${json}"
        if (notificationService.confirmSubscription(json, Constants.ARN_TOPIC_PRODUCT_CREATE_UPDATE_TYPE)){
            render "subscribed"
            return
        }
        String message = json.getString("Message")
        JSONObject productsJson = new JSONObject(message)
        JSONArray products = productsJson.getJSONArray("products")
        for (int i = 0; i < products.length(); i++) {
            JSONObject productJson = products.getJSONObject(i)
            Product productInstance = productService.createFromJson(productJson)
        }
    }
}
