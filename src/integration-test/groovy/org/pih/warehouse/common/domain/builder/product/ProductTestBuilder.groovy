package org.pih.warehouse.common.domain.builder.product

import org.pih.warehouse.common.domain.builder.base.TestBuilder
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductType

class ProductTestBuilder extends TestBuilder<Product> {

    @Override
    protected Map<String, Object> getDefaults() {
        return [
                name: "Test Product",
                description: "A product to be used by tests. Can be deleted safely.",
                productType: ProductType.defaultProductType.get(),
                category: Category.getRootCategory(),
                pricePerUnit: 1,
                costPerUnit: 1,
                abcClass: "A",
                unitOfMeasure: "each",
                upc: "012345678905",
                ndc: "11111-111-11",
                manufacturer: "Test Manufacturer",
                manufacturerCode: "TEST-MANU-CODE-123",
                manufacturerName: "Test Product Manufacturer Name",
                brandName: "Test Product Brand Name",
                vendor: "Test Vendor",
                vendorCode: "TEST-VENDOR-CODE-123",
                vendorName: "Test Product Vendor Name",
                color: "red",
        ] as Map<String, Object>
    }

    ProductTestBuilder name(String name) {
        args.name = name
        return this
    }

    /**
     * Randomizes the product name. Useful for guaranteeing a new product.
     */
    ProductTestBuilder randomizedName() {
        args.name = randomUtil.randomStringFieldValue('name')
        return this
    }

    ProductTestBuilder category(Category category) {
        args.category = category
        return this
    }
}
