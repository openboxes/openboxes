package org.pih.warehouse.component.product

import grails.gorm.transactions.Transactional
import io.restassured.builder.ResponseSpecBuilder
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import org.json.JSONObject
import spock.lang.Ignore
import spock.lang.Shared

import org.pih.warehouse.component.api.product.CategoryApi
import org.pih.warehouse.component.base.ApiSpec
import org.pih.warehouse.product.Category

class CategoryApiSpec extends ApiSpec {

    @Shared
    CategoryApi categoryApi

    @Shared
    Category category

    void setup() {
        categoryApi = new CategoryApi(baseRequestSpec)
    }

    @Transactional
    void setupData() {
        category = findOrBuild(Category)
    }

    @Transactional
    void cleanupData() {
        category.delete()
    }

    void 'get category'() {
        expect:
        categoryApi.get(category.id, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('id', Matchers.equalTo(category.id))
                .expectBody('name', Matchers.equalTo(category.name))
                .expectBody('description', Matchers.equalTo(category.description))
                // TODO: Assert other fields
                .build())
    }

    void 'get non-existing category'() {
        expect:
        categoryApi.get(INVALID_ID, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    void 'create category'() {
        given:
        String createBody = new JSONObject()
                .put('name', randomUtil.randomFieldValue('name'))
                .put('description', randomUtil.randomFieldValue('description'))
                // TODO: Fill other fields
                .toString()

        expect:
        categoryApi.create(createBody, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('id', Matchers.notNullValue())
                .build())
    }

    @Ignore("Delete fails with: Batch update returned unexpected row count from update [0]; actual row count: 0; expected: 1. Fix this then re-enable the test.")
    void 'delete category'() {
        expect:
        categoryApi.delete(category.id, responseSpecUtil.NO_CONTENT_RESPONSE_SPEC)
    }

    void 'delete non-existing category'() {
        expect:
        categoryApi.delete(INVALID_ID, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    void 'list categories'() {
        expect:
        categoryApi.list(new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('size()', Matchers.greaterThan(0))
                .expectBody("data.id", Matchers.hasItem(category.id))
                .build())
    }
}
