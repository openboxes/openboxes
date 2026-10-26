package org.pih.warehouse.api.regression.obaf.picking

import grails.validation.ValidationException
import spock.lang.PendingFeature

import org.pih.warehouse.api.PickTaskStatus
import org.pih.warehouse.api.regression.obaf.support.ObafTaskIntegrationSpec
import org.pih.warehouse.api.picking.SearchPickTaskCommand
import org.pih.warehouse.core.DeliveryTypeCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.picking.PickTask
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.shipping.Shipment

/**
 * PickTaskService behaviour on a real database: start, pick, short pick, reallocate, drop to
 * staging, the order status that follows, and the stock moved when the facility tracks internal
 * transactions (bin -> tote on pick, tote -> staging lane on drop).
 */
class PickTaskIntegrationSpec extends ObafTaskIntegrationSpec {

    // ------------------------------------------------------------------ start

    void 'start: PENDING -> PICKING, assignee and dates recorded; the same assignee may start again'() {
        given:
        PickSite s = pickSite('PT-ST')
        PickOrder o = pickOrder(s, [5])
        String picker = person()

        when:
        Throwable first = pickPatch(o.taskIds[0], [action: 'start', assigneeId: picker])
        Throwable again = pickPatch(o.taskIds[0], [action: 'start', assigneeId: picker])
        Map t = pickTask(o.taskIds[0])

        then:
        first == null
        again == null
        t.status == 'PICKING'
        t.assignee == picker
        t.dateStarted != null
        status(o.requisitionId) == RequisitionStatus.PICKING
    }

    void 'start: a task already assigned to someone else is refused and stays theirs'() {
        given:
        PickSite s = pickSite('PT-ST2')
        PickOrder o = pickOrder(s, [5])
        String first = person()
        String second = person()
        pickPatch(o.taskIds[0], [action: 'start', assigneeId: first])

        when:
        Throwable refused = pickPatch(o.taskIds[0], [action: 'start', assigneeId: second])

        then:
        refused instanceof ValidationException
        refused.message.contains('already assigned')
        pickTask(o.taskIds[0]).assignee == first
    }

    void 'an unknown action or an unknown task is refused'() {
        given:
        PickSite s = pickSite('PT-UA')
        PickOrder o = pickOrder(s, [5])

        expect:
        pickPatch(o.taskIds[0], [action: 'explode']) instanceof UnsupportedOperationException
        pickPatch(o.taskIds[0], [:]) instanceof UnsupportedOperationException
        rootCause(pickPatch('no-such-task', [action: 'start'])) instanceof org.hibernate.ObjectNotFoundException
    }

    // ------------------------------------------------------------------ pick

    void 'pick: moves the required quantity from the bin into the tote, PICKED; the order is PICKED once every task is'() {
        given:
        PickSite s = pickSite('PT-PK')
        PickOrder o = pickOrder(s, [3, 4])
        String picker = person()

        when: 'the first of two tasks is picked'
        Throwable e1 = pickPatch(o.taskIds[0], [action: 'pick', outboundContainerId: s.tote, pickedById: picker])

        then: 'the signed-in user is recorded as the picker, whatever pickedById says'
        e1 == null
        pickTask(o.taskIds[0]).subMap(['status', 'quantityPicked', 'outboundContainer', 'pickedBy']) ==
                [status: 'PICKED', quantityPicked: 3, outboundContainer: 'TOTE', pickedBy: adminId()]
        onHand(s.dc, s.product) == [BIN: 17, ALT: 20, TOTE: 3]
        status(o.requisitionId) == RequisitionStatus.PICKING

        when: 'the second, addressed by the tote NUMBER instead of its id'
        String toteNumber = tx { Location.get(s.tote).locationNumber }
        Throwable e2 = pickPatch(o.taskIds[1], [action: 'pick', outboundContainerId: toteNumber, pickedById: picker])

        then:
        e2 == null
        onHand(s.dc, s.product) == [BIN: 13, ALT: 20, TOTE: 7]
        status(o.requisitionId) == RequisitionStatus.PICKED
    }

