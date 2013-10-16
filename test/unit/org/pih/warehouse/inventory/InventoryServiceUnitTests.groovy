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

import org.pih.warehouse.core.BaseUnitTest;
import org.pih.warehouse.core.Constants;
import org.pih.warehouse.core.Location
// import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product



class InventoryServiceUnitTests extends BaseUnitTest {

	protected void setUp() {
		super.setUp()
		mockLogging(InventoryService)
		mockDomain(Transaction)
	}
	
	private void setUp_transactionEntryTests() {
		def inventory = Location.findByName("Boston Location").inventory
		
		// create some transactions
		def transaction = new Transaction(transactionType: TransactionType.get(7), transactionDate: new Date() - 5, inventory: inventory)
		def transaction2 = new Transaction(transactionType: TransactionType.get(2), transactionDate: new Date() - 4, inventory: inventory)
		def transaction3 = new Transaction(transactionType: TransactionType.get(11), transactionDate: new Date() - 3, inventory: inventory)
		def transaction4 = new Transaction(transactionType: TransactionType.get(8), transactionDate: new Date() - 2, inventory: inventory)
		def transaction5 = new Transaction(transactionType: TransactionType.get(2), transactionDate: new Date() - 1, inventory: inventory)
		
		mockDomain(Transaction, [transaction, transaction2, transaction3, transaction4, transaction5])
		
		def transactionEntries = []
		
		// define some aspirin lot 1 transaction entries
		transactionEntries += new TransactionEntry (quantity:10, transaction: transaction,
			inventoryItem: InventoryItem.findByProductAndLotNumber(Product.findByName("Aspirin"), "1"))
		transactionEntries += new TransactionEntry (quantity:2, transaction: transaction2,
			inventoryItem: InventoryItem.findByProductAndLotNumber(Product.findByName("Aspirin"), "1"))
		transactionEntries += new TransactionEntry (quantity:100, transaction: transaction3,
			inventoryItem: InventoryItem.findByProductAndLotNumber(Product.findByName("Aspirin"), "1"))
		transactionEntries += new TransactionEntry (quantity:24, transaction: transaction4,
			inventoryItem: InventoryItem.findByProductAndLotNumber(Product.findByName("Aspirin"), "1"))
		transactionEntries += new TransactionEntry (quantity:30, transaction: transaction5,
			inventoryItem: InventoryItem.findByProductAndLotNumber(Product.findByName("Aspirin"), "1"))
		
		// define some aspirin lot 2 transaction entries
		transactionEntries += new TransactionEntry (quantity:25, transaction: transaction,
			inventoryItem: InventoryItem.findByProductAndLotNumber(Product.findByName("Aspirin"), "2"))
		transactionEntries += new TransactionEntry (quantity:2, transaction: transaction2,
			inventoryItem: InventoryItem.findByProductAndLotNumber(Product.findByName("Aspirin"), "2"))
		// even though there is no entry for this lot on this transaction, it is  product inventory transaction so should reset the quantity count
		transactionEntries += new TransactionEntry (quantity:16, transaction: transaction4,
			inventoryItem: InventoryItem.findByProductAndLotNumber(Product.findByName("Aspirin"), "2"))
		transactionEntries += new TransactionEntry (quantity:13, transaction: transaction5,
			inventoryItem: InventoryItem.findByProductAndLotNumber(Product.findByName("Aspirin"), "2"))
		
		// define some tylenol lot 1 transaction entries
		transactionEntries += new TransactionEntry (quantity:36, transaction: transaction,
			inventoryItem: InventoryItem.findByProductAndLotNumber(Product.findByName("Tylenol"), "1"))
		transactionEntries += new TransactionEntry (quantity:21, transaction: transaction2,
			inventoryItem: InventoryItem.findByProductAndLotNumber(Product.findByName("Tylenol"), "1"))
		transactionEntries += new TransactionEntry (quantity:33, transaction: transaction4,
			inventoryItem: InventoryItem.findByProductAndLotNumber(Product.findByName("Tylenol"), "1"))
		transactionEntries += new TransactionEntry (quantity:23, transaction: transaction5,
			inventoryItem: InventoryItem.findByProductAndLotNumber(Product.findByName("Tylenol"), "1"))
		
		
		mockDomain(TransactionEntry, transactionEntries)
	}


