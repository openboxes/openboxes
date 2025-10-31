package unit.org.pih.warehouse.inventory

import grails.testing.gorm.DataTest
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.CycleCount
import org.pih.warehouse.inventory.CycleCountItem
import org.pih.warehouse.inventory.CycleCountItemStatus
import org.pih.warehouse.inventory.CycleCountProductAvailabilityService
import org.pih.warehouse.inventory.CycleCountStatus
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.product.Product

@Unroll
class CycleCountProductAvailabilityServiceSpec extends Specification implements DataTest {

    @Shared
    CycleCountProductAvailabilityService cycleCountProductAvailabilityService

    @Shared
    ProductAvailabilityService productAvailabilityServiceStub

    void setupSpec() {
        mockDomains(CycleCount, CycleCountItem)
    }

    void setup() {
        cycleCountProductAvailabilityService = new CycleCountProductAvailabilityService()

        // Set up the stubs
        productAvailabilityServiceStub = Stub(ProductAvailabilityService)
        cycleCountProductAvailabilityService.productAvailabilityService = productAvailabilityServiceStub

        GroovyMock(AuthService, global: true)
        AuthService.currentUser >> new User()
    }

    void 'refreshProductAvailability should not change items if QoH has not changed'() {
        given: 'a cycle count'
        Location facility = new Location()
        Product product = new Product()
        InventoryItem inventoryItem = new InventoryItem(product: product, lotNumber: 'lotNumber')
        Location binLocation = new Location(name: 'binLocation')
        CycleCount cycleCount = new CycleCount(
                facility: facility,
                status: CycleCountStatus.INVESTIGATING,
                cycleCountItems: [
                        new CycleCountItem(
                                inventoryItem: inventoryItem,
                                location: binLocation,
                                product: product,
                                countIndex: 1,
                                quantityOnHand: 20,
                                custom: false,
                                status: CycleCountItemStatus.COUNTED
                        ),
                ]
        )

        and: 'mocked available items'
        List<AvailableItem> availableItems = [
                new AvailableItem(
                        inventoryItem: inventoryItem,
                        binLocation: binLocation,
                        quantityOnHand: 20,  // == QoH in the cycle count item
                ),
        ]
        productAvailabilityServiceStub.getAvailableItems(facility, [product.id], false, true) >> availableItems

        when: 'we refresh product availability'
        CycleCountProductAvailabilityService.CycleCountItemsForRefresh changedItems =
                cycleCountProductAvailabilityService.refreshProductAvailability(cycleCount, false, 1)

        then: 'items should not have changed'
        assert !changedItems.itemsHaveChanged()
        assert cycleCount.cycleCountItems.size() == 1

        CycleCountItem cycleCountItem = cycleCount.getCycleCountItem(product, binLocation, inventoryItem, 1)
        assert cycleCountItem.quantityOnHand == 20  // QoH is unchanged
    }

    void 'refreshProductAvailability should update QoH of an item when it has changed'() {
        given: 'a cycle count'
        Location facility = new Location()
        Product product = new Product()
        InventoryItem inventoryItem = new InventoryItem(product: product, lotNumber: 'lotNumber')
        Location binLocation = new Location(name: 'binLocation')
        CycleCount cycleCount = new CycleCount(
                facility: facility,
                status: CycleCountStatus.INVESTIGATING,
                cycleCountItems: [
                        new CycleCountItem(
                                inventoryItem: inventoryItem,
                                location: binLocation,
                                product: product,
                                countIndex: 1,
                                quantityOnHand: 20,
                                custom: false,
                                status: CycleCountItemStatus.COUNTED
                        ),
                ]
        )

        and: 'mocked available items'
        List<AvailableItem> availableItems = [
                new AvailableItem(
                        inventoryItem: inventoryItem,
                        binLocation: binLocation,
                        quantityOnHand: 30,  // QoH has changed (+10)
                ),
        ]
        productAvailabilityServiceStub.getAvailableItems(facility, [product.id], false, true) >> availableItems

        when: 'we refresh product availability'
        CycleCountProductAvailabilityService.CycleCountItemsForRefresh changedItems =
                cycleCountProductAvailabilityService.refreshProductAvailability(cycleCount, false, 1)

        then: 'items should have changed'
        assert changedItems.itemsHaveChanged()
        assert cycleCount.cycleCountItems.size() == 1

        CycleCountItem cycleCountItem = cycleCount.getCycleCountItem(product, binLocation, inventoryItem, 1)
        assert cycleCountItem.quantityOnHand == 30  // QoH is updated
    }

