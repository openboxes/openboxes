package org.pih.warehouse.api.regression.obaf.support

import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.DeliveryTypeCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.api.regression.support.RegressionRoleFixtures
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.order.OrderType
import org.pih.warehouse.picking.PickTaskService
import org.pih.warehouse.picklist.Picklist
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.putaway.PutawayService
import org.pih.warehouse.putaway.PutawayTaskService
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentType

/**
 * Base of the pick task / putaway task / sortation / container / receipt specs: the allocation
 * base's depots, bins, products, stock and orders, plus a picking site (bin, tote, staging lane),
 * a putaway site (receiving bin, cart, storage bins, lost and found), pick orders with a picklist
 * and shipment (the state the web or the allocation engine leaves a PICKING order in), multi-item
 * putaway orders and stock-on-hand reads that refresh availability first.
 *
 * PickTask (pick_task VIEW over picklist_item) and PutawayTask (putaway_task VIEW over the order
 * items of PUTAWAY_ORDER orders) are read in their own transactions, as a new request would.
 */
abstract class ObafTaskIntegrationSpec extends ObafIntegrationSpec {

    static final Date SOON = new Date() + 60

    @Autowired
    PickTaskService pickTaskService

    @Autowired
    PutawayTaskService putawayTaskService

    @Autowired
    PutawayService putawayService

    // ------------------------------------------------------------------ people

    String adminId() {
        return tx { User.findByUsername('admin').id } as String
    }

    /**
     * A User to act as a picker / assignee: picklist_item.assignee_id, picked_by_id and staged_by_id
     * reference `user`, not `person` (a plain Person fails the foreign key at flush).
     */
    String person(String prefix = 'Picker') {
        return RegressionRoleFixtures.ensureUser("test-${prefix.toLowerCase()}-${uid().toLowerCase()}".toString(),
                [RoleType.ROLE_ASSISTANT], [:]).id
    }

    // ------------------------------------------------------------------ picking

    static class PickSite {
        String dc, bin, alt, tote, staging, product, lot, lotId
        DeliveryTypeCode deliveryType
    }

    /**
     * A depot with TRACK_INTERNAL_TRANSACTIONS (unless {@code internal} is false), a storage bin
     * and an alternative bin holding {@code onHand} each of one lot, a tote (outbound container)
     * and a staging lane for {@code deliveryType}.
     */
    PickSite pickSite(String prefix, DeliveryTypeCode deliveryType = DeliveryTypeCode.LOCAL_DELIVERY,
                      boolean internal = true, int onHand = 20) {
        PickSite s = new PickSite(deliveryType: deliveryType)
        s.dc = depot(prefix, internal ? [ActivityCode.TRACK_INTERNAL_TRANSACTIONS] : [])
        ActivityCode lane = deliveryType.activityCode
        s.bin = bin(s.dc, 'BIN')
        s.alt = bin(s.dc, 'ALT')
        s.tote = bin(s.dc, 'TOTE', [ActivityCode.OUTBOUND_CONTAINER, ActivityCode.HOLD_STOCK] + (lane ? [lane] : []), false)
        s.staging = bin(s.dc, 'STAGE', [ActivityCode.STAGING_LOCATION, ActivityCode.HOLD_STOCK] + (lane ? [lane] : []), false)
        s.product = product(prefix)
        s.lot = "L-${uid()}".toString()
        stock(s.dc, s.product, [[s.bin, onHand, s.lot, SOON], [s.alt, onHand, s.lot, SOON]])
        s.lotId = lotId(s.product, s.lot)
        return s
    }

    static class PickOrder {
        String requisitionId, requestNumber, requisitionItemId, picklistId, shipmentId
        List<String> taskIds = []
    }

