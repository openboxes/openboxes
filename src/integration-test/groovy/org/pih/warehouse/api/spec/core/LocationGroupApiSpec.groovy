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
        locationGroupApiWrapper.api.read(INVALID_ID, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    void 'create location group should succeed and return new id when name is provided'() {
        expect:
        locationGroupApiWrapper.createOK(new LocationGroup(name: "New Test Location Group ${UUID.randomUUID()}")) != null
    }

    void 'create location group should return 400 when name exceeds max length of 255 characters'() {
        given:
        String body = new JSONObject()
                .put('name', 'a' * 256)
                .toString()

        expect:
        locationGroupApiWrapper.api.create(body, responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
    }

    void 'update location group should succeed and return updated data when valid data is provided'() {
        given:
        String updatedName = "Updated ${locationGroup.name}"
        JSONObject body = new JSONObject()
                .put('name', updatedName)
                .put('version', locationGroup.version)

        when:
        LocationGroup updated = locationGroupApiWrapper.updateOK(locationGroup.id, body)

        then:
        updated.id == locationGroup.id
        updated.name == updatedName
    }

    void 'update location group should succeed and set address when address is provided'() {
        given:
        JSONObject addressBody = new JSONObject()
                .put('address', '123 Main St')
                .put('city', 'Boston')
                .put('country', 'US')
        JSONObject body = new JSONObject()
                .put('name', locationGroup.name)
                .put('address', addressBody)

        when:
        LocationGroup updated = locationGroupApiWrapper.updateOK(locationGroup.id, body)

        then:
        updated.address.address == '123 Main St'
        updated.address.city == 'Boston'
        updated.address.country == 'US'
    }

    void 'update location group should fail when location group does not exist'() {
        given:
        JSONObject body = new JSONObject()
                .put('name', 'Some Name')
                .put('version', 0)

        expect:
        locationGroupApiWrapper.api.update(INVALID_ID, body.toString(), responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    void 'delete location group should succeed when location group exists'() {
        expect:
        locationGroupApiWrapper.deleteOK(locationGroup.id)
    }

    void 'delete location group should fail when location group does not exist'() {
        expect:
        locationGroupApiWrapper.api.delete(INVALID_ID, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }
}
