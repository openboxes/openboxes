package org.pih.warehouse.api.regression.obaf.allocation

import org.springframework.beans.factory.annotation.Autowired
import spock.lang.PendingFeature

import org.pih.warehouse.allocation.AllocationDetailsDto
import org.pih.warehouse.allocation.AllocationDto
import org.pih.warehouse.allocation.AllocationMode
import org.pih.warehouse.allocation.AllocationService
import org.pih.warehouse.allocation.AllocationSourceStrategy
import org.pih.warehouse.allocation.AllocationStatus
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.api.regression.obaf.support.ObafIntegrationSpec
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Comment
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.history.EventLogCode
import org.pih.warehouse.inventory.CycleCountRequest
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionStatus

/**
 * AllocationService against a real database: product_availability, the picklist it writes,
 * InventoryLevel preferred bins, availability given back by deallocation, the failure audit
 * trail (REQUIRES_NEW comment + event log) and the negative-inventory fallback.
 *
 * Each feature uses its own depot. Depots here do NOT require mobile picking, so an AUTO
 * allocation writes picks with quantityPicked = quantity (pick == allocate), except where the
 * feature adds REQUIRE_MOBILE_PICKING.
 */
class AllocationEngineIntegrationSpec extends ObafIntegrationSpec {

    @Autowired
    AllocationService allocationService

    static final Date SOON = new Date() + 30
    static final Date LATER = new Date() + 200
    static final Date LATEST = new Date() + 400

    List autoAllocate(String requisitionId, List<AllocationSourceStrategy> strategies = []) {
        return tx { allocationService.allocate(Requisition.get(requisitionId), AllocationMode.AUTO, strategies) } as List
    }

    void 'FEFO across storage bins: earliest lot first, split over bins, display last (STORAGE_FIRST)'() {
        given:
        String dc = depot('AE-FEFO')
        String stor = bin(dc, 'STOR')
        String stor2 = bin(dc, 'STOR2')
        String disp = bin(dc, 'DISP', [ActivityCode.DISPLAY_STOCK])
        String p = product('FEFO')
        stock(dc, p, [[stor, 5, 'LATE', LATER], [stor2, 5, 'EARLY', SOON], [disp, 50, 'DISP', new Date() + 2]])
        String req = requisition(dc, [[productId: p, quantity: 7]])

        when:
        autoAllocate(req)

        then:
        picks(req) == [['STOR', 'LATE', 2, 2], ['STOR2', 'EARLY', 5, 5]]
    }

    void 'a real InventoryLevel preferred bin is drained before an earlier lot in another bin'() {
        given:
        String dc = depot('AE-PREF')
        String preferred = bin(dc, 'PREF')
        String other = bin(dc, 'OTHER')
        String p = product('PREF')
        stock(dc, p, [[preferred, 3, 'P-LATE', LATEST], [other, 10, 'O-EARLY', SOON]])
        tx {
            new InventoryLevel(product: Product.get(p), inventory: Location.get(dc).inventory,
                    preferredBinLocation: Location.get(preferred)).save(flush: true, failOnError: true)
        }
        String req = requisition(dc, [[productId: p, quantity: 4]])

        when:
        autoAllocate(req)

        then:
        picks(req) == [['OTHER', 'O-EARLY', 1, 1], ['PREF', 'P-LATE', 3, 3]]
    }

    void 'the order allocation source strategy (DISPLAY_FIRST) is honoured'() {
        given:
        String dc = depot('AE-DF')
        String stor = bin(dc, 'STOR')
        String disp = bin(dc, 'DISP', [ActivityCode.DISPLAY_STOCK])
        String p = product('DF')
        stock(dc, p, [[stor, 10, 'S', SOON], [disp, 10, 'D', LATER]])
        String req = requisition(dc, [[productId: p, quantity: 3]], [allocationSourceStrategy: AllocationSourceStrategy.DISPLAY_FIRST])

        when:
        autoAllocate(req)

        then:
        picks(req) == [['DISP', 'D', 3, 3]]
    }

    void 'held and receiving-sortation bins are never allocated'() {
        given:
        String dc = depot('AE-HOLD')
        String held = bin(dc, 'HELD', [ActivityCode.HOLD_STOCK])
        String sort = bin(dc, 'SORT', [ActivityCode.INBOUND_SORTATION])
        String ok = bin(dc, 'OK')
        String p = product('HOLD')
        stock(dc, p, [[held, 10, 'H', SOON], [sort, 10, 'X', SOON], [ok, 2, 'OK', LATEST]])
        String req = requisition(dc, [[productId: p, quantity: 2]])

        when:
        autoAllocate(req)

        then:
        picks(req) == [['OK', 'OK', 2, 2]]
    }