    void 'pick: an unknown tote, a tote of another facility, or one that is not an outbound container, holds no stock or serves another delivery type, is refused'() {
        given:
        PickSite s = pickSite('PT-PKW')
        PickOrder o = pickOrder(s, [2])
        String wrongLane = bin(s.dc, 'TOTE-PU', [org.pih.warehouse.core.ActivityCode.OUTBOUND_CONTAINER,
                                                  org.pih.warehouse.core.ActivityCode.HOLD_STOCK,
                                                  org.pih.warehouse.core.ActivityCode.DELIVERY_TYPE_PICKUP], false)
        String noHold = bin(s.dc, 'TOTE-NH', [org.pih.warehouse.core.ActivityCode.OUTBOUND_CONTAINER,
                                               org.pih.warehouse.core.ActivityCode.DELIVERY_TYPE_LOCAL_DELIVERY], false)
        String otherTote = pickSite('PT-PKW2').tote

        expect:
        pickPatch(o.taskIds[0], [action: 'pick', outboundContainerId: 'no-such-tote']).message.contains('does not exist')
        // no container, or a tote of another facility: there is no such tote at the task's facility
        pickPatch(o.taskIds[0], [action: 'pick']).message.contains('does not exist')
        pickPatch(o.taskIds[0], [action: 'pick', outboundContainerId: otherTote]).message.contains('does not exist')
        pickPatch(o.taskIds[0], [action: 'pick', outboundContainerId: s.alt]).message.contains('OUTBOUND_CONTAINER')
        pickPatch(o.taskIds[0], [action: 'pick', outboundContainerId: noHold]).message.contains('HOLD_STOCK')
        pickPatch(o.taskIds[0], [action: 'pick', outboundContainerId: wrongLane]).message.contains('DELIVERY_TYPE_LOCAL_DELIVERY')
        pickTask(o.taskIds[0]).status == 'PENDING'
        onHand(s.dc, s.product) == [BIN: 20, ALT: 20]
    }

    void 'pick at a facility without TRACK_INTERNAL_TRANSACTIONS records the pick but moves no stock'() {
        given:
        PickSite s = pickSite('PT-PKN', DeliveryTypeCode.LOCAL_DELIVERY, false)
        PickOrder o = pickOrder(s, [4])

        when:
        Throwable e = pickPatch(o.taskIds[0], [action: 'pick', outboundContainerId: s.tote, pickedById: person()])

        then:
        e == null
        pickTask(o.taskIds[0]).subMap(['status', 'quantityPicked', 'outboundContainer']) ==
                [status: 'PICKED', quantityPicked: 4, outboundContainer: 'TOTE']
        onHand(s.dc, s.product) == [BIN: 20, ALT: 20]
        status(o.requisitionId) == RequisitionStatus.PICKED
    }

    void 'pick: a pick that cannot be taken from the bin (not enough on hand) is refused and nothing changes'() {
        given:
        PickSite s = pickSite('PT-PKS', DeliveryTypeCode.LOCAL_DELIVERY, true, 2)
        PickOrder o = pickOrder(s, [5])

        when:
        Throwable e = pickPatch(o.taskIds[0], [action: 'pick', outboundContainerId: s.tote, pickedById: person()])

        then:
        e != null
        pickTask(o.taskIds[0]).status == 'PENDING'
        onHand(s.dc, s.product) == [BIN: 2, ALT: 2]
    }

    @PendingFeature(reason = 'PickTaskService.groovy:244-257 pick() always transfers quantityRequired and PickTaskStatus allows PICKED -> PICKED, so repeating a pick (a mobile retry) moves the same units into the tote again')
    void 'pick is idempotent: a second pick of a PICKED task moves no more stock'() {
        given:
        PickSite s = pickSite('PT-PK2')
        PickOrder o = pickOrder(s, [5])
        String picker = person()
        pickPatch(o.taskIds[0], [action: 'pick', outboundContainerId: s.tote, pickedById: picker])
        assert onHand(s.dc, s.product) == [BIN: 15, ALT: 20, TOTE: 5]

        when:
        pickPatch(o.taskIds[0], [action: 'pick', outboundContainerId: s.tote, pickedById: picker])

        then:
        onHand(s.dc, s.product) == [BIN: 15, ALT: 20, TOTE: 5]
        pickTask(o.taskIds[0]).quantityPicked == 5
    }

