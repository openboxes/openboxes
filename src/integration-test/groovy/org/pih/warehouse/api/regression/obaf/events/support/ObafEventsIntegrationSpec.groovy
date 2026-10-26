package org.pih.warehouse.api.regression.obaf.events.support

import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

import java.util.concurrent.ThreadPoolExecutor

import org.pih.warehouse.api.regression.obaf.support.ObafTaskIntegrationSpec
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.MailService
import org.pih.warehouse.report.NotificationService
import org.pih.warehouse.core.WebhookPublisherService

/**
 * Base of the event / webhook / notification specs: {@link ObafTaskIntegrationSpec}'s fixtures
 * (cron triggers paused, tx with a bound user, depots, stock, orders, pick and putaway sites) plus
 * - a loopback webhook receiver wired into `openboxes.n8n` (and `openboxes.webhook`) per feature,
 * - a {@link RecordingMailService} swapped into NotificationService for the feature,
 * - helpers to turn ENABLE_WEBHOOKS on for a depot and to read posted payloads as maps.
 * Config overrides and the mail service are restored in cleanup; receivers are closed there too.
 */
abstract class ObafEventsIntegrationSpec extends ObafTaskIntegrationSpec {

    @Autowired
    WebhookPublisherService webhookPublisherService

    @Autowired
    NotificationService notificationService

    List<Closeable> closeables = []
    private MailService savedMailService

    void cleanup() {
        closeables.reverseEach { try { it.close() } catch (Exception ignored) { } }
        closeables.clear()
        if (savedMailService) {
            notificationService.mailService = savedMailService
            savedMailService = null
        }
    }

    /** A receiver on 127.0.0.1 that `openboxes.n8n` posts to (enabled, no headers unless given). */
    LoopbackReceiver webhookReceiver(Map<String, String> headers = [:]) {
        LoopbackReceiver r = receiver()
        config.set('openboxes.n8n.enabled', true)
        config.set('openboxes.n8n.endpoint.url', r.url)
        config.set('openboxes.n8n.endpoint.headers', headers)
        return r
    }

    LoopbackReceiver receiver() {
        LoopbackReceiver r = new LoopbackReceiver()
        closeables << r
        return r
    }

    RecordingMailService recordMail() {
        RecordingMailService recorder = new RecordingMailService()
        if (!savedMailService) {
            savedMailService = notificationService.mailService
        }
        notificationService.mailService = recorder
        return recorder
    }

    /** Adds activities to an existing location (e.g. ENABLE_WEBHOOKS on a depot built by pickSite). */
    void addActivities(String locationId, Collection<ActivityCode> codes) {
        tx {
            Location l = Location.get(locationId)
            l.supportedActivities = ((l.supportedActivities ?: []) as Set<String>) + (codes*.id as Set<String>)
            l.save(flush: true, failOnError: true)
        }
    }

    /** The payloads the receiver got, parsed, optionally only those mentioning {@code marker}. */
    static List<Map> payloads(LoopbackReceiver r, String marker = null) {
        return r.bodies.findAll { !marker || it.contains(marker) }.collect { new JsonSlurper().parseText(it) as Map }
    }

    /** Payloads of one eventType that mention {@code marker} (a request number, a product code...). */
    static List<Map> events(LoopbackReceiver r, String eventType, String marker) {
        return payloads(r, marker).findAll { it.eventType == eventType }
    }

    /**
     * Waits for asynchronous listeners that should NOT post (or post once only) to have had their chance.
     *
     * Every listener that posts to the receiver either runs on the committing thread (AFTER_COMMIT, so it has posted
     * before the call that committed returns) or is an @Async listener on the application's `taskExecutor`, queued
     * at commit time. So once that executor has drained, and stays drained over a few checks, there is nothing left
     * that could still post: wait for that instead of a fixed time, with a short floor and a long upper bound.
     */
    void settle(long maxMs = 15000) {
        Thread.sleep(SETTLE_FLOOR_MS)
        ThreadPoolExecutor executor = grailsApplication.mainContext.getBean('taskExecutor', ThreadPoolTaskExecutor).threadPoolExecutor
        long deadline = System.currentTimeMillis() + maxMs
        int idleChecks = 0
        while (idleChecks < SETTLE_IDLE_CHECKS) {
            if (System.currentTimeMillis() > deadline) {
                throw new AssertionError("The async executor did not drain within ${maxMs} ms " +
                        "(active ${executor.activeCount}, queued ${executor.queue.size()})")
            }
            boolean idle = executor.activeCount == 0 && executor.queue.isEmpty() &&
                    executor.completedTaskCount == executor.taskCount
            idleChecks = idle ? idleChecks + 1 : 0
            Thread.sleep(SETTLE_POLL_MS)
        }
    }

    static final long SETTLE_FLOOR_MS = 250
    static final long SETTLE_POLL_MS = 50
    static final int SETTLE_IDLE_CHECKS = 4
}
