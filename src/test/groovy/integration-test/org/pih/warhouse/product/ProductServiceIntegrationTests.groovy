package org.pih.warehouse.product

import grails.testing.services.ServiceUnitTest
import spock.lang.Ignore

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Role
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.User
import org.pih.warehouse.importer.CSVUtils
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Shared
import spock.lang.Specification
import testutils.DbHelper
import static org.junit.Assert.*;

@Ignore('Fix these tests and move them to ProductServiceSpec or convert them to API tests')
class ProductServiceIntegrationTests extends Specification implements ServiceUnitTest<ProductService> {

    @Shared
    @Autowired
    AuthService authService
    @Shared
    def product1
    @Shared
    def product2
    @Shared
    def product3
    @Shared
    def product4
    @Shared
    def product5
    @Shared
    def product6
    @Shared
    def group1
    @Shared
    def group2

    protected void setup() {
        when:
        Role financeRole = Role.findByRoleType(RoleType.ROLE_FINANCE)
        User user = DbHelper.findOrCreateAdminUser('Justin', 'Miranda', 'justin@openboxes.com', 'jmiranda', 'password', true)
        user.addToRoles(financeRole)
        user.save()

        authService.currentUser = user

        product1 = DbHelper.findOrCreateProductWithGroups('boo floweree 250mg', ['Hoo moodiccina', 'Boo floweree'])
        product2 = DbHelper.findOrCreateProductWithGroups('boo pill', ['Boo floweree'])
        product3 = DbHelper.findOrCreateProductWithGroups('foo', ['Hoo moodiccina'])
        product4 = DbHelper.findOrCreateProductWithGroups('abc tellon', ['Hoo moodiccina'])
        product5 = DbHelper.findOrCreateProductWithGroups('goomoon', ['Boo floweree'])
        product6 = DbHelper.findOrCreateProductWithGroups('buhoo floweree root', [])
        group1 = ProductGroup.findByName("Hoo moodiccina")
        group2 = ProductGroup.findByName("Boo floweree")

        // Create new root category if it doesn't exist
        def category = DbHelper.findOrCreateCategory('ROOT')
        category.isRoot = true
        category.save(failOnError: true, flush: true)
        then:
        assertTrue !category.hasErrors()

        // Create products with tags
        when:
        DbHelper.findOrCreateProductWithTags('Ibuprofen 200mg tablet', ['nsaid', 'pain', 'favorite'])
        DbHelper.findOrCreateProductWithTags('Acetaminophen 325mg tablet', ['pain', 'pain reliever'])
        DbHelper.findOrCreateProductWithTags('Naproxen 220mg tablet', ['pain reliever', 'pain', 'nsaid', 'fever reducer'])

        // Create a tag without products
        DbHelper.findOrCreateTag('tagwithnoproducts')

        // Create a product with a unique product code
        def product7 = DbHelper.findOrCreateProduct('Test Product')
        product7.productCode = 'AB13'
        product7.save(failOnError: true, flush: true)
        then:
        assertNotNull product7
        assertTrue !product7.hasErrors()
    }

    protected void tearDown() {
        AuthService.currentUser.remove()
    }

    /**
     * Adds quotes around each element and a newline after the end of the row.
     */
    String csvize(row) {
        return csvize(row, ",")
    }

    String csvize(row, delimiter) {
        return "\"" + row.join("\"" + delimiter + "\"") + "\"\n"
    }

    void csvize() {
        when:
        def row = ["1", "test", "another", "last one"]

        then:
        assertEquals csvize(row), "\"1\",\"test\",\"another\",\"last one\"\n"
    }

    void searchProductAndProductGroup_shouldGetAllProductsUnderMatchedGroups(){
        when:
        def result = service.searchProductAndProductGroup("floweree", true)
        println result

        // Only searches products, not product groups any longer
        then:
        assert result.size() == 2
        assert result.any{ it[1] == "boo floweree 250mg" && it[2] == "boo floweree 250mg" && it[0] == product1.id}
        assert result.any{ it[1] == "buhoo floweree root" &&  it[2] == "buhoo floweree root" && it[0] == product6.id}

    }

