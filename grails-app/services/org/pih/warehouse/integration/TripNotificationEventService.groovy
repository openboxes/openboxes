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

import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.integration.xml.trip.Orders
import org.springframework.context.ApplicationListener

class TripNotificationEventService implements ApplicationListener<TripNotificationEvent>  {

    boolean transactional = true

    def stockMovementService

    void onApplicationEvent(TripNotificationEvent tripNotificationEvent) {
        log.info "Trip notification Orders Count" + tripNotificationEvent?.trip?.tripOrderDetails?.orders?.size()
        List<Orders> orders = tripNotificationEvent.trip.tripOrderDetails.orders
        orders.each { Orders deliveryOrder ->
            log.info "Trip notification order" + deliveryOrder.toString()
            String identifier = deliveryOrder.extOrderId
            String trackingNumber = deliveryOrder.orderId

            // FIXME Need to ensure that stock movement is ready to receive notifcation i.e. shipment has been created
            StockMovement stockMovement = stockMovementService.getStockMovementByIdentifier(identifier)
            if (!stockMovement?.shipment) {
                stockMovement?.shipment = stockMovementService.createShipment(stockMovement, false)
            }
            stockMovementService.createOrUpdateTrackingNumber(stockMovement?.shipment, trackingNumber)
        }
    }
}
