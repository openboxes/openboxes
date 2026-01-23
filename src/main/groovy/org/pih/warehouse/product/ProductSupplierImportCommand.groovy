package org.pih.warehouse.product

import grails.validation.Validateable

import org.pih.warehouse.core.RatingTypeCode

class ProductSupplierImportCommand {
    boolean active
    String id
    String code
    String name
    String productCode
    String productName
    String legacyProductCode
    String supplierName
    String supplierCode
    String manufacturerName
    String manufacturerCode
    Integer minOrderQuantity
    BigDecimal contractPricePrice
    Date contractPriceValidUntil
    RatingTypeCode ratingType
    String globalPreferenceTypeName
    Date globalPreferenceTypeValidityStartDate
    Date globalPreferenceTypeValidityEndDate
    String globalPreferenceTypeComments
    String defaultProductPackageUomCode
    Integer defaultProductPackageQuantity
    BigDecimal defaultProductPackagePrice
}
