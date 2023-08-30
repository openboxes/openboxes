package org.pih.warehouse.jobs

import grails.util.Holders
import org.apache.commons.lang.WordUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import util.LiquibaseUtil

class JobUtils {

    private static final transient Logger log = LoggerFactory.getLogger(JobUtils)

    private static String getKey(Class clazz) {
        return WordUtils.uncapitalize(clazz.simpleName)
    }

    /** Return true if the given job class can run safely at the present time. */
    static boolean shouldExecute(Class clazz) {
        if (!Holders.flatConfig["openboxes.jobs.${getKey(clazz)}.enabled"]) {
            log.info "Job ${getKey(clazz)} is disabled"
            return false
        }
        if (LiquibaseUtil.isRunningMigrations()) {
            log.info "Postponing job execution for ${getKey(clazz)} until liquibase migrations are complete"
            return false
        }
        log.info "Job ${getKey(clazz)} is enabled"
        return true
    }

    static String getCronName(Class clazz) {
        return "${getKey(clazz)}CronTrigger"
    }

    static String getCronExpression(Class clazz) {
        def cronExpression = Holders.flatConfig["openboxes.jobs.${getKey(clazz)}.cronExpression"]
        log.info "Setting cron expression: ${cronExpression} for job ${getKey(clazz)}"
        return cronExpression
    }
}
