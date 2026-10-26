package org.pih.warehouse.api.regression.obaf.events

import org.springframework.transaction.TransactionStatus

import org.pih.warehouse.api.regression.obaf.events.support.LoopbackReceiver
import org.pih.warehouse.api.regression.obaf.events.support.ObafEventsIntegrationSpec
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.CycleCountCompletedEvent
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.CycleCount
import org.pih.warehouse.inventory.CycleCountItem
import org.pih.warehouse.inventory.CycleCountItemStatus
import org.pih.warehouse.inventory.CycleCountStatus
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionAction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionSource
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.product.Product

/**
 * CycleCountCompletedEventService (@Async, after commit) on a real database: a closed count posts
 * one cycleCount.completed per counted product, built from the count's baseline and adjustment
 * transactions (which themselves post no adjustment.created); an open count, a facility without
 * ENABLE_WEBHOOKS, or a publication that is rolled back posts nothing. The count is built directly
 * (CycleCount + items + the two transactions CycleCountService would write), then the event is
 * published the way CycleCountService.closeCycleCount does. The payload arithmetic in detail is in
 * the unit CycleCountWebhookPayloadSpec.
 */
class CycleCountWebhookIntegrationSpec extends ObafEventsIntegrationSpec {

    static final Date SOON = new Date() + 90

    static class Count {
        String dc, bin, product, code, lot, cycleCountId
    }

    /** A count of one lot in one bin: on hand {@code onHand}, counted {@code counted}; items in {@code itemStatus}. */
    Count count(String prefix, int onHand, int counted, CycleCountItemStatus itemStatus = CycleCountItemStatus.APPROVED,
                boolean webhooks = true) {
        Count c = new Count()
        c.dc = depot(prefix, webhooks ? [ActivityCode.ENABLE_WEBHOOKS] : [])
        c.bin = bin(c.dc, 'CC')
        c.product = product(prefix)
        c.lot = "CC-${uid()}"
        c.code = tx { Product.get(c.product).productCode } as String
        c.cycleCountId = tx {
            authService.setCurrentUser(User.findByUsername('admin'))
            Location facility = Location.get(c.dc)
            Product p = Product.get(c.product)
            InventoryItem lot = new InventoryItem(product: p, lotNumber: c.lot, expirationDate: SOON).save(flush: true, failOnError: true)
            CycleCount cc = new CycleCount(facility: facility, dateLastRefreshed: new Date(), status: CycleCountStatus.REQUESTED)
            cc.save(flush: true, failOnError: true)
            CycleCountItem item = new CycleCountItem(cycleCount: cc, facility: facility, inventoryItem: lot, location: Location.get(c.bin),
                    product: p, countIndex: 0, status: itemStatus, quantityOnHand: onHand, quantityCounted: counted, custom: false,
                    createdBy: User.findByUsername('admin'), updatedBy: User.findByUsername('admin'))
            item.save(flush: true, failOnError: true)
            cc.addToCycleCountItems(item)
            cc.save(flush: true, failOnError: true)   // beforeUpdate recomputes the status from the items
            // as CycleCountProductInventoryTransactionService does: one source, both transactions linked to the count
            // (getCycleCountTransactions inner-joins transactionSource: a transaction without one is never found)
            TransactionSource source = new TransactionSource(transactionAction: TransactionAction.CYCLE_COUNT, cycleCount: cc,
                    origin: facility).save(flush: true, failOnError: true)
            transactionOf(cc, source, facility, Constants.INVENTORY_BASELINE_TRANSACTION_TYPE_ID, lot, c.bin, onHand)
            if (counted != onHand) {
                transactionOf(cc, source, facility, Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID, lot, c.bin, counted - onHand)
            }
            cc.id
        } as String
        return c
    }

    private static void transactionOf(CycleCount cc, TransactionSource source, Location facility, String typeId, InventoryItem lot,
                                      String binId, int quantity) {
        Transaction t = new Transaction(transactionType: TransactionType.get(typeId), inventory: facility.inventory,
                transactionDate: new Date() - 1, cycleCount: cc, transactionSource: source, comment: 'test count')
        t.addToTransactionEntries(new TransactionEntry(inventoryItem: lot, binLocation: Location.get(binId), quantity: quantity))
        t.save(flush: true, failOnError: true)
    }

    void publishCompleted(String cycleCountId, boolean rollback = false) {
        CycleCount.withNewTransaction { TransactionStatus st ->
            grailsApplication.mainContext.publishEvent(new CycleCountCompletedEvent(CycleCount.get(cycleCountId)))
            if (rollback) {
                st.setRollbackOnly()
            }
        }
    }

    void 'a completed count posts cycleCount.completed for the product, after commit; its own transactions post no adjustment.created'() {
        given:
        LoopbackReceiver r = webhookReceiver()
        Count c = count('CCW-OK', 10, 8)

        expect:
        tx { CycleCount.get(c.cycleCountId).status } == CycleCountStatus.COMPLETED
        tx { CycleCount.get(c.cycleCountId).products*.id } == [c.product]
        tx { Transaction.findAllByCycleCount(CycleCount.get(c.cycleCountId))*.transactionType*.id as Set } == [Constants.INVENTORY_BASELINE_TRANSACTION_TYPE_ID, Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID] as Set

        when:
        publishCompleted(c.cycleCountId)

        then:
        eventually { events(r, 'cycleCount.completed', c.code) }
        settle()
        payloads(r, c.code)*.eventType == ['cycleCount.completed']
        Map p = events(r, 'cycleCount.completed', c.code)[0]
        p.count.product == c.code
        p.count.totals == [quantityBeforeAdjustment: 10, quantityAfterAdjustment: 8, quantityVariance: -2]
        p.count.adjustments.collect { [prefixOf(it.location), it.quantityBeforeAdjustment, it.quantityAfterAdjustment] } == [['CC', 10, 8]]
        p.metadata.facilityId == c.dc
    }

    void 'a count that matched posts a zero variance and no adjustment rows'() {
        given:
        LoopbackReceiver r = webhookReceiver()
        Count c = count('CCW-EQ', 5, 5)

        when:
        publishCompleted(c.cycleCountId)

        then:
        eventually { events(r, 'cycleCount.completed', c.code) }
        events(r, 'cycleCount.completed', c.code)[0].count.totals == [quantityBeforeAdjustment: 5, quantityAfterAdjustment: 5, quantityVariance: 0]
        events(r, 'cycleCount.completed', c.code)[0].count.adjustments == []
    }

    void 'nothing is posted for #label'() {
        given:
        LoopbackReceiver r = webhookReceiver()
        Count c = count("CCW-${uid()}", 6, 4, itemStatus, webhooks)

        when:
        publishCompleted(c.cycleCountId, rollback)
        settle()

        then:
        events(r, 'cycleCount.completed', c.code).isEmpty()

        where:
        label                                   | itemStatus                        | webhooks | rollback
        'a count still being counted'           | CycleCountItemStatus.COUNTED      | true     | false
        'a facility without ENABLE_WEBHOOKS'    | CycleCountItemStatus.APPROVED     | false    | false
        'a publication that is rolled back'     | CycleCountItemStatus.APPROVED     | true     | true
    }
}
