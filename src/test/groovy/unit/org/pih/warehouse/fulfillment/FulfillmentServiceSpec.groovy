package unit.org.pih.warehouse.fulfillment

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.core.Location
import org.pih.warehouse.fulfillment.FulfillmentService
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.outbound.ImportPackingListItem
import org.pih.warehouse.product.Product
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class FulfillmentServiceSpec extends Specification implements ServiceUnitTest<FulfillmentService>, DataTest {

    void setup() {
        service.productAvailabilityService = new ProductAvailabilityService()

        mockDomain(Product)
        GroovyMock(Product, global: true)
        Product.findByProductCode("product-code-123") >> new Product(productCode: "product-code-123")
    }

    void 'bindOrInferLotNumber should return exact lotNumber string when lotNumber was provided'() {
        given:
        Map data = [
                lotNumber: "fake-lot-number",
                binLocation: null
        ]
        ImportPackingListItem importedFile = new ImportPackingListItem()

        when:
        String boundLotNumber = service.bindOrInferLotNumber(importedFile, data)

        then:
        boundLotNumber == data.lotNumber
    }

    void 'bindOrInferLotNumber should return default lot when lotNumber is not provided (default lot is empty string)'() {
        given:
        Map data = [
                product: "product-code-123",
                lotNumber: null,
                binLocation: null
        ]
        ImportPackingListItem importedFile = new ImportPackingListItem()
        Location location = new Location(id: "fake-location")
        importedFile.origin = location

        ProductAvailabilityService productAvailabilityServiceSpy = Spy(service.productAvailabilityService)

        productAvailabilityServiceSpy.getAvailableBinLocations(_, _) >> [
                new AvailableItem(inventoryItem: new InventoryItem(lotNumber: ""))
        ]

        service.productAvailabilityService = productAvailabilityServiceSpy

        when:
        String inferredLotNumber = service.bindOrInferLotNumber(importedFile, data)

        then:
        inferredLotNumber == ""
    }

    void 'bindOrInferLotNumber should return default lot when lotNumber is not provided (default lot is null)'() {
        given:
        Map data = [
                product: "product-code-123",
                lotNumber: null,
                binLocation: null
        ]
        ImportPackingListItem importedFile = new ImportPackingListItem()
        Location location = new Location(id: "fake-location")
        importedFile.origin = location

        ProductAvailabilityService productAvailabilityServiceSpy = Spy(service.productAvailabilityService)

        productAvailabilityServiceSpy.getAvailableBinLocations(_, _) >> [
                new AvailableItem(inventoryItem: new InventoryItem(lotNumber: null))
        ]

        service.productAvailabilityService = productAvailabilityServiceSpy

        when:
        String inferredLotNumber = service.bindOrInferLotNumber(importedFile, data)

        then:
        inferredLotNumber == null
    }

    void 'bindOrInferLotNumber should return null if there are multiple stocks with default lot'() {
        given:
        Map data = [
                product: "product-code-123",
                lotNumber: null,
                binLocation: null
        ]
        ImportPackingListItem importedFile = new ImportPackingListItem()
        Location location = new Location(id: "fake-location")
        importedFile.origin = location

        InventoryItem defaultLot = new InventoryItem(lotNumber: "")

        ProductAvailabilityService productAvailabilityServiceSpy = Spy(service.productAvailabilityService)

        productAvailabilityServiceSpy.getAvailableBinLocations(_, _) >> [
                new AvailableItem(inventoryItem: defaultLot, binLocation: new Location(name: "first-bin")),
                new AvailableItem(inventoryItem: defaultLot, binLocation: new Location(name: "second-bin")),
        ]

        service.productAvailabilityService = productAvailabilityServiceSpy

        when:
        String inferredLotNumber = service.bindOrInferLotNumber(importedFile, data)

        then:
        inferredLotNumber == null
    }

    void 'bindOrInferLotNumber should return proper lotNumber when binLocation is provided and is available'() {
        given:
        String binLocation = "fake-bin-location"
        Map data = [
                product: "product-code-123",
                lotNumber: null,
                binLocation: binLocation
        ]
        ImportPackingListItem importedFile = new ImportPackingListItem()
        Location location = new Location(id: "fake-location")
        importedFile.origin = location

        ProductAvailabilityService productAvailabilityServiceSpy = Spy(service.productAvailabilityService)

        productAvailabilityServiceSpy.getAvailableBinLocations(_, _) >> [
                new AvailableItem(
                        inventoryItem: new InventoryItem(lotNumber: "fake-lot-number-1"),
                        binLocation:  new Location(name: binLocation),
                ),
                new AvailableItem(
                        inventoryItem: new InventoryItem(lotNumber: "fake-lot-number-2"),
                        binLocation:  new Location(name: "another-bin"),
                ),
        ]

        service.productAvailabilityService = productAvailabilityServiceSpy

        when:
        String inferredLotNumber = service.bindOrInferLotNumber(importedFile, data)

        then:
        inferredLotNumber == "fake-lot-number-1"
    }

    void 'bindOrInferLotNumber should return null when binLocation is provided and is available in multiple bins'() {
        given:
        String binLocation = "fake-bin-location"
        Map data = [
                product: "product-code-123",
                lotNumber: null,
                binLocation: binLocation
        ]
        ImportPackingListItem importedFile = new ImportPackingListItem()
        Location location = new Location(id: "fake-location")
        importedFile.origin = location

        ProductAvailabilityService productAvailabilityServiceSpy = Spy(service.productAvailabilityService)

        productAvailabilityServiceSpy.getAvailableBinLocations(_, _) >> [
                new AvailableItem(
                        inventoryItem: new InventoryItem(lotNumber: "fake-lot-number-1"),
                        binLocation:  new Location(name: binLocation),
                ),
                new AvailableItem(
                        inventoryItem: new InventoryItem(lotNumber: "fake-lot-number-2"),
                        binLocation:  new Location(name: binLocation),
                ),
                new AvailableItem(
                        inventoryItem: new InventoryItem(lotNumber: "fake-lot-number-3"),
                        binLocation:  new Location(name: "another-bin"),
                ),
        ]

        service.productAvailabilityService = productAvailabilityServiceSpy

        when:
        String inferredLotNumber = service.bindOrInferLotNumber(importedFile, data)

        then:
        inferredLotNumber == null
    }

    void 'bindOrInferLotNumber should return proper lotNumber when binLocation is not provided but stock in default bin is available'() {
        given:
        Map data = [
                product: "product-code-123",
                lotNumber: null,
                binLocation: null
        ]
        ImportPackingListItem importedFile = new ImportPackingListItem()
        Location location = new Location(id: "fake-location")
        importedFile.origin = location

        ProductAvailabilityService productAvailabilityServiceSpy = Spy(service.productAvailabilityService)

        productAvailabilityServiceSpy.getAvailableBinLocations(_, _) >> [
                new AvailableItem(
                        inventoryItem: new InventoryItem(lotNumber: "fake-lot-number-1"),
                        binLocation:  new Location(name: "bin-one"),
                ),
                new AvailableItem(
                        inventoryItem: new InventoryItem(lotNumber: "fake-lot-number-2"),
                        binLocation:  new Location(name: "bin-two"),
                ),
                new AvailableItem(
                        inventoryItem: new InventoryItem(lotNumber: "fake-lot-number-3"),
                        binLocation:  null
                ),
        ]

        service.productAvailabilityService = productAvailabilityServiceSpy

        when:
        String inferredLotNumber = service.bindOrInferLotNumber(importedFile, data)

        then:
        inferredLotNumber == "fake-lot-number-3"
    }

    void 'bindOrInferLotNumber should return null when binLocation is not provided and stock is available in multiple default bins'() {
        given:
        Map data = [
                product: "product-code-123",
                lotNumber: null,
                binLocation: null
        ]
        ImportPackingListItem importedFile = new ImportPackingListItem()
        Location location = new Location(id: "fake-location")
        importedFile.origin = location

        ProductAvailabilityService productAvailabilityServiceSpy = Spy(service.productAvailabilityService)

        productAvailabilityServiceSpy.getAvailableBinLocations(_, _) >> [
                new AvailableItem(
                        inventoryItem: new InventoryItem(lotNumber: "fake-lot-number-1"),
                        binLocation:  new Location(name: "bin-one"),
                ),
                new AvailableItem(
                        inventoryItem: new InventoryItem(lotNumber: "fake-lot-number-2"),
                        binLocation:  null,
                ),
                new AvailableItem(
                        inventoryItem: new InventoryItem(lotNumber: "fake-lot-number-3"),
                        binLocation:  null
                ),
        ]

        service.productAvailabilityService = productAvailabilityServiceSpy

        when:
        String inferredLotNumber = service.bindOrInferLotNumber(importedFile, data)

        then:
        inferredLotNumber == null
    }
}
