/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.picklist

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.User
import org.pih.warehouse.requisition.Requisition

/**
 * Represents a plan of action for issuing items from inventory.  The
 * picklist could be generated automatically via an automated algorithm
 * (e.g. FEFO, FIFO) or by hand.
 *
 *
 * @author jmiranda*
 */
class Picklist implements Serializable {

    def beforeInsert = {
        def currentUser = AuthService.currentUser.get()
        if (currentUser) {
            createdBy = currentUser
            updatedBy = currentUser
        }

    }
    def beforeUpdate = {
        def currentUser = AuthService.currentUser.get()
        if (currentUser) {
            updatedBy = currentUser
        }
    }

    String id
    String name
    String description        // a user-defined, searchable name for the order

    Requisition requisition
    Person picker

    Date datePicked

    // Audit fields
    Date dateCreated
    Date lastUpdated
    User createdBy
    User updatedBy

    static belongsTo = [requisition: Requisition]
    static hasMany = [picklistItems: PicklistItem]
    static mapping = {
        id generator: 'uuid'
        picklistItems cascade: "all-delete-orphan", sort: "sortOrder"
    }

    static constraints = {
        name(nullable: true)
        description(nullable: true)
        picker(nullable: true)
        datePicked(nullable: true)
        dateCreated(nullable: true)
        lastUpdated(nullable: true)
        createdBy(nullable: true)
        updatedBy(nullable: true)
    }

    String toString() {
        "id: ${id}, name:${name}"
    }

    Map toJson() {
        [
                id           : id,
                version      : version,
                picklistItems: picklistItems?.collect { it.toJson() }
        ]
    }

}
