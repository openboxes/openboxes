package org.pih.warehouse.regression.platform.inventory

import spock.lang.PendingFeature

import org.pih.warehouse.inventory.*
import grails.testing.gorm.DataTest
import spock.lang.Specification

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.ReasonCode
import org.pih.warehouse.core.User
import org.pih.warehouse.core.VarianceTypeCode
import org.pih.warehouse.product.Product

class InventoryTransactionsSummarySpec extends Specification implements DataTest {

    void setupSpec() {
        mockDomains(User, Location)
    }

    ReasonCode damaged = ReasonCode.values()[0]
    ReasonCode expired = ReasonCode.values()[1]

    void 'the variance type follows the sign of the difference'() {
        expect:
        new InventoryTransactionsSummary(quantityDifference: difference).varianceTypeCode == expected

        where:
        difference || expected
        3          || VarianceTypeCode.MORE
        -2         || VarianceTypeCode.LESS
        0          || VarianceTypeCode.EQUAL
    }

    void 'the action is a cycle count, an import, an adjustment or a stock record'() {
        expect:
        new InventoryTransactionsSummary(transaction: transaction, baselineTransaction: baseline).transactionAction ==
                expected

        where:
        transaction                                              | baseline          || expected
        new Transaction(cycleCount: new CycleCount())            | null              || TransactionAction.CYCLE_COUNT
        new Transaction(comment: 'Imported from stock.xls')      | new Transaction() || TransactionAction.INVENTORY_IMPORT
        new Transaction(comment: 'Recount')                      | null              || TransactionAction.INVENTORY_ADJUSTMENT
        new Transaction()                                        | new Transaction() || TransactionAction.RECORD_STOCK
    }

    void 'root causes come from the discrepancy reasons of the cycle count'() {
        given:
        CycleCount cycleCount = new CycleCount(cycleCountItems: [
                new CycleCountItem(discrepancyReasonCode: damaged),
                new CycleCountItem(discrepancyReasonCode: null),
                new CycleCountItem(discrepancyReasonCode: expired)] as Set)

        expect:
        new InventoryTransactionsSummary(transaction: new Transaction(cycleCount: cycleCount)).rootCauses as Set ==
                [damaged, expired] as Set
        new InventoryTransactionsSummary(transaction: new Transaction()).rootCauses == null
    }

    void 'the transaction comment is listed when the entries have none for the product'() {
        given:
        Product product = new Product(name: 'Gloves')
        product.id = 'p1'
        Product other = new Product(name: 'Masks')
        other.id = 'p2'
        Transaction transaction = new Transaction(comment: 'Monthly count', transactionEntries: [
                new TransactionEntry(product: product),
                new TransactionEntry(product: other, comments: 'other product')] as Set)

        expect:
        new InventoryTransactionsSummary(transaction: transaction, product: product).comments == ['Monthly count']
        new InventoryTransactionsSummary(transaction: new Transaction(transactionEntries: [] as Set), product: product)
                .comments == []
    }

    @PendingFeature(reason = 'OBAF getComments nests the entry comments list as one element')
    void 'the transaction comment and the entry comments for the product form one flat list of strings'() {
        given:
        Product product = new Product(name: 'Gloves')
        product.id = 'p1'
        Transaction transaction = new Transaction(comment: 'Monthly count', transactionEntries: [
                new TransactionEntry(product: product, comments: 'damaged box')] as Set)
        Transaction withoutComment = new Transaction(transactionEntries: [
                new TransactionEntry(product: product, comments: 'damaged box')] as Set)

        expect:
        new InventoryTransactionsSummary(transaction: transaction, product: product).comments ==
                ['Monthly count', 'damaged box']
        new InventoryTransactionsSummary(transaction: withoutComment, product: product).comments == ['damaged box']
    }

    void 'the json view describes the transaction, product, facility and variance'() {
        given:
        Product product = Stub(Product) {
            getId() >> 'p1'
            getName() >> 'Gloves'
            getProductCode() >> 'G1'
            getDisplayNameOrDefaultName() >> 'Nitrile gloves'
        }
        Location facility = new Location(name: 'Depot', locationNumber: 'D1')
        Date recorded = new Date(5000)
        InventoryTransactionsSummary summary = new InventoryTransactionsSummary(
                transaction: new Transaction(transactionNumber: 'T-1', comment: 'Count', transactionEntries: [] as Set),
                product: product, facility: facility, quantityBefore: 5, quantityAfter: 3, quantityDifference: -2,
                dateRecorded: recorded, recordedBy: new User(firstName: 'Ann', lastName: 'Lee'),
                baselineTransaction: new Transaction())

        when:
        Map json = summary.toJson()

        then:
        json.transaction == [transactionNumber: 'T-1']
        json.product == [id: 'p1', name: 'Gloves', productCode: 'G1', displayName: 'Nitrile gloves']
        json.facility.name == 'Depot'
        json.quantityDifference == -2
        json.dateRecorded == recorded
        json.recordedBy == [name: 'Ann Lee']
        json.rootCauses == null
        json.comments == ['Count']
        json.varianceTypeCode == 'LESS'
        json.transactionAction == 'RECORD_STOCK'
    }
}
