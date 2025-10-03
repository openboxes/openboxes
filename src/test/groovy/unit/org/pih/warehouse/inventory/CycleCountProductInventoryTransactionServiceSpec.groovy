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
import org.pih.warehouse.inventory.product.availability.AvailableItemMap
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
        AvailableItemMap availableItems = new AvailableItemMap()
        availableItems.putAll([
                new AvailableItem(
                        inventoryItem: new InventoryItem(product: product, lotNumber: "lot1"),
                        binLocation: new Location(name: "bin1"),
                        quantityOnHand: 50,
                ),
                new AvailableItem(
                        inventoryItem: new InventoryItem(product: product, lotNumber: "lot2"),
                        binLocation: new Location(name: "bin2"),
                        quantityOnHand: 25,
                )
        ])
        productAvailabilityServiceStub.getAvailableItemsAtDateAsMap(facility, [product], null) >> availableItems

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

        TransactionEntry transactionEntryLot1 = transactionEntries.find { it.inventoryItem.lotNumber == "lot1" }
        assert transactionEntryLot1.quantity == 50
        assert transactionEntryLot1.binLocation.name == "bin1"

        TransactionEntry transactionEntryLot2 = transactionEntries.find { it.inventoryItem.lotNumber == "lot2" }
        assert transactionEntryLot2.quantity == 25
        assert transactionEntryLot2.binLocation.name == "bin2"
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
        AvailableItemMap availableItems = new AvailableItemMap()
        availableItems.putAll([
                new AvailableItem(
                        inventoryItem: new InventoryItem(product: product, lotNumber: "lot1"),
                        binLocation: new Location(name: "bin1"),
                        quantityOnHand: 50,
                ),
                new AvailableItem(
                        inventoryItem: new InventoryItem(product: product, lotNumber: "lot2"),
                        binLocation: new Location(name: "bin2"),
                        quantityOnHand: 25,
                )
        ])
        productAvailabilityServiceStub.getAvailableItemsAtDateAsMap(
                facility, [product], transactionDate) >> availableItems

        and: 'no other transactions exist at that time'
        inventoryServiceStub.hasTransactionEntriesOnDate(facility, transactionDate, [product]) >> false

        when: 'we create a baseline with date provided'
        Transaction transaction = cycleCountProductInventoryTransactionService.createInventoryBaselineTransaction(
                facility, cycleCount, [product], transactionDate, "TEST COMMENT")

        then: 'we should have entries for both items'
        assert transaction.transactionType == productInventoryTransactionType
        assert transaction.transactionDate == transactionDate
        assert transaction.transactionNumber == "123ABC"
        assert transaction.comment == "TEST COMMENT"

        List<TransactionEntry> transactionEntries = transaction.transactionEntries as List<TransactionEntry>
        assert transactionEntries.size() == 2

        TransactionEntry transactionEntryLot1 = transactionEntries.find { it.inventoryItem.lotNumber == "lot1" }
        assert transactionEntryLot1.quantity == 50
        assert transactionEntryLot1.binLocation.name == "bin1"

        TransactionEntry transactionEntryLot2 = transactionEntries.find { it.inventoryItem.lotNumber == "lot2" }
        assert transactionEntryLot2.quantity == 25
        assert transactionEntryLot2.binLocation.name == "bin2"
    }

    void 'createInventoryBaselineTransaction should create an empty baseline when there are no available items'() {
        given: 'mocked inputs'
        Location facility = new Location(inventory: new Inventory())
        Product product = new Product()
        CycleCount cycleCount = new CycleCount(cycleCountItems: [])

        and: 'a mocked transaction number'
        transactionIdentifierServiceStub.generate(_ as Transaction) >> "123ABC"

        and: 'no mocked available items'
        productAvailabilityServiceStub.getAvailableItemsAtDateAsMap(facility, [product], null) >> new AvailableItemMap()

        and: 'no other transactions exist at that time'
        inventoryServiceStub.hasTransactionEntriesOnDate(facility, _ as Date, []) >> false

        and: 'the default inventory item exists'
        inventoryServiceStub.findOrCreateDefaultInventoryItem(product) >> new InventoryItem(
                product: product, lotNumber: "DEFAULT")

        when:
        Transaction transaction = cycleCountProductInventoryTransactionService.createInventoryBaselineTransaction(
                facility, cycleCount, [product], null, null)

        then:
        assert transaction.transactionType == productInventoryTransactionType
        assert transaction.transactionNumber == "123ABC"

        List<TransactionEntry> transactionEntries = transaction.transactionEntries as List<TransactionEntry>
        assert transactionEntries.size() == 1

        TransactionEntry transactionEntry = transactionEntries.get(0)
        assert transactionEntry.quantity == 0
        assert transactionEntry.inventoryItem.lotNumber == "DEFAULT"
        assert transactionEntry.binLocation == null
    }

    void 'createInventoryBaselineTransaction should create entries for zero-stock product when also given product with stock'() {
        given: 'mocked inputs'
        Location facility = new Location(inventory: new Inventory())
        Product product1 = new Product()
        product1.id = 1
        Product product2 = new Product()
        product2.id = 2
        CycleCount cycleCount = new CycleCount(cycleCountItems: [])

        and: 'a mocked transaction number'
        transactionIdentifierServiceStub.generate(_ as Transaction) >> "123ABC"

        and: 'an available item exists for only one of the two products'
        AvailableItemMap availableItems = new AvailableItemMap()
        availableItems.put(new AvailableItem(
                inventoryItem: new InventoryItem(product: product1, lotNumber: "lot1"),
                binLocation: new Location(),
                quantityOnHand: 50,
        ))
        productAvailabilityServiceStub.getAvailableItemsAtDateAsMap(
                facility, [product1, product2], null) >> availableItems

        and: 'no other transactions exist at that time'
        inventoryServiceStub.hasTransactionEntriesOnDate(facility, _ as Date, []) >> false

        and: 'the default inventory item exists'
        inventoryServiceStub.findOrCreateDefaultInventoryItem(product2) >> new InventoryItem(
                product: product2, lotNumber: "DEFAULT")

        when:
        Transaction transaction = cycleCountProductInventoryTransactionService.createInventoryBaselineTransaction(
                facility, cycleCount, [product1, product2], null, null)

        then: 'we should have entries for both products, one with zero stock'
        assert transaction.transactionType == productInventoryTransactionType
        assert transaction.transactionNumber == "123ABC"

        List<TransactionEntry> transactionEntries = transaction.transactionEntries as List<TransactionEntry>
        assert transactionEntries.size() == 2

        TransactionEntry transactionEntryProduct1 = transactionEntries.find { it.product == product1 }
        assert transactionEntryProduct1.quantity == 50
        assert transactionEntryProduct1.inventoryItem.lotNumber == "lot1"
        assert transactionEntryProduct1.binLocation != null

        TransactionEntry transactionEntryProduct2 = transactionEntries.find { it.product == product2 }
        assert transactionEntryProduct2.quantity == 0
        assert transactionEntryProduct2.inventoryItem.lotNumber == "DEFAULT"
        assert transactionEntryProduct2.binLocation == null
    }

    void 'createInventoryBaselineTransaction should fail when other transactions exist for that date'() {
        given: 'mocked inputs'
        Location facility = new Location(inventory: new Inventory())
        Product product = new Product()
        CycleCount cycleCount = new CycleCount(cycleCountItems: [])
        Date transactionDate = new Date()

        and: 'mocked available items'
        AvailableItemMap availableItems = new AvailableItemMap()
        availableItems.put(new AvailableItem(
                inventoryItem: new InventoryItem(product: product, lotNumber: "lot1"),
                binLocation: new Location(),
                quantityOnHand: 50,
        ))
        productAvailabilityServiceStub.getAvailableItemsAtDateAsMap(
                facility, [product], transactionDate) >> availableItems

        and: 'other transactions do exist at that time'
        inventoryServiceStub.hasTransactionEntriesOnDate(facility, transactionDate, [product]) >> true

        when:
        cycleCountProductInventoryTransactionService.createInventoryBaselineTransaction(
                facility, cycleCount, [product], transactionDate, null)

        then:
        thrown(IllegalArgumentException)
    }
}
