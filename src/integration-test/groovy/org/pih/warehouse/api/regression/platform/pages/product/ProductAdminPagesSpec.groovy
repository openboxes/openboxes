package org.pih.warehouse.api.regression.platform.pages.product

import spock.lang.PendingFeature


import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import io.restassured.response.Response

import org.pih.warehouse.common.domain.builder.product.ProductTestBuilder
import org.pih.warehouse.core.EntityTypeCode
import org.pih.warehouse.api.regression.platform.pages.ServerPages
import org.pih.warehouse.product.Attribute
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAssociation
import org.pih.warehouse.product.ProductCatalog
import org.pih.warehouse.product.ProductCatalogItem
import org.pih.warehouse.product.ProductGroup
import org.pih.warehouse.product.ProductType

import static org.pih.warehouse.api.regression.platform.pages.ServerPages.page
import static org.pih.warehouse.api.regression.platform.pages.ServerPages.redirectPath

/**
 * The product reference data server pages: attributes, categories, product types, product groups and product
 * catalogs (AttributeController, CategoryController, ProductTypeController, ProductGroupController,
 * ProductCatalogController).
 */
class ProductAdminPagesSpec extends RegressionApiSpec {

    ServerPages pages

    @Override
    void setupData() {
        pages = new ServerPages(serverPort, cookie)
    }

    Product newProduct() {
        return Product.withNewTransaction {
            new ProductTestBuilder().name(randomUtil.randomStringFieldValue('Admin page product')).category(rootCategory)
                    .build(true)
        }
    }

    void 'attributes are created with options, edited and deleted'() {
        given:
        String name = randomUtil.randomStringFieldValue('Attribute')

        expect:
        page(pages.get('/attribute/list', [q: 'no-such-attribute'])) != null
        page(pages.get('/attribute/create')) != null
        redirectPath(pages.get('/attribute/index')) == '/attribute/list'
        redirectPath(pages.get('/attribute/show/unknown')) == '/attribute/list'
        redirectPath(pages.get('/attribute/edit/unknown')) == '/attribute/list'
        redirectPath(pages.post('/attribute/delete', [id: 'unknown'])) == '/attribute/list'

        when:
        Response saved = pages.post('/attribute/save', [name: name, code: name, active: 'false', required: 'false', allowOther: 'false', allowMultiple: 'false', exportable: 'false',
                entityTypeCode: EntityTypeCode.PRODUCT.name(), option: ['Red', 'Blue', '']])
        String id = Attribute.withNewSession { Attribute.findByName(name)?.id }

        then:
        id
        redirectPath(saved) == "/attribute/edit/${id}"
        Attribute.withNewSession {
            Attribute attribute = Attribute.get(id)
            assert attribute.options == ['Red', 'Blue']
            assert attribute.entityTypeCodes as List == [EntityTypeCode.PRODUCT]
            true
        }
        page(pages.get("/attribute/show/${id}")).contains(name)
        page(pages.get("/attribute/edit/${id}")).contains(name)
        page(pages.get('/attribute/list', [q: name])).contains(name)

        when: 'saved again with other options'
        Response edited = pages.post('/attribute/save', [id: id, option: ['Green', 'Yellow']])

        then:
        redirectPath(edited) == "/attribute/edit/${id}"
        Attribute.withNewSession { Attribute.get(id).options.collect { it } } == ['Green', 'Yellow']

        when:
        Response deleted = pages.post('/attribute/delete', [id: id])

        then:
        redirectPath(deleted) == '/attribute/list'
        Attribute.withNewSession { Attribute.get(id) } == null
    }

