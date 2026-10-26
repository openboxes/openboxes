// Companion spec: extra features for upstream's InferringOutboundImportValuesSpec (the upstream file stays authoritative).
package org.pih.warehouse.regression.platform.fulfillment

import spock.lang.PendingFeature

import org.pih.warehouse.fulfillment.*
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
class InferringOutboundImportValuesMoreSpec extends Specification implements ServiceUnitTest<FulfillmentService>, DataTest {

    void setup() {
        service.productAvailabilityService = new ProductAvailabilityService()

        mockDomain(Product)
        GroovyMock(Product, global: true)
        Product.findByProductCode("product-code-123") >> new Product(productCode: "product-code-123")
    }

    @PendingFeature(reason = 'OBAF looks up bin and lot in obj.origin only, so a row whose origin binds later finds nothing')
    void 'bindOrInferBinLocation looks in the row origin when the origin is not bound yet (origin given as #description)'() {
        given: 'a row whose origin field is bound after its bin location'
        Location rowOrigin = new Location(name: "Row origin")
        Location binA = new Location(name: "bin-a")
        // Mocking Location globally intercepts its constructor too, so the locations are built first
        GroovyMock(Location, global: true)
        Location.get("row-origin") >> rowOrigin
        Map data = [product: "product-code-123", lotNumber: null, binLocation: null, origin: origin]
        ImportPackingListItem importedFile = new ImportPackingListItem()

        ProductAvailabilityService productAvailabilityServiceSpy = Spy(service.productAvailabilityService)
        productAvailabilityServiceSpy.getAvailableBinLocations({ it.is(rowOrigin) }, _) >> [
                new AvailableItem(inventoryItem: new InventoryItem(lotNumber: ""), binLocation: binA)
        ]
        productAvailabilityServiceSpy.getAvailableBinLocations(_, _) >> []
        service.productAvailabilityService = productAvailabilityServiceSpy

        expect:
        service.bindOrInferBinLocation(importedFile, data)?.name == "bin-a"
        service.bindOrInferLotNumber(importedFile, data) == ""

        where:
        description | origin
        'an id'     | "row-origin"
        'an object' | [id: "row-origin"]
    }
}
