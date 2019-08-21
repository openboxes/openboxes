/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.fulfillment

import org.pih.warehouse.core.Person
import org.pih.warehouse.requisition.Requisition

class Fulfillment implements Serializable {

    String id

    // Attributes
    FulfillmentStatus status

    Person fulfilledBy                // person whom fulfilled request
    Date dateFulfilled                // the date that the request was fulfilled

    // Audit fields
    Date dateCreated
    Date lastUpdated

    // Bi-directional Associations
    static belongsTo = [requisition: Requisition]

    // One-to-many associations
    static hasMany = [fulfillmentItems: FulfillmentItem]

    static mapping = {
        id generator: 'uuid'
        fulfillmentItems cascade: "all-delete-orphan", sort: "id"
    }

    // Constraints
    static constraints = {
        status(nullable: true)
        requisition(nullable: false)
        fulfilledBy(nullable: true)
        dateFulfilled(nullable: true)
    }
}