    void validateProducts_shouldFailWhenProductNameIsMissing() {
        when:
        def row = ["1235", "SKU-1", "", "", "category 123", "", "Description", "Unit of Measure", "tag1,tag2", "0.01", "", "", "", "", "", "Manufacturer", "Brand", "ManufacturerCode", "Manufacturer Name", "Vendor", "Vendor Code", "Vendor Name", "UPC", "NDC", "Date Created", "Date Updated"]
        def csv = csvize(Constants.EXPORT_PRODUCT_COLUMNS) + csvize(row)

        def message = shouldFail(RuntimeException) {
            service.validateProducts(csv)
        }
        then:
        assertTrue message.contains("Product name cannot be empty")
    }

    void importProducts_shouldNotUpdateProductsWhenSaveToDatabaseIsFalse() {
        when:
        def product = DbHelper.findOrCreateProduct('Sudafed')
        assertNotNull product.id
        def row1 = ["${product.id}", "", "Sudafed 2", "OTC Medicines", "", "Description", "Unit of Measure", "tag1,tag2", "0.01", "Manufacture", "Brand", "ManufacturerCode", "Manufacturer Name", "Vendor", "Vendor Code", "Vendor Name", "UPC", "NDC", "Date Created", "Date Updated"]
        def csv = csvize(Constants.EXPORT_PRODUCT_COLUMNS) + csvize(row1)
        def products = service.validateProducts(csv)
        service.importProducts(products)
        def product2 = Product.get(product.id)
        println ("Get product " + product2.name + " " + product2.id + " " + product2.productCode + " " + product.isAttached())
        then:
        assertEquals "Sudafed", product2.name
        assertEquals "Medicines", product2.category.name
    }

    void validateProducts_shouldCreateNewProductWithNewCategory() {
        when:
        def category = Category.findByName("category 123")

        then:
        assertNull category

        when:
        def row1 = ["1235", "", "Default", "product 1235", "", "category 123", "", "Description", "Unit of Measure", "tag1,tag2", "0.01", null, null, null, null, null, "Manufacturer", "Brand", "ManufacturerCode", "Manufacturer Name", "Vendor", "Vendor Code", "Vendor Name", "UPC", "NDC", "Date Created", "Date Updated"]
        def csv = csvize(Constants.EXPORT_PRODUCT_COLUMNS) + csvize(row1)
        def products = service.validateProducts(csv)

        // FIXME Hack to keep from running into the following error
        // not-null property references a null or transient value: org.pih.warehouse.product.Product.dateCreated; nested exception is org.hibernate.PropertyValueException: not-null property references a null or transient value: org.pih.warehouse.product.Product.dateCreated
        products[0].dateCreated = new Date()
        products[0].lastUpdated = new Date()

        service.importProducts(products)
        def product = Product.findByName("product 1235")

        then:
        assertNotNull product

        when:
        category = Category.findByName("category 123")

        then:
        assertNotNull category
    }

    void importProducts_shouldCreateNewProductWithExistingCategory() {
        when:
        def category = Category.findByName("Medicines")

        then:
        assertNotNull category

        when:
        def row1 = ["1235", "", "Default", "product 1235", "", "Medicines", "", "Description", "Unit of Measure", "tag1,tag2", "0.01", null, null, null, null, null, "Manufacturer", "Brand", "ManufacturerCode", "Manufacturer Name", "Vendor", "Vendor Code", "Vendor Name", "UPC", "NDC", "Date Created", "Date Updated"]
        def csv = csvize(Constants.EXPORT_PRODUCT_COLUMNS) + csvize(row1)
        def products = service.validateProducts(csv)

        // FIXME Hack to keep from running into the following error
        // not-null property references a null or transient value: org.pih.warehouse.product.Product.dateCreated; nested exception is org.hibernate.PropertyValueException: not-null property references a null or transient value: org.pih.warehouse.product.Product.dateCreated
        products[0].dateCreated = new Date()
        products[0].lastUpdated = new Date()

        service.importProducts(products)
        def product = Product.findByName("product 1235")

        then:
        assertNotNull product
        assertEquals category, product.category
    }

