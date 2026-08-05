package org.pih.warehouse.inventory

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.pih.warehouse.api.AllocatedItem
import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.api.SuggestedItem
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import spock.lang.Specification
import spock.lang.Unroll

/**
 * OBLS-919: availability used for allocation decisions must come from transactions + live picklist
 * reservations, not the asynchronously-refreshed product_availability view, so two outbounds for the same
 * product cannot both reserve the same stock. These specs pin the reservation-aware computation that makes
 * the second (sequential) allocation see the first one's reservation.
 */
@Unroll
class StockMovementServiceLiveAvailabilitySpec extends Specification
        implements ServiceUnitTest<StockMovementService>, DataTest {

    InventoryService inventoryService
    ProductAvailabilityService productAvailabilityService

    Location facility
    Location bin
    Product product
    InventoryItem inventoryItem
    RequisitionItem requisitionItem

    void setupSpec() {
        mockDomains Location, Product, InventoryItem, Requisition, RequisitionItem
    }

    void setup() {
        inventoryService = Mock(InventoryService)
        productAvailabilityService = Mock(ProductAvailabilityService)
        service.inventoryService = inventoryService
        service.productAvailabilityService = productAvailabilityService

        // sortAvailableItems is a passthrough for the purpose of these tests
        productAvailabilityService.sortAvailableItems(_) >> { List args -> args[0] }

        facility = new Location(id: 'facility-1')
        bin = new Location(id: 'bin-1')
        product = new Product(id: 'product-1')
        inventoryItem = new InventoryItem(id: 'item-1')
        // A brand new requisition item with no picklist of its own (its own reservations are empty)
        requisitionItem = new RequisitionItem(product: product, requisition: new Requisition(origin: facility))
    }

    void 'getAvailableItemsFromTransactions subtracts live picklist reservations from transaction QoH'() {
        given: 'transactions show 5 on hand in the bin'
        inventoryService.getProductQuantityByBinLocation(facility, product, Boolean.TRUE) >> [
                new BinLocationItem(inventoryItem: inventoryItem, binLocation: bin, quantity: 5),
        ]

        and: 'another pending requisition has already reserved all 5'
        productAvailabilityService.getQuantityPickedByProductAndLocation(facility, product) >> [
                new AllocatedItem(inventoryItem: inventoryItem, binLocation: bin, quantityAllocated: 5),
        ]

        when:
        List<AvailableItem> availableItems = service.getAvailableItemsFromTransactions(facility, requisitionItem)

        then: 'on hand is still 5 but nothing is available to promise'
        availableItems.size() == 1
        availableItems[0].quantityOnHand == 5
        availableItems[0].quantityAvailable == 0

        and: 'so the allocator can suggest nothing for a request of 5 (second outbound fails)'
        List<SuggestedItem> suggested = service.getSuggestedItems(availableItems, 5)
        suggested.isEmpty()
    }

    void 'getAvailableItemsFromTransactions reports the full quantity when nothing is reserved'() {
        given: 'transactions show 5 on hand and no reservations exist'
        inventoryService.getProductQuantityByBinLocation(facility, product, Boolean.TRUE) >> [
                new BinLocationItem(inventoryItem: inventoryItem, binLocation: bin, quantity: 5),
        ]
        productAvailabilityService.getQuantityPickedByProductAndLocation(facility, product) >> []

        when:
        List<AvailableItem> availableItems = service.getAvailableItemsFromTransactions(facility, requisitionItem)

        then: 'all 5 are available to promise (first outbound can allocate)'
        availableItems.size() == 1
        availableItems[0].quantityOnHand == 5
        availableItems[0].quantityAvailable == 5

        and:
        List<SuggestedItem> suggested = service.getSuggestedItems(availableItems, 5)
        suggested.sum { it.quantityPicked } == 5
    }

    void 'getAvailableItemsFromTransactions nets a partially reserved bin down to the remainder'() {
        given: 'transactions show 5 on hand, 3 already reserved by another requisition'
        inventoryService.getProductQuantityByBinLocation(facility, product, Boolean.TRUE) >> [
                new BinLocationItem(inventoryItem: inventoryItem, binLocation: bin, quantity: 5),
        ]
        productAvailabilityService.getQuantityPickedByProductAndLocation(facility, product) >> [
                new AllocatedItem(inventoryItem: inventoryItem, binLocation: bin, quantityAllocated: 3),
        ]

        when:
        List<AvailableItem> availableItems = service.getAvailableItemsFromTransactions(facility, requisitionItem)

        then:
        availableItems[0].quantityAvailable == 2
    }
}
