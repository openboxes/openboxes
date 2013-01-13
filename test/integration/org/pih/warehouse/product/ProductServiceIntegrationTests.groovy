package org.pih.warehouse.product

import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.Tag;
import org.pih.warehouse.core.User;

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
	protected void setUp(){
		product1 = DbHelper.createProductWithGroups("boo floweree 250mg",[
			"Hoo moodiccina",
			"Boo floweree"
		])
		product2 = DbHelper.createProductWithGroups("boo pill",["Boo floweree"])
		product3 = DbHelper.createProductWithGroups("foo",["Hoo moodiccina"])
		product4 = DbHelper.createProductWithGroups("abc tellon",["Hoo moodiccina"])
		product5 = DbHelper.createProductWithGroups("goomoon",["Boo floweree"])
		product6 = DbHelper.createProductWithGroups("buhoo floweree root",[])
		group1 = ProductGroup.findByDescription("Hoo moodiccina")
		group2 = ProductGroup.findByDescription("Boo floweree")
		
		
		DbHelper.createProductWithTags("Ibuprofen 200mg tablet", ["nsaid","pain","favorite"])
		DbHelper.createProductWithTags("Acetaminophen 325mg tablet", ["pain","pain reliever"])
		DbHelper.createProductWithTags("Naproxen 220mg tablet", ["pain reliever","pain","nsaid","fever reducer"])
		DbHelper.createTag("tagwithnoproducts")	
	}

	void test_searchProductAndProductGroup_shouldGetAllProductsUnderMachtedGroups(){
		def result = productService.searchProductAndProductGroup("floweree")
		assert result.size() == 5
		assert result.any{ it[1] == "boo floweree 250mg" && it[2] == "Boo floweree" && it[0] == product1.id && it[3] == group2.id}
		assert result.any{ it[1] == "boo floweree 250mg" && it[2] == "Hoo moodiccina" && it[0] == product1.id && it[3] == group1.id}
		assert result.any{ it[1] == "boo pill" && it[2] == "Boo floweree" && it[0] == product2.id && it[3] == group2.id}
		assert result.any{ it[1] == "goomoon" &&  it[2] == "Boo floweree" && it[0] == product5.id && it[3] == group2.id}
		assert result.any{ it[1] == "buhoo floweree root" &&  it[2] == null && it[0] == product6.id && it[3] == null}
	}

	/*
	void test_import_shouldFailWhenFormatIsInvalid() {		
		def csv = """\"ID\"\t\"Name\"\t\"Category\"\t\"Description\"\t\"Product Code\"\t\"Unit of Measure\"\t\"Manufacturer\"\t\"Manufacturer Code\"\t\"Cold Chain\"\t\"UPC\"\t\"NDC\"\t\"Date Created\"\t\"Date Updated\"\n\"1235\"\t\"product 1235\"\t\"category 123\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"false\"\t\"\"\t\"\"\t\"\"\t\"\""""
		def service = new ProductService()
		def message = shouldFail(RuntimeException) {
			service.importProducts(csv)
		}
		assertEquals("Invalid format", message)		
	}
	*/

	void test_importProducts_shouldFailWhenProductNameIsMissing() {
		def csv = "\"ID\",\"Product Code\",\"Name\",\"Category\",\"Description\",\"Unit of Measure\",\"Manufacturer\",\"Manufacturer Code\",\"Cold Chain\",\"UPC\",\"NDC\",\"Date Created\",\"Date Updated\"\n" + 
			"\"1235\",\"\",\"\",\"category 123\",\"\",\"\",\"\",\"\",\"false\",\"\",\"\",\"\",\"\""

		def message = shouldFail(RuntimeException) {
			productService.importProducts(csv)
		}
		assertEquals("Product name cannot be empty", message)
	}

	void test_importProducts_shouldNotUpdateProductsWhenSaveToDatabaseIsFalse() {
		def product = DbHelper.createProductIfNotExists("Sudafed");
		assertNotNull product.id
		def csv = "\"ID\",\"Product Code\",\"Name\",\"Category\",\"Description\",\"Unit of Measure\",\"Manufacturer\",\"Manufacturer Code\",\"Cold Chain\",\"UPC\",\"NDC\",\"Date Created\",\"Date Updated\"\n" + 
			"\"${product.id}\",\"\",\"Sudafed 2\",\"OTC Medicines\",\"\",\"\",\"\",\"\",\"false\",\"\",\"\",\"\",\"\""
		productService.importProducts(csv, false)		
		def product2 = Product.get(product.id)
		println ("Get product " + product2.name + " " + product2.id + " " + product2.productCode + " " + product.isAttached())		
		assertEquals "Sudafed", product2.name
		assertEquals "Medicines", product2.category.name
	}

	void test_importProducts_shouldCreateNewProductWithNewCategory() {
		def category = Category.findByName("category 123")
		assertNull category
		def csv = "\"ID\",\"Product Code\",\"Name\",\"Category\",\"Description\",\"Unit of Measure\",\"Manufacturer\",\"Manufacturer Code\",\"Cold Chain\",\"UPC\",\"NDC\",\"Date Created\",\"Date Updated\"\n" + 
			"\"1235\",\"\",\"product 1235\",\"category 123\",\"\",\"\",\"\",\"\",\"false\",\"\",\"\",\"\",\"\""
		
		productService.importProducts(csv, true)	
		def product = Product.findByName("product 1235")
		assertNotNull product
		category = Category.findByName("category 123")
		assertNotNull category
	}

	void test_importProducts_shouldCreateNewProductWithExistingCategory() { 
		def category = Category.findByName("Medicines")
		assertNotNull category
		def csv = "\"ID\",\"Product Code\",\"Name\",\"Category\",\"Description\",\"Unit of Measure\",\"Manufacturer\",\"Manufacturer Code\",\"Cold Chain\",\"UPC\",\"NDC\",\"Date Created\",\"Date Updated\"\n" + 
			"\"1235\",\"\",\"product 1235\",\"Medicines\",\"\",\"\",\"\",\"\",\"false\",\"\",\"\",\"\",\"\""		
		productService.importProducts(csv, true)			
		def product = Product.findByName("product 1235")
		assertNotNull product
		assertEquals category, product.category		
	}

	void test_importProducts_shouldUpdateNameOnExistingProduct() {
		def productBefore = DbHelper.createProductIfNotExists("Sudafed");
		assertNotNull productBefore.id
		def csv = "\"ID\",\"Product Code\",\"Name\",\"Category\",\"Description\",\"Unit of Measure\",\"Manufacturer\",\"Manufacturer Code\",\"Cold Chain\",\"UPC\",\"NDC\",\"Date Created\",\"Date Updated\"\n" + 
			"\"${productBefore.id}\",\"AB12\",\"Sudafed 2\",\"Medicines\",\"\",\"\",\"\",\"\",\"false\",\"\",\"\",\"\",\"\""
		productService.importProducts(csv, true)
		
		
		def productAfter = Product.get(productBefore.id)
		println ("Get product " + productAfter.name + " " + productAfter.id + " " + productAfter.productCode)
		assertEquals "Sudafed 2", productAfter.name		
	}
	
	void test_importProducts_shouldUpdateAllFieldsOnExistingProduct() {
		def productBefore = DbHelper.createProductIfNotExists("Sudafed");
		assertNotNull productBefore.id
		def csv = "\"ID\",\"Product Code\",\"Name\",\"Category\",\"Description\",\"Unit of Measure\",\"Manufacturer\",\"Manufacturer Code\",\"Cold Chain\",\"UPC\",\"NDC\",\"Date Created\",\"Date Updated\"\n" + 
			"\"${productBefore.id}\",\"AB12\",\"Sudafed 2\",\"Medicines\",\"Sudafed description\",\"each\",\"Acme\",\"ACME-249248\",\"true\",\"UPC-1202323\",\"NDC-122929-39292\",\"\",\"\""
		productService.importProducts(csv, true)
		def productAfter = Product.get(productBefore.id)
		println ("Get product " + productAfter.name + " " + productAfter.id + " " + productAfter.productCode)
		assertEquals "AB12", productAfter.productCode
		assertEquals "Sudafed 2", productAfter.name	
		assertEquals "Sudafed description", productAfter.description
		assertEquals "Medicines", productAfter.category.name
		assertEquals "each", productAfter.unitOfMeasure
		assertEquals "Acme", productAfter.manufacturer
		assertEquals "ACME-249248", productAfter.manufacturerCode
		assertEquals "UPC-1202323", productAfter.upc
		assertEquals "NDC-122929-39292", productAfter.ndc
		assertTrue productAfter.coldChain		
	}
	
	void test_getDelimiter_shouldDetectCommaDelimiter() { 		
		def csv = "\"ID\",\"Product Code\",\"Name\",\"Category\",\"Description\",\"Unit of Measure\",\"Manufacturer\",\"Manufacturer Code\",\"Cold Chain\",\"UPC\",\"NDC\",\"Date Created\",\"Date Updated\"\n" + 
			"\"\",\"AB12\",\"Sudafed 2\",\"Medicines\",\"Sudafed description\",\"each\",\"Acme\",\"ACME-249248\",\"true\",\"UPC-1202323\",\"NDC-122929-39292\",\"\",\"\""
		def delimiter = productService.getDelimiter(csv)
		assertEquals ",", delimiter
	}

	void test_getDelimiter_shouldDetectTabDelimiter() {		
		def csv = "\"ID\"\t\"Name\"\t\"Category\"\t\"Description\"\t\"Product Code\"\t\"Unit of Measure\"\t\"Manufacturer\"\t\"Manufacturer Code\"\t\"Cold Chain\"\t\"UPC\"\t\"NDC\"\t\"Date Created\"\t\"Date Updated\"\n" + 
			"\"\"\"AB12\"\t\t\"Sudafed 2\"\t\"Medicines\"\t\"Sudafed description\"\t\"each\"\t\"Acme\"\t\"ACME-249248\"\t\"true\"\t\"UPC-1202323\"\t\"NDC-122929-39292\"\t\"\"\t\"\""
		def delimiter = productService.getDelimiter(csv)
		assertEquals "\t", delimiter
	}

	
	void test_getDelimiter_shouldDetectSemiColonDelimiter() {
		def csv = "\"ID\";\"Product Code\";\"Name\";\"Category\";\"Description\";\"Unit of Measure\";\"Manufacturer\";\"Manufacturer Code\";\"Cold Chain\";\"UPC\";\"NDC\";\"Date Created\";\"Date Updated\"\n" +
			"\"\";\"00001\";\"Sudafed 2\";\"Medicines\";\"Sudafed description\";\"each\";\"Acme\";\"ACME-249248\";\"true\";\"UPC-1202323\";\"NDC-122929-39292\";\"\";\"\""
		def delimiter = productService.getDelimiter(csv)
		assertEquals ";", delimiter
	}
	/*
	void test_getDelimiter_shouldFailOnInvalidDelimiter() {
		def csv = """\"ID\"&\"Name\"&\"Category\"&\"Description\"&\"Product Code\"&\"Unit of Measure\"&\"Manufacturer\"&\"Manufacturer Code\"&\"Cold Chain\"&\"UPC\"&\"NDC\"&\"Date Created\"&\"Date Updated\"\n\"${product1.id}\"&\"Sudafed 2\"&\"Medicines\"&\"Sudafed description\"&\"00001\"&\"each\"&\"Acme\"&\"ACME-249248\"&\"true\"&\"UPC-1202323\"&\"NDC-122929-39292\"&\"\"&\"\""""
		def service = new ProductService()
		def message = shouldFail(RuntimeException) {
			service.getDelimiter(csv)
		}
		assertEquals("Invalid format", message)
	}
	*/

		

	/*
	void test_import_shouldFailWhenExistingProductHasChangedOnServer() {		
		// Create a new product 
		def today = new Date()
		def product1 = DbHelper.createProductIfNotExists("Sudafed");
		assertNotNull product1.id
		assertNotNull product1.lastUpdated
		assertEquals "Product should have been updated on server today", product1.lastUpdated.clearTime(), today.clearTime()
				
		// Attempt to import should fail due to the fact that the product's lastUpdated date is after the lastUpdated date in the CSV
		def csv = """\"ID\",\"Name\",\"Category\",\"Description\",\"Product Code\",\"Unit of Measure\",\"Manufacturer\",\"Manufacturer Code\",\"Cold Chain\",\"UPC\",\"NDC\",\"Date Created\",\"Date Updated\"\n\"${product1.id}\",\"Sudafed 2\",\"Medicines\",\"\",\"\",\"\",\"\",\"\",\"false\",\"\",\"\",\"2010-08-25 00:00:00.0\",\"2013-01-01 00:00:00.0\""""
		def service = new ProductService()
		def message = shouldFail(RuntimeException) {
			service.importProducts(csv)
		}		
		assertEquals("Product has been modified on server", message)		
	}
	*/
	
	/*
	void test_import_shouldFailWhenExistingProductCategoryHasChanged() {
		// Create a new product
		def today = new Date()
		def product1 = DbHelper.createProductIfNotExists("Sudafed");
		assertNotNull product1.id
		assertNotNull product1.lastUpdated
		assertEquals "Product should have been updated on server today", product1.lastUpdated.clearTime(), today.clearTime()
				
		// Attempt to import should fail due to the fact that the product's lastUpdated date is after the lastUpdated date in the CSV
		def csv = """\"ID\",\"Name\",\"Category\",\"Description\",\"Product Code\",\"Unit of Measure\",\"Manufacturer\",\"Manufacturer Code\",\"Cold Chain\",\"UPC\",\"NDC\",\"Date Created\",\"Date Updated\"\n\"${product1.id}\",\"Sudafed 2\",\"Supplies\",\"\",\"\",\"\",\"\",\"\",\"false\",\"\",\"\",\"\",\"\""""
		def service = new ProductService()
		def message = shouldFail(RuntimeException) {
			service.importProducts(csv)
		}		
		assertEquals("Product category cannot be modified", message)

		def product2 = Product.findByName("Sudafed")
		assertEquals product2.category.name, "Medicines"				
	}
	*/

	
	void test_findOrCreateCategory_shouldReturnExistingCategory() { 
		def categoryName = "Medicines"
		def existingCategory = Category.findByName(categoryName)		
		assertNotNull existingCategory
		def category = productService.findOrCreateCategory(categoryName)
		assertEquals existingCategory, category
	}

	void test_findOrCreateCategory_shouldCreateNewCategory() {
		def categoryName = "Nonexistent Category"
		def nonexistentCategory = Category.findByName(categoryName)
		assertNull nonexistentCategory
		def category = productService.findOrCreateCategory(categoryName)
		def existingCategory = Category.findByName(categoryName)
		assertEquals existingCategory, category
	}

	void test_findOrCreateCategory_shouldReturnRootCategoryOnRoot() {
		def categoryName = "ROOT"
		def category = productService.findOrCreateCategory(categoryName)
		assertEquals category.name, "ROOT"
	}

	void test_findOrCreateCategory_shouldReturnRootCategoryOnEmpty() {
		def categoryName = ""
		def category = productService.findOrCreateCategory(categoryName)
		assertEquals category.name, "ROOT"
	}

	
	void test_exportProducts_shouldReturnAllProducts() { 
		
		def csv = productService.exportProducts()		
		println csv
		def lines = csv.split("\n")
		assertEquals 36, lines.size()
	}
	
	
	void test_exportProducts_shouldRenderProductsAsCsv() { 
		def csv = productService.exportProducts()
		
		println csv
		def lines = csv.split("\n")
		def columns = lines[0].replaceAll( "\"", "" ).split(",")
		
		println columns
		assertEquals "ID", columns[0]
		assertEquals "Product Code", columns[1]
		assertEquals "Name", columns[2]
		assertEquals "Category", columns[3]
		assertEquals "Description", columns[4]		
		assertEquals "Unit of Measure", columns[5]
		assertEquals "Manufacturer", columns[6]
		assertEquals "Manufacturer Code", columns[7]
		assertEquals "Cold Chain", columns[8]
		assertEquals "UPC", columns[9]
		assertEquals "NDC", columns[10]
		assertEquals "Date Created", columns[11]
		assertEquals "Date Updated", columns[12]
	}

	void test_getExistingProducts() { 
		def product1 = DbHelper.createProductIfNotExists("Sudafed");
		def product2 = DbHelper.createProductIfNotExists("Advil");
		
		assertNotNull product1.id
		def csv = "\"ID\",\"Product Code\",\"Name\",\"Category\",\"Description\",\"Unit of Measure\",\"Manufacturer\",\"Manufacturer Code\",\"Cold Chain\",\"UPC\",\"NDC\",\"Date Created\",\"Date Updated\"\n" +
			"\"${product1.id}\",\"\",\"Sudafed\",\"Medicines\",\"\",\"\",\"\",\"\",\"false\",\"\",\"\",\"\",\"\"\n" +
			"\"${product2.id}\",\"\",\"Advil\",\"Medicines\",\"\",\"\",\"\",\"\",\"\",\"false\",\"\",\"\",\"\",\"\""		
						
		def existingProducts = productService.getExistingProducts(csv)
		assertEquals 2, existingProducts.size()
		assertEquals "Sudafed", existingProducts[0].name
		assertEquals "Advil", existingProducts[1].name		
	}
	
	void test_getExistingProducts_shouldReturnAdvil() {
		def product = DbHelper.createProductIfNotExists("Advil");		
		assertNotNull product.id
		def csv = "\"ID\",\"Product Code\",\"Name\",\"Category\",\"Description\",\"Unit of Measure\",\"Manufacturer\",\"Manufacturer Code\",\"Cold Chain\",\"UPC\",\"NDC\",\"Date Created\",\"Date Updated\"\n" +
			"\"\",\"\",\"Sudafed\",\"Medicines\",\"\",\"\",\"\",\"\",\"false\",\"\",\"\",\"\",\"\"\n" +
			"\"${product.id}\",\"\",\"Advil\",\"Medicines\",\"\",\"\",\"\",\"\",\"false\",\"\",\"\",\"\",\"\""
			
		def existingProducts = productService.getExistingProducts(csv)
		assertEquals 1, existingProducts.size()
		assertEquals "Advil", existingProducts[0].name
	}
	
	
	void test_getAllTags() { 
		def service = new ProductService();
		def tags = service.getAllTags()
		println tags
		assertEquals 6, tags.size()
	}
	
	void test_getPopularTags() { 		
		//def service = new ProductService();
		
		def popularTagMap = productService.getPopularTags()
		
		def expectedProduct = Product.findByName("Ibuprofen 200mg tablet")
		def expectedTags = ["favorite", "fever reducer", "nsaid", "pain", "pain reliever"]
		def excludedTags = ["tagwithnoproducts"]
		
		
		println popularTagMap
		assertNotNull popularTagMap
		assertEquals 5, popularTagMap.keySet().size()
		def actualTags = popularTagMap.keySet().collect { it.tag }
		assertEquals expectedTags, actualTags
		assertEquals 1, popularTagMap[Tag.findByTag("fever reducer")]
		assertEquals 2, popularTagMap[Tag.findByTag("nsaid")]
		assertEquals 3, popularTagMap[Tag.findByTag("pain")]
		assertEquals 2, popularTagMap[Tag.findByTag("pain reliever")]
		//assertEquals expectedProduct, popularTagMap["favorite"][0]
		
		
	}
	
	void test_getTopLevelCategories() { 		
		
		def topLevelCategories = productService.getTopLevelCategories()
		println topLevelCategories
		assertNotNull topLevelCategories		
		assertEquals 5, topLevelCategories.size()		
	}		
	
	void test_addTagsToProduct() { 
		def product = Product.findByName("Ibuprofen 200mg tablet")
		assertNotNull product
		
		productService.addTagsToProduct(product, ["awesome", "super"])
		println product.tags*.tag
		
		assertEquals product.tagsToString(), "awesome,favorite,nsaid,pain,super"
		
	}
	
	
	
	void test_deleteTag() { 
		try { 
			def product = Product.findByName("Ibuprofen 200mg tablet")
			assertNotNull product
		
			println product.tags*.tag
			assertEquals product.tagsToString(), "favorite,nsaid,pain"
		
			Tag tag = Tag.findByTag("favorite")
		
			product.removeFromTags(tag)
			tag.delete();
			
			println product.tags*.tag
			assertEquals "nsaid,pain", product.tagsToString()
			
			Tag tag2 = Tag.findBytag("favorite")
			assertNull tag2
		} catch (Exception e) { 
			e.printStackTrace()
		}
	}
	
}
