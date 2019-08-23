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


import grails.plugin.quartz2.TriggerHelper
import org.quartz.JobDetail
import org.quartz.JobKey
import org.quartz.Trigger
import org.quartz.TriggerKey
import org.quartz.impl.StdScheduler

import java.text.ParseException

class JobsController {

    StdScheduler quartzScheduler

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        log.info "Jobs"

        Set<JobKey> jobKeys = quartzScheduler.getJobKeys()

        [jobKeys: jobKeys]
    }

    def show = {
        JobKey jobKey = JobKey.jobKey(params.id)
        JobDetail jobDetail = quartzScheduler.getJobDetail(jobKey)

        log.info(jobKey)
        log.info(jobDetail)
        def triggers = quartzScheduler.getTriggersOfJob(jobKey)

        [jobDetail: jobDetail, jobKey: jobKey, triggers: triggers]
    }


    def unscheduleJob = {
        // find jobKey of job
        JobKey jobKey = JobKey.jobKey(params.id)
        if (jobKey) {
            JobDetail jobDetail = quartzScheduler.getJobDetail(jobKey)

            // get list of existing triggers
            def triggersList = quartzScheduler.getTriggersOfJob(jobKey)
            triggersList.each {
                log.info "Unscheduling trigger " + it
                // remove all existing triggers
                quartzScheduler.unscheduleJob(it.key)
            }
        } else {
            flash.message = "Unable to find job with jobKey = ${params.id}"
        }
        redirect(action: "show", id: params.id)
    }

    def unscheduleTrigger = {
        // find jobKey of job

        TriggerKey triggerKey = TriggerKey.triggerKey(params.id)
        Trigger trigger = quartzScheduler.getTrigger(triggerKey)
        JobKey jobKey = trigger.jobKey
        if (trigger) {
            quartzScheduler.unscheduleJob(triggerKey)
        } else {
            flash.message = "Unable to unschedule trigger with trigger key ${params.id}"
        }
        redirect(action: "show", id: jobKey.name)
    }

    def scheduleJob = {
        JobKey jobKey = JobKey.jobKey(params.id)
        if (jobKey) {
            // cronExpression 0 0 22 * * ?
            try {
                Trigger trigger = TriggerHelper.cronTrigger(jobKey, params.cronExpression, [:])
                def date = quartzScheduler.scheduleJob(trigger)
                flash.message = "Job ${jobKey} scheduled " + date
            } catch (ParseException e) {
                flash.message = "Unable to schedule job with cron expression ${params.cronExpression} due to the following error: " + e.message
            }
        } else {
            flash.message = "Unable to find job with jobKey = ${params.id}"
        }

        redirect(action: "show", id: params.id)

    }

}
