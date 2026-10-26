package org.pih.warehouse.api.regression.platform.pages.product

import spock.lang.PendingFeature


import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import io.restassured.response.Response

import org.pih.warehouse.common.domain.builder.product.ProductTestBuilder
import org.pih.warehouse.core.Synonym
import org.pih.warehouse.core.SynonymTypeCode
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.api.regression.platform.pages.ServerPages
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductCatalog
import org.pih.warehouse.product.ProductComponent
import org.pih.warehouse.product.ProductGroup
import org.pih.warehouse.product.ProductType

import static org.pih.warehouse.api.regression.platform.pages.ServerPages.page
import static org.pih.warehouse.api.regression.platform.pages.ServerPages.redirectPath

/**
 * The product server pages (ProductController): creating and editing products, their packages, groups, components,
 * synonyms and catalogs, and the product exports.
 */
class ProductPagesSpec extends RegressionApiSpec {

    ServerPages pages

    @Override
    void setupData() {
        pages = new ServerPages(serverPort, cookie)
    }

    Product newProduct(String prefix = 'Page product') {
        return Product.withNewTransaction {
            new ProductTestBuilder()
                    .name(randomUtil.randomStringFieldValue(prefix))
                    .category(rootCategory)
                    .build(true)
        }
    }

    def <T> T withProduct(String id, Closure<T> closure) {
        return Product.withNewSession { closure(Product.get(id)) }
    }

    void 'creating a product from the edit form'() {
        given:
        String name = randomUtil.randomStringFieldValue('Created product')
        String productTypeId = ProductType.withNewSession { ProductType.defaultProductType.list().first().id }

        expect:
        page(pages.get('/product/create')) != null
        redirectPath(pages.get('/product/index')) == '/product/list'

        when:
        String saved = page(pages.post('/product/save', [name: name, 'category.id': rootCategory.id,
                'productType.id': productTypeId, tagsToBeAdded: 'page-tag-a']))
        Product created = Product.withNewSession { Product.findByName(name) }

        then:
        saved.contains(name)
        created
        created.productCode
        withProduct(created.id) { it.tags*.tag } == ['page-tag-a']
        page(pages.get("/product/renderCreatedEmail/${created.id}")).contains(name)
    }

    void 'editing a product updates its name and tags'() {
        given:
        Product edited = newProduct()
        String newName = randomUtil.randomStringFieldValue('Renamed product')

        expect:
        page(pages.get("/product/edit/${edited.id}")).contains(edited.name)
        redirectPath(pages.get('/product/edit/unknown-product')) == '/inventory/browse'
        redirectPath(pages.get("/product/redirect/${edited.id}")) == "/inventoryItem/showStockCard/${edited.id}"

        when:
        Response updated = pages.post('/product/update', [id: edited.id, name: newName,
                'category.id': rootCategory.id, tagsToBeAdded: 'page-tag-b,page-tag-c'])

        then:
        redirectPath(updated) == "/product/edit/${edited.id}"
        withProduct(edited.id) { it.name } == newName
        withProduct(edited.id) { it.tags*.tag.sort() } == ['page-tag-b', 'page-tag-c']
        redirectPath(pages.post('/product/update', [id: 'unknown-product'])) == '/inventoryItem/browse'
    }

    void 'product groups are removed from a product and deleted'() {
        given:
        Product grouped = newProduct()
        String groupName = randomUtil.randomStringFieldValue('Group')
        String groupId = ProductGroup.withNewTransaction {
            ProductGroup group = new ProductGroup(name: groupName, category: Category.get(rootCategory.id))
            group.addToProducts(Product.get(grouped.id))
            group.save(failOnError: true, flush: true)
            Product.get(grouped.id).addToProductGroups(group).save(failOnError: true, flush: true)
            group.id
        }

        expect:
        page(pages.get("/product/renderTemplate/${grouped.id}", [templateName: 'productGroups'])).contains(groupName)

        when:
        String removed = page(pages.post("/product/removeFromProductGroups/${groupId}", [productId: grouped.id]))

        then:
        !removed.contains(groupName)
        withProduct(grouped.id) { it.productGroups.size() } == 0

        when:
        String deleted = page(pages.post("/product/deleteProductGroup/${groupId}", [productId: grouped.id]))

        then:
        deleted != null
        ProductGroup.withNewSession { ProductGroup.get(groupId) } == null

        expect: 'an unknown product answers 404'
        pages.post("/product/removeFromProductGroups/${groupId}", [productId: 'unknown']).statusCode == 404
        pages.post("/product/deleteProductGroup/${groupId}", [productId: 'unknown']).statusCode == 404
    }

