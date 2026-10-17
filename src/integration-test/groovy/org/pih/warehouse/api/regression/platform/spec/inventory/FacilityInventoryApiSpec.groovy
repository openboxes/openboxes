package org.pih.warehouse.api.regression.platform.spec.inventory

import spock.lang.PendingFeature




import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import spock.lang.Ignore
import org.pih.warehouse.api.spec.inventory.*
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.response.Response
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Unroll

import org.pih.warehouse.api.regression.platform.client.inventory.InventoryLevelApi
import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAvailability

/**
 * A facility's inventory settings and stock through the API: inventory levels (JSON, CSV, spreadsheet), the preferred
 * bin locations they configure, the reorder report, and importing stock from a CSV.
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation(tolerate = [
        'known response GET /facilities/[^/]+/inventories/reorderReport',
])
class FacilityInventoryApiSpec extends CycleCountSpec {

    @Autowired
    InventoryLevelApi inventoryLevelApi

    /** An inventory level at the main facility for the product, with a preferred bin location. */
    Map<String, String> inventoryLevel(Product product) {
        return InventoryLevel.withNewTransaction {
            Location bin = new LocationTestBuilder()
                    .name("Preferred bin ${UUID.randomUUID().toString().take(6)}")
                    .asBinLocation(Location.load(facility.id))
                    .build(true)
            InventoryLevel level = new InventoryLevel(
                    inventory: Inventory.load(facility.inventory.id),
                    product: Product.load(product.id),
                    preferredBinLocation: bin,
                    minQuantity: 5,
                    reorderQuantity: 10,
                    maxQuantity: 20,
                    forecastQuantity: 12.5,
                    binLocation: 'Shelf 4')
            level.save(failOnError: true, flush: true)
            return [level: level.id, bin: bin.id]
        }
    }

    @PendingFeature(reason = 'the inventory levels XLS download has no content type')
    void 'the inventory levels of a facility list as JSON and download as CSV and as a spreadsheet'() {
        given:
        Product product = stockedProduct(2)
        Map<String, String> level = inventoryLevel(product)

        when:
        Response json = inventoryLevelApi.list(facility.id, null, ok().build())
        Response csv = inventoryLevelApi.list(facility.id, 'csv', ok()
                .expectHeader('Content-Disposition', Matchers.containsString('inventory-levels.csv')).build())
        Response xls = inventoryLevelApi.list(facility.id, 'xls', ok()
                .expectContentType('application/vnd.ms-excel')
                .expectHeader('Content-Disposition', Matchers.containsString('inventory-levels.xls')).build())

        then:
        json.jsonPath().getList('data.id').contains(level.level)
        csv.asString().contains(product.productCode)
        xls.asByteArray().length > 0
    }

    @PendingFeature(reason = 'an unknown facility answers 500, not 404')
    void 'the inventory levels of an unknown facility answer 404'() {
        expect:
        inventoryLevelApi.list(INVALID_ID, null, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    @PendingFeature(reason = 'an unknown facility answers 500, not 404')
    void 'the same list through /api/inventoryLevels takes the facility as a parameter; without one it is a 400'() {
        given:
        Product product = stockedProduct(1)
        Map<String, String> level = inventoryLevel(product)

        expect:
        inventoryLevelApi.listByQuery([facilityId: facility.id], ok().build())
                .jsonPath().getList('data.id').contains(level.level)
        inventoryLevelApi.listByQuery([facilityId: INVALID_ID], responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
        inventoryLevelApi.listByQuery([:], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody('errorCode', Matchers.equalTo(HttpStatus.SC_BAD_REQUEST))
                .build())
    }

    void 'the reorder report lists as JSON and downloads as CSV'() {
        given:
        Product product = stockedProduct(3)
        inventoryLevel(product)

        when:
        Response json = inventoryLevelApi.reorderReport(facility.id, [inventoryLevelStatus: 'ALL_PRODUCTS'],
                ok().build())
        Response csv = inventoryLevelApi.reorderReport(facility.id, [format: 'csv'], ok()
                .expectContentType('text/csv')
                .expectHeader('Content-Disposition', Matchers.containsString('Reorder report')).build())

        then:
        json.asString().contains(product.id)
        csv.asString().contains(product.productCode)
    }

    @Unroll
    void 'the reorder report filters by #status'() {
        expect:
        inventoryLevelApi.reorderReport(facility.id, [inventoryLevelStatus: status], ok().build())

        where:
        status << ['ALL_PRODUCTS', 'BELOW_MAXIMUM', 'BELOW_REORDER', 'BELOW_MINIMUM']
    }

    void 'importing a stock CSV raises the quantity on hand to the imported quantity'() {
        given:
        Product product = stockedProduct(2)
        // The column names of the import template
        String csv = 'Product code,Product,Lot number,Expiration date,Bin location,Quantity\n' +
                "${product.productCode},${product.name},,,,5\n"

        when: 'the import (its baseline is dated a second before now) comes after the stock recorded above'
        inventoryLevelApi.importInventory(facility.id, csv, 'text/csv', responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        awaitTransactedQuantityOnHand(product, 5)
    }

    @PendingFeature(reason = 'camelCase CSV headers are not recognised (500)')
    void 'an import with camelCase headers raises the quantity on hand the same way'() {
        given:
        Product product = stockedProduct(2)
        String csv = 'productCode,product,lotNumber,expirationDate,binLocation,quantity\n' +
                "${product.productCode},${product.name},,,,6\n"

        when: 'the import comes after the stock recorded above (stockedProduct dates it seconds back)'
        inventoryLevelApi.importInventory(facility.id, csv, 'text/csv', responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        awaitTransactedQuantityOnHand(product, 6)
    }

    @PendingFeature(reason = 'skip-unchanged compares with the lot quantity across every facility')
    void 'stock of the same lot at another depot does not stop the import raising this one'() {
        given: 'more of the lot at another depot than the import asks for here'
        Product product = stockedProduct(2)
        ProductAvailability.withNewTransaction {
            Location otherDepot = new LocationTestBuilder()
                    .name("Other depot ${UUID.randomUUID().toString().take(6)}")
                    .asFacility()
                    .build(true)
            InventoryItem lot = InventoryItem.findByProductAndLotNumberIsNull(Product.load(product.id))
            ProductAvailability elsewhere = new ProductAvailability(product: lot.product, location: otherDepot,
                    inventoryItem: lot, productCode: product.productCode, lotNumber: 'DEFAULT',
                    binLocationName: 'DEFAULT', quantityOnHand: 50)
            elsewhere.id = UUID.randomUUID().toString()  // assigned ids are not bound from the map
            elsewhere.save(failOnError: true, flush: true)
        }
        String csv = 'Product code,Product,Lot number,Expiration date,Bin location,Quantity\n' +
                "${product.productCode},${product.name},,,,5\n"

        when: 'the import comes after the stock recorded above (stockedProduct dates it seconds back)'
        inventoryLevelApi.importInventory(facility.id, csv, 'text/csv', responseSpecUtil.OK_RESPONSE_SPEC)

        then: 'each poll refreshes the availability from the committed transactions, so a few seconds are plenty'
        awaitTransactedQuantityOnHand(product, 5)
    }

    @PendingFeature(reason = 'an import into an unknown facility answers 500, not 404')
    void 'an import into an unknown facility answers 404'() {
        expect:
        inventoryLevelApi.importInventory(INVALID_ID,
                'Product code,Product,Lot number,Expiration date,Bin location,Quantity\nX,X,,,,5\n', 'text/csv',
                responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    @PendingFeature(reason = 'invalid quantities on skipped rows are accepted (200)')
    void 'an import refuses a missing, negative or non-numeric quantity even where the row would be skipped'() {
        given:
        Product product = stockedProduct(2)
        String header = 'Product code,Product,Lot number,Expiration date,Bin location,Quantity\n'

        expect:
        inventoryLevelApi.importInventory(facility.id, header + "${product.productCode},${product.name},,,,-1\n",
                'text/csv', responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
        inventoryLevelApi.importInventory(facility.id, header + "${product.productCode},${product.name},,,,many\n",
                'text/csv', responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
        inventoryLevelApi.importInventory(facility.id, header + "${product.productCode},${product.name},,,, \n",
                'text/csv', responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
        quantityOnHand(product) == 2
    }

    @PendingFeature(reason = 'an unknown product, empty file or non-CSV body answers 500, not 400')
    void 'an import with an unknown product, an empty file or another file type answers 400'() {
        expect:
        inventoryLevelApi.importInventory(facility.id,
                'Product code,Product,Lot number,Expiration date,Bin location,Quantity\nNO-SUCH-PRODUCT,Nothing,,,,5\n',
                'text/csv', responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
        inventoryLevelApi.importInventory(facility.id, '', 'text/csv',
                responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
        inventoryLevelApi.importInventory(facility.id, 'productCode,quantity\nX,1\n', 'text/plain',
                responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
    }
}
