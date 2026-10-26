package org.pih.warehouse.api.regression.obaf.events

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.TransactionStatus
import spock.lang.PendingFeature

import org.pih.warehouse.api.regression.obaf.events.support.LoopbackReceiver
import org.pih.warehouse.api.regression.obaf.events.support.ObafEventsIntegrationSpec
import org.pih.warehouse.api.regression.obaf.support.ObafTaskIntegrationSpec.PickOrder
import org.pih.warehouse.api.regression.obaf.support.ObafTaskIntegrationSpec.PickSite
import org.pih.warehouse.api.regression.obaf.support.QuartzRunner
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.DeliveryTypeCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.RequisitionEvent
import org.pih.warehouse.core.User
import org.pih.warehouse.core.WebhookEventType
import org.pih.warehouse.jobs.AutomaticIssuanceJob
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionService
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.requisition.RequisitionType

/**
 * WHEN the requisition events reach the webhook receiver, on a real database and real commits, at a depot with
 * ENABLE_WEBHOOKS: requisition.created (Requisition.afterInsert -> RequisitionEventService,
 * AFTER_COMMIT), pick.started (PickTaskService.start, synchronous), requisition.staged (the drop
 * that stages the order -> RequisitionEventService, AFTER_COMMIT), requisition.issued, and the two
 * enum values nothing sends (pick.completed, shipment.received). What the payload contains is in
 * WebhookDeliveryIntegrationSpec.
 */
class RequisitionWebhookEventsIntegrationSpec extends ObafEventsIntegrationSpec {

    @Autowired
    RequisitionService requisitionService

    String number(String requisitionId) {
        return tx { Requisition.get(requisitionId).requestNumber } as String
    }

    /** A requisition built and flushed in a transaction that is then rolled back; returns its request number. */
    String rolledBackRequisition(String depotId, String productId) {
        String n = "LORB${uid()}".toString()
        String elsewhere = destination()
        Requisition.withNewTransaction { TransactionStatus st ->
            authService.setCurrentUser(User.findByUsername('admin'))
            Requisition r = new Requisition(name: "Test rolled back ${n}", requestNumber: n, origin: Location.get(depotId),
                    destination: Location.get(elsewhere), requestedBy: Person.get('1'), dateRequested: new Date(),
                    requestedDeliveryDate: new Date() + 1, status: RequisitionStatus.CREATED, type: RequisitionType.DEFAULT,
                    deliveryTypeCode: DeliveryTypeCode.LOCAL_DELIVERY, autoAllocationRequested: false)
            r.addToRequisitionItems(new RequisitionItem(product: Product.get(productId), quantity: 1, quantityApproved: 1,
                    substitutable: false, orderIndex: 0))
            r.save(flush: true, failOnError: true)
            st.setRollbackOnly()
        }
        return n
    }

    // ------------------------------------------------------------------ requisition.created

    void 'requisition.created is posted once, after commit, for an order created at an ENABLE_WEBHOOKS depot, with its lines'() {
        given:
        String dc = depot('WH-CR', [ActivityCode.ENABLE_WEBHOOKS])
        LoopbackReceiver r = webhookReceiver()

        when:
        String req = requisition(dc, [[productId: product('CR1'), quantity: 4], [productId: product('CR2'), quantity: 1]])
        String n = number(req)

        then:
        eventually { events(r, 'requisition.created', n) }
        settle()
        events(r, 'requisition.created', n).size() == 1
        events(r, 'requisition.created', n)[0].lines*.quantityRequested.sort() == [1, 4]
    }

    void 'requisition.created: an insert that is rolled back posts nothing'() {
        given:
        String dc = depot('WH-CRRB', [ActivityCode.ENABLE_WEBHOOKS])
        String p = product('CRRB')
        LoopbackReceiver r = webhookReceiver()

        when:
        String n = rolledBackRequisition(dc, p)
        settle()

        then:
        tx { Requisition.findByRequestNumber(n) } == null
        payloads(r, n).isEmpty()
    }