    /**
     * A PICKING order from the site's depot: one line for the sum of {@code quantities}, one picklist
     * item (pick task) per quantity, all from the site's bin, and an outbound shipment.
     */
    PickOrder pickOrder(PickSite s, List<Integer> quantities, Map props = [:], boolean withShipment = true) {
        int total = quantities.sum() as int
        String req = requisition(s.dc, [[productId: s.product, quantity: total]],
                [status: RequisitionStatus.PICKING, deliveryTypeCode: s.deliveryType] + props)
        return tx {
            Requisition r = Requisition.get(req)
            PickOrder o = new PickOrder(requisitionId: r.id, requestNumber: r.requestNumber,
                    requisitionItemId: r.requisitionItems.first().id)
            Picklist picklist = new Picklist(name: r.name, requisition: r)
            quantities.eachWithIndex { Integer q, int i ->
                picklist.addToPicklistItems(new PicklistItem(requisitionItem: r.requisitionItems.first(),
                        inventoryItem: InventoryItem.get(s.lotId), binLocation: Location.get(s.bin),
                        quantity: q, quantityPicked: 0, sortOrder: i))
            }
            picklist.save(flush: true, failOnError: true)
            r.picklist = picklist
            r.save(flush: true, failOnError: true)
            if (withShipment) {
                Shipment shipment = new Shipment(name: r.name, shipmentNumber: r.requestNumber, requisition: r,
                        origin: r.origin, destination: r.destination, expectedShippingDate: new Date() + 1,
                        shipmentType: ShipmentType.get('5'))
                shipment.save(flush: true, failOnError: true)
                o.shipmentId = shipment.id
            }
            o.picklistId = picklist.id
            o.taskIds = picklist.picklistItems.sort { it.sortOrder }*.id
            return o
        } as PickOrder
    }

    /** Runs a PickTaskService.patch in its own transaction (as a PATCH request would) and returns null or the failure. */
    Throwable pickPatch(String taskId, Map data) {
        try {
            tx { pickTaskService.patch(taskId, data) }
            return null
        } catch (Throwable t) {
            return t
        }
    }

    /** The pick task (view row) as a map of plain values. */
    Map pickTask(String id) {
        return tx {
            org.pih.warehouse.picking.PickTask t = org.pih.warehouse.picking.PickTask.get(id)
            if (!t) {
                return null
            }
            [id                 : t.id, identifier: t.identifier, status: t.status?.name(), requisitionId: t.requisition?.id,
             requisitionNumber  : t.requisitionNumber, requisitionStatus: t.requisitionStatus?.name(),
             deliveryTypeCode   : t.deliveryTypeCode?.name(), priority: t.priority, facility: t.facility?.id,
             requisitionItemId  : t.requisitionItem?.id, product: t.product?.id, location: prefixOf(t.location?.name),
             outboundContainer  : prefixOf(t.outboundContainer?.name), stagingLocation: prefixOf(t.stagingLocation?.name),
             inventoryItem      : t.inventoryItem?.id, quantityRequired: t.quantityRequired as Integer,
             quantityPicked     : t.quantityPicked as Integer, assignee: t.assignee?.id, pickedBy: t.pickedBy?.id,
             stagedBy           : t.stagedBy?.id, dateStarted: t.dateStarted, datePicked: t.datePicked,
             dateStaged         : t.dateStaged, reasonCode: t.reasonCode,
             orderTotalTaskCount: t.orderTotalTaskCount, orderOpenTaskCount: t.orderOpenTaskCount,
             orderPickStatusCode: t.orderPickStatusCode?.name()]
        } as Map
    }

    // ------------------------------------------------------------------ stock

    /**
     * Quantity on hand per bin prefix for one product at the depot, after a forced availability
     * refresh (transfers made with disableRefresh only queue an asynchronous refresh).
     */
    Map<String, Integer> onHand(String depotId, String productId) {
        tx { productAvailabilityService.refreshProductsAvailability(depotId, [productId], true) }
        Map<String, Integer> result = [:].withDefault { 0 }
        availability(depotId, productId).each { List key, List<Integer> v ->
            if (v[0]) {
                result[key[0] as String] += v[0]
            }
        }
        return new LinkedHashMap(result).findAll { k, v -> v } as Map<String, Integer>
    }

    // ------------------------------------------------------------------ putaway

    static class PutawaySite {
        String dc, recv, cart, stor1, stor2, lostAndFound, product, lot, lotId
    }

    /** A depot with a receiving bin holding {@code onHand} of one lot, a putaway cart, two storage bins and a lost and found bin. */
    PutawaySite putawaySite(String prefix, int onHand = 10, Collection<ActivityCode> depotExtra = [], boolean withLostAndFound = true) {
        PutawaySite s = new PutawaySite()
        s.dc = depot(prefix, depotExtra)
        s.recv = bin(s.dc, 'RECV', [ActivityCode.RECEIVE_STOCK])
        s.cart = bin(s.dc, 'CART', [ActivityCode.PUTAWAY_CART, ActivityCode.HOLD_STOCK], false)
        s.stor1 = bin(s.dc, 'STOR1')
        s.stor2 = bin(s.dc, 'STOR2')
        if (withLostAndFound) {
            s.lostAndFound = bin(s.dc, 'LNF', [ActivityCode.LOST_AND_FOUND, ActivityCode.HOLD_STOCK], false)
        }
        s.product = product(prefix)
        s.lot = "L-${uid()}".toString()
        stock(s.dc, s.product, [[s.recv, onHand, s.lot, SOON]])
        s.lotId = lotId(s.product, s.lot)
        return s
    }

