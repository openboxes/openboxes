// Companion spec: extra features for upstream's CycleCountProductAvailabilityServiceSpec (the upstream file stays authoritative).
package org.pih.warehouse.regression.platform.inventory

import spock.lang.PendingFeature

import org.pih.warehouse.inventory.*
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
class CycleCountProductAvailabilityServiceMoreSpec extends Specification implements DataTest {

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

    void 'getAvailableItems asks for the non-zero stock of the given products at the facility'() {
        given:
        ProductAvailabilityService productAvailabilityServiceMock = Mock()
        cycleCountProductAvailabilityService.productAvailabilityService = productAvailabilityServiceMock
        Location facility = new Location()
        Product product = new Product()
        product.id = 'product-1'
        List<AvailableItem> availableItems = [new AvailableItem(quantityOnHand: 5)]

        when:
        List<AvailableItem> result = cycleCountProductAvailabilityService.getAvailableItems(facility, product)

        then:
        1 * productAvailabilityServiceMock.getAvailableItems(facility, ['product-1'], false, true) >> availableItems
        result == availableItems
    }

    void 'refreshProductAvailability keeps a counted item that is no longer available but zeroes its QoH'() {
        given: 'a cycle count with an item that has already been counted'
        Location facility = new Location()
        Product product = new Product()
        InventoryItem inventoryItem = new InventoryItem(product: product, lotNumber: 'lotNumber')
        Location binLocation = new Location(name: 'binLocation')
        CycleCount cycleCount = new CycleCount(
                facility: facility,
                status: CycleCountStatus.COUNTED,
                cycleCountItems: [
                        new CycleCountItem(
                                inventoryItem: inventoryItem,
                                location: binLocation,
                                product: product,
                                countIndex: 0,
                                quantityOnHand: 20,
                                quantityCounted: 15,
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

        then: 'the counted item stays in the count with a QoH of zero'
        assert changedItems.itemsToDelete.isEmpty()
        assert changedItems.itemsToUpdate.size() == 1
        assert cycleCount.cycleCountItems.size() == 1

        CycleCountItem cycleCountItem = cycleCount.getCycleCountItem(product, binLocation, inventoryItem, 0)
        assert cycleCountItem.quantityOnHand == 0
        assert cycleCountItem.quantityCounted == 15  // The count itself is untouched
    }

    void 'refreshProductAvailability removes a counted item that is no longer available when asked to remove out of stock items'() {
        given: 'a cycle count with an item that has already been counted'
        Location facility = new Location()
        Product product = new Product()
        InventoryItem inventoryItem = new InventoryItem(product: product, lotNumber: 'lotNumber')
        Location binLocation = new Location(name: 'binLocation')
        CycleCountItem countedItem = new CycleCountItem(
                inventoryItem: inventoryItem,
                location: binLocation,
                product: product,
                countIndex: 0,
                quantityOnHand: 20,
                quantityCounted: 15,
                custom: false,
                status: CycleCountItemStatus.COUNTED
        )
        CycleCount cycleCount = new CycleCount(
                facility: facility,
                status: CycleCountStatus.COUNTED,
                cycleCountItems: [countedItem]
        )

        and: 'mocked available items that do not contain the item'
        productAvailabilityServiceStub.getAvailableItems(facility, [product.id], false, true) >> []

        when: 'we refresh product availability, removing out of stock items implicitly'
        CycleCountProductAvailabilityService.CycleCountItemsForRefresh changedItems =
                cycleCountProductAvailabilityService.refreshProductAvailability(cycleCount, true)

        then: 'the item has been removed'
        assert changedItems.itemsToDelete == [countedItem]
        assert cycleCount.cycleCountItems.isEmpty()
    }

    void 'refreshProductAvailability adds new items to a count in progress as counting, pre-counting out of stock ones'() {
        given: 'a cycle count that is being counted'
        Location facility = new Location()
        Product product = new Product()
        InventoryItem existingInventoryItem = new InventoryItem(product: product, lotNumber: 'existingLotNumber')
        Location binLocation = new Location(name: 'binLocation')
        InventoryItem emptyInventoryItem = new InventoryItem(product: product, lotNumber: 'emptyLotNumber')
        // Inventory items are equal by id, so the two lots need distinct ids to be told apart
        existingInventoryItem.id = 'existing'
        emptyInventoryItem.id = 'empty'
        CycleCount cycleCount = new CycleCount(
                facility: facility,
                status: CycleCountStatus.COUNTING,
                cycleCountItems: [
                        new CycleCountItem(
                                inventoryItem: existingInventoryItem,
                                location: binLocation,
                                product: product,
                                countIndex: 0,
                                quantityOnHand: 20,
                                custom: false,
                                status: CycleCountItemStatus.COUNTING
                        ),
                ]
        )

        and: 'mocked available items with a new lot that has no stock'
        productAvailabilityServiceStub.getAvailableItems(facility, [product.id], false, true) >> [
                new AvailableItem(inventoryItem: existingInventoryItem, binLocation: binLocation, quantityOnHand: 20),
                new AvailableItem(inventoryItem: emptyInventoryItem, binLocation: binLocation, quantityOnHand: 0),
        ]

        when: 'we refresh product availability'
        Date before = new Date()
        CycleCountProductAvailabilityService.CycleCountItemsForRefresh changedItems =
                cycleCountProductAvailabilityService.refreshProductAvailability(cycleCount)
        Date after = new Date()

        then: 'the new item is counting, already counted as zero, and dated now since no item had a count date'
        assert changedItems.itemsToCreate*.inventoryItem == [emptyInventoryItem]
        CycleCountItem newCycleCountItem = cycleCount.getCycleCountItem(product, binLocation, emptyInventoryItem, 0)
        assert newCycleCountItem.status == CycleCountItemStatus.COUNTING
        assert newCycleCountItem.quantityOnHand == 0
        assert newCycleCountItem.quantityCounted == 0
        assert newCycleCountItem.assignee == null
        assert !newCycleCountItem.dateCounted.before(before) && !newCycleCountItem.dateCounted.after(after)
        assert cycleCount.status == CycleCountStatus.COUNTING
    }

    void 'refreshProductAvailability can refill a later count whose items have all been removed'() {
        given: 'a cycle count whose recount (index 1) has no items left'
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
                                quantityCounted: 10,
                                custom: false,
                                status: CycleCountItemStatus.COUNTED
                        ),
                ]
        )

        and: 'stock has come back'
        productAvailabilityServiceStub.getAvailableItems(facility, [product.id], false, true) >> [
                new AvailableItem(inventoryItem: inventoryItem, binLocation: binLocation, quantityOnHand: 25),
        ]

        when: 'we refresh the recount explicitly'
        CycleCountProductAvailabilityService.CycleCountItemsForRefresh changedItems =
                cycleCountProductAvailabilityService.refreshProductAvailability(cycleCount, false, 1)

        then: 'a recount item is created and the original count is left alone'
        assert changedItems.itemsToCreate.size() == 1
        assert cycleCount.cycleCountItems.size() == 2
        CycleCountItem recountItem = cycleCount.getCycleCountItem(product, binLocation, inventoryItem, 1)
        assert recountItem.status == CycleCountItemStatus.INVESTIGATING
        assert recountItem.quantityOnHand == 25
        assert recountItem.quantityCounted == null
        assert cycleCount.getCycleCountItem(product, binLocation, inventoryItem, 0).quantityOnHand == 20
        assert cycleCount.status == CycleCountStatus.INVESTIGATING
    }

    @PendingFeature(reason = 'OBAF treats an explicit countIndex 0 as missing and calls first() on the empty count')
    void 'refreshProductAvailability can refill a first count (index 0) whose items have all been removed'() {
        given: 'a cycle count with no items left in its first count'
        Location facility = new Location()
        Product product = new Product()
        InventoryItem inventoryItem = new InventoryItem(product: product, lotNumber: 'lotNumber')
        Location binLocation = new Location(name: 'binLocation')
        CycleCount cycleCount = new CycleCount(facility: facility, status: CycleCountStatus.COUNTING, cycleCountItems: [])

        and: 'stock has come back'
        productAvailabilityServiceStub.getAvailableItems(facility, _, false, true) >> [
                new AvailableItem(inventoryItem: inventoryItem, binLocation: binLocation, quantityOnHand: 5),
        ]

        when: 'we refresh the first count explicitly'
        CycleCountProductAvailabilityService.CycleCountItemsForRefresh changedItems =
                cycleCountProductAvailabilityService.refreshProductAvailability(cycleCount, false, 0)

        then: 'the item is added to the first count'
        changedItems.itemsToCreate.size() == 1
        cycleCount.getCycleCountItem(product, binLocation, inventoryItem, 0).quantityOnHand == 5
    }
}
