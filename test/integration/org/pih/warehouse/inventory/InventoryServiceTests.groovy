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

import org.junit.Ignore
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.product.Product
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.Tag;
import org.pih.warehouse.core.User
import org.springframework.core.io.ClassPathResource;
import testutils.DbHelper

import org.junit.Test

class InventoryServiceTests extends GroovyTestCase {
	
	def inventoryService
	
    protected def transactionType_consumptionDebit
    protected def  transactionType_inventory
    protected def  transactionType_productInventory
    protected def  transactionType_transferIn
    protected def  transactionType_transferOut
    protected def  bostonLocation
    protected def  haitiLocation
    protected def  warehouseLocationType
    protected def  supplierLocationType
    protected def  acmeLocation
    protected def  bostonInventory
    protected def  haitiInventory
    protected def  aspirinProduct
    protected def  tylenolProduct
	protected def  ibuprofenProduct
    protected def  advilProduct
    protected def  aspirinItem1
    protected def  aspirinItem2
    protected def tylenolItem
    def transaction1
    def transaction2
    def transaction3
    def transaction4
    def transaction5

    def level1
    def level2
    def level3
    def level4
    def level5
	def level6
	
    private void basicTestFixture(){
        warehouseLocationType = LocationType.get(Constants.WAREHOUSE_LOCATION_TYPE_ID)
        supplierLocationType = LocationType.get(Constants.SUPPLIER_LOCATION_TYPE_ID)

        // get or create a default location
        acmeLocation = DbHelper.creatLocationIfNotExist("Acme Supply Company", supplierLocationType)

        // create some default warehouses and inventories
        bostonLocation = DbHelper.creatLocationIfNotExist("Boston Location", warehouseLocationType)
        haitiLocation = DbHelper.creatLocationIfNotExist("Haiti Location", warehouseLocationType)

        bostonInventory = DbHelper.createInventory(bostonLocation)
        haitiInventory = DbHelper.createInventory(haitiLocation)

        // create some default transaction types
        transactionType_consumptionDebit = TransactionType.get(Constants.CONSUMPTION_TRANSACTION_TYPE_ID) //id:2
        transactionType_inventory = TransactionType.get(Constants.INVENTORY_TRANSACTION_TYPE_ID) //id:7
        transactionType_productInventory = TransactionType.get(Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID)  //id:11
        transactionType_transferIn =  TransactionType.get(Constants.TRANSFER_IN_TRANSACTION_TYPE_ID) //id:8
        transactionType_transferOut =  TransactionType.get(Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID) //id:9

        // create some products
        aspirinProduct = DbHelper.createProductIfNotExists("Aspirin" + UUID.randomUUID().toString()[0..5])
        tylenolProduct = DbHelper.createProductIfNotExists("Tylenol" + UUID.randomUUID().toString()[0..5])
		ibuprofenProduct = DbHelper.createProductIfNotExists("Ibuprofen" + UUID.randomUUID().toString()[0..5])
		advilProduct = DbHelper.createProductIfNotExists("Advil" + UUID.randomUUID().toString()[0..5])
		ibuprofenProduct.description = "Ibuprofen is a nonsteroidal anti-inflammatory drug (NSAID)"
		ibuprofenProduct.save(flush:true);
				
		tylenolProduct.brandName = "TYLENOL®"
		tylenolProduct.manufacturer = "McNeil Consumer Healthcare"
		tylenolProduct.manufacturerName = "Tylenol Extra Strength Acetaminophen 500 Mg 325 Caplets"
		tylenolProduct.manufacturerCode = "TYL325"
		tylenolProduct.vendor = "IDA"
		tylenolProduct.vendorCode = "025200"
		tylenolProduct.vendorName = "Acetaminophen 325 mg, film coated"
		tylenolProduct.save(flush:true);
		
		
        // create some inventory items
        aspirinItem1 = DbHelper.createInventoryItem(aspirinProduct, "1", new Date().plus(100))
        aspirinItem2 = DbHelper.createInventoryItem(aspirinProduct, "2", new Date().plus(10))
        tylenolItem = DbHelper.createInventoryItem(tylenolProduct, "lot9383")
    }

