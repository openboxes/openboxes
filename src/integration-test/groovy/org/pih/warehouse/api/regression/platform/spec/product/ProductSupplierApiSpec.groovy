package org.pih.warehouse.api.regression.platform.spec.product




import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import spock.lang.PendingFeature
import org.pih.warehouse.api.spec.product.*
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import org.apache.http.HttpStatus
import org.grails.web.json.JSONObject
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.hamcrest.Matchers
import spock.lang.Unroll

import org.pih.warehouse.common.domain.builder.product.ProductTestBuilder
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductSupplier

// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation(tolerate = [
        'known response POST /productSuppliers',
])
class ProductSupplierApiSpec extends ProductSourceSpec {

    @PendingFeature(reason = 'ProductSupplierService.groovy:475 productSupplierGormService is not injected (null although the bean exists), so POST /api/productSuppliers answers 500')
    void 'a product source is created with a generated code and read back'() {
        given:
        JSONObject body = sourceBody('Gloves from the supplier')

        when:
        Response created = productSupplierApi.create(body.toString(), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_CREATED)
                .expectBody('data.name', Matchers.equalTo('Gloves from the supplier'))
                .expectBody('data.product.id', Matchers.equalTo(item.id))
                .expectBody('data.supplier.id', Matchers.equalTo(supplier.id))
                .expectBody('data.manufacturer.id', Matchers.equalTo(manufacturer.id))
                .expectBody('data.ratingTypeCode', Matchers.equalTo('GOOD'))
                .expectBody('data.active', Matchers.equalTo(true))
                .build())
        String id = created.jsonPath().getString('data.id')
        String code = created.jsonPath().getString('data.code')

        then: 'the code is generated from the product and the supplier'
        code
        code.contains(item.productCode)