    void importProducts_shouldUpdateNameOnExistingProduct() {
        when:
        def productBefore = DbHelper.findOrCreateProduct('Sudafed')

        then:
        assertNotNull productBefore.id

        when:
        def row1 = ["${productBefore.id}", "AB12", "Default", "Sudafed 2.0", "", "Medicines", "", "Description", "Unit of Measure", "tag1,tag2", "0.01", "LotAndExpiryControl", "ColdChain", "ControlledSubstance", "HazardousMaterial", "Reconditioned", "Manufacturer", "Brand", "ManufacturerCode", "Manufacturer Name", "Vendor", "Vendor Code", "Vendor Name", "UPC", "NDC", "Date Created", "Date Updated"]
        def csv = csvize(Constants.EXPORT_PRODUCT_COLUMNS) + csvize(row1)

        def products = service.validateProducts(csv)
        service.importProducts(products)

        def productAfter = Product.get(productBefore.id)

        then:
        assertEquals "Sudafed 2.0", productAfter.name
    }

    void importProducts_shouldUpdateAllFieldsOnExistingProduct() {
        when:
        def productBefore = DbHelper.findOrCreateProduct('Sudafed')

        then:
        assertNotNull productBefore.id
        def row1 = [productBefore.id, "AB12", "Default", "Sudafed 2.0", "", "Medicines", "", "It's sudafed, dummy.", "EA", "tag1,tag2", "0.01", null, "true", null, null, null, "Acme", "Brand X", "ACME-249248", "Manufacturer Name", "Vendor", "Vendor Code", "Vendor Name", "UPC-1202323", "NDC-122929-39292", "", ""]
        def csv = csvize(Constants.EXPORT_PRODUCT_COLUMNS) + csvize(row1)

        when:
        def products = service.validateProducts(csv)
        service.importProducts(products)
        def productAfter = Product.get(productBefore.id)

        then:
        assertEquals productBefore.id, productAfter.id
        assertEquals "AB12", productAfter.productCode
        assertEquals "Sudafed 2.0", productAfter.name
        assertEquals "Medicines", productAfter.category.name
        assertEquals "It's sudafed, dummy.", productAfter.description
        assertEquals "EA", productAfter.unitOfMeasure
        assertEquals "Acme", productAfter.manufacturer
        assertEquals "Brand X", productAfter.brandName
        assertEquals "ACME-249248", productAfter.manufacturerCode
        assertEquals "Manufacturer Name", productAfter.manufacturerName
        assertEquals "Vendor", productAfter.vendor
        assertEquals "Vendor Code", productAfter.vendorCode
        assertEquals "Vendor Name", productAfter.vendorName
        assertTrue productAfter.coldChain
        assertEquals "UPC-1202323", productAfter.upc
        assertEquals "NDC-122929-39292", productAfter.ndc
    }

    void importProducts_shouldAddExistingTags() {
        when:
        def tag1 = new Tag(tag: "tag1").save(flush: true, failOnError: true);
        def tag2 = new Tag(tag: "tag2").save(flush: true, failOnError: true);
        def productBefore = DbHelper.findOrCreateProduct('Sudafed')

        then:
        assertNotNull productBefore.id
        assertEquals 0, productBefore?.tags?.size()?:0

        when:
        def row1 = ["${productBefore.id}", "AB12", "Default", "Sudafed 2.0", "", "Medicines", "", "It's sudafed, dummy.", "EA", "tag1,tag2", "0.01", null, null, null, null, null, "Acme", "Brand X", "ACME-249248", "Manufacturer Name", "Vendor", "Vendor Code", "Vendor Name", "UPC-1202323", "NDC-122929-39292", "", ""]

        def csv = csvize(Constants.EXPORT_PRODUCT_COLUMNS) + csvize(row1)
        def tags = ["tag1", "tag2"] as List

        def products = service.validateProducts(csv)
        service.importProducts(products, tags)

        def productAfter = Product.get(productBefore.id)
        println ("Get product " + productAfter.name + " " + productAfter.id + " " + productAfter.productCode)

        then:
        assertEquals productBefore.id, productAfter.id
        assertEquals 2, productAfter?.tags?.size()
    }

