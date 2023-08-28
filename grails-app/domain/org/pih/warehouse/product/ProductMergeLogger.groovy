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

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.User

class ProductMergeLogger implements Serializable {

    def beforeInsert() {
        def currentUser = AuthService.currentUser
        if (currentUser) {
            createdBy = currentUser
            updatedBy = currentUser
        }
    }

    def beforeUpdate() {
        def currentUser = AuthService.currentUser
        if (currentUser) {
            updatedBy = currentUser
        }
    }

    String id
    Product primaryProduct
    Product obsoleteProduct
    String relatedObjectId
    String relatedObjectClassName
    Date dateMerged
    Date dateReverted

    String comments

    User createdBy
    User updatedBy
    Date dateCreated
    Date lastUpdated

    static mapping = {
        id generator: 'uuid'
        version false
    }

    static constraints = {
        primaryProduct(nullable: false)
        obsoleteProduct(nullable: false)
        relatedObjectId(nullable: false, blank: true)
        relatedObjectClassName(nullable: false, blank: true)
        dateMerged(nullable: false)
        dateReverted(nullable: true)
        comments(nullable: true)
        createdBy(nullable: true)
        updatedBy(nullable: true)
    }
}
