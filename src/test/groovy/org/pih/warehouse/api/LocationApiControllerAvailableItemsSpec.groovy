package org.pih.warehouse.api

import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.hibernate.ObjectNotFoundException
import spock.lang.Specification

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
        productAvailabilityServiceStub.getAvailableItemsByLocation(location, 100, 0) >> [
                data      : [availableItem],
                totalCount: 1
        ]

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

    void 'availableItems uses default max 100 and offset 0 when params omitted'() {
        given:
        Location location = new Location(name: "Depot").save(validate: false)
        params.id = location.id
        Integer capturedMax
        Integer capturedOffset
        productAvailabilityServiceStub.getAvailableItemsByLocation(location, _, _) >> { Location loc, Integer max, Integer offset ->
            capturedMax = max
            capturedOffset = offset
            return [data: [], totalCount: 0]
        }

        when:
        controller.availableItems()

        then:
        capturedMax == 100
        capturedOffset == 0
        new JSONObject(controller.response.contentAsString).getJSONArray("data").length() == 0
    }

    void 'availableItems caps max at 1000'() {
        given:
        Location location = new Location(name: "Depot").save(validate: false)
        params.id = location.id
        params.max = "5000"
        Integer capturedMax
        productAvailabilityServiceStub.getAvailableItemsByLocation(location, _, _) >> { Location loc, Integer max, Integer offset ->
            capturedMax = max
            return [data: [], totalCount: 0]
        }

        when:
        controller.availableItems()

        then:
        capturedMax == 1000
    }

    void 'availableItems passes offset to the service'() {
        given:
        Location location = new Location(name: "Depot").save(validate: false)
        params.id = location.id
        params.max = "50"
        params.offset = "100"
        Integer capturedOffset
        productAvailabilityServiceStub.getAvailableItemsByLocation(location, _, _) >> { Location loc, Integer max, Integer offset ->
            capturedOffset = offset
            return [data: [], totalCount: 200]
        }

        when:
        controller.availableItems()

        then:
        capturedOffset == 100
        new JSONObject(controller.response.contentAsString).totalCount == 200
    }

    void 'availableItems throws when location is missing'() {
        given:
        params.id = "missing-id"

        when:
        controller.availableItems()

        then:
        thrown(ObjectNotFoundException)
    }

    void 'exportAvailableItems returns flat JSON without pagination args'() {
        given:
        Location location = new Location(name: "Depot").save(validate: false)
        Product product = new Product(name: "Ibuprofen", productCode: "IBU").save(validate: false)
        InventoryItem inventoryItem = new InventoryItem(product: product, lotNumber: "L2").save(validate: false)
        AvailableItem availableItem = new AvailableItem(
                inventoryItem: inventoryItem,
                binLocation: null,
                quantityOnHand: 5,
                quantityAvailable: 5
        )
        params.id = location.id
        Integer capturedMax = -1
        productAvailabilityServiceStub.getAvailableItemsByLocation(location, _, _) >> { Location loc, Integer max, Integer offset ->
            capturedMax = max
            return [data: [availableItem], totalCount: 1]
        }

        when:
        controller.exportAvailableItems()

        then:
        capturedMax == null
        JSONObject json = new JSONObject(controller.response.contentAsString)
        !json.has("totalCount")
        json.getJSONArray("data").length() == 1
        json.getJSONArray("data").getJSONObject(0).getJSONObject("location").getString("id") == location.id
    }

    void 'exportAvailableItems throws when location is missing'() {
        given:
        params.id = "missing-id"

        when:
        controller.exportAvailableItems()

        then:
        thrown(ObjectNotFoundException)
    }
}
