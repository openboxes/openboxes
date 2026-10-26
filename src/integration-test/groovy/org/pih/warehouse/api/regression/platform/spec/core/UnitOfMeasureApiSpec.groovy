package org.pih.warehouse.api.regression.platform.spec.core

import spock.lang.PendingFeature


import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import org.pih.warehouse.api.spec.core.*
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import org.apache.http.HttpStatus
import org.grails.web.json.JSONObject
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.regression.platform.client.core.UnitOfMeasureApi
import org.pih.warehouse.api.spec.base.ApiSpec

// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation
class UnitOfMeasureApiSpec extends RegressionApiSpec {

    @Autowired
    UnitOfMeasureApi unitOfMeasureApi

    private String createUnit() {
        String code = "T${UUID.randomUUID().toString().take(8)}"
        Response created = unitOfMeasureApi.create(new JSONObject()
                .put('name', "Test unit ${code}".toString())
                .put('code', code)
                .put('description', 'A unit of measure created by a test')
                .toString(), responseSpecUtil.CREATED_RESPONSE_SPEC)
        return created.jsonPath().getString('data.id')
    }

    void 'create unit of measure answers 201 with the new unit'() {
        expect:
        createUnit() != null
    }

    void 'create unit of measure answers 400 without a code'() {
        expect:
        unitOfMeasureApi.create(new JSONObject().put('name', 'No code').toString(),
                responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
    }

    void 'list units of measure includes a created unit'() {
        given:
        String id = createUnit()

        expect:
        unitOfMeasureApi.list(new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(id))
                .build())
    }

    void 'read unit of measure by id returns it'() {
        given:
        String id = createUnit()

        expect:
        unitOfMeasureApi.read(id, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.equalTo(id))
                .expectBody('data.description', Matchers.equalTo('A unit of measure created by a test'))
                .build())
    }

    void 'read unit of measure answers 404 for an unknown id'() {
        expect:
        unitOfMeasureApi.read(INVALID_ID, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    void 'update unit of measure with #method changes the given fields'() {
        given:
        String id = createUnit()

        expect:
        unitOfMeasureApi.update(id, new JSONObject().put('description', "Updated with ${method}".toString()).toString(),
                method, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.equalTo(id))
                .expectBody('data.description', Matchers.equalTo("Updated with ${method}".toString()))
                .build())

        where:
        method << [Method.PUT, Method.POST]
    }

    void 'update unit of measure answers 404 for an unknown id'() {
        expect:
        unitOfMeasureApi.update(INVALID_ID, '{"description": "x"}', Method.PUT,
                responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    void 'delete unit of measure removes it'() {
        given:
        String id = createUnit()

        when:
        unitOfMeasureApi.delete(id, responseSpecUtil.NO_CONTENT_RESPONSE_SPEC)

        then:
        unitOfMeasureApi.read(id, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    void 'delete unit of measure answers 404 for an unknown id'() {
        expect:
        unitOfMeasureApi.delete(INVALID_ID, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    void 'currencies lists the currency units'() {
        expect:
        unitOfMeasureApi.currencies(new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data', Matchers.notNullValue())
                .build())
    }

    void 'options lists the units of one type as select options'() {
        expect:
        unitOfMeasureApi.options('QUANTITY', new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data', Matchers.notNullValue())
                .build())
    }

    @PendingFeature(reason = 'unit-of-measure options with an unknown type answer 500, not 400')
    void 'options answers 400 for an unknown type'() {
        expect:
        unitOfMeasureApi.options('NOT_A_TYPE', responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
    }
}
