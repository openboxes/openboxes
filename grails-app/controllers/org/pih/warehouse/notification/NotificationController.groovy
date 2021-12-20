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
import org.apache.commons.lang3.time.DateUtils
import org.json.JSONArray
import org.json.JSONObject
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem

class NotificationController {

    def productService
    def grailsApplication

    private def getAmazonSnsClient() {
        return AmazonSNSClientBuilder
                .standard()
                .withRegion(grailsApplication.config.awssdk.sns.region)
                .withCredentials(
                        new AWSStaticCredentialsProvider(
                                new BasicAWSCredentials(
                                        grailsApplication.config.awssdk.sns.accessKey,
                                        grailsApplication.config.awssdk.sns.secretKey
                                )
                        )
                ).build()
    }

    def publish = {
        String message = request.JSON.toString()
        log.info "Publishing message " + message
        String TOPIC_ARN = grailsApplication.config.awssdk.sns.product.arn
//        String TOPIC_ARN = grailsApplication.config.awssdk.sns.order.arn
        PublishRequest publishRequest = new PublishRequest(TOPIC_ARN, message, "Demo publish")
        def result = amazonSnsClient.publish(publishRequest)
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
                ConfirmSubscriptionResult result = amazonSnsClient.confirmSubscription(request);
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
//            String subscribeUrl = grailsApplication.config.grails.serverURL + "/api/notifications/products"
            SubscribeRequest subscribeRequest = new SubscribeRequest(PRODUCT_TOPIC_ARN, "https", subscribeUrl)
            subscribeRequest.returnSubscriptionArn = true
            SubscribeResult result = amazonSnsClient.subscribe(subscribeRequest);
            log.info("Subscription ARN is " + result.subscriptionArn);
            render "subscribed ${result}"
        } catch (Exception e) {
            log.error("Error occurred while subscribing to ${PRODUCT_TOPIC_ARN}: " + e.message, e);
        }
        render "subscribed"
    }

    def createProduct = {
        JSONObject json = request.getJSON() as JSONObject
        log.info "json::${json}"
        if (json.has("Type") && json.getString("Type") == "SubscriptionConfirmation") {
            String token = json.getString("Token")
            try {
                String PRODUCT_TOPIC_ARN = grailsApplication.config.awssdk.sns.product.arn
                ConfirmSubscriptionRequest request = new ConfirmSubscriptionRequest(PRODUCT_TOPIC_ARN, token)
                ConfirmSubscriptionResult result = amazonSnsClient.confirmSubscription(request);
                log.info "result::${result?.toString()}"
            } catch (Exception e) {
                System.err.println(e.printStackTrace());
            }
            render "subscribed"
            return
        }

        String message = json.getString("Message")
        JSONObject productsJson = new JSONObject(message)
        JSONArray products = productsJson.getJSONArray("products")
        for (int i = 0; i < products.length(); i++) {
            JSONObject pJson = products.getJSONObject(i)
            log.info "PRODUCT JSON ${pJson} at index:${pJson}"
            Category category = Category.findById(pJson.getString("category"))
            if (!category) {
                category = new Category()
                category.id = pJson.getString("category")
                category.name = pJson.getString("category")
                category.save()
            }
            Product productInstance = Product.findByProductCode(pJson.getString("productCode"))
            if (!productInstance) {
                productInstance = new Product()
                productInstance.productCode = pJson.getString("productCode")
            }
            productInstance.id = pJson.getString("id")
            productInstance.name = pJson.getString("name")
            productInstance.description = pJson.getString("description")
            productInstance.pricePerUnit = pJson.getDouble("pricePerUnit")
            productInstance.unitOfMeasure = pJson.getString("unitOfMeasure")
            productInstance.brandName = pJson.getString("brandName")
            productInstance.category = category
//            if (!productInstance?.id || productInstance.validate()) {
                if (!productInstance.productCode) {
                    productInstance.productCode = productService.generateProductIdentifier(productInstance.productType)
                }
//            }
            try {
                if (!productInstance?.validate() || productInstance?.hasErrors()) {
                    productInstance?.errors?.allErrors?.each {
                        log.error("ERR:${it}")
                    }
                } else {
                    productInstance.save(flush: true, failOnError: true)
                }
            } catch (Exception ex) {
                log.error "Error in saving PRODUCT:${ex?.message}"
            }
        }
    }


}
