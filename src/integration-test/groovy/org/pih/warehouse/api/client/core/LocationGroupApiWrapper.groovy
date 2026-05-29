package org.pih.warehouse.api.client.core

import groovy.transform.InheritConstructors
import io.restassured.path.json.JsonPath
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.ApiWrapper

@TestComponent
@InheritConstructors
class LocationGroupApiWrapper extends ApiWrapper<LocationGroupApi> {

    JsonPath listOK() {
        return api.list(responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
    }

    JsonPath readOK(String id) {
        return api.read(id, responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
    }
}