    void 'categories are created, moved, updated and deleted from the tree'() {
        given:
        String name = randomUtil.randomStringFieldValue('Page category')

        expect:
        page(pages.get('/category/tree')) != null
        page(pages.get('/category/create')) != null
        redirectPath(pages.get('/category/index')) == '/category/list'
        redirectPath(pages.get('/category/show/unknown')) == '/category/tree'
        redirectPath(pages.get('/category/edit/unknown')) == '/category/tree'
        redirectPath(pages.post('/category/update', [id: 'unknown'])) == '/category/tree'
        redirectPath(pages.post('/category/delete', [id: 'unknown'])) == '/category/tree'

        when:
        Response saved = pages.post('/category/save', [name: name, 'parentCategory.id': rootCategory.id])
        String id = Category.withNewSession { Category.findByName(name)?.id }

        then:
        id
        redirectPath(saved) == "/category/tree/${id}"
        page(pages.get("/category/tree/${id}")).contains(name)
        page(pages.get("/category/edit/${id}")).contains(name)

        when:
        Response saveCategory = pages.post('/category/saveCategory', [id: id, description: 'Edited in the tree'])
        Response updated = pages.post('/category/update', [id: id, name: "${name} 2"])

        then:
        redirectPath(saveCategory) == '/category/tree'
        redirectPath(updated) == "/category/tree/${id}"
        Category.withNewSession { Category.get(id).name } == "${name} 2"
        Category.withNewSession { Category.get(id).description } == 'Edited in the tree'

        when: 'a second category is moved under the first'
        String childName = randomUtil.randomStringFieldValue('Page child category')
        pages.post('/category/saveCategory', [name: childName, 'parentCategory.id': rootCategory.id])
        String childId = Category.withNewSession { Category.findByName(childName).id }
        Response moved = pages.post('/category/move', [child: childId, newParent: id])

        then:
        redirectPath(moved) == '/category/tree'
        Category.withNewSession { Category.get(childId).parentCategory.id } == id

        when:
        Response deletedChild = pages.post('/category/deleteCategory', [id: childId])
        Response deleted = pages.post('/category/delete', [id: id])

        then:
        redirectPath(deletedChild) == '/category/tree'
        redirectPath(deleted) == '/category/tree'
        Category.withNewSession { Category.get(id) } == null
    }

    void 'product types are created, updated and deleted'() {
        given:
        String name = randomUtil.randomStringFieldValue('Page product type')

        expect:
        page(pages.get('/productType/list')) != null
        page(pages.get('/productType/create')) != null
        redirectPath(pages.get('/productType/index')) == '/productType/list'
        redirectPath(pages.get('/productType/show/unknown')) == '/productType/list'
        redirectPath(pages.get('/productType/edit/unknown')) == '/productType/list'
        redirectPath(pages.post('/productType/update', [id: 'unknown'])) == '/productType/list'
        redirectPath(pages.post('/productType/delete', [id: 'unknown'])) == '/productType/list'

        when: 'neither a code nor an identifier format'
        String refused = page(pages.post('/productType/save', [name: name]))

        then:
        refused != null
        ProductType.withNewSession { ProductType.findByName(name) } == null

        when:
        String code = randomUtil.randomStringFieldValue('PT').take(10)
        Response saved = pages.post('/productType/save', [name: name, code: code,
                supportedActivities: ['SEARCHABLE'], displayedFields: ['NAME']])
        String id = ProductType.withNewSession { ProductType.findByName(name)?.id }

        then:
        id
        redirectPath(saved) == "/productType/list/${id}"
        page(pages.get("/productType/show/${id}")).contains(name)
        page(pages.get("/productType/edit/${id}")).contains(name)

        when:
        Response updated = pages.post('/productType/update', [id: id, name: "${name} 2", displayedFields: ['NAME']])

        then:
        redirectPath(updated) == "/productType/list/${id}"
        ProductType.withNewSession { ProductType.get(id).name } == "${name} 2"

        when:
        Response deleted = pages.post('/productType/delete', [id: id])

        then:
        redirectPath(deleted) == '/productType/list'
        ProductType.withNewSession { ProductType.get(id) } == null
    }

