package org.pih.warehouse.api.regression.obaf.views

import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.PendingFeature

import java.util.zip.CRC32

import org.pih.warehouse.api.regression.obaf.support.ObafTaskIntegrationSpec
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.DeliveryTypeCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.order.OrderType
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionService
import org.pih.warehouse.requisition.RequisitionStatus

/**
 * The three views OBAF adds (pick_task, putaway_task, location_effective_supported_activities) and
 * the four it changes (cycle_count_metadata, edit_page_item, stock_movement_item,
 * stock_movement_list_item), read with SQL (or through their read-only domain) against seeded rows.
 */
class ObafViewsIntegrationSpec extends ObafTaskIntegrationSpec {

    @Autowired
    SessionFactory sessionFactory

    @Autowired
    RequisitionService requisitionService

    List<Map> sql(String query, Map params = [:]) {
        return tx {
            def q = sessionFactory.currentSession.createNativeQuery(query)
            params.each { k, v -> q.setParameter(k as String, v) }
            q.setResultTransformer(org.hibernate.transform.AliasToEntityMapResultTransformer.INSTANCE)
            q.list().collect { Map row -> row.collectEntries { k, v -> [(k as String).toLowerCase(), v] } }
        } as List<Map>
    }

    static String crc32(String s) {
        CRC32 c = new CRC32()
        c.update(s.getBytes('UTF-8'))
        return Long.toString(c.value)
    }

    // ------------------------------------------------------------------ pick_task

    void 'pick_task: one row per picklist item of a PICKING / PICKED / STAGED order, none before or after'() {
        given:
        PickSite s = pickSite('VW-PK')
        PickOrder o = pickOrder(s, [2, 3])

        expect:
        sql('select id, identifier, quantity_required, location_id, facility_id, requisition_item_id from pick_task where requisition_id = :r', [r: o.requisitionId])
                .collect { [it.id, it.identifier, it.quantity_required as Integer, it.location_id, it.facility_id, it.requisition_item_id] }
                .sort { it[2] } == [
                [o.taskIds[0], "PK-${crc32(o.taskIds[0])}".toString(), 2, s.bin, s.dc, o.requisitionItemId],
                [o.taskIds[1], "PK-${crc32(o.taskIds[1])}".toString(), 3, s.bin, s.dc, o.requisitionItemId]]

        when:
        List<Integer> rowsByStatus = [RequisitionStatus.VERIFYING, RequisitionStatus.PICKED, RequisitionStatus.STAGED, RequisitionStatus.ISSUED, RequisitionStatus.CANCELED].collect { RequisitionStatus st ->
            tx { Requisition r = Requisition.get(o.requisitionId); r.status = st; r.save(flush: true, failOnError: true) }
            sql('select id from pick_task where requisition_id = :r', [r: o.requisitionId]).size()
        }

        then:
        rowsByStatus == [0, 2, 2, 0, 0]
    }

    void 'pick_task: priority and staging lane follow the order delivery type'() {
        given:
        PickSite s = pickSite('VW-PR', DeliveryTypeCode.STOCK_TRANSFER_IBT)
        bin(s.dc, 'STAGE-PU', [ActivityCode.STAGING_LOCATION, ActivityCode.HOLD_STOCK, ActivityCode.DELIVERY_TYPE_PICKUP], false)
        bin(s.dc, 'NOHOLD-LD', [ActivityCode.STAGING_LOCATION, ActivityCode.DELIVERY_TYPE_LOCAL_DELIVERY], false)
        bin(s.dc, 'STAGE-SH', [ActivityCode.STAGING_LOCATION, ActivityCode.HOLD_STOCK, ActivityCode.DELIVERY_TYPE_SHIPPING], false)

        when:
        Map<String, List> byType = DeliveryTypeCode.values().collectEntries { DeliveryTypeCode code ->
            PickOrder o = pickOrder(s, [1], [deliveryTypeCode: code])
            Map t = pickTask(o.taskIds[0])
            [(code.name()): [t.priority, t.stagingLocation]]
        }

        then: 'priority = DeliveryTypeCode.priority; a lane needs STAGING_LOCATION + HOLD_STOCK + the DELIVERY_TYPE activity (none for DEFAULT)'
        byType == [PICK_UP           : [1, 'STAGE-PU'], LOCAL_DELIVERY: [2, null], SERVICE: [2, null], WILL_CALL: [3, null],
                   STOCK_TRANSFER_IBT: [3, 'STAGE'], SHIP_TO: [4, 'STAGE-SH'], DEFAULT: [5, null]]
        DeliveryTypeCode.values().every { byType[it.name()][0] == it.priority }
    }

