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

import grails.test.mixin.Mock
import grails.testing.services.ServiceUnitTest
import org.hibernate.SessionFactory
import spock.lang.Ignore

import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.User
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.importer.InventoryImportDataService
import org.pih.warehouse.product.Product
import org.springframework.core.io.ClassPathResource
import spock.lang.Shared
import spock.lang.Specification
import testutils.DbHelper
import static org.junit.Assert.*;

@Ignore('Fix these tests and move them to InventoryServiceSpec or convert them to API tests')
@Mock([InventoryImportDataService])
class InventoryServiceTests extends Specification implements ServiceUnitTest<InventoryService>  {

//    def service
    InventoryImportDataService inventoryImportDataService
    SessionFactory sessionFactory

    @Shared
    protected def transactionType_consumptionDebit
    @Shared
    protected def  transactionType_inventory
    @Shared
    protected def  transactionType_productInventory
    @Shared
    protected def  transactionType_transferIn
    @Shared
    protected def  transactionType_transferOut
    @Shared
    protected def  bostonLocation
    @Shared
    protected def  haitiLocation
    @Shared
    protected def  warehouseLocationType
    @Shared
    protected def  supplierLocationType
    @Shared
    protected def  acmeLocation
    @Shared
    protected def  bostonInventory
    @Shared
    protected def  haitiInventory
    @Shared
    protected def  aspirinProduct
    @Shared
    protected def  tylenolProduct
    @Shared
    protected def  ibuprofenProduct
    @Shared
    protected def  advilProduct
    @Shared
    protected def  aspirinItem1
    @Shared
    protected def  aspirinItem2
    @Shared
    protected def tylenolItem
    @Shared
    def transaction1
    @Shared
    def transaction2
    @Shared
    def transaction3
    @Shared
    def transaction4
    @Shared
    def transaction5

    @Shared
    def level1
    @Shared
    def level2
    @Shared
    def level3
    @Shared
    def level4
    @Shared
    def level5
    @Shared
    def level6

    private void basicTestFixture(){
        warehouseLocationType = LocationType.get(Constants.WAREHOUSE_LOCATION_TYPE_ID)
        supplierLocationType = LocationType.get(Constants.SUPPLIER_LOCATION_TYPE_ID)

        // get or create a default location
        acmeLocation = DbHelper.findOrCreateLocation('Acme Supply Company', supplierLocationType)

        // create some default warehouses and inventories
        bostonLocation = DbHelper.findOrCreateLocation('Boston Location', warehouseLocationType)
        haitiLocation = DbHelper.findOrCreateLocation('Haiti Location', warehouseLocationType)

        bostonInventory = DbHelper.createInventory(bostonLocation)
        haitiInventory = DbHelper.createInventory(haitiLocation)

        // create some default transaction types
        transactionType_consumptionDebit = TransactionType.get(Constants.CONSUMPTION_TRANSACTION_TYPE_ID) //id:2
        transactionType_inventory = TransactionType.get(Constants.INVENTORY_TRANSACTION_TYPE_ID) //id:7
        transactionType_productInventory = TransactionType.get(Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID)  //id:11
        transactionType_transferIn =  TransactionType.get(Constants.TRANSFER_IN_TRANSACTION_TYPE_ID) //id:8
        transactionType_transferOut =  TransactionType.get(Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID) //id:9

        // create some products
        aspirinProduct = DbHelper.findOrCreateProduct('Aspirin' + UUID.randomUUID().toString()[0..5])
        tylenolProduct = DbHelper.findOrCreateProduct('Tylenol' + UUID.randomUUID().toString()[0..5])
        ibuprofenProduct = DbHelper.findOrCreateProduct('Ibuprofen' + UUID.randomUUID().toString()[0..5])
        advilProduct = DbHelper.findOrCreateProduct('Advil' + UUID.randomUUID().toString()[0..5])
        ibuprofenProduct.description = 'Ibuprofen is a nonsteroidal anti-inflammatory drug (NSAID)'
        ibuprofenProduct.save(flush: true)

        tylenolProduct.brandName = 'TYLENOL®'
        tylenolProduct.manufacturer = 'McNeil Consumer Healthcare'
        tylenolProduct.manufacturerName = 'Tylenol Extra Strength Acetaminophen 500 Mg 325 Caplets'
        tylenolProduct.manufacturerCode = 'TYL325'
        tylenolProduct.vendor = 'IDA'
        tylenolProduct.vendorCode = '025200'
        tylenolProduct.vendorName = 'Acetaminophen 325 mg, film coated'
        tylenolProduct.save(flush: true)


        // create some inventory items
        aspirinItem1 = DbHelper.findOrCreateInventoryItem(aspirinProduct, '1', new Date().plus(100))
        aspirinItem2 = DbHelper.findOrCreateInventoryItem(aspirinProduct, '2', new Date().plus(10))
        tylenolItem = DbHelper.findOrCreateInventoryItem(tylenolProduct, 'lot9383')
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
        when:
        inventoryLevelTestFixture()
        //def service = new InventoryService()
        def terms = ["Tylenol"]
        def result = service.getProductsByTermsAndCategories(terms, null, false, haitiInventory, 25, 0)
        then:
        assert result.contains(tylenolProduct)

        when:
        terms = ["Advil"]
        result = service.getProductsByTermsAndCategories(terms, null, false, haitiInventory, 25, 0)
        then:
        assert result.contains(advilProduct)
    }

