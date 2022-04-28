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
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.core.ReasonCode

class OrderStatusNotificationController {

    def userService
    def productService
    def grailsApplication
    def identifierService
    def notificationService
    def stockMovementService

    def beforeInterceptor = {
        userService.authenticateIntegrationUser()
    }

    def afterInterceptor = {
        userService.invalidateIntegrationUserSession()
    }

    def publish = {
        String message = request.JSON.toString()
        log.info "Publishing message " + message
        String arn = grailsApplication.config.awssdk.sns.consumer.order.status.arn
        notificationService.publish(arn, message,"test message")
        render "published"
    }

    def subscribe = {
        String topicArn = grailsApplication.config.awssdk.sns.consumer.order.status.arn
        try {
            log.info "Subscribing to topic:${topicArn}"
            String subscribeUrl = g.createLink(uri: "/api/notifications/orderStatuses", absolute: true)
            if (grailsApplication.config.openboxes.integration.sns.orders.subscribeUrl) {
                subscribeUrl = grailsApplication.config.openboxes.integration.sns.orders.subscribeUrl
            }
            log.info "Order subscribeUrl::${subscribeUrl}"
            notificationService.subscribeTopic(topicArn, subscribeUrl)
            render "subscribed to ${topicArn} with subscriber ${subscribeUrl}"
        } catch (Exception e) {
            log.error("An error occurred while subscribing to topic ${topicArn}");
            render "Error subscribing to topic ${topicArn}: ${e.message}"
        }

    }

    def handleMessage = {
        JSONObject json = request.getJSON()
        log.info "handle message ${json.toString(4)}"
        if (notificationService.confirmSubscription(json, Constants.ARN_TOPIC_ORDER_STATUS_TYPE)) {
            render "subscribed"
            return
        }
        String message = json.getString("Message")
        JSONObject jsonObject = new JSONObject(message)
        log.info "handle order status ${jsonObject.toString(4)}"

        String identifier = jsonObject.id
        JSONArray orderItems = jsonObject.orderItems

        RequisitionStatus status = jsonObject.status as RequisitionStatus

        log.info "identifier: ${identifier}, status: ${status}"
        // Just remembered that we don't have a way of looking up stock movements by identifier
        if (identifier && status) {

            Requisition requisition = Requisition.findByRequestNumber(identifier)

            // Do not allow any edits when order has been moved to Packed status
            if (requisition.status >= RequisitionStatus.CHECKING) {
                throw new IllegalStateException("Cannot update requisition in status ${requisition.status}")
            }

            if (status in [RequisitionStatus.UNCHANGED]) {
                return
            }

            if (status in [RequisitionStatus.CANCELED]) {
                log.info "Canceling requisition ${identifier}"
                requisition.status = status as RequisitionStatus
                requisition.requisitionItems.each { RequisitionItem requisitionItem ->
                    requisitionItem.cancelQuantity(ReasonCode.REJECTED, "Canceled order")
                }
                requisition.save(flush:true)
            }
            else if (status in [RequisitionStatus.APPROVED]) {
                log.info "Approving requisition ${identifier}"

                List<RequisitionItem> requisitionItemList =
                        requisition.requisitionItems.toList()

                // Process the order items from the
                log.info "order items " + orderItems
                orderItems.each { orderItem ->
                    String orderItemUuid = orderItem.id
                    // Find by the external UUID (note: we're using description to hold UUID temporarily)
                    RequisitionItem requisitionItem =
                            requisitionItemList.find { it.description == orderItemUuid }

                    // Remove requisition item from list
                    requisitionItemList.remove(requisitionItem)

                    // Process requisition item as either canceled or approved
                    log.info "Requisition item " + requisitionItem
                    Integer quantity = orderItem.quantity
                    if (quantity == 0) {
                        // Cancel the original or modified item
                        if (requisitionItem?.modificationItem) {
                            requisitionItem.undoChanges()
                        }
                        requisitionItem?.cancelQuantity(ReasonCode.REJECTED, "Rejected quantity change")
                    }
                    else {
                        // Approve the original or modified item
                        if (requisitionItem?.modificationItem) {
                            requisitionItem?.modificationItem?.approveQuantity()
                        }
                        else {
                            requisitionItem?.approveQuantity()
                        }
                        requisitionItem?.save()
                    }
                }

                // The remaining requisition items should all be approved
                requisitionItemList.each { RequisitionItem requisitionItem ->
                    log.info "Approve quantity for requisition item: ${new JSONObject(requisitionItem.toJson()).toString(4)}"
                    requisitionItem?.modificationItem?.approveQuantity()
                }

            }
        }

        render([status: 200, text: "Order status message handled successfully"])
    }
}
