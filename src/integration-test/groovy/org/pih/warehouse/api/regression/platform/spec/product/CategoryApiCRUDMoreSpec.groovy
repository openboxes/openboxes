package org.pih.warehouse.api.regression.platform.spec.product

import spock.lang.PendingFeature



import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import org.pih.warehouse.api.regression.platform.client.product.RegressionCategoryApi
import org.pih.warehouse.api.spec.product.*
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.specification.ResponseSpecification
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import org.grails.web.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Shared

import org.pih.warehouse.api.client.product.CategoryApiWrapper
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.common.domain.builder.product.CategoryTestBuilder
import org.pih.warehouse.common.domain.builder.product.ProductTestBuilder
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product

/**
 * Test the basic CRUD endpoints for product categories.
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation
class CategoryApiCRUDMoreSpec extends RegressionApiSpec {

    @Autowired
    RegressionCategoryApi regressionCategoryApi

    @Autowired
    CategoryApiWrapper categoryApiWrapper

    @Shared
    Category category

    void setupData() {
        category = new CategoryTestBuilder()
                .build(true)
    }

    void cleanupData() {
        // Some tests delete the category through the API: the row may be gone already.
        Category.get(category.id)?.delete()
    }

    @PendingFeature(reason = 'deleting a referenced category answers 500 on MariaDB, not 409')
    void 'delete category answers 409 while products still use it'() {
        given: 'a category with a product in it (left in place: tests do not rely on a clean database)'
        Category inUse = categoryInUse()

        expect:
        regressionCategoryApi.delete(inUse.id, responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_CONFLICT))
    }

    @PendingFeature(reason = 'updating a category through the API changes nothing')
    void 'update category with #method changes its fields'() {
        given:
        String body = new JSONObject().put('description', "Updated with ${method}".toString()).toString()
        ResponseSpecification ok = new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('id', Matchers.equalTo(category.id))
                .expectBody('description', Matchers.equalTo("Updated with ${method}".toString()))
                .build()

        expect:
        method == 'PUT' ? regressionCategoryApi.update(category.id, body, ok)
                : regressionCategoryApi.updateWithPost(category.id, body, ok)

        where:
        method << ['PUT', 'POST']
    }

    @PendingFeature(reason = 'updating a category through the API changes nothing (200)')
    void 'update category answers 400 when the name is blank'() {
        expect:
        regressionCategoryApi.update(category.id, new JSONObject().put('name', '').toString(),
                responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
    }

    /** A category with a product in it, both saved through the API (the builders only read reference data). */
    Category categoryInUse() {
        Category inUse = categoryApiWrapper.createOK(new CategoryTestBuilder().build())
        Product unsaved = Product.withNewTransaction {
            new ProductTestBuilder().randomizedName().category(inUse).build()
        }
        productApiWrapper.saveOK(unsaved)
        return inUse
    }
}
