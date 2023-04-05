package org.pih.warehouse.product

import org.junit.Ignore
import org.junit.Test
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Role
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.User
import org.pih.warehouse.importer.CSVUtils
import testutils.DbHelper

class ProductServiceIntegrationTests extends GroovyTestCase {


	def productService
	def product1;
	def product2;
	def product3;
	def product4;
	def product5;
	def product6;
	def group1;
	def group2;

    /**
     *
     */
	protected void setUp() {
		super.setUp()

		Role financeRole = Role.findByRoleType(RoleType.ROLE_FINANCE)
		User user = DbHelper.findOrCreateAdminUser('Justin', 'Miranda', 'justin@openboxes.com', 'jmiranda', 'password', true)
		user.addToRoles(financeRole)
		user.save()

		AuthService.currentUser.set(user)

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
        assertTrue !category.hasErrors()

        // Create products with tags
		DbHelper.findOrCreateProductWithTags('Ibuprofen 200mg tablet', ['nsaid', 'pain', 'favorite'])
		DbHelper.findOrCreateProductWithTags('Acetaminophen 325mg tablet', ['pain', 'pain reliever'])
		DbHelper.findOrCreateProductWithTags('Naproxen 220mg tablet', ['pain reliever', 'pain', 'nsaid', 'fever reducer'])

		// Create a tag without products
		DbHelper.findOrCreateTag('tagwithnoproducts')

		// Create a product with a unique product code
		def product7 = DbHelper.findOrCreateProduct('Test Product')
		product7.productCode = 'AB13'
		product7.save(failOnError: true, flush: true)
		assertNotNull product7
		assertTrue !product7.hasErrors()
	}

