package org.pih.warehouse.api.regression.platform.pages.product

import spock.lang.PendingFeature


import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import io.restassured.response.Response

import org.pih.warehouse.common.domain.builder.product.ProductTestBuilder
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.PartyType
import org.pih.warehouse.core.PreferenceType
import org.pih.warehouse.api.regression.platform.pages.ServerPages
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductSupplier

import static org.pih.warehouse.api.regression.platform.pages.ServerPages.page
import static org.pih.warehouse.api.regression.platform.pages.ServerPages.redirectPath

/**
 * The product source server pages (ProductSupplierController): creating, editing, exporting and deleting the
 * sources of a product, with their contract prices and preferences.
 */
class ProductSupplierPagesSpec extends RegressionApiSpec {

    ServerPages pages

    Organization supplier

    Product sourced

    @Override
    void setupData() {
        pages = new ServerPages(serverPort, cookie)
        supplier = Organization.withNewTransaction {
            new Organization(code: randomUtil.randomStringFieldValue('SUP'),
                    name: randomUtil.randomStringFieldValue('Supplier'), active: true,
                    partyType: PartyType.findByCode('ORG')).save(failOnError: true)
        }
        sourced = Product.withNewTransaction {
            new ProductTestBuilder().name(randomUtil.randomStringFieldValue('Sourced product')).category(rootCategory)
                    .build(true)
        }
    }

    String preferenceTypeId() {
        return PreferenceType.withNewSession { PreferenceType.list().first().id }
    }

    def <T> T withSource(String id, Closure<T> closure) {
        return ProductSupplier.withNewSession { closure(ProductSupplier.get(id)) }
    }

    String saveSource(Map extra = [:]) {
        String code = randomUtil.randomStringFieldValue('SRC')
        pages.post('/productSupplier/save', [name: 'Page source', code: code, 'product.id': sourced.id,
                'supplier.id': supplier.id, supplierCode: 'SUP-CODE-1', dialog: 'true'] + extra)
        return ProductSupplier.withNewSession { ProductSupplier.findByCode(code)?.id }
    }

    void 'the source pages'() {
        expect:
        redirectPath(pages.get('/productSupplier/index')) == '/productSupplier/list'
        redirectPath(pages.get('/productSupplier/show/unknown')) == '/productSupplier/list'
        redirectPath(pages.post('/productSupplier/update', [id: 'unknown'])) == '/productSupplier/list'
        page(pages.get('/productSupplier/dialog', ['product.id': sourced.id])) != null
    }

    void 'saving a source with a contract price and preferences'() {
        given:
        String preferenceTypeId = preferenceTypeId()
        String code = randomUtil.randomStringFieldValue('SRC')

        when:
        Response saved = pages.post('/productSupplier/save', [name: 'Page source', code: code,
                'product.id': sourced.id, 'supplier.id': supplier.id, price: '4.25', toDate: '12/31/2099',
                defaultPreferenceType: preferenceTypeId, preferenceType: preferenceTypeId, dialog: 'true'])
        String id = ProductSupplier.withNewSession { ProductSupplier.findByCode(code)?.id }

        then:
        redirectPath(saved) == "/product/edit/${sourced.id}"
        id
        withSource(id) { ProductSupplier source ->
            assert source.contractPrice.price == 4.25
            assert source.contractPrice.toDate.format('MM/dd/yyyy') == '12/31/2099'
            assert source.productSupplierPreferences.size() == 2
            assert source.globalProductSupplierPreference.preferenceType.id == preferenceTypeId
            true
        }

        expect:
        page(pages.get('/productSupplier/dialog', ['product.id': sourced.id, id: id])).contains(code)

        when: 'unparsable and negative prices are refused'
        String unparsable = page(pages.post('/productSupplier/save', [name: 'Bad price', code: 'X',
                'product.id': sourced.id, price: 'cheap']))
        String negative = page(pages.post('/productSupplier/save', [name: 'Bad price', code: 'X',
                'product.id': sourced.id, price: '-2']))

        then:
        unparsable != null
        negative != null
        ProductSupplier.withNewSession { ProductSupplier.findAllByName('Bad price') } == []
    }

    void 'updating a source changes its price and preferences'() {
        given:
        String preferenceTypeId = preferenceTypeId()
        String id = saveSource([price: '3', defaultPreferenceType: preferenceTypeId, preferenceType: preferenceTypeId])

        when:
        Response updated = pages.post('/productSupplier/update', [id: id, name: 'Renamed source', price: '6.5'])

        then:
        redirectPath(updated) == "/productSupplier/list/${id}"
        withSource(id) { ProductSupplier source ->
            assert source.name == 'Renamed source'
            assert source.contractPrice.price == 6.5
            assert source.productSupplierPreferences.size() == 0
            true
        }

        when: 'preferences are set again and the price cleared'
        Response again = pages.post('/productSupplier/update', [id: id, defaultPreferenceType: preferenceTypeId,
                preferenceType: preferenceTypeId, dialog: 'true'])

        then:
        redirectPath(again) == "/product/edit/${sourced.id}"
        withSource(id) { ProductSupplier source ->
            assert source.contractPrice == null
            assert source.productSupplierPreferences.size() == 2
            true
        }

        when: 'bad prices'
        String unparsable = page(pages.post('/productSupplier/update', [id: id, price: 'cheap']))
        String negative = page(pages.post('/productSupplier/update', [id: id, price: '-1']))

        then:
        unparsable != null
        negative != null
    }

    void 'sources export as csv and excel'() {
        given:
        String id = saveSource([price: '2'])
        String code = withSource(id) { it.code }

        when:
        Response csv = pages.get('/productSupplier/export', [productId: sourced.id])
        Response xls = pages.get('/productSupplier/export', [productId: sourced.id, format: 'xls'])

        then:
        csv.statusCode == 200
        csv.contentType.startsWith('text/csv')
        csv.body.asString().contains(code)
        csv.body.asString().contains(sourced.productCode)
        xls.statusCode == 200
        xls.contentType.startsWith('application/vnd.ms-excel')
    }

    @PendingFeature(reason = 'the product supplier show page fails to render')
    void 'the source show page renders'() {
        given:
        String id = saveSource()
        String code = withSource(id) { it.code }

        expect:
        page(pages.get("/productSupplier/show/${id}")).contains(code)
    }
}
