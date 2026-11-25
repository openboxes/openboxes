package org.pih.warehouse.api.regression.obaf.replenishment

import grails.validation.ValidationException

import org.pih.warehouse.api.regression.obaf.support.ObafTaskIntegrationSpec
import org.pih.warehouse.api.replenishment.SearchReplenishmentTaskCommand
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.DeliveryTypeCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.order.Order
import org.pih.warehouse.picking.StagingLocationNotFoundException
import org.pih.warehouse.replenishment.ReplenishmentTask

/**
 * ReplenishmentTaskService on a real database: the replenishment_task view, the lists to work on, start, pick, short
 * pick and drop to staging, and the stock moved when the facility tracks internal transactions (bin -> tote on pick,
 * tote -> staging lane on drop).
 *
 * Uses the picking site of the base spec: the replenishment picks from BIN to refill ALT (the destination bin).
 */
class ReplenishmentTaskIntegrationSpec extends ObafTaskIntegrationSpec {

    Throwable drop(String containerId, String stagingLocationId) {
        try {
            tx {
                replenishmentTaskService.drop(Location.get(containerId), [action: 'drop', stagingLocationId: stagingLocationId])
            }
            return null
        } catch (Throwable t) {
            return t
        }
    }

    /** The tasks of the order (view rows), the original ones first, then those reallocated. */
    List<Map> tasksOf(ReplenOrder o) {
        List<String> ids = tx {
            ReplenishmentTask.findAllByOrder(Order.get(o.orderId))*.id
        } as List<String>
        return ids.collect { replenTask(it) }.sort { o.taskIds.contains(it.id) ? o.taskIds.indexOf(it.id) : o.taskIds.size() }
    }

    List<Map> lists(PickSite s, List<String> statuses = []) {
        return tx { replenishmentTaskService.listReplenishments(Location.get(s.dc), statuses) } as List<Map>
    }

    Map listOf(PickSite s, ReplenOrder o) {
        return lists(s).find { it.id == o.orderId }?.subMap(['status', 'tasksCount', 'openTasksCount']) as Map
    }

    // ------------------------------------------------------------------ view

    void 'the view has a task per picklist item of a replenishment line, with its bins; plain stock transfer lines are left out'() {
        given:
        PickSite s = pickSite('RT-VW')
        ReplenOrder replen = replenOrder(s, [3])
        ReplenOrder transfer = replenOrder(s, [4], null)

        expect:
        replenTask(replen.taskIds[0]).subMap(['status', 'location', 'destinationLocation', 'quantityRequired', 'facility']) ==
                [status: 'PENDING', location: 'BIN', destinationLocation: 'ALT', quantityRequired: 3, facility: s.dc]
        replenTask(replen.taskIds[0]).identifier.startsWith('RT-')
        replenTask(transfer.taskIds[0]) == null
    }

    void 'search: the tasks of a list in the given statuses, by order id or number'() {
        given:
        PickSite s = pickSite('RT-SR')
        ReplenOrder o = replenOrder(s, [2, 3])
        replenPatch(o.taskIds[0], [action: 'start'])
        replenPatch(o.taskIds[0], [action: 'pick', outboundContainerId: s.tote])

        when:
        List<String> open = tx {
            replenishmentTaskService.search(new SearchReplenishmentTaskCommand(facility: Location.get(s.dc), orderId: o.orderNumber,
                    status: [org.pih.warehouse.api.PickTaskStatus.PENDING, org.pih.warehouse.api.PickTaskStatus.PICKING]))*.id
        } as List<String>
        List<String> all = tx {
            replenishmentTaskService.search(new SearchReplenishmentTaskCommand(facility: Location.get(s.dc), orderId: o.orderId,
                    status: org.pih.warehouse.api.PickTaskStatus.values() as List))*.id
        } as List<String>

        then:
        open == [o.taskIds[1]]
        all as Set == o.taskIds as Set
    }

    // ------------------------------------------------------------------ lists