    void importProducts_shouldAddNewTags() {
        when:
        def tag1 = new Tag(tag: "tag1").save(flush: true, failOnError: true);
        def tag2 = new Tag(tag: "tag2").save(flush: true, failOnError: true);

        def product = DbHelper.findOrCreateProduct('Sudafed')

        then:
        assertNotNull product.id
        assertEquals 0, product?.tags?.size()?:0

        when:
        def row1 = ["${product.id}", "AB12", "Default", "Sudafed 2.0", "", "Medicines", "", "It's sudafed, dummy.", "EA", "tag1,tag2", "0.01", null, null, null, null, null, "Acme", "Brand X", "ACME-249248", "Manufacturer Name", "Vendor", "Vendor Code", "Vendor Name", "UPC-1202323", "NDC-122929-39292", "", ""]
        def csv = csvize(Constants.EXPORT_PRODUCT_COLUMNS) + csvize(row1)
        def tags = ["tag3", "tag4", "tag1", "tag2"] as List

        def products = service.validateProducts(csv)
        service.importProducts(products, tags)

        def result = Product.get(product.id)
        println ("Get product " + result.name + " " + result.id + " " + result.productCode)

        then:
        assertEquals product.id, result.id
        assertEquals 4, result?.tags?.size()
    }

    void getDelimiter_shouldDetectCommaDelimiter() {
        // def row = ["1235","SKU-1","","category 123","Description","Unit of Measure","Manufacture","Brand","ManufacturerCode","Manufacturer Name","Vendor","Vendor Code","Vendor Name","false","UPC","NDC","Date Created","Date Updated"]
        when:
        def row1 = ["", "AB12", "Sudafed 2", "Medicines", "", "Sudafed description", "EA", "tag1,tag2", "0.01", "Acme", "Brand X", "ACME-249248", "Vendor Y", "Y-1284", "Sudafed", "UPC-1202323", "NDC-122929-39292", "", ""]
        def csv = csvize(Constants.EXPORT_PRODUCT_COLUMNS) + csvize(row1)
        def delimiter = service.getDelimiter(csv)

        then:
        assertEquals ",", delimiter
    }

    void getDelimiter_shouldDetectTabDelimiter() {
        when:
        def row1 = ["", "AB12", "Sudafed 2", "Medicines", "", "Sudafed descrition", "each", "tag1,tag2", "0.01", "Acme", "Brand X", "ACME-249248", "Manufacturer Name", "Vendor", "Vendor Code", "Vendor Name", "UPC-1202323", "NDC-122929-39292", "", ""]
        def csv = csvize(Constants.EXPORT_PRODUCT_COLUMNS, "\t") + csvize(row1, "\t")
        def delimiter = service.getDelimiter(csv)

        then:
        assertEquals "\t", delimiter
    }

    void getDelimiter_shouldDetectSemiColonDelimiter() {
        when:
        def row1 = ["", "00001", "Sudafed 2", "Medicines", "Sudafed description", "each", "tag1,tag2", "0.01", "Acme", "Brand X", "ACME-249248", "Manufacturer Name", "Vendor", "Vendor Code", "Vendor Name", "UPC-1202323", "NDC-122929-39292", "", ""]
        def csv = csvize(Constants.EXPORT_PRODUCT_COLUMNS, ";") + csvize(row1, ";")
        def delimiter = service.getDelimiter(csv)

        then:
        assertEquals ";", delimiter
    }

    void findOrCreateCategory_shouldReturnExistingCategory() {
        when:
        def categoryName = "Medicines"
        def existingCategory = Category.findByName(categoryName)

        then:
        assertNotNull existingCategory

        when:
        def category = service.findOrCreateCategory(categoryName)

        then:
        assertEquals existingCategory, category
    }

    void findOrCreateCategory_shouldCreateNewCategory() {
        when:
        def categoryName = "Nonexistent Category"
        def nonexistentCategory = Category.findByName(categoryName)

        then:
        assertNull nonexistentCategory

        when:
        def category = service.findOrCreateCategory(categoryName)
        def existingCategory = Category.findByName(categoryName)

        then:
        assertEquals existingCategory, category
    }

    void findOrCreateCategory_shouldReturnRootCategoryOnRoot() {
        when:
        def categoryName = "ROOT"
        def category = service.findOrCreateCategory(categoryName)

        then:
        assertEquals category.name, "ROOT"
    }

    void findOrCreateCategory_shouldReturnRootCategoryOnEmpty() {
        when:
        def categoryName = ""
        def category = service.findOrCreateCategory(categoryName)

        then:
        assertNotNull category
        assertEquals category.name, "ROOT"
    }

