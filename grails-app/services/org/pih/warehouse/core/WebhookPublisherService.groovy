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

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.pih.warehouse.shipping.Shipment


class WebhookPublisherService {

    def apiClientService
    //def grailsApplication

    boolean transactional = false

    def publishEvent(Shipment shipment) {
        Map payloadData = [id: shipment.id, eventType: "shipment.shipped"]
        publishEvent(payloadData)
    }

    def publishEvent(Map payloadData) {
        try {
            String webhookUrl = ConfigurationHolder.config.openboxes.webhook.endpoint.url
            apiClientService.post(webhookUrl, payloadData)
        } catch (Exception e) {
            log.error("Failed to publish webhook event due to error: " + e.message, e)
        }
    }
}
