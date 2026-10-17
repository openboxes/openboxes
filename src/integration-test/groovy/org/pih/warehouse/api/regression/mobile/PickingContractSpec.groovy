package org.pih.warehouse.api.regression.mobile

import org.springframework.beans.factory.annotation.Autowired
import spock.lang.PendingFeature

import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.DeliveryTypeCode
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.EventType
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.picklist.Picklist
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.requisition.RequisitionType
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentType

/**
 * Outbound: the picking list (old picklist flow), pick tasks (discrete
 * picking: start, pick into a tote, short-pick, drop at staging, reallocate),
 * packing location, and Pick Up allocation. Runs in "Test Mobile DC" (see
 * MobileContractSpec.regressionDc). Every feature builds its own order: picking
 * moves stock and changes requisition status for good.
 *
 * Orders are built with GORM (requisition in PICKING with a picklist and a
 * shipment), like the web workflow leaves them, because the workflow API at a
 * depot that requires mobile picking stops before the shipment exists.
 */
class PickingContractSpec extends MobileContractSpec {

    @Autowired
    ProductAvailabilityService productAvailabilityService

    @Override
    String sessionLocationId() {
        return regressionDc().id
    }

    /** One order ready to pick: requisition PICKING, LOCAL_DELIVERY, picklist item(s), shipment. */
    static class PickOrder {
        Location dc, destination, bin, tote, staging, packing
        Product product
        InventoryItem lot
        Requisition requisition
        RequisitionItem requisitionItem
        List<String> taskIds = []
        String picklistId
        String shipmentId
    }

    void ensureEventTypes() {
        EventType.withNewTransaction {
            [EventCode.PACKING_SCHEDULED, EventCode.LOADING_SCHEDULED].each { EventCode code ->
                if (!EventType.findByEventCode(code)) {
                    new EventType(name: code.name(), eventCode: code).save(flush: true, failOnError: true)
                }
            }
        }
    }

    PickOrder pickOrder(int quantity = 5, int lines = 1) {
        ensureEventTypes()
        PickOrder o = new PickOrder()
        o.dc = regressionDc()
        String delivery = ActivityCode.DELIVERY_TYPE_LOCAL_DELIVERY.id
        o.destination = newDepot('PK-DEST')
        o.bin = newBin('PK-BIN', [ActivityCode.PICK_STOCK.id, ActivityCode.PUTAWAY_STOCK.id], BIN_TYPE, o.dc)
        o.tote = newBin('PK-TOTE', [ActivityCode.OUTBOUND_CONTAINER.id, ActivityCode.HOLD_STOCK.id, delivery], BIN_TYPE, o.dc)
        o.staging = newBin('PK-STAGE', [ActivityCode.STAGING_LOCATION.id, ActivityCode.HOLD_STOCK.id, delivery], BIN_TYPE, o.dc)
        o.packing = newBin('PK-PACK', [ActivityCode.PACK_STOCK.id], BIN_TYPE, o.dc)
        o.product = newProduct('PK')
        String lotNumber = "LOT-${uid()}".toString()
        Date expiry = new Date() + 365
        stock(o.product, [[o.bin, quantity * lines * 2, lotNumber, expiry]], o.dc)
        Requisition.withNewTransaction {
            o.lot = InventoryItem.findByProductAndLotNumber(Product.get(o.product.id), lotNumber)
            Requisition r = new Requisition(
                    name: "Test pick ${uid()}".toString(), requestNumber: "LP${uid()}".toString(),
                    origin: Location.get(o.dc.id), destination: Location.get(o.destination.id),
                    requestedBy: Person.get('1'), dateRequested: new Date(), requestedDeliveryDate: new Date(),
                    status: RequisitionStatus.PICKING, type: RequisitionType.DEFAULT, sourceType: null,
                    deliveryTypeCode: DeliveryTypeCode.LOCAL_DELIVERY)
            RequisitionItem ri = new RequisitionItem(product: Product.get(o.product.id), quantity: quantity * lines,
                    quantityApproved: quantity * lines, substitutable: false)
            r.addToRequisitionItems(ri)
            r.save(flush: true, failOnError: true)
            Picklist picklist = new Picklist(name: r.name, requisition: r)
            (0..<lines).each { int i ->
                picklist.addToPicklistItems(new PicklistItem(requisitionItem: ri, inventoryItem: o.lot,
                        binLocation: Location.get(o.bin.id), quantity: quantity, quantityPicked: 0, sortOrder: i))
            }
            picklist.save(flush: true, failOnError: true)
            r.picklist = picklist
            r.save(flush: true, failOnError: true)
            Shipment shipment = new Shipment(name: r.name, shipmentNumber: r.requestNumber, requisition: r, origin: r.origin, destination: r.destination,
                    expectedShippingDate: new Date() + 1, shipmentType: ShipmentType.get('5'))
            shipment.save(flush: true, failOnError: true)
            o.requisition = r
            o.requisitionItem = ri
            o.picklistId = picklist.id
            o.taskIds = picklist.picklistItems*.id.sort()
            o.shipmentId = shipment.id
        }
        return o
    }

