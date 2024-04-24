package org.pih.warehouse.test.api.product

import io.restassured.path.json.JsonPath
import io.restassured.specification.RequestSpecification
import org.json.JSONObject

import org.pih.warehouse.test.api.base.ApiService

class CategoryApiService extends ApiService {

    CategoryApi categoryApi

    private Set<String> createdCategoryIds = []

    CategoryApiService(RequestSpecification defaultRequestSpec) {
        super(defaultRequestSpec)

        categoryApi = new CategoryApi(defaultRequestSpec)
    }

    void cleanup() {
        createdCategoryIds.forEach { categoryId ->
            // TODO: Fails on foreign key errors if we don't clean up our test data first (such as products).
            //       This is a pain to debug and makes our tests annoying to write so we probably need a better
            //       way to clean up test data.
//            deleteOK(categoryId)
        }
    }

    JsonPath listOK() {
        return categoryApi.list(responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
    }

    JsonPath getOK(String categoryId) {
        return categoryApi.get(categoryId, responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
    }

    String createOK(String body) {
        JsonPath json = categoryApi.create(body, responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
        String categoryId = json.getString('id')
        createdCategoryIds.add(categoryId)
        return categoryId
    }

    /**
     * Inserts a category when you don't care about the content, you just need one to exist.
     */
    String createOK() {
        return createOK(new JSONObject()
                .put('name', randomUtil.randomFieldValue('name'))
                .put('description', randomUtil.randomFieldValue('description'))
                .toString())
    }

    JsonPath deleteOK(String categoryId) {
        JsonPath json = categoryApi.delete(categoryId, responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
        createdCategoryIds.remove(json.get('id'))
        return json
    }
}
