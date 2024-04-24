package org.pih.warehouse.test.product

import io.restassured.builder.ResponseSpecBuilder
import io.restassured.path.json.JsonPath
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import org.json.JSONObject
import spock.lang.Shared

import org.pih.warehouse.test.api.base.ApiService
import org.pih.warehouse.test.api.generic.GenericApiService
import org.pih.warehouse.test.api.product.CategoryApiService
import org.pih.warehouse.test.api.product.ProductApi
import org.pih.warehouse.test.base.ApiSpec
import org.pih.warehouse.test.util.generic.GenericResource

class ProductApiSpec extends ApiSpec {

    @Shared
    GenericApiService genericApiService

    @Shared
    CategoryApiService categoryApiService

    @Shared
    ProductApi productApi

    @Shared
    String categoryId

    void setupSpec() {
        genericApiService = new GenericApiService(baseRequestSpec)
        categoryApiService = new CategoryApiService(baseRequestSpec)
        productApi = new ProductApi(baseRequestSpec)

        categoryId = categoryApiService.createOK()
    }

    List<ApiService> registerApiServices() {
        return [genericApiService, categoryApiService]
    }

    void 'product CRUD using generic API'() {
        given: 'a product to create'
        JSONObject createBody = createProductRequestBody()

        expect: 'create succeeds'
        String productId = createProduct(createBody)

        and: 'get by id succeeds'
        genericApiService.getOK(GenericResource.PRODUCT, productId)

        and: 'delete succeeds'
        genericApiService.deleteOK(GenericResource.PRODUCT, productId)

        and: 'get by id finds nothing'
        genericApiService.get404(GenericResource.PRODUCT, productId)
    }

    void 'get product list'() {
        given: 'a product to create'
        JSONObject createBody = createProductRequestBody()

        expect: 'create succeeds'
        String productId = createProduct(createBody)

        and: 'the product should be returned from the list call'
        productApi.list(new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('size()', Matchers.greaterThan(0))
                .expectBody("data.id", Matchers.hasItem(productId))
                .build())
    }

    void 'get product demand'() {
        given: 'a product to create'
        JSONObject createBody = createProductRequestBody()

        expect: 'create succeeds'
        String productId = createProduct(createBody)

        and: 'get product demand returns as expected'
        productApi.getDemand(productId, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                // TODO: Assert other fields
                .build())

//        data :
//                totalDemand     : totalDemand,
//                totalDays       : demandPeriod,
//                dailyDemand     : dailyDemand,
//                monthlyDemand   : new BigDecimal(monthlyDemand).setScale(0, RoundingMode.HALF_UP),
//                onHandMonths    : onHandMonths,
//                quantityOnHand  : quantityOnHand
//
    }

    void 'get product demand summary'() {
        given: 'a product to create'
        JSONObject createBody = createProductRequestBody()

        expect: 'create succeeds'
        String productId = createProduct(createBody)

        and: 'get product demand summary returns as expected'
        productApi.getDemandSummary(productId, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                // TODO: Assert other fields
                .build())
//        data [
//                dateKey       : "",
//                year          : "Average Monthly",
//                month         : "",
//                monthName     : "",
//                quantityDemand: "${numberFormat.format(totalDemand / numberOfDays * 30)}",
//        ]
    }

    void 'get product summary'() {
        given: 'a product to create'
        JSONObject createBody = createProductRequestBody()

        expect: 'create succeeds'
        String productId = createProduct(createBody)

        and: 'get product summary returns as expected'
        productApi.getProductSummary(productId, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.product.id', Matchers.equalTo(productId))
                // TODO: Assert other fields
                .build())
    }

    JSONObject createProductRequestBody(){
        return new JSONObject()
                .put('name', randomUtil.randomFieldValue('name'))
                .put('productCode', randomUtil.randomFieldValue('productCode'))
                .put('category', new JSONObject()
                        .put('id', categoryId))
                .put('productType', new JSONObject()
                        .put('id', 'DEFAULT'))
    }

    /**
     *  TODO: We need the id of the created product but the generic create endpoint doesn't return it so we have to
     *        get all products then find the product we created and extract its id. Remove this method once
     *        the generic API returns the id.
     */
    String createProduct(JSONObject createBody) {
        genericApiService.createOK(GenericResource.PRODUCT, createBody)

        String productName = createBody.getString('name')
        JsonPath listJson = genericApiService.listOK(GenericResource.PRODUCT)
        return jsonPathUtil.extractFieldFromListGivenCriteria('id', 'name', productName, listJson)
    }
}
