/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.core

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.product.Product
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.product.Product

class Synonym implements Serializable {

    def beforeInsert = {
        createdBy = AuthService.currentUser
    }

    def beforeUpdate = {
        updatedBy = AuthService.currentUser
    }

    String id
    String name
    Locale locale
    SynonymTypeCode synonymTypeCode
    Date dateCreated
    Date lastUpdated
    User createdBy
    User updatedBy

    static belongsTo = [product: Product]

    static mapping = {
        id generator: 'uuid'
    }

    static constraints = {
        name(nullable: false, maxSize: 255)
        locale(nullable: true)
        synonymTypeCode(nullable: false)
        updatedBy(nullable: true)
        createdBy(nullable: true)
    }

    static PROPERTIES = [
            "Product Code"           : "product.productCode",
            "Class of synonym"       : "synonymTypeCode",
            "Locale"                 : "locale",
            "Synonym name"           : "name",
    ]

}
