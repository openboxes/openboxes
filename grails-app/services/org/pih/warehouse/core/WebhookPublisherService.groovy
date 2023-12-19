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
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.auth.AuthService


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

    def publishEvent(Map payload) {
        try {
            boolean webhooksEnabled = Holders.config.openboxes.webhook.enabled
            String webhookUrl = Holders.config.openboxes.webhook.endpoint.url
            Map headers = Holders.config.openboxes.webhook.endpoint.headers
            apiClientService.post(webhookUrl, payload, headers)
        } catch (Exception e) {
            log.error("Failed to publish webhook event due to error: " + e.message, e)
        }
    }
}
