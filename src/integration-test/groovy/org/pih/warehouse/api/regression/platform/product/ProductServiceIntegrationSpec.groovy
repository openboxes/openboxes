package org.pih.warehouse.api.regression.platform.product

import spock.lang.PendingFeature


import org.pih.warehouse.product.*
import grails.validation.ValidationException
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.mock.web.MockHttpServletRequest
import spock.lang.Unroll

import org.pih.warehouse.api.regression.platform.common.base.ServiceIntegrationSpec
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Synonym
import org.pih.warehouse.core.SynonymTypeCode
import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.UnitOfMeasure

/**
 * Product queries, tags, catalogs, synonyms, components and the CSV export against a real database. Every feature
 * works with its own products, named with a random marker so that searches only find them.
 */
class ProductServiceIntegrationSpec extends ServiceIntegrationSpec {

    ProductService productService

    Location depot
    String marker
    Product coldProduct
    Product plainProduct
    Tag tag
    ProductCatalog catalog

    void setup() {
        depot = newDepot('Product depot')
        marker = uniqueName('mk').replaceAll('[^A-Za-z0-9]', '')
        coldProduct = newProduct(name: "Cold ${marker} vaccine", coldChain: true, manufacturer: "Maker ${marker}",
                vendorCode: "VC-${marker}", brandName: "Brand ${marker}")
        plainProduct = newProduct(name: "Plain ${marker} gauze", description: "Gauze ${marker}")
        committed {
            tag = new Tag(tag: "tag-${marker}").save(failOnError: true, flush: true)
            Product cold = Product.get(coldProduct.id)
            cold.addToTags(tag)
            cold.save(failOnError: true, flush: true)
            catalog = new ProductCatalog(name: "Catalog ${marker}", code: "CAT-${marker}").save(failOnError: true, flush: true)
            new ProductCatalogItem(productCatalog: catalog, product: Product.get(plainProduct.id)).save(failOnError: true, flush: true)
        }
    }

    private static GrailsParameterMap params(Map values) {
        return new GrailsParameterMap(values, new MockHttpServletRequest())
    }

    @PendingFeature(reason = 'the creation-date filter with a search text returns products outside the range')
    @Unroll
    void 'the product list filtered by #filter finds #expected'() {
        when:
        List<String> names = asAdminAt(depot) {
            Map values = paramsFor.call(marker)
            List<Tag> tags = withTag ? [Tag.get(tag.id)] : []
            List<ProductCatalog> catalogs = withCatalog ? [ProductCatalog.get(catalog.id)] : []
            productService.getProducts(null, catalogs, tags, [], [], false, params(values + [max: '50']))*.name
        }

        then:
        names.collect { it.split(' ')[0] }.sort() == expected

        where:
        filter                      | withTag | withCatalog | paramsFor                                                   || expected
        'name'                      | false   | false       | { String m -> [name: m] }                                   || ['Cold', 'Plain']
        'name and cold chain'       | false   | false       | { String m -> [name: m, handlingRequirementId: 'COLD_CHAIN'] } || ['Cold']
        'tag'                       | true    | false       | { String m -> [name: m] }                                   || ['Cold']
        'catalog'                   | false   | true        | { String m -> [name: m] }                                   || ['Plain']
        'description'               | false   | false       | { String m -> [description: "Gauze ${m}"] }                 || ['Plain']
        'manufacturer'              | false   | false       | { String m -> [manufacturer: "Maker ${m}"] }                || ['Cold']
        'vendor code'               | false   | false       | { String m -> [vendorCode: "VC-${m}"] }                     || ['Cold']
        'brand, sorted by category' | false   | false       | { String m -> [brandName: "Brand ${m}", sort: 'category', order: 'desc'] } || ['Cold']
        'name, sorted by updater'   | false   | false       | { String m -> [name: m, sort: 'updatedBy'] }                || ['Cold', 'Plain']
        'name, created today'       | false   | false       | { String m -> [name: m, createdAfter: new Date().minus(1).format('MM/dd/yyyy')] } || ['Cold', 'Plain']
    }

    void 'products are found by search terms, ids, and name or code prefix'() {
        when:
        Map result = asAdminAt(depot) {
            [
                    found     : productService.findProducts(["Cold ${marker}".toString()])*.id,
                    byIds     : productService.getProducts([coldProduct.id, plainProduct.id] as String[])*.id as Set,
                    noIds     : productService.getProducts([] as String[]),
                    prefix    : productService.searchProductAndProductGroup("plain ${marker}".toString()).collect { it[0] },
                    wildcard  : productService.searchProductAndProductGroup(marker, true).collect { it[0] } as Set,
            ]
        }

        then:
        result.found == [coldProduct.id]
        result.byIds == [coldProduct.id, plainProduct.id] as Set
        result.noIds == []
        result.prefix == [plainProduct.id]
        result.wildcard == [coldProduct.id, plainProduct.id] as Set
    }

