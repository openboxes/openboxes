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

import org.codehaus.groovy.grails.validation.Validateable
import org.pih.warehouse.core.Location

@Validateable
class RecordInventoryRowCommand {
    String id
    String lotNumber
    Location binLocation
    InventoryItem inventoryItem
    Date expirationDate
    String description
    Integer oldQuantity
    Integer newQuantity
    String comment

    static constraints = {
        id(nullable: true)
        expirationDate(nullable: true)
        binLocation(nullable: true)
        inventoryItem(nullable: true)
        lotNumber(nullable: true)
        description(nullable: true)
        oldQuantity(nullable: false)
        newQuantity(nullable: false)
        comment(nullable: true)
    }

}