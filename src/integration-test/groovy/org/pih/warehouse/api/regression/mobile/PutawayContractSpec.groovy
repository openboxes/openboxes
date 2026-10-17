package org.pih.warehouse.api.regression.mobile

import spock.lang.PendingFeature

import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.order.Order
import org.pih.warehouse.product.Product

/**
 * Putaway: the putaway order flow (candidates, create, list, complete) and the
 * putaway-task flow (sortation: start, load into a cart; putaway: complete,
 * partial complete, cancel remaining; alternate destinations; reason codes).
 * Runs in Test Mobile DC. Each feature makes its own product, bins and order.
 */
class PutawayContractSpec extends MobileContractSpec {

    Location dc
    Location receiving
    Location storage
    Location cart
    Location lostAndFound
    Product p
    InventoryItem lot

    @Override
    String sessionLocationId() {
        return regressionDc().id
    }

    void setupData() {
        dc = regressionDc()
        receiving = newBin('PA-RECV', [ActivityCode.RECEIVE_STOCK.id], BIN_TYPE, dc)
        storage = newBin('PA-STOR', [ActivityCode.PUTAWAY_STOCK.id, ActivityCode.PICK_STOCK.id], BIN_TYPE, dc)
        cart = newBin('PA-CART', [ActivityCode.PUTAWAY_CART.id], BIN_TYPE, dc)
        if (!Location.withNewTransaction { Location.findAllByParentLocation(Location.get(dc.id)).any { it.supports(ActivityCode.LOST_AND_FOUND) } }) {
            lostAndFound = newBin('PA-LNF', [ActivityCode.LOST_AND_FOUND.id], BIN_TYPE, dc)
        }
        p = newProduct('PA')
        String lotNumber = "LOT-${uid()}".toString()
        stock(p, [[receiving, 10, lotNumber, new Date() + 365]], dc)
        lot = InventoryItem.withNewTransaction { InventoryItem.findByProductAndLotNumber(Product.get(p.id), lotNumber) }
    }

    /** The PutawayItem screen's body (src/screens/PutawayItem/index.tsx). */
    Map createBody(int qty = 10) {
        return [putawayNumber: '', putawayStatus: 'PENDING', putawayDate: '', putawayAssignee: '',
                origin: dc.id, destination: dc.id,
                putawayItems: [[putawayStatus: 'PENDING', product: p.id, inventoryItem: lot.id, putawayFacility: dc.id,
                                currentLocation: receiving.id, putawayLocation: storage.id, quantity: qty]],
                orderedBy: '', sortBy: null]
    }

    Map createPutaway(int qty = 10) {
        CallResult r = call('src/apis/putaway.ts#createPutawayOder', [:], createBody(qty))
        r.holds()
        return r.data.data as Map
    }

    String taskId(Map putaway) {
        return (putaway.putawayItems as List<Map>)[0].id
    }

    Map patch(String id, Map body) {
        CallResult r = call('src/apis/putaway.ts#patchPutawayTask', [facilityId: dc.id, putawayItemId: id], body)
        r.holds()
        return r.data.data as Map
    }

    @Covers('src/apis/others.ts#getReasonCodesByActivity')
    void 'GET /reason-codes answers the putaway discrepancy and picking shortage codes'() {
        when:
        CallResult putaway = call('src/apis/others.ts#getReasonCodesByActivity', [activityCode: 'PUTAWAY_DISCREPANCY'])
        CallResult picking = call('src/apis/others.ts#getReasonCodesByActivity', [activityCode: 'PICKING_SHORTAGE'])

        then:
        putaway.holds()
        picking.holds()
    }

    @Covers('src/apis/putaway.ts#getCandidates')
    void 'GET /locations/{id}/putawayCandidates lists received stock ready to put away'() {
        when:
        CallResult r = call('src/apis/putaway.ts#getCandidates', [locationId: dc.id])

        then:
        r.rows { it['product.id'] == p.id }.holds()
        (r.data.data as List<Map>).any { it['product.id'] == p.id && it.putawayStatus == 'READY' && it.quantity == 10 }
    }

    @Covers('src/apis/putaway.ts#createPutawayOder')
    void 'POST /putaways with the putaway item screen body creates a PENDING putaway'() {
        when:
        CallResult r = call('src/apis/putaway.ts#createPutawayOder', [:], createBody())

        then:
        r.holds()
    }

    @Covers('src/apis/putaway.ts#fetchPutAwayFromOrder')
    void 'GET /mobile/putaways lists the pending putaway'() {
        given:
        Map created = createPutaway()

        when:
        CallResult r = call('src/apis/putaway.ts#fetchPutAwayFromOrder', [:])

        then:
        r.rows { it.id == created.id }.holds()
        (r.data.data as List<Map>)*.id.contains(created.id)
    }

