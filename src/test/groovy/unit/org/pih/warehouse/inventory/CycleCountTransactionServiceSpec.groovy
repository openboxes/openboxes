package unit.org.pih.warehouse.inventory

import grails.testing.gorm.DataTest
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.CycleCount
import org.pih.warehouse.inventory.CycleCountItem
import org.pih.warehouse.inventory.CycleCountProductAvailabilityService
import org.pih.warehouse.inventory.CycleCountTransactionService
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.inventory.ProductInventorySnapshotSource
import org.pih.warehouse.inventory.ProductInventoryTransactionService
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionIdentifierService
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.product.Product

@Unroll
class CycleCountTransactionServiceSpec extends Specification implements DataTest {

    @Shared
    CycleCountTransactionService cycleCountTransactionService

    @Shared
    CycleCountProductAvailabilityService cycleCountProductAvailabilityServiceStub

    @Shared
    ProductAvailabilityService productAvailabilityServiceStub

    @Shared
    ProductInventoryTransactionService productInventoryTransactionServiceStub

    @Shared
    TransactionIdentifierService transactionIdentifierServiceStub

    @Shared
    TransactionType productInventoryTransactionType

    @Shared
    TransactionType adjustmentTransactionType

    void setupSpec() {
        mockDomains(Transaction, TransactionEntry, TransactionType)
    }

    void setup() {
        cycleCountTransactionService = new CycleCountTransactionService()

        // Set up the stubs
        cycleCountProductAvailabilityServiceStub = Stub(CycleCountProductAvailabilityService)
        cycleCountTransactionService.cycleCountProductAvailabilityService = cycleCountProductAvailabilityServiceStub
        cycleCountProductAvailabilityServiceStub.refreshProductAvailability(_ as CycleCount) >>
                new CycleCountProductAvailabilityService.CycleCountItemsForRefresh()

        productInventoryTransactionServiceStub = Stub(ProductInventoryTransactionService)
        cycleCountTransactionService.productInventoryTransactionService = productInventoryTransactionServiceStub

        productAvailabilityServiceStub = Stub(ProductAvailabilityService)
        cycleCountTransactionService.productAvailabilityService = productAvailabilityServiceStub

        transactionIdentifierServiceStub = Stub(TransactionIdentifierService)
        cycleCountTransactionService.transactionIdentifierService = transactionIdentifierServiceStub

        // Set up the transaction types
        productInventoryTransactionType = new TransactionType()
        productInventoryTransactionType.id = Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID
        productInventoryTransactionType.save(validate: false)

        adjustmentTransactionType = new TransactionType()
        adjustmentTransactionType.id = Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID
        adjustmentTransactionType.save(validate: false)
    }

    void 'createTransactions should succeed for a single product count when adjustments are not required'() {
        given: 'mocked inputs'
        Location facility = new Location(inventory: new Inventory())
        Product product = new Product()
        Date date = new Date()

        and: 'a cycle count with no discrepancies'
        CycleCount cycleCount = new CycleCount(
                facility: facility,
                cycleCountItems: [
                        new CycleCountItem(
                                inventoryItem: new InventoryItem(),
                                location: new Location(),
                                product: product,
                                countIndex: 0,
                                quantityOnHand: 10,
                                quantityCounted: 10,
                        ),
                ]
        )

        and: 'a mocked product inventory transaction'
        createExpectedProductInventoryTransaction(facility, product, date)

        when:
        List<Transaction> transactions = cycleCountTransactionService.createTransactions(cycleCount, true)

        then: 'the only transaction should be the product inventory one'
        assert transactions.size() == 1
        assert transactions.findAll{ it.transactionType != adjustmentTransactionType }.size() == 1
    }

    void 'createTransactions should succeed for a multiple product count when adjustments are not required'() {
        given: 'mocked inputs'
        Location facility = new Location(inventory: new Inventory())
        Product product1 = new Product()
        product1.id = 1
        Product product2 = new Product()
        product2.id = 2
        Date date = new Date()

        and: 'a cycle count with two products and no discrepancies'
        CycleCount cycleCount = new CycleCount(
                facility: facility,
                cycleCountItems: [
                        new CycleCountItem(
                                inventoryItem: new InventoryItem(),
                                location: new Location(),
                                product: product1,
                                countIndex: 0,
                                quantityOnHand: 10,
                                quantityCounted: 10,
                        ),
                        new CycleCountItem(
                                inventoryItem: new InventoryItem(),
                                location: new Location(),
                                product: product2,
                                countIndex: 0,
                                quantityOnHand: 20,
                                quantityCounted: 20,
                        ),
                ]
        )

        and: 'mocked product inventory transactions'
        createExpectedProductInventoryTransaction(facility, product1, date)
        createExpectedProductInventoryTransaction(facility, product2, date)

        when:
        List<Transaction> transactions = cycleCountTransactionService.createTransactions(cycleCount, true)

        then: 'the only transactions should be the product inventory ones'
        assert transactions.size() == 2
        assert transactions.findAll{ it.transactionType != adjustmentTransactionType }.size() == 2
    }