    void exportProducts_shouldReturnAllProducts() {
        when:
        def csv = service.exportProducts()
        def lines = csv.split(/[\r\n]/)

        // FIXME Export code appends column delimiter for every column (even the last)
        def expectedHeader = Constants.EXPORT_PRODUCT_COLUMNS.join(",").replace("\n", "") + ","
        def actualHeader = CSVUtils.stripBomIfPresent(lines[0])

        then:
        assertNotNull lines
        assertEquals expectedHeader, actualHeader
    }

    void exportProducts_shouldRenderProductsAsCsv() {
        when:
        def csv = service.exportProducts()
        def lines = csv.split(/[\r\n]/)

        // Remove quotes
        def columns = CSVUtils.stripBomIfPresent(lines[0]).replaceAll("\"", "").split(",")

        then:
        columns.eachWithIndex { String entry, int i ->
            assertEquals Constants.EXPORT_PRODUCT_COLUMNS[i], entry
        }
    }

    void getExistingProducts() {
        when:
        def product1 = DbHelper.findOrCreateProduct('Sudafed')
        def product2 = DbHelper.findOrCreateProduct('Advil')

        then:
        assertNotNull product1.id

        when:
        def row1 = ["${product1.id}", "", "Sudafed", "Medicines", "", "", "", "", "false", "", "", "", ""]
        def row2 = ["${product2.id}", "", "Advil", "Medicines", "", "", "", "", "", "false", "", "", "", ""]
        def csv = csvize(Constants.EXPORT_PRODUCT_COLUMNS) + csvize(row1) + csvize(row2)

        def existingProducts = service.getExistingProducts(csv)
        then:
        assertEquals 2, existingProducts.size()
        assertEquals "Sudafed", existingProducts[0].name
        assertEquals "Advil", existingProducts[1].name
    }

    void getExistingProducts_shouldReturnAdvil() {
        when:
        def product = DbHelper.findOrCreateProduct('Advil')

        then:
        assertNotNull product.id

        when:
        def row1 = ["", "", "Sudafed", "Medicines", "", "", "", "", "false", "", "", "", ""]
        def row2 = ["${product.id}", "", "Advil", "Medicines", "", "", "", "", "false", "", "", "", ""]
        def csv = csvize(Constants.EXPORT_PRODUCT_COLUMNS) + csvize(row1) + csvize(row2)
        def existingProducts = service.getExistingProducts(csv)

        then:
        assertEquals 1, existingProducts.size()
        assertEquals "Advil", existingProducts[0].name
    }

    void getAllTagLabels() {
        when:
        def service = new ProductService();
        def tags = service.getAllTagLabels()

        then:
        assertEquals 6, tags.size()
    }

    void getPopularTags() {
        when:
        def popularTagMap = service.getPopularTags()
        def expectedTags = ["favorite", "fever reducer", "nsaid", "pain", "pain reliever"]

        then:
        assertNotNull popularTagMap
        assertEquals 5, popularTagMap.keySet().size()

        when:
        def actualTags = popularTagMap.keySet().collect { it.tag }

        then:
        assertEquals expectedTags, actualTags
        assertEquals 1, popularTagMap[Tag.findByTag("fever reducer")]
        assertEquals 2, popularTagMap[Tag.findByTag("nsaid")]
        assertEquals 3, popularTagMap[Tag.findByTag("pain")]
        assertEquals 2, popularTagMap[Tag.findByTag("pain reliever")]
    }

    void getAllCategories() {
        when:
        def categories = Category.list()

        then:
        assertNotNull categories
        categories.each {
            println it.id + ":" + it?.name + ":" + it?.parentCategory?.name
        }
    }

    void getRootCategory() {
        when:
        def rootCategory = service.getRootCategory()

        then:
        assertNotNull rootCategory
        assertEquals rootCategory.name, "ROOT"
        assertTrue rootCategory.isRoot
        assertTrue rootCategory.isRootCategory()
    }

    // FIXME the data counted here are not created in this file
    @Ignore
    void getTopLevelCategories() {
        when:
        def topLevelCategories = service.getTopLevelCategories()

        then:
        assertNotNull topLevelCategories
        assertEquals 5, topLevelCategories.size()
    }

