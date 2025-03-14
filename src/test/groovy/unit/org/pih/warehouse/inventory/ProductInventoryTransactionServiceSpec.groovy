package unit.org.pih.warehouse.inventory

import grails.testing.gorm.DataTest
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.CycleCount
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
class ProductInventoryTransactionServiceSpec extends Specification implements DataTest {

    @Shared
    ProductInventoryTransactionService productInventoryTransactionService

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
        productInventoryTransactionService = new ProductInventoryTransactionService()

        productAvailabilityServiceStub = Stub(ProductAvailabilityService)
        productInventoryTransactionService.productAvailabilityService = productAvailabilityServiceStub

        transactionIdentifierServiceStub = Stub(TransactionIdentifierService)
        productInventoryTransactionService.transactionIdentifierService = transactionIdentifierServiceStub

        // Set up the transaction types
        productInventoryTransactionType = new TransactionType()
        productInventoryTransactionType.id = Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID
        productInventoryTransactionType.save(validate: false)
    }

    void 'createTransaction should succeed when some items are available'() {
        given: 'mocked inputs'
        Location facility = new Location(inventory: new Inventory())
        Product product = new Product()
        CycleCount cycleCount = new CycleCount(cycleCountItems: [])
        Date date = new Date()

        and: 'a mocked transaction number'
        transactionIdentifierServiceStub.generate(_ as Transaction) >> "123ABC"

        and: 'mocked available items'
        List<AvailableItem> availableItems = [
                new AvailableItem(
                        inventoryItem: new InventoryItem(),
                        binLocation: new Location(),
                        quantityOnHand: 50,
                ),
                new AvailableItem(
                        inventoryItem: new InventoryItem(),
                        binLocation: new Location(),
                        quantityOnHand: 25,
                )
        ]
        productAvailabilityServiceStub.getAvailableItems(
                facility, [product.id], false, true) >> availableItems

        when:
        Transaction transaction = productInventoryTransactionService.createTransaction(
                facility, product, ProductInventorySnapshotSource.CYCLE_COUNT, cycleCount, date)

        then:
        assert transaction.transactionType == productInventoryTransactionType
        assert transaction.transactionDate == date
        assert transaction.source == facility
        assert transaction.transactionNumber == "123ABC"

        List<TransactionEntry> transactionEntries = transaction.transactionEntries as List<TransactionEntry>
        assert transactionEntries.size() == 2
        assert transactionEntries.collect{ it.quantity }.containsAll([50, 25])
    }
}
