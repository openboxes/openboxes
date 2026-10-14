package org.pih.warehouse.api.regression.obaf.history

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.TransactionStatus
import spock.lang.PendingFeature
import spock.lang.Unroll

import org.pih.warehouse.api.regression.obaf.support.ObafIntegrationSpec
import org.pih.warehouse.api.regression.support.RegressionRoleFixtures
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.User
import org.pih.warehouse.core.history.EventLog
import org.pih.warehouse.core.history.EventLogCode
import org.pih.warehouse.core.history.HistoryContext
import org.pih.warehouse.core.history.HistoryItem
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionHistoryProvider
import org.pih.warehouse.requisition.RequisitionStatus

/**
 * The requisition status timeline OBAF added (Requisition.beforeInsert/beforeUpdate ->
 * RequisitionStatusChangedEvent -> RequisitionStatusChangedEventService AFTER_COMMIT ->
 * RequisitionEventManager -> Event + EventLog; RequisitionHistoryProvider reads it back), on a real
 * database, beyond OBAF's own unit specs and ObafListenersIntegrationSpec's forward/backward case:
 * the whole lifecycle including STAGED, history order, rollbacks of the database transaction,
 * multi-step backward moves, cancel/undo, who is recorded, and the status sort-order formula.
 */
class RequisitionHistoryIntegrationSpec extends ObafIntegrationSpec {

    static final List<RequisitionStatus> LIFECYCLE = [RequisitionStatus.CREATED, RequisitionStatus.VERIFYING, RequisitionStatus.PICKING,
                                                      RequisitionStatus.PICKED, RequisitionStatus.CHECKING, RequisitionStatus.STAGED,
                                                      RequisitionStatus.ISSUED]

    @Autowired
    RequisitionHistoryProvider requisitionHistoryProvider

    void setStatus(String req, RequisitionStatus s) {
        tx { Requisition r = Requisition.get(req); r.status = s; r.save(flush: true, failOnError: true) }
    }

    List<EventCode> eventCodes(String req) {
        return tx { Requisition.get(req).events.sort { a, b -> a.dateCreated <=> b.dateCreated ?: a.id <=> b.id }*.eventType*.eventCode } as List<EventCode>
    }

    /** The history the stock movement page shows (localised names need a web request). */
    List<HistoryItem> history(String req, boolean includeRolledBack = true) {
        return withWebRequest {
            tx { requisitionHistoryProvider.getHistory(Requisition.get(req), new HistoryContext(includeRolledBackEvents: includeRolledBack)) }
        } as List<HistoryItem>
    }

    String newRequisition(String prefix) {
        return requisition(depot(prefix), [[productId: product(prefix), quantity: 1]])
    }

    // ------------------------------------------------------------------ the forward timeline

    void 'every lifecycle status, STAGED included, gets exactly one Event and one EVENT_OCCURRED log'() {
        given:
        String req = newRequisition('HI-LC')

        when:
        LIFECYCLE.drop(1).each { RequisitionStatus s -> Thread.sleep(1100); setStatus(req, s) }

        then:
        eventually { eventCodes(req).size() == 7 }
        eventCodes(req) == [EventCode.CREATED, EventCode.VERIFYING, EventCode.PICKING, EventCode.PICKED, EventCode.CHECKING,
                            EventCode.STAGED, EventCode.ISSUED]
        tx { Requisition.get(req).eventLogs.findAll { it.eventLogCode == EventLogCode.EVENT_OCCURRED }.size() } == 7

        and: 'the history lists them oldest first (statuses a second apart)'
        history(req)*.eventType*.eventCode == eventCodes(req)
    }

    @PendingFeature(reason = 'HistoryItem.groovy:40-47 orders history by dateLogged, then date, then the LOCALISED event name: the Events RequisitionEventManager records carry second-precision timestamps (datetime columns), so status changes within one second (automatic allocation, a scripted pick) come back in alphabetical order ("Checking, Created, Picked, Picking, Staged, Verifying"), not in lifecycle order')
    void 'status changes within the same second still read in lifecycle order'() {
        given:
        String req = newRequisition('HI-FAST')

        when:
        LIFECYCLE.drop(1).dropRight(1).each { RequisitionStatus s -> setStatus(req, s) }
        eventually { eventCodes(req).size() == 6 }

        then:
        history(req)*.eventType*.eventCode == [EventCode.CREATED, EventCode.VERIFYING, EventCode.PICKING, EventCode.PICKED,
                                                EventCode.CHECKING, EventCode.STAGED]
    }

    void 'two status changes flushed in one transaction both reach the timeline'() {
        given:
        String req = newRequisition('HI-2F')

        when:
        tx {
            Requisition r = Requisition.get(req)
            r.status = RequisitionStatus.VERIFYING
            r.save(flush: true, failOnError: true)
            r.status = RequisitionStatus.PICKING
            r.save(flush: true, failOnError: true)
        }

        then:
        eventually { eventCodes(req).size() == 3 }
        eventCodes(req) as Set == [EventCode.CREATED, EventCode.VERIFYING, EventCode.PICKING] as Set
    }

