package org.pih.warehouse.api.spec.inventory

import java.time.Instant
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Ignore

import org.pih.warehouse.api.client.inventory.RecordStockApiWrapper
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.common.domain.builder.inventory.RecordInventoryCommandTestBuilder
import org.pih.warehouse.product.ProductAvailability

class RecordStockApiSpec extends ApiSpec {

    @Autowired
    RecordStockApiWrapper recordStockApiWrapper

    void 'record stock can zero out stock'() {
        when:
        recordStockApiWrapper.saveRecordStockOK(facility, new RecordInventoryCommandTestBuilder()
                .product(product)
                .inventory(facility.inventory)
                .transactionDateNow()
                .row(null, null, null, 0)
                .build())

        and:
        List<ProductAvailability> availability = getProductAvailability()

        then:
        assert availability.size() == 0
    }

    void 'record stock can set the stock for multiple items'() {
        when:
        recordStockApiWrapper.saveRecordStockOK(facility, new RecordInventoryCommandTestBuilder()
                .product(product)
                .inventory(facility.inventory)
                .transactionDateNow()
                .row('TEST-LOT-1', new Date(), null, 10)
                .row('TEST-LOT-2', null,       null, 20)
                .build())

        and:
        List<ProductAvailability> availability = getProductAvailability()

        then:
        assert availability.size() == 2

        when:
        ProductAvailability lot1Availability = availability.find { it.lotNumber == 'TEST-LOT-1' }

        then:
        assert lot1Availability != null
        assert lot1Availability.inventoryItem?.lotNumber == 'TEST-LOT-1'
        assert lot1Availability.inventoryItem?.expirationDate != null
        assert lot1Availability.quantityOnHand == 10

        when:
        ProductAvailability lot2Availability = availability.find { it.lotNumber == 'TEST-LOT-2' }

        then:
        assert lot2Availability != null
        assert lot2Availability.inventoryItem?.lotNumber == 'TEST-LOT-2'
        assert lot2Availability.inventoryItem?.expirationDate == null
        assert lot2Availability.quantityOnHand == 20
    }

    @Ignore("This doesn't work since product availability is a sum of all of these record stocks instead of only the latest one. Need to figure out what's going on.")
    void 'only the latest record stock applies'() {
        when: 'we set some quantity for the product'
        Instant originalTransactionDate = Instant.now().minusSeconds(100)
        recordStockApiWrapper.saveRecordStockOK(facility, new RecordInventoryCommandTestBuilder()
                .product(product)
                .inventory(facility.inventory)
                .transactionDate(originalTransactionDate)
                .row(null, null, null, 10)
                .build())

        and:
        List<ProductAvailability> availability = getProductAvailability()

        then: 'availability is updated'
        assert availability.size() == 1
        assert availability[0].quantityOnHand == 10

        when: 'we set some quantity for the product BEFORE the previous record stock'
        recordStockApiWrapper.saveRecordStockOK(facility, new RecordInventoryCommandTestBuilder()
                .product(product)
                .inventory(facility.inventory)
                .transactionDate(originalTransactionDate.minusSeconds(10))
                .row(null, null, null, 20)
                .build())

        and:
        availability = getProductAvailability()

        then: 'availability is unchanged'
        assert availability.size() == 1
        assert availability[0].quantityOnHand == 10

        when: 'we set some quantity for the product AFTER the previous record stock'
        recordStockApiWrapper.saveRecordStockOK(facility, new RecordInventoryCommandTestBuilder()
                .product(product)
                .inventory(facility.inventory)
                .transactionDate(originalTransactionDate.plusSeconds(10))
                .row(null, null, null, 40)
                .build())

        and:
        availability = getProductAvailability()

        then: 'availability is updated'
        assert availability.size() == 1
        assert availability[0].quantityOnHand == 40
    }

    List<ProductAvailability> getProductAvailability(){
        ProductAvailability.withNewSession {
            return ProductAvailability.createCriteria().list() {
                eq("location", facility)
                eq("product.id", product.id)
                // We eager fetch all the associated entities so that we don't get LazyInitializationException when
                // trying to access them later in the tests after the hibernate session has been closed.
                join("product")
                join("location")
                join("binLocation")
                join("inventoryItem")
            } as List<ProductAvailability>
        }
    }
}