    /** A PENDING putaway order with one item per row [quantity, fromBinId, toBinId]; returns [orderId, itemIds]. */
    Map putawayOrderOf(String depotId, String productId, String lotId, List<List> rows) {
        return tx {
            String n = uid()
            Location facility = Location.get(depotId)
            Order order = new Order(name: "Test putaway ${n}".toString(), orderNumber: "LPT${n}".toString(),
                    orderType: OrderType.findByCode(Constants.PUTAWAY_ORDER), status: OrderStatus.PENDING,
                    origin: facility, destination: facility, orderedBy: Person.get('1'), dateOrdered: new Date())
            List<OrderItem> items = rows.collect { List r ->
                OrderItem item = new OrderItem(product: Product.get(productId), inventoryItem: InventoryItem.get(lotId),
                        quantity: r[0] as Integer, quantityPerUom: 1, originBinLocation: Location.get(r[1] as String),
                        destinationBinLocation: r[2] ? Location.get(r[2] as String) : null,
                        orderItemStatusCode: OrderItemStatusCode.PENDING)
                order.addToOrderItems(item)
                item
            }
            order.save(flush: true, failOnError: true)
            return [orderId: order.id, itemIds: items*.id]
        } as Map
    }

    /** Runs a PutawayTaskService.patch in its own transaction; returns [result: task id or null, error: throwable]. */
    Map putawayPatch(String taskId, Map data) {
        try {
            String id = tx { putawayTaskService.patch(taskId, data)?.id } as String
            return [result: id, error: null]
        } catch (Throwable t) {
            return [result: null, error: t]
        }
    }

    /** The putaway task (view row) as a map of plain values. */
    Map putawayTask(String id) {
        return tx {
            org.pih.warehouse.putaway.PutawayTask t = org.pih.warehouse.putaway.PutawayTask.get(id)
            if (!t) {
                return null
            }
            [id               : t.id, identifier: t.identifier, status: t.status?.name(), product: t.product?.id,
             inventoryItem    : t.inventoryItem?.id, facility: t.facility?.id, location: prefixOf(t.location?.name),
             container        : prefixOf(t.container?.name), destination: prefixOf(t.destination?.name),
             quantity         : t.quantity as Integer, orderId: t.putawayOrder?.id, orderStatus: t.putawayOrderStatus?.name(),
             orderItemStatus  : t.putawayOrderItemStatus?.name(), assignee: t.assignee?.id, completedBy: t.completedBy?.id,
             orderedBy        : t.orderedBy?.id, dateStarted: t.dateStarted, dateCompleted: t.dateCompleted,
             deliveryTypeCode : t.deliveryTypeCode?.name(), discrepancyReasonCode: t.discrepancyReasonCode?.name(),
             comment          : t.comment, putawayTypeCode: t.putawayTypeCode?.name()]
        } as Map
    }

    /** Putaway task rows of an order, by quantity then status. */
    List<Map> putawayTasksOf(String orderId) {
        List<String> ids = tx {
            org.pih.warehouse.putaway.PutawayTask.findAllByPutawayOrder(Order.get(orderId))*.id
        } as List<String>
        return ids.collect { putawayTask(it) }.sort { a, b -> (a.quantity <=> b.quantity) ?: (a.status <=> b.status) }
    }

    String orderStatus(String orderId) {
        return tx { Order.get(orderId)?.status?.name() } as String
    }

    /** A facility-level InventoryLevel (internalLocation null) for the product with the given preferred bin. */
    String preferredBin(String depotId, String productId, String binId) {
        return tx {
            new InventoryLevel(product: Product.get(productId), inventory: Location.get(depotId).inventory,
                    preferredBinLocation: binId ? Location.get(binId) : null).save(flush: true, failOnError: true).id
        } as String
    }

    static Throwable rootCause(Throwable t) {
        Throwable c = t
        while (c?.cause && c.cause != c) {
            c = c.cause
        }
        return c
    }
}