    /** A Pick Up order waiting for allocation (CREATED), stock in a storage and a display bin. */
    PickOrder pickUpOrder(int quantity = 8) {
        PickOrder o = new PickOrder()
        o.dc = regressionDc()
        o.destination = newDepot('PU-DEST')
        o.bin = newBin('PU-STOR', [ActivityCode.PICK_STOCK.id, ActivityCode.PUTAWAY_STOCK.id], BIN_TYPE, o.dc)
        o.packing = newBin('PU-DISP', [ActivityCode.PICK_STOCK.id, ActivityCode.PUTAWAY_STOCK.id, ActivityCode.DISPLAY_STOCK.id], BIN_TYPE, o.dc)
        o.product = newProduct('PU')
        String lotNumber = "LOT-${uid()}".toString()
        stock(o.product, [[o.bin, 10, lotNumber, new Date() + 365], [o.packing, 5, "${lotNumber}-D".toString(), new Date() + 200]], o.dc)
        Requisition.withNewTransaction {
            o.lot = InventoryItem.findByProductAndLotNumber(Product.get(o.product.id), lotNumber)
            Requisition r = new Requisition(
                    name: "Test pick-up ${uid()}".toString(), requestNumber: "PU${uid()}".toString(),
                    origin: Location.get(o.dc.id), destination: Location.get(o.destination.id),
                    requestedBy: Person.get('1'), dateRequested: new Date(), requestedDeliveryDate: new Date() + 1,
                    status: RequisitionStatus.CREATED, type: RequisitionType.DEFAULT, sourceType: null,
                    deliveryTypeCode: DeliveryTypeCode.PICK_UP, autoAllocationRequested: false)
            RequisitionItem ri = new RequisitionItem(product: Product.get(o.product.id), quantity: quantity,
                    quantityApproved: quantity, substitutable: false, orderIndex: 0)
            r.addToRequisitionItems(ri)
            r.save(flush: true, failOnError: true)
            o.requisition = r
            o.requisitionItem = ri
        }
        return o
    }

    void refreshAvailability(PickOrder o) {
        Location.withNewTransaction {
            productAvailabilityService.refreshProductsAvailability(o.dc.id, [o.product.id], true)
        }
    }

    Map task(PickOrder o, String taskId) {
        CallResult r = call('src/apis/picking.ts#getPickTaskByIdApi', [facilityId: o.dc.id, taskId: taskId])
        assert r.status == 200: r.toString()
        return r.data.data as Map
    }

    // ------------------------------------------------------------------ picklist flow (orders.ts)

    @Covers('src/apis/orders.ts#getOrders')
    void 'GET /stockMovements (outbound, PICKING) lists the order with the fields the order list reads'() {
        given:
        PickOrder o = pickOrder()
        call('src/apis/orders.ts#submitPackingLocation', [id: o.requisition.id], ['packingLocation.id': o.packing.id]).holds()

        when:
        CallResult r = call('src/apis/orders.ts#getOrders', ['global.location.id': o.dc.id])

        then:
        r.rows { it.id == o.requisition.id }.holds()
        Map row = (r.data.data as List<Map>).find { it.id == o.requisition.id }
        row
        row.packingLocation.id == o.packing.id
    }

