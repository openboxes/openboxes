package org.pih.warehouse.api.client.product

import groovy.transform.InheritConstructors
import io.restassured.path.json.JsonPath
import org.grails.web.json.JSONObject
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.ApiWrapper
import org.pih.warehouse.product.Category

@TestComponent
@InheritConstructors
class CategoryApiWrapper extends ApiWrapper<CategoryApi> {

    Category createOK(Category category) {
        String body = new JSONObject()
                .put('name', category.name)
                .put('description', category.description)
                .put('parentCategory', jsonObjectUtil.asIdForRequestBody(category.parentCategory))
                .toString()
        return api.create(body, responseSpecUtil.OK_RESPONSE_SPEC)
                .jsonPath()
                .getObject("", Category.class)
    }

    JsonPath deleteOK(String categoryId) {
        return api.delete(categoryId, responseSpecUtil.NO_CONTENT_RESPONSE_SPEC).jsonPath()
    }
}