    void 'multi-line order: each line allocated from its own product, requisition untouched otherwise'() {
        given:
        String dc = depot('AE-ML')
        String b1 = bin(dc, 'B1')
        String pa = product('MLA')
        String pb = product('MLB')
        stock(dc, pa, [[b1, 4, 'A', SOON]])
        stock(dc, pb, [[b1, 6, 'B', SOON]])
        String req = requisition(dc, [[productId: pa, quantity: 4], [productId: pb, quantity: 5]])

        when:
        autoAllocate(req)

        then:
        picks(req) == [['B1', 'A', 4, 4], ['B1', 'B', 5, 5]]
        status(req) == RequisitionStatus.CREATED
    }

    void 'insufficient stock: nothing is written, and the failure survives the rollback as comment + ERROR_OCCURRED event log (one attempt)'() {
        given:
        String dc = depot('AE-SHORT')
        String b1 = bin(dc, 'B1')
        String ok = product('OKP')
        String scarce = product('SCARCE')
        stock(dc, ok, [[b1, 10, 'OK', SOON]])
        stock(dc, scarce, [[b1, 1, 'SC', SOON]])
        String req = requisition(dc, [[productId: ok, quantity: 2], [productId: scarce, quantity: 5]])

        when:
        autoAllocate(req)

        then:
        IllegalArgumentException e = thrown()
        e.message.contains('Required quantity: 5, Available quantity: 1')
        picks(req) == []
        tx {
            Requisition r = Requisition.get(req)
            assert r.comments*.comment.any { it.startsWith(Constants.ALLOCATION_FAILED) }
            assert r.eventLogs.findAll { it.eventLogCode == EventLogCode.ERROR_OCCURRED }*.message.every { it.startsWith(Constants.ALLOCATION_FAILED) }
            assert r.allocationAttemptCount == 1
            true
        }
    }

    void 'allocation reduces availability; deallocation gives it back and returns the order to CREATED'() {
        given:
        String dc = depot('AE-DEALLOC', [ActivityCode.REQUIRE_MOBILE_PICKING])
        String b1 = bin(dc, 'B1')
        String p = product('DEAL')
        stock(dc, p, [[b1, 10, 'L', SOON]])
        String req = requisition(dc, [[productId: p, quantity: 4]])

        when:
        autoAllocate(req)
        tx { Requisition r = Requisition.get(req); r.status = RequisitionStatus.PICKING; r.save(flush: true) }

        then: 'mobile picking: allocated, not yet picked'
        picks(req) == [['B1', 'L', 4, 0]]
        availability(dc, p)[['B1', 'L']] == [10, 6]

        when:
        Boolean result = tx { allocationService.deallocate(Requisition.get(req)) }

        then:
        result
        picks(req) == []
        availability(dc, p)[['B1', 'L']] == [10, 10]
        status(req) == RequisitionStatus.CREATED
        tx { Requisition.get(req).requisitionItems*.autoAllocated } == [null]
    }

    void 'allocating twice is idempotent: same picks, availability reduced once'() {
        given:
        String dc = depot('AE-IDEM', [ActivityCode.REQUIRE_MOBILE_PICKING])
        String b1 = bin(dc, 'B1')
        String b2 = bin(dc, 'B2')
        String p = product('IDEM')
        stock(dc, p, [[b1, 3, 'E', SOON], [b2, 10, 'L', LATER]])
        String req = requisition(dc, [[productId: p, quantity: 5]])

        when:
        autoAllocate(req)
        List first = picks(req)
        autoAllocate(req)

        then:
        first == [['B1', 'E', 3, 0], ['B2', 'L', 2, 0]]
        picks(req) == first
        availability(dc, p)[['B1', 'E']] == [3, 0]
        availability(dc, p)[['B2', 'L']] == [10, 8]
    }

    @PendingFeature(reason = 'AllocationService.groovy:237-251 allocate(Requisition) suggests every line from the same stock snapshot before writing any, so two lines of one product are both given the same units (over-allocation)')
    void 'two lines of the same product never share units: 5 on hand cannot be allocated as 4 + 4'() {
        given:
        String dc = depot('AE-DUP', [ActivityCode.REQUIRE_MOBILE_PICKING])
        String b1 = bin(dc, 'B1')
        String p = product('DUP')
        stock(dc, p, [[b1, 5, 'L', SOON]])
        String req = requisition(dc, [[productId: p, quantity: 4], [productId: p, quantity: 4]])

        when:
        try {
            autoAllocate(req)
        } catch (IllegalArgumentException refused) {
            // refusing is a correct outcome
        }

        then:
        (picks(req).sum { it[2] } ?: 0) <= 5
        availability(dc, p)[['B1', 'L']][1] >= 0
    }

    // ------------------------------------------------------------------ allocate by line id (the OutboundOrder API path)

