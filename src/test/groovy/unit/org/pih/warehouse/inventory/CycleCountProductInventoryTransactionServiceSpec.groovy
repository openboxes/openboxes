package unit.org.pih.warehouse.inventory

import grails.testing.gorm.DataTest
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.CycleCount
import org.pih.warehouse.inventory.CycleCountProductInventoryTransactionService
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionIdentifierService
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.product.Product

@Unroll
class CycleCountProductInventoryTransactionServiceSpec extends Specification implements DataTest {

    @Shared
    CycleCountProductInventoryTransactionService cycleCountProductInventoryTransactionService

    @Shared
    InventoryService inventoryServiceStub

    @Shared
    ProductAvailabilityService productAvailabilityServiceStub

    @Shared
    TransactionIdentifierService transactionIdentifierServiceStub

    @Shared
    TransactionType productInventoryTransactionType

    void setupSpec() {
        mockDomains(Transaction, TransactionEntry, TransactionType)
    }

    void setup() {
        cycleCountProductInventoryTransactionService = new CycleCountProductInventoryTransactionService()

        inventoryServiceStub = Stub(InventoryService)
        cycleCountProductInventoryTransactionService.inventoryService = inventoryServiceStub

        productAvailabilityServiceStub = Stub(ProductAvailabilityService)
        cycleCountProductInventoryTransactionService.productAvailabilityService = productAvailabilityServiceStub

        transactionIdentifierServiceStub = Stub(TransactionIdentifierService)
        cycleCountProductInventoryTransactionService.transactionIdentifierService = transactionIdentifierServiceStub

        // Set up the transaction types
        productInventoryTransactionType = new TransactionType()
        productInventoryTransactionType.id = Constants.INVENTORY_BASELINE_TRANSACTION_TYPE_ID
        productInventoryTransactionType.save(validate: false)
    }

    void 'createInventoryBaselineTransaction should succeed when some items are available and date is not provided'() {
        given: 'mocked inputs'
        Location facility = new Location(inventory: new Inventory())
        Product product = new Product()
        CycleCount cycleCount = new CycleCount(cycleCountItems: [])

        and: 'a date right before we create the transaction'
        Date date = new Date()

        and: 'a mocked transaction number'
        transactionIdentifierServiceStub.generate(_ as Transaction) >> "123ABC"

        and: 'mocked available items'
        List<AvailableItem> availableItems = [
                new AvailableItem(
                        inventoryItem: new InventoryItem(product: product),
                        binLocation: new Location(),
                        quantityOnHand: 50,
                ),
                new AvailableItem(
                        inventoryItem: new InventoryItem(product: product),
                        binLocation: new Location(),
                        quantityOnHand: 25,
                )
        ]
        productAvailabilityServiceStub.getAvailableItemsAtDate(facility, [product], null) >> availableItems

        and: 'no other transactions exist at that time'
        inventoryServiceStub.hasTransactionEntriesOnDate(facility, _ as Date, [product]) >> false

        when: 'we create a baseline with no date provided'
        Transaction transaction = cycleCountProductInventoryTransactionService.createInventoryBaselineTransaction(
                facility, cycleCount, [product], null, "TEST COMMENT")

        then:
        assert transaction.transactionType == productInventoryTransactionType
        assert transaction.transactionDate >= date  // The transaction date should be "now"
        assert transaction.transactionNumber == "123ABC"
        assert transaction.comment == "TEST COMMENT"

        List<TransactionEntry> transactionEntries = transaction.transactionEntries as List<TransactionEntry>
        assert transactionEntries.size() == 2
        assert transactionEntries.collect{ it.quantity }.containsAll([50, 25])
    }

    void 'createInventoryBaselineTransaction should succeed when some items are available and date is provided'() {
        given: 'mocked inputs'
        Location facility = new Location(inventory: new Inventory())
        Product product = new Product()
        CycleCount cycleCount = new CycleCount(cycleCountItems: [])
        Date transactionDate = new Date()

        and: 'a mocked transaction number'
        transactionIdentifierServiceStub.generate(_ as Transaction) >> "123ABC"

        and: 'mocked available items'
        List<AvailableItem> availableItems = [
                new AvailableItem(
                        inventoryItem: new InventoryItem(product: product),
                        binLocation: new Location(),
                        quantityOnHand: 50,
                ),
                new AvailableItem(
                        inventoryItem: new InventoryItem(product: product),
                        binLocation: new Location(),
                        quantityOnHand: 25,
                )
        ]
        productAvailabilityServiceStub.getAvailableItemsAtDate(facility, [product], transactionDate) >> availableItems

        and: 'no other transactions exist at that time'
        inventoryServiceStub.hasTransactionEntriesOnDate(facility, transactionDate, [product]) >> false

        when: 'we create a baseline with date provided'
        Transaction transaction = cycleCountProductInventoryTransactionService.createInventoryBaselineTransaction(
                facility, cycleCount, [product], transactionDate, "TEST COMMENT")

        then:
        assert transaction.transactionType == productInventoryTransactionType
        assert transaction.transactionDate == transactionDate
        assert transaction.transactionNumber == "123ABC"
        assert transaction.comment == "TEST COMMENT"

        List<TransactionEntry> transactionEntries = transaction.transactionEntries as List<TransactionEntry>
        assert transactionEntries.size() == 2
        assert transactionEntries.collect{ it.quantity }.containsAll([50, 25])
    }

    void 'createInventoryBaselineTransaction should do nothing when there are no available items'() {
        given: 'mocked inputs'
        Location facility = new Location(inventory: new Inventory())
        Product product = new Product()
        CycleCount cycleCount = new CycleCount(cycleCountItems: [])

        and: 'no mocked available items'
        productAvailabilityServiceStub.getAvailableItemsAtDate(facility, [product], null) >> []

        and: 'no other transactions exist at that time'
        inventoryServiceStub.hasTransactionEntriesOnDate(facility, _ as Date, []) >> false

        expect:
        assert cycleCountProductInventoryTransactionService.createInventoryBaselineTransaction(
                facility, cycleCount, [product], null, null) == null
    }

    void 'createInventoryBaselineTransaction should fail when other transactions exist for that date'() {
        given: 'mocked inputs'
        Location facility = new Location(inventory: new Inventory())
        Product product = new Product()
        CycleCount cycleCount = new CycleCount(cycleCountItems: [])
        Date transactionDate = new Date()

        and: 'mocked available items'
        List<AvailableItem> availableItems = [
                new AvailableItem(
                        inventoryItem: new InventoryItem(product: product),
                        binLocation: new Location(),
                        quantityOnHand: 50,
                ),
        ]
        productAvailabilityServiceStub.getAvailableItemsAtDate(facility, [product], transactionDate) >> availableItems

        and: 'other transactions do exist at that time'
        inventoryServiceStub.hasTransactionEntriesOnDate(facility, transactionDate, [product]) >> true

        when:
        cycleCountProductInventoryTransactionService.createInventoryBaselineTransaction(
                facility, cycleCount, [product], transactionDate, null)

        then:
        thrown(IllegalArgumentException)
    }
}
