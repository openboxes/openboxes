package org.pih.warehouse.component.product

import grails.gorm.transactions.Transactional
import io.restassured.builder.ResponseSpecBuilder
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import spock.lang.Shared

import org.pih.warehouse.component.api.generic.GenercApiWrapper
import org.pih.warehouse.component.api.product.ProductApi
import org.pih.warehouse.component.base.ApiSpec
import org.pih.warehouse.product.Product

/**
 * Test the product demand endpoints.
 */
class ProductApiDemandSpec extends ApiSpec {

    @Shared
    GenercApiWrapper genericApiWrapper

    @Shared
    ProductApi productApi

    @Shared
    Product product

    void setup() {
        genericApiWrapper = new GenercApiWrapper(baseRequestSpec)
        productApi = new ProductApi(baseRequestSpec)
    }

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