    void 'pick_task: status defaults to PENDING; order progress counts and pick status follow the tasks'() {
        given:
        PickSite s = pickSite('VW-PG')
        PickOrder o = pickOrder(s, [1, 1, 1])
        String picker = person()

        expect:
        progress(o) == [3, 3, 'NOT_PICKED']
        pickTask(o.taskIds[0]).status == 'PENDING'

        when:
        pickPatch(o.taskIds[0], [action: 'start', assigneeId: picker])

        then: 'a started task counts as progress'
        progress(o) == [3, 3, 'PARTIALLY_PICKED']

        when:
        pickPatch(o.taskIds[0], [action: 'pick', outboundContainerId: s.tote, pickedById: picker])
        pickPatch(o.taskIds[1], [action: 'start', assigneeId: picker])
        pickPatch(o.taskIds[1], [action: 'short-pick', quantityPicked: 0, pickedById: picker, reasonCode: 'STOCKOUT'])

        then: 'the zero-quantity short pick (STAGED, nothing picked) leaves the total'
        progress(o) == [2, 1, 'PARTIALLY_PICKED']

        when:
        pickPatch(o.taskIds[2], [action: 'pick', outboundContainerId: s.tote, pickedById: picker])

        then:
        progress(o) == [2, 0, 'PICKED']
    }

    List progress(PickOrder o) {
        Map t = pickTask(o.taskIds[0])
        return [t.orderTotalTaskCount, t.orderOpenTaskCount, t.orderPickStatusCode]
    }

    // ------------------------------------------------------------------ putaway_task

