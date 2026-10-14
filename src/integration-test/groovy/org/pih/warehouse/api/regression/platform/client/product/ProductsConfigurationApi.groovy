package org.pih.warehouse.api.regression.platform.client.product

import org.pih.warehouse.api.client.product.*
import groovy.transform.InheritConstructors
import io.restassured.builder.MultiPartSpecBuilder
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.AuthenticatedApi

/**
 * The product configuration wizard API (/api/productsConfiguration/...): the category tree and product list options,
 * their imports, and the category CSV.
 */
@TestComponent
@InheritConstructors
class ProductsConfigurationApi extends AuthenticatedApi {

    Response categoriesCount(ResponseSpecification responseSpec) {
        return request(null, responseSpec, Method.GET, '/productsConfiguration/categoriesCount')
    }

    Response categoryOptions(ResponseSpecification responseSpec) {
        return request(null, responseSpec, Method.GET, '/productsConfiguration/categoryOptions')
    }

    Response productOptions(ResponseSpecification responseSpec) {
        return request(null, responseSpec, Method.GET, '/productsConfiguration/productOptions')
    }

    Response downloadCategories(ResponseSpecification responseSpec) {
        return request(csvSpec(), responseSpec, Method.GET, '/productsConfiguration/downloadCategories')
    }

    Response downloadCategoryTemplate(ResponseSpecification responseSpec) {
        return request(csvSpec(), responseSpec, Method.GET, '/productsConfiguration/downloadCategoryTemplate')
    }

    /** Imports the category tree of the given option (the categoryOption query parameter, as the wizard sends it). */
    Response importCategories(String categoryOption, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
        if (categoryOption != null) {
            builder.addQueryParam('categoryOption', categoryOption)
        }
        return request(builder.build(), responseSpec, Method.POST, '/productsConfiguration/importCategories')
    }

    /** Imports the product list of the given option (the productOption query parameter, as the wizard sends it). */
    Response importProducts(String productOption, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
        if (productOption != null) {
            builder.addQueryParam('productOption', productOption)
        }
        return request(builder.build(), responseSpec, Method.POST, '/productsConfiguration/importProducts')
    }

    /** Uploads a category CSV as the multipart field "importFile". */
    Response importCategoryCsv(String fileName, String mimeType, byte[] content, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .setContentType('multipart/form-data')
                .addMultiPart(new MultiPartSpecBuilder(content)
                        .controlName('importFile')
                        .fileName(fileName)
                        .mimeType(mimeType)
                        .build())
                .build()
        return request(requestSpec, responseSpec, Method.POST, '/productsConfiguration/importCategoryCsv')
    }

    private static RequestSpecification csvSpec() {
        return new RequestSpecBuilder().setAccept('text/csv').build()
    }
}
