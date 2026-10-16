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

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationService
import org.pih.warehouse.replenishment.ReplenishmentService
import org.quartz.JobExecutionContext

/**
 * Generates replenishment lists for bins that have fallen below their minimum level.
 *
 * Triggered ad hoc for a single facility (job data "facilityId" and "targetActivityCodes") after picking
 * completes, and on a cron schedule as a safety net that sweeps all depots.
 */
class AutomaticReplenishmentJob {

    // Locations replenished by the scheduled sweep
    static final List<ActivityCode> SCHEDULED_TARGET_ACTIVITY_CODES = [ActivityCode.DISPLAY_STOCK]

    ReplenishmentService replenishmentService
    LocationService locationService
    AuthService authService

    def sessionRequired = false

    static concurrent = false

    static triggers = {
        cron name: JobUtils.getCronName(AutomaticReplenishmentJob),
                cronExpression: JobUtils.getCronExpression(AutomaticReplenishmentJob)
    }

    def execute(JobExecutionContext context) {
        if (!JobUtils.shouldExecute(AutomaticReplenishmentJob)) {
            log.info "Automatic replenishment job is disabled"
            return
        }

        String facilityId = context.mergedJobDataMap.get('facilityId')
        if (facilityId) {
            List<String> activityCodes = context.mergedJobDataMap.get('targetActivityCodes') as List<String>
            generateReplenishment(facilityId, activityCodes?.collect { ActivityCode.valueOf(it) })
            return
        }

        List<String> depotIds = authService.withSystemUser {
            locationService.getDepots().findAll { Location depot ->
                depot.active && hasTargetLocations(depot, SCHEDULED_TARGET_ACTIVITY_CODES)
            }*.id
        } as List<String>
        log.info "Found ${depotIds.size()} depots with locations to replenish automatically"

        depotIds.each { String depotId ->
            try {
                generateReplenishment(depotId, SCHEDULED_TARGET_ACTIVITY_CODES)
            } catch (Exception e) {
                // Don't let one failing depot stop the rest of the sweep.
                log.error("Error generating automatic replenishment for location ${depotId}", e)
            }
        }
    }

    void generateReplenishment(String facilityId, List<ActivityCode> targetActivityCodes) {
        authService.withSystemUser {
            Location facility = Location.get(facilityId)
            if (!facility) {
                log.warn("Location ${facilityId} not found, skipping automatic replenishment")
                return
            }
            replenishmentService.generateAutomaticReplenishment(facility, targetActivityCodes)
        }
    }

    private boolean hasTargetLocations(Location depot, List<ActivityCode> targetActivityCodes) {
        ActivityCode[] activityCodes = targetActivityCodes as ActivityCode[]
        return locationService.getInternalLocations(depot).any { Location internalLocation ->
            internalLocation.supportsAny(activityCodes)
        }
    }
}
