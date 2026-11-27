package org.pih.warehouse.api.regression.obaf.replenishment

import org.pih.warehouse.api.Putaway
import org.pih.warehouse.api.PutawayItem
import org.pih.warehouse.api.PutawayStatus
import org.pih.warehouse.api.regression.obaf.support.ObafTaskIntegrationSpec
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.product.Product

/**
 * Putaway of staged replenishment stock: the stock of a bin replenishment staging lane is offered as putaway
 * candidates, one per bin it was staged for, with that bin preset; putting it all away completes the order.
 *
 * Uses the picking site of the base spec: the replenishment picks from BIN through TOTE into STAGE.
 */
class ReplenishmentPutawayIntegrationSpec extends ObafTaskIntegrationSpec {

    /** Picks every task of the order in full into the tote, then drops the tote at the staging lane. */
    void pickAndStage(PickSite s, ReplenOrder o) {
        o.taskIds.each { String id ->
            assert replenPatch(id, [action: 'start']) == null
            assert replenPatch(id, [action: 'pick', outboundContainerId: s.tote]) == null
        }
        tx { replenishmentTaskService.drop(Location.get(s.tote), [action: 'drop', stagingLocationId: s.staging]) }
    }

    /** The putaway candidates of the depot (web list, or mobile list without open tasks) as plain values. */
    List<Map> candidates(PickSite s, boolean mobile = false) {
        onHand(s.dc, s.product) // forces the availability refresh the transfers only queue
        return tx {
            Location depot = Location.get(s.dc)
            List<PutawayItem> items = mobile ? putawayService.getPutawayCandidatesExcludingOpenTasks(depot) :
                    putawayService.getPutawayCandidates(depot)
            items.findAll { it.product?.id == s.product }.collect { PutawayItem item ->
                [from       : prefixOf(item.currentLocation?.name), to: prefixOf(item.putawayLocation?.name),
                 quantity   : item.quantity as Integer, preferredBin: prefixOf(item.preferredBin?.name)]
            }.sort { a, b -> (a.to ?: '') <=> (b.to ?: '') }
        } as List<Map>
    }

    /** Completes a web putaway of the candidate from the staging lane into the given bin. */
    void completePutaway(PickSite s, String toBin, int quantity) {
        tx {
            Location depot = Location.get(s.dc)
            PutawayItem item = putawayService.getPutawayCandidates(depot).find {
                it.product?.id == s.product && it.putawayLocation?.id == toBin
            }
            item.quantity = quantity
            item.putawayStatus = PutawayStatus.COMPLETED
            Putaway putaway = new Putaway(origin: depot, destination: depot, putawayDate: new Date(),
                    putawayStatus: PutawayStatus.COMPLETED, orderedBy: Person.get('1'), putawayAssignee: Person.get('1'))
            putaway.putawayItems.add(item)
            putawayService.completePutaway(putaway)
        }
    }

    void 'a staged task is a candidate from the lane into its destination bin, for the picked quantity'() {
        given:
        PickSite s = pickSite('RP-ONE')
        ReplenOrder o = replenOrder(s, [3])
        pickAndStage(s, o)

        expect:
        candidates(s) == [[from: 'STAGE', to: 'ALT', quantity: 3, preferredBin: null]]
        orderStatus(o.orderId) == 'APPROVED'
    }

    void 'the preferred bin is the location level inventory level of the destination bin'() {
        given:
        PickSite s = pickSite('RP-IL')
        tx {
            new InventoryLevel(product: Product.get(s.product), inventory: Location.get(s.dc).inventory,
                    internalLocation: Location.get(s.alt), minQuantity: 5, maxQuantity: 30).save(flush: true, failOnError: true)
        }
        ReplenOrder o = replenOrder(s, [3])
        pickAndStage(s, o)

        expect:
        candidates(s) == [[from: 'STAGE', to: 'ALT', quantity: 3, preferredBin: 'ALT']]
    }

