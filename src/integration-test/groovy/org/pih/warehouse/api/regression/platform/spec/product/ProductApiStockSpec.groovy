package org.pih.warehouse.api.regression.platform.spec.product

import spock.lang.PendingFeature




import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import org.pih.warehouse.api.regression.platform.client.product.RegressionProductApi
import org.pih.warehouse.api.spec.product.*
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.response.Response
import io.restassured.specification.ResponseSpecification
import org.apache.http.HttpStatus
import org.hamcrest.Matchers

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.common.domain.builder.inventory.RecordInventoryCommandTestBuilder
import org.pih.warehouse.common.domain.builder.product.ProductTestBuilder
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAssociation
import org.pih.warehouse.product.ProductAssociationTypeCode
import org.pih.warehouse.product.ProductCatalog
import org.pih.warehouse.product.ProductCatalogItem

import org.springframework.beans.factory.annotation.Autowired

/**
 * The per-product reads of stock, lots, demand, substitutes and catalogs (/api/products/...). Each test stocks a
 * product of its own in a bin of the main facility (the logged-in location).
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation
class ProductApiStockSpec extends RegressionApiSpec {

    static final String LOT = 'PRD-LOT-1'

    @Autowired
    RegressionProductApi regressionProductApi

    Location bin

    Product item

    /** The lot's expiration date: a first of the month, a few years ahead. */
    Date expires = Date.from(LocalDate.now().plusYears(3).withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant())

    @Override
    void setupData() {
        bin = Location.withNewTransaction {
            new LocationTestBuilder().name(randomUtil.randomStringFieldValue('Shelf')).asBinLocation(facility).build(true)
        }
        item = newProduct('Stocked product')
    }

    Product newProduct(String name) {
        return Product.withNewTransaction {
            new ProductTestBuilder().name(randomUtil.randomStringFieldValue(name)).category(rootCategory).build(true)
        }
    }

    /** Stock records of this test so far: each one gets its own second (one count per product and second). */
    int stockRecords = 0

    /** Records the quantity of the product's lot in the bin (in the past: a "now" can round up in the database). */
    void stock(Product stocked, int quantity, String lot = LOT, Date expiration = expires) {
        stockLots(stocked, [[lot, expiration, quantity]])
    }

    /**
     * Records the product's stock as these lots ([lot, expiration date, quantity] each) in one count: a count
     * covers all lots of the product, so a later count of one lot would zero the others.
     */
    void stockLots(Product stocked, List<List> lots) {
        RecordInventoryCommandTestBuilder command = new RecordInventoryCommandTestBuilder()
                .product(stocked)
                .inventory(facility.inventory)
                .transactionDate(Instant.now().minusSeconds(300 - 10 * stockRecords++))
        lots.each { List lot -> command.row(lot[0] as String, lot[1] as Date, bin, lot[2] as int) }
        recordStockApiWrapper.saveRecordStockOK(facility, command.build())
    }

    static ResponseSpecification error(int status) {
        return ProductSourceSpec.error(status)
    }

    // Available stock

    void 'the available items of a product are listed per bin and lot at the current location'() {
        given:
        stock(item, 7)

        expect:
        regressionProductApi.availableItems(['product.id': item.id], availableIn(bin, 7))
        regressionProductApi.availableItems([id: item.id, 'location.id': facility.id], availableIn(bin, 7))
        regressionProductApi.productAction(item.id, 'availableBins', [:], availableIn(bin, 7))
    }

    @PendingFeature(reason = 'available bins of an unknown product answer 200, not 404')
    void 'available bins of an unknown product answer 404'() {
        expect:
        regressionProductApi.productAction(INVALID_ID, 'availableBins', [:], error(HttpStatus.SC_NOT_FOUND))
    }

    @PendingFeature(reason = 'available items without a product or with an unknown location answer 500')
    void 'available items without a product or with an unknown location answer 400'() {
        expect:
        regressionProductApi.availableItems([:], error(HttpStatus.SC_BAD_REQUEST))
        regressionProductApi.availableItems(['product.id': item.id, 'location.id': INVALID_ID], error(HttpStatus.SC_BAD_REQUEST))
        regressionProductApi.productAction(item.id, 'availableBins', ['location.id': INVALID_ID], error(HttpStatus.SC_BAD_REQUEST))
    }

    private ResponseSpecification availableIn(Location binLocation, int quantity) {
        return new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.size()', Matchers.equalTo(1))
                .expectBody('data[0]."binLocation.id"', Matchers.equalTo(binLocation.id))
                .expectBody('data[0].lotNumber', Matchers.equalTo(LOT))
                .expectBody('data[0].quantityAvailable', Matchers.equalTo(quantity))
                .build()
    }

    // Lots

    void 'a lot of a product is read with its quantity on hand; an unknown lot has none'() {
        given:
        stock(item, 9)

        expect:
        regressionProductApi.getInventoryItem(item.id, LOT, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('inventoryItem.lotNumber', Matchers.equalTo(LOT))
                .expectBody('inventoryItem.product.id', Matchers.equalTo(item.id))
                .expectBody('quantityOnHand', Matchers.equalTo(9))
                .build())
        regressionProductApi.getInventoryItem(item.id, 'NO-SUCH-LOT', new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('inventoryItem', Matchers.nullValue())
                .expectBody('quantityOnHand', Matchers.equalTo(0))
                .build())
    }

    @PendingFeature(reason = 'a lot of an unknown product answers 200, not 404')
    void 'a lot of an unknown product answers 404'() {
        expect:
        regressionProductApi.getInventoryItem(INVALID_ID, LOT, error(HttpStatus.SC_NOT_FOUND))
    }

    void 'the lots of products are listed with their expiration dates, once per lot'() {
        given:
        Product other = newProduct('Other stocked product')
        stockLots(item, [[LOT, expires, 5], ['PRD-LOT-2', null, 3]])
        stock(other, 2)

        when:
        Response lots = regressionProductApi.lotNumbersWithExpirationDate([item.id, other.id], responseSpecUtil.OK_RESPONSE_SPEC)
        Map<String, List<Map>> data = lots.jsonPath().getMap('data')

        then:
        data.keySet() == [item.id, other.id] as Set
        data[item.id]*.lotNumber as Set == [LOT, 'PRD-LOT-2'] as Set
        data[item.id].find { it.lotNumber == 'PRD-LOT-2' }.expirationDate == null
        data[other.id]*.lotNumber == [LOT]
        data[other.id][0].expirationDate != null
    }

    void 'lots without products asked for are an empty map'() {
        expect:
        regressionProductApi.lotNumbersWithExpirationDate([], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data', Matchers.equalTo([:]))
                .build())
    }

    // Inventory counts

    void 'the latest inventory count date is given per product counted at the current location'() {
        given:
        Product notCounted = newProduct('Never counted product')
        stock(item, 3)

        when:
        Response latest = regressionProductApi.latestInventoryCountDate([item.id, notCounted.id], responseSpecUtil.OK_RESPONSE_SPEC)
        Map<String, Object> data = latest.jsonPath().getMap('data')

        then:
        data.keySet() == [item.id] as Set
        data[item.id] != null
    }

    @PendingFeature(reason = 'the latest inventory count date without products answers 500')
    void 'the latest inventory count date without products is an empty map'() {
        expect:
        regressionProductApi.latestInventoryCountDate([], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data', Matchers.equalTo([:]))
                .build())
    }

    // Demand

    void 'the availability and demand of a product at a location'() {
        given:
        stock(item, 11)

        expect:
        regressionProductApi.productAction(item.id, 'productAvailabilityAndDemand', [locationId: facility.id], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('quantityOnHand', Matchers.equalTo(11))
                .expectBody('quantityAvailable', Matchers.equalTo(11))
                .expectBody('monthlyDemand', Matchers.equalTo(0))
                .build())
    }

    void 'the demand of a product between two locations, with the quantity on hand at the destination'() {
        given:
        stock(item, 6)
        Location ward = Location.withNewTransaction {
            new LocationTestBuilder().name(randomUtil.randomStringFieldValue('Ward')).asFacility().build(true)
        }

        expect:
        regressionProductApi.productAction(item.id, 'productDemand', [originId: ward.id, destinationId: facility.id], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('quantityOnHand', Matchers.equalTo(6))
                .expectBody('monthlyDemand', Matchers.equalTo(0))
                .build())
    }

    @PendingFeature(reason = 'demand of an unknown product answers 500')
    void 'demand of an unknown product answers 404; without its locations 400'() {
        expect:
        regressionProductApi.productAction(INVALID_ID, 'productAvailabilityAndDemand', [locationId: facility.id], error(HttpStatus.SC_NOT_FOUND))
        regressionProductApi.productAction(item.id, 'productAvailabilityAndDemand', [:], error(HttpStatus.SC_BAD_REQUEST))
        regressionProductApi.productAction(INVALID_ID, 'productDemand', [originId: facility.id, destinationId: facility.id], error(HttpStatus.SC_NOT_FOUND))
        regressionProductApi.productAction(item.id, 'productDemand', [originId: facility.id], error(HttpStatus.SC_BAD_REQUEST))
    }

    // Substitutes and other associations

    void 'substitutions list the substitutes with their available stock and earliest expiry'() {
        given: 'the product has a lot that does not expire and one that expires after the substitute\'s'
        Product substitute = newProduct('Substitute product')
        stockLots(item, [[LOT, expires, 5], ['PRD-LOT-NO-EXPIRY', null, 4]])
        stock(substitute, 8, 'SUB-LOT', Date.from(expires.toInstant().minusSeconds(86400 * 60)))
        String associationId = associate(item, substitute, ProductAssociationTypeCode.SUBSTITUTE)

        expect:
        regressionProductApi.productAction(item.id, 'substitutions', [:], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.product.id', Matchers.equalTo(item.id))
                .expectBody('data.hasAssociations', Matchers.equalTo(true))
                .expectBody('data.hasEarlierExpiringItems', Matchers.equalTo(true))
                .expectBody('data.availableItems.lotNumber', Matchers.containsInAnyOrder(LOT, 'PRD-LOT-NO-EXPIRY'))
                .expectBody('data.substitutions.id', Matchers.contains(associationId))
                .expectBody('data.substitutions[0].type', Matchers.equalTo('SUBSTITUTE'))
                .expectBody('data.substitutions[0].product.id', Matchers.equalTo(substitute.id))
                .expectBody('data.substitutions[0].availableQuantity', Matchers.equalTo(8))
                .expectBody('data.substitutions[0].conversionFactor', Matchers.equalTo(2.0f))
                .build())
    }

    @PendingFeature(reason = 'substitutions answer 500 when the substitutes have no expiring stock')
    void 'substitutes without stock do not expire earlier'() {
        given:
        Product substitute = newProduct('Substitute without stock')
        stock(item, 5)
        associate(item, substitute, ProductAssociationTypeCode.SUBSTITUTE)

        expect:
        regressionProductApi.productAction(item.id, 'substitutions', [:], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.hasEarlierExpiringItems', Matchers.equalTo(false))
                .expectBody('data.substitutions[0].availableQuantity', Matchers.nullValue())
                .build())
    }

    void 'associated products are filtered by type, under productAssociations'() {
        given:
        Product substitute = newProduct('Substitute')
        Product equivalent = newProduct('Equivalent')
        associate(item, substitute, ProductAssociationTypeCode.SUBSTITUTE)
        String equivalentId = associate(item, equivalent, ProductAssociationTypeCode.EQUIVALENT)

        expect:
        regressionProductApi.productAction(item.id, 'associatedProducts', [type: 'EQUIVALENT'], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.productAssociations.id', Matchers.contains(equivalentId))
                .expectBody('data.hasEarlierExpiringItems', Matchers.equalTo(false))
                .build())
    }

    @PendingFeature(reason = 'associated products of an unknown product answer 200, not 404')
    void 'associated products of an unknown product answer 404'() {
        expect:
        regressionProductApi.productAction(INVALID_ID, 'associatedProducts', [type: 'SUBSTITUTE'], error(HttpStatus.SC_NOT_FOUND))
        regressionProductApi.productAction(INVALID_ID, 'substitutions', [:], error(HttpStatus.SC_NOT_FOUND))
    }

    private String associate(Product from, Product to, ProductAssociationTypeCode code) {
        return ProductAssociation.withNewTransaction {
            new ProductAssociation(product: Product.get(from.id), code: code, associatedProduct: Product.get(to.id),
                    quantity: 2, comments: 'test').save(failOnError: true, flush: true).id
        }
    }

    // Catalogs

    @PendingFeature(reason = 'withCatalogs of an unknown product answers 500, not 404')
    void 'a product is read with its catalogs'() {
        given:
        String catalogId = ProductCatalog.withNewTransaction {
            ProductCatalog catalog = new ProductCatalog(code: randomUtil.randomStringFieldValue('CAT'),
                    name: 'Essential medicines')
            catalog.addToProductCatalogItems(new ProductCatalogItem(product: Product.get(item.id)))
            catalog.save(failOnError: true, flush: true).id
        }

        expect:
        regressionProductApi.productAction(item.id, 'withCatalogs', [:], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.equalTo(item.id))
                .expectBody('data.productCode', Matchers.equalTo(item.productCode))
                .expectBody('data.catalogs.id', Matchers.contains(catalogId))
                .expectBody('data.catalogs[0].name', Matchers.equalTo('Essential medicines'))
                .build())
        regressionProductApi.productAction(INVALID_ID, 'withCatalogs', [:], error(HttpStatus.SC_NOT_FOUND))
    }
}
