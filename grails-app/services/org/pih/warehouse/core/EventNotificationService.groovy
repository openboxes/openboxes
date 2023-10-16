package org.pih.warehouse.core

import grails.core.GrailsApplication
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentStatusTransitionEvent
import org.springframework.context.event.EventListener

class EventNotificationService {

    GrailsApplication grailsApplication

    def webhookPublisherService
    def notificationService


    public static String SMTP_PROVIDER = "smtp"
    public static String WEBHOOK_PROVIDER = "webhook"


    List<Map> getNotificationConfig(String eventType) {
        def eventConfig = grailsApplication.config.openboxes.notifications;
        List<Map> eventConfigList = []
        if (eventConfig) {
            eventConfig?.each { config ->
                if (config?.enabled) {
                    if (config?.events?.contains(eventType)) {
                        eventConfigList << config
                    } else {
                        log.info "eventType:${eventType} not configured, configured types are :${config?.hooks?.eventTypes}"
                    }
                }
            }
        }
        return eventConfigList
    }


    @EventListener
    void onEvent(ShipmentStatusTransitionEvent event) {
        getNotificationConfig(event.shipmentStatusCode?.eventType)?.each { Map config ->
            log.info "Firing event:${config?.provider}, event:${event.shipmentStatusCode?.eventType}"
            Shipment shipment = event.source
            switch (config.provider?.toString()) {
                case WEBHOOK_PROVIDER:
                    webhookPublisherService.publishShippedEvent(shipment, config)
                    break
                case SMTP_PROVIDER:
                    log.info "Firing SMTP_PROVIDER"
                    notificationService.shipmentNotification(shipment, config)
                    break
                default:
                    log.warn("No available provider ${config.provider} for event type ${event.eventType}")
                    break
            }
        }
    }
}