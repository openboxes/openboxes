package org.pih.warehouse.api.regression.obaf.events

import org.springframework.transaction.TransactionStatus
import spock.lang.PendingFeature

import org.pih.warehouse.api.regression.obaf.events.support.LoopbackReceiver
import org.pih.warehouse.api.regression.obaf.events.support.ObafEventsIntegrationSpec
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.product.Product

/**
 * TransactionCreatedEventService (published from Transaction.afterInsert for EVERY transaction,
 * handled @Async after commit) on a real database: which transactions become adjustment.created,
 * one event per product, the before/after/variance arithmetic per bin, after commit only, no
 * repeat on update. ObafListenersIntegrationSpec already covers "an adjustment is posted" and "no
 * ENABLE_WEBHOOKS, nothing posted".
 */
class AdjustmentWebhookIntegrationSpec extends ObafEventsIntegrationSpec {

    static final Date SOON = new Date() + 90

    String code(String productId) {
        return tx { Product.get(productId).productCode } as String
    }

    /** A transaction of {@code typeId} at the depot with one entry per row [productId, binId, lot, quantity]. */
    String transaction(String depotId, String typeId, List<List> rows, boolean rollback = false) {
        String id = null
        String elsewhere = destination()   // created (in its own transaction) before this one starts
        Transaction.withNewTransaction { TransactionStatus st ->
            authService.setCurrentUser(User.findByUsername('admin'))
            Location facility = Location.get(depotId)
            Transaction t = new Transaction(transactionType: TransactionType.get(typeId), inventory: facility.inventory,
                    transactionDate: new Date() - 1, comment: 'test adjustment')
            if (typeId == Constants.TRANSFER_IN_TRANSACTION_TYPE_ID) {
                t.source = Location.get('2')
            }
            if (typeId == Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID) {
                t.destination = Location.get(elsewhere)
            }
            rows.each { List r ->
                Product p = Product.get(r[0] as String)
                InventoryItem lot = InventoryItem.findByProductAndLotNumber(p, r[2] as String) ?:
                        new InventoryItem(product: p, lotNumber: r[2] as String, expirationDate: SOON).save(flush: true, failOnError: true)
                t.addToTransactionEntries(new TransactionEntry(inventoryItem: lot, binLocation: Location.get(r[1] as String),
                        quantity: r[3] as Integer, reasonCode: 'DAMAGED', comments: "row ${r[3]}".toString()))
            }
            t.save(flush: true, failOnError: true)
            id = t.id
            if (rollback) {
                st.setRollbackOnly()
            }
        }
        return id
    }

    void 'record stock: before / after / variance come from the stock just before the adjustment, per bin and in total'() {
        given:
        String dc = depot('ADJ-RS', [ActivityCode.ENABLE_WEBHOOKS])
        String b1 = bin(dc, 'B1')
        String b2 = bin(dc, 'B2')
        String p = product('ADJRS')
        String pc = code(p)
        LoopbackReceiver r = webhookReceiver()
        stock(dc, p, [[b1, 7, 'L1', SOON], [b2, 3, 'L1', SOON]])
        eventually { events(r, 'adjustment.created', pc) }
        Thread.sleep(2500)   // the baseline is read at transactionDate - 1 s, and record stock dates its adjustment 1 s after its count (#43)

        when: 'b1 is counted at 4, b2 unchanged'
        stock(dc, p, [[b1, 4, 'L1', SOON], [b2, 3, 'L1', SOON]])

        then:
        eventually { events(r, 'adjustment.created', pc).size() == 2 }
        Map first = events(r, 'adjustment.created', pc)[0]
        first.adjustment.totals == [quantityBeforeAdjustment: 0, quantityAfterAdjustment: 10, quantityVariance: 10]
        Map second = events(r, 'adjustment.created', pc)[1]
        second.adjustment.totals == [quantityBeforeAdjustment: 10, quantityAfterAdjustment: 7, quantityVariance: -3]
        second.adjustment.adjustments.findAll { it.quantityVariance }.collect { [prefixOf(it.location), it.quantityBeforeAdjustment, it.quantityAfterAdjustment, it.quantityVariance] } ==
                [['B1', 7, 4, -3]]
        second.adjustment.product == pc
        second.eventDate ==~ WebhookDeliveryIntegrationSpec.ISO_8601
        second.adjustment.dateAdjusted ==~ WebhookDeliveryIntegrationSpec.ISO_8601
        second.triggeredBy == second.adjustment.adjustedBy
        second.metadata.facilityId == dc
    }

