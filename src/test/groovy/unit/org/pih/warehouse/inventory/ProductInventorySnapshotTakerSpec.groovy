package unit.org.pih.warehouse.inventory

import grails.testing.gorm.DataTest
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.CycleCount
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.inventory.ProductInventorySnapshotSource
import org.pih.warehouse.inventory.ProductInventorySnapshotTaker
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionIdentifierService
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.product.Product

@Unroll
class ProductInventorySnapshotTakerSpec extends Specification implements DataTest {

    @Shared
    ProductInventorySnapshotTaker productInventorySnapshotTaker

    @Shared
    ProductAvailabilityService productAvailabilityServiceStub

    @Shared
    TransactionIdentifierService transactionIdentifierServiceStub

    void setupSpec() {
        mockDomains(Transaction, TransactionEntry, TransactionType)
    }

    void setup() {
        productInventorySnapshotTaker = new ProductInventorySnapshotTaker()

        productAvailabilityServiceStub = Stub(ProductAvailabilityService)
        productInventorySnapshotTaker.productAvailabilityService = productAvailabilityServiceStub

        transactionIdentifierServiceStub = Stub(TransactionIdentifierService)
        productInventorySnapshotTaker.transactionIdentifierService = transactionIdentifierServiceStub
    }

    void 'createTransaction should succeed when some items are available'() {
        given: 'mocked inputs'
        Location facility = new Location(inventory: new Inventory())
        Product product = new Product()
        CycleCount cycleCount = new CycleCount()
        Date date = new Date()

        and: 'add the product inventory transaction type to the db because it is referenced in code'
        TransactionType transactionType = new TransactionType()
        transactionType.id = ProductInventorySnapshotSource.CYCLE_COUNT.transactionTypeId
        transactionType.save(validate: false)

        and: 'a mocked identifier'
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
        Transaction transaction = productInventorySnapshotTaker.createTransaction(
                facility, product, ProductInventorySnapshotSource.CYCLE_COUNT, cycleCount, date)

        then:
        assert transaction.transactionType == transactionType
        assert transaction.transactionDate == date
        assert transaction.source == facility
        assert transaction.transactionNumber == "123ABC"
        assert transaction.transactionEntries.size() == 2

        assert transaction.transactionEntries.collect{ it.quantity }.containsAll([50, 25])
    }
}