	protected void tearDown() {
		AuthService.currentUser.remove()
		super.tearDown()
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

    @Test
	void csvize() {
		def row = ["1", "test", "another", "last one"]
		assertEquals csvize(row), "\"1\",\"test\",\"another\",\"last one\"\n"
	}

    @Test
	void searchProductAndProductGroup_shouldGetAllProductsUnderMatchedGroups(){
		def result = productService.searchProductAndProductGroup("floweree", true)
		println result

		// Only searches products, not product groups any longer
		assert result.size() == 2
		assert result.any{ it[1] == "boo floweree 250mg" && it[2] == "boo floweree 250mg" && it[0] == product1.id}
		assert result.any{ it[1] == "buhoo floweree root" &&  it[2] == "buhoo floweree root" && it[0] == product6.id}

	}

    @Test
	void validateProducts_shouldFailWhenProductNameIsMissing() {
		def row = ["1235", "SKU-1", "", "", "category 123", "", "Description", "Unit of Measure", "tag1,tag2", "0.01", "", "", "", "", "", "Manufacturer", "Brand", "ManufacturerCode", "Manufacturer Name", "Vendor", "Vendor Code", "Vendor Name", "UPC", "NDC", "Date Created", "Date Updated"]
		def csv = csvize(Constants.EXPORT_PRODUCT_COLUMNS) + csvize(row)

		def message = shouldFail(RuntimeException) {
			productService.validateProducts(csv)
		}
		assertTrue message.contains("Product name cannot be empty")
	}

    @Ignore
	void importProducts_shouldNotUpdateProductsWhenSaveToDatabaseIsFalse() {
		def product = DbHelper.findOrCreateProduct('Sudafed')
		assertNotNull product.id
		def row1 = ["${product.id}", "", "Sudafed 2", "OTC Medicines", "", "Description", "Unit of Measure", "tag1,tag2", "0.01", "Manufacture", "Brand", "ManufacturerCode", "Manufacturer Name", "Vendor", "Vendor Code", "Vendor Name", "UPC", "NDC", "Date Created", "Date Updated"]
		def csv = csvize(Constants.EXPORT_PRODUCT_COLUMNS) + csvize(row1)
        def products = productService.validateProducts(csv)
        productService.importProducts(products)
		def product2 = Product.get(product.id)
		println ("Get product " + product2.name + " " + product2.id + " " + product2.productCode + " " + product.isAttached())
		assertEquals "Sudafed", product2.name
		assertEquals "Medicines", product2.category.name
	}

    @Test
	void validateProducts_shouldCreateNewProductWithNewCategory() {
		def category = Category.findByName("category 123")
		assertNull category
		def row1 = ["1235", "", "Default", "product 1235", "", "category 123", "", "Description", "Unit of Measure", "tag1,tag2", "0.01", null, null, null, null, null, "Manufacturer", "Brand", "ManufacturerCode", "Manufacturer Name", "Vendor", "Vendor Code", "Vendor Name", "UPC", "NDC", "Date Created", "Date Updated"]
		def csv = csvize(Constants.EXPORT_PRODUCT_COLUMNS) + csvize(row1)
        def products = productService.validateProducts(csv)

        // FIXME Hack to keep from running into the following error
        // not-null property references a null or transient value: org.pih.warehouse.product.Product.dateCreated; nested exception is org.hibernate.PropertyValueException: not-null property references a null or transient value: org.pih.warehouse.product.Product.dateCreated
        products[0].dateCreated = new Date()
        products[0].lastUpdated = new Date()

        productService.importProducts(products)
		def product = Product.findByName("product 1235")
		assertNotNull product
		category = Category.findByName("category 123")
		assertNotNull category
	}

    @Test
    void importProducts_shouldCreateNewProductWithExistingCategory() {
		def category = Category.findByName("Medicines")
		assertNotNull category
		def row1 = ["1235", "", "Default", "product 1235", "", "Medicines", "", "Description", "Unit of Measure", "tag1,tag2", "0.01", null, null, null, null, null, "Manufacturer", "Brand", "ManufacturerCode", "Manufacturer Name", "Vendor", "Vendor Code", "Vendor Name", "UPC", "NDC", "Date Created", "Date Updated"]
		def csv = csvize(Constants.EXPORT_PRODUCT_COLUMNS) + csvize(row1)
        def products = productService.validateProducts(csv)

        // FIXME Hack to keep from running into the following error
        // not-null property references a null or transient value: org.pih.warehouse.product.Product.dateCreated; nested exception is org.hibernate.PropertyValueException: not-null property references a null or transient value: org.pih.warehouse.product.Product.dateCreated
        products[0].dateCreated = new Date()
        products[0].lastUpdated = new Date()

        productService.importProducts(products)
		def product = Product.findByName("product 1235")
		assertNotNull product
		assertEquals category, product.category
	}

    @Test
	void importProducts_shouldUpdateNameOnExistingProduct() {
		def productBefore = DbHelper.findOrCreateProduct('Sudafed')
		assertNotNull productBefore.id
		def row1 = ["${productBefore.id}", "AB12", "Default", "Sudafed 2.0", "", "Medicines", "", "Description", "Unit of Measure", "tag1,tag2", "0.01", "LotAndExpiryControl", "ColdChain", "ControlledSubstance", "HazardousMaterial", "Reconditioned", "Manufacturer", "Brand", "ManufacturerCode", "Manufacturer Name", "Vendor", "Vendor Code", "Vendor Name", "UPC", "NDC", "Date Created", "Date Updated"]
		def csv = csvize(Constants.EXPORT_PRODUCT_COLUMNS) + csvize(row1)

        def products = productService.validateProducts(csv)
        productService.importProducts(products)

		def productAfter = Product.get(productBefore.id)
		println ("Get product " + productAfter.name + " " + productAfter.id + " " + productAfter.productCode)
		assertEquals "Sudafed 2.0", productAfter.name
	}

    @Test
	void importProducts_shouldUpdateAllFieldsOnExistingProduct() {
		def productBefore = DbHelper.findOrCreateProduct('Sudafed')
		assertNotNull productBefore.id
		def row1 = [productBefore.id, "AB12", "Default", "Sudafed 2.0", "", "Medicines", "", "It's sudafed, dummy.", "EA", "tag1,tag2", "0.01", null, "true", null, null, null, "Acme", "Brand X", "ACME-249248", "Manufacturer Name", "Vendor", "Vendor Code", "Vendor Name", "UPC-1202323", "NDC-122929-39292", "", ""]
		def csv = csvize(Constants.EXPORT_PRODUCT_COLUMNS) + csvize(row1)

        def products = productService.validateProducts(csv)
		productService.importProducts(products)
		def productAfter = Product.get(productBefore.id)
		println ("Get product " + productAfter.name + " " + productAfter.id + " " + productAfter.productCode)
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

    @Test
    void importProducts_shouldAddExistingTags() {
		def tag1 = new Tag(tag: "tag1").save(flush: true, failOnError: true);
		def tag2 = new Tag(tag: "tag2").save(flush: true, failOnError: true);
		def productBefore = DbHelper.findOrCreateProduct('Sudafed')

        assertNotNull productBefore.id
        assertEquals 0, productBefore?.tags?.size()?:0
		def row1 = ["${productBefore.id}", "AB12", "Default", "Sudafed 2.0", "", "Medicines", "", "It's sudafed, dummy.", "EA", "tag1,tag2", "0.01", null, null, null, null, null, "Acme", "Brand X", "ACME-249248", "Manufacturer Name", "Vendor", "Vendor Code", "Vendor Name", "UPC-1202323", "NDC-122929-39292", "", ""]

        def csv = csvize(Constants.EXPORT_PRODUCT_COLUMNS) + csvize(row1)
        def tags = ["tag1", "tag2"] as List

        def products = productService.validateProducts(csv)
        productService.importProducts(products, tags)

        def productAfter = Product.get(productBefore.id)
        println ("Get product " + productAfter.name + " " + productAfter.id + " " + productAfter.productCode)

        assertEquals productBefore.id, productAfter.id
        assertEquals 2, productAfter?.tags?.size()

    }

    @Test
    void importProducts_shouldAddNewTags() {
        def tag1 = new Tag(tag: "tag1").save(flush: true, failOnError: true);
        def tag2 = new Tag(tag: "tag2").save(flush: true, failOnError: true);

		def product = DbHelper.findOrCreateProduct('Sudafed')

        assertNotNull product.id
        assertEquals 0, product?.tags?.size()?:0

		def row1 = ["${product.id}", "AB12", "Default", "Sudafed 2.0", "", "Medicines", "", "It's sudafed, dummy.", "EA", "tag1,tag2", "0.01", null, null, null, null, null, "Acme", "Brand X", "ACME-249248", "Manufacturer Name", "Vendor", "Vendor Code", "Vendor Name", "UPC-1202323", "NDC-122929-39292", "", ""]
        def csv = csvize(Constants.EXPORT_PRODUCT_COLUMNS) + csvize(row1)
        def tags = ["tag3", "tag4", "tag1", "tag2"] as List

        def products = productService.validateProducts(csv)
        productService.importProducts(products, tags)

        def result = Product.get(product.id)
        println ("Get product " + result.name + " " + result.id + " " + result.productCode)

        assertEquals product.id, result.id
        assertEquals 4, result?.tags?.size()

    }


    @Test
	void getDelimiter_shouldDetectCommaDelimiter() {
		// def row = ["1235","SKU-1","","category 123","Description","Unit of Measure","Manufacture","Brand","ManufacturerCode","Manufacturer Name","Vendor","Vendor Code","Vendor Name","false","UPC","NDC","Date Created","Date Updated"]
		def row1 = ["", "AB12", "Sudafed 2", "Medicines", "", "Sudafed description", "EA", "tag1,tag2", "0.01", "Acme", "Brand X", "ACME-249248", "Vendor Y", "Y-1284", "Sudafed", "UPC-1202323", "NDC-122929-39292", "", ""]
		def csv = csvize(Constants.EXPORT_PRODUCT_COLUMNS) + csvize(row1)
		def delimiter = productService.getDelimiter(csv)
		assertEquals ",", delimiter
	}
    @Test
	void getDelimiter_shouldDetectTabDelimiter() {
		def row1 = ["", "AB12", "Sudafed 2", "Medicines", "", "Sudafed descrition", "each", "tag1,tag2", "0.01", "Acme", "Brand X", "ACME-249248", "Manufacturer Name", "Vendor", "Vendor Code", "Vendor Name", "UPC-1202323", "NDC-122929-39292", "", ""]
		def csv = csvize(Constants.EXPORT_PRODUCT_COLUMNS, "\t") + csvize(row1, "\t")
		def delimiter = productService.getDelimiter(csv)
		assertEquals "\t", delimiter
	}

    @Test
	void getDelimiter_shouldDetectSemiColonDelimiter() {
		def row1 = ["", "00001", "Sudafed 2", "Medicines", "Sudafed description", "each", "tag1,tag2", "0.01", "Acme", "Brand X", "ACME-249248", "Manufacturer Name", "Vendor", "Vendor Code", "Vendor Name", "UPC-1202323", "NDC-122929-39292", "", ""]
		def csv = csvize(Constants.EXPORT_PRODUCT_COLUMNS, ";") + csvize(row1, ";")
		def delimiter = productService.getDelimiter(csv)
		assertEquals ";", delimiter
	}

    @Test
	void findOrCreateCategory_shouldReturnExistingCategory() {
		def categoryName = "Medicines"
		def existingCategory = Category.findByName(categoryName)
		assertNotNull existingCategory
		def category = productService.findOrCreateCategory(categoryName)
		assertEquals existingCategory, category
	}

    @Test
	void findOrCreateCategory_shouldCreateNewCategory() {
		def categoryName = "Nonexistent Category"
		def nonexistentCategory = Category.findByName(categoryName)
		assertNull nonexistentCategory
		def category = productService.findOrCreateCategory(categoryName)
		def existingCategory = Category.findByName(categoryName)
		assertEquals existingCategory, category
	}

    @Test
	void findOrCreateCategory_shouldReturnRootCategoryOnRoot() {
		def categoryName = "ROOT"
		def category = productService.findOrCreateCategory(categoryName)

		println category
		assertEquals category.name, "ROOT"
	}

    @Test
	void findOrCreateCategory_shouldReturnRootCategoryOnEmpty() {
		def categoryName = ""
		def category = productService.findOrCreateCategory(categoryName)
		println category
        assertNotNull category
		assertEquals category.name, "ROOT"
	}

    @Test
	void exportProducts_shouldReturnAllProducts() {
		def csv = productService.exportProducts()
		def lines = csv.split(/[\r\n]/)

		// FIXME Export code appends column delimiter for every column (even the last)
		def expectedHeader = Constants.EXPORT_PRODUCT_COLUMNS.join(",").replace("\n", "") + ","
		def actualHeader = CSVUtils.stripBomIfPresent(lines[0])
		assertNotNull lines
		assertEquals expectedHeader, actualHeader
	}

    @Test
	void exportProducts_shouldRenderProductsAsCsv() {
		def csv = productService.exportProducts()

		println csv
		def lines = csv.split(/[\r\n]/)

		// Remove quotes
		def columns = CSVUtils.stripBomIfPresent(lines[0]).replaceAll("\"", "").split(",")
		println columns
		columns.eachWithIndex { String entry, int i ->
			assertEquals Constants.EXPORT_PRODUCT_COLUMNS[i], entry
		}
	}

    @Test
	void getExistingProducts() {
		def product1 = DbHelper.findOrCreateProduct('Sudafed')
		def product2 = DbHelper.findOrCreateProduct('Advil')
		assertNotNull product1.id
		def row1 = ["${product1.id}", "", "Sudafed", "Medicines", "", "", "", "", "false", "", "", "", ""]
		def row2 = ["${product2.id}", "", "Advil", "Medicines", "", "", "", "", "", "false", "", "", "", ""]
		def csv = csvize(Constants.EXPORT_PRODUCT_COLUMNS) + csvize(row1) + csvize(row2)

		def existingProducts = productService.getExistingProducts(csv)
		assertEquals 2, existingProducts.size()
		assertEquals "Sudafed", existingProducts[0].name
		assertEquals "Advil", existingProducts[1].name
	}

    @Test
	void getExistingProducts_shouldReturnAdvil() {
		def product = DbHelper.findOrCreateProduct('Advil')
		assertNotNull product.id
		def row1 = ["", "", "Sudafed", "Medicines", "", "", "", "", "false", "", "", "", ""]
		def row2 = ["${product.id}", "", "Advil", "Medicines", "", "", "", "", "false", "", "", "", ""]
		def csv = csvize(Constants.EXPORT_PRODUCT_COLUMNS) + csvize(row1) + csvize(row2)
		def existingProducts = productService.getExistingProducts(csv)
		assertEquals 1, existingProducts.size()
		assertEquals "Advil", existingProducts[0].name
	}

    @Test
	void getAllTagLabels() {
		def service = new ProductService();
		def tags = service.getAllTagLabels()
		println tags
		assertEquals 6, tags.size()
	}

    @Ignore
	void getPopularTags() {
		def popularTagMap = productService.getPopularTags()
		def expectedTags = ["favorite", "fever reducer", "nsaid", "pain", "pain reliever"]

		println popularTagMap
		assertNotNull popularTagMap
		assertEquals 5, popularTagMap.keySet().size()
		def actualTags = popularTagMap.keySet().collect { it.tag }
		assertEquals expectedTags, actualTags
		assertEquals 1, popularTagMap[Tag.findByTag("fever reducer")]
		assertEquals 2, popularTagMap[Tag.findByTag("nsaid")]
		assertEquals 3, popularTagMap[Tag.findByTag("pain")]
		assertEquals 2, popularTagMap[Tag.findByTag("pain reliever")]
	}

    @Test
    void getAllCategories() {
        def categories = Category.list()
        assertNotNull categories
        categories.each {
            println it.id + ":" + it?.name + ":" + it?.parentCategory?.name
        }
    }


    @Test
    void getRootCategory() {
        def rootCategory = productService.getRootCategory()
        assertNotNull rootCategory
        assertEquals rootCategory.name, "ROOT"
        assertTrue rootCategory.isRoot
        assertTrue rootCategory.isRootCategory()
    }

	// FIXME the data counted here are not created in this file
	@Ignore
	void getTopLevelCategories() {
		def topLevelCategories = productService.getTopLevelCategories()
		println topLevelCategories
		assertNotNull topLevelCategories
		assertEquals 5, topLevelCategories.size()
	}

    @Test
	void addTagsToProduct_shouldAddTagToProduct() {
		def product = Product.findByName("Ibuprofen 200mg tablet")
		assertNotNull product
		productService.addTagsToProduct(product, ["awesome", "super"])
		println product.tags*.tag
		assertEquals product.tagsToString(), "awesome,favorite,nsaid,pain,super"
	}

    @Test
    void addTagsToProduct_shouldNotAddSameTagMoreThanOnce() {
        def product = Product.findByName("Ibuprofen 200mg tablet")
        assertNotNull product
        productService.addTagsToProduct(product, ["awesome", "super", "awesome"])
        println product.tags*.tag
        assertEquals product.tagsToString(), "awesome,favorite,nsaid,pain,super"
    }

    @Test
    void addTagsToProducts_shouldAddEachTagToAllProducts() {
        def products = []
        def tags = ["newtag1", "newtag2"]
        def ibuprofen = Product.findByName("Ibuprofen 200mg tablet")
        def acetaminophen = Product.findByName("Acetaminophen 325mg tablet")
        products << ibuprofen
        products << acetaminophen

        productService.addTagsToProducts(products, tags)

        assertNotNull Tag.findByTag("newtag1")
        assertNotNull Tag.findByTag("newtag2")

        assert ibuprofen.tagsToString().contains("newtag1")
        assert ibuprofen.tagsToString().contains("newtag2")
        assert acetaminophen.tagsToString().contains("newtag1")
        assert acetaminophen.tagsToString().contains("newtag2")

    }

    @Test
    void addTagsToProducts_shouldNotAddTagMoreThanOnce() {
        def products = []
        def tags = ["sametag", "sametag"]
        def ibuprofen = Product.findByName("Ibuprofen 200mg tablet")
        def acetaminophen = Product.findByName("Acetaminophen 325mg tablet")
        products << ibuprofen
        products << acetaminophen

        productService.addTagsToProducts(products, tags)

        // Check to make sure the tag was created
        def tag = Tag.findByTag("sametag")
        assertNotNull tag

        // Should have only created the tag once
        def sameTags = Tag.findAllByTag("sametag")
        assertEquals 1, sameTags.size()

        // Check that both products have one instance of the tag
        assert ibuprofen.tagsToString().contains("sametag")
        assert acetaminophen.tagsToString().contains("sametag")

        // Check that the tag has only been added to the products once
        def occurrences = ibuprofen.tags.findIndexValues { it == tag }
        assertEquals 1, occurrences.size()

        occurrences = acetaminophen.tags.findIndexValues { it == tag }
        assertEquals 1, occurrences.size()
    }

    @Test
	void deleteTag_shouldDeleteTagFromDatabase() {
		def product = Product.findByName("Ibuprofen 200mg tablet")
		assertNotNull product
		assertEquals product.tagsToString(), "favorite,nsaid,pain"
		Tag tag = Tag.findByTag("favorite")
		assertNotNull tag
		productService.deleteTag(product, tag)
		assertEquals "nsaid,pain", product.tagsToString()
		Tag tag2 = Tag.findByTag("favorite")
		assertNull tag2
	}

    @Test
    void generateProductIdentifier_shouldGenerateUniqueIdentifiers() {

        for (int i = 0; i<100; i++) {

            println productService.generateProductIdentifier()
        }
    }


	@Test
	void saveProduct_failOnValidationError() {
        def product = new Product();
        def returnValue = productService.saveProduct(product)
        assertNull returnValue
        assertEquals 3, product.errors.getErrorCount()
        assertNotNull product.errors.getFieldError("name")
        assertNotNull product.errors.getFieldError("category")
        assertNotNull product.errors.getFieldError("productType")
        assertNull product.errors.getFieldError("description")
    }

    @Test
    void saveProduct_shouldSaveProduct() {
        def product = new Product();
		def productType = DbHelper.findOrCreateProductType("Default")
        product.name = "Test product"
        product.category = Category.getRootCategory()
		product.productType = productType
        def returnValue = productService.saveProduct(product)
        println returnValue
        assertNotNull returnValue
        assertEquals product, returnValue
        assertEquals returnValue.category, Category.getRootCategory()
        assertNotNull product.productCode
    }

    @Test
    void saveProduct_shouldSaveProductAndTags() {
        def product = new Product();
		def productType = DbHelper.findOrCreateProductType("Default")
        product.name = "Test product"
        product.category = Category.getRootCategory()
		product.productType = productType

        def returnValue = productService.saveProduct(product, "a tag,the next tag,another tag")
        assertNotNull returnValue
        assertNotNull product.id
        assertNotNull product.tags
        assertEquals 3, product.tags.size()
    }

    @Test
    void saveProduct_shouldFailOnInvalidCategory() {
        def product = new Product();
		def productType = DbHelper.findOrCreateProductType("Default")
		product.productType = productType
        productService.saveProduct(product)
        println product.errors
        assertNotNull product.errors.getFieldError("category")
    }


    @Test
    void saveProduct_shouldGenerateUniqueProductCode() {
        def product = new Product();
		def productType = DbHelper.findOrCreateProductType("Default")
        product.name = "New product"
        product.category = Category.getRootCategory()
		product.productType = productType
        productService.saveProduct(product)
        assertNotNull product
        assertNotNull product.productCode
    }

    @Test
    void saveProduct_shouldFailOnDuplicateProductCode() {
        def product = Product.findByProductCode("AB13")
		def productType = DbHelper.findOrCreateProductType("Default")
        assertNotNull product

        product = new Product();
        product.name = "New product"
        product.productCode = "AB13"
        product.category = Category.getRootCategory()
		product.productType = productType
        def returnValue = productService.saveProduct(product)
        println product.errors
        assertNull returnValue
        assertNotNull product.errors.getFieldError("productCode")


    }

    @Test
    void validateProductIdentifier_shouldReturnTrueOnUnique() {
        // Ensuring that product does not exist
        def product = Product.findByProductCode("ZZZZ")
        assertNull product
        assertTrue productService.validateProductIdentifier("ZZZZ")
    }

    @Test
    void validateProductIdentifier_shouldReturnFalseOnDuplicate() {
        // Ensuring that product does exist
        def product = Product.findByProductCode("AB13")
        assertNotNull product
        assertFalse productService.validateProductIdentifier("AB13")
    }

    @Test
    void validateProductIdentifier_shouldReturnFalseOnEmpty() {
        assertFalse productService.validateProductIdentifier("");
    }

    @Test
    void validateProductIdentifier_shouldReturnFalseOnNull() {
        assertFalse productService.validateProductIdentifier(null);
    }


    @Test
    void findOrCreateTag_shouldCreateTagSuccessfully() {
        def tag1 = Tag.findByTag("brand new tag")
        assertNull tag1
        def tag2 = productService.findOrCreateTag("brand new tag")
        assertNotNull tag2

    }

    @Test
    void findOrCreateTag_shouldFindExistingTag() {
        def tag1 = Tag.findByTag("favorite")
        assertNotNull tag1
        assertEquals 1, Tag.findAllByTag("favorite").size()

        def tag2 = productService.findOrCreateTag("favorite")
        assertNotNull tag2
        assertEquals tag1.id, tag2.id
        assertEquals tag1, tag2

        // Make sure there's still only one "favorite" tag
        assertEquals 1, Tag.findAllByTag("favorite").size()
    }

}