    @PendingFeature(reason = 'WebhookPublisherService.groovy:157-159 reads the "before" stock at transactionDate minus one second: a second adjustment of the product within about two seconds of the previous one (record stock dates its adjustment one second after its count; a quick recount, a mobile retry) ignores it and reports quantityBeforeAdjustment 0 and a NEGATIVE quantityAfterAdjustment (measured: 7 then 4 gives before 0, after -3)')
    void 'two adjustments of one product within the same second each report the stock before them'() {
        given:
        String dc = depot('ADJ-SS', [ActivityCode.ENABLE_WEBHOOKS])
        String b1 = bin(dc, 'B1')
        String p = product('ADJSS')
        String pc = code(p)
        LoopbackReceiver r = webhookReceiver()

        when:
        stock(dc, p, [[b1, 7, 'L1', SOON]])
        stock(dc, p, [[b1, 4, 'L1', SOON]])

        then:
        eventually { events(r, 'adjustment.created', pc).size() == 2 }
        events(r, 'adjustment.created', pc)*.adjustment*.totals as Set == [
                [quantityBeforeAdjustment: 0, quantityAfterAdjustment: 7, quantityVariance: 7],
                [quantityBeforeAdjustment: 7, quantityAfterAdjustment: 4, quantityVariance: -3]] as Set
    }

    void 'one adjustment transaction that touches two products posts one event per product, each with only its own rows'() {
        given:
        String dc = depot('ADJ-2P', [ActivityCode.ENABLE_WEBHOOKS])
        String b1 = bin(dc, 'B1')
        String p1 = product('ADJ2PA')
        String p2 = product('ADJ2PB')
        LoopbackReceiver r = webhookReceiver()

        when:
        transaction(dc, Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID, [[p1, b1, 'LA', 5], [p2, b1, 'LB', 2], [p2, b1, 'LB2', 1]])

        then:
        eventually { events(r, 'adjustment.created', code(p1)) && events(r, 'adjustment.created', code(p2)) }
        settle()
        events(r, 'adjustment.created', code(p1)).size() == 1
        events(r, 'adjustment.created', code(p2)).size() == 1
        events(r, 'adjustment.created', code(p1))[0].adjustment.adjustments*.quantityVariance == [5]
        events(r, 'adjustment.created', code(p2))[0].adjustment.adjustments*.quantityVariance.sort() == [1, 2]
        events(r, 'adjustment.created', code(p2))[0].adjustment.totals.quantityVariance == 3
        events(r, 'adjustment.created', code(p2))[0].adjustment.adjustments*.reasonCode.unique() == ['DAMAGED']
        events(r, 'adjustment.created', code(p1))[0].adjustment.comment == 'test adjustment'
    }

