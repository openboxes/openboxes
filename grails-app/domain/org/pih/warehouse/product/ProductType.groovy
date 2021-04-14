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

class ProductType {

    String id
    String name
    ProductTypeCode productTypeCode

    String code
    String productIdentifierFormat
    Integer sequenceNumber = 0

    Date dateCreated
    Date lastUpdated

    static hasMany = [supportedActivities: ProductActivityCode, requiredFields: ProductField, displayedFields: ProductField]

    static mapping = {
        id generator: 'uuid'
    }

    static constraints = {
        name(blank: false)
        code(nullable: true, unique: true)
        productTypeCode(nullable: false)
        productIdentifierFormat(nullable: true)
        sequenceNumber(nullable: true)
    }

    static transients = ["nextSequenceNumber"]

    Boolean isFieldDisplayed(ProductField field) {
        if (!displayedFields || displayedFields.isEmpty()) {
            return true
        }

        return displayedFields.contains(field)
    }

    Boolean isAnyFieldDisplayed(List<ProductField> fields) {
        if (!displayedFields || displayedFields.isEmpty()) {
            return true
        }

        return fields?.any{ displayedFields.contains(it) }
    }

    static List listAllBySupportedActivity(List<ProductActivityCode> supportedActivities) {
        return ProductType.findAll(
            "from ProductType pt where (:supportedActivities in elements(pt.supportedActivities))",
            [supportedActivities: supportedActivities*.toString()]
        )
    }

    Integer getNextSequenceNumber() {
        if (!sequenceNumber) {
            return 1
        }
        return sequenceNumber + 1
    }
}