    void 'AUTO by line id allocates the line and reports ALLOCATED'() {
        given:
        String dc = depot('AE-LINE')
        String b1 = bin(dc, 'B1')
        String p = product('LINE')
        stock(dc, p, [[b1, 10, 'L', SOON]])
        String req = requisition(dc, [[productId: p, quantity: 6]])
        String line = itemIds(req)[0]

        when:
        AllocationDetailsDto dto = tx { allocationService.allocate(line, AllocationMode.AUTO, [], []) }

        then:
        dto.status == AllocationStatus.ALLOCATED
        dto.quantityRequired == 6
        dto.quantityAllocated == 6
        dto.quantityRemaining == 0
        dto.allocations*.quantity == [6]
        picks(req) == [['B1', 'L', 6, 6]]
    }

    void 'MANUAL by line id creates picks, then updates them by pick id or by lot+bin, reporting the allocation status'() {
        given:
        String dc = depot('AE-MAN')
        String b1 = bin(dc, 'B1')
        String b2 = bin(dc, 'B2')
        String p = product('MAN')
        stock(dc, p, [[b1, 10, 'L1', SOON], [b2, 10, 'L2', LATER]])
        String l1 = lotId(p, 'L1')
        String l2 = lotId(p, 'L2')
        String req = requisition(dc, [[productId: p, quantity: 8]])
        String line = itemIds(req)[0]

        when: 'two manual picks'
        AllocationDetailsDto first = tx {
            allocationService.allocate(line, AllocationMode.MANUAL, [
                    new AllocationDto(inventoryItemId: l1, binLocationId: b1, quantity: 3),
                    new AllocationDto(inventoryItemId: l2, binLocationId: b2, quantity: 2)])
        }

        then:
        first.status == AllocationStatus.PARTIALLY_ALLOCATED
        first.quantityAllocated == 5
        first.quantityRemaining == 3
        picks(req) == [['B1', 'L1', 3, 0], ['B2', 'L2', 2, 0]]

        when: 'the first updated by its id, the second by lot + bin'
        String firstPickId = first.allocations.find { it.binLocationId == b1 }.id
        AllocationDetailsDto second = tx {
            allocationService.allocate(line, AllocationMode.MANUAL, [
                    new AllocationDto(id: firstPickId, inventoryItemId: l1, binLocationId: b1, quantity: 6),
                    new AllocationDto(inventoryItemId: l2, binLocationId: b2, quantity: 2)])
        }

        then:
        second.status == AllocationStatus.ALLOCATED
        second.quantityRemaining == 0
        second.allocations*.id as Set == first.allocations*.id as Set
        picks(req) == [['B1', 'L1', 6, 0], ['B2', 'L2', 2, 0]]
    }

    @PendingFeature(reason = 'AllocationService.groovy:137-141 a MANUAL allocation that drops an earlier pick deletes it without removing it from RequisitionItem.picklistItems (cascade all-delete-orphan): EntityNotFoundException "deleted object would be re-saved by cascade", the whole change rolls back (HTTP 500)')
    void 'MANUAL by line id removes a pick that is left out of the new allocation'() {
        given:
        String dc = depot('AE-MANDEL')
        String b1 = bin(dc, 'B1')
        String b2 = bin(dc, 'B2')
        String p = product('MANDEL')
        stock(dc, p, [[b1, 10, 'L1', SOON], [b2, 10, 'L2', LATER]])
        String l1 = lotId(p, 'L1')
        String l2 = lotId(p, 'L2')
        String req = requisition(dc, [[productId: p, quantity: 8]])
        String line = itemIds(req)[0]
        tx {
            allocationService.allocate(line, AllocationMode.MANUAL, [
                    new AllocationDto(inventoryItemId: l1, binLocationId: b1, quantity: 3),
                    new AllocationDto(inventoryItemId: l2, binLocationId: b2, quantity: 2)])
        }

        when:
        tx { allocationService.allocate(line, AllocationMode.MANUAL, [new AllocationDto(inventoryItemId: l1, binLocationId: b1, quantity: 8)]) }

        then:
        picks(req) == [['B1', 'L1', 8, 0]]
    }

    void 'by line id: an unknown line and MIXED mode are refused'() {
        given:
        String dc = depot('AE-REFUSE')
        String req = requisition(dc, [[productId: product('REF'), quantity: 1]])

        when:
        tx { allocationService.allocate('no-such-line', AllocationMode.AUTO, [], []) }

        then:
        thrown(IllegalArgumentException)

        when:
        tx { allocationService.allocate(itemIds(req)[0], AllocationMode.MIXED, [], []) }

        then:
        thrown(UnsupportedOperationException)
    }

