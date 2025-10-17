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

import grails.databinding.BindUsing

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.ProductPrice
import org.pih.warehouse.core.RatingTypeCode

import java.math.RoundingMode

import org.pih.warehouse.core.User

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

    // Rating assigned to the supplier product
    RatingTypeCode ratingTypeCode

    // Number of days between order and delivery
    BigDecimal standardLeadTimeDays

    // Minimum required to order
    BigDecimal minOrderQuantity

    ProductPrice contractPrice

    // Additional comments
    String comments

    Boolean tieredPricing = false

    // Auditing fields
    Date dateCreated
    Date lastUpdated
    User createdBy
    User updatedBy

    @BindUsing({ productSupplier, source -> source["active"] != null ? source["active"] : true })
    Boolean active = Boolean.TRUE

    ProductPackage defaultProductPackage


    static transients = ["defaultProductPackageDerived", "globalProductSupplierPreference", "attributes"]

    static hasMany = [productPackages: ProductPackage, productSupplierPreferences: ProductSupplierPreference]

    static mapping = {
        description type: 'text'
    }

    def beforeInsert() {
        createdBy = AuthService.currentUser
        updatedBy = AuthService.currentUser
    }

    def beforeUpdate() {
        updatedBy = AuthService.currentUser
    }

    static constraints = {
        defaultProductPackage(nullable: true)
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
        comments(nullable: true)

        contractPrice(nullable: true)
        active(nullable: true)

        // These are nullable to support pre-existing data from before the columns were added.
        createdBy(nullable: true)
        updatedBy(nullable: true)
    }

    ProductPackage getDefaultProductPackageDerived() {
        return productPackages ? productPackages.sort { it.lastUpdated }.last() : null
    }

    List<ProductAttribute> getAttributes() {
        return product.attributes.findAll {
            ProductAttribute productAttribute -> productAttribute.productSupplier == this
        }
    }

    ProductSupplierPreference getGlobalProductSupplierPreference() {
        return productSupplierPreferences ? productSupplierPreferences.find { it.destinationParty == null } : null
    }

    int compareTo(ProductSupplier obj) {
        return ratingTypeCode <=> obj.ratingTypeCode ?:
                dateCreated <=> obj.dateCreated ?:
                        id <=> obj.id
    }

    String getPackageSize() {
        // First we check for a default package assigned to 1:1 association
        if (defaultProductPackage) {
            return "${defaultProductPackage?.uom?.code}/${defaultProductPackage?.quantity}"
        }
        // If a default package of 1:1 association is not found, check for a default package "in an old way" (last updated)
        if (defaultProductPackageDerived) {
            return "${defaultProductPackageDerived?.uom?.code}/${defaultProductPackageDerived?.quantity}"
        }
        return null
    }

    BigDecimal getPackagePrice() {
        // First we check for a default package assigned to 1:1 association
        if (defaultProductPackage) {
            return defaultProductPackage?.productPrice?.price?.setScale(2, RoundingMode.HALF_UP) ?: 0.0
        }
        // If a default package of 1:1 association is not found, check for a default package "in an old way" (last updated)
        if (defaultProductPackageDerived) {
            return defaultProductPackageDerived?.productPrice?.price?.setScale(2, RoundingMode.HALF_UP) ?: 0.0
        }
        return 0.0
    }

    BigDecimal getUnitPrice() {
        // First we check for a default package assigned to 1:1 association
        if (defaultProductPackage) {
            return defaultProductPackage?.productPrice
                    ? (defaultProductPackage?.productPrice?.price / defaultProductPackage?.quantity)?.setScale(2, RoundingMode.HALF_UP)
                    : 0.0
        }
        // If a default package of 1:1 association is not found, check for a default package "in an old way" (last updated)
        if (defaultProductPackageDerived) {
            return defaultProductPackageDerived?.productPrice
                    ? (defaultProductPackageDerived?.productPrice?.price / defaultProductPackageDerived?.quantity)?.setScale(2, RoundingMode.HALF_UP)
                    : 0.0
        }
        return 0.0
    }

    static PROPERTIES = [
            "active"                              : "active",
            "ID"                                  : "id",
            "Product Source Code"                 : "code",
            "Product Source Name"                 : "name",
            "Product Code"                        : "product.productCode",
            "Product Name"                        : "product.name",
            "Legacy Product Code"                 : "productCode",
            "Supplier Name"                       : "supplier.name",
            "Supplier Item No"                    : "supplierCode",
            "Manufacturer Name"                   : "manufacturer.name",
            "Manufacturer Item No"                : "manufacturerCode",
            "Minimum Order Quantity"              : "minOrderQuantity",
            "Contract Price (Each)"               : "contractPrice.price",
            "Contract Price Valid Until"          : ["property": "contractPrice.toDate", "dateFormat": "MM/dd/yyyy"],
            "Rating Type"                         : "ratingTypeCode",
            "Default Global Preference Type"      : "globalProductSupplierPreference.preferenceType.name",
            "Preference Type Validity Start Date" : ["property": "globalProductSupplierPreference.validityStartDate", "dateFormat": "MM/dd/yyyy"],
            "Preference Type Validity End Date"   : ["property": "globalProductSupplierPreference.validityEndDate", "dateFormat": "MM/dd/yyyy"],
            "Preference Type Comment"             : "globalProductSupplierPreference.comments",
            "Default Package Type"                : "defaultProductPackage.uom.code",
            "Quantity per package"                : "defaultProductPackage.quantity",
            "Package price"                       : "defaultProductPackage.productPrice.price",
            "Date created"                        : ["property": "dateCreated", "dateFormat": "MM/dd/yyyy"],
            "Last updated"                        : ["property": "lastUpdated", "dateFormat": "MM/dd/yyyy"]
    ]

    Map toJson() {
        [
            id: id,
            name: name,
            product: [
                id: product.id,
                name: product.name,
                productCode: product.productCode
            ],
            code: code,
            supplier: supplier ? [
                id: supplier?.id,
                name: supplier?.name,
                code: supplier?.code,
                displayName: supplier?.name
                        ? supplier.name + (supplier?.code ? " (${supplier.code})" : "")
                        : ""
            ] : null,
            supplierCode: supplierCode,
            defaultProductPackage: defaultProductPackage ?: defaultProductPackageDerived,
            contractPrice: contractPrice ? [
                id: contractPrice?.id,
                price: contractPrice?.price,
                validUntil: contractPrice?.toDate,
            ] : null,
            minOrderQuantity: minOrderQuantity,
            productSupplierPreferences: productSupplierPreferences.collect { it.toJson() },
            packageSize: packageSize,
            packagePrice: packagePrice,
            unitPrice: unitPrice,
            dateCreated: dateCreated,
            lastUpdated: lastUpdated,
            createdBy: createdBy ? [
                id: createdBy?.id,
                name: createdBy?.name,
            ] : null,
            updatedBy: updatedBy ? [
                id: updatedBy?.id,
                name: updatedBy?.name,
            ] : null,
            active: active,
            ratingTypeCode: ratingTypeCode?.name,
            manufacturer: manufacturer ? [
                id: manufacturer?.id,
                name: manufacturer?.name,
            ] : null,
            description: description,
            manufacturerCode: manufacturerCode,
            brandName: brandName,
            attributes: attributes,
            tieredPricing: tieredPricing,
            productCode: productCode
        ]
    }
}