    @Covers('src/apis/orders.ts#getPickList')
    void 'GET /picklists/{id} answers the picklist with the fields the picklist screen reads'() {
        given:
        PickOrder o = pickOrder()

        when:
        CallResult r = call('src/apis/orders.ts#getPickList', [id: o.picklistId])

        then:
        r.holds()
    }

    @Covers('src/apis/orders.ts#getPickListItem')
    void 'GET /picklistItems/{id} answers the picklist item (saga wired, never dispatched)'() {
        given:
        PickOrder o = pickOrder()

        when:
        CallResult r = call('src/apis/orders.ts#getPickListItem', [id: o.taskIds[0]])

        then:
        r.holds()
        r.data.data.id == o.taskIds[0]
    }

    @Covers('src/apis/orders.ts#getStockMovement')
    @PendingFeature(reason = 'APP/SERVER MISMATCH (dead path): src/apis/orders.ts getStockMovement (saga wired, never dispatched) sends direction and status but no origin/destination; StockMovementApiController.list answers 400 "Origin and destination cannot be the same" and ignores `status` (the filter is requisitionStatusCode)')
    void 'GET /stockMovements?direction&status answers the outbound list'() {
        given:
        pickOrder()

        when:
        CallResult r = call('src/apis/orders.ts#getStockMovement', [direction: 'OUTBOUND', status: 'PICKING'])

        then:
        r.holds()
    }

    @Covers('src/apis/orders.ts#submitPackingLocation')
    void 'POST /stockMovements/{id}/packingLocation with the flat packingLocation.id key schedules packing'() {
        given:
        PickOrder o = pickOrder()

        when:
        CallResult r = call('src/apis/orders.ts#submitPackingLocation', [id: o.requisition.id], ['packingLocation.id': o.packing.id])

        then:
        r.holds()
    }

    @Covers('src/apis/orders.ts#submitPickListItem')
    void 'POST /picklistItems/{id} with the picklist screen body records the picked quantity'() {
        given:
        PickOrder o = pickOrder()

        when:
        CallResult r = call('src/apis/orders.ts#submitPickListItem', [id: o.taskIds[0]],
                ['product.id': o.product.id, productCode: o.product.productCode, quantityPicked: 2, shortage: false])

        then:
        r.holds()
        PicklistItem.withNewTransaction { PicklistItem.get(o.taskIds[0]).quantityPicked } == 2
    }

    @Covers('src/apis/transfer.ts#getStockMovements')
    void 'GET /stockMovements/{id} answers the stock movement (saga wired, never dispatched)'() {
        given:
        PickOrder o = pickOrder()

        when:
        CallResult r = call('src/apis/transfer.ts#getStockMovements', [id: o.requisition.id])

        then:
        r.holds()
    }

    // ------------------------------------------------------------------ pick tasks (picking.ts)

    @Covers('src/apis/picking.ts#getPickTasksApi')
    void 'GET /facilities/{f}/pick-tasks (queue: unassigned, by delivery type) lists the open task'() {
        given:
        PickOrder o = pickOrder()

        when:
        CallResult r = call('src/apis/picking.ts#getPickTasksApi',
                [facilityId: o.dc.id, 'params.deliveryTypeCode': 'LOCAL_DELIVERY'])

        then:
        r.rows { it.requisitionId == o.requisition.id }.holds()
        (r.data.data as List<Map>)*.id.contains(o.taskIds[0])
    }

    @Covers('src/apis/picking.ts#getPickTasksApi')
    void 'GET /facilities/{f}/pick-tasks with ordersCount batches whole orders'() {
        given:
        PickOrder o = pickOrder()

        when:
        CallResult r = call('src/apis/picking.ts#getPickTasksApi',
                [facilityId: o.dc.id, 'params.deliveryTypeCode': 'LOCAL_DELIVERY', 'params.ordersCount': 50])

        then:
        r.rows { it.requisitionId == o.requisition.id }.holds()
    }

