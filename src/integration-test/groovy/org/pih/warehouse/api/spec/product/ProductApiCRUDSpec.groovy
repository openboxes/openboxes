package org.pih.warehouse.api.spec.product

import grails.gorm.transactions.Transactional
import io.restassured.builder.ResponseSpecBuilder
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import org.grails.web.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Shared

import org.pih.warehouse.api.client.generic.GenericApiWrapper
import org.pih.warehouse.api.client.product.ProductApi
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductType
import org.pih.warehouse.api.client.generic.GenericResource

/**
 * Test the basic CRUD endpoints for products.
 */
class ProductApiCRUDSpec extends ApiSpec {

    @Autowired
    GenericApiWrapper genericApiWrapper

    @Autowired
    ProductApi productApi

    @Shared
    Category category

    @Shared
    ProductType productType

    @Shared
    Product product

    @Transactional
    void setupData() {
        product = Product.build()
        category = product.category
        productType = product.productType
    }

    void 'get product by id should succeed when product exists'() {
        expect:
        genericApiWrapper.getOK(GenericResource.PRODUCT, product.id)
    }

    void 'get product by id should fail when product does not exist'() {
        expect:
        genericApiWrapper.get404(GenericResource.PRODUCT, INVALID_ID)
    }

    void 'create product should succeed when fields are valid'() {
        given:
        JSONObject createBody = new JSONObject()
                .put('productCode', randomUtil.randomStringFieldValue('productCode'))
                .put('name', 'Test Name')
                .put('description', 'A product to be used by tests. Can be deleted safely.')
                .put('pricePerUnit', 1)
                .put('costPerUnit', 1)
                .put('unitOfMeasure', 'each')
                .put('upc', '012345678905')
                .put('ndc', '11111-111-11')
                .put('manufacturer', 'Test Manufacturer')
                .put('manufacturerCode', 'TEST-MANU-CODE-123"')
                .put('manufacturerName', 'Test Product Manufacturer Name')
                .put('brandName', 'Test Product Brand Name')
                .put('vendor', 'Test Vendor')
                .put('vendorCode', 'TEST-VENDOR-CODE-123')
                .put('vendorName', 'Test Product Vendor Name')
                .put('color', 'red')
                .put('category', new JSONObject()
                        .put('id', category.id))
                .put('productType', new JSONObject()
                        .put('id', productType.id))

        expect:
        genericApiWrapper.createOK(GenericResource.PRODUCT, createBody)
    }

    void 'get product list should successfully return all products'() {
        expect:
        productApi.list(new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('size()', Matchers.greaterThan(0))
                .expectBody("data.id", Matchers.hasItem(product.id))
                .build())
    }
}
