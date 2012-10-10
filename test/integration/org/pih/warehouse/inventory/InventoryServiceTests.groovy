/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 * */
package org.pih.warehouse.inventory

import org.pih.warehouse.core.BaseIntegrationTest

import org.pih.warehouse.product.Product
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location



class InventoryServiceTests extends BaseIntegrationTest {

    def transaction1
    def transaction2
    def transaction3
    def transaction4
    def transaction5

    void setUp_transactionEntryTests() {

        // create some transactions
        transaction1 = new Transaction(transactionType: transactionType_inventory,
                transactionDate: new Date() - 5, inventory: bostonInventory)
        transaction2 = new Transaction(transactionType: transactionType_consumptionDebit,
                transactionDate: new Date() - 4, inventory: bostonInventory)
        transaction3 = new Transaction(transactionType: transactionType_productInventory,
                transactionDate: new Date() - 3, inventory: bostonInventory)
        transaction4 = new Transaction(transactionType: transactionType_transferIn,
                transactionDate: new Date() - 2, inventory: bostonInventory, source: haitiLocation)
        transaction5 = new Transaction(transactionType: transactionType_consumptionDebit,
                transactionDate: new Date() - 1, inventory: bostonInventory, destination: haitiLocation)

        // define some aspirin lot 1 transaction entries
        transaction1.addToTransactionEntries(new TransactionEntry(quantity: 10, inventoryItem: aspirinItem1))
        transaction2.addToTransactionEntries(new TransactionEntry(quantity: 2, inventoryItem: aspirinItem1))
        transaction3.addToTransactionEntries(new TransactionEntry(quantity: 100, inventoryItem: aspirinItem1))
        transaction4.addToTransactionEntries(new TransactionEntry(quantity: 24, inventoryItem: aspirinItem1))
        transaction5.addToTransactionEntries(new TransactionEntry(quantity: 30, inventoryItem: aspirinItem1))

        // define some aspirin lot 2 transaction entries
        transaction1.addToTransactionEntries(new TransactionEntry(quantity: 25, inventoryItem: aspirinItem2))
        transaction2.addToTransactionEntries(new TransactionEntry(quantity: 2, inventoryItem: aspirinItem2))
        // even though there is no entry for this lot on this transaction, it is  product inventory transaction so should reset the quantity count
        transaction4.addToTransactionEntries(new TransactionEntry(quantity: 16, inventoryItem: aspirinItem2))
        transaction5.addToTransactionEntries(new TransactionEntry(quantity: 13, inventoryItem: aspirinItem2))

        // define some tylenol lot 1 transaction entries
        transaction1.addToTransactionEntries(new TransactionEntry(quantity: 36, inventoryItem: tylenolItem))
        transaction2.addToTransactionEntries(new TransactionEntry(quantity: 21, inventoryItem: tylenolItem))
        transaction4.addToTransactionEntries(new TransactionEntry(quantity: 33, inventoryItem: tylenolItem))
        transaction5.addToTransactionEntries(new TransactionEntry(quantity: 23, inventoryItem: tylenolItem))

        def transactions = [transaction1, transaction2, transaction3, transaction4, transaction5]
        transactions.each {
            if(!it.save(flush: true)){
              it.errors.allErrors.each {
                    println it
                }
            }
        }


        assert transaction1.id != null
        assert transaction2.id != null
        assert transaction3.id != null
        assert transaction4.id != null
        assert transaction5.id != null
    }


    void test_getQuantityByProductMap() {

        setUp_transactionEntryTests()

        def inventoryService = new InventoryService()

        def map = inventoryService.getQuantityByProductMap(TransactionEntry.list())

        assert map[aspirinProduct] == 97
        assert map[tylenolProduct] == 25
    }


    void test_getQuantityByInventoryItemMap() {

        setUp_transactionEntryTests()

        def inventoryService = new InventoryService()

        // fetch the map
        def map = inventoryService.getQuantityByInventoryItemMap(TransactionEntry.list())

        assert map[aspirinItem1] == 94
        assert map[aspirinItem2] == 3
        assert map[tylenolItem] == 25

    }