    void test_getQuantityByProductAndInventoryItemMap_shouldReturnEmptyMapOnNullParameter() {
        def inventoryService = new InventoryService();
        def map = inventoryService.getQuantityByProductAndInventoryItemMap(null)
        assertEquals [:],map
    }
	
	void test_getQuantityByProductAndInventoryItemMap() {
		
		setUp_transactionEntryTests()
		
		def inventoryService = new InventoryService()
		
		// fetch the map
		def map = inventoryService.getQuantityByProductAndInventoryItemMap(TransactionEntry.list())
		
		// make sure the quantities are correct
		def aspirin = Product.findByName("Aspirin")
		def tylenol = Product.findByName("Tylenol")
		def aspirinLot1 = InventoryItem.findByProductAndLotNumber(aspirin, "1")
		def aspirinLot2 = InventoryItem.findByProductAndLotNumber(aspirin, "2")
		def tylenolLot1 = InventoryItem.findByProductAndLotNumber(tylenol, "1")
		
		assert map[aspirin][aspirinLot1] == 94
		assert map[aspirin][aspirinLot2] == 3
		assert map[tylenol][tylenolLot1] == 25
		
	}
	
	void test_getQuantityByProductMap() {
		
		setUp_transactionEntryTests()
		
		def inventoryService = new InventoryService()
		
		// fetch the map
		def map = inventoryService.getQuantityByProductMap(TransactionEntry.list())
		
		// make sure the quantities are correct
		def aspirin = Product.findByName("Aspirin")
		def tylenol = Product.findByName("Tylenol")
		
		assert map[aspirin] == 97
		assert map[tylenol] == 25
	}
	
	
	void test_getQuantityByInventoryItemMap() {
		
		setUp_transactionEntryTests()
		
		def inventoryService = new InventoryService()
		
		// fetch the map
		def map = inventoryService.getQuantityByInventoryItemMap(TransactionEntry.list())
		
		// make sure the quantities are correct
		def aspirin = Product.findByName("Aspirin")
		def tylenol = Product.findByName("Tylenol")
		def aspirinLot1 = InventoryItem.findByProductAndLotNumber(aspirin, "1")
		def aspirinLot2 = InventoryItem.findByProductAndLotNumber(aspirin, "2")
		def tylenolLot1 = InventoryItem.findByProductAndLotNumber(tylenol, "1")
		
		assert map[aspirinLot1] == 94
		assert map[aspirinLot2] == 3
		assert map[tylenolLot1] == 25
		
	}
	
