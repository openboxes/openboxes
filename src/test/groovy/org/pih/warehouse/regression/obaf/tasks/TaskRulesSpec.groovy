package org.pih.warehouse.regression.obaf.tasks

import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.api.PickTaskStatus
import org.pih.warehouse.api.PutawayTaskAdapter
import org.pih.warehouse.api.PutawayTaskStatus
import org.pih.warehouse.api.StatusCategory
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.PutawayTypeCode
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.putaway.PutawayTask
import org.pih.warehouse.receiving.ReceiptItem
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.shipping.ShipmentItem

import static org.pih.warehouse.api.PickTaskStatus.*

/**
 * The pure rules behind pick and putaway tasks: the two state machines (allowed and rollback
 * transitions), the putaway status categories, PutawayTaskAdapter's mappings between tasks and
 * order items / orders, and PutawayTask's derived putaway type and backorder reference.
 */
class TaskRulesSpec extends Specification {

    static final Map<PutawayTaskStatus, List<PutawayTaskStatus>> PUTAWAY_ALLOWED = [
            (PutawayTaskStatus.PENDING)    : [PutawayTaskStatus.STARTED, PutawayTaskStatus.IN_PROGRESS, PutawayTaskStatus.COMPLETED, PutawayTaskStatus.CANCELED],
            (PutawayTaskStatus.STARTED)    : [PutawayTaskStatus.IN_PROGRESS, PutawayTaskStatus.COMPLETED, PutawayTaskStatus.CANCELED],
            (PutawayTaskStatus.IN_PROGRESS): [PutawayTaskStatus.COMPLETED, PutawayTaskStatus.CANCELED],
            (PutawayTaskStatus.COMPLETED)  : [], (PutawayTaskStatus.CANCELED): []]

    // ------------------------------------------------------------------ PickTaskStatus

    @Unroll
    void 'pick task #from -> #to is #verdict'() {
        expect:
        PickTaskStatus.validateTransition(from, to) == (verdict == 'allowed')

        where:
        [from, to] << [PickTaskStatus.values().toList(), PickTaskStatus.values().toList()].combinations()
        verdict = ([(PENDING): [PICKING, PICKED], (PICKING): [PICKING, PICKED, STAGED], (PICKED): [PICKED, STAGED], (STAGED): []][from].contains(to)) ? 'allowed' : 'refused'
    }

    void 'pick task: the allowed table, the rollback table, and nothing leaves STAGED'() {
        expect:
        PickTaskStatus.ALLOWED_STATE_TRANSITIONS == [(PENDING): [PICKING, PICKED] as Set, (PICKING): [PICKING, PICKED, STAGED] as Set,
                                                     (PICKED) : [PICKED, STAGED] as Set, (STAGED): [] as Set]
        PickTaskStatus.ROLLBACK_STATE_TRANSITIONS == [(PENDING): [] as Set, (PICKING): [PENDING] as Set,
                                                      (PICKED) : [PICKING, PENDING] as Set, (STAGED): [PICKED, PICKING, PENDING] as Set]
        PickTaskStatus.validateRollback(STAGED, PENDING)
        !PickTaskStatus.validateRollback(PENDING, PICKING)
        !PickTaskStatus.validateRollback(PICKED, STAGED)
        PickTaskStatus.list() == [PENDING, PICKING, PICKED, STAGED]
    }

    // ------------------------------------------------------------------ PutawayTaskStatus

    @Unroll
    void 'putaway task #from -> #to is #verdict'() {
        expect:
        PutawayTaskStatus.validateTransition(from, to) == (verdict == 'allowed')

        where:
        [from, to] << [PutawayTaskStatus.values().toList(), PutawayTaskStatus.values().toList()].combinations()
        verdict = (PUTAWAY_ALLOWED[from].contains(to)) ? 'allowed' : 'refused'
    }