    void 'tasks into the same bin make one candidate; tasks into different bins stay apart'() {
        given:
        PickSite s = pickSite('RP-GRP')
        String alt2 = bin(s.dc, 'ALT2')
        ReplenOrder o = replenOrder(s, [2, 3])
        ReplenOrder other = replenOrder(s, [4], alt2)
        pickAndStage(s, o)
        pickAndStage(s, other)

        expect:
        candidates(s) == [[from: 'STAGE', to: 'ALT', quantity: 5, preferredBin: null],
                          [from: 'STAGE', to: 'ALT2', quantity: 4, preferredBin: null]]
    }

    void 'putting away one bin leaves the other offered; putting away all of an order completes it'() {
        given:
        PickSite s = pickSite('RP-DONE')
        String alt2 = bin(s.dc, 'ALT2')
        ReplenOrder o = replenOrder(s, [2])
        ReplenOrder other = replenOrder(s, [4], alt2)
        pickAndStage(s, o)
        pickAndStage(s, other)

        when:
        completePutaway(s, s.alt, 2)

        then:
        candidates(s) == [[from: 'STAGE', to: 'ALT2', quantity: 4, preferredBin: null]]
        orderStatus(o.orderId) == 'COMPLETED'
        orderStatus(other.orderId) == 'APPROVED'

        when: 'part of the other is put away'
        completePutaway(s, alt2, 1)

        then:
        candidates(s) == [[from: 'STAGE', to: 'ALT2', quantity: 3, preferredBin: null]]
        orderStatus(other.orderId) == 'APPROVED'

        when:
        completePutaway(s, alt2, 3)

        then:
        candidates(s) == []
        orderStatus(other.orderId) == 'COMPLETED'
        onHand(s.dc, s.product) == [BIN: 14, ALT: 22, ALT2: 4]
    }

    void 'a mobile putaway task out of the lane completes the order'() {
        given:
        PickSite s = pickSite('RP-MOB')
        ReplenOrder o = replenOrder(s, [3])
        pickAndStage(s, o)
        Map po = putawayOrderOf(s.dc, s.product, s.lotId, [[3, s.staging, s.alt]])

        expect: 'only the open putaway task is offered, the ready candidate it covers is gone'
        candidates(s, true) == [[from: 'STAGE', to: 'ALT', quantity: 3, preferredBin: null]]

        when:
        Map r = putawayPatch(po.itemIds[0], [action: 'complete', destination: s.alt, isDirectPutaway: true])

        then:
        r.error == null
        orderStatus(o.orderId) == 'COMPLETED'
        candidates(s, true) == []
        onHand(s.dc, s.product) == [BIN: 17, ALT: 23]
    }

    void 'an open putaway task into one bin hides only that bin on mobile'() {
        given:
        PickSite s = pickSite('RP-OPEN')
        String alt2 = bin(s.dc, 'ALT2')
        ReplenOrder o = replenOrder(s, [2])
        ReplenOrder other = replenOrder(s, [4], alt2)
        pickAndStage(s, o)
        pickAndStage(s, other)
        putawayOrderOf(s.dc, s.product, s.lotId, [[4, s.staging, alt2]])

        expect: 'ALT2 is offered once, as the open task itself, and ALT is still a ready candidate'
        candidates(s, true) == [[from: 'STAGE', to: 'ALT', quantity: 2, preferredBin: null],
                                [from: 'STAGE', to: 'ALT2', quantity: 4, preferredBin: null]]
    }

    void 'lane stock no staged task accounts for is a candidate without a destination'() {
        given:
        PickSite s = pickSite('RP-LOOSE')
        ReplenOrder o = replenOrder(s, [3])
        pickAndStage(s, o)
        // 2 more recorded in the lane, which no staged task accounts for
        stock(s.dc, s.product, [[s.staging, 2, s.lot, SOON]])

        expect:
        onHand(s.dc, s.product).STAGE == 5
        candidates(s) == [[from: 'STAGE', to: null, quantity: 2, preferredBin: null],
                          [from: 'STAGE', to: 'ALT', quantity: 3, preferredBin: null]]
    }
}
