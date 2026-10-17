package org.pih.warehouse.api.regression.obaf.jobs

import groovy.json.JsonSlurper

import org.pih.warehouse.api.regression.obaf.support.ObafIntegrationSpec
import org.pih.warehouse.api.regression.obaf.support.QuartzRunner
import org.pih.warehouse.api.regression.support.CaptureServers
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.RequisitionEvent
import org.pih.warehouse.core.WebhookEventType
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.jobs.AutomaticIssuanceJob
import org.pih.warehouse.jobs.PutawayLocationReassignmentJob
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionStatus

/**
 * The three always-on listeners (no config gate) fire and do what they claim, on a real
 * transaction commit:
 * - TransactionCreatedEventService (@Async, every Transaction insert): an adjustment at an
 *   ENABLE_WEBHOOKS facility is published to the webhook endpoint (a loopback capture server here);
 * - RequisitionStatusChangedEventService (every requisition status change): the status Event
 *   timeline follows status, a backward move removes the later Event;
 * - InventoryLevelUpdatedEventService (every InventoryLevel update): queues
 *   PutawayLocationReassignmentJob for that level (the job's effect: ObafJobsIntegrationSpec).
 * Plus RequisitionEventService: a STAGED event schedules AutomaticIssuanceJob for that order.
 */
class ObafListenersIntegrationSpec extends ObafIntegrationSpec {

    static final Date SOON = new Date() + 30

    void 'TransactionCreatedEventService publishes an adjustment at an ENABLE_WEBHOOKS facility to the webhook endpoint'() {
        given:
        CaptureServers.Labelary receiver = new CaptureServers.Labelary()   // a loopback HTTP capture server
        config.set('openboxes.n8n.enabled', true)
        config.set('openboxes.n8n.endpoint.url', "${receiver.origin}/webhook".toString())
        config.set('openboxes.n8n.endpoint.headers', [:])
        String dc = depot('LS-WH', [ActivityCode.ENABLE_WEBHOOKS])
        String b1 = bin(dc, 'B1')
        String p = product('WH')
        String productCode = tx { Product.get(p).productCode }

        when: 'record stock writes an adjustment transaction'
        stock(dc, p, [[b1, 7, 'L', SOON]])

        then:
        eventually(20000) { receiver.bodies.any { it.contains(productCode) } }
        Map payload = new JsonSlurper().parseText(receiver.bodies.find { it.contains(productCode) }) as Map
        payload.eventType == WebhookEventType.ADJUSTMENT_CREATED.name
        payload.adjustment.product == productCode
        payload.adjustment.totals.quantityAfterAdjustment == 7
        payload.metadata.facilityId == dc

        cleanup:
        receiver?.close()
    }

    void 'TransactionCreatedEventService publishes nothing for a facility without ENABLE_WEBHOOKS'() {
        given:
        CaptureServers.Labelary receiver = new CaptureServers.Labelary()
        config.set('openboxes.n8n.enabled', true)
        config.set('openboxes.n8n.endpoint.url', "${receiver.origin}/webhook".toString())
        config.set('openboxes.n8n.endpoint.headers', [:])
        String dc = depot('LS-NOWH')
        String b1 = bin(dc, 'B1')
        String p = product('NOWH')
        String productCode = tx { Product.get(p).productCode }

        when:
        stock(dc, p, [[b1, 3, 'L', SOON]])
        Thread.sleep(3000)   // the listener is @Async: give it time to (not) post

        then:
        !receiver.bodies.any { it.contains(productCode) }

        cleanup:
        receiver?.close()
    }

    void 'RequisitionStatusChangedEventService keeps the status Event timeline in step, forwards and backwards'() {
        given:
        String dc = depot('LS-ST')
        String req = requisition(dc, [[productId: product('ST'), quantity: 1]])

        when:
        [RequisitionStatus.VERIFYING, RequisitionStatus.PICKING].each { RequisitionStatus s ->
            tx { Requisition r = Requisition.get(req); r.status = s; r.save(flush: true, failOnError: true) }
        }

        then:
        eventually { eventCodes(req) == [EventCode.CREATED, EventCode.VERIFYING, EventCode.PICKING] as Set }

        when: 'back to VERIFYING (a rollback)'
        tx { Requisition r = Requisition.get(req); r.status = RequisitionStatus.VERIFYING; r.save(flush: true, failOnError: true) }

        then:
        eventually { eventCodes(req) == [EventCode.CREATED, EventCode.VERIFYING] as Set }
    }

    Set<EventCode> eventCodes(String requisitionId) {
        return tx { Requisition.get(requisitionId).events*.eventType*.eventCode as Set } as Set<EventCode>
    }

    void 'InventoryLevelUpdatedEventService queues PutawayLocationReassignmentJob for the updated level'() {
        given:
        String dc = depot('LS-IL')
        String slot = bin(dc, 'SLOT')
        String p = product('IL')
        String level = tx {
            new InventoryLevel(product: Product.get(p), inventory: Location.get(dc).inventory).save(flush: true, failOnError: true).id
        }
        List<String> seen = Collections.synchronizedList([])
        org.quartz.listeners.JobListenerSupport spy = new org.quartz.listeners.JobListenerSupport() {
            String getName() { 'test-il-spy' }

            void jobToBeExecuted(org.quartz.JobExecutionContext context) {
                seen << (context.mergedJobDataMap.get('inventoryLevelId') as String)
            }
        }
        scheduler().listenerManager.addJobListener(spy,
                org.quartz.impl.matchers.KeyMatcher.keyEquals(QuartzRunner.jobKey(scheduler(), PutawayLocationReassignmentJob)))

        when:
        tx { InventoryLevel il = InventoryLevel.get(level); il.preferredBinLocation = Location.get(slot); il.save(flush: true, failOnError: true) }

        then:
        eventually { seen.contains(level) }

        cleanup:
        scheduler().listenerManager.removeJobListener('test-il-spy')
    }

    void 'RequisitionEventService: a STAGED event schedules AutomaticIssuanceJob for that order ~1 s after the availability refresh delay'() {
        given:
        String dc = depot('LS-STAGED')
        String req = requisition(dc, [[productId: product('STG'), quantity: 1]])
        long before = System.currentTimeMillis()

        when:
        tx { grailsApplication.mainContext.publishEvent(new RequisitionEvent(req, WebhookEventType.REQUISITION_STAGED)) }

        then:
        org.quartz.Trigger trigger = eventually {
            scheduler().getTriggersOfJob(QuartzRunner.jobKey(scheduler(), AutomaticIssuanceJob)).find { it.jobDataMap.get('requisitionId') == req }
        }
        trigger.startTime.time >= before + 5000 + 1000
    }
}
