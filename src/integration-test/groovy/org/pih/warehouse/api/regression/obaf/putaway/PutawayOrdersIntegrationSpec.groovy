package org.pih.warehouse.api.regression.obaf.putaway

import org.pih.warehouse.api.Putaway
import org.pih.warehouse.api.PutawayItem
import org.pih.warehouse.api.regression.obaf.support.ObafTaskIntegrationSpec
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderType
import org.pih.warehouse.product.Product

/**
 * PutawayService (the putaway ORDER side behind the tasks): where a putaway location comes from
 * (no slotting, STATIC_SLOTTING, DYNAMIC_SLOTTING with a zone or a bin, PUTAWAY_STRATEGY_SINGLE_LPN
 * occupancy), the putaway candidates of a facility, putaway generation, and the web putaway
 * rollback / rollback-and-delete actions (PutawayController.rollback / rollbackAndDelete).
 */
class PutawayOrdersIntegrationSpec extends ObafTaskIntegrationSpec {

    String nextLocation(String dc, String productId) {
        return tx { prefixOf(putawayService.getNextAvailablePutawayLocation(Location.get(dc), Product.get(productId))?.name) } as String
    }

    /** A depot whose only bins are a receiving bin (no PUTAWAY_STOCK) and the given putaway bins. */
    String slottingDepot(String prefix, Collection<ActivityCode> extra = []) {
        return depot(prefix, extra)
    }

    String recvBin(String dc) {
        return bin(dc, 'RECV', [ActivityCode.RECEIVE_STOCK], false)
    }

    String zone(String dc, String name) {
        return tx {
            new Location(name: "${name}-${uid()}".toString(), locationType: LocationType.findByLocationTypeCode(LocationTypeCode.ZONE),
                    parentLocation: Location.get(dc), active: true).save(flush: true, failOnError: true).id
        } as String
    }

    void inZone(String binId, String zoneId) {
        tx { Location b = Location.get(binId); b.zone = Location.get(zoneId); b.save(flush: true, failOnError: true) }
    }

    String preferredLocation(String dc, String productId, String locationId) {
        return preferredBin(dc, productId, locationId)
    }

    // ------------------------------------------------------------------ next available putaway location

    void 'no slotting: the first putaway bin by sort order then name, whatever the product preferred bin'() {
        given:
        String dc = slottingDepot('PO-NS')
        recvBin(dc)
        bin(dc, 'B-BIN')
        String a = bin(dc, 'A-BIN')
        String c = bin(dc, 'C-BIN')
        bin(dc, 'NOPUT', [ActivityCode.PICK_STOCK], false)
        String p = product('PO-NS')
        preferredLocation(dc, p, c)

        expect:
        nextLocation(dc, p) == 'A-BIN'
    }

    void 'STATIC_SLOTTING: the preferred bin, or nothing when the product has none'() {
        given:
        String dc = slottingDepot('PO-SS', [ActivityCode.STATIC_SLOTTING])
        recvBin(dc)
        bin(dc, 'A-BIN')
        String slot = bin(dc, 'SLOT')
        String slotted = product('PO-SS')
        String unslotted = product('PO-SSU')
        preferredLocation(dc, slotted, slot)

        expect:
        nextLocation(dc, slotted) == 'SLOT'
        nextLocation(dc, unslotted) == null
    }

    void 'DYNAMIC_SLOTTING: a preferred ZONE gives the first free bin of the zone; a preferred bin is used as is; otherwise any free bin'() {
        given:
        String dc = slottingDepot('PO-DS', [ActivityCode.DYNAMIC_SLOTTING])
        recvBin(dc)
        bin(dc, 'A-BIN')
        String z = zone(dc, 'ZONE')
        String z2 = bin(dc, 'Z2')
        String z1 = bin(dc, 'Z1')
        inZone(z1, z)
        inZone(z2, z)
        String slot = bin(dc, 'SLOT')
        String byZone = product('PO-DSZ')
        String byBin = product('PO-DSB')
        String free = product('PO-DSF')
        preferredLocation(dc, byZone, z)
        preferredLocation(dc, byBin, slot)

        expect:
        nextLocation(dc, byZone) == 'Z1'
        nextLocation(dc, byBin) == 'SLOT'
        nextLocation(dc, free) == 'A-BIN'
    }

