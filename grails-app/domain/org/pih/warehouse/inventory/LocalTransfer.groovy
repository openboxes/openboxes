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

import org.pih.warehouse.core.Constants


/**
 * This class is meant to link together two transactions--a Transfer Out and a Transfer In--that
 * represent a transfer of goods between two locally-managed warehouses.  This is for when we want to 
 * create a direct, immediate transfer, as opposed to transferring via a Shipment 
 *
 * Use the following methods within InventorySevice to create, modify, and delete LocalTransfers:
 *  getLocalTransfer(Tranasction)
 *  isLocalTransfer(Transaction)
 *  isValidForLocalTransfer(Transaction)
 *  deleteLocalTransfer(Transaction)
 *  saveLocalTransfer(Transaction)
 *
 * These methods make sure the two linked transactions stay in sync. See docs on these methods for more information.
 *
 */
class LocalTransfer implements Serializable {

    String id

    // Core data elements
    Transaction sourceTransaction
    Transaction destinationTransaction

    // Auditing
    Date dateCreated
    Date lastUpdated

    String toString() { return "${id}" }

    // Constraints
    static constraints = {
        sourceTransaction(nullable: false, unique: true,
                validator: { transaction -> transaction.transactionType.id == Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID })

        destinationTransaction(nullable: false, unique: true,
                validator: { transaction -> transaction.transactionType.id == Constants.TRANSFER_IN_TRANSACTION_TYPE_ID })
    }

    static mapping = {
        id generator: 'uuid'
        sourceTransaction cascade: "all-delete-orphan"
        destinationTransaction cascade: "all-delete-orphan"
    }
}