    void 'transaction type #label: posted as adjustment.created = #posted'() {
        given:
        String dc = depot('ADJ-TY', [ActivityCode.ENABLE_WEBHOOKS])
        String b1 = bin(dc, 'B1')
        String p = product('ADJTY')
        LoopbackReceiver r = webhookReceiver()

        when:
        transaction(dc, typeId, [[p, b1, 'LT', 2]])
        if (posted) {
            eventually { events(r, 'adjustment.created', code(p)) }
        } else {
            settle()
        }

        then:
        (events(r, 'adjustment.created', code(p)).size() == 1) == posted

        where:
        label                     | typeId                                             || posted
        'ADJUSTMENT_CREDIT'       | Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID    || true
        'ADJUSTMENT_DEBIT'        | Constants.ADJUSTMENT_DEBIT_TRANSACTION_TYPE_ID     || true
        'CONSUMPTION (a debit)'   | Constants.CONSUMPTION_TRANSACTION_TYPE_ID          || true
        'EXPIRATION (a debit)'    | Constants.EXPIRATION_TRANSACTION_TYPE_ID           || true
        'TRANSFER_IN'             | Constants.TRANSFER_IN_TRANSACTION_TYPE_ID          || false
        'TRANSFER_OUT'            | Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID         || false
        'PRODUCT_INVENTORY'       | Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID    || false
        'INVENTORY_BASELINE'      | Constants.INVENTORY_BASELINE_TRANSACTION_TYPE_ID   || false
    }

    void 'a debit is reported with a negative variance'() {
        given:
        String dc = depot('ADJ-DB', [ActivityCode.ENABLE_WEBHOOKS])
        String b1 = bin(dc, 'B1')
        String p = product('ADJDB')
        LoopbackReceiver r = webhookReceiver()

        when:
        transaction(dc, Constants.ADJUSTMENT_DEBIT_TRANSACTION_TYPE_ID, [[p, b1, 'LD', 2]])

        then:
        eventually { events(r, 'adjustment.created', code(p)) }
        events(r, 'adjustment.created', code(p))[0].adjustment.totals.quantityVariance == -2
    }

    void 'an adjustment that is rolled back posts nothing'() {
        given:
        String dc = depot('ADJ-RB', [ActivityCode.ENABLE_WEBHOOKS])
        String b1 = bin(dc, 'B1')
        String p = product('ADJRB')
        LoopbackReceiver r = webhookReceiver()

        when:
        String id = transaction(dc, Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID, [[p, b1, 'LR', 2]], true)
        settle()

        then:
        tx { Transaction.get(id) } == null
        payloads(r, code(p)).isEmpty()
    }

    void 'updating an adjustment afterwards posts nothing more'() {
        given:
        String dc = depot('ADJ-UP', [ActivityCode.ENABLE_WEBHOOKS])
        String b1 = bin(dc, 'B1')
        String p = product('ADJUP')
        LoopbackReceiver r = webhookReceiver()
        String id = transaction(dc, Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID, [[p, b1, 'LU', 2]])
        eventually { events(r, 'adjustment.created', code(p)) }

        when:
        tx { Transaction t = Transaction.get(id); t.comment = 'edited'; t.save(flush: true, failOnError: true) }
        settle()

        then:
        events(r, 'adjustment.created', code(p)).size() == 1
    }

    void 'characterization: the listener is @Async: the post happens on another thread, after the saving thread has moved on'() {
        given:
        String dc = depot('ADJ-AS', [ActivityCode.ENABLE_WEBHOOKS])
        String b1 = bin(dc, 'B1')
        String p = product('ADJAS')
        LoopbackReceiver r = webhookReceiver()
        r.hang = true   // the receiver never answers: a synchronous publisher would block the save below

        when:
        long t0 = System.currentTimeMillis()
        transaction(dc, Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID, [[p, b1, 'LA', 2]])
        long took = System.currentTimeMillis() - t0

        then:
        took < 10000
        eventually { r.hits.any { it.body.contains(code(p)) } }
    }

    void 'with the event webhook path switched off an adjustment posts nothing'() {
        given:
        String dc = depot('ADJ-OFF', [ActivityCode.ENABLE_WEBHOOKS])
        String b1 = bin(dc, 'B1')
        String p = product('ADJOFF')
        LoopbackReceiver r = webhookReceiver()
        config.set('openboxes.n8n.enabled', false)

        when:
        transaction(dc, Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID, [[p, b1, 'LO', 2]])
        settle()

        then:
        r.hits.isEmpty()
    }
}
