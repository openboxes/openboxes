package org.pih.warehouse.api.client.core

import groovy.transform.InheritConstructors
import io.restassured.response.Response
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.ApiWrapper

@TestComponent
@InheritConstructors
class LocationApiWrapper extends ApiWrapper<LocationApi> {

    Response getAvailableItemsOK(String locationId) {
        return api.getAvailableItems(locationId, responseSpecUtil.OK_RESPONSE_SPEC)
    }

    Response getAvailableItemsOK(String locationId, Integer max, Integer offset) {
        return api.getAvailableItems(locationId, max, offset, responseSpecUtil.OK_RESPONSE_SPEC)
    }

    Response getAvailableItemsExpectingStatus(String locationId, int statusCode) {
        return api.getAvailableItems(locationId, responseSpecUtil.buildStatusCodeResponseSpec(statusCode))
    }
}