    @Ignore // FIXME We removed the showHidden feature awhile back
    void test_getProductsByTermsAndCategoriesWithoutHiddenProductsWithInventoryLevelsNotSupported() {
        when:
        inventoryLevelTestFixture()
        //def service = new InventoryService()
        def terms = ["Tylenol"]
        def result = service.getProductsByTermsAndCategories(terms, null, false, bostonInventory, 25, 0)
        then:
        assert !result.contains(tylenolProduct)

        when:
        terms = ["Ibuprofen"]
        result = service.getProductsByTermsAndCategories(terms, null, false, bostonInventory, 25, 0)
        then:
        assert !result.contains(ibuprofenProduct)
    }

    @Ignore // FIXME We removed the showHidden feature awhile back
    void test_getProductsByTermsAndCategoriesWithoutHiddenProductsWithInventoryLevelsSupported() {
        when:
        inventoryLevelTestFixture()
        //def service = new InventoryService()
        def terms = ["Aspirin"]
        def result = service.getProductsByTermsAndCategories(terms, null, false, bostonInventory, 25, 0)
        then:
        assert result.contains(aspirinProduct)

        when:
        terms = ["Ibuprofen"]
        result = service.getProductsByTermsAndCategories(terms, null, false, bostonInventory, 25, 0)
        then:
        assert !result.contains(ibuprofenProduct)
    }

    void getProductsByTermsAndCategories_shouldFindByBrand() {
        when:
        inventoryLevelTestFixture()
        def terms = ["TYLENOL®"]
        def result = service.getProductsByTermsAndCategories(terms, null, false, haitiInventory, 25, 0)
        println result
        then:
        assert result.contains(tylenolProduct)
    }

    void getProductsByTermsAndCategories_shouldFindByManufacturer() {
        when:
        inventoryLevelTestFixture()
        def terms = ["McNeil"]
        def result = service.getProductsByTermsAndCategories(terms, null, false, haitiInventory, 25, 0)
        println result
        then:
        assert result.contains(tylenolProduct)

        when:
        terms = ["TYL325"]
        result = service.getProductsByTermsAndCategories(terms, null, false, haitiInventory, 25, 0)
        println result
        then:
        assert result.contains(tylenolProduct)
    }

    void getProductsByTermsAndCategories_shouldFindByVendor() {
        when:
        inventoryLevelTestFixture()
        def terms = ["IDA"]
        def result = service.getProductsByTermsAndCategories(terms, null, false, haitiInventory, 25, 0)
        println result
        then:
        assert result.contains(tylenolProduct)

        when:
        terms = ["025200"]
        result = service.getProductsByTermsAndCategories(terms, null, false, haitiInventory, 25, 0)
        println result
        then:
        assert result.contains(tylenolProduct)

        when:
        terms = ["Extra Strength"]
        result = service.getProductsByTermsAndCategories(terms, null, false, haitiInventory, 25, 0)
        println result
        then:
        assert result.contains(tylenolProduct)
    }