    void 'putaway task: every status rolls back to PENDING; OPEN and CLOSED categories'() {
        expect:
        PutawayTaskStatus.values().every { PutawayTaskStatus.ROLLBACK_STATE_TRANSITIONS[it] == PutawayTaskStatus.PENDING }
        PutawayTaskStatus.values().every { PutawayTaskStatus.validateRollback(it, PutawayTaskStatus.PENDING) }
        !PutawayTaskStatus.validateRollback(PutawayTaskStatus.COMPLETED, PutawayTaskStatus.STARTED)
        PutawayTaskStatus.toSet(StatusCategory.OPEN) as Set == [PutawayTaskStatus.PENDING, PutawayTaskStatus.STARTED, PutawayTaskStatus.IN_PROGRESS] as Set
        PutawayTaskStatus.toSet(StatusCategory.CLOSED) as Set == [PutawayTaskStatus.COMPLETED, PutawayTaskStatus.CANCELED] as Set
        PutawayTaskStatus.toSet(null) == []
        PutawayTaskStatus.values().findAll { it.open } as Set == PutawayTaskStatus.toSet(StatusCategory.OPEN) as Set
        PutawayTaskStatus.values().findAll { it.closed } as Set == PutawayTaskStatus.toSet(StatusCategory.CLOSED) as Set
    }

    // ------------------------------------------------------------------ PutawayTaskAdapter

    @Unroll
    void 'adapter: task #status <-> order item #itemStatus, order #orderStatus'() {
        expect:
        PutawayTaskAdapter.toOrderItemStatusCode(status) == itemStatus
        PutawayTaskAdapter.toOrderStatus(status) == orderStatus
        PutawayTaskAdapter.convertFromOrderItemStatus(itemStatus) == status

        where:
        status                        | itemStatus                      | orderStatus
        PutawayTaskStatus.PENDING     | OrderItemStatusCode.PENDING     | OrderStatus.PENDING
        PutawayTaskStatus.STARTED     | OrderItemStatusCode.STARTED     | OrderStatus.APPROVED
        PutawayTaskStatus.IN_PROGRESS | OrderItemStatusCode.IN_PROGRESS | OrderStatus.PLACED
        PutawayTaskStatus.COMPLETED   | OrderItemStatusCode.COMPLETED   | OrderStatus.COMPLETED
        PutawayTaskStatus.CANCELED    | OrderItemStatusCode.CANCELED    | OrderStatus.CANCELED
    }

    void 'adapter: no status maps to PENDING both ways; BACKORDER reads as PENDING'() {
        expect:
        PutawayTaskAdapter.toOrderItemStatusCode(null) == OrderItemStatusCode.PENDING
        PutawayTaskAdapter.toOrderStatus(null) == OrderStatus.PENDING
        PutawayTaskAdapter.convertFromOrderItemStatus(null) == PutawayTaskStatus.PENDING
        PutawayTaskAdapter.convertFromOrderItemStatus(OrderItemStatusCode.BACKORDER) == PutawayTaskStatus.PENDING
        PutawayTaskAdapter.toPutawayTask((OrderItem) null) == null
    }

    void 'adapter: toOrderItem copies status, container, destination and discrepancy reason onto the item'() {
        given:
        Location cart = new Location(name: 'CART')
        Location dest = new Location(name: 'DEST')
        OrderItem item = new OrderItem(orderItemStatusCode: OrderItemStatusCode.PENDING)
        PutawayTask task = new PutawayTask(status: PutawayTaskStatus.IN_PROGRESS, container: cart, destination: dest,
                discrepancyReasonCode: org.pih.warehouse.core.ReasonCode.PUTAWAY_DISCREPANCY)

        when:
        OrderItem out = PutawayTaskAdapter.toOrderItem(task, item)

        then:
        out.is(item)
        item.orderItemStatusCode == OrderItemStatusCode.IN_PROGRESS
        item.containerLocation.is(cart)
        item.destinationBinLocation.is(dest)
        item.discrepancyReasonCode == org.pih.warehouse.core.ReasonCode.PUTAWAY_DISCREPANCY
        item.lastUpdated != null
    }

