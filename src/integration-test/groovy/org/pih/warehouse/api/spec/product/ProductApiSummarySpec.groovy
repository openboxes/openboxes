package org.pih.warehouse.api.spec.product

import io.restassured.builder.ResponseSpecBuilder
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Ignore

import org.pih.warehouse.api.client.product.ProductApiWrapper
import org.pih.warehouse.api.spec.base.ApiSpec

/**
 * Test the product summary endpoints.
 */
@Ignore("Until we can have our APIs update product availability sequentially these tests are too flaky to be enabled.")
class ProductApiSummarySpec extends ApiSpec {

    private static final int QUANTITY_ON_HAND = 10

    @Autowired
    ProductApiWrapper productApiWrapper

    void 'get product summary should return correctly given a valid product'() {
        given:
        setStock(product, null, null, QUANTITY_ON_HAND)

        expect:
        productApiWrapper.api.getProductSummary(product.id, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody("data.product.id", Matchers.equalTo(product.id))
                .expectBody("data.location.id", Matchers.equalTo(facility.id))
                .expectBody("data.quantityOnHand", Matchers.equalTo(QUANTITY_ON_HAND))
                .build())
    }
}
