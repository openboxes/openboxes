/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.product

import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.PreferenceTypeCode
import org.pih.warehouse.core.RatingTypeCode
import org.pih.warehouse.core.UnitOfMeasure

class ProductSupplier implements Serializable, Comparable<ProductSupplier> {

    String id

    // Unique identifier for product supplier combination
    String code

    String name

    String description

    // Product code of the original product used to derive this
    String productCode

    // The generic product
    Product product

    // National drug code - http://en.wikipedia.org/wiki/National_Drug_Code
    String ndc
    String upc

    // Manufacturer
    Organization manufacturer
    String manufacturerCode // Manufacturer's product code (e.g. catalog code)
    String manufacturerName // Manufacturer's product name
    String brandName        // Manufacturer's brand name
    String modelNumber      // Manufacturer's model number

    // Supplier
    Organization supplier
    String supplierCode        // Supplier's product code
    String supplierName        // Supplier's alternative product name

    // Indicates whether the supplier product is preferred
    PreferenceTypeCode preferenceTypeCode

    // Rating assigned to the supplier product
    RatingTypeCode ratingTypeCode

    // Number of days between order and delivery
    BigDecimal standardLeadTimeDays

    // Minimum required to order
    BigDecimal minOrderQuantity

    // Additional comments
    String comments

    // Auditing fields
    Date dateCreated
    Date lastUpdated

    static transients = ["defaultProductPackage"]

    static hasMany = [productPackages:ProductPackage]

    static mapping = {
        description type: 'text'
    }

    static constraints = {

        code(nullable: true)
        name(nullable: false)
        description(nullable: true)
        product(nullable: false)
        productCode(nullable: true)
        ndc(nullable: true, maxSize: 255)
        upc(nullable: true, maxSize: 255)

        supplier(nullable: true)
        supplierCode(nullable: true, maxSize: 255)
        supplierName(nullable: true, maxSize: 255)

        modelNumber(nullable: true, maxSize: 255)
        brandName(nullable: true, maxSize: 255)
        manufacturer(nullable: true, maxSize: 255)
        manufacturerCode(nullable: true, maxSize: 255)
        manufacturerName(nullable: true, maxSize: 255)

        standardLeadTimeDays(nullable: true)
        minOrderQuantity(nullable: true)
        ratingTypeCode(nullable: true)
        preferenceTypeCode(nullable: true)
        comments(nullable: true)

    }

    ProductPackage getDefaultProductPackage() {
        return productPackages ? productPackages.toArray()[0] : null
    }


    int compareTo(ProductSupplier obj) {
        return preferenceTypeCode <=> obj.preferenceTypeCode ?:
                ratingTypeCode <=> obj.ratingTypeCode ?:
                        dateCreated <=> obj.dateCreated ?:
                                id <=> obj.id
    }

    static PROPERTIES = [
            "id"                     : "id",
            "code"                   : "code",
            "productCode"            : "product.productCode",
            "legacyProductCode"      : "productCode",
            "productName"            : "name",
            "description"            : "description",
            "supplierId"             : "supplier.id",
            "supplierName"           : "supplier.name",
            "supplierCode"           : "supplierCode",
            "supplierProductName"    : "supplierName",
            "manufacturerId"         : "manufacturer.id",
            "manufacturerName"       : "manufacturer.name",
            "manufacturerCode"       : "manufacturerCode",
            "manufacturerProductName": "manufacturerName",
            "standardLeadTimeDays"   : "standardLeadTimeDays",
            "preferenceTypeCode"     : "preferenceTypeCode",
            "ratingTypeCode"         : "ratingTypeCode",
            "comments"               : "comments"
    ]
}