    @PendingFeature(reason = 'PickTaskService.groovy:248,255 pick() after a partial short pick transfers the full quantityRequired again and overwrites quantityPicked, so the tote ends up with more than the task requires')
    void 'pick after a partial short pick moves only what is still to pick'() {
        given:
        PickSite s = pickSite('PT-PK3')
        PickOrder o = pickOrder(s, [5])
        String picker = person()
        pickPatch(o.taskIds[0], [action: 'start', assigneeId: picker])
        pickPatch(o.taskIds[0], [action: 'short-pick', outboundContainerId: s.tote, quantityPicked: 2, pickedById: picker])
        assert onHand(s.dc, s.product) == [BIN: 18, ALT: 20, TOTE: 2]

        when:
        pickPatch(o.taskIds[0], [action: 'pick', outboundContainerId: s.tote, pickedById: picker])

        then:
        onHand(s.dc, s.product) == [BIN: 15, ALT: 20, TOTE: 5]
        pickTask(o.taskIds[0]).quantityPicked == 5
    }

    // ------------------------------------------------------------------ short pick

    void 'short pick without a reason: partial quantity moved, the task stays PICKING; the rest completes it'() {
        given:
        PickSite s = pickSite('PT-SP')
        PickOrder o = pickOrder(s, [5])
        String picker = person()
        pickPatch(o.taskIds[0], [action: 'start', assigneeId: picker])

        when:
        Throwable e1 = pickPatch(o.taskIds[0], [action: 'short-pick', outboundContainerId: s.tote, quantityPicked: 2, pickedById: picker])

        then:
        e1 == null
        pickTask(o.taskIds[0]).subMap(['status', 'quantityPicked', 'reasonCode']) == [status: 'PICKING', quantityPicked: 2, reasonCode: null]
        onHand(s.dc, s.product) == [BIN: 18, ALT: 20, TOTE: 2]

        when:
        Throwable e2 = pickPatch(o.taskIds[0], [action: 'short-pick', outboundContainerId: s.tote, quantityPicked: 3, pickedById: picker])

        then:
        e2 == null
        pickTask(o.taskIds[0]).subMap(['status', 'quantityPicked']) == [status: 'PICKED', quantityPicked: 5]
        onHand(s.dc, s.product) == [BIN: 15, ALT: 20, TOTE: 5]
        status(o.requisitionId) == RequisitionStatus.PICKED
    }

    void 'short pick with a reason code: what was picked is moved, the task is PICKED with the shortage recorded'() {
        given:
        PickSite s = pickSite('PT-SPR')
        PickOrder o = pickOrder(s, [5])
        String picker = person()
        pickPatch(o.taskIds[0], [action: 'start', assigneeId: picker])

        when:
        Throwable e = pickPatch(o.taskIds[0], [action: 'short-pick', outboundContainerId: s.tote, quantityPicked: 3,
                                               pickedById: picker, reasonCode: 'STOCKOUT'])

        then:
        e == null
        pickTask(o.taskIds[0]).subMap(['status', 'quantityPicked', 'reasonCode']) == [status: 'PICKED', quantityPicked: 3, reasonCode: 'STOCKOUT']
        tx { PicklistItem.get(o.taskIds[0]).quantityCanceled } == 2
        onHand(s.dc, s.product) == [BIN: 17, ALT: 20, TOTE: 3]
        status(o.requisitionId) == RequisitionStatus.PICKED
    }

    void 'short pick: more than the task still needs is refused and nothing moves'() {
        given:
        PickSite s = pickSite('PT-SPX')
        PickOrder o = pickOrder(s, [5])
        String picker = person()
        pickPatch(o.taskIds[0], [action: 'start', assigneeId: picker])

        when:
        Throwable e = pickPatch(o.taskIds[0], [action: 'short-pick', outboundContainerId: s.tote, quantityPicked: 6, pickedById: picker])

        then:
        e instanceof ValidationException
        pickTask(o.taskIds[0]).subMap(['status', 'quantityPicked']) == [status: 'PICKING', quantityPicked: 0]
        onHand(s.dc, s.product) == [BIN: 20, ALT: 20]
    }

