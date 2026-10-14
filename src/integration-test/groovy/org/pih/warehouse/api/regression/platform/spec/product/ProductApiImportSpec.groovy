package org.pih.warehouse.api.regression.platform.spec.product

import spock.lang.PendingFeature



import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import org.pih.warehouse.api.regression.platform.client.product.RegressionProductApi
import org.pih.warehouse.api.spec.product.*
import io.restassured.builder.ResponseSpecBuilder
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.core.Constants
import org.pih.warehouse.product.Product

/**
 * Product import from a CSV in the product export's column layout, sent as the request body (POST /api/products/import).
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation
class ProductApiImportSpec extends RegressionApiSpec {

    @Autowired
    RegressionProductApi regressionProductApi

    void 'a CSV row creates a product'() {
        given:
        String code = randomUtil.randomStringFieldValue('IMP').take(30)
        String name = randomUtil.randomStringFieldValue('Imported product')

        when:
        regressionProductApi.importCsv(csv([row(code: code, name: name, category: rootCategory.name, description: 'From a file')]),
                'text/csv', new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.productCode', Matchers.contains(code))
                .expectBody('data.name', Matchers.contains(name))
                .build())

        then:
        Product.withNewSession {
            Product imported = Product.findByProductCode(code)
            assert imported.description == 'From a file'
            assert imported.category.name == rootCategory.name
            true
        }
    }

    void 'a CSV row with the id of a product updates it'() {
        given:
        String code = randomUtil.randomStringFieldValue('UPD').take(30)
        regressionProductApi.importCsv(csv([row(code: code, name: 'Before')]), 'text/csv', responseSpecUtil.OK_RESPONSE_SPEC)
        String id = Product.withNewSession { Product.findByProductCode(code).id }

        when:
        regressionProductApi.importCsv(csv([row(id: id, code: code, name: 'After')]), 'text/csv', new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.contains(id))
                .build())

        then:
        Product.withNewSession { Product.get(id).name } == 'After'
    }

    @PendingFeature(reason = 'a bad product import (empty, wrong type, short rows, bad flags, unknown type/category) answers 500, not 400')
    void 'a product CSV that is #problem answers 400'() {
        expect:
        regressionProductApi.importCsv(body, contentType, ProductSourceSpec.error(HttpStatus.SC_BAD_REQUEST))

        where:
        problem                          | contentType  | body
        'empty'                          | 'text/csv'   | ''
        'not sent as text/csv'           | 'text/plain' | csv([row(code: 'NOT-CSV', name: 'Not a CSV')])
        'in another column layout'       | 'text/csv'   | 'Code,Name\nX,Y\n'
        'missing a name'                 | 'text/csv'   | csv([row(code: 'NO-NAME', name: '')])
        'naming an unknown product type' | 'text/csv'   | csv([row(code: 'BAD-TYPE', name: 'Bad type', type: 'No such type')])
        'naming an unknown category'     | 'text/csv'   | csv([row(code: 'BAD-CAT', name: 'Bad category', category: 'No such category here')])
        'with a short row'               | 'text/csv'   | csv([['', 'true', 'SHORT', 'Default', 'Short row']])
        'with an invalid active flag'    | 'text/csv'   | csv([row(code: 'BAD-FLAG', name: 'Bad flag', active: 'maybe')])
    }

    /** A row in the export's column layout; unset columns are empty. */
    private static List<String> row(Map values) {
        List<String> row = [''] * Constants.EXPORT_PRODUCT_COLUMNS.size()
        row[0] = values.id ?: ''
        row[1] = values.active ?: 'true'
        row[2] = values.code
        row[3] = values.type ?: 'Default'
        row[4] = values.name
        row[6] = values.category ?: ''
        row[8] = values.description ?: ''
        return row
    }

    private static String csv(List<List<String>> rows) {
        return ([Constants.EXPORT_PRODUCT_COLUMNS] + rows).collect { it.join(',') }.join('\n') + '\n'
    }
}
