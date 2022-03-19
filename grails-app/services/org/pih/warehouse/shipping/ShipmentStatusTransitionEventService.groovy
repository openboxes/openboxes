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

    def userService
    def notificationService

    void onApplicationEvent(ShipmentStatusTransitionEvent event) {
        log.info "Application event ${event} has been published!"
        Shipment shipment = Shipment.get(event?.source?.id)
        log.info "Shipment ${shipment?.shipmentNumber} from ${shipment?.origin} to ${shipment.destination}"

        if (event.shipmentStatusCode == ShipmentStatusCode.CREATED) {

            def users = []
            users.addAll(userService.findUsersByRoleTypes(shipment.origin,
                    [RoleType.ROLE_SHIPMENT_NOTIFICATION, RoleType.ROLE_SHIPMENT_OUTBOUND_CREATED_NOTIFICATION]))
            users.addAll(userService.findUsersByRoleTypes(shipment.destination,
                    [RoleType.ROLE_SHIPMENT_NOTIFICATION, RoleType.ROLE_SHIPMENT_INBOUND_CREATED_NOTIFICATION]))

            notificationService.sendShipmentCreatedNotification(shipment, users)
        }
        else if(event.shipmentStatusCode == ShipmentStatusCode.SHIPPED) {

            def users = []
            users.addAll(userService.findUsersByRoleTypes(shipment.origin,
                    [RoleType.ROLE_SHIPMENT_NOTIFICATION, RoleType.ROLE_SHIPMENT_OUTBOUND_SHIPPED_NOTIFICATION]))
            users.addAll(userService.findUsersByRoleTypes(shipment.destination,
                    [RoleType.ROLE_SHIPMENT_NOTIFICATION, RoleType.ROLE_SHIPMENT_INBOUND_SHIPPED_NOTIFICATION]))

            notificationService.sendShipmentIssuedNotification(shipment, users)

            notificationService.sendShipmentItemsShippedNotification(shipment)
        }
        else if (event.shipmentStatusCode in [ShipmentStatusCode.RECEIVED]) {

            def users = []
            users.addAll(userService.findUsersByRoleTypes(shipment.origin,
                    [RoleType.ROLE_SHIPMENT_NOTIFICATION, RoleType.ROLE_SHIPMENT_OUTBOUND_RECEIVED_NOTIFICATION]))
            users.addAll(userService.findUsersByRoleTypes(shipment.destination,
                    [RoleType.ROLE_SHIPMENT_NOTIFICATION, RoleType.ROLE_SHIPMENT_INBOUND_RECEIVED_NOTIFICATION]))

            notificationService.sendShipmentReceiptNotification(shipment, users)

            // Send notification email to recipients on completed receipt
            notificationService.sendReceiptNotifications(event?.partialReceipt)
        }

    }

}
