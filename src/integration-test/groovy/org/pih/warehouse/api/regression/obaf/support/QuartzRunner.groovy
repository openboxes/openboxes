package org.pih.warehouse.api.regression.obaf.support

import org.quartz.JobDataMap
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.Trigger
import org.quartz.impl.matchers.GroupMatcher
import org.quartz.impl.matchers.KeyMatcher
import org.quartz.listeners.JobListenerSupport

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Fires an OBAF job through the application's RUNNING Quartz scheduler (Grails quartz plugin,
 * GrailsJobFactory), i.e. on a Quartz worker thread with exactly the session handling the plugin
 * gives the job in production (all eight OBAF jobs declare sessionRequired = false), and waits
 * for that one execution to finish.
 *
 * Each firing carries a unique `regressionRunId` in its JobDataMap so the listener matches this
 * execution and not a concurrent one (the jobs ignore unknown keys).
 */
class QuartzRunner {

    /** The JobDataMap key that marks a run fired here (see ObafIntegrationSpec.vetoEventScheduledJobs). */
    static final String RUN_ID = 'regressionRunId'

    static class Run {
        JobExecutionException exception
        boolean finished
        long millis
    }

    static JobKey jobKey(Scheduler scheduler, Class jobClass) {
        JobKey key = scheduler.getJobKeys(GroupMatcher.anyJobGroup()).find { it.name == jobClass.name }
        assert key: "no Quartz job registered for ${jobClass.name}"
        return key
    }

    static Run fire(Scheduler scheduler, Class jobClass, Map data = [:], long timeoutMs = 60000) {
        JobKey key = jobKey(scheduler, jobClass)
        String runId = UUID.randomUUID().toString()
        CountDownLatch done = new CountDownLatch(1)
        Run run = new Run()
        JobListenerSupport listener = new JobListenerSupport() {
            @Override
            String getName() { return "test-quartz-runner-${runId}" }

            @Override
            void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
                if (context.mergedJobDataMap.get(RUN_ID) == runId) {
                    run.exception = jobException
                    done.countDown()
                }
            }
        }
        scheduler.listenerManager.addJobListener(listener, KeyMatcher.keyEquals(key))
        long start = System.currentTimeMillis()
        try {
            JobDataMap map = new JobDataMap(data)
            map.put(RUN_ID, runId)
            scheduler.triggerJob(key, map)
            run.finished = done.await(timeoutMs, TimeUnit.MILLISECONDS)
        } finally {
            scheduler.listenerManager.removeJobListener(listener.name)
            run.millis = System.currentTimeMillis() - start
        }
        assert run.finished: "${jobClass.simpleName} did not finish within ${timeoutMs} ms"
        return run
    }

    /** JobDataMaps of the triggers currently scheduled for the job (e.g. by a listener's schedule()/triggerNow()). */
    static List<Map> pendingTriggerData(Scheduler scheduler, Class jobClass) {
        JobKey key = jobKey(scheduler, jobClass)
        return scheduler.getTriggersOfJob(key).collect { Trigger t -> new LinkedHashMap(t.jobDataMap ?: [:]) }
    }
}
