package org.pih.warehouse.regression.platform.inventory

import org.pih.warehouse.inventory.*
import grails.testing.gorm.DataTest
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Instant

import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product

@Unroll
class TransactionSpec extends Specification implements DataTest {

    private static final Date PAST_DATE = Date.from(Instant.parse('2020-03-15T10:00:00Z'))
    private static final Date FAR_FUTURE_DATE = Date.from(Instant.parse('2999-01-01T00:00:00Z'))

    void setupSpec() {
        mockDomains(Transaction, TransactionType, TransactionEntry, LocalTransfer, Inventory, InventoryItem, Location, Product)
    }

    void 'transactionDate should be #description'() {
        given:
        Transaction transaction = new Transaction(transactionDate: transactionDate)

        expect:
        assert transaction.validate(['transactionDate']) == expected
        assert transaction.errors.getFieldError('transactionDate')?.code == expectedErrorCode

        where:
        transactionDate || expected | expectedErrorCode | description
        PAST_DATE       || true     | null              | 'accepted when it is in the past'
        FAR_FUTURE_DATE || false    | 'validator.invalid' | 'rejected when it is in the future'
        null            || false    | 'nullable'        | 'required'
    }

    void 'a #typeName transaction with source #sourceName and destination #destinationName should validate source #sourceValid and destination #destinationValid'() {
        given:
        TransactionType transactionType = new TransactionType(name: typeName)
        transactionType.id = typeId
        Transaction transaction = new Transaction(
                transactionType: transactionType,
                source: sourceName ? new Location(name: sourceName) : null,
                destination: destinationName ? new Location(name: destinationName) : null,
        )

        when:
        transaction.validate(['source', 'destination'])

        then:
        assert !transaction.errors.hasFieldErrors('source') == sourceValid
        assert !transaction.errors.hasFieldErrors('destination') == destinationValid

        where:
        typeName       | typeId                                     | sourceName | destinationName || sourceValid | destinationValid
        'Transfer in'  | Constants.TRANSFER_IN_TRANSACTION_TYPE_ID  | 'Supplier' | null            || true        | true
        'Transfer in'  | Constants.TRANSFER_IN_TRANSACTION_TYPE_ID  | null       | null            || false       | true
        'Transfer out' | Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID | null       | 'Ward'          || true        | true
        'Transfer out' | Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID | null       | null            || true        | false
        'Adjustment'   | Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID | null  | null            || true        | true
        'Adjustment'   | Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID | 'Supplier' | 'Ward'    || false       | false
    }

    void 'compareTo should order by transaction date and then by date created'() {
        given:
        Transaction earliest = buildDated('2020-01-01T00:00:00Z', '2020-01-05T00:00:00Z')
        Transaction sameDateCreatedFirst = buildDated('2020-01-02T00:00:00Z', '2020-01-02T08:00:00Z')
        Transaction sameDateCreatedLater = buildDated('2020-01-02T00:00:00Z', '2020-01-02T09:00:00Z')
        Transaction latest = buildDated('2020-01-03T00:00:00Z', '2020-01-01T00:00:00Z')

        expect:
        assert [latest, sameDateCreatedLater, earliest, sameDateCreatedFirst].sort() ==
                [earliest, sameDateCreatedFirst, sameDateCreatedLater, latest]
        assert sameDateCreatedFirst.compareTo(sameDateCreatedLater) < 0
        assert sameDateCreatedLater.compareTo(sameDateCreatedFirst) > 0
        assert earliest.compareTo(buildDated('2020-01-01T00:00:00Z', '2020-01-05T00:00:00Z')) == 0
    }

    void 'associatedLocation should be the id of the warehouse that owns the inventory'() {
        given:
        Location warehouse = new Location(name: 'Warehouse')
        warehouse.id = 'warehouse-1'

        expect:
        assert new Transaction(inventory: new Inventory(warehouse: warehouse)).associatedLocation == 'warehouse-1'
        assert new Transaction().associatedLocation == null
    }

    void 'associatedProducts should list each product of the entries once'() {
        given:
        Product firstProduct = new Product(name: 'first')
        firstProduct.id = 'product-1'
        Product secondProduct = new Product(name: 'second')
        secondProduct.id = 'product-2'
        Transaction transaction = new Transaction(transactionEntries: [
                new TransactionEntry(inventoryItem: new InventoryItem(product: firstProduct, lotNumber: 'A')),
                new TransactionEntry(inventoryItem: new InventoryItem(product: secondProduct, lotNumber: 'B')),
                new TransactionEntry(inventoryItem: new InventoryItem(product: firstProduct, lotNumber: 'C')),
        ])

        expect:
        assert transaction.associatedProducts == ['product-1', 'product-2']
        assert new Transaction().associatedProducts == null
    }

    void 'a transaction outside a local transfer should have no other transaction and not be internal'() {
        given:
        Transaction transaction = saveTransaction(Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID, null, new Location(name: 'Ward'))

        expect:
        assert transaction.localTransfer == null
        assert transaction.otherTransaction == null
        assert !transaction.isInternal
    }

    void 'both sides of a local transfer should see each other through it'() {
        given:
        Location warehouse = new Location(name: 'Warehouse').save(validate: false)
        Location ward = new Location(name: 'Ward').save(validate: false)
        // The warehouse sends to the ward, and the ward records the warehouse as its source
        Transaction transferOut = saveTransaction(Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID, null, ward)
        Transaction transferIn = saveTransaction(Constants.TRANSFER_IN_TRANSACTION_TYPE_ID, warehouse, null)
        LocalTransfer localTransfer = new LocalTransfer(sourceTransaction: transferOut, destinationTransaction: transferIn)
                .save(validate: false, flush: true)

        expect:
        assert transferOut.localTransfer.id == localTransfer.id
        assert transferIn.localTransfer.id == localTransfer.id
        assert transferOut.otherTransaction.id == transferIn.id
        assert transferIn.otherTransaction.id == transferOut.id

        and: 'a transfer between two different locations is not internal'
        assert !transferOut.isInternal
        assert !transferIn.isInternal
    }

    void 'a local transfer should be internal when the location sent to is the location received from'() {
        given:
        Location ward = new Location(name: 'Ward').save(validate: false)
        Transaction transferOut = saveTransaction(Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID, null, ward)
        Transaction transferIn = saveTransaction(Constants.TRANSFER_IN_TRANSACTION_TYPE_ID, ward, null)
        new LocalTransfer(sourceTransaction: transferOut, destinationTransaction: transferIn)
                .save(validate: false, flush: true)

        expect:
        assert transferOut.isInternal
        assert transferIn.isInternal
    }

    private static Transaction saveTransaction(String transactionTypeId, Location source, Location destination) {
        TransactionType transactionType = TransactionType.get(transactionTypeId)
        if (!transactionType) {
            transactionType = new TransactionType(name: "Type ${transactionTypeId}")
            transactionType.id = transactionTypeId
            transactionType.save(validate: false)
        }
        return new Transaction(
                transactionType: transactionType,
                transactionDate: PAST_DATE,
                source: source,
                destination: destination,
                disableRefresh: true,
        ).save(validate: false, flush: true)
    }

    private static Transaction buildDated(String transactionDate, String dateCreated) {
        Transaction transaction = new Transaction(transactionDate: date(transactionDate))
        // dateCreated is not bindable through the map constructor
        transaction.dateCreated = date(dateCreated)
        return transaction
    }

    private static Date date(String isoInstant) {
        return Date.from(Instant.parse(isoInstant))
    }
}