    void 'PUTAWAY_STRATEGY_SINGLE_LPN bins holding stock, or already the target of a pending putaway, are not offered'() {
        given:
        String dc = slottingDepot('PO-LPN')
        String recv = bin(dc, 'RECV', [ActivityCode.RECEIVE_STOCK], false)
        String a = bin(dc, 'A-BIN', [ActivityCode.PUTAWAY_STRATEGY_SINGLE_LPN])
        String b = bin(dc, 'B-BIN', [ActivityCode.PUTAWAY_STRATEGY_SINGLE_LPN])
        String c = bin(dc, 'C-BIN', [ActivityCode.PUTAWAY_STRATEGY_SINGLE_LPN])
        String d = bin(dc, 'D-BIN')
        String p = product('PO-LPN')
        stock(dc, p, [[recv, 4, 'L', SOON], [a, 1, 'L', SOON]])
        putawayOrder(dc, p, 'L', 4, recv, b)

        expect: 'A holds stock, B is the target of a pending putaway: C is next'
        nextLocation(dc, p) == 'C-BIN'

        when: 'C fills up too'
        // A record stock dates its baseline a second back and the database keeps whole (rounded) seconds: a second
        // record stock of the product within about two seconds can land on the first one's second and is refused
        Thread.sleep(2100)
        stock(dc, p, [[recv, 4, 'L', SOON], [a, 1, 'L', SOON], [c, 2, 'L', SOON]])

        then: 'a bin without the single-LPN strategy is offered even when it holds stock'
        nextLocation(dc, p) == 'D-BIN'
    }

    // ------------------------------------------------------------------ candidates and generation

    List<List> candidates(String dc, boolean mobile = false) {
        return tx {
            List<PutawayItem> items = mobile ? putawayService.getPutawayCandidatesExcludingOpenTasks(Location.get(dc)) :
                    putawayService.getPutawayCandidates(Location.get(dc))
            items.collect { [prefixOf(it.currentLocation?.name), it.quantity as Integer, it.id ? 'ORDER' : 'READY'] }.sort { it[2] }
        } as List<List>
    }

    void 'putaway candidates: stock in receiving bins; a bin with a pending putaway is shown as that putaway only, the mobile list also offers its remainder'() {
        given:
        String dc = depot('PO-CA')
        String recv = bin(dc, 'RECV', [ActivityCode.RECEIVE_STOCK], false)
        String stor = bin(dc, 'STOR')
        String p = product('PO-CA')
        stock(dc, p, [[recv, 10, 'L', SOON], [stor, 3, 'L', SOON]])

        expect:
        candidates(dc) == [['RECV', 10, 'READY']]

        when: '4 of the 10 get a putaway order'
        putawayOrder(dc, p, 'L', 4, recv, stor)

        then:
        candidates(dc) == [['RECV', 4, 'ORDER']]
        candidates(dc, true) == [['RECV', 4, 'ORDER'], ['RECV', 6, 'READY']]
    }

    void 'generatePutaways: one PENDING putaway order per candidate without one, to its putaway location, ordered by the assignee'() {
        given:
        String dc = depot('PO-GEN', [ActivityCode.STATIC_SLOTTING])
        String recv = bin(dc, 'RECV', [ActivityCode.RECEIVE_STOCK], false)
        String slotA = bin(dc, 'SLOTA')
        String slotB = bin(dc, 'SLOTB')
        String pa = product('PO-GA')
        String pb = product('PO-GB')
        stock(dc, pa, [[recv, 5, 'A', SOON]])
        stock(dc, pb, [[recv, 2, 'B', SOON]])
        preferredLocation(dc, pa, slotA)
        preferredLocation(dc, pb, slotB)
        putawayOrder(dc, pb, 'B', 2, recv, slotB)

        when: 'run with a web request bound (the job runs it without one, see ObafJobsIntegrationSpec)'
        withWebRequest { tx { putawayService.generatePutaways(Location.get(dc), User.findByUsername('admin')) } }
        List<List> orders = tx {
            Order.findAllByOriginAndOrderType(Location.get(dc), OrderType.findByCode(Constants.PUTAWAY_ORDER)).collectMany { Order o ->
                o.orderItems.collect { OrderItem i -> [i.product.id == pa ? 'A' : 'B', prefixOf(i.destinationBinLocation?.name), i.quantity as Integer, o.status.name(), o.orderedBy?.username] }
            }.sort { it[0] }
        } as List<List>

        then:
        // B already had its (fixture) putaway: no second order for it
        orders == [['A', 'SLOTA', 5, 'PENDING', 'admin'], ['B', 'SLOTB', 2, 'PENDING', 'admin']]
    }