    void 'requisition.created is not sent again when the order is updated afterwards'() {
        given:
        String dc = depot('WH-CRUP', [ActivityCode.ENABLE_WEBHOOKS])
        LoopbackReceiver r = webhookReceiver()
        String req = requisition(dc, [[productId: product('CRUP'), quantity: 2]])
        String n = number(req)
        eventually { events(r, 'requisition.created', n) }

        when:
        tx { Requisition q = Requisition.get(req); q.status = RequisitionStatus.VERIFYING; q.description = 'edited'; q.save(flush: true, failOnError: true) }
        settle()

        then:
        payloads(r, n)*.eventType == ['requisition.created']
    }

    void 'the receiver being down never rolls the order back'() {
        given:
        String dc = depot('WH-DOWN', [ActivityCode.ENABLE_WEBHOOKS])
        config.set('openboxes.n8n.enabled', true)
        config.set('openboxes.n8n.endpoint.url', "http://127.0.0.1:${LoopbackReceiver.closedPort()}/webhook".toString())
        config.set('openboxes.n8n.endpoint.headers', [:])

        when:
        String req = requisition(dc, [[productId: product('DOWN'), quantity: 2]])

        then:
        tx { Requisition.get(req)?.requisitionItems?.size() } == 1
    }

    // ------------------------------------------------------------------ the pick flow: pick.started, requisition.staged

    void 'pick flow at an ENABLE_WEBHOOKS depot: created, then pick.started per task, then requisition.staged once when the last task is dropped'() {
        given:
        PickSite s = pickSite('WH-PK')
        addActivities(s.dc, [ActivityCode.ENABLE_WEBHOOKS])
        LoopbackReceiver r = webhookReceiver()
        PickOrder o = pickOrder(s, [3, 2])
        String picker = person()
        eventually { events(r, 'requisition.created', o.requestNumber) }

        when:
        o.taskIds.each { assert !pickPatch(it, [action: 'start', assigneeId: picker]) }
        o.taskIds.each { assert !pickPatch(it, [action: 'pick', outboundContainerId: s.tote, pickedById: picker]) }
        tx { pickTaskService.drop(s.tote, [action: 'drop', stagingLocationId: s.staging, stagedById: picker]) }

        then:
        status(o.requisitionId) == RequisitionStatus.STAGED
        eventually { events(r, 'requisition.staged', o.requestNumber) }
        settle()
        payloads(r, o.requestNumber)*.eventType == ['requisition.created', 'pick.started', 'pick.started', 'requisition.staged']

        and: 'the staged payload shows the staging lane and who staged each line (the signed-in user)'
        Map staged = events(r, 'requisition.staged', o.requestNumber)[0]
        staged.lines[0].quantityPicked == 5
        staged.lines[0].stagingLocations*.id == [s.staging]
        staged.lines[0].stagedBy*.id == [adminId()]
        staged.lines[0].outboundContainers*.id == [s.tote]

        and: 'nothing ever sends pick.completed (no emitter), even though every task was picked'
        events(r, 'pick.completed', o.requestNumber).isEmpty()
    }

    @PendingFeature(reason = 'PickTaskService.groovy:234 publishes pick.started synchronously inside start(), before the PicklistItem changes and before the transaction commits: a start that is rolled back (a later failure in the same request) has already told the webhook receiver the pick started')
    void 'pick.started is not sent for a start that is rolled back'() {
        given:
        PickSite s = pickSite('WH-PKRB')
        addActivities(s.dc, [ActivityCode.ENABLE_WEBHOOKS])
        LoopbackReceiver r = webhookReceiver()
        PickOrder o = pickOrder(s, [3])
        String picker = person()

        when:
        Requisition.withNewTransaction { TransactionStatus st ->
            authService.setCurrentUser(User.findByUsername('admin'))
            pickTaskService.patch(o.taskIds[0], [action: 'start', assigneeId: picker])
            st.setRollbackOnly()
        }
        settle()

        then:
        pickTask(o.taskIds[0]).status == 'PENDING'
        events(r, 'pick.started', o.requestNumber).isEmpty()
    }

    @PendingFeature(reason = 'PickTaskService.groovy:225-234 allows PICKING -> PICKING for the same assignee (a mobile retry of start) and publishes pick.started on every call: the webhook receiver gets a duplicate pick.started for one task')
    void 'a retried start of a task already PICKING by the same assignee sends no second pick.started'() {
        given:
        PickSite s = pickSite('WH-PKDUP')
        addActivities(s.dc, [ActivityCode.ENABLE_WEBHOOKS])
        LoopbackReceiver r = webhookReceiver()
        PickOrder o = pickOrder(s, [3])
        String picker = person()

        when:
        assert !pickPatch(o.taskIds[0], [action: 'start', assigneeId: picker])
        assert !pickPatch(o.taskIds[0], [action: 'start', assigneeId: picker])

        then:
        events(r, 'pick.started', o.requestNumber).size() == 1
    }

