package org.pih.warehouse.api.client

import groovy.transform.InheritConstructors
import io.restassured.path.json.JsonPath
import org.grails.web.json.JSONObject
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.ApiWrapper

@TestComponent
@InheritConstructors
class TestingDatesApiWrapper extends ApiWrapper<TestingDatesApi> {

    JsonPath testDatesOK(JSONObject body) {
        return api.testDates(body.toString(), responseSpecUtil.OK_RESPONSE_SPEC)
                .jsonPath()
    }
}