    void 'a status change whose transaction rolls back records nothing'() {
        given:
        String req = newRequisition('HI-RB')
        eventually { eventCodes(req) == [EventCode.CREATED] }

        when:
        Requisition.withNewTransaction { TransactionStatus st ->
            Requisition r = Requisition.get(req)
            r.status = RequisitionStatus.VERIFYING
            r.save(flush: true, failOnError: true)
            st.setRollbackOnly()
        }
        Thread.sleep(1500)

        then:
        status(req) == RequisitionStatus.CREATED
        eventCodes(req) == [EventCode.CREATED]
    }

    void 'the Event and its log are recorded as the user who changed the status'() {
        given:
        String username = "test-hist-${uid().toLowerCase()}".toString()
        RegressionRoleFixtures.ensureUser(username, [RoleType.ROLE_MANAGER], [:])
        String req = newRequisition('HI-WHO')

        when:
        actAs(username)
        setStatus(req, RequisitionStatus.VERIFYING)

        then:
        eventually { eventCodes(req).size() == 2 }
        tx { Requisition.get(req).events.find { it.eventType.eventCode == EventCode.VERIFYING }.createdBy?.username } == username
        tx { Requisition.get(req).eventLogs.find { it.eventCode == EventCode.VERIFYING }.createdBy?.username } == username
    }

    // ------------------------------------------------------------------ backward moves

    @PendingFeature(reason = 'RequisitionEventManager.groovy:48-56,116-131 rolls back only the Event of the status being LEFT: a backward move over several steps (PICKING -> CREATED, what AllocationService.deallocate does) removes PICKING but keeps VERIFYING, so the timeline of an order back at CREATED still shows "Verifying"')
    void 'a backward move over several statuses removes every Event past the new status'() {
        given:
        String req = newRequisition('HI-BK')
        setStatus(req, RequisitionStatus.VERIFYING)
        setStatus(req, RequisitionStatus.PICKING)
        eventually { eventCodes(req).size() == 3 }

        when:
        setStatus(req, RequisitionStatus.CREATED)

        then:
        eventually(5000) { eventCodes(req) == [EventCode.CREATED] }
    }

    void 'cancel and undo: CANCELED is recorded as CANCELLED; returning to the previous status rolls it back and logs the rollback'() {
        given:
        String req = newRequisition('HI-CX')
        setStatus(req, RequisitionStatus.VERIFYING)
        eventually { eventCodes(req).size() == 2 }

        when:
        setStatus(req, RequisitionStatus.CANCELED)

        then:
        eventually { eventCodes(req).contains(EventCode.CANCELLED) }

        when:
        setStatus(req, RequisitionStatus.VERIFYING)

        then:
        eventually { !eventCodes(req).contains(EventCode.CANCELLED) }
        eventCodes(req) == [EventCode.CREATED, EventCode.VERIFYING]
        tx { Requisition.get(req).eventLogs.count { EventLog l -> l.eventLogCode == EventLogCode.EVENT_ROLLBACK_OCCURRED && l.eventCode == EventCode.CANCELLED } } == 1

        and: 'the history shows the rollback only when asked to'
        history(req, true).any { it.eventType.name.endsWith(' - Rollback') }
        !history(req, false).any { it.eventType.name.endsWith(' - Rollback') }
        !history(req, false).any { it.eventType.eventCode == EventCode.CANCELLED }
    }

    // ------------------------------------------------------------------ the status sort-order formula

    @Unroll
    void 'the statusSortOrder formula matches the enum sortOrder for a #status order'() {
        given:
        String req = requisition(depot('HI-SO'), [[productId: product('SO'), quantity: 1]], [status: status])

        expect:
        tx { Requisition.get(req).statusSortOrder } == status.sortOrder

        where:
        status << (RequisitionStatus.listOutboundOptions() - [RequisitionStatus.STAGED] + [RequisitionStatus.PENDING_APPROVAL, RequisitionStatus.APPROVED])
    }

    @PendingFeature(reason = 'RequisitionStatus.groovy:176-196 getStatusSortOrderFormula has no STAGED branch, so a STAGED order\'s statusSortOrder (Requisition, OutboundStockMovement, OutboundStockMovementListItem) is 0: sorting the outbound list by status puts STAGED orders before CREATED instead of between CHECKING and ISSUED')
    void 'the statusSortOrder formula of a STAGED order is STAGED.sortOrder (between CHECKING and ISSUED)'() {
        given:
        String req = requisition(depot('HI-SOS'), [[productId: product('SOS'), quantity: 1]], [status: RequisitionStatus.STAGED])

        expect:
        tx { Requisition.get(req).statusSortOrder } == RequisitionStatus.STAGED.sortOrder
    }
}