    @Covers('src/apis/picking.ts#getOpenPickTasksApi')
    void 'GET /facilities/{f}/pick-tasks?status=PENDING&status=PICKING lists the discrete order rows'() {
        given:
        PickOrder o = pickOrder()

        when:
        CallResult r = call('src/apis/picking.ts#getOpenPickTasksApi', [facilityId: o.dc.id, excludeAssignedRequisitions: true])

        then:
        r.rows { it.requisitionId == o.requisition.id }.holds()
        (r.data.data as List<Map>).find { it.id == o.taskIds[0] }?.requisitionId == o.requisition.id
    }

    @Covers('src/apis/picking.ts#getPickTaskCountsApi')
    void 'GET /facilities/{f}/pick-tasks/counts answers a count per delivery type'() {
        given:
        PickOrder o = pickOrder()

        when:
        CallResult r = call('src/apis/picking.ts#getPickTaskCountsApi', [facilityId: o.dc.id])

        then:
        r.holds()
        (r.data.data as List<Map>).find { it.deliveryTypeCode == 'LOCAL_DELIVERY' }.availableCount >= 1
    }

    @Covers('src/apis/picking.ts#getPickTaskCountsApi')
    @PendingFeature(reason = 'KNOWN MISMATCH: the app types the counts rows as {deliveryTypeCode, availableCount, totalCount} (src/types/picking.ts); PickTaskApiController.counts sends no totalCount (the app does not read it today)')
    void 'GET /facilities/{f}/pick-tasks/counts sends a totalCount per delivery type'() {
        given:
        PickOrder o = pickOrder()

        when:
        CallResult r = call('src/apis/picking.ts#getPickTaskCountsApi', [facilityId: o.dc.id])

        then:
        (r.data.data as List<Map>).every { it.totalCount instanceof Number }
    }

    @Covers('src/apis/picking.ts#getPickTasksByRequisitionApi')
    void 'GET /facilities/{f}/pick-tasks?requisitionId&status lists the order tasks'() {
        given:
        PickOrder o = pickOrder(5, 2)

        when:
        CallResult r = call('src/apis/picking.ts#getPickTasksByRequisitionApi',
                [facilityId: o.dc.id, requisitionId: o.requisition.id, 'statuses[]': ['PENDING', 'PICKING']])

        then:
        r.holds()
        (r.data.data as List<Map>)*.id.sort() == o.taskIds
    }

    @Covers('src/apis/picking.ts#getPickTaskByIdApi')
    void 'GET /facilities/{f}/pick-tasks/{id} answers the task the picking screen reads'() {
        given:
        PickOrder o = pickOrder()

        when:
        CallResult r = call('src/apis/picking.ts#getPickTaskByIdApi', [facilityId: o.dc.id, taskId: o.taskIds[0]])

        then:
        r.holds()
        r.data.data.stagingLocation.id
    }

    @Covers('src/apis/picking.ts#patchPickTaskApi')
    void 'PATCH pick-tasks/{id} start then pick moves the task into the tote'() {
        given:
        PickOrder o = pickOrder()

        when:
        CallResult start = call('src/apis/picking.ts#patchPickTaskApi', [facilityId: o.dc.id, taskId: o.taskIds[0]],
                [action: 'start', assigneeId: '1'])
        CallResult pick = call('src/apis/picking.ts#patchPickTaskApi', [facilityId: o.dc.id, taskId: o.taskIds[0]],
                [action: 'pick', outboundContainerId: o.tote.id, pickedById: '1'])
        Map after = task(o, o.taskIds[0])

        then:
        start.holds()
        pick.holds()
        after.status == 'PICKED'
        after.outboundContainer.id == o.tote.id
        after.assignee.firstName
    }

