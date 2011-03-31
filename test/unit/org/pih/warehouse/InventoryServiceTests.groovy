package org.pih.warehouse

import org.pih.warehouse.core.BaseUnitTest 
import org.pih.warehouse.inventory.Warehouse
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionType


class InventoryServiceTests extends BaseUnitTest {
    protected void setUp() {
        super.setUp()
       mockLogging(InventoryService)
    }

    void test_isValidForLocalTransfer_shouldCheckIfTransactionSupportsLocalTransfer() {
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
    	def inventoryService = new InventoryService()
    	
    	// save a local transaction based on a Transfer In Transaction
    	inventoryService.saveLocalTransfer(Transaction.get(40))
    	
    	// confirm that this transaction is now associated with a local transfer
    	assert inventoryService.isLocalTransfer(Transaction.get(40)) == true
    	def localTransfer = inventoryService.getLocalTransfer(Transaction.get(40))
    	
    	// confirm that the local transfer has the appropriate source and destination transaction
    	assert localTransfer.destinationTransaction == Transaction.get(40)
    	def newTransaction = localTransfer.sourceTransaction
    	assert newTransaction.transactionType == TransactionType.get(9)
    	assert newTransaction.inventory == Warehouse.findByName("Haiti Warehouse").inventory
    	assert newTransaction.source == null
    	assert newTransaction.destination == Warehouse.findByName("Boston Warehouse")
    	
    	// now try a local transaction based on a Transfer Out Transaction
    	inventoryService.saveLocalTransfer(Transaction.get(50))
    	
    	// confirm that this transaction is now associated with a local transfer
    	assert inventoryService.isLocalTransfer(Transaction.get(50)) == true
    	localTransfer = inventoryService.getLocalTransfer(Transaction.get(50))
    	
    	// confirm that the local transfer has the appropriate source and destination transaction
    	assert localTransfer.sourceTransaction == Transaction.get(50)
    	newTransaction = localTransfer.destinationTransaction
    	assert newTransaction.transactionType == TransactionType.get(8)
    	assert newTransaction.inventory == Warehouse.findByName("Haiti Warehouse").inventory
    	assert newTransaction.source == Warehouse.findByName("Boston Warehouse")
    	assert newTransaction.destination == null
    	
    }
    
    void test_saveLocalTransfer_shouldEditExistingLocalTransfer() {
    	def inventoryService = new InventoryService()
    	
    	def baseTransaction = Transaction.get(40)
    	
    	// first create a local transfer
    	inventoryService.saveLocalTransfer(baseTransaction)
    	
    	// now modify the base transaction
    	baseTransaction.inventory = Warehouse.findByName("Haiti Warehouse").inventory
    	baseTransaction.source = Warehouse.findByName("Boston Warehouse")
    	
    	// resave the local transfer
    	inventoryService.saveLocalTransfer(baseTransaction) 	
    	
    	// now check that the local transfer transactions have been updated accordingly
    	def localTransfer = inventoryService.getLocalTransfer(baseTransaction)
    	assert localTransfer.destinationTransaction == baseTransaction
    	def newTransaction = localTransfer.sourceTransaction
    	assert newTransaction.transactionType == TransactionType.get(9)
    	assert newTransaction.inventory == Warehouse.findByName("Boston Warehouse").inventory
    	assert newTransaction.source == null
    	assert newTransaction.destination == Warehouse.findByName("Haiti Warehouse")
    }
}