    void 'refreshProductAvailability should add new items to the count'() {
        given: 'a cycle count'
        Location facility = new Location()
        Product product = new Product()
        InventoryItem existingInventoryItem = new InventoryItem(product: product, lotNumber: 'existingLotNumber')
        Location existingBinLocation = new Location(name: 'existingBinLocation')
        Date existingDateCounted = new Date()
        User existingAssignee = new User()
        InventoryItem newInventoryItem = new InventoryItem(product: product, lotNumber: 'newLotNumber')
        Location newBinLocation = new Location(name: 'newBinLocation')
        CycleCount cycleCount = new CycleCount(
                facility: facility,
                status: CycleCountStatus.INVESTIGATING,
                cycleCountItems: [
                        new CycleCountItem(
                                inventoryItem: existingInventoryItem,
                                location: existingBinLocation,
                                product: product,
                                countIndex: 0,
                                quantityOnHand: 20,
                                custom: false,
                                // The new item will copy these fields
                                dateCounted: existingDateCounted,
                                assignee: existingAssignee,
                                status: CycleCountItemStatus.COUNTED
                        ),
                ]
        )

        and: 'mocked available items'
        List<AvailableItem> availableItems = [
                new AvailableItem(
                        inventoryItem: existingInventoryItem,
                        binLocation: existingBinLocation,
                        quantityOnHand: 20,  // QoH is unchanged
                ),
                new AvailableItem(
                        inventoryItem: newInventoryItem,
                        binLocation: newBinLocation,
                        quantityOnHand: 30,
                ),
        ]
        productAvailabilityServiceStub.getAvailableItems(facility, [product.id], false, true) >> availableItems

        when: 'we refresh product availability'
        CycleCountProductAvailabilityService.CycleCountItemsForRefresh changedItems =
                cycleCountProductAvailabilityService.refreshProductAvailability(cycleCount)

        then: 'items should have changed'
        assert changedItems.itemsHaveChanged()
        assert cycleCount.cycleCountItems.size() == 2

        CycleCountItem existingCycleCountItem = cycleCount.getCycleCountItem(
                product, existingBinLocation, existingInventoryItem, 0)
        assert existingCycleCountItem.quantityOnHand == 20  // QoH is unchanged

        CycleCountItem newCycleCountItem = cycleCount.getCycleCountItem(product, newBinLocation, newInventoryItem, 0)
        assert newCycleCountItem.status == CycleCountItemStatus.INVESTIGATING
        assert newCycleCountItem.countIndex == 0
        assert newCycleCountItem.quantityOnHand == 30
        assert newCycleCountItem.quantityCounted == null
        assert newCycleCountItem.facility == facility
        assert newCycleCountItem.product == product
        assert newCycleCountItem.dateCounted == existingDateCounted
        assert newCycleCountItem.custom == false
        assert newCycleCountItem.assignee == existingAssignee
    }

    void 'refreshProductAvailability should remove an item from the count when QoH becomes 0'() {
        given: 'a cycle count'
        Location facility = new Location()
        Product product = new Product()
        InventoryItem inventoryItem = new InventoryItem(product: product, lotNumber: 'lotNumber')
        Location binLocation = new Location(name: 'binLocation')
        CycleCount cycleCount = new CycleCount(
                facility: facility,
                status: CycleCountStatus.INVESTIGATING,
                cycleCountItems: [
                        new CycleCountItem(
                                inventoryItem: inventoryItem,
                                location: binLocation,
                                product: product,
                                countIndex: 0,
                                quantityOnHand: 20,
                                custom: false,
                                status: CycleCountItemStatus.COUNTED
                        ),
                ]
        )

        and: 'mocked available items'
        List<AvailableItem> availableItems = [
                new AvailableItem(
                        inventoryItem: inventoryItem,
                        binLocation: binLocation,
                        quantityOnHand: 0,  // QoH is zero now
                ),
        ]
        productAvailabilityServiceStub.getAvailableItems(facility, [product.id], false, true) >> availableItems

        when: 'we refresh product availability'
        CycleCountProductAvailabilityService.CycleCountItemsForRefresh changedItems =
                cycleCountProductAvailabilityService.refreshProductAvailability(cycleCount)

        then: 'items should have changed'
        assert changedItems.itemsHaveChanged()
        assert cycleCount.cycleCountItems.size() == 0  // The item has been removed!
    }

    void 'refreshProductAvailability should remove an item from the count when not in available items'() {
        given: 'a cycle count'
        Location facility = new Location()
        Product product = new Product()
        InventoryItem inventoryItem = new InventoryItem(product: product, lotNumber: 'lotNumber')
        Location binLocation = new Location(name: 'binLocation')
        CycleCount cycleCount = new CycleCount(
                facility: facility,
                status: CycleCountStatus.INVESTIGATING,
                cycleCountItems: [
                        new CycleCountItem(
                                inventoryItem: inventoryItem,
                                location: binLocation,
                                product: product,
                                countIndex: 0,
                                quantityOnHand: 20,
                                custom: false,
                                status: CycleCountItemStatus.COUNTED
                        ),
                ]
        )

        and: 'mocked available items that do not contain the item'
        productAvailabilityServiceStub.getAvailableItems(facility, [product.id], false, true) >> []

        when: 'we refresh product availability'
        CycleCountProductAvailabilityService.CycleCountItemsForRefresh changedItems =
                cycleCountProductAvailabilityService.refreshProductAvailability(cycleCount)

        then: 'items should have changed'
        assert changedItems.itemsHaveChanged()
        assert cycleCount.cycleCountItems.size() == 0  // The item has been removed!
    }