    @Covers('src/apis/putaway.ts#fetchPutAwayFromOrder')
    @PendingFeature(reason = 'KNOWN MISMATCH: src/apis/putaway.ts fetchPutAwayFromOrder searches with ?q=<scanned order or product>; PutawayApiController.mobilePutaways ignores q and answers every PENDING putaway of the location')
    void 'GET /mobile/putaways?q= narrows the list to the searched putaway'() {
        given:
        Map wanted = createPutaway(5)
        Map other = createPutaway(5)

        when:
        CallResult r = call('src/apis/putaway.ts#fetchPutAwayFromOrder', [q: wanted.putawayNumber])

        then:
        r.holds()
        (r.data.data as List<Map>)*.id.contains(wanted.id)
        !(r.data.data as List<Map>)*.id.contains(other.id)
    }

    @Covers('src/apis/putaway.ts#submitPutawayItem')
    void 'POST /putaways/{itemId} with the completed putaway body completes the putaway'() {
        given:
        Map created = createPutaway()
        Map item = (created.putawayItems as List<Map>)[0]
        Map body = [id: created.id, putawayNumber: created.putawayNumber, putawayStatus: 'COMPLETED', putawayDate: '',
                    putawayAssignee: '', origin: created['origin.id'], destination: created['destination.id'],
                    putawayItems: [[id: item.id, putawayStatus: 'COMPLETED', currentFacility: item['currentFacility.id'],
                                    currentLocation: item['currentLocation.id'], product: item['product.id'],
                                    inventoryItem: item['inventoryItem.id'], putawayFacility: item['putawayFacility.id'],
                                    putawayLocation: item['putawayLocation.id'], quantity: item.quantity,
                                    scannedPutawayLocation: storage.locationNumber]],
                    orderedBy: '', sortBy: '']

        when:
        CallResult r = call('src/apis/putaway.ts#submitPutawayItem', [id: item.id], body)

        then:
        r.holds()
        Order.withNewTransaction { Order.get(created.id).status?.name() } == 'COMPLETED'
    }

    @Covers('src/apis/putaway.ts#getPutawayTasks')
    void 'GET putaway-tasks?statusCategory=OPEN&product= lists the open task for the scanned product'() {
        given:
        Map created = createPutaway()

        when:
        CallResult r = call('src/apis/putaway.ts#getPutawayTasks', [facilityId: dc.id, productId: p.id])

        then:
        r.rows { it.id == taskId(created) }.holds()
        (r.data.data as List<Map>)*.id.contains(taskId(created))
    }

    @Covers(['src/apis/putaway.ts#patchPutawayTask', 'src/apis/putaway.ts#getPutawayDetails'])
    void 'sortation: start, load into a cart, list the cart, complete to the destination'() {
        given:
        String id = taskId(createPutaway())

        when:
        Map started = patch(id, [action: 'start'])
        Map loaded = patch(id, [action: 'load', quantity: 10, container: cart.locationNumber, override: true])
        CallResult inCart = call('src/apis/putaway.ts#getPutawayDetails', [facilityId: dc.id, containerId: cart.id])
        Map task = (inCart.data.data as List<Map>).find { it.inventoryItem?.product?.id == p.id }
        Map done = patch(task.id as String, [action: 'complete', destination: storage.id, isCancelRemaining: false, reasonCode: null])

        then:
        started.status == 'STARTED'
        inCart.rows { it.inventoryItem?.product?.id == p.id }.holds()
        task.status == 'IN_PROGRESS'
        done.status == 'COMPLETED'
    }

    @Covers('src/apis/putaway.ts#patchPutawayTask')
    void 'direct putaway: partial complete, then complete the rest with cancel remaining'() {
        given:
        String id = taskId(createPutaway())
        patch(id, [action: 'start'])

        when:
        Map remaining = patch(id, [action: 'partialComplete', quantity: 6, destination: storage.id, reasonCode: 'DAMAGED', isDirectPutaway: true])
        Map done = patch(remaining.id as String, [action: 'complete', destination: remaining.destination?.id, isCancelRemaining: true,
                                                  reasonCode: 'DAMAGED', isDirectPutaway: true])

        then:
        remaining.id
        done.status == 'COMPLETED'
    }

    @Covers('src/apis/locations.ts#getAlternativeDestinations')
    void 'GET putaway-tasks/{id}/alternate-destinations answers the product bins'() {
        given:
        String id = taskId(createPutaway())
        Location other = newBin('PA-ALT', [ActivityCode.PUTAWAY_STOCK.id, ActivityCode.PICK_STOCK.id], BIN_TYPE, dc)
        InventoryLevel.withNewTransaction {
            new InventoryLevel(product: Product.get(p.id), inventory: Location.get(dc.id).inventory, internalLocation: Location.get(other.id))
                    .save(flush: true, failOnError: true)
        }

        when:
        CallResult r = call('src/apis/locations.ts#getAlternativeDestinations', [facilityId: dc.id, taskId: id])

        then:
        r.holds()
        (r.data.data as List<Map>)*.id.contains(other.id)
    }
}