    @PendingFeature(reason = 'deleting a product component is not saved')
    void 'components are added to and removed from an assembly'() {
        given:
        Product assembly = newProduct('Assembly')
        Product component = newProduct('Component')
        String uomId = UnitOfMeasure.withNewSession { UnitOfMeasure.list().first().id }

        when:
        String added = page(pages.post('/product/addProductComponent', ['assemblyProduct.id': assembly.id,
                'componentProduct.id': component.id, quantity: '2', unitOfMeasure: uomId]))
        String componentId = withProduct(assembly.id) { it.productComponents.find()?.id }

        then:
        added.contains(component.productCode)
        componentId
        ProductComponent.withNewSession { ProductComponent.get(componentId).quantity } == 2

        when:
        String deleted = page(pages.post("/product/deleteProductComponent/${componentId}"))

        then:
        !deleted.contains(component.productCode)
        ProductComponent.withNewSession { ProductComponent.get(componentId) } == null
        withProduct(assembly.id) { it.productComponents.size() } == 0
    }

    void 'synonyms are added, edited and deleted'() {
        given:
        Product named = newProduct()

        when:
        String added = page(pages.post("/product/addSynonymToProduct/${named.id}", [
                synonymTypeCode: SynonymTypeCode.ALTERNATE_NAME.name(), synonym: 'Page alias', locale: 'en']))
        String synonymId = withProduct(named.id) { it.synonyms.find()?.id }

        then:
        added.contains('Page alias')
        synonymId
        page(pages.get("/product/editProductSynonymDialog/${synonymId}")).contains('Page alias')

        when:
        Response edited = pages.post('/product/editProductSynonym', ['synonym.id': synonymId,
                synonymTypeCode: SynonymTypeCode.ALTERNATE_NAME.name(), synonym: 'Page alias 2', locale: 'en'])

        then:
        edited.statusCode == 200
        Synonym.withNewSession { Synonym.get(synonymId).name } == 'Page alias 2'

        when:
        String deleted = page(pages.post("/product/deleteSynonym/${synonymId}", [productId: named.id]))

        then:
        !deleted.contains('Page alias 2')
        withProduct(named.id) { it.synonyms.size() } == 0
    }

    void 'products are added to and removed from catalogs'() {
        given:
        Product cataloged = newProduct()
        String catalogId = ProductCatalog.withNewTransaction {
            new ProductCatalog(code: randomUtil.randomStringFieldValue('CAT'),
                    name: randomUtil.randomStringFieldValue('Catalog')).save(failOnError: true, flush: true).id
        }

        when:
        Response added = pages.post('/product/addToProductCatalog', ['product.id': cataloged.id,
                'productCatalog.id': catalogId])
        List included = pages.request().accept('application/json').get("/product/includesProduct/${cataloged.id}")
                .jsonPath().getList('products.id')

        then:
        redirectPath(added) == "/product/productCatalogs/${cataloged.id}"
        included == [catalogId]
        page(pages.get("/product/productCatalogs/${cataloged.id}")) != null

        when:
        Response removed = pages.post("/product/removeFromProductCatalog/${cataloged.id}",
                ['productCatalog.id': catalogId])

        then:
        redirectPath(removed) == "/product/productCatalogs/${cataloged.id}"
        pages.request().accept('application/json').get("/product/includesProduct/${cataloged.id}")
                .jsonPath().getList('products.id') == []
    }

    void 'the synonym template exports as excel'() {
        given:
        Product exported = newProduct()

        when:
        Response synonyms = pages.get('/product/exportSynonymTemplate', [productCode: exported.productCode])
        Response empty = pages.get('/product/exportSynonymTemplate')

        then:
        synonyms.statusCode == 200
        synonyms.contentType.startsWith('application/vnd.ms-excel')
        empty.statusCode == 200
    }

