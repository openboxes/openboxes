package org.pih.warehouse.api.spec.product

import io.restassured.builder.ResponseSpecBuilder
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import org.grails.web.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Ignore
import spock.lang.Shared

import org.pih.warehouse.api.client.product.CategoryApiWrapper
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.common.domain.builder.product.CategoryTestBuilder
import org.pih.warehouse.product.Category

/**
 * Test the basic CRUD endpoints for product categories.
 */
class CategoryApiCRUDSpec extends ApiSpec {

    @Autowired
    CategoryApiWrapper categoryApiWrapper

    @Shared
    Category category

    void setupData() {
        category = new CategoryTestBuilder()
                .build(true)
    }

    void cleanupData() {
        category.delete()
    }

    void 'get category by id should succeed when category exists'() {
        expect:
        categoryApiWrapper.api.get(category.id, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('id', Matchers.equalTo(category.id))
                .expectBody('name', Matchers.equalTo(category.name))
                .expectBody('description', Matchers.equalTo(category.description))
                .build())
    }

    void 'get category by id should fail when category does not exist'() {
        expect:
        categoryApiWrapper.api.get(INVALID_ID, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    void 'create category should succeed when fields are valid'() {
        given:
        String createBody = new JSONObject()
                .put('name', randomUtil.randomStringFieldValue('name'))
                .put('description', randomUtil.randomStringFieldValue('description'))
                .toString()

        expect:
        categoryApiWrapper.api.create(createBody, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('id', Matchers.notNullValue())
                .build())
    }

    @Ignore("Delete fails with: Batch update returned unexpected row count from update [0]; actual row count: 0; expected: 1. Fix this then re-enable the test.")
    void 'delete category should succeed when category exists'() {
        expect:
        categoryApiWrapper.deleteOK(category.id)
    }

    void 'delete category should fail when category does not exist'() {
        expect:
        categoryApiWrapper.api.delete(INVALID_ID, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    void 'list categories should successfully return all categories'() {
        expect:
        categoryApiWrapper.api.list(new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('size()', Matchers.greaterThan(0))
                .expectBody("data.id", Matchers.hasItem(category.id))
                .build())
    }
}