    void 'refreshProductAvailability should only remove items from the most recent count when not in available items'() {
        given: 'a cycle count with a recount'
        Location facility = new Location()
        Product product = new Product()
        InventoryItem inventoryItem = new InventoryItem(product: product, lotNumber: 'lotNumber')
        Location binLocation = new Location(name: 'binLocation')
        CycleCountItem countItem = new CycleCountItem(
                inventoryItem: inventoryItem,
                location: binLocation,
                product: product,
                countIndex: 0,  // count
                quantityOnHand: 20,
                custom: false,
                status: CycleCountItemStatus.COUNTED
        )
        countItem.id = '0'
        CycleCountItem recountItem = new CycleCountItem(
                inventoryItem: inventoryItem,
                location: binLocation,
                product: product,
                countIndex: 1,  // recount
                quantityOnHand: 30,
                custom: false,
                status: CycleCountItemStatus.INVESTIGATING
        )
        recountItem.id = '1'
        CycleCount cycleCount = new CycleCount(
                facility: facility,
                status: CycleCountStatus.INVESTIGATING,
                cycleCountItems: [countItem, recountItem]
        )

        and: 'mocked available items that do not contain the item'
        productAvailabilityServiceStub.getAvailableItems(facility, [product.id], false, true) >> []

        when: 'we refresh product availability'
        CycleCountProductAvailabilityService.CycleCountItemsForRefresh changedItems =
                cycleCountProductAvailabilityService.refreshProductAvailability(cycleCount)

        then: 'items should have changed'
        assert changedItems.itemsHaveChanged()
        assert cycleCount.cycleCountItems.size() == 1  // Only the recount item has been removed!

        CycleCountItem countCycleCountItem = cycleCount.getCycleCountItem(product, binLocation, inventoryItem, 0)
        assert countCycleCountItem.quantityOnHand == 20  // QoH is unchanged
    }

    void 'refreshProductAvailability should update QoH for custom count items when available items is deleted'() {
        given: 'a cycle count'
        Location facility = new Location()
        Product product = new Product()
        InventoryItem inventoryItem = new InventoryItem(product: product, lotNumber: 'lotNumber')
        Location binLocation = new Location(name: 'binLocation')
        CycleCount cycleCount = new CycleCount(
                facility: facility,
                status: CycleCountStatus.INVESTIGATING,
                cycleCountItems: [
                        new CycleCountItem(
                                inventoryItem: inventoryItem,
                                location: binLocation,
                                product: product,
                                countIndex: 0,
                                quantityOnHand: 20,
                                custom: true,  // This is a custom row
                                status: CycleCountItemStatus.COUNTED
                        ),
                ]
        )

        and: 'mocked available items that do not contain the item'
        productAvailabilityServiceStub.getAvailableItems(facility, [product.id], false, true) >> []

        when: 'we refresh product availability'
        CycleCountProductAvailabilityService.CycleCountItemsForRefresh changedItems =
                cycleCountProductAvailabilityService.refreshProductAvailability(cycleCount)

        then: 'items should have changed'
        assert changedItems.itemsHaveChanged()
        assert cycleCount.cycleCountItems.size() == 1  // The item has not been removed!

        CycleCountItem cycleCountItem = cycleCount.getCycleCountItem(product, binLocation, inventoryItem, 0)
        assert cycleCountItem.quantityOnHand == 0  // QoH is changed
    }