    void 'a refused start (task assigned to someone else) sends nothing'() {
        given:
        PickSite s = pickSite('WH-PKNO')
        addActivities(s.dc, [ActivityCode.ENABLE_WEBHOOKS])
        PickOrder o = pickOrder(s, [3])
        assert !pickPatch(o.taskIds[0], [action: 'start', assigneeId: person()])
        LoopbackReceiver r = webhookReceiver()

        when:
        Throwable refused = pickPatch(o.taskIds[0], [action: 'start', assigneeId: person()])

        then:
        refused != null
        events(r, 'pick.started', o.requestNumber).isEmpty()
    }

    // ------------------------------------------------------------------ requisition.staged / requisition.issued, directly

    void 'requisition.staged published in a transaction that rolls back posts nothing and schedules no issuance'() {
        given:
        String dc = depot('WH-STRB', [ActivityCode.ENABLE_WEBHOOKS])
        String req = requisition(dc, [[productId: product('STRB'), quantity: 1]])
        String n = number(req)
        LoopbackReceiver r = webhookReceiver()

        when:
        Requisition.withNewTransaction { TransactionStatus st ->
            grailsApplication.mainContext.publishEvent(new RequisitionEvent(req, WebhookEventType.REQUISITION_STAGED))
            st.setRollbackOnly()
        }
        settle()

        then:
        events(r, 'requisition.staged', n).isEmpty()
        !scheduler().getTriggersOfJob(QuartzRunner.jobKey(scheduler(), AutomaticIssuanceJob)).any { it.jobDataMap.get('requisitionId') == req }
    }

    void 'characterization: a RequisitionEvent published outside any transaction is handled at once (fallbackExecution)'() {
        given:
        String dc = depot('WH-STNT', [ActivityCode.ENABLE_WEBHOOKS])
        String req = requisition(dc, [[productId: product('STNT'), quantity: 1]])
        String n = number(req)
        LoopbackReceiver r = webhookReceiver()

        when:
        Requisition.withNewSession {
            grailsApplication.mainContext.publishEvent(new RequisitionEvent(req, WebhookEventType.REQUISITION_STAGED))
        }

        then:
        events(r, 'requisition.staged', n).size() == 1
    }

    @PendingFeature(reason = 'RequisitionService.groovy:905 publishes REQUISITION_ISSUED when an order is issued, but RequisitionEventService.groovy:23-35 has no case for it (only STAGED and CREATED): requisition.issued is a declared WebhookEventType that never reaches the webhook receiver')
    void 'issuing an order sends requisition.issued'() {
        given:
        String dc = depot('WH-ISS', [ActivityCode.ENABLE_WEBHOOKS])
        String req = requisition(dc, [[productId: product('ISS'), quantity: 1]], [status: RequisitionStatus.STAGED])
        String n = number(req)
        LoopbackReceiver r = webhookReceiver()

        when:
        tx { requisitionService.triggerRequisitionStatusTransition(Requisition.get(req), User.findByUsername('admin'), RequisitionStatus.ISSUED) }

        then:
        status(req) == RequisitionStatus.ISSUED
        eventually(5000) { events(r, 'requisition.issued', n) }
    }

    void 'characterization: the declared requisition.issued, pick.completed and shipment.received never reach the webhook receiver through RequisitionEventService'() {
        given:
        String dc = depot('WH-NONE', [ActivityCode.ENABLE_WEBHOOKS])
        String req = requisition(dc, [[productId: product('NONE'), quantity: 1]])
        String n = number(req)
        LoopbackReceiver r = webhookReceiver()

        when:
        [WebhookEventType.REQUISITION_ISSUED, WebhookEventType.PICK_COMPLETED, WebhookEventType.SHIPMENT_RECEIVED, WebhookEventType.PICK_STARTED].each { t ->
            tx { grailsApplication.mainContext.publishEvent(new RequisitionEvent(req, t)) }
        }
        settle()

        then: 'only CREATED and STAGED are handled'
        payloads(r, n).isEmpty()
    }
}
