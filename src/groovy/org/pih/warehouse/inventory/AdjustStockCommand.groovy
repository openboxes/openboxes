/**
 * Copyright (c) 2016 OpenBoxes. All rights reserved.
 *
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 *
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.inventory

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Product

class AdjustStockCommand {

    String reasonCode
    String comments
    Location location
    Inventory inventory
    InventoryItem inventoryItem
    Product product
    Integer oldQuantity
    Integer newQuantity
    User adjustedBy
    Date adjustedOn

    static constraints = {

        //adjustedBy(nullable:false)
        //adjustedOn(nullable:false)
        product(nullable:false)
        inventory(nullable:false)
        inventoryItem(nullable:false)
        reasonCode(blank:false)
        comments(nullable:true)
        newQuantity blank: false, validator: { newQuantity, object ->
            if(newQuantity == null) {
                return 'adjustment.newQuantity.required'
            }
            else if (object.oldQuantity == newQuantity) {
                return 'adjustment.newQuantity.notEqual'
            }
            else if (newQuantity < 0) {
                return "adjustment.newQuantity.notNegative"
            }
        }
    }

}
