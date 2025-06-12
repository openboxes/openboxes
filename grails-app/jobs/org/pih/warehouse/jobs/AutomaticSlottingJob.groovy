package org.pih.warehouse.jobs

import grails.util.Holders
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.quartz.DisallowConcurrentExecution
import org.quartz.JobExecutionContext

@DisallowConcurrentExecution
class AutomaticSlottingJob {

    def putawayService
    def locationService

    static triggers = {
        cron name: 'autoSlottingJobCronTrigger',
                cronExpression: Holders.config.openboxes.jobs.automaticSlottingJob.cronExpression
    }

    def execute(JobExecutionContext context) {

        if (!Holders.config.openboxes.jobs.automaticSlottingJob.enabled) {
            log.info"Automatic slotting job is disabled"
            return
        }

        log.info "Running automatic slotting job ... "
        List<Location> locations = locationService.getLocationsSupportingActivities([ActivityCode.DYNAMIC_SLOTTING, ActivityCode.STATIC_SLOTTING])
        locations.each { Location location ->

            def users = putawayService.getPutawayUsers(location)
            if (!users || users.empty) {
                log.warn("Unable to find a suitable putaway assignee for putaway orders")
                return
            }

            // Eventually we could use a round-robin algorithm to assign the user
            putawayService.generatePutaways(location, users[0])
        }
    }
}
