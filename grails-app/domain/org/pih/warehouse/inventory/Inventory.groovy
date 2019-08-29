/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.inventory

import org.pih.warehouse.core.Location

class Inventory implements java.io.Serializable {

    String id

    // Core data elements
    Location warehouse        // we could assume that a warehouse has an inventory

    // Auditing
    Date dateCreated
    Date lastUpdated

    // Association mapping
    static belongsTo = [warehouse: Location]
    static hasMany = [configuredProducts: InventoryLevel]

    // Show use warehouse name
    String toString() { return "${warehouse.name}" }

    static mapping = {
        id generator: 'uuid'
        cache true
    }

    // Constraints
    static constraints = {
        warehouse(nullable: false)
    }

}
