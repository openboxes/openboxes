package org.pih.warehouse.api.regression.obaf.putaway

import grails.validation.ValidationException
import grails.web.servlet.mvc.GrailsParameterMap
import spock.lang.PendingFeature

import org.pih.warehouse.api.PutawayTaskStatus
import org.pih.warehouse.api.StatusCategory
import org.pih.warehouse.api.regression.obaf.support.ObafTaskIntegrationSpec
import org.pih.warehouse.api.regression.support.RegressionRoleFixtures
import org.pih.warehouse.api.putaway.SearchPutawayTaskCommand
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.ReasonCode
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.putaway.PutawayTask

/**
 * PutawayTaskService behaviour on a real database (PutawayTask is the putaway_task VIEW over the
 * order items of PUTAWAY_ORDER orders; every change is written to the OrderItem / Order): assign,
 * start, load into a cart (whole and split), complete (direct and from the cart, with cancel
 * remaining), partial complete, rollback, rerun of the putaway strategy, alternate destinations,
 * search / count and the cycle-time metric.
 */
class PutawayTaskIntegrationSpec extends ObafTaskIntegrationSpec {

    // ------------------------------------------------------------------ assign / start

    void 'assign: a PENDING task gets its putaway cart and assignee; re-assigning another cart needs override'() {
        given:
        PutawaySite s = putawaySite('PA-AS')
        String cart2 = bin(s.dc, 'CART2', [ActivityCode.PUTAWAY_CART, ActivityCode.HOLD_STOCK], false)
        String picker = person('Putaway')
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[10, s.recv, s.stor1]])
        String task = po.itemIds[0]

        when:
        Map first = putawayPatch(task, [action: 'assign', container: s.cart, assignee: picker])

        then:
        first.error == null
        putawayTask(task).subMap(['status', 'container', 'assignee']) == [status: 'PENDING', container: 'CART', assignee: picker]

        expect: 'another cart without override is refused, with override it is taken'
        putawayPatch(task, [action: 'assign', container: cart2]).error.message.contains('already assigned')
        putawayTask(task).container == 'CART'
        putawayPatch(task, [action: 'assign', container: cart2, override: true]).error == null
        putawayTask(task).container == 'CART2'
    }

    void 'assign: a cart outside the facility, or a task that is no longer PENDING, is refused'() {
        given:
        PutawaySite s = putawaySite('PA-AS2')
        PutawaySite other = putawaySite('PA-AS3')
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[10, s.recv, s.stor1]])
        String task = po.itemIds[0]

        expect:
        putawayPatch(task, [action: 'assign', container: other.cart]).error instanceof IllegalArgumentException

        when:
        putawayPatch(task, [action: 'start'])

        then:
        putawayPatch(task, [action: 'assign', container: s.cart]).error instanceof IllegalStateException
    }

    void 'assign with only an assignee sets the assignee and keeps the task'() {
        given:
        PutawaySite s = putawaySite('PA-ASN')
        String picker = person('Putaway')
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[10, s.recv, s.stor1]])

        when:
        Map r = putawayPatch(po.itemIds[0], [action: 'assign', assignee: picker])

        then:
        r.error == null
        putawayTask(po.itemIds[0]).assignee == picker
    }

    void 'start: STARTED, the current user is the assignee, the order is APPROVED'() {
        given:
        PutawaySite s = putawaySite('PA-ST')
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[10, s.recv, s.stor1]])

        when:
        Map r = putawayPatch(po.itemIds[0], [action: 'start'])

        then:
        r.error == null
        putawayTask(po.itemIds[0]).subMap(['status', 'assignee', 'orderStatus']) == [status: 'STARTED', assignee: adminId(), orderStatus: 'APPROVED']
        putawayTask(po.itemIds[0]).dateStarted != null
    }

    void 'an unknown action is refused; noop and cancel change nothing'() {
        given:
        PutawaySite s = putawaySite('PA-UA')
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[10, s.recv, s.stor1]])

        expect:
        putawayPatch(po.itemIds[0], [action: 'teleport']).error instanceof UnsupportedOperationException
        putawayPatch(po.itemIds[0], [action: 'noop']).error == null
        putawayPatch(po.itemIds[0], [action: 'cancel']).error == null
        putawayTask(po.itemIds[0]).status == 'PENDING'
        rootCause(putawayPatch('no-such-task', [action: 'noop']).error) instanceof org.hibernate.ObjectNotFoundException
    }

    // ------------------------------------------------------------------ load

    void 'load all of it: IN_PROGRESS, the stock moves from the receiving bin into the cart'() {
        given:
        PutawaySite s = putawaySite('PA-LD', 10, [ActivityCode.TRACK_INTERNAL_TRANSACTIONS])
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[10, s.recv, s.stor1]])
        putawayPatch(po.itemIds[0], [action: 'assign', container: s.cart])

        when:
        Map r = putawayPatch(po.itemIds[0], [action: 'load', quantity: 10, container: s.cart])

        then:
        r.error == null
        r.result == po.itemIds[0]
        putawayTask(po.itemIds[0]).subMap(['status', 'container', 'quantity', 'orderStatus']) ==
                [status: 'IN_PROGRESS', container: 'CART', quantity: 10, orderStatus: 'PLACED']
        onHand(s.dc, s.product) == [CART: 10]
    }

    void 'load part of it: the loaded part becomes its own IN_PROGRESS task in the cart, the rest a PENDING task left at the receiving bin'() {
        given:
        PutawaySite s = putawaySite('PA-LDP', 10, [ActivityCode.TRACK_INTERNAL_TRANSACTIONS])
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[10, s.recv, s.stor1]])
        putawayPatch(po.itemIds[0], [action: 'assign', container: s.cart])

        when:
        Map r = putawayPatch(po.itemIds[0], [action: 'load', quantity: 4, container: s.cart])

        then:
        r.error == null
        r.result != po.itemIds[0]
        putawayTasksOf(po.orderId).collect { it.subMap(['quantity', 'status', 'location', 'container', 'destination']) } == [
                [quantity: 4, status: 'IN_PROGRESS', location: 'RECV', container: 'CART', destination: 'STOR1'],
                [quantity: 6, status: 'PENDING', location: 'RECV', container: null, destination: 'STOR1'],
                [quantity: 10, status: 'CANCELED', location: 'RECV', container: 'CART', destination: 'STOR1'],
        ]
        putawayTask(r.result).quantity == 4
        onHand(s.dc, s.product) == [RECV: 6, CART: 4]
    }

    void 'load: too much, nothing, a location that is not a cart, or another cart without override is refused'() {
        given:
        PutawaySite s = putawaySite('PA-LDX', 10, [ActivityCode.TRACK_INTERNAL_TRANSACTIONS])
        String cart2 = bin(s.dc, 'CART2', [ActivityCode.PUTAWAY_CART, ActivityCode.HOLD_STOCK], false)
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[10, s.recv, s.stor1]])
        putawayPatch(po.itemIds[0], [action: 'assign', container: s.cart])

        expect:
        putawayPatch(po.itemIds[0], [action: 'load', quantity: 11, container: s.cart]).error instanceof IllegalArgumentException
        putawayPatch(po.itemIds[0], [action: 'load', quantity: 0, container: s.cart]).error instanceof IllegalArgumentException
        putawayPatch(po.itemIds[0], [action: 'load', quantity: 5, container: 'no-such-cart']).error instanceof IllegalStateException
        putawayPatch(po.itemIds[0], [action: 'load', quantity: 5, container: s.stor2]).error.message.contains('PUTAWAY_CART')
        putawayPatch(po.itemIds[0], [action: 'load', quantity: 5, container: cart2]).error.message.contains('does not match')
        putawayTask(po.itemIds[0]).status == 'PENDING'
        onHand(s.dc, s.product) == [RECV: 10]

        when: 'override takes the other cart'
        Map r = putawayPatch(po.itemIds[0], [action: 'load', quantity: 10, container: cart2, override: true])

        then:
        r.error == null
        putawayTask(po.itemIds[0]).container == 'CART2'
        onHand(s.dc, s.product) == [CART2: 10]
    }

    void 'load at a facility without TRACK_INTERNAL_TRANSACTIONS records the cart but moves no stock, and complete takes it from the receiving bin'() {
        given:
        PutawaySite s = putawaySite('PA-LDN')
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[10, s.recv, s.stor1]])
        putawayPatch(po.itemIds[0], [action: 'assign', container: s.cart])

        when:
        Map r = putawayPatch(po.itemIds[0], [action: 'load', quantity: 10, container: s.cart])

        then:
        r.error == null
        putawayTask(po.itemIds[0]).subMap(['status', 'container']) == [status: 'IN_PROGRESS', container: 'CART']
        onHand(s.dc, s.product) == [RECV: 10]

        when:
        Map c = putawayPatch(po.itemIds[0], [action: 'complete', destination: s.stor2])

        then:
        c.error == null
        putawayTask(po.itemIds[0]).status == 'COMPLETED'
        onHand(s.dc, s.product) == [STOR2: 10]
    }

    @PendingFeature(reason = 'PutawayTaskAdapter.groovy:91-110 the task PATCH load / partialComplete return is built by toPutawayTask(OrderItem), which maps identifier = order number, facility = order.origin, assignee = order.orderedBy, dateStarted = order.dateOrdered, unlike the putaway_task view (PT-crc32 identifier, approved_by, date_approved) every GET answers: the same task reads differently in the PATCH response')
    void 'load answers the same task the putaway_task view reads'() {
        given:
        PutawaySite s = putawaySite('PA-LDV')
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[10, s.recv, s.stor1]])
        putawayPatch(po.itemIds[0], [action: 'start'])

        when:
        Map answered = tx {
            PutawayTask t = putawayTaskService.patch(po.itemIds[0], [action: 'load', quantity: 4, container: s.cart, override: true])
            [id: t.id, identifier: t.identifier, assignee: t.assignee?.id, status: t.status?.name(), quantity: t.quantity as Integer]
        } as Map
        Map viewed = putawayTask(answered.id)

        then:
        answered == viewed.subMap(['id', 'identifier', 'assignee', 'status', 'quantity'])
    }

    // ------------------------------------------------------------------ complete

    void 'complete, direct: COMPLETED, the stock moves from the receiving bin to the destination, the order is COMPLETED'() {
        given:
        PutawaySite s = putawaySite('PA-CD')
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[10, s.recv, s.stor1]])

        when:
        Map r = putawayPatch(po.itemIds[0], [action: 'complete', destination: s.stor1, isDirectPutaway: true])

        then:
        r.error == null
        putawayTask(po.itemIds[0]).subMap(['status', 'destination', 'completedBy', 'orderStatus']) ==
                [status: 'COMPLETED', destination: 'STOR1', completedBy: adminId(), orderStatus: 'COMPLETED']
        putawayTask(po.itemIds[0]).dateCompleted != null
        onHand(s.dc, s.product) == [STOR1: 10]
    }

    void 'complete from the cart to another destination than planned (the two-step flow)'() {
        given:
        PutawaySite s = putawaySite('PA-CC', 10, [ActivityCode.TRACK_INTERNAL_TRANSACTIONS])
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[10, s.recv, s.stor1]])
        putawayPatch(po.itemIds[0], [action: 'start'])
        putawayPatch(po.itemIds[0], [action: 'load', quantity: 10, container: s.cart, override: true])

        when:
        Map r = putawayPatch(po.itemIds[0], [action: 'complete', destination: s.stor2])

        then:
        r.error == null
        putawayTask(po.itemIds[0]).subMap(['status', 'destination']) == [status: 'COMPLETED', destination: 'STOR2']
        onHand(s.dc, s.product) == [STOR2: 10]
    }

    void 'complete with cancel remaining sends the quantity to lost and found with the discrepancy reason'() {
        given:
        PutawaySite s = putawaySite('PA-CR')
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[10, s.recv, s.stor1]])

        when:
        Map r = putawayPatch(po.itemIds[0], [action: 'complete', destination: s.stor1, isCancelRemaining: true,
                                              reasonCode: 'PUTAWAY_DISCREPANCY', isDirectPutaway: true])

        then:
        r.error == null
        putawayTask(po.itemIds[0]).subMap(['status', 'destination', 'discrepancyReasonCode', 'putawayTypeCode']) ==
                [status: 'COMPLETED', destination: 'LNF', discrepancyReasonCode: 'PUTAWAY_DISCREPANCY', putawayTypeCode: 'QUARANTINE']
        onHand(s.dc, s.product) == [LNF: 10]
    }

    void 'complete: no destination, cancel remaining without a lost and found bin, or completing twice is refused'() {
        given:
        PutawaySite s = putawaySite('PA-CX', 10, [], false)
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[10, s.recv, s.stor1]])

        expect:
        putawayPatch(po.itemIds[0], [action: 'complete', isDirectPutaway: true]).error instanceof ValidationException
        putawayPatch(po.itemIds[0], [action: 'complete', destination: s.stor1, isCancelRemaining: true, isDirectPutaway: true])
                .error.message.contains('No discrepancy location')
        putawayTask(po.itemIds[0]).status == 'PENDING'
        onHand(s.dc, s.product) == [RECV: 10]

        when:
        putawayPatch(po.itemIds[0], [action: 'complete', destination: s.stor1, isDirectPutaway: true])
        Map again = putawayPatch(po.itemIds[0], [action: 'complete', destination: s.stor2, isDirectPutaway: true])

        then:
        again.error instanceof IllegalStateException
        onHand(s.dc, s.product) == [STOR1: 10]
    }

    @PendingFeature(reason = 'PutawayTaskService.groovy:233-248 save() marks the putaway ORDER COMPLETED when "every split task" is completed, but an order whose items were never split has no split tasks (every() of an empty list is true): completing the first of two items completes the whole order while the second is still pending')
    void 'completing one item of a two-item putaway order leaves the order open'() {
        given:
        PutawaySite s = putawaySite('PA-C2')
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[6, s.recv, s.stor1], [4, s.recv, s.stor2]])

        when:
        putawayPatch(po.itemIds[0], [action: 'complete', destination: s.stor1, isDirectPutaway: true])

        then:
        putawayTask(po.itemIds[1]).status == 'PENDING'
        orderStatus(po.orderId) != 'COMPLETED'
    }

    // ------------------------------------------------------------------ partial complete

    void 'partial complete: the completed part goes to the destination, the rest becomes a PENDING task routed there too'() {
        given:
        PutawaySite s = putawaySite('PA-PC')
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[10, s.recv, s.stor1]])

        when:
        Map r = putawayPatch(po.itemIds[0], [action: 'partialComplete', quantity: 4, destination: s.stor2,
                                              reasonCode: 'PUTAWAY_DISCREPANCY', isDirectPutaway: true])

        then:
        r.error == null
        putawayTasksOf(po.orderId).collect { it.subMap(['quantity', 'status', 'destination']) } == [
                [quantity: 4, status: 'COMPLETED', destination: 'STOR2'],
                [quantity: 6, status: 'PENDING', destination: 'STOR2'],
                [quantity: 10, status: 'CANCELED', destination: 'STOR1'],
        ]
        putawayTask(r.result).quantity == 6
        onHand(s.dc, s.product) == [RECV: 6, STOR2: 4]
        orderStatus(po.orderId) != 'COMPLETED'
    }

    void 'partial complete without a destination sends the part to lost and found'() {
        given:
        PutawaySite s = putawaySite('PA-PCL')
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[10, s.recv, s.stor1]])

        when:
        Map r = putawayPatch(po.itemIds[0], [action: 'partialComplete', quantity: 3, isDirectPutaway: true])

        then:
        r.error == null
        onHand(s.dc, s.product) == [RECV: 7, LNF: 3]
    }

    void 'partial complete of the whole quantity or more is refused'() {
        given:
        PutawaySite s = putawaySite('PA-PCX')
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[10, s.recv, s.stor1]])

        expect:
        putawayPatch(po.itemIds[0], [action: 'partialComplete', quantity: q, destination: s.stor2, isDirectPutaway: true]).error instanceof IllegalArgumentException
        putawayTask(po.itemIds[0]).status == 'PENDING'
        onHand(s.dc, s.product) == [RECV: 10]

        where:
        q << [10, 11]
    }

    // ------------------------------------------------------------------ rollback

    void 'rollback of a completed task: the transfer is undone, the task and order are PENDING again'() {
        given:
        PutawaySite s = putawaySite('PA-RB')
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[10, s.recv, s.stor1]])
        putawayPatch(po.itemIds[0], [action: 'complete', destination: s.stor1, isDirectPutaway: true])
        assert onHand(s.dc, s.product) == [STOR1: 10]

        when:
        Map r = putawayPatch(po.itemIds[0], [action: 'rollback'])

        then:
        r.error == null
        putawayTask(po.itemIds[0]).subMap(['status', 'orderStatus', 'orderItemStatus']) == [status: 'PENDING', orderStatus: 'PENDING', orderItemStatus: 'PENDING']
        onHand(s.dc, s.product) == [RECV: 10]

        and: 'it can be put away again'
        putawayPatch(po.itemIds[0], [action: 'complete', destination: s.stor2, isDirectPutaway: true]).error == null
        onHand(s.dc, s.product) == [STOR2: 10]
    }

    @PendingFeature(reason = 'PutawayTaskService.groovy:620-629 executeRollback clears completedBy / dateCompleted on the view object only; save() (lines 222-255) never writes them back to the order, so a rolled-back PENDING task still reports who completed it and when')
    void 'rollback clears who completed the task and when'() {
        given:
        PutawaySite s = putawaySite('PA-RBC')
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[10, s.recv, s.stor1]])
        putawayPatch(po.itemIds[0], [action: 'complete', destination: s.stor1, isDirectPutaway: true])

        when:
        putawayPatch(po.itemIds[0], [action: 'rollback'])

        then:
        putawayTask(po.itemIds[0]).subMap(['status', 'completedBy', 'dateCompleted']) == [status: 'PENDING', completedBy: null, dateCompleted: null]
    }

    @PendingFeature(reason = 'PutawayTaskService.groovy:596-614 rollback() deletes transfers only when the ORDER is COMPLETED; a completed task of an order that is not (the completed part of a partial complete) goes back to PENDING while its stock stays at the destination, so the task can be put away again')
    void 'rollback of the completed part of a partial complete returns its stock'() {
        given:
        PutawaySite s = putawaySite('PA-RBP')
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[10, s.recv, s.stor1]])
        putawayPatch(po.itemIds[0], [action: 'partialComplete', quantity: 4, destination: s.stor2, isDirectPutaway: true])
        String completedPart = putawayTasksOf(po.orderId).find { it.status == 'COMPLETED' }.id

        when:
        Map r = putawayPatch(completedPart, [action: 'rollback'])

        then:
        r.error == null
        putawayTask(completedPart).status == 'PENDING'
        onHand(s.dc, s.product) == [RECV: 10]
    }

    // ------------------------------------------------------------------ rerun strategy

    void 'rerunStrategy re-routes a PENDING task to the product preferred bin'() {
        given:
        PutawaySite s = putawaySite('PA-RR')
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[10, s.recv, s.stor1]])
        preferredBin(s.dc, s.product, s.stor2)

        when:
        Map r = putawayPatch(po.itemIds[0], [action: 'rerunStrategy'])

        then:
        r.error == null
        putawayTask(po.itemIds[0]).subMap(['destination', 'comment', 'status']) == [destination: 'STOR2', comment: 'Default Location', status: 'PENDING']
    }

    void 'rerunStrategy on the PENDING rest of a partial putaway from the cart returns its stock to the receiving bin'() {
        given:
        PutawaySite s = putawaySite('PA-RRC', 10, [ActivityCode.TRACK_INTERNAL_TRANSACTIONS])
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[10, s.recv, s.stor1]])
        putawayPatch(po.itemIds[0], [action: 'load', quantity: 10, container: s.cart, override: true])
        Map partial = putawayPatch(po.itemIds[0], [action: 'partialComplete', quantity: 4, destination: s.stor2,
                                                    reasonCode: 'PUTAWAY_DISCREPANCY'])

        expect:
        partial.error == null
        putawayTask(partial.result).subMap(['quantity', 'status', 'container']) == [quantity: 6, status: 'PENDING', container: 'CART']
        onHand(s.dc, s.product) == [CART: 6, STOR2: 4]

        when:
        Map r = putawayPatch(partial.result, [action: 'rerunStrategy'])

        then:
        r.error == null
        putawayTask(partial.result).subMap(['quantity', 'status']) == [quantity: 6, status: 'PENDING']
        onHand(s.dc, s.product) == [RECV: 6, STOR2: 4]

        when: 'the rest can be sorted again'
        Map reload = putawayPatch(partial.result, [action: 'load', quantity: 6, container: s.cart, override: true])

        then:
        reload.error == null
        onHand(s.dc, s.product) == [CART: 6, STOR2: 4]
    }

    void 'rerunStrategy on the PENDING rest of a partial putaway without TRACK_INTERNAL_TRANSACTIONS moves no stock'() {
        given:
        PutawaySite s = putawaySite('PA-RRCN')
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[10, s.recv, s.stor1]])
        putawayPatch(po.itemIds[0], [action: 'load', quantity: 10, container: s.cart, override: true])
        Map partial = putawayPatch(po.itemIds[0], [action: 'partialComplete', quantity: 4, destination: s.stor2,
                                                    reasonCode: 'PUTAWAY_DISCREPANCY'])

        when:
        Map r = putawayPatch(partial.result, [action: 'rerunStrategy'])

        then:
        partial.error == null
        r.error == null
        putawayTask(partial.result).subMap(['quantity', 'status']) == [quantity: 6, status: 'PENDING']
        onHand(s.dc, s.product) == [RECV: 6, STOR2: 4]
    }

    void 'rerunStrategy with nothing to route to clears the destination (UNASSIGNED)'() {
        given:
        PutawaySite s = putawaySite('PA-RRN')
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[10, s.recv, s.stor1]])

        when:
        Map r = putawayPatch(po.itemIds[0], [action: 'rerunStrategy'])

        then:
        r.error == null
        putawayTask(po.itemIds[0]).subMap(['destination', 'putawayTypeCode']) == [destination: null, putawayTypeCode: 'UNASSIGNED']
    }

    void 'rerunStrategy: a completed task is refused; an in-progress one only for a superuser, keeping its cart'() {
        given:
        PutawaySite s = putawaySite('PA-RRX')
        Map done = putawayOrderOf(s.dc, s.product, s.lotId, [[2, s.recv, s.stor1]])
        putawayPatch(done.itemIds[0], [action: 'complete', destination: s.stor1, isDirectPutaway: true])
        Map moving = putawayOrderOf(s.dc, s.product, s.lotId, [[8, s.recv, s.stor1]])
        putawayPatch(moving.itemIds[0], [action: 'load', quantity: 8, container: s.cart, override: true])
        preferredBin(s.dc, s.product, s.stor2)
        RegressionRoleFixtures.ensureUser('test-obaf-rerun', [RoleType.ROLE_MANAGER], [:])

        expect:
        putawayPatch(done.itemIds[0], [action: 'rerunStrategy']).error instanceof IllegalStateException

        when: 'a manager (not superuser)'
        actAs('test-obaf-rerun')
        Map refused = putawayPatch(moving.itemIds[0], [action: 'rerunStrategy'])
        actAs('admin')
        Map allowed = putawayPatch(moving.itemIds[0], [action: 'rerunStrategy'])

        then:
        refused.error.message.contains('superuser')
        allowed.error == null
        putawayTask(moving.itemIds[0]).subMap(['destination', 'container', 'status']) == [destination: 'STOR2', container: 'CART', status: 'IN_PROGRESS']
    }

    // ------------------------------------------------------------------ alternate destinations

    void 'alternate destinations: the preferred bin first, then the bins with a bin-level stock level'() {
        given:
        PutawaySite s = putawaySite('PA-AD')
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[10, s.recv, s.stor1]])
        preferredBin(s.dc, s.product, s.stor2)
        tx {
            new org.pih.warehouse.inventory.InventoryLevel(product: org.pih.warehouse.product.Product.get(s.product),
                    inventory: Location.get(s.dc).inventory, internalLocation: Location.get(s.stor1))
                    .save(flush: true, failOnError: true)
        }

        when:
        List<String> names = tx { putawayTaskService.getAlternateDestinations(PutawayTask.get(po.itemIds[0])).collect { prefixOf(it.name) } } as List<String>

        then:
        names == ['STOR2', 'STOR1']
    }

    void 'alternate destinations of a cross-dock task are the other locations of its cross-dock zone'() {
        given:
        PutawaySite s = putawaySite('PA-ADX')
        String zone = tx {
            Location z = new Location(name: "ZONE-${uid()}".toString(), locationType: org.pih.warehouse.core.LocationType.findByLocationTypeCode(org.pih.warehouse.core.LocationTypeCode.ZONE),
                    parentLocation: Location.get(s.dc), active: true)
            z.save(flush: true, failOnError: true).id
        } as String
        String lane1 = bin(s.dc, 'LANE1', [ActivityCode.CROSS_DOCKING, ActivityCode.DELIVERY_TYPE_LOCAL_DELIVERY])
        String lane2 = bin(s.dc, 'LANE2', [ActivityCode.CROSS_DOCKING, ActivityCode.DELIVERY_TYPE_LOCAL_DELIVERY])
        tx { [lane1, lane2].each { Location l = Location.get(it); l.zone = Location.get(zone); l.save(flush: true, failOnError: true) } }
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[10, s.recv, lane1]])

        when:
        List<String> names = tx { putawayTaskService.getAlternateDestinations(PutawayTask.get(po.itemIds[0])).collect { prefixOf(it.name) }.sort() } as List<String>

        then:
        putawayTask(po.itemIds[0]).putawayTypeCode == 'CROSS_DOCK'
        names == ['LANE1', 'LANE2']
    }

    // ------------------------------------------------------------------ search / count / metrics

    SearchPutawayTaskCommand cmd(Map props) {
        return new SearchPutawayTaskCommand(props)
    }

    GrailsParameterMap params(Map p = [:]) {
        return new GrailsParameterMap(p, null)
    }

    void 'search: by facility and status category, a status narrows the category, a search term matches the product code prefix'() {
        given:
        PutawaySite s = putawaySite('PA-SE', 20)
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[5, s.recv, s.stor1], [6, s.recv, s.stor2], [7, s.recv, s.stor1]])
        putawayPatch(po.itemIds[0], [action: 'complete', destination: s.stor1, isDirectPutaway: true])
        putawayPatch(po.itemIds[1], [action: 'start'])
        String code = tx { org.pih.warehouse.product.Product.get(s.product).productCode } as String

        when:
        Closure<List<Integer>> q = { Map c, Map p -> tx { putawayTaskService.search(cmd([facility: Location.get(s.dc)] + c), params(p))*.quantity*.intValue().sort() } as List<Integer> }

        then:
        q([statusCategory: StatusCategory.OPEN], [:]) == [6, 7]
        q([statusCategory: StatusCategory.CLOSED], [:]) == [5]
        q([statusCategory: StatusCategory.OPEN], [status: 'STARTED']) == [6]
        q([:], [:]) == [5, 6, 7]
        q([searchTerm: code], [:]) == [5, 6, 7]
        q([searchTerm: 'zz-no-such-product'], [:]) == []
        tx { putawayTaskService.count(cmd([facility: Location.get(s.dc), statusCategory: StatusCategory.OPEN]), params()) } == 2
    }

    @PendingFeature(reason = 'PutawayTaskService.groovy:115-118 count() UNIONS the status params with the status category (statuses += category) while search() INTERSECTS them, so a list page filtered by status and category reports a total that does not match its rows')
    void 'count agrees with search for a status within a category'() {
        given:
        PutawaySite s = putawaySite('PA-CNT', 20)
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[5, s.recv, s.stor1], [6, s.recv, s.stor2]])
        putawayPatch(po.itemIds[1], [action: 'start'])

        when:
        SearchPutawayTaskCommand c = cmd([facility: tx { Location.get(s.dc) }, statusCategory: StatusCategory.OPEN])
        int rows = tx { putawayTaskService.search(cmd([facility: Location.get(s.dc), statusCategory: StatusCategory.OPEN]), params([status: 'STARTED'])).size() } as int
        int total = tx { putawayTaskService.count(cmd([facility: Location.get(s.dc), statusCategory: StatusCategory.OPEN]), params([status: 'STARTED'])) } as int

        then:
        rows == 1
        total == rows
    }

    @PendingFeature(reason = 'PutawayTaskService.groovy:47 the sort-order fallback is written `... ? params.order : \'desc\' as Integer`: any order value other than asc/desc throws NumberFormatException (HTTP 500) instead of falling back to desc')
    void 'search with an unknown sort order falls back to descending'() {
        given:
        PutawaySite s = putawaySite('PA-SO')
        putawayOrderOf(s.dc, s.product, s.lotId, [[5, s.recv, s.stor1]])

        when:
        List rows = tx { putawayTaskService.search(cmd([facility: Location.get(s.dc)]), params([order: 'sideways'])) } as List

        then:
        rows.size() == 1
    }

    void 'average putaway cycle time: minutes from task creation to completion for completed tasks in the window, 0 without any'() {
        given:
        PutawaySite s = putawaySite('PA-CT')
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[10, s.recv, s.stor1]])
        Date from = new Date() - 1
        Date to = new Date() + 1

        expect:
        tx { putawayTaskService.getAveragePutawayCycleTime(Location.get(s.dc), from, to) } == 0

        when:
        putawayPatch(po.itemIds[0], [action: 'complete', destination: s.stor1, isDirectPutaway: true])
        BigDecimal minutes = tx { putawayTaskService.getAveragePutawayCycleTime(Location.get(s.dc), from, to) } as BigDecimal

        then:
        minutes >= 0
        minutes < 5
        tx { putawayTaskService.getAveragePutawayCycleTime(Location.get(s.dc), from - 10, from) } == 0
    }
}