    void 'lists: PENDING until a task is started, PICKING while tasks are open, PICKED while picks wait for staging, then gone'() {
        given:
        PickSite s = pickSite('RT-LS')
        ReplenOrder o = replenOrder(s, [2, 3])

        expect:
        listOf(s, o) == [status: 'PENDING', tasksCount: 2, openTasksCount: 2]

        when:
        replenPatch(o.taskIds[0], [action: 'start'])
        replenPatch(o.taskIds[0], [action: 'pick', outboundContainerId: s.tote])

        then:
        listOf(s, o) == [status: 'PICKING', tasksCount: 2, openTasksCount: 1]
        lists(s, ['PENDING']).every { it.id != o.orderId }

        when:
        replenPatch(o.taskIds[1], [action: 'start'])
        replenPatch(o.taskIds[1], [action: 'pick', outboundContainerId: s.tote])

        then:
        listOf(s, o) == [status: 'PICKED', tasksCount: 2, openTasksCount: 0]

        when:
        Throwable e = drop(s.tote, s.staging)

        then:
        e == null
        listOf(s, o) == null
    }

    // ------------------------------------------------------------------ start

    void 'start: PENDING -> PICKING for the signed-in user, who may start it again; a task assigned to someone else is refused'() {
        given:
        PickSite s = pickSite('RT-ST')
        ReplenOrder o = replenOrder(s, [5])
        ReplenOrder other = replenOrder(s, [5])
        String picker = person()

        when:
        Throwable first = replenPatch(o.taskIds[0], [action: 'start'])
        Throwable again = replenPatch(o.taskIds[0], [action: 'start'])

        then:
        first == null
        again == null
        replenTask(o.taskIds[0]).subMap(['status', 'assignee']) == [status: 'PICKING', assignee: adminId()]

        when: 'admin assigns a task to a picker, then tries to start it for themselves'
        Throwable assigned = replenPatch(other.taskIds[0], [action: 'start', assigneeId: picker])
        Throwable refused = replenPatch(other.taskIds[0], [action: 'start'])

        then:
        assigned == null
        refused instanceof ValidationException
        refused.message.contains('already assigned')
        replenTask(other.taskIds[0]).assignee == picker
    }

    // ------------------------------------------------------------------ pick

    void 'pick: moves the required quantity from the bin into the tote, PICKED, by the signed-in user'() {
        given:
        PickSite s = pickSite('RT-PK')
        ReplenOrder o = replenOrder(s, [3])
        replenPatch(o.taskIds[0], [action: 'start'])

        when:
        String toteNumber = tx { Location.get(s.tote).locationNumber }
        Throwable e = replenPatch(o.taskIds[0], [action: 'pick', outboundContainerId: toteNumber])

        then:
        e == null
        replenTask(o.taskIds[0]).subMap(['status', 'quantityPicked', 'outboundContainer', 'pickedBy']) ==
                [status: 'PICKED', quantityPicked: 3, outboundContainer: 'TOTE', pickedBy: adminId()]
        onHand(s.dc, s.product) == [BIN: 17, ALT: 20, TOTE: 3]
    }

    void 'pick: an unknown tote, one of another facility or a bin that is not an outbound container is refused'() {
        given:
        PickSite s = pickSite('RT-PKW')
        ReplenOrder o = replenOrder(s, [2])
        String otherTote = pickSite('RT-PKW2').tote

        expect:
        replenPatch(o.taskIds[0], [action: 'pick', outboundContainerId: 'no-such-tote']).message.contains('does not exist')
        replenPatch(o.taskIds[0], [action: 'pick', outboundContainerId: otherTote]).message.contains('does not exist')
        replenPatch(o.taskIds[0], [action: 'pick', outboundContainerId: s.alt]).message.contains('OUTBOUND_CONTAINER')
        replenTask(o.taskIds[0]).status == 'PENDING'
        onHand(s.dc, s.product) == [BIN: 20, ALT: 20]
    }

