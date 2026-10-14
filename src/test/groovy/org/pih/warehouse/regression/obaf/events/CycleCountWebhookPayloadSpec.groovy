package org.pih.warehouse.regression.obaf.events

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.ApiClientService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.core.WebhookPublisherService
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionCode
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.regression.obaf.support.UnitAllocationData
import org.pih.warehouse.product.Product

/**
 * WebhookPublisherService.publishCycleCountCompletedEvent: the cycleCount.completed payload built
 * from a count's baseline and adjustment transactions (in-memory, never saved), posted to the
 * event webhook path; ApiClientService is a mock that records the call. The integration specs cover the
 * transport; CycleCountCompletedEventService needs a completed count (not built here).
 */
class CycleCountWebhookPayloadSpec extends Specification implements ServiceUnitTest<WebhookPublisherService>, DataTest {

    static final String ISO = /\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(Z|[+-]\d{2}:\d{2})/

    List<Map> posted = []
    Location facility
    Location binA
    Location binB
    Product product
    Product other
    InventoryItem lot1
    InventoryItem lot2
    User counter

    void setup() {
        config.openboxes.n8n.enabled = true
        config.openboxes.n8n.endpoint.url = 'http://127.0.0.1:9/webhook'
        config.openboxes.n8n.endpoint.headers = [:]
        service.apiClientService = Stub(ApiClientService) {
            post(_ as String, _ as Map, _) >> { String url, Map payload, Map headers -> posted << payload; null }
        }
        facility = UnitAllocationData.facility('Count DC', [ActivityCode.ENABLE_WEBHOOKS])
        facility.locationNumber = 'CDC'
        binA = UnitAllocationData.bin(facility, 'A')
        binA.locationNumber = 'A-01'
        binB = UnitAllocationData.bin(facility, 'B')
        binB.locationNumber = 'B-01'
        product = UnitAllocationData.product('CC-1')
        other = UnitAllocationData.product('CC-2')
        lot1 = UnitAllocationData.lot(product, 'L1')
        lot2 = UnitAllocationData.lot(product, 'L2')
        counter = new User(firstName: 'Count', lastName: 'Er', username: 'counter')
    }

    Transaction txn(String id, TransactionCode code, List<List> rows, String comment = null) {
        Transaction t = new Transaction(transactionType: new TransactionType(name: code.name(), transactionCode: code),
                transactionDate: new Date(), comment: comment, createdBy: counter)
        t.id = id
        t.transactionEntries = rows.collect { List r ->
            TransactionEntry e = new TransactionEntry(inventoryItem: r[0] as InventoryItem, binLocation: r[1] as Location, quantity: r[2] as Integer,
                    reasonCode: r.size() > 3 ? r[3] : null)
            e.transaction = t
            e
        }
        return t
    }

    void 'a count with a discrepancy: totals from the baseline and the adjustment, one row per adjusted bin/lot'() {
        given: 'counted on hand A/L1 10, B/L2 5 (and another product); the adjustment takes 2 from A/L1 and finds 1 of L1 in B'
        Transaction baseline = txn('base-1', TransactionCode.PRODUCT_INVENTORY, [[lot1, binA, 10], [lot2, binB, 5],
                                                                                [UnitAllocationData.lot(other, 'X'), binA, 99]])
        Transaction adjustment = txn('adj-1', TransactionCode.CREDIT, [[lot1, binA, -2, 'DAMAGED'], [lot1, binB, 1, 'FOUND']], 'recount')

        when:
        service.publishCycleCountCompletedEvent(product, facility, baseline, adjustment)

        then:
        posted.size() == 1
        Map p = posted[0]
        p.eventType == 'cycleCount.completed'
        p.eventDate ==~ ISO
        p.triggeredBy == counter.name
        p.count.id == 'adj-1'
        p.count.comment == 'recount'
        p.count.countedBy == counter.name
        p.count.product == 'CC-1'
        p.count.totals == [quantityBeforeAdjustment: 15, quantityAfterAdjustment: 14, quantityVariance: -1]
        p.count.adjustments.collect { [it.location, it.quantityBeforeAdjustment, it.quantityAfterAdjustment, it.quantityVariance, it.reasonCode] } as Set == [
                ['A-01', 10, 8, -2, 'DAMAGED'], ['B-01', 0, 1, 1, 'FOUND']] as Set
        p.metadata.facilityCode == 'CDC'
        p.metadata.attemptNumber == 1
    }

    void 'a count that matched (baseline only, no adjustment): variance 0, no rows, the baseline identifies it'() {
        given:
        Transaction baseline = txn('base-2', TransactionCode.PRODUCT_INVENTORY, [[lot1, binA, 4]])

        when:
        service.publishCycleCountCompletedEvent(product, facility, baseline, null)

        then:
        posted.size() == 1
        posted[0].count.id == 'base-2'
        posted[0].count.comment == null
        posted[0].count.totals == [quantityBeforeAdjustment: 4, quantityAfterAdjustment: 4, quantityVariance: 0]
        posted[0].count.adjustments == []
    }

    void 'no baseline, no product or no facility: nothing is posted'() {
        when:
        service.publishCycleCountCompletedEvent(product, facility, null, null)
        service.publishCycleCountCompletedEvent(null, facility, txn('b', TransactionCode.PRODUCT_INVENTORY, []), null)
        service.publishCycleCountCompletedEvent(product, null, txn('b', TransactionCode.PRODUCT_INVENTORY, []), null)

        then:
        posted.isEmpty()
    }

    void 'the event webhook path switched off: nothing is posted'() {
        given:
        config.openboxes.n8n.enabled = false

        when:
        service.publishCycleCountCompletedEvent(product, facility, txn('b', TransactionCode.PRODUCT_INVENTORY, [[lot1, binA, 1]]), null)

        then:
        posted.isEmpty()
    }

    void 'publishInventoryAdjustmentEvent without a product, facility or transaction posts nothing'() {
        when:
        service.publishInventoryAdjustmentEvent(null, facility, txn('t', TransactionCode.CREDIT, []))
        service.publishInventoryAdjustmentEvent(product, null, txn('t', TransactionCode.CREDIT, []))
        service.publishInventoryAdjustmentEvent(product, facility, null)

        then:
        posted.isEmpty()
    }
}
