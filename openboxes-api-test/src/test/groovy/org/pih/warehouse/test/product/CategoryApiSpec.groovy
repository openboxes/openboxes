package org.pih.warehouse.test.product

import io.restassured.builder.ResponseSpecBuilder
import io.restassured.response.Response
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import org.json.JSONObject
import spock.lang.Shared

import org.pih.warehouse.test.api.base.ApiService
import org.pih.warehouse.test.api.product.CategoryApi
import org.pih.warehouse.test.base.ApiSpec

class CategoryApiSpec extends ApiSpec {

    @Shared
    CategoryApi categoryApi

    void setupSpec() {
        categoryApi = new CategoryApi(baseRequestSpec)
    }

    List<ApiService> registerApiServices() {
        return []
    }

    void 'product CRUD'() {
        given: 'a category to create'
        JSONObject createBody = createCategoryRequestBody()
        String categoryName = createBody.get('name')
        String categoryDesc = createBody.get('description')

        expect: 'create succeeds'
        Response createResponse = categoryApi.create(createBody.toString(), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('id', Matchers.notNullValue())
                .build())

        when: 'we use the category id we just created'
        String categoryId = createResponse.body().jsonPath().getString('id')

        then: 'get by id succeeds'
        categoryApi.get(categoryId, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('id', Matchers.equalTo(categoryId))
                .expectBody('name', Matchers.equalTo(categoryName))
                .expectBody('description', Matchers.equalTo(categoryDesc))
                .build())

        // TODO: In CategoryApiController the line "category.properties = params" isn't working because
        //       there are no fields in the params, only in the request body. Are we expected to provide
        //       the fields to update as params, or should we switch it to pulling the actual request body?
//        when: 'we change the category'
//        String updatedCategoryName = 'testNameUpdated'
//        String updatedCategoryDesc = 'testDescUpdated'
//        String updateBody = new JSONObject()
//                .put('id', categoryId)
//                .put('name', updatedCategoryName)
//                .put('description', updatedCategoryDesc)
//                .toString()
//
//        then: 'update succeeds'
//        categoryApi.update(categoryId, updateBody, new ResponseSpecBuilder()
//                .expectStatusCode(HttpStatus.SC_OK)
//                .expectBody('id', Matchers.equalTo(categoryId))
//                .expectBody('name', Matchers.equalTo(updatedCategoryName))
//                .expectBody('description', Matchers.equalTo(updatedCategoryDesc))
//                .build())

        and: 'delete succeeds'
        categoryApi.delete(categoryId, responseSpecUtil.NO_CONTENT_RESPONSE_SPEC)

        and: 'get by id finds nothing'
        categoryApi.get(categoryId, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    void 'list categories'() {
        given: 'a category to create'
        JSONObject createBody = createCategoryRequestBody()

        expect: 'create succeeds'
        Response createResponse = categoryApi.create(createBody.toString(), responseSpecUtil.OK_RESPONSE_SPEC)

        when: 'we get the category id we just created'
        String categoryId = createResponse.body().jsonPath().getString('id')

        then: 'the category should be returned from the list call'
        categoryApi.list(new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('size()', Matchers.greaterThan(0))
                .expectBody("data.id", Matchers.hasItem(categoryId))
                .build())
    }

    JSONObject createCategoryRequestBody() {
        return new JSONObject()
                .put('name', randomUtil.randomFieldValue('name'))
                .put('description', randomUtil.randomFieldValue('description'))
    }
}





