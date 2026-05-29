package org.pih.warehouse.api.spec.core

import io.restassured.builder.ResponseSpecBuilder
import org.apache.http.HttpStatus
import org.grails.web.json.JSONObject
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Shared

import org.pih.warehouse.api.client.core.LocationGroupApiWrapper
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.common.domain.builder.core.LocationGroupTestBuilder
import org.pih.warehouse.core.LocationGroup

class LocationGroupApiSpec extends ApiSpec {

    @Autowired
    LocationGroupApiWrapper locationGroupApiWrapper

    @Shared
    LocationGroup locationGroup

    @Override
    void setupData() {
        locationGroup = new LocationGroupTestBuilder().build(true)
    }

    @Override
    void cleanupData() {
        LocationGroup.get(locationGroup.id)?.delete()
    }

    void 'list location groups should return all location groups including the one created in setup'() {
        expect:
        locationGroupApiWrapper.api.list(new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(locationGroup.id))
                .build())
    }

    void 'list location groups with q filter matching name should include matching location group'() {
        expect:
        locationGroupApiWrapper.api.list(locationGroup.name, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(locationGroup.id))
                .build())
    }

    void 'list location groups with q filter not matching should not include location group'() {
        expect:
        locationGroupApiWrapper.api.list("ZZZZZZ_NO_MATCH_ZZZZZZ", new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.not(Matchers.hasItem(locationGroup.id)))
                .build())
    }

    void 'read location group by id should return correct data when location group exists'() {
        expect:
        locationGroupApiWrapper.api.read(locationGroup.id, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.equalTo(locationGroup.id))
                .expectBody('data.name', Matchers.equalTo(locationGroup.name))
                .build())
    }

    void 'read location group by id should fail when location group does not exist'() {
        expect:
        locationGroupApiWrapper.api.read(INVALID_ID, responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_INTERNAL_SERVER_ERROR))
    }

    void 'create location group should succeed and return new id when name is provided'() {
        given:
        String body = new JSONObject()
                .put('name', "New Test Location Group ${UUID.randomUUID()}")
                .toString()

        expect:
        locationGroupApiWrapper.api.create(body, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.notNullValue())
                .build())
    }

    void 'create location group should return 400 when name exceeds max length of 255 characters'() {
        given:
        String body = new JSONObject()
                .put('name', 'a' * 256)
                .toString()

        expect:
        locationGroupApiWrapper.api.create(body, responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
    }

    void 'delete location group should succeed when location group exists'() {
        expect:
        locationGroupApiWrapper.api.delete(locationGroup.id, responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_NO_CONTENT))
    }

    void 'delete location group should fail when location group does not exist'() {
        expect:
        locationGroupApiWrapper.api.delete(INVALID_ID, responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_INTERNAL_SERVER_ERROR))
    }
}
