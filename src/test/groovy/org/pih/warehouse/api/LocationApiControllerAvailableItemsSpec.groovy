package org.pih.warehouse.api

import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.hibernate.ObjectNotFoundException
import spock.lang.Specification

import org.pih.warehouse.PaginatedList
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.product.Product

class LocationApiControllerAvailableItemsSpec extends Specification
        implements DataTest, ControllerUnitTest<LocationApiController> {

    ProductAvailabilityService productAvailabilityServiceStub

    void setupSpec() {
        mockDomains(Location, Product, InventoryItem)
    }

    void setup() {
        productAvailabilityServiceStub = Stub(ProductAvailabilityService)
        controller.productAvailabilityService = productAvailabilityServiceStub
    }

    void 'availableItems returns flat rows with required fields for a valid location'() {
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

        params.id = location.id
        productAvailabilityServiceStub.getAvailableItems(location, null, false, true, [max: 10, offset: 0]) >>
                new PaginatedList([availableItem], 1)

        when:
        controller.availableItems()

        then:
        JSONObject json = new JSONObject(controller.response.contentAsString)
        json.totalCount == 1
        JSONArray data = json.getJSONArray("data")
        data.length() == 1
        JSONObject row = data.getJSONObject(0)
        row.getJSONObject("location").getString("id") == location.id
        row.getJSONObject("location").getString("name") == "Depot A"
        row.getString("productCode") == "ASA"
        row.getString("product.name") == "Aspirin"
        row.getString("lotNumber") == "LOT1"
        row.getInt("quantityOnHand") == 10
        row.getInt("quantityAvailable") == 8
        !row.has("zones")
    }

    void 'availableItems throws when location is missing'() {
        given:
        params.id = "missing-id"

        when:
        controller.availableItems()

        then:
        thrown(ObjectNotFoundException)
    }
}
