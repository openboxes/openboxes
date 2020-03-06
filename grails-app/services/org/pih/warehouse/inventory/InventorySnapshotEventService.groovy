/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional
import org.springframework.context.ApplicationListener

@Transactional
class InventorySnapshotEventService implements ApplicationListener<InventorySnapshotEvent> {

    def productAvailabilityService
    def inventorySnapshotService

    void onApplicationEvent(InventorySnapshotEvent event) {
        log.info "Application event ${event} has been published"

        if (event?.disableRefresh) {
            log.info "Application event been disabled by event publisher"
            log.info "Application event " + event.properties
            return
        }

        if (event?.inventoryItem) {
            productAvailabilityService.updateProductAvailability(event?.inventoryItem)
            inventorySnapshotService.updateInventorySnapshots(event?.inventoryItem)
        }
        if (event?.binLocation) {
            productAvailabilityService.updateProductAvailability(event?.binLocation)
            inventorySnapshotService.updateInventorySnapshots(event?.binLocation)
        }
        if (event?.product) {
            productAvailabilityService.updateProductAvailability(event?.product)
            inventorySnapshotService.updateInventorySnapshots(event?.product)
        }
    }
}