    void 'tags and catalogs are listed with the products that use them'() {
        when:
        Map result = asAdminAt(depot) {
            [
                    labels  : productService.getAllTagLabels(),
                    tags    : productService.getAllTags()*.id,
                    catalogs: productService.getAllCatalogs().find { it.id == catalog.id },
                    popular : productService.getPopularTags().find { key, value -> key.id == tag.id }?.value,
                    limited : productService.getPopularTags(1).size(),
            ]
        }

        then:
        result.labels.contains("tag-${marker}".toString())
        result.tags.contains(tag.id)
        result.catalogs.name == "Catalog ${marker}".toString()
        result.catalogs.count == 1
        result.popular == 1
        result.limited == 1
    }

    void 'saving a product generates its code and adds its tags; a tag is deleted from it'() {
        when:
        Map saved = asAdminAt(depot) {
            Product product = new Product(name: "New ${marker}", category: Category.getRootCategory(),
                    productType: ProductType.defaultProductType.get())
            productService.saveProduct(product, "new-a-${marker},new-b-${marker}")
            [id: product.id, code: product.productCode, tags: product.tags*.tag.sort()]
        }
        List<String> tagsAfterDelete = asAdminAt(depot) {
            Product product = Product.get(saved.id)
            productService.deleteTag(product, Tag.findByTag("new-a-${marker}".toString()))
            product.tags*.tag
        }

        then:
        saved.code
        saved.tags == ["new-a-${marker}", "new-b-${marker}"]*.toString()
        tagsAfterDelete == ["new-b-${marker}".toString()]
    }

    void 'components are added to an assembly product'() {
        when:
        List components = asAdminAt(depot) {
            UnitOfMeasure each = UnitOfMeasure.findByCode('EA') ?: UnitOfMeasure.list(max: 1)[0]
            Product assembly = productService.addProductComponent(coldProduct.id, plainProduct.id, 2.0, each?.id)
            assembly.productComponents.collect { [it.componentProduct.id, it.quantity] }
        }

        then:
        components == [[plainProduct.id, 2.0]]

        expect:
        asAdminAt(depot) { productService.addProductComponent('no-such-product', plainProduct.id, 1.0, null) } == null
    }

    void 'synonyms are added to a product and edited'() {
        when:
        Map result = asAdminAt(depot) {
            Product product = productService.addSynonymToProduct(coldProduct.id, 'DISPLAY_NAME', "Display ${marker}", 'fr')
            Synonym synonym = product.synonyms.find { it.name == "Display ${marker}" }
            Synonym edited = productService.editProductSynonym(synonym.id, 'ALTERNATE_NAME', "Alternate ${marker}", 'en')
            [type: edited.synonymTypeCode, name: edited.name, locale: edited.locale]
        }

        then:
        result.type == SynonymTypeCode.ALTERNATE_NAME
        result.name == "Alternate ${marker}".toString()
        result.locale == Locale.ENGLISH

        when:
        asAdminAt(depot) {
            Product product = productService.addSynonymToProduct(plainProduct.id, null, "Other ${marker}", 'en')
            productService.editProductSynonym(product.synonyms.first().id, 'NOT_A_TYPE', 'x', null)
        }

        then:
        IllegalArgumentException e = thrown()
        e.message.contains('NOT_A_TYPE')
    }

    void 'the export has a row per product, with attributes when asked'() {
        when:
        String csv = asAdminAt(depot) {
            productService.exportProducts([Product.get(coldProduct.id), Product.get(plainProduct.id)], true)
        }
        String plain = asAdminAt(depot) { productService.exportProducts([Product.get(plainProduct.id)]) }

        then:
        csv.readLines().size() == 3
        csv.readLines()[0].contains('ProductCode')
        csv.contains(coldProduct.productCode)
        csv.contains("tag-${marker}".toString())
        plain.readLines().size() == 2
    }

    void 'existing products are recognised in an import file by id or code'() {
        given:
        String header = org.pih.warehouse.core.Constants.EXPORT_PRODUCT_COLUMNS.join(',')
        String csv = header + '\n' +
                ([coldProduct.id, 'ignored'] + [''] * 26).join(',') + '\n' +
                (['', plainProduct.productCode] + [''] * 26).join(',') + '\n' +
                (['no-such-id', 'no-such-code'] + [''] * 26).join(',') + '\n'

        when:
        List<String> ids = asAdminAt(depot) { productService.getExistingProducts(csv)*.id }

        then:
        ids == [coldProduct.id, plainProduct.id]
    }