    void 'refreshProductAvailability should update QoH for custom count items when QoH becomes 0'() {
        given: 'a cycle count'
        Location facility = new Location()
        Product product = new Product()
        InventoryItem inventoryItem = new InventoryItem(product: product, lotNumber: 'lotNumber')
        Location binLocation = new Location(name: 'binLocation')
        CycleCount cycleCount = new CycleCount(
                facility: facility,
                status: CycleCountStatus.INVESTIGATING,
                cycleCountItems: [
                        new CycleCountItem(
                                inventoryItem: inventoryItem,
                                location: binLocation,
                                product: product,
                                countIndex: 0,
                                quantityOnHand: 20,
                                custom: true,  // This is a custom row
                                status: CycleCountItemStatus.COUNTED
                        ),
                ]
        )

        and: 'mocked available items'
        List<AvailableItem> availableItems = [
                new AvailableItem(
                        inventoryItem: inventoryItem,
                        binLocation: binLocation,
                        quantityOnHand: 0,  // QoH is zero now
                ),
        ]
        productAvailabilityServiceStub.getAvailableItems(facility, [product.id], false, true) >> availableItems

        when: 'we refresh product availability'
        CycleCountProductAvailabilityService.CycleCountItemsForRefresh changedItems =
                cycleCountProductAvailabilityService.refreshProductAvailability(cycleCount)

        then: 'items should have changed'
        assert changedItems.itemsHaveChanged()
        assert cycleCount.cycleCountItems.size() == 1  // The item has not been removed!

        CycleCountItem cycleCountItem = cycleCount.getCycleCountItem(product, binLocation, inventoryItem, 0)
        assert cycleCountItem.quantityOnHand == 0  // QoH is changed
    }

    void 'refreshProductAvailability should only update QoH for the most recent count'() {
        given: 'a cycle count'
        Location facility = new Location()
        Product product = new Product()
        InventoryItem inventoryItem = new InventoryItem(product: product, lotNumber: 'lotNumber')
        Location binLocation = new Location(name: 'binLocation')
        CycleCountItem countItem = new CycleCountItem(
                inventoryItem: inventoryItem,
                location: binLocation,
                product: product,
                countIndex: 0,  // count
                quantityOnHand: 20,
                custom: false,
                status: CycleCountItemStatus.COUNTED
        )
        countItem.id = '0'
        CycleCountItem recountItem = new CycleCountItem(
                inventoryItem: inventoryItem,
                location: binLocation,
                product: product,
                countIndex: 1,  // recount
                quantityOnHand: 30,
                custom: false,
                status: CycleCountItemStatus.INVESTIGATING
        )
        recountItem.id = '1'
        CycleCount cycleCount = new CycleCount(
                facility: facility,
                status: CycleCountStatus.INVESTIGATING,
                cycleCountItems: [countItem, recountItem]
        )

        and: 'mocked available items'
        List<AvailableItem> availableItems = [
                new AvailableItem(
                        inventoryItem: inventoryItem,
                        binLocation: binLocation,
                        quantityOnHand: 40,  // QoH is changed (+10)
                ),
        ]
        productAvailabilityServiceStub.getAvailableItems(facility, [product.id], false, true) >> availableItems

        when: 'we refresh product availability'
        CycleCountProductAvailabilityService.CycleCountItemsForRefresh changedItems =
                cycleCountProductAvailabilityService.refreshProductAvailability(cycleCount)

        then: 'items should have changed'
        assert changedItems.itemsHaveChanged()
        assert cycleCount.cycleCountItems.size() == 2

        CycleCountItem countCycleCountItem = cycleCount.getCycleCountItem(product, binLocation, inventoryItem, 0)
        assert countCycleCountItem.quantityOnHand == 20  // QoH is unchanged

        CycleCountItem recountCycleCountItem = cycleCount.getCycleCountItem(product, binLocation, inventoryItem, 1)
        assert recountCycleCountItem.quantityOnHand == 40  // QoH is updated
    }

    void 'OBPIH-7097 refreshProductAvailability should not remove items that were custom added during a count when refreshing during recount'() {
        given: 'a cycle count'
        Location facility = new Location()
        Product product = new Product()
        InventoryItem inventoryItem = new InventoryItem(product: product, lotNumber: 'lotNumber')
        Location binLocation = new Location(name: 'binLocation')
        CycleCount cycleCount = new CycleCount(
                facility: facility,
                status: CycleCountStatus.INVESTIGATING,
                cycleCountItems: [
                        new CycleCountItem(
                                inventoryItem: inventoryItem,
                                location: binLocation,
                                product: product,
                                countIndex: 0,  // count
                                quantityOnHand: 0,
                                custom: true,  // this is a custom row
                                status: CycleCountItemStatus.COUNTED
                        ),
                        new CycleCountItem(
                                inventoryItem: inventoryItem,
                                location: binLocation,
                                product: product,
                                countIndex: 1,  // recount
                                quantityOnHand: 0,
                                custom: false,  // this is NOT a custom row
                                status: CycleCountItemStatus.INVESTIGATING
                        ),
                ]
        )

        and: 'mocked available items that do not contain the item'
        productAvailabilityServiceStub.getAvailableItems(facility, [product.id], false, true) >> []

        when: 'we refresh product availability'
        CycleCountProductAvailabilityService.CycleCountItemsForRefresh changedItems =
                cycleCountProductAvailabilityService.refreshProductAvailability(cycleCount)

        then: 'items should not have changed'
        assert !changedItems.itemsHaveChanged()
    }
}