    void 'putaway_task: one row per item of a PUTAWAY_ORDER order, at the order destination; other orders have none'() {
        given:
        PutawaySite s = putawaySite('VW-PA')
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[4, s.recv, s.stor1], [6, s.recv, s.stor2]])
        String purchaseItem = tx {
            Order order = new Order(name: "Test po ${uid()}".toString(), orderNumber: "LPO${uid()}".toString(),
                    orderType: OrderType.findByCode('PURCHASE_ORDER'), status: OrderStatus.PENDING,
                    origin: Location.get('2'), destination: Location.get(s.dc), orderedBy: Person.get('1'), dateOrdered: new Date())
            OrderItem item = new OrderItem(product: Product.get(s.product), quantity: 3, quantityPerUom: 1)
            order.addToOrderItems(item)
            order.save(flush: true, failOnError: true)
            item.id
        } as String

        expect:
        sql('select id, identifier, facility_id, location_id, destination_id, quantity, status, putaway_order_id from putaway_task where putaway_order_id = :o', [o: po.orderId])
                .collect { [it.id, it.identifier, it.facility_id, it.location_id, it.destination_id, it.quantity as Integer, it.status] }
                .sort { it[5] } == [
                [po.itemIds[0], "PT-${crc32(po.itemIds[0])}".toString(), s.dc, s.recv, s.stor1, 4, 'PENDING'],
                [po.itemIds[1], "PT-${crc32(po.itemIds[1])}".toString(), s.dc, s.recv, s.stor2, 6, 'PENDING']]
        sql('select id from putaway_task where id = :i', [i: purchaseItem]) == []
    }

    void 'putaway_task: status follows the order item status (no status reads PENDING)'() {
        given:
        PutawaySite s = putawaySite('VW-PS')
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[1, s.recv, s.stor1]])

        when:
        List<OrderItemStatusCode> statuses = [OrderItemStatusCode.PENDING, OrderItemStatusCode.STARTED, OrderItemStatusCode.IN_PROGRESS,
                                              OrderItemStatusCode.COMPLETED, OrderItemStatusCode.CANCELED, null]
        List<String> read = statuses.collect { OrderItemStatusCode code ->
            tx { OrderItem i = OrderItem.get(po.itemIds[0]); i.orderItemStatusCode = code; i.save(flush: true, failOnError: true) }
            putawayTask(po.itemIds[0]).status
        }

        then:
        read == ['PENDING', 'STARTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELED', 'PENDING']
    }

    void 'putaway_task: the delivery type comes from the destination lane DELIVERY_TYPE_* activity, DEFAULT otherwise'() {
        given:
        PutawaySite s = putawaySite('VW-PD', 20)
        Map<String, ActivityCode> lanes = [WILL_CALL: ActivityCode.DELIVERY_TYPE_WILL_CALL, PICK_UP: ActivityCode.DELIVERY_TYPE_PICKUP,
                                           SERVICE: ActivityCode.DELIVERY_TYPE_SERVICE, SHIP_TO: ActivityCode.DELIVERY_TYPE_SHIPPING,
                                           LOCAL_DELIVERY: ActivityCode.DELIVERY_TYPE_LOCAL_DELIVERY]

        when:
        Map<String, String> read = lanes.collectEntries { String name, ActivityCode code ->
            String lane = bin(s.dc, "LANE-${name}", [code, ActivityCode.CROSS_DOCKING])
            Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[1, s.recv, lane]])
            [(name): putawayTask(po.itemIds[0]).deliveryTypeCode]
        }
        Map plain = putawayOrderOf(s.dc, s.product, s.lotId, [[1, s.recv, s.stor1]])

        then:
        read == lanes.keySet().collectEntries { [(it): it] }
        putawayTask(plain.itemIds[0]).deliveryTypeCode == 'DEFAULT'
    }

    @PendingFeature(reason = 'views/putaway-task.sql:22-37 the delivery_type_code CASE has no DELIVERY_TYPE_STOCK_TRANSFER_IBT branch (pick-task.sql has one): a putaway task to a stock-transfer lane reads DEFAULT, so it is neither cross-dock demand of that type nor matched to its cart')
    void 'putaway_task: a STOCK_TRANSFER_IBT lane gives STOCK_TRANSFER_IBT'() {
        given:
        PutawaySite s = putawaySite('VW-PDI')
        String lane = bin(s.dc, 'LANE-IBT', [ActivityCode.DELIVERY_TYPE_STOCK_TRANSFER_IBT, ActivityCode.CROSS_DOCKING])
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[1, s.recv, lane]])

        expect:
        putawayTask(po.itemIds[0]).deliveryTypeCode == 'STOCK_TRANSFER_IBT'
    }

    void 'putaway_task: assignee and start date are the ORDER\'s, so starting one task shows on every task of the order (characterised FIXME)'() {
        given:
        PutawaySite s = putawaySite('VW-PAS')
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[4, s.recv, s.stor1], [6, s.recv, s.stor2]])

        when:
        putawayPatch(po.itemIds[0], [action: 'start'])

        then:
        putawayTask(po.itemIds[0]).subMap(['status', 'assignee']) == [status: 'STARTED', assignee: adminId()]
        putawayTask(po.itemIds[1]).subMap(['status', 'assignee']) == [status: 'PENDING', assignee: adminId()]
        putawayTask(po.itemIds[1]).dateStarted != null
    }

    // ------------------------------------------------------------------ location_effective_supported_activities

    void 'location_effective_supported_activities: a location own activities replace its type\'s; without any it falls back to the type'() {
        given:
        String dc = depot('VW-LE')
        String own = bin(dc, 'OWN', [ActivityCode.LOST_AND_FOUND], false)
        String binType = ObafTaskIntegrationSpec.BIN_TYPE
        String inherited = tx {
            Location b = new Location(name: "TYPE-${uid()}".toString(), locationType: org.pih.warehouse.core.LocationType.get(binType),
                    parentLocation: Location.get(dc), active: true)
            b.save(flush: true, failOnError: true).id
        } as String
        Set<String> typeActivities = tx { new HashSet<String>(org.pih.warehouse.core.LocationType.get(binType).supportedActivities.collect { it.toString() }) } as Set<String>

        expect:
        activities(own) == [ActivityCode.LOST_AND_FOUND.id] as Set
        activities(inherited) == typeActivities
        !typeActivities.isEmpty()
    }

    Set<String> activities(String locationId) {
        return sql('select supported_activities_string a from location_effective_supported_activities where location_id = :l', [l: locationId])*.a as Set<String>
    }

    // ------------------------------------------------------------------ stock_movement_item / edit_page_item

    void 'stock_movement_item and edit_page_item carry the backorder columns; a fully backordered line is revised to 0'() {
        given:
        String dc = depot('VW-BO')
        String p = product('VW-BO')
        String req = requisition(dc, [[productId: p, quantity: 5], [productId: p, quantity: 4]])
        List<String> items = itemIds(req)
        tx {
            RequisitionItem full = RequisitionItem.get(items[0])
            full.quantityBackordered = 5
            full.backorderedReasonCode = 'BACKORDER'
            full.save(flush: true, failOnError: true)
            RequisitionItem part = RequisitionItem.get(items[1])
            part.quantityBackordered = 1
            part.backorderedReasonCode = 'BACKORDER'
            part.save(flush: true, failOnError: true)
        }

        expect:
        sql('select id, quantity_backordered, backordered_reason_code, quantity_revised from stock_movement_item where id in (:ids)', [ids: items])
                .collect { [it.id, it.quantity_backordered as Integer, it.backordered_reason_code, it.quantity_revised as Integer] }
                .sort { it[1] } == [[items[1], 1, 'BACKORDER', null], [items[0], 5, 'BACKORDER', 0]]
        sql('select id, quantity_backordered, backordered_reason_code, quantity_revised from edit_page_item where id in (:ids)', [ids: items])
                .collect { [it.id, it.quantity_backordered as Integer, it.backordered_reason_code, it.quantity_revised as Integer] }
                .sort { it[1] } == [[items[1], 1, 'BACKORDER', null], [items[0], 5, 'BACKORDER', 0]]
    }

    // ------------------------------------------------------------------ stock_movement_list_item

    void 'stock_movement_list_item: delivery type, priority and the latest event log of the order'() {
        given:
        String dc = depot('VW-SL')
        String p = product('VW-SL')
        String req = requisition(dc, [[productId: p, quantity: 1]], [deliveryTypeCode: DeliveryTypeCode.WILL_CALL, priority: 7])

        expect: 'creating the order already logged an event'
        listItem(req) == [delivery_type_code: 'WILL_CALL', priority: 7, latest_event_log_code: 'EVENT_OCCURRED', latest_event_log_message: null]

        when:
        tx { requisitionService.addSystemEventLog(Requisition.get(req), 'first problem') }
        Thread.sleep(1100)
        tx { requisitionService.addSystemEventLog(Requisition.get(req), 'second problem') }

        then:
        listItem(req) == [delivery_type_code: 'WILL_CALL', priority: 7, latest_event_log_code: 'ERROR_OCCURRED', latest_event_log_message: 'second problem']
    }

    Map listItem(String requisitionId) {
        List<Map> rows = sql('select delivery_type_code, priority, latest_event_log_code, latest_event_log_message from stock_movement_list_item where id = :i', [i: requisitionId])
        assert rows.size() == 1
        return rows[0].collectEntries { k, v -> [k, k == 'priority' && v != null ? v as Integer : v] }
    }

    // ------------------------------------------------------------------ cycle_count_metadata

    void 'cycle_count_metadata: the inventory level cycle-count frequency overrides the ABC frequency'() {
        given:
        String dc = depot('VW-CC')
        String b1 = bin(dc, 'B1')
        String p = product('VW-CC')
        stock(dc, p, [[b1, 3, 'L', SOON]])
        String inventoryId = tx { Location.get(dc).inventory.id } as String

        expect: 'no inventory level: the ABC (DEFAULT) frequency'
        List<Map> before = metadata(inventoryId, p)
        before.size() == 1

        when:
        tx { new InventoryLevel(product: Product.get(p), inventory: Location.get(dc).inventory, cycleCountFrequencyDays: 11).save(flush: true, failOnError: true) }
        List<Map> after = metadata(inventoryId, p)

        then:
        after.size() == 1
        after[0].frequency as Integer == 11
        after[0].days_until_next_count != null
    }

    @PendingFeature(reason = 'views/cycle-count-metadata.sql:28-30 LEFT JOINs inventory_level on (inventory, product) only: a product with a bin-level inventory level as well as the facility one gets one cycle_count_metadata row per level, all with the same CRC32 id (the Hibernate entity then reads duplicates)')
    void 'cycle_count_metadata: one row per product and inventory even with bin-level inventory levels'() {
        given:
        String dc = depot('VW-CCD')
        String b1 = bin(dc, 'B1')
        String p = product('VW-CCD')
        stock(dc, p, [[b1, 3, 'L', SOON]])
        String inventoryId = tx { Location.get(dc).inventory.id } as String
        tx {
            new InventoryLevel(product: Product.get(p), inventory: Location.get(dc).inventory, cycleCountFrequencyDays: 11).save(flush: true, failOnError: true)
            new InventoryLevel(product: Product.get(p), inventory: Location.get(dc).inventory, internalLocation: Location.get(b1), cycleCountFrequencyDays: 30).save(flush: true, failOnError: true)
        }

        expect:
        metadata(inventoryId, p).size() == 1
    }

    List<Map> metadata(String inventoryId, String productId) {
        // (the CRC32 id is an unsigned INT the JDBC driver cannot read as Integer on a native query, so it is read as text)
        return sql('select cast(id as char) id, frequency, days_until_next_count from cycle_count_metadata where inventory_id = :i and product_id = :p', [i: inventoryId, p: productId])
    }
}
