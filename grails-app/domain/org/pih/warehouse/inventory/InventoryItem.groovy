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
import grails.util.Holders

/**
 * Also known as ProductLot.
 *
 * Represents a "lot" of a product, which is a group of product instances that were all made under the same conditions.
 * As such, the lot number (which uniquely identifies the lot) is typically controlled by the product supplier.
 *
 * While quantity calculations at facilities are based on inventory item, the inventory item itself is not
 * associated with a particular inventory. The same inventory item can be used by multiple facilities.
 * This is why it is clearer to think of inventory item as a product lot. It is tied only to the product itself.
 */
class InventoryItem implements Serializable {

    def publishPersistenceEvent() {
        Holders.grailsApplication.mainContext.publishEvent(new InventorySnapshotEvent(this))
        Holders.grailsApplication.mainContext.publishEvent(new RefreshProductAvailabilityEvent(this))
    }

    def afterUpdate() {
        publishPersistenceEvent()
    }
    def afterDelete() {
        publishPersistenceEvent()
    }


    String id

    Product product                        // Product that we're tracking
    String lotNumber                        // Lot information for a product
    Date expirationDate
    LotStatusCode lotStatus

    String comments

    Integer quantity
    Integer quantityOnHand
    Integer quantityAvailableToPromise

    Boolean disableRefresh = Boolean.FALSE

    // Auditing
    Date dateCreated
    Date lastUpdated

    static transients = ['quantity', 'quantityOnHand', 'quantityAvailableToPromise', 'expirationStatus', 'associatedProducts', 'disableRefresh', 'recalled', 'pickable', 'default']

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
        lotStatus(nullable: true)
    }

    Map toJson() {
        [
                "inventoryItemId": id,
                "productId"      : product?.id,
                "productName"    : product?.name,
                "lotNumber"      : lotNumber ?: null,
                "expirationDate" : expirationDate?.format("MM/dd/yyyy"),
                "quantityOnHand" : quantity ?: 0,
                "quantityATP"    : quantity ?: 0,       //todo: quantity available to promise will coming soon
                "expires"        : expirationStatus,
                "lotStatus"      : lotStatus
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

    def getAssociatedProducts() {
        return [product?.id]
    }

    Boolean isRecalled() {
        return lotStatus == LotStatusCode.RECALLED
    }

    Boolean isPickable() {
        return !recalled
    }

    Boolean getIsDefault() {
        return !lotNumber
    }

}