    private void setUp_localTransferTests() {
        // create some test transactions

        transaction1 = new Transaction(transactionType: transactionType_inventory,
                transactionDate: new Date(), inventory: bostonInventory)
        transaction2 = new Transaction(transactionType: transactionType_transferIn,
                transactionDate: new Date(), inventory: bostonInventory, source: "sourceString")
        transaction3 = new Transaction(transactionType: transactionType_transferOut,
                transactionDate: new Date(), inventory: bostonInventory, destination: acmeLocation)
        transaction4 = new Transaction(transactionType: transactionType_transferIn,
                transactionDate: new Date(), inventory: bostonInventory, source: haitiLocation)
        transaction5 = new Transaction(transactionType: transactionType_transferOut,
                transactionDate: new Date(), inventory: bostonInventory, destination: haitiLocation)

//		mockDomain(Transaction, [new Transaction(id: 10, transactionType: TransactionType.get(7), transactionDate: new Date(), inventory: Location.findByName("Boston Location").inventory),
        //		new Transaction(id: 20, transactionType: TransactionType.get(Constants.TRANSFER_IN_TRANSACTION_TYPE_ID), transactionDate: new Date(), inventory: Location.findByName("Boston Location").inventory, source: Location.findByName("Acme Supply Company")),
        //		new Transaction(id: 30, transactionType: TransactionType.get(Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID), transactionDate: new Date(), inventory: Location.findByName("Boston Location").inventory, destination: Location.findByName("Acme Supply Company")),
        //		new Transaction(id: 40, transactionType: TransactionType.get(Constants.TRANSFER_IN_TRANSACTION_TYPE_ID), transactionDate: new Date(), inventory: Location.findByName("Boston Location").inventory, source: Location.findByName("Haiti Location")),
        //		new Transaction(id: 50, transactionType: TransactionType.get(Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID), transactionDate: new Date(), inventory: Location.findByName("Boston Location").inventory, destination: Location.findByName("Haiti Location"))])
        //
        //		// create the (empty) LocalTransfer domain
        //		mockDomain(LocalTransfer)


    }

    void test_isValidForLocalTransfer_shouldCheckIfTransactionSupportsLocalTransfer() {
        setUp_localTransferTests()

        def inventoryService = new InventoryService()

        // a transaction that isn't of transfer in or transfer out type shouldn't be marked as valid
        assert inventoryService.isValidForLocalTransfer(transaction1) == false

        // a transaction that's source or destination isn't a warehouse shouldn't pass validation //todo: need revist later; by Peter
//        assert inventoryService.isValidForLocalTransfer(transaction2) == false
//        assert inventoryService.isValidForLocalTransfer(transaction3) == false

        // transfer in/transfer out transactions associated with warehouses should pass validation
        assert inventoryService.isValidForLocalTransfer(transaction4) == true
        assert inventoryService.isValidForLocalTransfer(transaction5) == true
    }

    	void test_saveLocalTransfer_shouldCreateNewLocalTransfer() {
    		setUp_localTransferTests()

    		def inventoryService = new InventoryService()

    		def warehouse = bostonLocation

    		assert warehouse.inventory != null
    		assert transaction4.inventory != null

    		// save a local transaction based on a Transfer In Transaction
    		inventoryService.saveLocalTransfer(transaction4)

    		// confirm that this transaction is now associated with a local transfer
    		assert inventoryService.isLocalTransfer(transaction4) == true
    		def localTransfer = inventoryService.getLocalTransfer(transaction4)

    		// confirm that the local transfer has the appropriate source and destination transaction
    		assert localTransfer.destinationTransaction == transaction4
    		def newTransaction = localTransfer.sourceTransaction
    		assert newTransaction.transactionType ==  transactionType_transferOut
    		assert newTransaction.inventory == haitiInventory
    		assert newTransaction.source == null
    		assert newTransaction.destination == bostonLocation

    		// now try a local transaction based on a Transfer Out Transaction
    		inventoryService.saveLocalTransfer(transaction5)

    		// confirm that this transaction is now associated with a local transfer
    		assert inventoryService.isLocalTransfer(transaction5) == true
    		localTransfer = inventoryService.getLocalTransfer(transaction5)

    		// confirm that the local transfer has the appropriate source and destination transaction
    		assert localTransfer.sourceTransaction == transaction5
    		newTransaction = localTransfer.destinationTransaction
    		assert newTransaction.transactionType == transactionType_transferIn
    		assert newTransaction.inventory == haitiInventory
    		assert newTransaction.source == bostonLocation
    		assert newTransaction.destination == null

    	}

    	void test_saveLocalTransfer_shouldEditExistingLocalTransfer() {
    		setUp_localTransferTests()

    		def inventoryService = new InventoryService()

    		def baseTransaction = transaction4

    		// first create a local transfer
    		inventoryService.saveLocalTransfer(baseTransaction)

    		// now modify the base transaction
    		baseTransaction.inventory = haitiInventory
    		baseTransaction.source = bostonLocation

    		// resave the local transfer
    		inventoryService.saveLocalTransfer(baseTransaction)

    		// now check that the local transfer transactions have been updated accordingly
    		def localTransfer = inventoryService.getLocalTransfer(baseTransaction)
    		assert localTransfer.destinationTransaction == baseTransaction
    		def newTransaction = localTransfer.sourceTransaction
    		assert newTransaction.transactionType == transactionType_transferOut
    		assert newTransaction.inventory == bostonInventory
    		assert newTransaction.source == null
    		assert newTransaction.destination == haitiLocation
    	}

}
