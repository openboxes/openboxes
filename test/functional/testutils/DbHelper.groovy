/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package testutils

import org.pih.warehouse.core.User
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.Category
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.core.Constants


class DbHelper {

    static User CreateUserIfRequired(userName, password) {
        def user =  User.findByName(userName)
        if(!user)
          user = new User(username: userName)
        user.password = password
        user.locale = 'en'
        user.warehouse = Location.findByName("Boston Headquarters")
        user.active = true
        user.save(flush: true)
        user
    }

    static InventoryItem CreateProductInInventory(String productName, int quantity) {
        Product product = new Product()
        product.name = productName
        product.category = Category.findByName("Medicines")
        product.manufacturer = "TWTest"
        product.manufacturerCode ="TestABC"
        product.save(flush:true)

        InventoryItem item = new InventoryItem()
        item.product = product
        item.lotNumber = "lot57"
        item.expirationDate =  new Date().plus(30)
        item.save(flush:true)

        Location boston =  Location.findByName("Boston Headquarters");


        Transaction transaction = new Transaction()
        transaction.transactionDate = new Date()
        transaction.inventory = boston.inventory
	    transaction.transactionType = TransactionType.get(Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID)

        TransactionEntry transactionEntry = new TransactionEntry()
        transactionEntry.quantity = quantity
		transactionEntry.inventoryItem = item

        transaction.addToTransactionEntries(transactionEntry)
        transaction.save(flush:true)

        item
    }
}
