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

import org.pih.warehouse.core.ActivityCode
import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import org.pih.warehouse.core.RoleType
import org.springframework.context.ApplicationListener

@Transactional
class ShipmentStatusTransitionEventService implements ApplicationListener<ShipmentStatusTransitionEvent> {

    GrailsApplication grailsApplication
    def notificationService
    def webhookPublisherService

    void onApplicationEvent(ShipmentStatusTransitionEvent event) {
        log.info "Application event ${event} has been published!"
        Shipment shipment = Shipment.get(event?.source?.id)

        if (!shipment) {
            log.warn("Shipment not found; skipping notification.")
            return
        }

        def originNotificationsEnabled = shipment.origin.supports(ActivityCode.ENABLE_NOTIFICATIONS) ?: false
        def destinationNotificationsEnabled = shipment.destination.supports(ActivityCode.ENABLE_NOTIFICATIONS) ?: false

        log.info "Shipment ${shipment?.shipmentNumber} from ${shipment?.origin} to ${shipment.destination}"
        List outboundCreatedRoleTypes = [RoleType.ROLE_SHIPMENT_NOTIFICATION, RoleType.ROLE_SHIPMENT_OUTBOUND_CREATED_NOTIFICATION]
        List inboundCreatedRoleTypes = [RoleType.ROLE_SHIPMENT_NOTIFICATION, RoleType.ROLE_SHIPMENT_INBOUND_CREATED_NOTIFICATION]
        List inboundShippedRoleTypes = [RoleType.ROLE_SHIPMENT_NOTIFICATION, RoleType.ROLE_SHIPMENT_INBOUND_SHIPPED_NOTIFICATION]
        List outboundShippedRoleTypes = [RoleType.ROLE_SHIPMENT_NOTIFICATION, RoleType.ROLE_SHIPMENT_OUTBOUND_SHIPPED_NOTIFICATION]
        List inboundReceivedRoleTypes = [RoleType.ROLE_SHIPMENT_NOTIFICATION, RoleType.ROLE_SHIPMENT_INBOUND_RECEIVED_NOTIFICATION]
        List outboundReceivedRoleTypes = [RoleType.ROLE_SHIPMENT_NOTIFICATION, RoleType.ROLE_SHIPMENT_OUTBOUND_RECEIVED_NOTIFICATION]

        if (event.shipmentStatusCode == ShipmentStatusCode.CREATED) {
            if (originNotificationsEnabled) {
                notificationService.sendShipmentCreatedNotification(shipment, shipment.origin, outboundCreatedRoleTypes)
            }
            if (destinationNotificationsEnabled) {
                notificationService.sendShipmentCreatedNotification(shipment, shipment.destination, inboundCreatedRoleTypes)
            }
        } else if (event.shipmentStatusCode == ShipmentStatusCode.SHIPPED) {
            if (originNotificationsEnabled) {
                notificationService.sendShipmentIssuedNotification(shipment, shipment.origin, outboundShippedRoleTypes)
            }
            if (destinationNotificationsEnabled) {
                notificationService.sendShipmentIssuedNotification(shipment, shipment.destination, inboundShippedRoleTypes)
            }
            notificationService.sendShipmentItemsShippedNotification(shipment)
            // Temporarily hard-code publishing webhook events for shipped events
            webhookPublisherService.publishShippedEvent(shipment)
        } else if (event.shipmentStatusCode in [ShipmentStatusCode.RECEIVED, ShipmentStatusCode.PARTIALLY_RECEIVED]) {
            if (originNotificationsEnabled) {
                notificationService.sendShipmentReceiptNotification(shipment, shipment.origin, outboundReceivedRoleTypes)
            }
            if (destinationNotificationsEnabled) {
                notificationService.sendShipmentReceiptNotification(shipment, shipment.destination, inboundReceivedRoleTypes)
            }
            // Send notification email to recipients on completed receipt
            notificationService.sendReceiptNotifications(event?.partialReceipt)
        }
    }
}