    void 'product groups are created and their products changed'() {
        given:
        Product first = newProduct()
        Product second = newProduct()
        String name = randomUtil.randomStringFieldValue('Page group')

        expect:
        page(pages.get('/productGroup/list')) != null
        page(pages.get('/productGroup/list', [q: 'no-such-group'])) != null
        page(pages.get('/productGroup/create')) != null
        redirectPath(pages.get('/productGroup/index')) == '/productGroup/list'
        redirectPath(pages.get('/productGroup/show/unknown')) == '/productGroup/list'
        redirectPath(pages.get('/productGroup/edit/unknown')) == '/productGroup/list'
        redirectPath(pages.post('/productGroup/update', [id: 'unknown'])) == '/productGroup/list'
        redirectPath(pages.post('/productGroup/addProducts', [id: 'unknown'])) == '/productGroup/list'
        redirectPath(pages.post('/productGroup/delete', [id: 'unknown'])) == '/productGroup/list'
        page(pages.get('/productGroup/addToProductGroup', ['product.id': [first.id, second.id]])).contains(first.name)

        when:
        Response saved = pages.post('/productGroup/save', [name: name, 'category.id': rootCategory.id,
                'product.id': [first.id]])
        String id = ProductGroup.withNewSession { ProductGroup.findByName(name)?.id }

        then:
        id
        redirectPath(saved) == "/productGroup/edit/${id}"
        page(pages.get("/productGroup/show/${id}")).contains(name)
        page(pages.get("/productGroup/edit/${id}")).contains(first.name)
        page(pages.get('/productGroup/list', [q: name])).contains(name)

        when:
        Response added = pages.post('/productGroup/addProducts', [id: id, 'product.id': [second.id]])

        then:
        redirectPath(added) == "/productGroup/edit/${id}"
        ProductGroup.withNewSession { ProductGroup.get(id).products*.id.sort() } == [first.id, second.id].sort()

        when:
        String removedPage = page(pages.post('/productGroup/removeProductsFromProductGroup', [id: id,
                'delete-product.id': [second.id]]))
        String addedPage = page(pages.post('/productGroup/addProductsToProductGroup', [id: id,
                'add-product.id': [second.id]]))
        String deletedFromGroup = page(pages.post('/productGroup/deleteProductFromProductGroup', [id: id,
                'product.id': second.id]))

        then:
        removedPage != null
        addedPage != null
        deletedFromGroup != null
        !ProductGroup.withNewSession { ProductGroup.get(id).products*.id.contains(second.id) }

        when:
        String addedTemplate = page(pages.post('/productGroup/addProductToProductGroup', [id: id,
                'product.id': second.id]))

        then:
        addedTemplate.contains(second.name)

        when:
        Response updated = pages.post('/productGroup/update', [id: id, name: "${name} 2"])

        then:
        redirectPath(updated) == '/productGroup/list'
        ProductGroup.withNewSession { ProductGroup.get(id).name } == "${name} 2"

        when:
        Response deleted = pages.post('/productGroup/delete', [id: id])

        then:
        redirectPath(deleted) == '/productGroup/list'
        ProductGroup.withNewSession { ProductGroup.get(id) } == null
    }

    @PendingFeature(reason = 'exporting an unknown catalog answers 500 (catalog pages)')
    void 'product catalogs are created, filled, exported and deleted'() {
        given:
        Product cataloged = newProduct()
        String code = randomUtil.randomStringFieldValue('PC')

        expect:
        page(pages.get('/productCatalog/list')) != null
        page(pages.get('/productCatalog/create')) != null
        redirectPath(pages.get('/productCatalog/index')) == '/productCatalog/list'
        redirectPath(pages.get('/productCatalog/show/unknown')) == '/productCatalog/list'
        redirectPath(pages.get('/productCatalog/edit/unknown')) == '/productCatalog/list'
        redirectPath(pages.post('/productCatalog/update', [id: 'unknown'])) == '/productCatalog/list'
        redirectPath(pages.post('/productCatalog/delete', [id: 'unknown'])) == '/productCatalog/list'
        pages.get('/productCatalog/exportProductCatalog/unknown').statusCode == 404

        when:
        Response saved = pages.post('/productCatalog/save', [code: code, name: "Catalog ${code}"])
        String id = ProductCatalog.withNewSession { ProductCatalog.findByCode(code)?.id }

        then:
        id
        redirectPath(saved) == "/productCatalog/list/${id}"
        page(pages.get("/productCatalog/edit/${id}")).contains(code)
        page(pages.post('/productCatalog/save', [name: 'No code'])) != null

        when:
        Response added = pages.post('/productCatalog/addProductCatalogItem', ['productCatalog.id': id,
                'product.id': cataloged.id])
        String itemId = ProductCatalog.withNewSession { ProductCatalog.get(id).productCatalogItems.find()?.id }
        Response exported = pages.get("/productCatalog/exportProductCatalog/${id}")

        then:
        redirectPath(added) == "/productCatalog/productCatalogItems/${id}"
        itemId
        page(pages.get("/productCatalog/productCatalogItems/${id}")).contains(cataloged.productCode)
        exported.statusCode == 200
        exported.body.asString().contains(cataloged.productCode)

        when:
        Response removed = pages.post("/productCatalog/removeProductCatalogItem/${itemId}")

        then:
        redirectPath(removed) == "/productCatalog/productCatalogItems/${id}"
        ProductCatalog.withNewSession { ProductCatalog.get(id).productCatalogItems.size() } == 0

        when:
        Response updated = pages.post('/productCatalog/update', [id: id, name: 'Renamed catalog'])
        String refusedImport = page(pages.post("/productCatalog/importProductCatalog/${id}"))

        then:
        redirectPath(updated) == "/productCatalog/edit/${id}"
        ProductCatalog.withNewSession { ProductCatalog.get(id).name } == 'Renamed catalog'
        refusedImport != null

        when:
        Response deleted = pages.post('/productCatalog/delete', [id: id])

        then:
        redirectPath(deleted) == '/productCatalog/list'
        ProductCatalog.withNewSession { ProductCatalog.get(id) } == null
    }

