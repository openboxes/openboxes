package org.pih.warehouse.core

import grails.core.GrailsApplication
import grails.util.Holders
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentStatusCode
import org.pih.warehouse.shipping.ShipmentStatusTransitionEvent
import org.springframework.context.event.EventListener

class WebhookPublisherService {

    boolean transactional = false

    GrailsApplication grailsApplication
    def apiClientService

    List<Map> getWebhookNotificationConfig(String eventType) {
        def eventConfig = grailsApplication.config.openboxes.application.event.webhook;
        List<Map> eventConfigList = []
        if (eventConfig) {
            eventConfig?.each { config ->
                if(config?.hooks?.enabled){
                    if(config?.hooks?.eventTypes?.contains(eventType)){
                        eventConfigList << config?.hooks
                    }else{
                        log.info "eventType:${eventType} not configured, configured types are :${config?.hooks?.eventTypes}"
                    }
                }
            }
        }
        return eventConfigList
    }


    @EventListener
    void onEvent(ShipmentStatusTransitionEvent event){
        getWebhookNotificationConfig(event.eventType)?.each {Map config ->
            log.info "Firing event:${config}, event:${event}"
            publishShippedEvent(event.source, config)
        }
    }

    def publishShippedEvent(Shipment shipment, Map config) {


        Map payload = [
                id       : shipment.id,
                type     : "shipment.shipped",
                timestamp: new Date().time,
                user     : AuthService.currentUser?.get()?.id,
                location : AuthService.currentLocation?.get()?.id,
                data     : [
                        id            : shipment.id,
                        shipmentNumber: shipment.shipmentNumber,
                        origin        : shipment.origin?.id,
                        destination   : shipment.destination?.id,
                        shipmentType  : shipment.shipmentType,
                        shipmentItems : shipment.shipmentItems?.collect {
                            [
                                    id            : it.id,
                                    productName   : it.inventoryItem.product?.name,
                                    productCode   : it.inventoryItem?.product.productCode,
                                    lotNumber     : it?.inventoryItem?.lotNumber,
                                    expirationDate: it?.inventoryItem?.expirationDate.format("MM/dd/yyyy"),
                                    quantity      : it.quantity
                            ]
                        }
                ]
        ]
        publishEvent(payload, config)
    }

    def publishEvent(Map payload, Map config) {
        try {
            String webhookUrl = config.url
            Map headers = config.headers
            log.info "Payload:$payload, webhookUrl:${webhookUrl}, headers:${headers}"
            apiClientService.post(webhookUrl, payload, headers)
        } catch (Exception e) {
            log.error("Failed to publish webhook event due to error: " + e.message, e)
        }
    }
}
