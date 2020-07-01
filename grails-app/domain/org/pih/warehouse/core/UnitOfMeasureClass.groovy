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

class UnitOfMeasureClass implements Serializable {

    def beforeInsert = {
        createdBy = AuthService.currentUser.get()
    }
    def beforeUpdate = {
        updatedBy = AuthService.currentUser.get()
    }

    String id
    String name
    String code
    String description
    Boolean active
    UnitOfMeasureType type
    UnitOfMeasure baseUom

    // Auditing
    Date dateCreated
    Date lastUpdated
    User createdBy
    User updatedBy

    static mapping = {
        id generator: 'uuid'
    }

    static constraints = {
        name(nullable: false)
        code(nullable: false, unique: true)
        description(nullable: true)
        active(nullable: true)
        type(nullable: false)
        baseUom(nullable: true)
        createdBy(nullable: true)
        updatedBy(nullable: true)
    }

    String toString() { return name }

}