    void 'createTransactions should succeed for a single product count when adjustments are required'() {
        given: 'mocked inputs'
        Location facility = new Location(inventory: new Inventory())
        Product product = new Product()
        InventoryItem inventoryItem = new InventoryItem()
        Date date = new Date()

        and: 'a cycle count with discrepancies'
        String binNameNegativeAdjustment = "binNeg"
        String binNamePositiveAdjustment = "binPos"
        CycleCount cycleCount = new CycleCount(
                facility: facility,
                cycleCountItems: [
                        new CycleCountItem(
                                inventoryItem: inventoryItem,
                                location: new Location(name: binNameNegativeAdjustment),
                                product: product,
                                countIndex: 0,
                                quantityOnHand: 20,
                                quantityCounted: 19,  // Negative discrepancy (-1)
                        ),
                        new CycleCountItem(
                                inventoryItem: inventoryItem,
                                location: new Location(name: binNamePositiveAdjustment),
                                product: product,
                                countIndex: 0,
                                quantityOnHand: 30,
                                quantityCounted: 33,  // Positive discrepancy (+3)
                        ),
                        new CycleCountItem(
                                inventoryItem: inventoryItem,
                                location: new Location(),
                                product: product,
                                countIndex: 0,
                                quantityOnHand: 10,
                                quantityCounted: 10,  // No discrepancy (+0)
                        ),
                ]
        )

        and: 'a mocked transaction number'
        transactionIdentifierServiceStub.generate(_ as Transaction) >> "123ABC"

        and: 'a mocked product inventory transaction'
        createExpectedProductInventoryTransaction(facility, product, date)

        when:
        List<Transaction> transactions = cycleCountTransactionService.createTransactions(cycleCount, true)

        then: 'both a product inventory and adjustment transaction should be created'
        assert transactions.size() == 2
        Transaction productInventoryTransaction = transactions.find{ it.transactionType != adjustmentTransactionType }
        assert productInventoryTransaction != null

        // The adjustment transaction isn't mocked so make sure to assert on its fields
        Transaction adjustmentTransaction = transactions.find{ it.transactionType == adjustmentTransactionType }
        assert adjustmentTransaction != null
        assert adjustmentTransaction.transactionNumber == "123ABC"
        assert adjustmentTransaction.source == facility
        assert adjustmentTransaction.inventory == facility.inventory
        assert adjustmentTransaction.cycleCount == cycleCount

        // We expect two entries because we had two discrepancy.
        List<TransactionEntry> entries = adjustmentTransaction.transactionEntries as List<TransactionEntry>
        assert entries.size() == 2

        TransactionEntry negativeTransactionEntry = entries.find{ it.binLocation.name == binNameNegativeAdjustment }
        assert negativeTransactionEntry != null
        assert negativeTransactionEntry.product == product
        assert negativeTransactionEntry.inventoryItem == inventoryItem
        assert negativeTransactionEntry.quantity == -1

        TransactionEntry positiveTransactionEntry = entries.find{ it.binLocation.name == binNamePositiveAdjustment }
        assert positiveTransactionEntry != null
        assert positiveTransactionEntry.product == product
        assert negativeTransactionEntry.inventoryItem == inventoryItem
        assert positiveTransactionEntry.quantity == 3
    }

    private Transaction createExpectedProductInventoryTransaction(Location facility, Product product, Date date) {
        // This is mocked data so we don't really care about any of the other fields, not even the transaction entries.
        Transaction transaction = new Transaction(
                transactionType: productInventoryTransactionType,
        )

        productInventoryTransactionServiceStub.createTransaction(
                facility, product, ProductInventorySnapshotSource.CYCLE_COUNT, date) >> transaction

        return transaction
    }
}
