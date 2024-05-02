package org.pih.warehouse.component.product

import grails.gorm.transactions.Transactional
import io.restassured.builder.ResponseSpecBuilder
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import org.json.JSONObject
import spock.lang.Shared

import org.pih.warehouse.component.api.generic.GenercApiWrapper
import org.pih.warehouse.component.api.product.ProductApi
import org.pih.warehouse.component.base.ApiSpec
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductType
import org.pih.warehouse.util.generic.GenericResource

class ProductApiSpec extends ApiSpec {

    @Shared
    GenercApiWrapper genericApiWrapper

    @Shared
    ProductApi productApi

    @Shared
    Category category

    @Shared
    ProductType productType

    @Shared
    Product product

    void setup() {
        genericApiWrapper = new GenercApiWrapper(baseRequestSpec)
        productApi = new ProductApi(baseRequestSpec)
    }

    @Transactional
    void setupData() {
        product = findOrBuild(Product)
        category = product.category
        productType = product.productType
    }

    void 'get product via generic API'() {
        expect:
        genericApiWrapper.getOK(GenericResource.PRODUCT, product.id)
    }

    void 'get non-existing product via generic API'() {
        expect:
        genericApiWrapper.get404(GenericResource.PRODUCT, INVALID_ID)
    }

    void 'create product via generic API'() {
        given:
        JSONObject createBody = new JSONObject()
                .put('name', randomUtil.randomFieldValue('name'))
                .put('productCode', randomUtil.randomFieldValue('productCode'))
                .put('category', new JSONObject()
                        .put('id', category.id))
                .put('productType', new JSONObject()
                        .put('id', productType.id))
                // TODO: Fill other fields

        expect:
        genericApiWrapper.createOK(GenericResource.PRODUCT, createBody)
    }

    void 'get product list'() {
        expect:
        productApi.list(new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('size()', Matchers.greaterThan(0))
                .expectBody("data.id", Matchers.hasItem(product.id))
                .build())
    }

    void 'get product demand'() {
        expect:
        productApi.getDemand(product.id, new ResponseSpecBuilder()
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
        expect:
        productApi.getDemandSummary(product.id, new ResponseSpecBuilder()
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
        expect:
        productApi.getProductSummary(product.id, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.product.id', Matchers.equalTo(product.id))
                // TODO: Assert other fields
                .build())
    }
}
