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

/**
 * Definition of core system transaction codes
 *
 * DEBIT - a transaction with results in an decrease in quantity of an inventory item
 * CREDIT - a transaction which results in an increase in quantity of an inventory item
 * INVENTORY - a transaction which sets the inventory of a certain inventory item
 * PRODUCT_INVENTORY - a transaction which sets the inventory of a certain *product*
 *
 * Examples:
 * Credit and debit transactions are fairly straightforward.  If, for a particular inventory item,
 * you have a CREDIT transaction of 25, followed by a DEBIT transaction of 10, your current quantity
 * for that item (assuming there are no other transactions) would be 15
 *
 * An INVENTORY transaction overrides any previously DEBIT or CREDIT transactions.  For instance, given the 
 * following set of transactions for a given inventory item:
 *
 * 1/1/2001 - CREDIT - quantity = 25
 * 1/10/2001 - DEBIT - quantity = 15
 * 1/20/2001 - INVENTORY - quantity = 30
 *
 * The quantity on 1/19/2001 would be 10, but the quantity on 1/20/2001 would be 30, as the INVENTORY transaction overrides
 * the previous CREDIT and DEBIT transactions.
 *
 * A PRODUCT_INVENTORY transaction works in the same way, except that it is applied across *all* lots
 * for a specific product.  For instance, let's add another inventory item for the same product.
 *
 * 1/1/2001 - CREDIT - Lot 1 quantity = 25, Lot 2 quantity = 30
 * 1/10/2001 - DEBIT - Lot 1 quantity = 15
 * 1/20/2001 - INVENTORY Lot 1 quantity = 30
 * 1/25/2001 - CREDIT - Lot 2 quantity = 15
 *
 * Given these transactions, the quantity of Lot 1 on 1/25/2001 would be 30, and the quantity of Lot 2 on 1/25/2001 would be 45.
 *
 * But if we were to change the third transaction to a PRODUCT_INVENTORY transaction as follows:
 *
 * 1/1/2001 - CREDIT - Lot 1 quantity = 25, Lot 2 quantity = 30
 * 1/10/2001 - DEBIT - Lot 1 quantity = 15
 * 1/20/2001 - PRODUCT_INVENTORY Lot 1 quantity = 30
 * 1/25/2001 - CREDIT - Lot 2 quantity = 15
 *
 * The quantity of Lot 1 on 1/25/2001 would still be 30, but the quantity of Lot 2 on 1/25/2001 would be 15.  This is because the
 * 1/20 PRODUCT_INVENTORY implicitly sets the quantity of *all* inventory items for referenced products to 0.
 *
 */
enum TransactionCode {

    DEBIT,
    CREDIT,
    INVENTORY,
    PRODUCT_INVENTORY

    static list() {
        [DEBIT, CREDIT, INVENTORY, PRODUCT_INVENTORY]
    }
}