    void deleteTag_shouldDeleteTagFromDatabase() {
        when:
        def product = Product.findByName("Ibuprofen 200mg tablet")
        then:
        assertNotNull product
        assertEquals product.tagsToString(), "favorite,nsaid,pain"
        when:
        Tag tag = Tag.findByTag("favorite")
        then:
        assertNotNull tag
        when:
        service.deleteTag(product, tag)
        then:
        assertEquals "nsaid,pain", product.tagsToString()
        when:
        Tag tag2 = Tag.findByTag("favorite")
        then:
        assertNull tag2
    }

    void generateProductIdentifier_shouldGenerateUniqueIdentifiers() {
        when: "generate product Identifier"
        for (int i = 0; i<100; i++) {
            assert service.generateProductIdentifier()
        }

        then: assert true // workaround for time being
    }

    void saveProduct_failOnValidationError() {
        when:
        def product = new Product();
        def returnValue = service.saveProduct(product)

        then:
        assertNull returnValue
        assertEquals 3, product.errors.getErrorCount()
        assertNotNull product.errors.getFieldError("name")
        assertNotNull product.errors.getFieldError("category")
        assertNotNull product.errors.getFieldError("productType")
        assertNull product.errors.getFieldError("description")
    }

    void saveProduct_shouldSaveProduct() {
        when:
        def product = new Product();
        def productType = DbHelper.findOrCreateProductType("Default")
        product.name = "Test product"
        product.category = Category.getRootCategory()
        product.productType = productType
        def returnValue = service.saveProduct(product)

        then:
        assertNotNull returnValue
        assertEquals product, returnValue
        assertEquals returnValue.category, Category.getRootCategory()
        assertNotNull product.productCode
    }

    void saveProduct_shouldSaveProductAndTags() {
        when:
        def product = new Product();
        def productType = DbHelper.findOrCreateProductType("Default")
        product.name = "Test product"
        product.category = Category.getRootCategory()
        product.productType = productType

        def returnValue = service.saveProduct(product, "a tag,the next tag,another tag")
        then:
        assertNotNull returnValue
        assertNotNull product.id
        assertNotNull product.tags
        assertEquals 3, product.tags.size()
    }

    void saveProduct_shouldFailOnInvalidCategory() {
        when:
        def product = new Product();
        def productType = DbHelper.findOrCreateProductType("Default")
        product.productType = productType
        service.saveProduct(product)

        then:
        assertNotNull product.errors.getFieldError("category")
    }

    void saveProduct_shouldGenerateUniqueProductCode() {
        when:
        def product = new Product();
        def productType = DbHelper.findOrCreateProductType("Default")
        product.name = "New product"
        product.category = Category.getRootCategory()
        product.productType = productType
        service.saveProduct(product)

        then:
        assertNotNull product
        assertNotNull product.productCode
    }

    void saveProduct_shouldFailOnDuplicateProductCode() {
        when:
        def product = Product.findByProductCode("AB13")
        def productType = DbHelper.findOrCreateProductType("Default")

        then:
        assertNotNull product

        when:
        product = new Product();
        product.name = "New product"
        product.productCode = "AB13"
        product.category = Category.getRootCategory()
        product.productType = productType
        def returnValue = service.saveProduct(product)

        then:
        assertNull returnValue
        assertNotNull product.errors.getFieldError("productCode")
    }

    void findOrCreateTag_shouldCreateTagSuccessfully() {
        when:
        def tag1 = Tag.findByTag("brand new tag")

        then:
        assertNull tag1

        when:
        def tag2 = service.findOrCreateTag("brand new tag")

        then:
        assertNotNull tag2
    }

    void findOrCreateTag_shouldFindExistingTag() {
        when:
        def tag1 = Tag.findByTag("favorite")

        then:
        assertNotNull tag1
        assertEquals 1, Tag.findAllByTag("favorite").size()

        when:
        def tag2 = service.findOrCreateTag("favorite")

        then:
        assertNotNull tag2
        assertEquals tag1.id, tag2.id
        assertEquals tag1, tag2

        // Make sure there's still only one "favorite" tag
        assertEquals 1, Tag.findAllByTag("favorite").size()
    }
}
