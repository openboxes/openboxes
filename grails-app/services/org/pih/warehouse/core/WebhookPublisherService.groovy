package org.pih.warehouse.core

import grails.util.Holders
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentStatusCode
import org.pih.warehouse.shipping.ShipmentStatusTransitionEvent

class WebhookPublisherService {

    boolean transactional = false

    def onEventShipmentUpdated = { Shipment shipment, Map oldMap, Map newMap ->
        // now here we can have our business logic on which field we have to call webhooks.
        // writing code example to check shipment status changed or not.
        if(oldMap.containsKey("currentStatus") || newMap?.containsKey("currentStatus")){
            log.info "Shipment:${shipment}, Status changed from:${oldMap?.get('currentStatus')} to ${newMap?.get('currentStatus')}"
            // One place to handle shipment status handle and fire the event
            ShipmentStatusCode shipmentStatusCode = newMap["currentStatus"]
            Holders.grailsApplication.mainContext.publishEvent(new ShipmentStatusTransitionEvent(shipment, shipmentStatusCode))
        }
        // Other logic for other fields
    }

    def onEventProductCreated = { Product product ->
        log.info "Fired onEventProductCreated:product:${product}"
    }

    def onEventProductUpdated = { Product product, Map oldMap, Map newMap ->
        log.info "Fired onEventProductUpdated:product:${product}, old:${oldMap}, new:${newMap}"
    }
}
