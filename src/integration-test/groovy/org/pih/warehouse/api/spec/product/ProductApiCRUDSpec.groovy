package org.pih.warehouse.api.spec.product

import io.restassured.builder.ResponseSpecBuilder
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import org.grails.web.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.client.generic.GenericApiWrapper
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.api.client.generic.GenericResource

/**
 * Test the basic CRUD endpoints for products.
 */
class ProductApiCRUDSpec extends ApiSpec {

    @Autowired
    GenericApiWrapper genericApiWrapper

    void 'get product by id should succeed when product exists'() {
        expect:
        genericApiWrapper.getOK(GenericResource.PRODUCT, product.id)
    }

    void 'get product by id should fail when product does not exist'() {
        expect:
        genericApiWrapper.get404(GenericResource.PRODUCT, INVALID_ID)
    }

    void 'create product should succeed when fields are valid'() {
        given: 'a request body'
        String body = new JSONObject()
                .put('name', 'Test Name')
                .put('description', 'A product to be used by tests. Can be deleted safely.')
                .put('active', true)
                .put('pricePerUnit', 1)
                .put('costPerUnit', 1)
                .put('abcClass', 'Test ABC Class')
                .put('unitOfMeasure', 'each')
                .put('upc', '012345678905')
                .put('ndc', '11111-111-11')
                .put('manufacturer', 'Test Manufacturer')
                .put('manufacturerCode', 'TEST-MANU-CODE-123')
                .put('manufacturerName', 'Test Product Manufacturer Name')
                .put('brandName', 'Test Product Brand Name')
                .put('modelNumber', 'Test Model Number')
                .put('vendor', 'Test Vendor')
                .put('vendorCode', 'TEST-VENDOR-CODE-123')
                .put('vendorName', 'Test Product Vendor Name')
                .put('color', 'red')
                .put('coldChain', false)
                .put('controlledSubstance', false)
                .put('hazardousMaterial', false)
                .put('reconditioned', false)
                .put('lotAndExpiryControl', false)
                // Use existing categories and product types
                .put('category', jsonObjectUtil.asIdForRequestBody(product.category))
                .put('productType', jsonObjectUtil.asIdForRequestBody(product.productType))
                .toString()

        expect: 'creating the product succeeds and returns the fields as they were input'
        productApiWrapper.api.save(body, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('product.id', Matchers.notNullValue())
                .expectBody('product.productCode', Matchers.notNullValue())  // an identifier was assigned
                .expectBody('product.name', Matchers.equalTo('Test Name'))
                .expectBody('product.description', Matchers.equalTo('A product to be used by tests. Can be deleted safely.'))
                .expectBody('product.active', Matchers.equalTo(true))
                .expectBody('product.pricePerUnit', Matchers.equalTo(1))
                .expectBody('product.costPerUnit', Matchers.equalTo(1))
                .expectBody('product.abcClass', Matchers.equalTo('Test ABC Class'))
                .expectBody('product.unitOfMeasure', Matchers.equalTo('each'))
                .expectBody('product.upc', Matchers.equalTo('012345678905'))
                .expectBody('product.ndc', Matchers.equalTo('11111-111-11'))
                .expectBody('product.manufacturer', Matchers.equalTo('Test Manufacturer'))
                .expectBody('product.manufacturerCode', Matchers.equalTo('TEST-MANU-CODE-123'))
                .expectBody('product.manufacturerName', Matchers.equalTo('Test Product Manufacturer Name'))
                .expectBody('product.brandName', Matchers.equalTo('Test Product Brand Name'))
                .expectBody('product.modelNumber', Matchers.equalTo('Test Model Number'))
                .expectBody('product.vendor', Matchers.equalTo('Test Vendor'))
                .expectBody('product.vendorCode', Matchers.equalTo('TEST-VENDOR-CODE-123'))
                .expectBody('product.vendorName', Matchers.equalTo('Test Product Vendor Name'))
                .expectBody('product.color', Matchers.equalTo('red'))
                .expectBody('product.coldChain', Matchers.equalTo(false))
                .expectBody('product.controlledSubstance', Matchers.equalTo(false))
                .expectBody('product.hazardousMaterial', Matchers.equalTo(false))
                .expectBody('product.reconditioned', Matchers.equalTo(false))
                .expectBody('product.lotAndExpiryControl', Matchers.equalTo(false))
                .expectBody('product.category.id', Matchers.equalTo(product.category?.id))
                .expectBody('product.productType.id', Matchers.equalTo(product.productType?.id))
                .build())
    }

    void 'get product list should successfully return all products'() {
        expect:
        productApiWrapper.api.list(new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                // There could be any number of products in the database so we don't assert on a specific number.
                // However we do know there will be at least 1 (the one we create in ApiSpec) so check for that one.
                .expectBody('size()', Matchers.greaterThan(0))
                .expectBody("data.id", Matchers.hasItem(product.id))
                .build())
    }
}
