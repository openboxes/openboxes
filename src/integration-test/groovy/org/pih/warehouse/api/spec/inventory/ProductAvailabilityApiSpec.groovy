package org.pih.warehouse.api.spec.inventory

import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.client.inventory.ProductAvailabilityApiWrapper
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.api.util.ResponseSpecUtil

class ProductAvailabilityApiSpec extends ApiSpec {

    @Autowired
    ProductAvailabilityApiWrapper productAvailabilityApiWrapper

    @Autowired
    ResponseSpecUtil responseSpecUtil

    void 'list returns stock for the facility with required fields'() {
        given:
        setStock(product, null, null, 10)

        when:
        def response = productAvailabilityApiWrapper.listOK(facility.id)
        List data = response.jsonPath().getList("data")

        then:
        assert data.size() >= 1
        def row = data.find { it.productCode == product.productCode }

        assert row != null
        assert row["product.name"] == product.name
        assert row.productCode == product.productCode
        assert row.quantityOnHand == 10
        assert row.containsKey("quantityAvailable")
        assert row.containsKey("lotNumber")
        assert row.containsKey("expirationDate")
        assert row.containsKey("binLocation")
        assert row.containsKey("zone")
    }

    void 'list excludes zero quantity on hand rows'() {
        given:
        setStock(product, null, null, 0)

        when:
        def response = productAvailabilityApiWrapper.listOK(facility.id)
        List data = response.jsonPath().getList("data")

        then:
        assert !data.any { it.productCode == product.productCode && it.quantityOnHand == 0 }
    }

    void 'list returns error for unknown facility id'() {
        expect:
        productAvailabilityApiWrapper.listExpectingStatus(INVALID_ID, HttpStatus.SC_BAD_REQUEST)
    }
}
