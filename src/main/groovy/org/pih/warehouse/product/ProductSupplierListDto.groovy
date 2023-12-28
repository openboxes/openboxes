package org.pih.warehouse.product

class ProductSupplierListDto {

    String id

    String productCode

    // Supplier product name
    String name

    String productName

    // Source code
    String code

    String supplierName

    String supplierCode

    List<ProductSupplierPreferenceListDto> productSupplierPreferences

    String packageSize

    BigDecimal eachPrice

    Date dateCreated

    Boolean active
}
