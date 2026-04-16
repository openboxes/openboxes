/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse.core

import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.shipping.ShipmentItem


@Transactional
class WebhookPublisherService {

    def apiClientService

    def publishShippedEvent(Shipment shipment) {

        boolean webhooksEnabled = shipment.origin.supports(ActivityCode.ENABLE_WEBHOOKS)
        if (!webhooksEnabled) {
            log.info "Location ${shipment.origin} does not support activity code ${ActivityCode.ENABLE_WEBHOOKS}"
            return
        }

        Map payload = [
                id       : shipment.id,
                type     : "shipment.shipped",
                timestamp: new Date().time,
                user     : AuthService.currentUser?.id,
                location : AuthService.currentLocation?.id,
                data     : [
                        id            : shipment.id,
                        shipmentNumber: shipment.shipmentNumber,
                        origin        : shipment.origin.id,
                        destination   : shipment.destination.id,
                        shipmentType  : shipment.shipmentType,
                        shipmentItems : shipment.shipmentItems.collect {
                            [
                                    id            : it.id,
                                    productName   : it.inventoryItem.product?.name,
                                    productCode   : it.inventoryItem?.product.productCode,
                                    lotNumber     : it?.inventoryItem?.lotNumber,
                                    expirationDate: it?.inventoryItem?.expirationDate ?
                                            it.inventoryItem.expirationDate.format(Constants.EXPIRATION_DATE_FORMAT) :
                                            null,
                                    quantity      : it.quantity
                            ]
                        }
                ]
        ]
        publishEvent(payload)
    }

    def publishOrderConfirmation(Requisition requisition, WebhookNotificationComment comment) {
        if (!requisition) {
            log.warn "Cannot publish order confirmation webhook event without a requisition"
            return
        }

        boolean webhooksEnabled = requisition.origin.supports(ActivityCode.ENABLE_WEBHOOKS)
        if (!webhooksEnabled) {
            log.info "Location ${requisition.origin} does not support activity code ${ActivityCode.ENABLE_WEBHOOKS}"
            return
        }

        String eventId = UUID.randomUUID().toString()
        String webhookId = UUID.randomUUID().toString()
        Date dateTriggered = new Date()
        User triggeredBy = AuthService.currentUser
        Location facility = requisition.origin

        Map payload = [
                eventId: eventId,
                eventDate: dateTriggered.toString(),
                requisitionNumber: requisition.requestNumber,
                requisitionType: requisition.type,
                deliveryTypeCode: requisition.deliveryTypeCode.toString(),
                comment: comment.toString(),
                triggeredBy: triggeredBy?.name,
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

        publishEvent(payload, "openboxes.n8n")
    }

    def publishEvent(Map payload, String configPath = "openboxes.webhook") {
        try {
            boolean webhooksEnabled = Holders.config.get("${configPath}.enabled")
            if (!webhooksEnabled) {
                log.info "Webhooks for config path ${configPath} are disabled"
                return
            }

            String webhookUrl = Holders.config.get("${configPath}.endpoint.url")
            Map headers = Holders.config.get("${configPath}.endpoint.headers")
            apiClientService.post(webhookUrl, payload, headers)
        } catch (Exception e) {
            log.error("Failed to publish webhook event due to error: " + e.message, e)
        }
    }
}
