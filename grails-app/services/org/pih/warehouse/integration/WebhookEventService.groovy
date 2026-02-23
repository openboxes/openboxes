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

import grails.util.Holders
import org.grails.web.json.JSONObject
import org.pih.warehouse.core.ApiClientService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.ShipmentItem

// TODO: Implement now and move out of this service (MVP)
class WebhookEventType {}

// TODO: Implement now and move out of this service (MVP)
class WebhookStatus {}

// TODO: Implement now and move out of this service (MVP)
interface WebhookEvent {
    Map getPayload()
}

// TODO: Move out of this service
class OrderConfirmationEvent implements WebhookEvent {
    String eventId
    String webhookId
    Requisition requisition
    Location facility
    User triggeredBy
    Date dateTriggered

    @Override
    Map getPayload() {
        return [
                eventId: eventId, // need an idempotent key here - UUID would be fine for now i guess
                // TODO: Q: determine event types - how this differs from the status which is "CONSUMED" here
                eventType: "requisition.issued",
                // TODO: determine the date format for this (iirc ISO8601 - YYYY-MM-DDThh:mm:ssZ)
                eventDate: dateTriggered.toString(),
                requisitionNumber: requisition.requestNumber, // unique identifier (e.g. Excede SlsId for part orders)
                requisitionType: requisition.type,            // probably not needed for this but include as much metadata as possible
                deliveryTypeCode: requisition.deliveryTypeCode.toString(),  // PICK_UP, SERVICE, WILL_CALL, SHIP_TO, LOCAL_DELIVERY
                status: "CONSUMED",
                // TODO: determine format for triggerBy - id, username, full name?
                triggeredBy: triggeredBy.name,      // who issued the requisition (use default like admin if auto-issued)
                location: "Front Counter",    // might not need this for now
                // TODO: Change into multiple shipments once it will be in use (MVP2+)
                lines: requisition.shipment?.shipmentItems?.collect { ShipmentItem item ->
                    [
                            "productId": item.product.id,
                            "productCode": item.product.productCode,
                            "inventoryItemId": item.inventoryItem.id,
                            "quantityRequested": item.requisitionItem.calculateQuantityRequired(),
                            "quantityIssued": item.quantity,
                            "locationId": item.binLocation.id,
                            "locationNumber": item.binLocation.locationNumber
                    ]
                },
                metadata: [
                        "facilityId": facility.id,
                        "facilityCode": facility.locationNumber,
                        "facilityName": facility.name,
                        "webhookId": webhookId,
                        "attemptNumber": 1
                ]
        ]
    }
}

// TODO: Implement MVP2+
class WebhookEventLog {}

class WebhookEventService {

    ApiClientService apiClientService

    /**
     * TODO:
     *      add WebhookType
     *      add WebhookEvent
     *      add WebhookStatus - success, failed, retrying, etc
     *  later:
     *      add WebhookEventLog - domain with db table
     *      Retry mechanism
     *
     * */

    void triggerOrderConfirmationWebhookEvent(WebhookEventType webhookEventType, String eventId, Map payload) {
        // TODO: Build proper event basing on the webhookEventType

        // TODO: Check if shipment.origin.supports(ActivityCode.ENABLE_WEBHOOKS) ???

        try {
            OrderConfirmationEvent webhookEvent = new OrderConfirmationEvent(
                    eventId: eventId ?: UUID.randomUUID(),
                    webhookId: payload.webhookId ?: UUID.randomUUID(),
                    requisition: payload.requisition,
                    facility: payload.facility,
                    triggeredBy: payload.triggeredBy,
                    dateTriggered: payload.dateTriggered ?: new Date()
            )
            // TODO: Determine if requires saving => if WebhookEvent should be stored in the db
            // webhookEvent.save()
            publishWebhookEvent(webhookEvent)
        } catch (Exception exception) {
            // there might be a more elegant solution here, but let's keep it simple
            // TODO: Implement handleWebhookException
            // handleWebhookException(webhookEvent, exception)
        }
    }


    void publishWebhookEvent(WebhookEvent webhookEvent) {
        boolean webhooksEnabled = Holders.config.openboxes.n8n.enabled
        if (!webhooksEnabled) {
            return
        }

        String webhookUrl = Holders.config.openboxes.n8n.endpoint.url
        String apiKey = Holders.config.openboxes.n8n.endpoint.apiKey
        Map headers = ["X-API-KEY": apiKey]
        try {
            JSONObject response = apiClientService.post(webhookUrl, webhookEvent.payload, apiKey ? headers : [:])
            log.info("Successfully published webhook event with id ${webhookEvent.eventId} and received response: ${response.toString()}")
            // TODO: check if response has error message and schedule retry
        } catch (Exception e) {
            log.error("Failed to publish webhook event due to error: " + e.message, e)
            // TODO: schedule retry
        }
    }

    void scheduleRetry(WebhookEvent webhookEvent) {
        // TODO: Implement this later (MVP2?)
    }

    void handleWebhookException(WebhookEvent webhookEvent, Exception exception) {
        // TODO: Implement this later (MVP2?)
        /**
        webhook.attemptCount++
        webhook.lastExceptionMessage = exception.message
        webhook.dateLastAttempted = new Date()
        webhook.webhookId = UUID.randomUUID().toString()

        // Update payload with new attempt info
        def payloadMap = JSON.parse(webhook.payload) as Map
        payloadMap.metadata.attemptNumber = webhook.attemptCount
        payloadMap.metadata.webhookId = webhook.webhookId
        webhook.payload = payloadMap as JSONObject

        // Retry fewer times since N8N will handle Exceed retries
        def maxRetries = grailsApplication.config.n8n.webhook.maxRetries ?: 3

        // bump retry count, log exceptions, alert admin users?
        // check if we should retry and if there are more retry attempts left
        // retry (set status to RETRYING so we don't send multiple retries at the same time)
        if (webhook.attemptCount < maxRetries) {
            webhook.status = WebhookStatus.RETRYING
            webhook.save(flush: true)
            webhookEventService.scheduleRetry(webhook)
        } else {
            // Only goes to DLQ if N8N itself is unreachable
            webhook.status = WebhookStatus.FAILED
            webhook.save(flush: true)
            log.error("Webhook ${webhook.eventId} failed after ${maxRetries} attempts to publish webhook event")
        }
        */
    }
}
