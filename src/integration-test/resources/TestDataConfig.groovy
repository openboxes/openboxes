import org.pih.warehouse.product.ProductTypeCode
import org.pih.warehouse.common.util.RandomUtil

RandomUtil randomUtil = new RandomUtil()

testDataConfig {

    /**
     * The build-test-data plugin isn't very smart by default. When you generate a new domain instance it uses the field
     * name as the value, and will leave any nullable fields blank. If we want our test data to be more complete and
     * representative of real data, we need to configure each of our domains here. We make sure to generate a random
     * value for any unique fields to avoid causing unique constraint violations in tests.
     *
     * https://longwa.github.io/build-test-data/index#testdataconfig
     */
    sampleData {

        'org.pih.warehouse.product.Category' {
            name = "Test Category"
            description = "A category to be used by tests. Can be deleted safely."
        }

        'org.pih.warehouse.product.ProductType' {
            // Unique Fields
            name = randomUtil.randomStringFieldValue("name")
            code = randomUtil.randomStringFieldValue("code")

            productTypeCode = ProductTypeCode.GOOD
            productIdentifierFormat = ""
        }

        'org.pih.warehouse.product.ProductAvailability' {
            // Unique Fields
            id = randomUtil.randomStringFieldValue("id")

            productCode = "TEST-SKU-123"
            lotNumber = "1"
            quantityOnHand = 10
            quantityAllocated = 0
            quantityOnHold = 0
        }

        'org.pih.warehouse.product.Product' {
            // Unique Fields
            productCode = randomUtil.randomStringFieldValue("productCode")

            name = "Test Product"
            description = "A product to be used by tests. Can be deleted safely."
            pricePerUnit = 1
            costPerUnit = 1
            abcClass = ""
            unitOfMeasure = "each"
            upc = "012345678905"
            ndc = "11111-111-11"
            manufacturer = "Test Manufacturer"
            manufacturerCode = "TEST-MANU-CODE-123"
            manufacturerName = "Test Product Manufacturer Name"
            brandName = "Test Product Brand Name"
            vendor = "Test Vendor"
            vendorCode = "TEST-VENDOR-CODE-123"
            vendorName = "Test Product Vendor Name"
            color = "red"
        }
    }
}