        and:
        productSupplierApi.read(id, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.equalTo(id))
                .expectBody('data.code', Matchers.equalTo(code))
                .expectBody('data.supplierCode', Matchers.equalTo(body.getString('supplierCode')))
                .expectBody('data.manufacturerCode', Matchers.equalTo('MFG-1'))
                .expectBody('data.productSupplierPreferences', Matchers.empty())
                .build())
    }

    @PendingFeature(reason = 'ProductSupplierService.groovy:475 productSupplierGormService is not injected (null although the bean exists), so POST /api/productSuppliers answers 500')
    void 'a product source keeps the code it is given'() {
        given:
        String code = randomUtil.randomStringFieldValue('PS')

        expect:
        productSupplierApi.create(sourceBody().put('code', code).toString(), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_CREATED)
                .expectBody('data.code', Matchers.equalTo(code))
                .build())
    }

    void 'creating a product source without #missing answers 400'() {
        given:
        JSONObject body = sourceBody()
        body.remove(missing)

        expect:
        productSupplierApi.create(body.toString(), error(HttpStatus.SC_BAD_REQUEST))

        where:
        missing << ['supplier', 'name', 'product']
    }

    void 'reading an unknown product source answers 404'() {
        expect:
        productSupplierApi.read(INVALID_ID, error(HttpStatus.SC_NOT_FOUND))
    }

    @PendingFeature(reason = 'ProductSupplierService.groovy:475 productSupplierGormService is not injected (null although the bean exists), so POST /api/productSuppliers answers 500')
    void 'updating a product source with #method changes its details'() {
        given:
        String id = newSource()
        JSONObject change = sourceBody('Renamed source').put('ratingTypeCode', 'POOR').put('active', false)

        when:
        productSupplierApi.update(id, change.toString(), method, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.equalTo(id))
                .expectBody('data.name', Matchers.equalTo('Renamed source'))
                .expectBody('data.ratingTypeCode', Matchers.equalTo('POOR'))
                .expectBody('data.active', Matchers.equalTo(false))
                .build())

        then:
        ProductSupplier.withNewSession { ProductSupplier.get(id).name } == 'Renamed source'

        where:
        method << [Method.PUT, Method.POST]
    }

    @PendingFeature(reason = 'ProductSupplierService.groovy:475 productSupplierGormService is not injected (null although the bean exists), so POST /api/productSuppliers answers 500')
    void 'updating an unknown product source answers 404 and an invalid update 400'() {
        given:
        String id = newSource()

        expect:
        productSupplierApi.update(INVALID_ID, sourceBody().toString(), Method.PUT, error(HttpStatus.SC_NOT_FOUND))
        productSupplierApi.update(id, sourceBody().put('name', '').toString(), Method.PUT, error(HttpStatus.SC_BAD_REQUEST))
    }

    @PendingFeature(reason = 'ProductSupplierService.groovy:475 productSupplierGormService is not injected (null although the bean exists), so POST /api/productSuppliers answers 500')
    void 'a product source is deleted with its packages and preferences'() {
        given:
        String id = newSource()
        productSupplierPreferenceApi.create(preferenceBody(id).toString(), responseSpecUtil.CREATED_RESPONSE_SPEC)
        productSupplierPreferenceApi.savePackage(new JSONObject()
                .put('productSupplier', id)
                .put('uom', box.id)
                .put('productPackageQuantity', 10)
                .put('productPackagePrice', 25)
                .toString(), responseSpecUtil.OK_RESPONSE_SPEC)

        when:
        productSupplierApi.delete(id, responseSpecUtil.NO_CONTENT_RESPONSE_SPEC)

        then:
        productSupplierApi.read(id, error(HttpStatus.SC_NOT_FOUND))
    }

    @PendingFeature(reason = 'deleting an unknown product source answers 500')
    void 'deleting an unknown product source answers 404'() {
        expect:
        productSupplierApi.delete(INVALID_ID, error(HttpStatus.SC_NOT_FOUND))
    }

    @PendingFeature(reason = 'ProductSupplierService.groovy:475 productSupplierGormService is not injected (null although the bean exists), so POST /api/productSuppliers answers 500')
    void 'the list filters by product, supplier and search term, and leaves out inactive sources unless asked'() {
        given:
        String active = newSource(sourceBody('Active source'))
        String inactive = newSource(sourceBody('Inactive source').put('active', false))
        String otherSupplier = newSource(sourceBody('Other supplier source').put('supplier', organization('Supplier').id))
        String supplierCode = randomUtil.randomStringFieldValue('FIND')
        String searched = newSource(sourceBody('Searched source').put('supplierCode', supplierCode))

        expect:
        productSupplierApi.list([product: item.id], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.containsInAnyOrder(active, otherSupplier, searched))
                .expectBody('totalCount', Matchers.equalTo(3))
                .build())
        productSupplierApi.list([product: item.id, includeInactive: true], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(inactive))
                .expectBody('totalCount', Matchers.equalTo(4))
                .build())
        productSupplierApi.list([product: item.id, supplier: supplier.id], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.containsInAnyOrder(active, searched))
                .build())
        productSupplierApi.list([searchTerm: supplierCode, product: item.id], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.contains(searched))
                .build())
    }

    @PendingFeature(reason = 'ProductSupplierService.groovy:475 productSupplierGormService is not injected (null although the bean exists), so POST /api/productSuppliers answers 500')
    void 'the list sorts by #sort #order'() {
        given:
        ['B source', 'A source', 'C source'].each { String name -> newSource(sourceBody(name)) }

        expect:
        productSupplierApi.list([product: item.id, sort: sort, order: order], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.name', Matchers.contains(*expected))
                .build())

        where:
        sort   | order  || expected
        'name' | 'asc'  || ['A source', 'B source', 'C source']
        'name' | 'desc' || ['C source', 'B source', 'A source']
    }

    @PendingFeature(reason = 'ProductSupplierService.groovy:475 productSupplierGormService is not injected (null although the bean exists), so POST /api/productSuppliers answers 500')
    @Unroll
    void 'with a search term the list sorts by #sort #order'() {
        given: 'two sources found by one search term, differing in every sort column'
        String term = randomUtil.randomStringFieldValue('SORT').take(30)
        Map<String, String> sources = ['A', 'B'].collectEntries { String letter ->
            Product sourced = Product.withNewTransaction {
                new ProductTestBuilder().name("${letter} sorted product ${term}").category(rootCategory).build(true)
            }
            String id = newSource(sourceBody("${letter} sorted source")
                    .put('product', sourced.id)
                    .put('supplier', organization("${letter} supplier").id)
                    .put('code', "${letter}-${term}")
                    .put('supplierCode', "${term}-${letter}")
                    .put('active', letter == 'A'))
            [(letter): id]
        }
        ProductSupplier.withNewTransaction {
            // An update statement: saving the record would keep the timestamps the database gave it
            ProductSupplier.executeUpdate('update ProductSupplier set dateCreated = :earlier where id = :id',
                    [earlier: ProductSupplier.get(sources.A).dateCreated - 1, id: sources.B])
        }
        // Product codes are generated: the expected order by code is the order of the generated codes
        if (sort == 'product.productCode') {
            Map<String, String> codes = ProductSupplier.withNewSession {
                sources.collectEntries { String letter, String id -> [(letter): ProductSupplier.get(id).product.productCode] }
            }
            expected = codes.keySet().sort { codes[it] }
            if (order == 'desc') {
                expected = expected.reverse()
            }
        }

        expect:
        productSupplierApi.list([searchTerm: term, includeInactive: true, sort: sort, order: order], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.contains(*expected.collect { sources[it] }))
                .build())

        where:
        sort                   | order  || expected
        'product.productCode'  | 'asc'  || ['A', 'B']
        'product.productCode'  | 'desc' || ['B', 'A']
        'product.name'         | 'desc' || ['B', 'A']
        'supplier.displayName' | 'desc' || ['B', 'A']
        'code'                 | 'desc' || ['B', 'A']
        'name'                 | 'desc' || ['B', 'A']
        'dateCreated'          | 'asc'  || ['B', 'A']
        'active'               | 'desc' || ['A', 'B']
    }

    @PendingFeature(reason = 'ProductSupplierService.groovy:475 productSupplierGormService is not injected (null although the bean exists), so POST /api/productSuppliers answers 500')
    void 'the export lists every matching source, not one page of them'() {
        given: 'more sources than the list pages by default'
        String term = randomUtil.randomStringFieldValue('MANY').take(30)
        List<String> ids = (1..11).collect { int n -> newSource(sourceBody("Many ${n}").put('code', "${term}-${n}".toString())) }

        expect:
        productSupplierApi.list([searchTerm: term, tab: 'DETAILS'], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.size()', Matchers.equalTo(10))
                .expectBody('totalCount', Matchers.equalTo(11))
                .build())
        productSupplierApi.export([searchTerm: term], 'application/json', new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('totalCount', Matchers.equalTo(11))
                .expectBody('data.ID', Matchers.containsInAnyOrder(*ids))
                .build())

        and: 'as the export button sends it'
        productSupplierApi.export([searchTerm: term, tab: 'DETAILS', includeInactive: true, disableMaxLimit: true],
                'application/json', new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('totalCount', Matchers.equalTo(11))
                .build())
    }

    @PendingFeature(reason = 'an export without matches gives one blank row')
    void 'exporting no product sources gives no rows; the files keep their header'() {
        given:
        String nothing = randomUtil.randomStringFieldValue('NOTHING')

        expect:
        productSupplierApi.export([searchTerm: nothing], 'application/json', new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data', Matchers.empty())
                .expectBody('totalCount', Matchers.equalTo(0))
                .build())
        productSupplierApi.export([searchTerm: nothing, format: 'csv'], 'text/csv', responseSpecUtil.OK_RESPONSE_SPEC)
                .asString().replace('\uFEFF', '').readLines() == [ProductSupplier.PROPERTIES.keySet().join(',')]
        excelRows(productSupplierApi.export([searchTerm: nothing, format: 'xls'], 'application/vnd.ms-excel',
                responseSpecUtil.OK_RESPONSE_SPEC).asByteArray()) == [ProductSupplier.PROPERTIES.keySet().toList()]
    }

    /** The rows of the first sheet, as cell texts. */
    private static List<List<String>> excelRows(byte[] file) {
        Sheet sheet = new HSSFWorkbook(new ByteArrayInputStream(file)).getSheetAt(0)
        return sheet.collect { Row row -> row.collect { it.stringCellValue } }
    }

    @PendingFeature(reason = 'ProductSupplierService.groovy:475 productSupplierGormService is not injected (null although the bean exists), so POST /api/productSuppliers answers 500')
    void 'the list filters by the default preference type, or by having none'() {
        given:
        String withDefault = newSource(sourceBody('Preferred source'))
        productSupplierPreferenceApi.create(preferenceBody(withDefault).toString(), responseSpecUtil.CREATED_RESPONSE_SPEC)
        String withoutDefault = newSource(sourceBody('Plain source'))

        expect:
        productSupplierApi.list([product: item.id, defaultPreferenceTypes: preferred.id], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.contains(withDefault))
                .build())
        productSupplierApi.list([product: item.id, defaultPreferenceTypes: 'NONE'], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.contains(withoutDefault))
                .build())
    }

    @PendingFeature(reason = 'ProductSupplierService.groovy:475 productSupplierGormService is not injected (null although the bean exists), so POST /api/productSuppliers answers 500')
    void 'the export lists the sources with their default package and global preference'() {
        given:
        String id = newSource(sourceBody('Exported source'))
        productSupplierPreferenceApi.create(preferenceBody(id).toString(), responseSpecUtil.CREATED_RESPONSE_SPEC)
        productSupplierPreferenceApi.savePackage(new JSONObject()
                .put('productSupplier', id)
                .put('uom', box.id)
                .put('productPackageQuantity', 12)
                .put('productPackagePrice', 30)
                .toString(), responseSpecUtil.OK_RESPONSE_SPEC)

        when:
        Response exported = productSupplierApi.export([product: item.id], 'application/json', new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('totalCount', Matchers.equalTo(1))
                .build())
        Map row = exported.jsonPath().getMap('data[0]')

        then:
        row['ID'] == id
        row['Product Source Name'] == 'Exported source'
        row['Product Code'] == item.productCode
        row['Supplier Name'] == supplier.name
        row['Default Package Type'] == box.code
        row['Quantity per package'] == 12
        row['Default Global Preference Type'] == preferred.name
        row['Preference Type Comment'] == 'first choice'
    }

    @PendingFeature(reason = 'ProductSupplierService.groovy:475 productSupplierGormService is not injected (null although the bean exists), so POST /api/productSuppliers answers 500')
    void 'the export comes as a #format file'() {
        given:
        newSource(sourceBody('File source'))

        when:
        Response exported = productSupplierApi.export([product: item.id, format: format], accept,
                responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        exported.contentType.startsWith(accept)
        exported.header('Content-disposition').contains(".${format}")
        format != 'csv' || exported.asString().contains('File source')

        where:
        format | accept
        'csv'  | 'text/csv'
        'xls'  | 'application/vnd.ms-excel'
    }
}
