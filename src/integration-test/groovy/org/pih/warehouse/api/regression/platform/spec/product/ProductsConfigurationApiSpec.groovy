package org.pih.warehouse.api.regression.platform.spec.product

import spock.lang.PendingFeature




import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import org.pih.warehouse.api.spec.product.*
import grails.core.GrailsApplication
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.response.Response
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.regression.platform.client.product.ProductsConfigurationApi
import org.pih.warehouse.core.Constants
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product

/**
 * The product configuration wizard: category tree and product list options, their imports, and the category CSV.
 * Imports of configured options use an option added to the configuration for the test (a small file of its own),
 * so the shared database does not get the full shipped category trees or product lists.
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation
class ProductsConfigurationApiSpec extends ProductSourceSpec {

    static final String OPTION = 'testOption'

    @Autowired
    ProductsConfigurationApi productsConfigurationApi

    @Autowired
    GrailsApplication grailsApplication

    void cleanup() {
        wizardOptions('categoryOptions').remove(OPTION)
        wizardOptions('productOptions').remove(OPTION)
    }

    void 'the category count is the number of categories'() {
        given:
        long count = Category.withNewSession { Category.count() }

        expect:
        productsConfigurationApi.categoriesCount(new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data', Matchers.equalTo(count as int))
                .build())
    }

    void 'the categories download as CSV with their parents; the template is the header line'() {
        when:
        Response categories = productsConfigurationApi.downloadCategories(responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        categories.contentType.startsWith('text/csv')
        categories.header('Content-disposition').contains('Categories.csv')
        categories.asString().readLines().first().replace('\uFEFF', '') == 'Category name,Parent Category Name'  // after the byte order mark
        categories.asString().contains(rootCategory.name)

        when:
        Response template = productsConfigurationApi.downloadCategoryTemplate(responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        template.contentType.startsWith('text/csv')
        template.asString() == 'Category Name,Parent Category Name\n'
    }

    @PendingFeature(reason = 'children listed before their parent are created without it')
    void 'a configured category tree is imported under its root, children listed before their parents too'() {
        given:
        String top = randomUtil.randomStringFieldValue('Top')
        String child = randomUtil.randomStringFieldValue('Child')
        configureOption('categoryOptions', [enabled: true, rootCategoryName: Constants.ROOT_CATEGORY_NAME,
                fileUrl: fileUrl("Category Name,Parent Category Name\nROOT,\n${child},${top}\n${top},ROOT\n")])

        when:
        productsConfigurationApi.importCategories(OPTION, responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        parentOf(top) == Constants.ROOT_CATEGORY_NAME
        parentOf(child) == top
    }

    void 'importing a disabled category option, or none, changes nothing'() {
        given:
        String top = randomUtil.randomStringFieldValue('Top')
        configureOption('categoryOptions', [enabled: false, fileUrl: fileUrl("Name,Parent\n${top},ROOT\n")])

        when:
        productsConfigurationApi.importCategories(OPTION, responseSpecUtil.OK_RESPONSE_SPEC)
        productsConfigurationApi.importCategories(null, responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        Category.withNewSession { Category.findByName(top) } == null
    }

    @PendingFeature(reason = 'an unknown option answers 500, not 400')
    void 'importing an unknown category option answers 400'() {
        expect:
        productsConfigurationApi.importCategories('noSuchOption', error(HttpStatus.SC_BAD_REQUEST))
    }

    @PendingFeature(reason = 'parents are not resolved across the file (ROOT parent)')
    void 'an uploaded category CSV is imported under the root category'() {
        given:
        String top = randomUtil.randomStringFieldValue('Uploaded')
        String child = randomUtil.randomStringFieldValue('Uploaded child')
        byte[] csv = "Category Name,Parent Category Name\n${top},ROOT\n${child},${top}\n".getBytes('UTF-8')

        when:
        productsConfigurationApi.importCategoryCsv('categories.csv', 'text/csv', csv, responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        parentOf(top) == Constants.ROOT_CATEGORY_NAME
        parentOf(child) == top
    }

    @PendingFeature(reason = 'a bad category CSV answers 500, not 400')
    void 'an uploaded category CSV that is #problem answers 400 and imports nothing'() {
        given:
        String name = randomUtil.randomStringFieldValue('Refused')

        when:
        productsConfigurationApi.importCategoryCsv('categories.csv', mimeType, content(name).getBytes('UTF-8'),
                error(HttpStatus.SC_BAD_REQUEST))

        then:
        Category.withNewSession { Category.findByName(name) } == null

        where:
        problem                    | mimeType     | content
        'empty'                    | 'text/csv'   | { String n -> '' }
        'not a CSV file'           | 'text/plain' | { String n -> "Name,Parent\n${n},ROOT\n" }
        'naming an unknown parent' | 'text/csv'   | { String n -> "Name,Parent\n${n},ROOT\nOrphan ${n},No such parent ${n}\n" }
        'missing the parent'       | 'text/csv'   | { String n -> "Name,Parent\n${n},ROOT\nNo parent ${n}\n" }
        'a cycle of parents'       | 'text/csv'   | { String n -> "Name,Parent\n${n},ROOT\nA ${n},B ${n}\nB ${n},A ${n}\n" }
    }

    void 'a configured product list is imported by a superuser'() {
        given:
        String code = randomUtil.randomStringFieldValue('WIZ').take(30)
        String name = randomUtil.randomStringFieldValue('Wizard product')
        configureOption('productOptions', [enabled: true, fileUrl: fileUrl(productCsv(code, name))])

        when:
        productsConfigurationApi.importProducts(OPTION, responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        Product.withNewSession { Product.findByProductCode(code)?.name } == name
    }

    @PendingFeature(reason = 'an unknown product option answers 500, not 400')
    void 'importing an unknown product option answers 400; a disabled one changes nothing'() {
        given:
        String code = randomUtil.randomStringFieldValue('OFF').take(30)
        configureOption('productOptions', [enabled: false, fileUrl: fileUrl(productCsv(code, 'Disabled product'))])

        expect:
        productsConfigurationApi.importProducts('noSuchOption', error(HttpStatus.SC_BAD_REQUEST))
        productsConfigurationApi.importProducts(OPTION, responseSpecUtil.OK_RESPONSE_SPEC)
        Product.withNewSession { Product.findByProductCode(code) } == null
    }

    /** One product row in the product export's column layout (the import reads that layout). */
    private static String productCsv(String code, String name) {
        List<String> header = Constants.EXPORT_PRODUCT_COLUMNS
        List<String> row = [''] * header.size()
        row[1] = 'true'          // Active
        row[2] = code            // ProductCode
        row[3] = 'Default'       // ProductType
        row[4] = name            // Name
        return header.join(',') + '\n' + row.join(',') + '\n'
    }

    private Map wizardOptions(String kind) {
        return grailsApplication.config.openboxes.configurationWizard[kind] as Map
    }

    private void configureOption(String kind, Map option) {
        wizardOptions(kind).put(OPTION, option)
    }

    private static String fileUrl(String content) {
        File file = File.createTempFile('wizard-option', '.csv')
        file.deleteOnExit()
        file.setText(content, 'UTF-8')
        return file.toURI().toString()
    }

    private static String parentOf(String name) {
        return Category.withNewSession { Category.findByName(name)?.parentCategory?.name }
    }
}