    // ------------------------------------------------------------------ short pick

    void 'short pick with a reason: records the quantity and the reason, PICKED, the shortage goes to lost and found; the reason may be given as an object'() {
        given:
        PickSite s = pickSite('RT-SP')
        ReplenOrder o = replenOrder(s, [5])
        replenPatch(o.taskIds[0], [action: 'start'])

        when:
        Throwable e = replenPatch(o.taskIds[0], [action: 'short-pick', outboundContainerId: s.tote, quantityPicked: 2,
                                                 reasonCode: [name: 'DAMAGED']])

        then:
        e == null
        replenTask(o.taskIds[0]).subMap(['status', 'quantityPicked', 'reasonCode', 'outboundContainer', 'pickedBy']) ==
                [status: 'PICKED', quantityPicked: 2, reasonCode: 'DAMAGED', outboundContainer: 'TOTE', pickedBy: adminId()]
        // the 3 not found are held in lost and found; ALT is the destination, so nothing is reallocated
        onHand(s.dc, s.product) == [BIN: 15, ALT: 20, TOTE: 2, LNF: 3]
        tasksOf(o).size() == 1
    }

    void 'short pick of zero with a reason: nothing is picked, the 5 go to lost and found, STAGED (nothing to stage)'() {
        given:
        PickSite s = pickSite('RT-SZ')
        ReplenOrder o = replenOrder(s, [5])
        replenPatch(o.taskIds[0], [action: 'start'])

        when:
        Throwable e = replenPatch(o.taskIds[0], [action: 'short-pick', outboundContainerId: null, quantityPicked: 0,
                                                 reasonCode: 'INSUFFICIENT_QUANTITY_AVAILABLE'])

        then:
        e == null
        replenTask(o.taskIds[0]).subMap(['status', 'quantityPicked', 'reasonCode']) ==
                [status: 'STAGED', quantityPicked: 0, reasonCode: 'INSUFFICIENT_QUANTITY_AVAILABLE']
        onHand(s.dc, s.product) == [BIN: 15, ALT: 20, LNF: 5]
    }

    void 'short pick of more than required is refused and moves nothing'() {
        given:
        PickSite s = pickSite('RT-SO')
        ReplenOrder o = replenOrder(s, [3])
        replenPatch(o.taskIds[0], [action: 'start'])

        when:
        Throwable e = replenPatch(o.taskIds[0], [action: 'short-pick', outboundContainerId: s.tote, quantityPicked: 4,
                                                 reasonCode: 'DAMAGED'])

        then:
        e instanceof ValidationException
        replenTask(o.taskIds[0]).subMap(['status', 'quantityPicked']) == [status: 'PICKING', quantityPicked: 0]
        onHand(s.dc, s.product) == [BIN: 20, ALT: 20]
    }

    // ------------------------------------------------------------------ shortage: hold and reallocate

    void 'shortage: the quantity not found is held in lost and found, and a new task picks it from another bin'() {
        given: 'a replenishment of DISP, with stock in BIN and ALT'
        PickSite s = pickSite('RT-SH')
        String disp = bin(s.dc, 'DISP')
        ReplenOrder o = replenOrder(s, [5], disp)
        replenPatch(o.taskIds[0], [action: 'start'])

        when:
        Throwable e = replenPatch(o.taskIds[0], [action: 'short-pick', outboundContainerId: s.tote, quantityPicked: 2,
                                                 reasonCode: 'DIFFERENT_LOCATION'])

        then:
        e == null
        onHand(s.dc, s.product) == [BIN: 15, ALT: 20, TOTE: 2, LNF: 3]

        and: 'a PENDING task of the same order picks the 3 from ALT, and the list has it to pick'
        List<Map> reallocated = tasksOf(o).findAll { it.id != o.taskIds[0] }
        reallocated*.subMap(['status', 'location', 'destinationLocation', 'quantityRequired']) ==
                [[status: 'PENDING', location: 'ALT', destinationLocation: 'DISP', quantityRequired: 3]]
        listOf(s, o) == [status: 'PICKING', tasksCount: 2, openTasksCount: 1]
    }

