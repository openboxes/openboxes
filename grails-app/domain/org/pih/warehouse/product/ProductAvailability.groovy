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

import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem

class ProductAvailability {

    String id

    // Foreign keys
    Product product
    Location location
    Location binLocation
    InventoryItem inventoryItem

    // Unique constraint
    String productCode
    String lotNumber
    String binLocationName

    // Quantities
    Integer quantityOnHand
    Integer quantityAllocated
    Integer quantityOnHold
    Integer quantityAvailableToPromise
    Integer quantityNotPicked

    // Auditing
    Date dateCreated
    Date lastUpdated

    static mapping = {
        id generator: "assigned"
        quantityNotPicked formula: "quantity_on_hand - quantity_allocated"
    }

    static transients = ["pickable", "recalled"]

    static constraints = {
        product(nullable:false)
        location(nullable:false)
        binLocation(nullable:true)
        inventoryItem(nullable:false)
        quantityOnHand(nullable:false)
        quantityAllocated(nullable: true)
        quantityOnHold(nullable: true)
        quantityAvailableToPromise(nullable: true)
        quantityNotPicked(nullable: true)
    }

    Boolean isPickable() {
        return (inventoryItem ? inventoryItem.pickable : true) && (binLocation ? binLocation.pickable : true)
    }

    @Override
    boolean equals(o) {
        if (this.is(o)) {
            return true
        }
        if (getClass() != o.class) {
            return false
        }

        ProductAvailability that = (ProductAvailability) o

        if (binLocation != that.binLocation) {
            return false
        }
        if (binLocationName != that.binLocationName) {
            return false
        }
        if (dateCreated != that.dateCreated) {
            return false
        }
        if (id != that.id) {
            return false
        }
        if (inventoryItem != that.inventoryItem) {
            return false
        }
        if (lastUpdated != that.lastUpdated) {
            return false
        }
        if (location != that.location) {
            return false
        }
        if (lotNumber != that.lotNumber) {
            return false
        }
        if (product != that.product) {
            return false
        }
        if (productCode != that.productCode) {
            return false
        }
        if (quantityAllocated != that.quantityAllocated) {
            return false
        }
        if (quantityAvailableToPromise != that.quantityAvailableToPromise) {
            return false
        }
        if (quantityNotPicked != that.quantityNotPicked) {
            return false
        }
        if (quantityOnHand != that.quantityOnHand) {
            return false
        }
        if (quantityOnHold != that.quantityOnHold) {
            return false
        }
        if (version != that.version) {
            return false
        }
        return true
    }

    @Override
    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (product != null ? product.hashCode() : 0)
        result = 31 * result + (location != null ? location.hashCode() : 0)
        result = 31 * result + (binLocation != null ? binLocation.hashCode() : 0)
        result = 31 * result + (inventoryItem != null ? inventoryItem.hashCode() : 0)
        result = 31 * result + (productCode != null ? productCode.hashCode() : 0)
        result = 31 * result + (lotNumber != null ? lotNumber.hashCode() : 0)
        result = 31 * result + (binLocationName != null ? binLocationName.hashCode() : 0)
        result = 31 * result + (quantityOnHand != null ? quantityOnHand.hashCode() : 0)
        result = 31 * result + (quantityAllocated != null ? quantityAllocated.hashCode() : 0)
        result = 31 * result + (quantityOnHold != null ? quantityOnHold.hashCode() : 0)
        result = 31 * result + (quantityAvailableToPromise != null ? quantityAvailableToPromise.hashCode() : 0)
        result = 31 * result + (quantityNotPicked != null ? quantityNotPicked.hashCode() : 0)
        result = 31 * result + (dateCreated != null ? dateCreated.hashCode() : 0)
        result = 31 * result + (lastUpdated != null ? lastUpdated.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        return result
    }
}