    @Covers('src/apis/picking.ts#patchPickTaskApi')
    void 'PATCH pick-tasks/{id} short-pick with a reason code name records the shortage'() {
        given:
        PickOrder o = pickOrder()
        call('src/apis/picking.ts#patchPickTaskApi', [facilityId: o.dc.id, taskId: o.taskIds[0]], [action: 'start', assigneeId: '1']).holds()

        when:
        CallResult r = call('src/apis/picking.ts#patchPickTaskApi', [facilityId: o.dc.id, taskId: o.taskIds[0]],
                [action: 'short-pick', outboundContainerId: o.tote.id, quantityPicked: 3, pickedById: '1', reasonCode: 'STOCKOUT'])
        Map after = task(o, o.taskIds[0])

        then:
        r.holds()
        after.reasonCode == 'STOCKOUT'
    }

    @Covers('src/apis/picking.ts#patchPickTaskApi')
    void 'PATCH pick-tasks/{id} short-pick of zero with no tote stages the started task'() {
        given:
        PickOrder o = pickOrder(5, 2)
        call('src/apis/picking.ts#patchPickTaskApi', [facilityId: o.dc.id, taskId: o.taskIds[0]], [action: 'start', assigneeId: '1']).holds()

        when:
        CallResult r = call('src/apis/picking.ts#patchPickTaskApi', [facilityId: o.dc.id, taskId: o.taskIds[0]],
                [action: 'short-pick', outboundContainerId: null, quantityPicked: 0, pickedById: '1', reasonCode: 'STOCKOUT'])

        then:
        r.holds()
    }

    @Covers('src/apis/picking.ts#getPickTasksByStatusAndContainerApi')
    void 'GET pick-tasks?outboundContainerId&status=PICKED lists what is in the tote'() {
        given:
        PickOrder o = pickOrder()
        call('src/apis/picking.ts#patchPickTaskApi', [facilityId: o.dc.id, taskId: o.taskIds[0]], [action: 'start', assigneeId: '1']).holds()
        call('src/apis/picking.ts#patchPickTaskApi', [facilityId: o.dc.id, taskId: o.taskIds[0]],
                [action: 'pick', outboundContainerId: o.tote.id, pickedById: '1']).holds()

        when:
        CallResult r = call('src/apis/picking.ts#getPickTasksByStatusAndContainerApi',
                [facilityId: o.dc.id, outboundContainerId: o.tote.id, status: 'PICKED'])

        then:
        r.holds()
        (r.data.data as List<Map>)*.id == [o.taskIds[0]]
    }

    @Covers('src/apis/picking.ts#dropPickTaskApi')
    void 'PATCH pick-tasks/containers/{tote} drop stages the picked tote'() {
        given:
        PickOrder o = pickOrder()
        call('src/apis/picking.ts#patchPickTaskApi', [facilityId: o.dc.id, taskId: o.taskIds[0]], [action: 'start', assigneeId: '1']).holds()
        call('src/apis/picking.ts#patchPickTaskApi', [facilityId: o.dc.id, taskId: o.taskIds[0]],
                [action: 'pick', outboundContainerId: o.tote.id, pickedById: '1']).holds()
        refreshAvailability(o)
        String stagingId = task(o, o.taskIds[0]).stagingLocation.id

        when:
        CallResult r = call('src/apis/picking.ts#dropPickTaskApi', [facilityId: o.dc.id, outboundContainerId: o.tote.id],
                [action: 'drop', stagingLocationId: stagingId, stagedById: '1'])

        then:
        r.holds()
        PicklistItem.withNewTransaction { PicklistItem.get(o.taskIds[0]).status?.toString() } == 'STAGED'
    }

    @Covers('src/apis/picking.ts#reallocatePickTaskApi')
    void 'POST pick-tasks/{id}/reallocate with the reallocate modal body moves the task to another bin'() {
        given:
        PickOrder o = pickOrder()
        Location other = newBin('PK-ALT', [ActivityCode.PICK_STOCK.id, ActivityCode.PUTAWAY_STOCK.id], BIN_TYPE, o.dc)
        stock(o.product, [[o.bin, 10, o.lot.lotNumber, o.lot.expirationDate], [other, 10, o.lot.lotNumber, o.lot.expirationDate]], o.dc)

        when:
        CallResult r = call('src/apis/picking.ts#reallocatePickTaskApi', [facilityId: o.dc.id, taskId: o.taskIds[0]],
                [picklistItems: [[inventoryItem: [id: o.lot.id], binLocation: [id: other.id], quantity: 5]]])

        then:
        r.holds()
        (r.data.data as List<Map>).any { it.location?.id == other.id }
    }

