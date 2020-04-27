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

class UnitOfMeasure implements Serializable {

    def beforeInsert = {
        createdBy = AuthService.currentUser.get()
    }
    def beforeUpdate = {
        updatedBy = AuthService.currentUser.get()
    }

    String id
    String name                    // unit of measure name (cubic millimeters)
    String code                    // abbreviation (e.g. mm3)
    String description            // description of unit of measure (optional)
    UnitOfMeasureClass uomClass

    // Auditing
    Date dateCreated
    Date lastUpdated
    User createdBy
    User updatedBy

    static mapping = {
        id generator: 'uuid'
    }

    static constraints = {
        name(nullable: false, maxSize: 255)
        code(nullable: false, unique: true, maxSize: 255)
        description(nullable: true, maxSize: 255)
        uomClass(nullable: true)
        createdBy(nullable: true)
        updatedBy(nullable: true)
    }

    String toString() { return name }
}
