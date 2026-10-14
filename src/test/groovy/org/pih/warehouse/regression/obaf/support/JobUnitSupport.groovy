package org.pih.warehouse.regression.obaf.support

import org.quartz.JobDataMap
import org.quartz.JobExecutionContext

/**
 * Helpers for the job unit specs: a JobExecutionContext carrying a merged JobDataMap, and
 * recorders for the Quartz plugin's static `triggerNow` / `schedule` on a job class (replaced
 * through the metaclass, so nothing reaches a scheduler).
 */
trait JobUnitSupport {

    JobExecutionContext context(Map data = [:]) {
        JobDataMap map = new JobDataMap(data)
        return [getMergedJobDataMap: { -> map }] as JobExecutionContext
    }

    /** Records every `Job.triggerNow(map)` into the returned list. */
    List<Map> recordTriggerNow(Class jobClass) {
        List<Map> calls = []
        jobClass.metaClass.static.triggerNow = { Map params -> calls << params }
        return calls
    }

    /** Records every `Job.schedule(Date, map)` into the returned list as [runAt: date, params: map]. */
    List<Map> recordSchedule(Class jobClass) {
        List<Map> calls = []
        jobClass.metaClass.static.schedule = { Date runAt, Map params -> calls << [runAt: runAt, params: params] }
        return calls
    }

    void resetMetaClasses(Class... classes) {
        classes.each { GroovySystem.metaClassRegistry.removeMetaClass(it) }
    }
}
