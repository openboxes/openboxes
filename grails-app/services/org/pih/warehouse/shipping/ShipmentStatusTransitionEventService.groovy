/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.shipping

import org.pih.warehouse.core.RoleType
import org.springframework.context.ApplicationListener

class ShipmentStatusTransitionEventService implements ApplicationListener<ShipmentStatusTransitionEvent> {

    boolean transactional = true

    def notificationService

    void onApplicationEvent(ShipmentStatusTransitionEvent event) {
        log.info "Application event ${event} has been published!"
        Shipment shipment = Shipment.get(event?.source?.id)
        log.info "Shipment ${shipment?.shipmentNumber} from ${shipment?.origin} to ${shipment.destination}"
        List outboundCreatedRoleTypes = [RoleType.ROLE_SHIPMENT_NOTIFICATION, RoleType.ROLE_SHIPMENT_OUTBOUND_CREATED_NOTIFICATION]
        List inboundCreatedRoleTypes = [RoleType.ROLE_SHIPMENT_NOTIFICATION, RoleType.ROLE_SHIPMENT_INBOUND_CREATED_NOTIFICATION]
        List inboundShippedRoleTypes = [RoleType.ROLE_SHIPMENT_NOTIFICATION, RoleType.ROLE_SHIPMENT_INBOUND_SHIPPED_NOTIFICATION]
        List outboundShippedRoleTypes = [RoleType.ROLE_SHIPMENT_NOTIFICATION, RoleType.ROLE_SHIPMENT_OUTBOUND_SHIPPED_NOTIFICATION]
        List inboundReceivedRoleTypes = [RoleType.ROLE_SHIPMENT_NOTIFICATION, RoleType.ROLE_SHIPMENT_INBOUND_RECEIVED_NOTIFICATION]
        List outboundReceivedRoleTypes = [RoleType.ROLE_SHIPMENT_NOTIFICATION, RoleType.ROLE_SHIPMENT_OUTBOUND_RECEIVED_NOTIFICATION]

        if (event.shipmentStatusCode == ShipmentStatusCode.CREATED) {
            notificationService.sendShipmentCreatedNotification(shipment, shipment.origin, outboundCreatedRoleTypes)
            notificationService.sendShipmentCreatedNotification(shipment, shipment.destination, inboundCreatedRoleTypes)
        }
        else if(event.shipmentStatusCode == ShipmentStatusCode.SHIPPED) {
            notificationService.sendShipmentIssuedNotification(shipment, shipment.origin, outboundShippedRoleTypes)
            notificationService.sendShipmentIssuedNotification(shipment, shipment.destination, inboundShippedRoleTypes)
        }
        else if (event.shipmentStatusCode in [ShipmentStatusCode.RECEIVED, ShipmentStatusCode.PARTIALLY_RECEIVED]) {
            notificationService.sendShipmentReceiptNotification(shipment, shipment.origin, outboundReceivedRoleTypes)
            notificationService.sendShipmentReceiptNotification(shipment, shipment.destination, inboundReceivedRoleTypes)
        }

    }

}