	/*
	private void setUp_localTransferTests () {
		// create some test transactions
		mockDomain(Transaction, [new Transaction(id: 10, transactionType: TransactionType.get(7), transactionDate: new Date(), inventory: Location.findByName("Boston Location").inventory),
		new Transaction(id: 20, transactionType: TransactionType.get(Constants.TRANSFER_IN_TRANSACTION_TYPE_ID), transactionDate: new Date(), inventory: Location.findByName("Boston Location").inventory, source: Location.findByName("Acme Supply Company")),
		new Transaction(id: 30, transactionType: TransactionType.get(Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID), transactionDate: new Date(), inventory: Location.findByName("Boston Location").inventory, destination: Location.findByName("Acme Supply Company")),
		new Transaction(id: 40, transactionType: TransactionType.get(Constants.TRANSFER_IN_TRANSACTION_TYPE_ID), transactionDate: new Date(), inventory: Location.findByName("Boston Location").inventory, source: Location.findByName("Haiti Location")),
		new Transaction(id: 50, transactionType: TransactionType.get(Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID), transactionDate: new Date(), inventory: Location.findByName("Boston Location").inventory, destination: Location.findByName("Haiti Location"))])
		
		// create the (empty) LocalTransfer domain
		mockDomain(LocalTransfer)
		
		
	}
	
	void test_isValidForLocalTransfer_shouldCheckIfTransactionSupportsLocalTransfer() {
		setUp_localTransferTests()
		
		def inventoryService = new InventoryService()
		
		// a transaction that isn't of transfer in or transfer out type shouldn't be marked as valid
		assert inventoryService.isValidForLocalTransfer(Transaction.get(10)) == false
		
		// a transaction that's source or destination isn't a warehouse shouldn't pass validation
		assert inventoryService.isValidForLocalTransfer(Transaction.get(20)) == false
		assert inventoryService.isValidForLocalTransfer(Transaction.get(30)) == false
		
		// transfer in/transfer out transactions associated with warehouses should pass validation
		assert inventoryService.isValidForLocalTransfer(Transaction.get(40)) == true
		assert inventoryService.isValidForLocalTransfer(Transaction.get(50)) == true
	}
	
	void test_saveLocalTransfer_shouldCreateNewLocalTransfer() {
		setUp_localTransferTests()
		
		def inventoryService = new InventoryService()
		
		def warehouse = Location.findByName("Boston Location")
		
		assert warehouse.inventory != null
		assert Transaction.get(40).inventory != null
		
		// save a local transaction based on a Transfer In Transaction
		inventoryService.saveLocalTransfer(Transaction.get(40))
		
		// confirm that this transaction is now associated with a local transfer
		assert inventoryService.isLocalTransfer(Transaction.get(40)) == true
		def localTransfer = inventoryService.getLocalTransfer(Transaction.get(40))
		
		// confirm that the local transfer has the appropriate source and destination transaction
		assert localTransfer.destinationTransaction == Transaction.get(40)
		def newTransaction = localTransfer.sourceTransaction
		assert newTransaction.transactionType == TransactionType.get(9)
		assert newTransaction.inventory == Location.findByName("Haiti Location").inventory
		assert newTransaction.source == null
		assert newTransaction.destination == Location.findByName("Boston Location")
		
		// now try a local transaction based on a Transfer Out Transaction
		inventoryService.saveLocalTransfer(Transaction.get(50))
		
		// confirm that this transaction is now associated with a local transfer
		assert inventoryService.isLocalTransfer(Transaction.get(50)) == true
		localTransfer = inventoryService.getLocalTransfer(Transaction.get(50))
		
		// confirm that the local transfer has the appropriate source and destination transaction
		assert localTransfer.sourceTransaction == Transaction.get(50)
		newTransaction = localTransfer.destinationTransaction
		assert newTransaction.transactionType == TransactionType.get(8)
		assert newTransaction.inventory == Location.findByName("Haiti Location").inventory
		assert newTransaction.source == Location.findByName("Boston Location")
		assert newTransaction.destination == null
		
	}
	
	void test_saveLocalTransfer_shouldEditExistingLocalTransfer() {
		setUp_localTransferTests()
		
		def inventoryService = new InventoryService()
		
		def baseTransaction = Transaction.get(40)
		
		// first create a local transfer
		inventoryService.saveLocalTransfer(baseTransaction)
		
		// now modify the base transaction
		baseTransaction.inventory = Location.findByName("Haiti Location").inventory
		baseTransaction.source = Location.findByName("Boston Location")
		
		// resave the local transfer
		inventoryService.saveLocalTransfer(baseTransaction) 	
		
		// now check that the local transfer transactions have been updated accordingly
		def localTransfer = inventoryService.getLocalTransfer(baseTransaction)
		assert localTransfer.destinationTransaction == baseTransaction
		def newTransaction = localTransfer.sourceTransaction
		assert newTransaction.transactionType == TransactionType.get(9)
		assert newTransaction.inventory == Location.findByName("Boston Location").inventory
		assert newTransaction.source == null
		assert newTransaction.destination == Location.findByName("Haiti Location")
	}
	*/

//    Unit test hates createCriteria() T_T
//
//    void test_getInventoryItemsWithQuantity() {
//
//        def product = new Product(id: "prod")
//        def product2 = new Product(id: "prod2")
//
//        def inventoryItem1 = new InventoryItem(id: "inventItem1", product:product, lotNumber: "abcd")
//        def inventoryItem2 = new InventoryItem(id: "inventItem2", product:product2, lotNumber: "efgh")
//
//        def inventory = new Inventory(id: "inventory")
//        def transaction1 = new Transaction(id: "transaction1")
//        def transaction2 = new Transaction(id: "transaction2")
//        def transaction3 = new Transaction(id: "transaction3")
//
//        def transactionType1 = new TransactionType(id: "transType1", transactionCode: TransactionCode.CREDIT)
//        def transactionType2 = new TransactionType(id: "transType2", transactionCode: TransactionCode.DEBIT)
//        def transactionType3 = new TransactionType(id: "transType3", transactionCode: TransactionCode.INVENTORY)
//
//        transaction1.inventory = inventory
//        transaction1.transactionType = transactionType1
//        transaction2.inventory = inventory
//        transaction2.transactionType = transactionType2
//        transaction3.inventory = inventory
//        transaction3.transactionType = transactionType3
//
//        def transactionEntry1 = new TransactionEntry(id: "transactionEntry1", quantity: 200, inventoryItem: inventoryItem1)
//        def transactionEntry2 = new TransactionEntry(id: "transactionEntry2", quantity: 400, inventoryItem: inventoryItem1)
//        def transactionEntry3 = new TransactionEntry(id: "transactionEntry3", quantity: 500, inventoryItem: inventoryItem1)
//        def transactionEntry4 = new TransactionEntry(id: "transactionEntry4", quantity: 100, inventoryItem: inventoryItem2)
//        def transactionEntry5 = new TransactionEntry(id: "transactionEntry5", quantity: 50, inventoryItem: inventoryItem2)
//        def transactionEntry6 = new TransactionEntry(id: "transactionEntry6", quantity: 300, inventoryItem: inventoryItem2)
//
//        transaction1.addToTransactionEntries(transactionEntry1)
//        transaction1.addToTransactionEntries(transactionEntry2)
//        transaction2.addToTransactionEntries(transactionEntry3)
//        transaction2.addToTransactionEntries(transactionEntry4)
//        transaction3.addToTransactionEntries(transactionEntry5)
//        transaction3.addToTransactionEntries(transactionEntry6)
//
//        mockDomain(Product, [product, product2])
//        mockDomain(InventoryItem, [inventoryItem1, inventoryItem2])
//        mockDomain(Inventory, [inventory])
//        mockDomain(Transaction, [transaction1, transaction2, transaction3])
//        mockDomain(TransactionType, [transactionType1, transactionType2, transactionType3])
//        mockDomain(TransactionEntry, [transactionEntry1, transactionEntry2, transactionEntry3, transactionEntry4, transactionEntry5, transactionEntry6])
//
//        def service = new InventoryService()
//        def inventoryItems = service.getInventoryItemsWithQuantity([product, product2], inventory)
//
//        assert inventoryItems.size() == 2
//        assert inventoryItems[0].lotNumber == inventoryItem1.lotNumber
//        assert inventoryItems[1].lotNumber == inventoryItem2.lotNumber
//
//        assert inventoryItems[0].quantity == inventoryItem1.quantity
//        assert inventoryItems[0].quantity == 100
//        assert inventoryItems[1].quantity == inventoryItem2.quantity
//        assert inventoryItems[1].quantity == 300
//
//    }

}