    void 'product associations are saved with their mutual association, updated, exported and deleted'() {
        given:
        Product first = newProduct()
        Product second = newProduct()

        expect:
        page(pages.get('/productAssociation/create')) != null
        redirectPath(pages.get('/productAssociation/index')) == '/productAssociation/list'
        redirectPath(pages.get('/productAssociation/show/unknown')) == '/productAssociation/list'
        redirectPath(pages.get('/productAssociation/edit/unknown')) == '/productAssociation/list'
        redirectPath(pages.post('/productAssociation/update', [id: 'unknown'])) == '/productAssociation/list'
        redirectPath(pages.post('/productAssociation/delete', [id: 'unknown'])) == '/productAssociation/list'
        page(pages.get('/productAssociation/dialog', ['product.id': first.id])) != null

        when:
        Response saved = pages.post('/productAssociation/save', ['product.id': first.id,
                'associatedProduct.id': second.id, code: 'SUBSTITUTE', quantity: '2', hasMutualAssociation: 'true'])
        Map ids = ProductAssociation.withNewSession {
            ProductAssociation association = ProductAssociation.findByProductAndAssociatedProduct(
                    Product.get(first.id), Product.get(second.id))
            [id: association?.id, mutual: association?.mutualAssociation?.id,
             mutualQuantity: association?.mutualAssociation?.quantity]
        }

        then:
        redirectPath(saved) == "/product/edit/${first.id}"
        ids.id
        ids.mutual
        ids.mutualQuantity == 0.5

        expect:
        page(pages.get("/productAssociation/edit/${ids.id}")) != null
        page(pages.get('/productAssociation/dialog', ['product.id': first.id, id: ids.id])) != null
        page(pages.get('/productAssociation/list', [q: first.productCode, code: 'SUBSTITUTE'])).contains(first.productCode)
        pages.get('/productAssociation/list', [q: first.productCode, format: 'csv']).body.asString()
                .contains(first.productCode)
        pages.get('/productAssociation/export').body.asString().contains(first.productCode)

        when:
        Response updated = pages.post('/productAssociation/update', [id: ids.id, 'product.id': first.id,
                'associatedProduct.id': second.id, code: 'SUBSTITUTE', quantity: '4', hasMutualAssociation: 'true'])

        then:
        redirectPath(updated) == "/product/edit/${first.id}"
        ProductAssociation.withNewSession { ProductAssociation.get(ids.mutual).quantity } == 0.25

        when: 'the mutual association is dropped'
        Response dropped = pages.post('/productAssociation/update', [id: ids.id, quantity: '4'])

        then:
        redirectPath(dropped) == "/product/edit/${first.id}"
        ProductAssociation.withNewSession { ProductAssociation.get(ids.id).mutualAssociation } == null
        ProductAssociation.withNewSession { ProductAssociation.get(ids.mutual) } == null

        when:
        Response deleted = pages.post('/productAssociation/delete', [id: ids.id])

        then:
        redirectPath(deleted) == '/productAssociation/list'
        ProductAssociation.withNewSession { ProductAssociation.get(ids.id) } == null
    }

