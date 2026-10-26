package org.pih.warehouse.api.regression.platform.spec.product

import spock.lang.PendingFeature



import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import org.pih.warehouse.api.regression.platform.client.generic.RegressionGenericApi
import org.pih.warehouse.api.regression.platform.client.generic.RegressionGenericApiWrapper
import org.pih.warehouse.api.regression.platform.client.generic.RegressionGenericResource
import org.pih.warehouse.api.regression.platform.client.product.RegressionProductApi
import org.pih.warehouse.api.spec.product.*
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import org.grails.web.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.common.domain.builder.product.CategoryTestBuilder
import org.pih.warehouse.common.domain.builder.product.ProductTestBuilder
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product

/**
 * Test the basic CRUD endpoints for products.
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation
class ProductApiCRUDMoreSpec extends RegressionApiSpec {

    @Autowired
    RegressionProductApi regressionProductApi

    @Autowired
    RegressionGenericApiWrapper regressionGenericApiWrapper

    void 'read product by id returns the product'() {
        expect:
        regressionProductApi.read(product.id, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.equalTo(product.id))
                .expectBody('data.name', Matchers.equalTo(product.name))
                .expectBody('data.productCode', Matchers.equalTo(product.productCode))
                .build())
    }

    void 'read product answers 404 for an unknown id'() {
        expect:
        regressionProductApi.read(INVALID_ID, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    void 'update product with #method changes the given fields only'() {
        given:
        Product created = newProduct()
        String body = new JSONObject().put('description', "Updated with ${method}".toString()).toString()

        expect:
        regressionProductApi.update(created.id, body, method, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.equalTo(created.id))
                .expectBody('data.description', Matchers.equalTo("Updated with ${method}".toString()))
                .expectBody('data.name', Matchers.equalTo(created.name))
                .build())

        where:
        method << [Method.PUT, Method.POST]
    }

    void 'update product clears an optional reference sent as null'() {
        given:
        Product created = newProduct()

        expect:
        regressionProductApi.update(created.id, '{"productFamily": null, "glAccount": null}', Method.PUT,
                responseSpecUtil.OK_RESPONSE_SPEC)
    }

    void 'update product answers 404 for an unknown id'() {
        expect:
        regressionProductApi.update(INVALID_ID, '{"description": "x"}', Method.PUT,
                responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    void 'update product answers 400 when a field is invalid'() {
        expect:
        regressionProductApi.update(product.id, new JSONObject().put('name', '').toString(), Method.PUT,
                responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
    }

    void 'delete product removes it'() {
        given:
        Product created = newProduct()

        when:
        regressionProductApi.delete(created.id, responseSpecUtil.NO_CONTENT_RESPONSE_SPEC)

        then:
        regressionProductApi.read(created.id, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    void 'delete product answers 404 for an unknown id'() {
        expect:
        regressionProductApi.delete(INVALID_ID, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    void 'search finds the product by name'() {
        expect:
        regressionProductApi.search([name: product.name], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(product.id))
                .build())
    }

    @PendingFeature(reason = 'availableItems=false acts as true')
    void 'search with availableItems=false is the plain search'() {
        expect:
        regressionProductApi.search([name: product.name, availableItems: false], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(product.id))
                .build())
    }

    @PendingFeature(reason = 'a short search answers a non-JSON body')
    void 'search answers an empty list for a term shorter than three characters'() {
        expect:
        regressionProductApi.search([name: 'Te'], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data', Matchers.empty())
                .build())
    }

    void 'list filters by the q parameter and pages with max and offset'() {
        expect:
        regressionProductApi.list([q: product.productCode, max: 10, offset: 0], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(product.id))
                .expectBody('totalCount', Matchers.greaterThanOrEqualTo(1))
                .build())
    }

    @PendingFeature(reason = 'includeCategoryChildren=false acts as true')
    void 'list filters by category, with or without the subcategories, as the product list page asks'() {
        given: 'a product in a subcategory of the root test category'
        Category child = categoryApiWrapper.createOK(new CategoryTestBuilder()
                .name("Docs Child ${UUID.randomUUID().toString().take(8)}")
                .parentCategory(rootCategory)
                .build())
        Product inChild = newProduct(child)

        expect:
        regressionProductApi.list([categoryId: rootCategory.id, includeCategoryChildren: true, format: 'list'],
                new ResponseSpecBuilder()
                        .expectStatusCode(HttpStatus.SC_OK)
                        .expectBody('data.id', Matchers.hasItem(inChild.id))
                        .build())
        regressionProductApi.list([categoryId: rootCategory.id, includeCategoryChildren: false, format: 'list'],
                new ResponseSpecBuilder()
                        .expectStatusCode(HttpStatus.SC_OK)
                        .expectBody('data.id', Matchers.not(Matchers.hasItem(inChild.id)))
                        .build())
        regressionProductApi.list([categoryId: rootCategory.id, format: 'list'], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.not(Matchers.hasItem(inChild.id)))
                .build())
    }

    @PendingFeature(reason = 'the creation-date filter with a search text returns products outside the range')
    void 'list filters by creation date in the MM/DD/YYYY form the product list page sends'() {
        given:
        String yesterday = (new Date() - 1).format('MM/dd/yyyy')
        String tomorrow = (new Date() + 1).format('MM/dd/yyyy')

        expect:
        regressionProductApi.list([q: product.productCode, createdAfter: yesterday, createdBefore: tomorrow],
                new ResponseSpecBuilder()
                        .expectStatusCode(HttpStatus.SC_OK)
                        .expectBody('data.id', Matchers.hasItem(product.id))
                        .build())
        regressionProductApi.list([q: product.productCode, createdBefore: yesterday], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.not(Matchers.hasItem(product.id)))
                .build())
    }

    void 'list with format=list answers the compact list items'() {
        expect:
        regressionProductApi.list([q: product.productCode, format: 'list'], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(product.id))
                .build())
    }

    void 'list with format=csv downloads the products as CSV'() {
        when:
        Response response = regressionProductApi.listCsv([q: product.productCode], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectContentType('text/csv')
                .expectHeader('Content-Disposition', Matchers.containsString('Products-'))
                .build())

        then:
        response.asString().readLines().size() >= 2  // a header row and the product
        response.asString().contains(product.productCode)
    }

    /** A new product saved through the API (the builder looks up reference data, so it builds in a transaction). */
    Product newProduct(Category category = rootCategory) {
        Product unsaved = Product.withNewTransaction {
            new ProductTestBuilder()
                    .name("Docs Product ${UUID.randomUUID().toString().take(8)}")
                    .category(category)
                    .build()
        }
        return productApiWrapper.saveOK(unsaved)
    }
}