    void 'short pick of zero with a reason on an untouched task: STAGED (terminal, nothing to stage), and every line short cancels the order'() {
        given:
        PickSite s = pickSite('PT-SP0')
        PickOrder o = pickOrder(s, [5])
        String picker = person()
        pickPatch(o.taskIds[0], [action: 'start', assigneeId: picker])

        when:
        Throwable e = pickPatch(o.taskIds[0], [action: 'short-pick', quantityPicked: 0, pickedById: picker, reasonCode: 'STOCKOUT'])

        then:
        e == null
        tx { PicklistItem.get(o.taskIds[0]).status } == 'STAGED'
        status(o.requisitionId) == RequisitionStatus.CANCELED
        onHand(s.dc, s.product) == [BIN: 20, ALT: 20]
    }

    void 'short pick of zero with a reason on a task nobody started is refused (PENDING -> STAGED is not a transition)'() {
        given:
        PickSite s = pickSite('PT-SP0N')
        PickOrder o = pickOrder(s, [5])

        when:
        Throwable e = pickPatch(o.taskIds[0], [action: 'short-pick', quantityPicked: 0, pickedById: person(), reasonCode: 'STOCKOUT'])

        then:
        e instanceof IllegalStateException
        e.message == 'Invalid transition PENDING -> STAGED'
        pickTask(o.taskIds[0]).status == 'PENDING'
        status(o.requisitionId) == RequisitionStatus.PICKING
    }

    void 'short pick of zero with a reason after a partial pick: PICKED, the order goes on'() {
        given:
        PickSite s = pickSite('PT-SP02')
        PickOrder o = pickOrder(s, [5])
        String picker = person()
        pickPatch(o.taskIds[0], [action: 'start', assigneeId: picker])
        pickPatch(o.taskIds[0], [action: 'short-pick', outboundContainerId: s.tote, quantityPicked: 2, pickedById: picker])

        when:
        Throwable e = pickPatch(o.taskIds[0], [action: 'short-pick', quantityPicked: 0, pickedById: picker, reasonCode: 'STOCKOUT'])

        then:
        e == null
        pickTask(o.taskIds[0]).subMap(['status', 'quantityPicked', 'reasonCode']) == [status: 'PICKED', quantityPicked: 2, reasonCode: 'STOCKOUT']
        status(o.requisitionId) == RequisitionStatus.PICKED
    }

    void 'short pick of zero with no reason changes nothing'() {
        given:
        PickSite s = pickSite('PT-SP00')
        PickOrder o = pickOrder(s, [5])
        String picker = person()
        pickPatch(o.taskIds[0], [action: 'start', assigneeId: picker])

        when:
        Throwable e = pickPatch(o.taskIds[0], [action: 'short-pick', quantityPicked: 0, pickedById: picker])

        then:
        e == null
        pickTask(o.taskIds[0]).subMap(['status', 'quantityPicked']) == [status: 'PICKING', quantityPicked: 0]
    }

    // ------------------------------------------------------------------ reallocate

    void 'reallocate: the task is replaced by new pending tasks in the chosen bins'() {
        given:
        PickSite s = pickSite('PT-RA')
        PickOrder o = pickOrder(s, [5])

        when:
        List<String> newIds = tx {
            pickTaskService.reallocate(PickTask.get(o.taskIds[0]), [
                    [inventoryItem: [id: s.lotId], binLocation: [id: s.alt], quantity: 3],
                    [inventoryItem: [id: s.lotId], binLocation: [id: s.bin], quantity: 2],
            ])*.id
        } as List<String>

        then:
        tx { PicklistItem.get(o.taskIds[0]) } == null
        newIds.size() == 2
        newIds.collect { pickTask(it) }.collect { [it.location, it.quantityRequired, it.status] }.sort { it[0] } ==
                [['ALT', 3, 'PENDING'], ['BIN', 2, 'PENDING']]
    }

