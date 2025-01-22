package org.pih.warehouse.api.client.product

import groovy.transform.InheritConstructors
import io.restassured.response.Response
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.ApiWrapper
import org.pih.warehouse.product.ProductClassificationDto

@TestComponent
@InheritConstructors
class ProductClassificationApiWrapper extends ApiWrapper<ProductClassificationApi> {

    List<ProductClassificationDto> listOK(String facilityId) {
        return api.list(facilityId, responseSpecUtil.OK_RESPONSE_SPEC)
                .jsonPath()
                .getList("data", ProductClassificationDto.class)
    }

    Response list(String facilityId, int statusCode) {
        return api.list(facilityId, responseSpecUtil.buildStatusCodeResponseSpec(statusCode))
    }
}
