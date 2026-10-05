package org.pih.warehouse.api.client.inventory

import groovy.transform.InheritConstructors
import io.restassured.response.Response
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.ApiWrapper

@TestComponent
@InheritConstructors
class ProductAvailabilityApiWrapper extends ApiWrapper<ProductAvailabilityApi> {

    Response listOK(String facilityId) {
        return api.list(facilityId, responseSpecUtil.OK_RESPONSE_SPEC)
    }

    Response listOK(String facilityId, Integer max, Integer offset) {
        return api.list(facilityId, max, offset, responseSpecUtil.OK_RESPONSE_SPEC)
    }

    Response listExpectingStatus(String facilityId, int statusCode) {
        return api.list(facilityId, responseSpecUtil.buildStatusCodeResponseSpec(statusCode))
    }
}