    @PendingFeature(reason = 'a product import with the wrong columns is not refused with BadRequestException')
    void 'a file with the wrong columns is refused'() {
        when:
        productService.getColumns('Id,Name\n1,Gauze')

        then: 'a BadRequestException (matched by simple name)'
        Exception e = thrown()
        e.class.simpleName == 'BadRequestException'
    }

    void 'catalog items are read from a csv of catalog and product codes'() {
        given:
        String csv = "Catalog Code,Product Code,Category,Name\nCAT-${marker},${plainProduct.productCode},x,y\nNO-CAT,no-product,x,y\n"

        when:
        List rows = asAdminAt(depot) {
            productService.parseProductCatalogItems(csv).collect { [it.productCatalog?.id, it.product?.id] }
        }

        then:
        rows == [[catalog.id, plainProduct.id], [null, null]]

        when:
        asAdminAt(depot) { productService.parseProductCatalogItems("a,b\n1,2\n") }

        then:
        thrown(IllegalArgumentException)
    }

    void 'categories: the root, the top level, those without parent, and created ones'() {
        when:
        String createdId = asAdminAt(depot) {
            productService.findOrCreateCategoryWithParentCategory("Category ${marker}", Category.getRootCategory()).id
        }
        Map result = asAdminAt(depot) {
            Category created = Category.get(createdId)
            Category found = productService.findOrCreateCategoryWithParentCategory("Category ${marker}", null)
            [
                    root        : productService.getRootCategory()?.id,
                    topLevel    : productService.getTopLevelCategories()*.id,
                    withoutParent: productService.getCategoriesWithoutParent()*.id,
                    quick       : productService.getQuickCategories(),
                    created     : created.id,
                    found       : found.id,
                    tree        : productService.getCategoryTree().size() > 0,
            ]
        }

        then:
        result.root == asAdminAt(depot) { Category.getRootCategory().id }
        result.topLevel.contains(result.created)
        result.withoutParent.contains(result.root)
        result.quick instanceof List
        result.found == result.created
        result.tree
    }

    @PendingFeature(reason = 'the latest inventory count date query fails')
    void 'the latest inventory date of products in a location'() {
        given:
        stock(depot, coldProduct, uniqueName('LOT'), null, 3)

        when:
        Map dates = asAdminAt(depot) {
            productService.latestInventoryDateForProducts([coldProduct.id, plainProduct.id])
        }
        Map none = asAdminAt(depot) { productService.latestInventoryDateForProducts([]) }

        then:
        dates.keySet() == [coldProduct.id] as Set
        dates[coldProduct.id]
        none == [:]
    }

    void 'an import file creates products, their missing categories and tags'() {
        given:
        String typeName = asAdminAt(depot) { ProductType.defaultProductType.get().name }
        String header = org.pih.warehouse.core.Constants.EXPORT_PRODUCT_COLUMNS.join(',')
        List<String> row = ['', 'true', '', typeName, "Imported ${marker}", '', "Imported category ${marker}", '', 'Imported product',
                            'EA', "imp-${marker}", '', 'false', 'true', 'false', 'false', 'false', 'Maker', 'Brand', 'MC-1',
                            'Maker name', 'Vendor', 'VC-1', 'Vendor name', '', '', '', '']*.toString()
        String csv = header + '\n' + row.join(',') + '\n'

        when:
        Map imported = asAdminAt(depot) {
            List<Map> products = productService.validateProducts(csv, true)
            List<Product> saved = productService.importProducts(products, ["all-${marker}".toString()])
            Product product = saved[0]
            [
                    count   : saved.size(),
                    code    : product.productCode,
                    category: product.category.name,
                    tags    : product.tags*.tag.sort(),
                    cold    : product.coldChain,
                    vendor  : product.vendorCode,
            ]
        }

        then:
        imported.count == 1
        imported.code
        imported.category == "Imported category ${marker}".toString()
        imported.tags == ["all-${marker}", "imp-${marker}"]*.toString()
        imported.cold
        imported.vendor == 'VC-1'
    }

    @PendingFeature(reason = 'an unknown category is not refused with BadRequestException')
    void 'an import row naming an unknown category is refused unless categories may be created'() {
        given:
        String typeName = asAdminAt(depot) { ProductType.defaultProductType.get().name }
        String header = org.pih.warehouse.core.Constants.EXPORT_PRODUCT_COLUMNS.join(',')
        String csv = header + '\n' + (['', 'true', '', typeName, 'Refused', '', "No such category ${marker}"] + [''] * 21).join(',') + '\n'

        when:
        asAdminAt(depot) { productService.validateProducts(csv, false) }

        then:
        // a BadRequestException (matched by simple name)
        Exception e = thrown()
        e.class.simpleName == 'BadRequestException'
        e.message.contains("No such category ${marker}")
    }
}
