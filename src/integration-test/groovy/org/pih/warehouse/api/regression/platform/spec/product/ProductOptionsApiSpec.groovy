package org.pih.warehouse.api.regression.platform.spec.product

import spock.lang.PendingFeature



import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import org.pih.warehouse.api.regression.platform.client.product.RegressionProductClassificationApi
import org.pih.warehouse.api.spec.product.*
import io.restassured.builder.ResponseSpecBuilder
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.regression.platform.client.core.SelectOptionsApi
import org.pih.warehouse.core.RatingTypeCode
import org.pih.warehouse.product.Product

/**
 * The option lists of the product source pages, and product classifications asked for by query parameter.
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation
class ProductOptionsApiSpec extends ProductSourceSpec {

    @Autowired
    SelectOptionsApi regressionSelectOptionsApi

    @Autowired
    RegressionProductClassificationApi regressionProductClassificationApi

    void 'preference type options list the preference types, with Multiple and None when asked'() {
        expect:
        regressionSelectOptionsApi.options('/preferenceTypeOptions', [:], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(preferred.id))
                .expectBody('data.id', Matchers.not(Matchers.hasItems('MULTIPLE')))
                .expectBody('data.id', Matchers.not(Matchers.hasItems('NONE')))
                .expectBody("data.find { it.id == '${preferred.id}' }.label".toString(), Matchers.equalTo(preferred.name))
                .build())
        regressionSelectOptionsApi.options('/preferenceTypeOptions', [includeMultiple: true, includeNone: true], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data[0].id', Matchers.equalTo('MULTIPLE'))
                .expectBody('data[1].id', Matchers.equalTo('NONE'))
                .expectBody('data.id', Matchers.hasItem(preferred.id))
                .build())
    }

    void 'rating type options list every rating'() {
        expect:
        regressionSelectOptionsApi.options('/ratingTypeCodeOptions', [:], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.contains(*RatingTypeCode.list()*.name()))
                .expectBody('data.value', Matchers.contains(*RatingTypeCode.list()*.name()))
                .build())
    }

    void 'product classifications are listed for the facility given as a query parameter'() {
        given:
        String abcClass = "Z${UUID.randomUUID().toString().take(6)}"
        Product.withNewTransaction { Product.get(item.id).abcClass = abcClass }

        expect:
        regressionProductClassificationApi.listAll([facilityId: facility.id], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.name', Matchers.hasItem(abcClass))
                .build())
    }

    @PendingFeature(reason = 'classifications without/with an unknown facility answer 500')
    void 'product classifications without a facility answer 400, with an unknown one 404'() {
        expect:
        regressionProductClassificationApi.listAll([:], error(HttpStatus.SC_BAD_REQUEST))
        regressionProductClassificationApi.listAll([facilityId: INVALID_ID], error(HttpStatus.SC_NOT_FOUND))
    }
}