    private void productTagTestFixture() {
        when:
        basicTestFixture()
        User user = User.get(1)
        then:
        assertNotNull user
        println user
        when:
        aspirinProduct.addToTags(new Tag(tag: "thistag"))
        aspirinProduct.save(flush:true, failOnError:true)
        tylenolProduct.addToTags(new Tag(tag: "thattag"))
        tylenolProduct.save(flush:true, failOnError:true)
        then:
        assertEquals 1, aspirinProduct.tags.size()
        assertEquals 1, tylenolProduct.tags.size()
        assertEquals 2, Tag.list().size()
    }

    void test_getQuantityByProductMap() {
        when:
        transactionEntryTestFixture()
        def map = service.getQuantityByProductMap(TransactionEntry.list())

        then:
        assert map[aspirinProduct] == 97
        assert map[tylenolProduct] == 25
    }

    //todo: getQuantity is broken now, need to know why
    void getQuantity(){
        when:
        transactionEntryTestFixture()

        then:
        assert service.getQuantity(bostonInventory, (Location) null, aspirinItem1) == 94
        assert service.getQuantity(bostonInventory, (Location) null, aspirinItem2) == 3
        assert service.getQuantity(bostonInventory, (Location) null, tylenolItem) == 25
    }

    void getQuantity_shouldHandleTransactionsForSameDay(){
        when:
        transactionEntryTestFixture2()

        then:
        assert service.getQuantity(bostonInventory, (Location) null, aspirinItem1) == 94
        assert service.getQuantity(bostonInventory, (Location) null, aspirinItem2) == 3
        assert service.getQuantity(bostonInventory, (Location) null, tylenolItem) == 25
    }

    void test_getProductsQuantityForInventory(){
        when:
        transactionEntryTestFixture()
        def results = service.getQuantityForProducts(bostonInventory, [tylenolProduct.id])
        then:
        assert results[tylenolProduct.id] == 25
        assert results.keySet().size() == 1
    }

    void test_getProductsQuantityForInventoryWithEmptyProductArray(){
        when:
        transactionEntryTestFixture()
        def results = service.getQuantityForProducts(bostonInventory, [])
        then:
        assert results == [:]
    }

    void test_getQuantityByInventoryItemMap() {
        when:
        transactionEntryTestFixture()
        def map = service.getQuantityByInventoryItemMap(TransactionEntry.list())

        then:
        assert map[aspirinItem1] == 94
        assert map[aspirinItem2] == 3
        assert map[tylenolItem] == 25
    }

    void test_getInventoryItemsWithQuantity() {

        when:
        transactionEntryTestFixture()

        def products = [aspirinProduct, tylenolProduct]
        def inventoryItems = service.getInventoryItemsWithQuantity(products, bostonInventory)
        then:
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
        when:
        localTransferTestFixture()

        // a transaction that isn't of transfer in or transfer out type shouldn't be marked as valid
        service.validateForLocalTransfer(transaction1)
        then:
        assert transaction1.errors.hasFieldErrors("transactionType")

        // a transaction that's source or destination isn't a warehouse shouldn't pass validation //todo: need revist later; by Peter
//        assert service.isValidForLocalTransfer(transaction2) == false
//        assert service.isValidForLocalTransfer(transaction3) == false

        // transfer in/transfer out transactions associated with warehouses should pass validation
        when:
        service.validateForLocalTransfer(transaction4)
        then:
        assert !transaction4.hasErrors()
        when:
        service.validateForLocalTransfer(transaction5)
        then:
        assert !transaction5.hasErrors()
    }

    void test_saveLocalTransfer_shouldCreateNewLocalTransfer() {
        when:
        localTransferTestFixture()
        def warehouse = bostonLocation

        then:
        assert warehouse.inventory != null
        assert transaction4.inventory != null

        // save a local transaction based on a Transfer In Transaction
        when:
        service.saveLocalTransfer(transaction4)

        // confirm that this transaction is now associated with a local transfer
        then:
        assert service.isLocalTransfer(transaction4) == true
        when:
        def localTransfer = service.getLocalTransfer(transaction4)

        // confirm that the local transfer has the appropriate source and destination transaction
        then:
        assert localTransfer.destinationTransaction == transaction4

        when:
        def newTransaction = localTransfer.sourceTransaction
        then:
        assert newTransaction.transactionType ==  transactionType_transferOut
        assert newTransaction.inventory == haitiInventory
        assert newTransaction.source == null
        assert newTransaction.destination == bostonLocation

        // now try a local transaction based on a Transfer Out Transaction
        when:
        service.saveLocalTransfer(transaction5)

        // confirm that this transaction is now associated with a local transfer
        then:
        assert service.isLocalTransfer(transaction5) == true
        when:
        localTransfer = service.getLocalTransfer(transaction5)

        // confirm that the local transfer has the appropriate source and destination transaction
        then:
        assert localTransfer.sourceTransaction == transaction5
        when:
        newTransaction = localTransfer.destinationTransaction
        then:
        assert newTransaction.transactionType == transactionType_transferIn
        assert newTransaction.inventory == haitiInventory
        assert newTransaction.source == bostonLocation
        assert newTransaction.destination == null

    }

