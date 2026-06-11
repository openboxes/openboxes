/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.jobs

import org.pih.warehouse.api.Putaway
import org.pih.warehouse.api.PutawayItem
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationService
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.putaway.PutawayService
import org.quartz.JobExecutionContext

class PutawayLocationReslottingJob {

    PutawayService putawayService
    LocationService locationService

    static concurrent = true

    def sessionRequired = false

    // Should never be triggered on a schedule - should only be triggered by persistence event listener
    static triggers = {}

    void execute(JobExecutionContext context) {
        if (!JobUtils.shouldExecute(PutawayLocationReslottingJob)) {
            log.info("Reslotting Putaway location job is disabled")
            return
        }

        String inventoryLevelId = context.mergedJobDataMap.get('inventoryLevelId')
        InventoryLevel inventoryLevel = InventoryLevel.read(inventoryLevelId)
        if (!inventoryLevel) {
            log.warn "InventoryLevel with id ${inventoryLevelId} not found, cannot trigger reslotting"
            return
        }
        if (inventoryLevel.preferredBinLocation?.supports(ActivityCode.UNDEFINED_LOCATION)) {
            // the update hasn't changed preferredBinLocation type from UNDEFINED; no reslotting
            return
        }
        if (inventoryLevel.internalLocation?.supports(ActivityCode.UNDEFINED_LOCATION)) {
            // the update hasn't changed internalLocation type from UNDEFINED; no reslotting
            return
        }

        log.info("Reslotting Putaway location for " + inventoryLevel.product)
        List<Location> binLocations = locationService.getLocationsSupportingActivity(ActivityCode.UNDEFINED_LOCATION)
        List<Putaway> putaways = putawayService.getPutawayOrders(inventoryLevel.product, binLocations, inventoryLevel.inventory.warehouse)
        putaways?.each { Putaway putaway ->
            List<PutawayItem> putawayItems = putaway.putawayItems.findAll { it.putawayLocation?.supports(ActivityCode.UNDEFINED_LOCATION) }
            putawayItems?.each { it.putawayLocation = inventoryLevel.preferredBinLocation ?: inventoryLevel.internalLocation }
            putawayService.savePutaway(putaway)
        }
    }
}