    void 'reallocate needs picklist items'() {
        given:
        PickSite s = pickSite('PT-RA0')
        PickOrder o = pickOrder(s, [5])

        when:
        tx { pickTaskService.reallocate(PickTask.get(o.taskIds[0]), []) }

        then:
        thrown(IllegalArgumentException)
        pickTask(o.taskIds[0]) != null
    }

    void 'reallocate does not check the new quantities against the line (characterised)'() {
        given:
        PickSite s = pickSite('PT-RAQ')
        PickOrder o = pickOrder(s, [5])

        when:
        List<Integer> quantities = tx {
            pickTaskService.reallocate(PickTask.get(o.taskIds[0]), [
                    [inventoryItem: [id: s.lotId], binLocation: [id: s.alt], quantity: 9],
            ])*.quantityRequired
        } as List<Integer>

        then:
        quantities == [9]
    }

    @PendingFeature(reason = 'PickTaskService.groovy:299-321 reallocate() deletes the picklist item whatever its status: a PICKED task (its stock already moved to the tote) is deleted and replaced by new pending picks, so the order is picked twice')
    void 'reallocate refuses a task that has already been picked'() {
        given:
        PickSite s = pickSite('PT-RAP')
        PickOrder o = pickOrder(s, [5])
        pickPatch(o.taskIds[0], [action: 'pick', outboundContainerId: s.tote, pickedById: person()])

        when:
        Throwable e = null
        try {
            tx {
                pickTaskService.reallocate(PickTask.get(o.taskIds[0]),
                        [[inventoryItem: [id: s.lotId], binLocation: [id: s.alt], quantity: 5]])
            }
        } catch (Throwable t) {
            e = t
        }

        then:
        e != null
        pickTask(o.taskIds[0]).status == 'PICKED'
    }

    // ------------------------------------------------------------------ drop to staging

    void 'drop: PICKED tasks in the tote move to the staging lane, STAGED; the order is STAGED once every task is'() {
        given:
        PickSite s = pickSite('PT-DR')
        PickOrder o = pickOrder(s, [3, 4])
        String picker = person()
        o.taskIds.each { pickPatch(it, [action: 'pick', outboundContainerId: s.tote, pickedById: picker]) }
        assert status(o.requisitionId) == RequisitionStatus.PICKED

        when:
        tx { pickTaskService.drop(s.tote, [action: 'drop', stagingLocationId: s.staging, stagedById: picker]) }

        then: 'the signed-in user is recorded as the user who staged them, whatever stagedById says'
        o.taskIds.collect { pickTask(it).subMap(['status', 'stagedBy']) } ==
                [[status: 'STAGED', stagedBy: adminId()], [status: 'STAGED', stagedBy: adminId()]]
        tx { PicklistItem.get(o.taskIds[0]).stagingLocation?.id } == s.staging
        onHand(s.dc, s.product) == [BIN: 13, ALT: 20, STAGE: 7]
        status(o.requisitionId) == RequisitionStatus.STAGED
    }

    void 'drop: an order with no shipment gets one when it is staged'() {
        given:
        PickSite s = pickSite('PT-DRS')
        PickOrder o = pickOrder(s, [3], [:], false)
        String picker = person()
        pickPatch(o.taskIds[0], [action: 'pick', outboundContainerId: s.tote, pickedById: picker])

        when:
        tx { pickTaskService.drop(s.tote, [action: 'drop', stagingLocationId: s.staging, stagedById: picker]) }

        then:
        status(o.requisitionId) == RequisitionStatus.STAGED
        tx { Shipment.findAllByRequisition(Requisition.get(o.requisitionId)).collect { it.shipmentItems*.quantity.sum() } } == [3]
    }

