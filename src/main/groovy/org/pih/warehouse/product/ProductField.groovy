package org.pih.warehouse.product

enum ProductField {
    ACTIVE('active'),
    PRODUCT_CODE('productCode'),
    NAME('name'),
    CATEGORY('category'),
    GL_ACCOUNT('glAccount'),
    UNIT_OF_MEASURE('unitOfMeasure'),
    DESCRIPTION('description'),
    COLD_CHAIN('coldChain'),
    CONTROLLED_SUBSTANCE('controlledSubstance'),
    HAZARDOUS_MATERIAL('hazardousMaterial'),
    RECONDITIONED('reconditioned'),
    SERIALIZED('serialized'),
    LOT_CONTROL('lotControl'),
    LOT_AND_EXPIRY_CONTROL('lotAndExpiryControl'),
    ABC_CLASS('abcClass'),
    UPC('upc'),
    NDC('ndc'),
    TAGS('tags'),
    BRAND_NAME('brandName'),
    MANUFACTURER('manufacturer'),
    MANUFACTURER_CODE('manufacturerCode'),
    MANUFACTURER_NAME('manufacturerName'),
    MODEL_NUMBER('modelNumber'),
    VENDOR('vendor'),
    VENDOR_CODE('vendorCode'),
    VENDOR_NAME('vendorName'),
    PRICE_PER_UNIT('pricePerUnit')

    final String fieldName

    ProductField(String fieldName) {
        this.fieldName = fieldName
    }
}