    void 'product attribute values export as excel'() {
        when:
        Response products = pages.get('/productAttributeValue/exportProductAttribute')
        Response sources = pages.get('/productAttributeValue/exportProductAttribute',
                [entityTypeCode: 'PRODUCT_SUPPLIER'])

        then:
        products.statusCode == 200
        products.contentType.startsWith('application/vnd.ms-excel')
        sources.statusCode == 200
        sources.header('Content-disposition').contains('productSourceAttribute.xls')
    }

    void 'stale forms do not overwrite attributes and categories'() {
        given:
        String name = randomUtil.randomStringFieldValue('Stale attribute')
        pages.post('/attribute/save', [name: name, code: name, active: 'false', required: 'false', allowOther: 'false',
                allowMultiple: 'false', exportable: 'false', option: ['A', 'B']])
        String attributeId = Attribute.withNewSession { Attribute.findByName(name).id }
        String categoryName = randomUtil.randomStringFieldValue('Stale category')
        pages.post('/category/save', [name: categoryName, 'parentCategory.id': rootCategory.id])
        String categoryId = Category.withNewSession { Category.findByName(categoryName).id }

        when:
        String staleAttribute = page(pages.post('/attribute/save', [id: attributeId, version: '-1', name: 'Changed']))
        String staleCategory = page(pages.post('/category/update', [id: categoryId, version: '-1', name: 'Changed']))

        then:
        staleAttribute.contains(name)
        staleCategory.contains(categoryName)
        Category.withNewSession { Category.get(categoryId).name } == categoryName

        cleanup:
        pages.post('/attribute/delete', [id: attributeId])
        pages.post('/category/delete', [id: categoryId])
    }

    @PendingFeature(reason = 'a single option is stored character by character')
    void 'an attribute saved with a single option keeps it whole'() {
        given:
        String name = randomUtil.randomStringFieldValue('Single option attribute')

        when:
        pages.post('/attribute/save', [name: name, code: name, active: 'false', required: 'false',
                allowOther: 'false', allowMultiple: 'false', exportable: 'false',
                entityTypeCode: EntityTypeCode.PRODUCT.name(), option: 'Green'])

        then:
        Attribute.withNewSession { Attribute.findByName(name).options.collect { it } } == ['Green']
    }

    @PendingFeature(reason = 'deleting a product type with products redirects twice (500)')
    void 'a product type with products is not deleted'() {
        given:
        String name = randomUtil.randomStringFieldValue('Used product type')
        pages.post('/productType/save', [name: name, code: randomUtil.randomStringFieldValue('UPT').take(10),
                displayedFields: ['NAME']])
        String id = ProductType.withNewSession { ProductType.findByName(name).id }
        Product typed = newProduct()
        Product.withNewTransaction {
            Product product = Product.get(typed.id)
            product.productType = ProductType.get(id)
            product.save(failOnError: true, flush: true)
        }

        when:
        Response refused = pages.post('/productType/delete', [id: id])

        then:
        redirectPath(refused).startsWith('/productType/list')
        ProductType.withNewSession { ProductType.get(id) } != null
        page(pages.get('/productType/list')).contains('some Products exist with this type')
    }

    @PendingFeature(reason = 'the catalog and association show pages fail to render')
    void 'the catalog and association show pages render'() {
        given:
        Product first = newProduct()
        Product second = newProduct()
        String catalogId = ProductCatalog.withNewTransaction {
            ProductCatalog catalog = new ProductCatalog(code: randomUtil.randomStringFieldValue('SC'),
                    name: randomUtil.randomStringFieldValue('Shown catalog'))
            catalog.addToProductCatalogItems(new ProductCatalogItem(product: Product.get(first.id)))
            catalog.save(failOnError: true, flush: true).id
        }
        pages.post('/productAssociation/save', ['product.id': first.id, 'associatedProduct.id': second.id,
                code: 'SUBSTITUTE', quantity: '1'])
        String associationId = ProductAssociation.withNewSession {
            ProductAssociation.findByProductAndAssociatedProduct(Product.get(first.id), Product.get(second.id)).id
        }

        expect:
        page(pages.get("/productCatalog/show/${catalogId}")).contains(first.name)
        page(pages.get("/productAssociation/show/${associationId}")).contains(second.name)
    }
}