    void 'drop: an empty tote, a location that is not a staging lane, or an unknown action is refused'() {
        given:
        PickSite s = pickSite('PT-DRX')
        PickOrder o = pickOrder(s, [3])

        expect: 'nothing has been picked into the tote yet'
        dropError(s.tote, [action: 'drop', stagingLocationId: s.staging]).message.contains('is empty')

        when:
        pickPatch(o.taskIds[0], [action: 'pick', outboundContainerId: s.tote, pickedById: person()])

        then:
        dropError(s.tote, [action: 'drop', stagingLocationId: s.alt]).message.contains('STAGING_LOCATION')
        dropError(s.tote, [action: 'drop', stagingLocationId: 'no-such-lane']).message.contains('does not exist')
        dropError(s.tote, [action: 'stack']) instanceof UnsupportedOperationException
        dropError('no-such-tote', [action: 'drop']) instanceof IllegalArgumentException
        pickTask(o.taskIds[0]).status == 'PICKED'
        onHand(s.dc, s.product) == [BIN: 17, ALT: 20, TOTE: 3]
    }

    @PendingFeature(reason = 'PickTaskService.groovy:429-433 drop finds the tasks to stage from the STOCK in the tote; without TRACK_INTERNAL_TRANSACTIONS a pick never moves stock into the tote (transferToContainer skips), so a PICKED task can never be dropped ("Outbound container ... is empty") and the order never reaches STAGED')
    void 'drop at a facility without TRACK_INTERNAL_TRANSACTIONS stages the picked tasks'() {
        given:
        PickSite s = pickSite('PT-DRN', DeliveryTypeCode.LOCAL_DELIVERY, false)
        PickOrder o = pickOrder(s, [3])
        String picker = person()
        pickPatch(o.taskIds[0], [action: 'pick', outboundContainerId: s.tote, pickedById: picker])

        when:
        Throwable e = dropError(s.tote, [action: 'drop', stagingLocationId: s.staging, stagedById: picker])

        then:
        e == null
        pickTask(o.taskIds[0]).status == 'STAGED'
        status(o.requisitionId) == RequisitionStatus.STAGED
    }

    Throwable dropError(String tote, Map data) {
        try {
            tx { pickTaskService.drop(tote, data) }
            return null
        } catch (Throwable t) {
            return t
        }
    }

    // ------------------------------------------------------------------ rollback

    void 'rollbackPickTasks: the picks are undone (stock back in the bin), the picklist cleared, the order back to VERIFYING'() {
        given:
        PickSite s = pickSite('PT-RB')
        PickOrder o = pickOrder(s, [3, 4])
        String picker = person()
        pickPatch(o.taskIds[0], [action: 'pick', outboundContainerId: s.tote, pickedById: picker])
        assert onHand(s.dc, s.product) == [BIN: 17, ALT: 20, TOTE: 3]

        when:
        boolean done = tx { pickTaskService.rollbackPickTasks(Requisition.get(o.requisitionId)) }

        then:
        done
        status(o.requisitionId) == RequisitionStatus.VERIFYING
        tx { PicklistItem.findAllByIdInList(o.taskIds) }.isEmpty()
        onHand(s.dc, s.product) == [BIN: 20, ALT: 20]
    }

    void 'rollbackPickTasks refuses an issued order'() {
        given:
        PickSite s = pickSite('PT-RBI')
        PickOrder o = pickOrder(s, [3])
        tx { Requisition r = Requisition.get(o.requisitionId); r.status = RequisitionStatus.ISSUED; r.save(flush: true, failOnError: true) }

        when:
        tx { pickTaskService.rollbackPickTasks(Requisition.get(o.requisitionId)) }

        then:
        thrown(IllegalStateException)
        tx { PicklistItem.get(o.taskIds[0]) } != null
    }

    // ------------------------------------------------------------------ search and counts

    SearchPickTaskCommand cmd(String dc, Map props = [:]) {
        return new SearchPickTaskCommand([facility: Location.get(dc)] + props)
    }

    List<String> search(String dc, Map props = [:], Map params = [:]) {
        return tx { pickTaskService.search(cmd(dc, props), new grails.web.servlet.mvc.GrailsParameterMap(params, null))*.id } as List<String>
    }

