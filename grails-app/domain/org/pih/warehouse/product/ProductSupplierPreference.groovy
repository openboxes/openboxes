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
import org.pih.warehouse.core.PreferenceType
import org.pih.warehouse.core.User

class ProductSupplierPreference {

    String id

    ProductSupplier productSupplier
    Organization destinationParty
    PreferenceType preferenceType

    String comments

    Date validityStartDate
    Date validityEndDate

    // Auditing fields
    Date dateCreated
    Date lastUpdated
    User createdBy
    User updatedBy

    static mapping = {
        id generator: 'uuid'
    }

    static constraints = {
        productSupplier(nullable: false)
        destinationParty(nullable: true, unique: ['productSupplier'])
        preferenceType(nullable: false)
        comments(nullable: true)
        validityStartDate(nullable: true)
        validityEndDate(nullable: true)
        updatedBy(nullable: true)
        createdBy(nullable: true)
    }

    static PROPERTIES = [
            "Product Source Code"                  : "productSupplier.code",
            "Organization Code"                    : "destinationParty.code",
            "Organization Name"                    : ["property": "destinationParty.name", "defaultValue": "DEFAULT"],
            "Preference Type"                      : "preferenceType.name",
            "Preference Type Validity Start Date"  : ["property": "validityStartDate", "dateFormat": "MM/dd/yyyy"],
            "Preference Type Validity End Date"    : ["property": "validityEndDate", "dateFormat": "MM/dd/yyyy"],
            "Preference Type Comment"              : "comments",
    ]
}
