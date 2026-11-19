package org.pih.warehouse.api.regression.obaf.replenishment

import grails.validation.ValidationException
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.regression.obaf.support.ObafTaskIntegrationSpec
import org.pih.warehouse.api.replenishment.SearchReplenishmentTaskCommand
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.order.OrderType
import org.pih.warehouse.order.OrderTypeCode
import org.pih.warehouse.picking.StagingLocationNotFoundException
import org.pih.warehouse.picklist.Picklist
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.replenishment.ReplenishmentTask
import org.pih.warehouse.replenishment.ReplenishmentTaskService

/**
 * ReplenishmentTaskService on a real database: the replenishment_task view, the lists to work on, start, pick, short
 * pick and drop to staging, and the stock moved when the facility tracks internal transactions (bin -> tote on pick,
 * tote -> staging lane on drop).
 *
 * Uses the picking site of the base spec: the replenishment picks from BIN to refill ALT (the destination bin).
 */
class ReplenishmentTaskIntegrationSpec extends ObafTaskIntegrationSpec {

    @Autowired
    ReplenishmentTaskService replenishmentTaskService

    static class ReplenOrder {
        String orderId, orderNumber
        List<String> taskIds = []
    }

    /**
     * An APPROVED transfer order of the site's depot: one line per quantity, each from BIN into the destination bin
     * (ALT unless given; null makes a plain stock transfer line), with one picklist item (task) per line.
     */
    ReplenOrder replenOrder(PickSite s, List<Integer> quantities, String destinationBin = s.alt) {
        return tx {
            String n = uid()
            Location facility = Location.get(s.dc)
            Order order = new Order(name: "Test replenishment ${n}".toString(), orderNumber: "R${n}".toString(),
                    orderType: OrderType.findByCode(OrderTypeCode.TRANSFER_ORDER.name()), status: OrderStatus.APPROVED,
                    origin: facility, destination: facility, orderedBy: Person.get('1'), dateOrdered: new Date())
            List<OrderItem> items = quantities.collect { Integer q ->
                OrderItem item = new OrderItem(product: Product.get(s.product), inventoryItem: InventoryItem.get(s.lotId),
                        quantity: q, quantityPerUom: 1, originBinLocation: Location.get(s.bin),
                        destinationBinLocation: destinationBin ? Location.get(destinationBin) : null,
                        orderItemStatusCode: OrderItemStatusCode.PENDING)
                order.addToOrderItems(item)
                item
            }
            order.save(flush: true, failOnError: true)

            Picklist picklist = new Picklist(name: order.name, order: order)
            items.eachWithIndex { OrderItem item, int i ->
                picklist.addToPicklistItems(new PicklistItem(orderItem: item, inventoryItem: item.inventoryItem,
                        binLocation: item.originBinLocation, quantity: item.quantity, quantityPicked: 0, sortOrder: i))
            }
            picklist.save(flush: true, failOnError: true)
            return new ReplenOrder(orderId: order.id, orderNumber: order.orderNumber,
                    taskIds: picklist.picklistItems.sort { it.sortOrder }*.id)
        } as ReplenOrder
    }

    Throwable replenPatch(String taskId, Map data) {
        try {
            tx { replenishmentTaskService.patch(taskId, data) }
            return null
        } catch (Throwable t) {
            return t
        }
    }

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

    Map replenTask(String id) {
        return tx {
            ReplenishmentTask t = ReplenishmentTask.get(id)
            if (!t) {
                return null
            }
            [id               : t.id, status: t.status?.name(), orderId: t.order?.id, facility: t.facility?.id,
             location         : prefixOf(t.location?.name), destinationLocation: prefixOf(t.destinationLocation?.name),
             outboundContainer: prefixOf(t.outboundContainer?.name), stagingLocation: prefixOf(t.stagingLocation?.name),
             quantityRequired : t.quantityRequired as Integer, quantityPicked: t.quantityPicked as Integer,
             assignee         : t.assignee?.id, pickedBy: t.pickedBy?.id, stagedBy: t.stagedBy?.id,
             reasonCode       : t.reasonCode, identifier: t.identifier]
        } as Map
    }

    List<Map> lists(PickSite s, List<String> statuses = []) {
        return tx { replenishmentTaskService.listReplenishments(Location.get(s.dc), statuses) } as List<Map>
    }

    Map listOf(PickSite s, ReplenOrder o) {
        return lists(s).find { it.id == o.orderId }?.subMap(['status', 'tasksCount']) as Map
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
        listOf(s, o) == [status: 'PENDING', tasksCount: 2]

        when:
        replenPatch(o.taskIds[0], [action: 'start'])
        replenPatch(o.taskIds[0], [action: 'pick', outboundContainerId: s.tote])

        then:
        listOf(s, o) == [status: 'PICKING', tasksCount: 1]
        lists(s, ['PENDING']).every { it.id != o.orderId }

        when:
        replenPatch(o.taskIds[1], [action: 'start'])
        replenPatch(o.taskIds[1], [action: 'pick', outboundContainerId: s.tote])

        then:
        listOf(s, o) == [status: 'PICKED', tasksCount: 0]

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

    void 'short pick with a reason: records the quantity and the reason, PICKED; the reason may be given as an object'() {
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
        onHand(s.dc, s.product) == [BIN: 18, ALT: 20, TOTE: 2]
    }

    void 'short pick of zero with a reason: nothing moves, STAGED (nothing to stage)'() {
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
        onHand(s.dc, s.product) == [BIN: 20, ALT: 20]
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
