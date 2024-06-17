package org.pih.warehouse.api.spec.product

import grails.gorm.transactions.Transactional
import io.restassured.builder.ResponseSpecBuilder
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Shared

import org.pih.warehouse.api.client.product.ProductApi
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.product.Product

/**
 * Test the product demand endpoints.
 */
class ProductApiDemandSpec extends ApiSpec {

    @Autowired
    ProductApi productApi

    @Shared
    Product product

    @Transactional
    void setupData() {
        product = Product.build()
    }

    void 'get product demand returns nothing when no data exists for the product'() {
        expect:
        productApi.getDemand(product.id, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody("data.totalDemand", Matchers.equalTo(null))
                .expectBody("data.totalDays", Matchers.equalTo(null))
                .expectBody("data.dailyDemand", Matchers.equalTo(null))
                .expectBody("data.monthlyDemand", Matchers.equalTo(null))
                .expectBody("data.onHandMonths", Matchers.equalTo(null))
                .expectBody("data.quantityOnHand", Matchers.equalTo(null))
                .build())
    }

    void 'get product demand summary returns nothing when no data exists for the product'() {
        expect:
        productApi.getDemandSummary(product.id, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody("data", Matchers.equalTo([]))
                .build())
    }

    // TODO: Add more tests that verify a real product summary.
}