    void 'search: by default only PENDING and PICKING tasks of the facility, by priority then age'() {
        given:
        PickSite s = pickSite('PT-SE')
        PickOrder local = pickOrder(s, [2])
        PickOrder done = pickOrder(s, [2])
        pickPatch(done.taskIds[0], [action: 'pick', outboundContainerId: s.tote, pickedById: person()])
        PickOrder started = pickOrder(s, [2])
        pickPatch(started.taskIds[0], [action: 'start', assigneeId: person()])

        expect:
        search(s.dc) == [local.taskIds[0], started.taskIds[0]]
        search(s.dc, [status: [PickTaskStatus.PICKED]]) == [done.taskIds[0]]
        search(s.dc, [requisitionId: done.requestNumber]) == [done.taskIds[0]]
        search(s.dc, [requisitionId: done.requisitionId]) == [done.taskIds[0]]
        search(s.dc, [outboundContainerId: tx { Location.get(s.tote).locationNumber }]) == [done.taskIds[0]]
    }

    void 'search: ordersCount batches whole orders, skipping orders someone has started; assigneeId and excludeAssignedRequisitions'() {
        given:
        PickSite s = pickSite('PT-SB')
        PickOrder a = pickOrder(s, [1, 1])
        PickOrder b = pickOrder(s, [1])
        PickOrder c = pickOrder(s, [1])
        String picker = person()
        pickPatch(b.taskIds[0], [action: 'start', assigneeId: picker])

        expect:
        search(s.dc, [ordersCount: 1]) == a.taskIds
        search(s.dc, [ordersCount: 2]) as Set == (a.taskIds + c.taskIds) as Set
        search(s.dc, [assigneeId: picker]) == b.taskIds
        search(s.dc, [excludeAssignedRequisitions: true]) as Set == (a.taskIds + c.taskIds) as Set
    }

    void 'search: a PICKED-only search flags tasks whose picked stock is no longer in the tote'() {
        given:
        PickSite s = pickSite('PT-SI')
        PickOrder o = pickOrder(s, [3])
        pickPatch(o.taskIds[0], [action: 'pick', outboundContainerId: s.tote, pickedById: person()])

        when:
        Map before = tx { pickTaskService.search(cmd(s.dc, [status: [PickTaskStatus.PICKED]]), new grails.web.servlet.mvc.GrailsParameterMap([:], null)).collectEntries { [it.id, it.hasStockIssue] } } as Map
        tx {
            // the tote is emptied through a side channel (a stock transfer back to the bin)
            org.pih.warehouse.inventory.TransferStockCommand t = new org.pih.warehouse.inventory.TransferStockCommand(
                    location: Location.get(s.dc), binLocation: Location.get(s.tote), inventoryItem: InventoryItem.get(s.lotId),
                    quantity: 3, otherLocation: Location.get(s.dc), otherBinLocation: Location.get(s.bin), transferOut: true)
            inventoryService.transferStock(t)
        }
        onHand(s.dc, s.product)
        Map after = tx { pickTaskService.search(cmd(s.dc, [status: [PickTaskStatus.PICKED]]), new grails.web.servlet.mvc.GrailsParameterMap([:], null)).collectEntries { [it.id, it.hasStockIssue] } } as Map
        Map other = tx { pickTaskService.search(cmd(s.dc, [status: [PickTaskStatus.PICKED, PickTaskStatus.PENDING]]), new grails.web.servlet.mvc.GrailsParameterMap([:], null)).collectEntries { [it.id, it.hasStockIssue] } } as Map

        then:
        before == [(o.taskIds[0]): false]
        after == [(o.taskIds[0]): true]
        other == [(o.taskIds[0]): null]
    }

    void 'counts: distinct orders per delivery type with open tasks, every delivery type listed'() {
        given:
        PickSite s = pickSite('PT-CN')
        pickOrder(s, [1, 1])
        pickOrder(s, [1])
        PickOrder picked = pickOrder(s, [1])
        pickPatch(picked.taskIds[0], [action: 'pick', outboundContainerId: s.tote, pickedById: person()])

        when:
        List<Map> counts = tx { pickTaskService.countOrdersByDeliveryType(cmd(s.dc)) } as List<Map>

        then:
        counts*.deliveryTypeCode == DeliveryTypeCode.values()*.name()
        counts.find { it.deliveryTypeCode == 'LOCAL_DELIVERY' }.availableCount == 2
        counts.findAll { it.deliveryTypeCode != 'LOCAL_DELIVERY' }*.availableCount.every { it == 0 }
    }
}