    @PendingFeature(reason = 'AllocationService.groovy:169/384 the MANUAL top-up excludes the manual picks by object identity (never matches), so it re-suggests the bin/lot the manual pick already exhausted: 8 allocated from a bin holding 5')
    void 'MANUAL with a short manual selection tops up from OTHER stock, never over-allocating the chosen bin'() {
        given:
        String dc = depot('AE-TOPUP', [ActivityCode.REQUIRE_MOBILE_PICKING])
        String b1 = bin(dc, 'B1')
        String b2 = bin(dc, 'B2')
        String p = product('TOPUP')
        stock(dc, p, [[b1, 5, 'EARLY', SOON], [b2, 10, 'LATE', LATEST]])
        String req = requisition(dc, [[productId: p, quantity: 8]])
        String line = itemIds(req)[0]

        when:
        tx {
            def ri = org.pih.warehouse.requisition.RequisitionItem.get(line)
            List items = productAvailabilityService.getAvailableItems(Location.get(dc), [p]).findAll { it.binLocation?.id == b1 }
            allocationService.allocate(ri, 8, AllocationMode.MANUAL, items)
        }

        then:
        picks(req).groupBy { it[0] }.collectEntries { k, v -> [(k): v.sum { it[2] }] } == [B1: 5, B2: 3]
    }

    // ------------------------------------------------------------------ negative allocation / fallback

    void 'negative allocation: real stock first, the shortfall to the facility fallback location, a cycle count requested, a system comment'() {
        given:
        String dc = depot('AE-NEG', [ActivityCode.ALLOW_NEGATIVE_INVENTORY])
        String b1 = bin(dc, 'B1')
        String shortfall = bin(dc, 'SHORTFALL', [ActivityCode.NEGATIVE_INVENTORY_FALLBACK])
        String p = product('NEG')
        stock(dc, p, [[b1, 3, 'L', SOON]])
        String req = requisition(dc, [[productId: p, quantity: 5]], [negativeAllocationAllowed: true])

        when:
        autoAllocate(req)

        then:
        picks(req) == [['B1', 'L', 3, 3], ['SHORTFALL', null, 2, 2]]
        tx {
            assert CycleCountRequest.findByProductAndFacility(Product.get(p), Location.get(dc))
            Requisition r = Requisition.get(req)
            assert r.comments*.comment.any { it.startsWith('Inventory shortfall: no location at') && it.contains('quantity 2') }
            true
        }
    }

    void 'negative allocation prefers a bin allowed to go negative over the fallback location'() {
        given:
        String dc = depot('AE-NEGBIN', [ActivityCode.ALLOW_NEGATIVE_INVENTORY])
        String negBin = bin(dc, 'NEGBIN', [ActivityCode.ALLOW_NEGATIVE_INVENTORY])
        bin(dc, 'SHORTFALL', [ActivityCode.NEGATIVE_INVENTORY_FALLBACK])
        String p = product('NEGBIN')
        stock(dc, p, [[negBin, 1, 'L', SOON]])
        String req = requisition(dc, [[productId: p, quantity: 4]], [negativeAllocationAllowed: true])

        when:
        autoAllocate(req)

        then:
        picks(req) == [['NEGBIN', null, 3, 3], ['NEGBIN', 'L', 1, 1]]
        tx { Requisition.get(req).comments*.comment.any { it.startsWith('Negative inventory: allocated quantity 3') } }
    }

    void 'negative allocation is not used when the order does not allow it'() {
        given:
        String dc = depot('AE-NEGNO', [ActivityCode.ALLOW_NEGATIVE_INVENTORY])
        String b1 = bin(dc, 'B1')
        bin(dc, 'SHORTFALL', [ActivityCode.NEGATIVE_INVENTORY_FALLBACK])
        String p = product('NEGNO')
        stock(dc, p, [[b1, 3, 'L', SOON]])
        String req = requisition(dc, [[productId: p, quantity: 5]], [negativeAllocationAllowed: false])

        when:
        autoAllocate(req)

        then:
        thrown(IllegalArgumentException)
        picks(req) == []
    }

    // ------------------------------------------------------------------ read side

    void 'getOutboundOrder lists each line with its available items, allocations and status'() {
        given:
        String dc = depot('AE-READ')
        String b1 = bin(dc, 'B1')
        String p = product('READ')
        stock(dc, p, [[b1, 10, 'L', SOON]])
        String req = requisition(dc, [[productId: p, quantity: 4]])
        autoAllocate(req)

        when:
        StockMovement order = withWebRequest { tx { allocationService.getOutboundOrder(req) } }

        then:
        order.lineItems.size() == 1
        order.lineItems[0].allocationStatus == AllocationStatus.ALLOCATED
        order.lineItems[0].quantityAllocated == 4
        order.lineItems[0].allocations*.quantity == [4]
        order.lineItems[0].availableItems*.binLocation*.id == [b1]
    }

    void 'getOutboundOrder of an unknown id is refused'() {
        when:
        withWebRequest { tx { allocationService.getOutboundOrder('no-such-order') } }

        then:
        thrown(Exception)
    }
}
