/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 * */
package org.pih.warehouse.core

import org.pih.warehouse.inventory.TransactionType

class BaseIntegrationTest extends GroovyTestCase {

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
    protected def  aspirinItem1
    protected def  aspirinItem2
    protected def tylenolItem

    protected void setUp() {
        super.setUp()
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
        aspirinProduct = DbHelper.creatProductIfNotExist("Aspirin" + UUID.randomUUID().toString()[0..5])
        tylenolProduct = DbHelper.creatProductIfNotExist("Tylenol" + UUID.randomUUID().toString()[0..5])

        // create some inventory items
        aspirinItem1 = DbHelper.createInventoryItem(aspirinProduct, "1")
        aspirinItem2 = DbHelper.createInventoryItem(aspirinProduct, "2")
        tylenolItem = DbHelper.createInventoryItem(tylenolProduct, "1")
    }



    void testDataHasBeenInitialized() {

        assert transactionType_consumptionDebit.id != null
        assert transactionType_inventory.id != null
        assert transactionType_productInventory.id != null
        assert transactionType_transferIn.id != null
        assert transactionType_transferOut.id != null


        assert bostonLocation.id != null
        assert haitiLocation.id != null
        assert warehouseLocationType.id != null
        assert supplierLocationType.id != null
        assert acmeLocation.id != null
        assert bostonInventory.id != null
        assert haitiInventory.id != null
        assert aspirinProduct.id != null
        assert tylenolProduct.id != null
        assert aspirinItem1.id != null
        assert aspirinItem2.id != null
        assert tylenolItem.id != null

    }

}