    private void transactionEntryTestFixture() {

        basicTestFixture()

        transaction1 = new Transaction(transactionType: transactionType_productInventory, transactionDate: new Date() - 5, inventory: bostonInventory)
        transaction1.addToTransactionEntries(new TransactionEntry(quantity: 10, inventoryItem: aspirinItem1))
        transaction1.addToTransactionEntries(new TransactionEntry(quantity: 25, inventoryItem: aspirinItem2))
        transaction1.addToTransactionEntries(new TransactionEntry(quantity: 36, inventoryItem: tylenolItem))

        transaction2 = new Transaction(transactionType: transactionType_consumptionDebit, transactionDate: new Date() - 4, inventory: bostonInventory)
        transaction2.addToTransactionEntries(new TransactionEntry(quantity: 2, inventoryItem: aspirinItem1))
        transaction2.addToTransactionEntries(new TransactionEntry(quantity: 2, inventoryItem: aspirinItem2))
        transaction2.addToTransactionEntries(new TransactionEntry(quantity: 21, inventoryItem: tylenolItem))

        transaction3 = new Transaction(transactionType: transactionType_productInventory, transactionDate: new Date() - 3, inventory: bostonInventory)
        transaction3.addToTransactionEntries(new TransactionEntry(quantity: 100, inventoryItem: aspirinItem1))

        transaction4 = new Transaction(transactionType: transactionType_transferIn, transactionDate: new Date() - 2, inventory: bostonInventory, source: haitiLocation)
        transaction4.addToTransactionEntries(new TransactionEntry(quantity: 24, inventoryItem: aspirinItem1))
        transaction4.addToTransactionEntries(new TransactionEntry(quantity: 16, inventoryItem: aspirinItem2))
        transaction4.addToTransactionEntries(new TransactionEntry(quantity: 33, inventoryItem: tylenolItem))

        transaction5 = new Transaction(transactionType: transactionType_consumptionDebit, transactionDate: new Date() - 1, inventory: bostonInventory, destination: haitiLocation)
        transaction5.addToTransactionEntries(new TransactionEntry(quantity: 30, inventoryItem: aspirinItem1))
        transaction5.addToTransactionEntries(new TransactionEntry(quantity: 13, inventoryItem: aspirinItem2))
        transaction5.addToTransactionEntries(new TransactionEntry(quantity: 23, inventoryItem: tylenolItem))

        def transactions = [transaction1, transaction2, transaction3, transaction4, transaction5]
        transactions.each {
            if (!it.save(flush: true)) {
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

    private void transactionEntryTestFixture2() {

        basicTestFixture()

        transaction1 = new Transaction(transactionType: transactionType_inventory, transactionDate: new Date(), inventory: bostonInventory)
        transaction1.addToTransactionEntries(new TransactionEntry(quantity: 10, inventoryItem: aspirinItem1))
        transaction1.addToTransactionEntries(new TransactionEntry(quantity: 25, inventoryItem: aspirinItem2))
        transaction1.addToTransactionEntries(new TransactionEntry(quantity: 36, inventoryItem: tylenolItem))


        transaction2 = new Transaction(transactionType: transactionType_consumptionDebit, transactionDate: new Date(), inventory: bostonInventory)
        transaction2.addToTransactionEntries(new TransactionEntry(quantity: 2, inventoryItem: aspirinItem1))
        transaction2.addToTransactionEntries(new TransactionEntry(quantity: 2, inventoryItem: aspirinItem2))
        transaction2.addToTransactionEntries(new TransactionEntry(quantity: 21, inventoryItem: tylenolItem))

        transaction3 = new Transaction(transactionType: transactionType_productInventory, transactionDate: new Date(), inventory: bostonInventory)
        transaction3.addToTransactionEntries(new TransactionEntry(quantity: 100, inventoryItem: aspirinItem1))


        transaction4 = new Transaction(transactionType: transactionType_transferIn, transactionDate: new Date(), inventory: bostonInventory, source: haitiLocation)
        transaction4.addToTransactionEntries(new TransactionEntry(quantity: 24, inventoryItem: aspirinItem1))
        transaction4.addToTransactionEntries(new TransactionEntry(quantity: 16, inventoryItem: aspirinItem2))
        transaction4.addToTransactionEntries(new TransactionEntry(quantity: 33, inventoryItem: tylenolItem))

        transaction5 = new Transaction(transactionType: transactionType_consumptionDebit, transactionDate: new Date(), inventory: bostonInventory, destination: haitiLocation)
        transaction5.addToTransactionEntries(new TransactionEntry(quantity: 30, inventoryItem: aspirinItem1))
        transaction5.addToTransactionEntries(new TransactionEntry(quantity: 13, inventoryItem: aspirinItem2))
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



    private void localTransferTestFixture() {
        basicTestFixture()

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
    }

    private void inventoryLevelTestFixture() {
        basicTestFixture()
        level1 = new InventoryLevel(status: InventoryStatus.SUPPORTED, inventory: bostonInventory, product: aspirinProduct)
        level2 = new InventoryLevel(status: InventoryStatus.NOT_SUPPORTED, inventory: bostonInventory, product: tylenolProduct)
        level3 = new InventoryLevel(status: InventoryStatus.SUPPORTED, inventory: haitiInventory, product: aspirinProduct)
        level4 = new InventoryLevel(status: InventoryStatus.NOT_SUPPORTED, inventory: bostonInventory, product: ibuprofenProduct)
        level5 = new InventoryLevel(status: InventoryStatus.NOT_SUPPORTED, inventory: haitiInventory, product: ibuprofenProduct)
		level6 = new InventoryLevel(status: InventoryStatus.SUPPORTED, inventory: haitiInventory, product: tylenolProduct)
		
        bostonInventory.addToConfiguredProducts(level1)
        bostonInventory.addToConfiguredProducts(level2)
        bostonInventory.addToConfiguredProducts(level4)
        haitiInventory.addToConfiguredProducts(level3)
        haitiInventory.addToConfiguredProducts(level5)
		haitiInventory.addToConfiguredProducts(level6)
		
        bostonInventory.save(flush:true)
        haitiInventory.save(flush:true)

        level1.save(flush:true)
        level2.save(flush:true)
        level3.save(flush:true)
        level4.save(flush:true)
        level5.save(flush:true)
        level6.save(flush:true)
    }

    @Ignore // FIXME We removed the showHidden feature awhile back
    void test_getProductsByTermsAndCategoriesWithoutHiddenProductsNoInventoryLevelsAtCurrentInventory() {
        inventoryLevelTestFixture()
        //def inventoryService = new InventoryService()
        def terms = ["Tylenol"]
        def result = inventoryService.getProductsByTermsAndCategories(terms, null, false, haitiInventory, 25, 0)
        assert result.contains(tylenolProduct)

        terms = ["Advil"]
        result = inventoryService.getProductsByTermsAndCategories(terms, null, false, haitiInventory, 25, 0)
        assert result.contains(advilProduct)
    }

    @Ignore // FIXME We removed the showHidden feature awhile back
    void test_getProductsByTermsAndCategoriesWithoutHiddenProductsWithInventoryLevelsNotSupported() {
        inventoryLevelTestFixture()
        //def inventoryService = new InventoryService()
        def terms = ["Tylenol"]
        def result = inventoryService.getProductsByTermsAndCategories(terms, null, false, bostonInventory, 25, 0)
        assert !result.contains(tylenolProduct)

        terms = ["Ibuprofen"]
        result = inventoryService.getProductsByTermsAndCategories(terms, null, false, bostonInventory, 25, 0)
        assert !result.contains(ibuprofenProduct)
    }

    @Ignore // FIXME We removed the showHidden feature awhile back
    void test_getProductsByTermsAndCategoriesWithoutHiddenProductsWithInventoryLevelsSupported() {
        inventoryLevelTestFixture()
        //def inventoryService = new InventoryService()
        def terms = ["Aspirin"]
        def result = inventoryService.getProductsByTermsAndCategories(terms, null, false, bostonInventory, 25, 0)
        assert result.contains(aspirinProduct)

        terms = ["Ibuprofen"]
        result = inventoryService.getProductsByTermsAndCategories(terms, null, false, bostonInventory, 25, 0)
        assert !result.contains(ibuprofenProduct)
    }

	@Test
	void getProductsByTermsAndCategories_shouldFindByBrand() {
		inventoryLevelTestFixture()
		def terms = ["TYLENOL®"]
		def result = inventoryService.getProductsByTermsAndCategories(terms, null, false, haitiInventory, 25, 0)
		println result
		assert result.contains(tylenolProduct)		
	}

	@Test
	void getProductsByTermsAndCategories_shouldFindByManufacturer() { 		
		inventoryLevelTestFixture()
		def terms = ["McNeil"]
		def result = inventoryService.getProductsByTermsAndCategories(terms, null, false, haitiInventory, 25, 0)
		println result
		assert result.contains(tylenolProduct)		
		
		terms = ["TYL325"]
		result = inventoryService.getProductsByTermsAndCategories(terms, null, false, haitiInventory, 25, 0)
		println result
		assert result.contains(tylenolProduct)
	}

	@Test
	void getProductsByTermsAndCategories_shouldFindByVendor() {
		inventoryLevelTestFixture()
		def terms = ["IDA"]
		def result = inventoryService.getProductsByTermsAndCategories(terms, null, false, haitiInventory, 25, 0)
		println result
		assert result.contains(tylenolProduct)
		
		terms = ["025200"]
		result = inventoryService.getProductsByTermsAndCategories(terms, null, false, haitiInventory, 25, 0)
		println result
		assert result.contains(tylenolProduct)
		
		terms = ["Extra Strength"]
		result = inventoryService.getProductsByTermsAndCategories(terms, null, false, haitiInventory, 25, 0)
		println result
		assert result.contains(tylenolProduct)
		
	}

    private void productTagTestFixture() {
		basicTestFixture()
		User user = User.get(1)
		assertNotNull user 
		println user
		aspirinProduct.addToTags(new Tag(tag: "thistag"))
		aspirinProduct.save(flush:true, failOnError:true)
		tylenolProduct.addToTags(new Tag(tag: "thattag"))
		tylenolProduct.save(flush:true, failOnError:true)
		assertEquals 1, aspirinProduct.tags.size()
		assertEquals 1, tylenolProduct.tags.size()
		assertEquals 2, Tag.list().size()
		
	}

    void test_getQuantityByProductMap() {

        transactionEntryTestFixture()
        def map = inventoryService.getQuantityByProductMap(TransactionEntry.list())

        assert map[aspirinProduct] == 97
        assert map[tylenolProduct] == 25
    }


    @Test
    //todo: getQuantity is broken now, need to know why
    void getQuantity(){
        transactionEntryTestFixture()

        assert inventoryService.getQuantity(bostonInventory, (Location) null, aspirinItem1) == 94
        assert inventoryService.getQuantity(bostonInventory, (Location) null, aspirinItem2) == 3
        assert inventoryService.getQuantity(bostonInventory, (Location) null, tylenolItem) == 25
    }

    @Test
    void getQuantity_shouldHandleTransactionsForSameDay(){
        transactionEntryTestFixture2()

        assert inventoryService.getQuantity(bostonInventory, (Location) null, aspirinItem1) == 94
        assert inventoryService.getQuantity(bostonInventory, (Location) null, aspirinItem2) == 3
        assert inventoryService.getQuantity(bostonInventory, (Location) null, tylenolItem) == 25
    }



    @Test
    void test_getProductsQuantityForInventory(){
        transactionEntryTestFixture()
        //def inventoryService = new InventoryService()
        def results = inventoryService.getQuantityForProducts(bostonInventory, [tylenolProduct.id])
        assert results[tylenolProduct.id] == 25
        assert results.keySet().size() == 1
    }

    @Test
	void test_getProductsQuantityForInventoryWithEmptyProductArray(){
		transactionEntryTestFixture()
		//def inventoryService = new InventoryService()
		def results = inventoryService.getQuantityForProducts(bostonInventory, [])		
		assert results == [:]
	}

	
	
    void test_getQuantityByInventoryItemMap() {

        transactionEntryTestFixture()

        //def inventoryService = new InventoryService()

        // fetch the map
        def map = inventoryService.getQuantityByInventoryItemMap(TransactionEntry.list())

        assert map[aspirinItem1] == 94
        assert map[aspirinItem2] == 3
        assert map[tylenolItem] == 25

    }

    void test_getInventoryItemsWithQuantity() {

        transactionEntryTestFixture()

        def products = [aspirinProduct, tylenolProduct]

        //def inventoryService = new InventoryService()
        def inventoryItems = inventoryService.getInventoryItemsWithQuantity(products, bostonInventory)
        assert inventoryItems.size() == 2
        assert inventoryItems[aspirinProduct]
        assert inventoryItems[tylenolProduct]
        assert inventoryItems[aspirinProduct].size() == 2
        assert inventoryItems[tylenolProduct].size() == 1
        assert inventoryItems[aspirinProduct].find{ it.id == aspirinItem1.id }.quantity == 94
        assert inventoryItems[aspirinProduct].find{ it.id == aspirinItem2.id }.quantity == 3
        assert inventoryItems[tylenolProduct].find{ it.id == tylenolItem.id }.quantity == 25
        assert inventoryItems[aspirinProduct][0].id == aspirinItem2.id //sorted by expirationDate
        assert inventoryItems[aspirinProduct][1].id == aspirinItem1.id


    }

    void test_isValidForLocalTransfer_shouldCheckIfTransactionSupportsLocalTransfer() {
        localTransferTestFixture()

        //def inventoryService = new InventoryService()

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
        localTransferTestFixture()

        //def inventoryService = new InventoryService()

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
        localTransferTestFixture()

        //def inventoryService = new InventoryService()

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



    void test_getProductsByTags() {
        productTagTestFixture()
        //def inventoryService = new InventoryService()
        def tags = ["thistag", "thattag"].collect { Tag.findByTag(it).id }

        def results = inventoryService.getProductsByTags(tags, 10, 0)
        assertEquals 2, results.size()
    }


    void test_getProductsByTag() {
        productTagTestFixture()
        def tags = Tag.list()
        assertEquals 2, tags.size()

        //def inventoryService = new InventoryService()
        def results = inventoryService.getProductsByTag("thistag")
        assertEquals 1, results.size()
    }

    @Test
    void getProductsByTag_shouldNotFailDueToSQLGrammarException() {
        productTagTestFixture()
        def tags = ["thistag"].collect { Tag.findByTag(it).id }
        def results = inventoryService.getProductsByTags(tags, -1, 0)
        assertEquals 1, results.size()
    }


    void test_getProductsByTermsAndCategoriesAndLotNumberWithProductSearchTerm() {
        basicTestFixture()
        def terms = ["Asp", "rin"]
        //def inventoryService = new InventoryService()
        def results = inventoryService.getProductsByTermsAndCategories(terms, null, true, bostonInventory,  25, 0)
        assert results.contains(aspirinProduct)
    }


    void test_getProductsByTermsAndCategoriesAndLotNumberWithLotNumberSearchTerm() {
        basicTestFixture()
        def terms = ["lot9383"]
        //def inventoryService = new InventoryService()
        def results = inventoryService.getProductsByTermsAndCategories(terms, null, true, bostonInventory, 1000, 0)
        assert results.contains(tylenolProduct)
    }

	void test_getProductsByTermsAndCategoriesWithProductName() {
		basicTestFixture()
		def terms = ["Ibuprofen"]
		//def inventoryService = new InventoryService()
		def results = inventoryService.getProductsByTermsAndCategories(terms, null, true, bostonInventory, 25, 0)
		assert results.contains(ibuprofenProduct)
	}

	void test_getProductsByTermsAndCategoriesWithDescription() {
		basicTestFixture()
		def terms = ["NSAID"]
		//def inventoryService = new InventoryService()
		def results = inventoryService.getProductsByTermsAndCategories(terms, null, true, bostonInventory, 25, 0)
		assert results.contains(ibuprofenProduct)
	}

    @Test
    void importInventory_shouldSaveRecordInventoryTransaction() {

        def location = Location.list()[0]
        def resource = new ClassPathResource("resources/inventory2.xls")
        def file = resource.getFile()
        assert file.exists()

        def command = new ImportDataCommand()
        command.location = location
        command.importFile = file
        command.date = new Date()

        def data = inventoryService.importInventoryData(command)

        def productCodes = data.collect { it.productCode }.unique()
        productCodes.each {
            def product = Product.findByProductCode(it)
            if (product) {
                def quantityOnHand = inventoryService.getQuantityOnHand(location, product)
                println "Product ${product.productCode} ${product.name}: ${quantityOnHand}"
            }
        }

    }
}
