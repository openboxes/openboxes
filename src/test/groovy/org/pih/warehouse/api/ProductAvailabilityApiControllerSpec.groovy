package org.pih.warehouse.api

import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.hibernate.ObjectNotFoundException
import spock.lang.Specification

import org.pih.warehouse.PaginatedList
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.AvailableItemsListCommand
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.product.Product

class ProductAvailabilityApiControllerSpec extends Specification
        implements DataTest, ControllerUnitTest<ProductAvailabilityApiController> {

    ProductAvailabilityService productAvailabilityServiceStub

    void setupSpec() {
        mockDomains(Location, Product, InventoryItem)
    }

    void setup() {
        productAvailabilityServiceStub = Stub(ProductAvailabilityService)
        controller.productAvailabilityService = productAvailabilityServiceStub
    }

    void 'list returns flat rows with required fields for a valid facility'() {
        given:
        Location location = new Location(name: "Depot A").save(validate: false)
        Product product = new Product(name: "Aspirin", productCode: "ASA").save(validate: false)
        InventoryItem inventoryItem = new InventoryItem(product: product, lotNumber: "LOT1").save(validate: false)
        Location bin = new Location(name: "Bin-1").save(validate: false)

        AvailableItem availableItem = new AvailableItem(
                inventoryItem: inventoryItem,
                binLocation: bin,
                quantityOnHand: 10,
                quantityAvailable: 8
        )

        AvailableItemsListCommand command = new AvailableItemsListCommand(facilityId: location.id)
        productAvailabilityServiceStub.getAvailableItems(location, null, false, true, [max: 10, offset: 0]) >>
                new PaginatedList([availableItem], 1)

        when:
        controller.list(command)

        then:
        JSONObject json = new JSONObject(controller.response.contentAsString)
        json.totalCount == 1
        JSONArray data = json.getJSONArray("data")
        data.length() == 1
        JSONObject row = data.getJSONObject(0)
        row.getString("productCode") == "ASA"
        row.getString("product.name") == "Aspirin"
        row.getString("lotNumber") == "LOT1"
        row.getInt("quantityOnHand") == 10
        row.getInt("quantityAvailable") == 8
    }

    void 'list throws when facility is missing'() {
        given:
        AvailableItemsListCommand command = new AvailableItemsListCommand(facilityId: "missing-id")

        when:
        controller.list(command)

        then:
        thrown(ObjectNotFoundException)
    }
}
