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
 * Triggered ad hoc for a single facility (job data "facilityId") after picking completes, and on a cron schedule
 * as a safety net that sweeps all depots. Both replenish the locations supporting the configured
 * openboxes.jobs.automaticReplenishmentJob.targetActivityCodes (job data "targetActivityCodes" overrides them
 * for an ad hoc run), and do nothing when there are none.
 */
class AutomaticReplenishmentJob {
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
            // Codes given in the job data override the configured ones
            List<ActivityCode> targetActivityCodes = replenishmentService.parseActivityCodes(
                    context.mergedJobDataMap.get('targetActivityCodes') as List<String>, "job data")
            if (!targetActivityCodes) {
                targetActivityCodes = replenishmentService.getTargetActivityCodes()
            }
            if (!targetActivityCodes) {
                log.info "No target activity codes given or configured (openboxes.jobs.automaticReplenishmentJob.targetActivityCodes), " +
                        "skipping automatic replenishment for location ${facilityId}"
                return
            }
            generateReplenishment(facilityId, targetActivityCodes)
            // The AutomaticReplenishmentJob works in twofolds. One time it works with PICK_COMPLETED RequisitionEvent.
            // In this case a specific facilityId is given. Then it returns  here. And the other time it works as a fallback
            // mechanism where it checks everything (below).
            return
        }

        List<ActivityCode> targetActivityCodes = replenishmentService.getTargetActivityCodes()
        if (!targetActivityCodes) {
            log.info "No target activity codes configured (openboxes.jobs.automaticReplenishmentJob.targetActivityCodes), " +
                    "skipping automatic replenishment"
            return
        }
        List<String> facilityIds = authService.withSystemUser {
            locationService.getDepots().findAll { Location depot ->
                depot.active && hasTargetLocations(depot, targetActivityCodes)
            }*.id
        } as List<String>
        log.info "Found ${facilityIds.size()} facilities with locations to replenish automatically"

        facilityIds.each { String depotId ->
            try {
                generateReplenishment(depotId, targetActivityCodes)
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