    void 'shortage: neither the bin that came up short nor the bin being replenished is reallocated from'() {
        given: 'a replenishment of ALT, whose only other stock is in BIN'
        PickSite s = pickSite('RT-SX')
        ReplenOrder o = replenOrder(s, [5])
        replenPatch(o.taskIds[0], [action: 'start'])

        when:
        Throwable e = replenPatch(o.taskIds[0], [action: 'short-pick', outboundContainerId: null, quantityPicked: 0,
                                                 reasonCode: 'INSUFFICIENT_QUANTITY_AVAILABLE'])

        then:
        e == null
        tasksOf(o)*.id == o.taskIds
        listOf(s, o) == null
    }

    void 'shortage: only what is left in the bin is held, and only what is available elsewhere is reallocated'() {
        given: '4 in BIN and 4 in ALT, 6 to replenish'
        PickSite s = pickSite('RT-SC', DeliveryTypeCode.LOCAL_DELIVERY, true, 4)
        String disp = bin(s.dc, 'DISP')
        ReplenOrder o = replenOrder(s, [6], disp)
        replenPatch(o.taskIds[0], [action: 'start'])

        when:
        Throwable e = replenPatch(o.taskIds[0], [action: 'short-pick', outboundContainerId: null, quantityPicked: 0,
                                                 reasonCode: 'INSUFFICIENT_QUANTITY_AVAILABLE'])

        then:
        e == null
        onHand(s.dc, s.product) == [ALT: 4, LNF: 4]
        tasksOf(o).findAll { it.id != o.taskIds[0] }*.subMap(['location', 'quantityRequired']) ==
                [[location: 'ALT', quantityRequired: 4]]
    }

    void 'shortage at a facility without lost and found: nothing is held, the shortage is still reallocated'() {
        given:
        PickSite s = pickSite('RT-SN', DeliveryTypeCode.LOCAL_DELIVERY, true, 20, false)
        String disp = bin(s.dc, 'DISP')
        ReplenOrder o = replenOrder(s, [5], disp)
        replenPatch(o.taskIds[0], [action: 'start'])

        when:
        Throwable e = replenPatch(o.taskIds[0], [action: 'short-pick', outboundContainerId: s.tote, quantityPicked: 2,
                                                 reasonCode: 'DAMAGED'])

        then:
        e == null
        onHand(s.dc, s.product) == [BIN: 18, ALT: 20, TOTE: 2]
        tasksOf(o).findAll { it.id != o.taskIds[0] }*.subMap(['location', 'quantityRequired']) ==
                [[location: 'ALT', quantityRequired: 3]]
    }

    void 'a short pick without a reason is no shortage, and a shortage is handled once'() {
        given:
        PickSite s = pickSite('RT-S1')
        String disp = bin(s.dc, 'DISP')
        ReplenOrder o = replenOrder(s, [5], disp)
        replenPatch(o.taskIds[0], [action: 'start'])

        when: 'part is picked without a reason: the task stays open'
        Throwable partial = replenPatch(o.taskIds[0], [action: 'short-pick', outboundContainerId: s.tote, quantityPicked: 1])

        then:
        partial == null
        replenTask(o.taskIds[0]).status == 'PICKING'
        onHand(s.dc, s.product) == [BIN: 19, ALT: 20, TOTE: 1]
        tasksOf(o).size() == 1

        when: 'the rest is reported short, then reported short again'
        Throwable first = replenPatch(o.taskIds[0], [action: 'short-pick', outboundContainerId: null, quantityPicked: 0,
                                                     reasonCode: 'DAMAGED'])
        Throwable again = replenPatch(o.taskIds[0], [action: 'short-pick', outboundContainerId: null, quantityPicked: 0,
                                                     reasonCode: 'DAMAGED'])

        then: 'the 4 are held and reallocated once'
        first == null
        again == null
        replenTask(o.taskIds[0]).status == 'PICKED'
        onHand(s.dc, s.product) == [BIN: 15, ALT: 20, TOTE: 1, LNF: 4]
        tasksOf(o).findAll { it.id != o.taskIds[0] }*.quantityRequired == [4]
    }

