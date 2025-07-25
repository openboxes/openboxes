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

import grails.util.Holders
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product

class TransactionEntry implements Comparable, Serializable {

    String id
    Integer quantity
    Product product
    InventoryItem inventoryItem        // The inventory item being tracked
    Location binLocation            // Bin location of inventory item

    String reasonCode
    String comments

    static belongsTo = [transaction: Transaction]

    static mapping = {
        id generator: 'uuid'
    }
    static constraints = {
        product(nullable: true)
        inventoryItem(nullable: false)
        binLocation(nullable: true)
        quantity(nullable: false)
        reasonCode(nullable: true)
        comments(nullable: true, maxSize: 255)
    }

    static namedQueries = {
        countByTransactionTypes { Location facility, Product product, List<TransactionType> transactionTypes, Date startDate, Date endDate ->
            createAlias 'transaction', 'transaction'
            createAlias 'inventoryItem', 'inventoryItem'
            projections {
                countDistinct('transaction.id', 'transactionCount')
            }
            eq 'product', product
            eq 'transaction.inventory', facility.inventory
            'in'('transaction.transactionType', transactionTypes)
            between 'transaction.transactionDate', startDate, endDate
        }
        dateLastCounted { Location facility, Product product ->
            createAlias "transaction", "transaction"
            createAlias "inventoryItem", "inventoryItem"
            createAlias "transaction.transactionType", "transactionType"
            projections {
                max("transaction.transactionDate", "dateLastCounted")
            }
            eq 'inventoryItem.product', product
            eq 'transaction.inventory', facility.inventory
            inList "transactionType.id", Holders.grailsApplication.config.openboxes.inventoryCount.transactionTypes
        }

    }


    /**
     * Sort by the sort parameters of the parent transaction
     */
    int compareTo(obj) {
        transaction.compareTo(obj.transaction)
    }

    @Override
    int hashCode() {
        if (this.id != null) {
            return this.id.hashCode()
        }
        return super.hashCode()
    }

    @Override
    boolean equals(Object o) {
        if (o instanceof TransactionEntry) {
            TransactionEntry that = (TransactionEntry) o
            return this.id == that.id
        }
        return false
    }
}