    // ------------------------------------------------------------------ rollback (web putaway actions)

    void 'rollbackPutaway of a completed putaway: transfers undone, split items removed, items and order PENDING'() {
        given:
        PutawaySite s = putawaySite('PO-RB')
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[10, s.recv, s.stor1]])
        putawayPatch(po.itemIds[0], [action: 'complete', destination: s.stor1, isDirectPutaway: true])
        assert orderStatus(po.orderId) == 'COMPLETED'

        when:
        tx { putawayService.rollbackPutaway(putawayService.getPutaway(po.orderId)) }

        then:
        orderStatus(po.orderId) == 'PENDING'
        putawayTask(po.itemIds[0]).status == 'PENDING'
        onHand(s.dc, s.product) == [RECV: 10]
    }

    void 'rollbackPutaway of a pending putaway changes nothing'() {
        given:
        PutawaySite s = putawaySite('PO-RBN')
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[10, s.recv, s.stor1]])

        when:
        tx { putawayService.rollbackPutaway(putawayService.getPutaway(po.orderId)) }

        then:
        orderStatus(po.orderId) == 'PENDING'
        onHand(s.dc, s.product) == [RECV: 10]
    }

    void 'rollbackAndDelete of a completed putaway: the stock goes back and the order is gone'() {
        given:
        PutawaySite s = putawaySite('PO-RD')
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[10, s.recv, s.stor1]])
        putawayPatch(po.itemIds[0], [action: 'complete', destination: s.stor1, isDirectPutaway: true])

        when:
        tx { putawayService.rollbackAndDelete(putawayService.getPutaway(po.orderId)) }

        then:
        tx { Order.get(po.orderId) } == null
        onHand(s.dc, s.product) == [RECV: 10]
    }

    void 'rollbackAndDelete after a partial complete returns the completed part too'() {
        given:
        PutawaySite s = putawaySite('PO-RDP')
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[10, s.recv, s.stor1]])
        putawayPatch(po.itemIds[0], [action: 'partialComplete', quantity: 4, destination: s.stor2, isDirectPutaway: true])
        assert onHand(s.dc, s.product) == [RECV: 6, STOR2: 4]
        String statusBefore = orderStatus(po.orderId)

        when:
        Throwable e = null
        try {
            tx { putawayService.rollbackAndDelete(putawayService.getPutaway(po.orderId)) }
        } catch (Throwable t) {
            e = t
        }

        then:
        statusBefore == 'PENDING'
        e == null
        tx { Order.get(po.orderId) } == null
        onHand(s.dc, s.product) == [RECV: 10]
    }

    void 'rollback of an unknown putaway is refused'() {
        when:
        tx { putawayService.rollbackPutaway(new Putaway(id: 'no-such-putaway')) }

        then:
        thrown(org.hibernate.ObjectNotFoundException)
    }

    // ------------------------------------------------------------------ reassignment job effect on tasks

    void 'putaway location reassignment re-routes the open task off the UNDEFINED location, leaving a completed one alone'() {
        given:
        PutawaySite s = putawaySite('PO-RA', 10)
        String undefined = bin(s.dc, 'UNDEF', [ActivityCode.UNDEFINED_LOCATION])
        Map open = putawayOrderOf(s.dc, s.product, s.lotId, [[6, s.recv, undefined]])
        Map done = putawayOrderOf(s.dc, s.product, s.lotId, [[4, s.recv, undefined]])
        putawayPatch(done.itemIds[0], [action: 'complete', destination: undefined, isDirectPutaway: true])
        String level = preferredBin(s.dc, s.product, s.stor2)

        when:
        org.pih.warehouse.api.regression.obaf.support.QuartzRunner.fire(scheduler(), org.pih.warehouse.jobs.PutawayLocationReassignmentJob, [inventoryLevelId: level])

        then:
        putawayTask(open.itemIds[0]).destination == 'STOR2'
        putawayTask(done.itemIds[0]).destination == 'UNDEF'
    }
}
