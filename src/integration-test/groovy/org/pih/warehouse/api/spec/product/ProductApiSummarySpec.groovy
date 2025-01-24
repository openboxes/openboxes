package org.pih.warehouse.api.spec.product

import grails.gorm.transactions.Transactional
import io.restassured.builder.ResponseSpecBuilder
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.product.ProductAvailability
import spock.lang.Ignore
import spock.lang.Shared

import org.pih.warehouse.api.client.product.ProductApi
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.product.Product

/**
 * Test the product summary endpoints.
 */
@Ignore("For some reason we get duplicate key errors on product.product_code during setupData even though we're only creating one and the code is randomized. Figure out why then re-enable these tests.")
class ProductApiSummarySpec extends ApiSpec {

    private static final int QUANTITY_ON_HAND = 10

    @Autowired
    ProductApi productApi

    @Shared
    ProductAvailability productAvailability

    @Shared
    Product product

    @Transactional
    void setupData() {
        productAvailability = ProductAvailability.build(quantityOnHand: QUANTITY_ON_HAND, location: location)
        product = productAvailability.product
    }

    void 'get product summary should return correctly given a valid product'() {
        expect:
        productApi.getProductSummary(product.id, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.product.id', Matchers.equalTo(product.id))
                .expectBody("data.location.id", Matchers.equalTo(location.id))
                 .expectBody("data.quantityOnHand", Matchers.equalTo(QUANTITY_ON_HAND))
                .build())
    }
}