    void test_saveLocalTransfer_shouldEditExistingLocalTransfer() {
        when:
        localTransferTestFixture()
        def baseTransaction = transaction4

        // first create a local transfer
        service.saveLocalTransfer(baseTransaction)

        // now modify the base transaction
        baseTransaction.inventory = haitiInventory
        baseTransaction.source = bostonLocation

        // resave the local transfer
        service.saveLocalTransfer(baseTransaction)

        // now check that the local transfer transactions have been updated accordingly
        def localTransfer = service.getLocalTransfer(baseTransaction)
        then:
        assert localTransfer.destinationTransaction == baseTransaction

        when:
        def newTransaction = localTransfer.sourceTransaction
        then:
        assert newTransaction.transactionType == transactionType_transferOut
        assert newTransaction.inventory == bostonInventory
        assert newTransaction.source == null
        assert newTransaction.destination == haitiLocation
    }



    void test_getProductsByTags() {
        when:
        productTagTestFixture()
        def tags = ["thistag", "thattag"].collect { Tag.findByTag(it).id }

        def results = service.getProductsByTags(tags, 10, 0)
        then:
        assertEquals 2, results.size()
    }

    void test_getProductsByTag() {
        when:
        productTagTestFixture()
        def tags = Tag.list()
        then:
        assertEquals 2, tags.size()
        when:
        def results = service.getProductsByTag("thistag")
        then:
        assertEquals 1, results.size()
    }

    void getProductsByTag_shouldNotFailDueToSQLGrammarException() {
        when:
        productTagTestFixture()
        def tags = ["thistag"].collect { Tag.findByTag(it).id }
        def results = service.getProductsByTags(tags, -1, 0)
        then:
        assertEquals 1, results.size()
    }


    void test_getProductsByTermsAndCategoriesAndLotNumberWithProductSearchTerm() {
        when:
        transactionEntryTestFixture()
        def terms = ["Asp", "rin"]
        def results = service.getProductsByTermsAndCategories(terms, null, true, bostonInventory,  25, 0)
        then:
        assert results.contains(aspirinProduct)
    }


    void test_getProductsByTermsAndCategoriesAndLotNumberWithLotNumberSearchTerm() {
        when:
        basicTestFixture()
        def terms = ["lot9383"]
        def results = service.getProductsByTermsAndCategories(terms, null, true, bostonInventory, 1000, 0)
        then:
        assert results.contains(tylenolProduct)
    }

    void test_getProductsByTermsAndCategoriesWithProductName() {
        when:
        basicTestFixture()
        def terms = ["Ibuprofen"]
        def results = service.getProductsByTermsAndCategories(terms, null, true, bostonInventory, 25, 0)
        then:
        assert results.contains(ibuprofenProduct)
    }

    void test_getProductsByTermsAndCategoriesWithDescription() {
        when:
        basicTestFixture()
        def terms = ["NSAID"]
        def results = service.getProductsByTermsAndCategories(terms, null, true, bostonInventory, 25, 0)
        then:
        assert results.contains(ibuprofenProduct)
    }

    void importInventory_shouldSaveRecordInventoryTransaction() {

        when:
        def location = Location.list()[0]
        def resource = new ClassPathResource("resources/inventory2.xls")
        def file = resource.getFile()

        then:
        assert file.exists()

        when:
        def command = new ImportDataCommand()
        command.location = location
        command.importFile = file
        command.date = new Date()

        def data = inventoryImportDataService.importData(command)

        def productCodes = data.collect { it.productCode }.unique()
        productCodes.each {
            def product = Product.findByProductCode(it)
            if (product) {
                def quantityOnHand = service.getQuantityOnHand(location, product)
                println "Product ${product.productCode} ${product.name}: ${quantityOnHand}"
            }
        }
        then:
        assert true
    }
}