    // ------------------------------------------------------------------ item details + Pick Up allocation (picking.ts, pua.ts)

    @Covers('src/apis/picking.ts#getStockMovementItemDetailsApi')
    void 'GET /stockMovementItems/{id}/details?stepNumber=4 answers available stock and the allocation'() {
        given:
        PickOrder o = pickOrder()

        when:
        CallResult r = call('src/apis/picking.ts#getStockMovementItemDetailsApi', [requisitionItemId: o.requisitionItem.id])

        then:
        r.holds()
    }

    @Covers('src/apis/pua.ts#getOutboundOrders')
    void 'GET /stockMovements (Pick Up, CREATED/VERIFYING) lists the order waiting for allocation'() {
        given:
        PickOrder o = pickUpOrder()

        when:
        CallResult r = call('src/apis/pua.ts#getOutboundOrders', ['global.location.id': o.dc.id])

        then:
        r.rows { it.id == o.requisition.id }.holds()
        (r.data.data as List<Map>).find { it.id == o.requisition.id }?.lineItemCount == 1
    }

    @Covers('src/apis/pua.ts#allocate')
    void 'POST /outbound-orders/{id}/items/{itemId}/allocations AUTO with the app strategies allocates'() {
        given:
        PickOrder o = pickUpOrder()

        when:
        CallResult r = call('src/apis/pua.ts#allocate', [orderId: o.requisition.id, lineItemId: o.requisitionItem.id],
                [mode: 'AUTO', strategies: ['WAREHOUSE_FIRST']])
        CallResult display = call('src/apis/pua.ts#allocate', [orderId: o.requisition.id, lineItemId: o.requisitionItem.id],
                [mode: 'AUTO', strategies: ['DISPLAY_FIRST']])

        then:
        r.holds()
        display.holds()
    }

    @Covers('src/apis/pua.ts#allocate')
    void 'POST .../allocations MANUAL with the app allocation rows allocates exactly those'() {
        given:
        PickOrder o = pickUpOrder()

        when:
        CallResult r = call('src/apis/pua.ts#allocate', [orderId: o.requisition.id, lineItemId: o.requisitionItem.id],
                [mode: 'MANUAL', allocations: [[inventoryItemId: o.lot.id, binLocationId: o.bin.id, quantity: 8]]])
        CallResult details = call('src/apis/pua.ts#getOutboundOrderDetails', [id: o.requisition.id])

        then:
        r.holds()
        details.data.data.lineItems[0].allocations*.quantity == [8]
    }

    @Covers('src/apis/pua.ts#getOutboundOrderDetails')
    void 'GET /outbound-orders/{id} answers lines, available stock and allocations'() {
        given:
        PickOrder o = pickUpOrder()
        call('src/apis/pua.ts#allocate', [orderId: o.requisition.id, lineItemId: o.requisitionItem.id],
                [mode: 'MANUAL', allocations: [[inventoryItemId: o.lot.id, binLocationId: o.bin.id, quantity: 4]]]).holds()

        when:
        CallResult r = call('src/apis/pua.ts#getOutboundOrderDetails', [id: o.requisition.id])

        then:
        r.holds()
    }

    @Covers('src/apis/pua.ts#updateOrderStatus')
    void 'POST /stockMovements/{id}/status PICKING releases the allocated Pick Up order to picking'() {
        given:
        PickOrder o = pickUpOrder()
        call('src/apis/pua.ts#allocate', [orderId: o.requisition.id, lineItemId: o.requisitionItem.id],
                [mode: 'AUTO', strategies: ['WAREHOUSE_FIRST']]).holds()

        when:
        CallResult r = call('src/apis/pua.ts#updateOrderStatus', [orderId: o.requisition.id], [status: 'PICKING'])

        then:
        r.holds()
        Requisition.withNewTransaction { Requisition.get(o.requisition.id).status } == RequisitionStatus.PICKING
    }
}