    void 'the import pages refuse a missing file'() {
        given:
        Product target = newProduct()

        expect:
        page(pages.get('/product/importAsCsv')) != null
        page(pages.get('/product/uploadCsv')) != null
        page(pages.post('/product/uploadCsv', [:])) != null
        page(pages.post('/product/importCsv', [:])) != null
        redirectPath(pages.get('/product/importDependencies')) == '/product/importProducts'
        redirectPath(pages.request().multiPart('file', 'synonyms.xls', new byte[0], 'application/vnd.ms-excel')
                .multiPart('product.id', target.id).post('/product/importProductSynonyms')) == "/product/edit/${target.id}"
    }

    @PendingFeature(reason = 'an unknown document answers 500, not a not-found')
    void 'templates, search, barcodes and documents'() {
        given:
        Product shown = newProduct()

        expect:
        page(pages.get("/product/renderTemplate/${shown.id}", [templateName: 'productGroups'])) != null
        page(pages.get('/product/renderTemplate/unknown-product')).contains('unknown-product')
        page(pages.get('/product/search')) != null
        page(pages.get("/product/addDocument/${shown.id}")) != null
        page(pages.get('/product/showMergeProductDialog', [primaryProduct: shown.id,
                template: 'mergeProductConfirmationDialog'])) != null
        page(pages.get('/product/productMergeLogs')) != null
        page(pages.get('/product/batchEdit', [categoryId: rootCategory.id])).contains(shown.productCode)
        pages.get('/product/renderImage/unknown-document').statusCode == 404
        pages.get('/product/viewThumbnail/unknown-document').statusCode == 404
        redirectPath(pages.post('/product/deleteDocument/x', ['product.id': 'unknown'])) == '/product/list'
        redirectPath(pages.post('/product/deleteDocument/unknown', ['product.id': shown.id])) == "/product/edit/${shown.id}"

        when:
        Response barcode = pages.get('/product/barcode', [format: 'CODE_128', data: shown.productCode])

        then:
        barcode.statusCode == 200
        barcode.body.asByteArray().length > 0
    }

    void 'deleting products without stock'() {
        given:
        Product single = newProduct()
        Product batch = newProduct()

        when:
        Response deleted = pages.post("/product/delete/${single.id}")
        Response refused = pages.post("/product/delete/unknown-product")
        Response deletedBatch = pages.post('/product/deleteProducts', ['product.id': batch.id])

        then:
        redirectPath(deleted) == '/product/list'
        withProduct(single.id) { it } == null
        redirectPath(refused) == '/product/edit/unknown-product'
        redirectPath(deletedBatch) == '/inventory/browse'
        withProduct(batch.id) { it } == null
    }

    void 'the product forms refuse invalid and stale changes'() {
        given:
        Product edited = newProduct()
        String productTypeId = ProductType.withNewSession { ProductType.defaultProductType.list().first().id }
        String name = randomUtil.randomStringFieldValue('Uncategorized product')

        when: 'a product without a category'
        String refused = page(pages.post('/product/save', [name: name, 'productType.id': productTypeId]))

        then:
        refused != null
        Product.withNewSession { Product.findByName(name) } == null

        when: 'an update from a stale form'
        String stale = page(pages.post('/product/update', [id: edited.id, version: '-1', name: 'Stale name']))

        then:
        stale.contains(edited.name)
        withProduct(edited.id) { it.name } == edited.name
    }

    @PendingFeature(reason = 'the legacy product search page answers 500')
    void 'the product search finds products by code'() {
        given:
        Product found = newProduct('Searched product')

        expect:
        page(pages.get('/product/search', [q: found.productCode])).contains(found.name)
    }

    void 'the chosen products export as csv'() {
        given:
        Product first = newProduct()
        Product second = newProduct()

        when:
        Response exported = pages.request().queryParam('product.id', first.id, second.id).get('/product/exportProducts')

        then:
        exported.statusCode == 200
        exported.contentType.startsWith('text/csv')
        exported.body.asString().contains(first.productCode)
        exported.body.asString().contains(second.productCode)
    }

    @PendingFeature(reason = 'the batch edit product properties page answers 500')
    void 'the batch edit properties page renders'() {
        expect:
        page(pages.get('/product/batchEditProperties')) != null
        page(pages.get('/product/batchEditProperties', [categoryId: rootCategory.id])) != null
    }
}
