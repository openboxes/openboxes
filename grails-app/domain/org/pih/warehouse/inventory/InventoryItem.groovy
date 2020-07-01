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

import org.pih.warehouse.product.Product

/**
 * Represents an instance of a product, referenced by lot number
 *
 * Note that an inventory item does not directly reference an inventory,
 * and in fact a single inventory item may be tied to multiple inventories
 * at the same time (if a lot is split between multiple warehouses)
 *
 * Transaction Entries are tied to Inventory Items, and
 * these entries are used to calculate the quantity levels of inventory items
 *
 * We may rename InventoryItem to ProductInstance, as this may
 * be a clearer name
 */
class InventoryItem implements Serializable {

    def publishPersistenceEvent = {
        publishEvent(new InventorySnapshotEvent(this))
    }

    def afterInsert = publishPersistenceEvent
    def afterUpdate = publishPersistenceEvent
    def afterDelete = publishPersistenceEvent


    String id

    Product product                        // Product that we're tracking
    String lotNumber                        // Lot information for a product
    Date expirationDate

    String comments

    Integer quantity
    Integer quantityOnHand
    Integer quantityAvailableToPromise

    // Auditing
    Date dateCreated
    Date lastUpdated

    static transients = ['quantity', 'quantityOnHand', 'quantityAvailableToPromise', 'expirationStatus']

    static belongsTo = [product: Product]

    static mapping = {
        id generator: 'uuid'
        cache true
    }

    // Notice the unique constraint on lotNumber/product
    static constraints = {
        product(nullable: false)
        lotNumber(nullable: true, unique: ['product'], maxSize: 255)
        expirationDate(shared:"expirationDateConstraint")
        comments(nullable: true)
    }

    Map toJson() {
        [
                "inventoryItemId": id,
                "productId"      : product?.id,
                "productName"    : product?.name,
                "lotNumber"      : lotNumber,
                "expirationDate" : expirationDate?.format("MM/dd/yyyy"),
                "quantityOnHand" : quantity ?: 0,
                "quantityATP"    : quantity ?: 0,       //todo: quantity available to promise will coming soon
                "expires"        : expirationStatus
        ]
    }

    @Override
    String toString() { return "${product?.productCode}:${lotNumber}:${expirationDate}" }

    @Override
    int hashCode() {
        if (this.id != null) {
            return this.id.hashCode()
        }
        return super.hashCode()
    }

    @Override
    boolean equals(Object o) {
        if (o instanceof InventoryItem) {
            InventoryItem that = (InventoryItem) o
            return this.id == that.id
        }
        return false
    }

    def getExpirationStatus() {
        def today = new Date()
        if (expirationDate) {
            def daysToExpiry = expirationDate - today
            if (daysToExpiry <= 0) {
                return "expired"
            } else if (daysToExpiry <= 30) {
                return "within30Days"
            } else if (daysToExpiry <= 60) {
                return "within60Days"
            } else if (daysToExpiry <= 90) {
                return "within90Days"
            } else if (daysToExpiry <= 180) {
                return "within180Days"
            } else if (daysToExpiry <= 365) {
                return "within365Days"
            } else if (daysToExpiry > 365) {
                return "greaterThan365Days"
            }
        }
        return "never"
    }

}