    // ------------------------------------------------------------------ drop

    void 'drop: the picks of the tote move to the staging lane once each, even two tasks of one lot, STAGED by the signed-in user'() {
        given:
        PickSite s = pickSite('RT-DR')
        ReplenOrder o = replenOrder(s, [3, 4])
        o.taskIds.each {
            replenPatch(it, [action: 'start'])
            replenPatch(it, [action: 'pick', outboundContainerId: s.tote])
        }

        when:
        String stagingNumber = tx { Location.get(s.staging).locationNumber }
        List<String> staged = tx {
            replenishmentTaskService.drop(Location.get(s.tote), [action: 'drop', stagingLocationId: stagingNumber])*.id
        } as List<String>

        then:
        staged as Set == o.taskIds as Set
        o.taskIds.every {
            replenTask(it).subMap(['status', 'stagingLocation', 'stagedBy']) ==
                    [status: 'STAGED', stagingLocation: 'STAGE', stagedBy: adminId()]
        }
        onHand(s.dc, s.product) == [BIN: 13, ALT: 20, STAGE: 7]
    }

    void 'drop: a staging location of another facility, a bin that is not a staging location, or an empty tote is refused'() {
        given:
        PickSite s = pickSite('RT-DW')
        ReplenOrder o = replenOrder(s, [3])
        String otherStaging = pickSite('RT-DW2').staging

        when:
        Throwable empty = drop(s.tote, s.staging)
        replenPatch(o.taskIds[0], [action: 'start'])
        replenPatch(o.taskIds[0], [action: 'pick', outboundContainerId: s.tote])
        Throwable other = drop(s.tote, otherStaging)
        Throwable notStaging = drop(s.tote, s.alt)

        then:
        empty instanceof IllegalStateException
        empty.message.contains('is empty')
        other instanceof StagingLocationNotFoundException
        notStaging.message.contains('STAGING_LOCATION')
        replenTask(o.taskIds[0]).status == 'PICKED'
        onHand(s.dc, s.product) == [BIN: 17, ALT: 20, TOTE: 3]
    }

    void 'drop: a staging lane not set aside for bin replenishment (an outbound lane) is refused'() {
        given:
        PickSite s = pickSite('RT-DB')
        ReplenOrder o = replenOrder(s, [3])
        String outboundLane = bin(s.dc, 'OUTSTAGE', [ActivityCode.STAGING_LOCATION, ActivityCode.HOLD_STOCK], false)
        replenPatch(o.taskIds[0], [action: 'start'])
        replenPatch(o.taskIds[0], [action: 'pick', outboundContainerId: s.tote])

        when:
        Throwable e = drop(s.tote, outboundLane)

        then:
        e instanceof IllegalArgumentException
        e.message.contains('BIN_REPLENISHMENT')
        replenTask(o.taskIds[0]).status == 'PICKED'
        onHand(s.dc, s.product) == [BIN: 17, ALT: 20, TOTE: 3]
    }

    // ------------------------------------------------------------------ facility

    void 'a task of another facility is not found'() {
        given:
        PickSite s = pickSite('RT-FC')
        PickSite other = pickSite('RT-FC2')
        ReplenOrder o = replenOrder(s, [3])

        expect:
        tx { replenishmentTaskService.get(o.taskIds[0], Location.get(other.dc)) } == null
        rootCause(tx {
            try {
                replenishmentTaskService.patch(o.taskIds[0], [action: 'start'], Location.get(other.dc))
                null
            } catch (Throwable t) {
                t
            }
        } as Throwable) instanceof org.hibernate.ObjectNotFoundException
    }
}