    void 'adapter: toPutawayTask(OrderItem) maps from the order (characterised: origin, orderedBy, dateOrdered and the order number, unlike the putaway_task view)'() {
        given:
        Location origin = new Location(name: 'ORIGIN')
        Location destination = new Location(name: 'DESTINATION')
        org.pih.warehouse.core.User orderedBy = new org.pih.warehouse.core.User(username: 'orderer')
        org.pih.warehouse.core.User approvedBy = new org.pih.warehouse.core.User(username: 'approver')
        Date ordered = new Date() - 2
        Date approved = new Date() - 1
        Order order = new Order(orderNumber: 'PA-1', origin: origin, destination: destination, orderedBy: orderedBy,
                approvedBy: approvedBy, dateOrdered: ordered, dateApproved: approved, status: OrderStatus.APPROVED)
        OrderItem item = new OrderItem(order: order, quantity: 4, orderItemStatusCode: OrderItemStatusCode.STARTED)
        item.id = 'item-1'

        when:
        PutawayTask t = PutawayTaskAdapter.toPutawayTask(item)

        then:
        t.id == 'item-1'
        t.status == PutawayTaskStatus.STARTED
        t.quantity == 4
        t.identifier == 'PA-1'
        t.facility.is(origin)
        t.assignee.is(orderedBy)
        t.dateStarted == ordered
        t.putawayOrderStatus == OrderStatus.APPROVED
    }

    // ------------------------------------------------------------------ PutawayTask derived values

    Location lane(Collection<ActivityCode> activities) {
        Location l = new Location(name: 'L')
        l.supportedActivities = activities*.id as Set<String>
        return l
    }

    @Unroll
    void 'putaway type of a task to #destination is #type'() {
        expect:
        new PutawayTask(destination: destination == 'nowhere' ? null : lane(activities)).putawayTypeCode == type

        where:
        destination      | activities                                                    || type
        'nowhere'        | []                                                            || PutawayTypeCode.UNASSIGNED
        'a cross-dock'   | [ActivityCode.CROSS_DOCKING, ActivityCode.LOST_AND_FOUND]     || PutawayTypeCode.CROSS_DOCK
        'lost and found' | [ActivityCode.LOST_AND_FOUND]                                 || PutawayTypeCode.QUARANTINE
        'a bin'          | [ActivityCode.PICK_STOCK, ActivityCode.PUTAWAY_STOCK]         || PutawayTypeCode.STANDARD
    }

    void 'backorder reference: only for a cross-dock task; the soft reference first, else the linked demand order number'() {
        given:
        Requisition order = new Requisition(requestNumber: 'REQ-9')
        RequisitionItem demand = new RequisitionItem()
        demand.requisition = order
        ShipmentItem soft = new ShipmentItem(backorderReference: 'SOFT-1', backorderItem: demand)
        ShipmentItem hard = new ShipmentItem(backorderItem: demand)

        expect:
        task([ActivityCode.CROSS_DOCKING], soft).backorderReferenceNumber == 'SOFT-1'
        task([ActivityCode.CROSS_DOCKING], hard).backorderReferenceNumber == 'REQ-9'
        task([ActivityCode.PICK_STOCK], soft).backorderReferenceNumber == null
        task([ActivityCode.CROSS_DOCKING], null).backorderReferenceNumber == null
    }

    PutawayTask task(Collection<ActivityCode> destinationActivities, ShipmentItem shipmentItem) {
        OrderItem item = new OrderItem()
        if (shipmentItem) {
            item.receiptItem = new ReceiptItem(shipmentItem: shipmentItem)
        }
        return new PutawayTask(destination: lane(destinationActivities), putawayOrderItem: item)
    }
}
