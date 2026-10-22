package org.pih.warehouse.api.client.product

import groovy.transform.InheritConstructors
import io.restassured.response.Response
import org.apache.http.HttpStatus
import org.grails.web.json.JSONObject
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.ApiWrapper
import org.pih.warehouse.product.ProductPackage
import org.pih.warehouse.product.ProductPackageCommand

@TestComponent
@InheritConstructors
class ProductPackageApiWrapper extends ApiWrapper<ProductPackageApi> {

    ProductPackage createOK(ProductPackageCommand command) {
        return create(command, HttpStatus.SC_OK)
                .jsonPath()
                .getObject("data.defaultProductPackage", ProductPackage.class)
    }

    Response create(ProductPackageCommand command, int statusCode) {
        String body = new JSONObject()
                .put('productSupplier', jsonObjectUtil.asIdForRequestBody(command.productSupplier))
                .put('uom', jsonObjectUtil.asIdForRequestBody(command.uom))
                .put('productPackageQuantity', command.productPackageQuantity)
                .put('minOrderQuantity', command.minOrderQuantity)
                .put('productPackagePrice', command.productPackagePrice)
                .put('contractPricePrice', command.contractPricePrice)
                .put('contractPriceValidUntil', jsonObjectUtil.asDateForRequestBody(command.contractPriceValidUntil))
                .put('tieredPricing', command.tieredPricing)
                .toString()
        return api.create(body, responseSpecUtil.buildStatusCodeResponseSpec(statusCode))
    }
}
