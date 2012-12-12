package testutils

import grails.validation.ValidationException;

import org.pih.warehouse.product.*
import org.pih.warehouse.inventory.*
import org.pih.warehouse.core.*


class DbHelper {

    static Location creatLocationIfNotExist(def name, def locationType){
        def existingOne = Location.findByName(name)
        if(existingOne) return existingOne
        def newOne = new Location(name:name, locationType: locationType)
        newOne.save(flush:true)
        newOne
    }

    static Inventory createInventory(def location){
        def inventory = new Inventory(warehouse:location)
        location.inventory = inventory
        inventory.save(flush:true)
        location.save(flush:true)
        inventory
    }

	static Category createCategoryIfNotExists(name) { 
		def category = Category.findByName(name)
		if (category) return category
		category = new Category(name: name).save();
		category
	}

	static Product creatProductIfNotExist(def name){
        def existingOne = Product.findByName(name)
        if(existingOne) return existingOne
        def newOne = new Product(name:name)
        newOne.category = createCategoryIfNotExists("Medicines")
        newOne.save(flush:true)
        newOne
    }

  static Product createProductWithGroups(def name, def groupNames){
      Product product = Product.findByName(name)
      if(!product){
         product = new Product(name: name, category: createCategoryIfNotExists("Integration"))
         product.save(failOnError:true,flush:true)
      }
      groupNames.each{ groupName ->
      def productGroup = ProductGroup.findByDescription(groupName)
        if(!productGroup){
            productGroup = new ProductGroup(description: groupName)
            productGroup.category = createCategoryIfNotExists("Integration")
            productGroup.save(failOnError:true,flush:true)
        }
        productGroup.addToProducts(product)
        productGroup.save(failOnError:true,flush:true)
        product.addToProductGroups(productGroup)
        product.save(failOnError:true,flush:true)
      }

      product
  }

    static InventoryItem createInventoryItem(Product product, String lotNumber, expirationDate = new Date().plus(30) ) {
        def existingOne = InventoryItem.findByProductAndLotNumber(product, lotNumber)
        if(existingOne) return existingOne
        InventoryItem item = new InventoryItem()
        item.product = product
        item.lotNumber = lotNumber
        item.expirationDate =  expirationDate
        item.save(flush:true)
        item
    }

    static recordInventory(Product product, Location location, String lotNumber, Date expirationDate, int quantity, Date transactionDate){
      def transactionType =  TransactionType.get(Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID)

      def transaction = new Transaction(inventory: location.inventory, transactionType: transactionType, createdBy: User.get(2), transactionDate: transactionDate)
      TransactionEntry transactionEntry = new TransactionEntry()
      transactionEntry.quantity = quantity
		  transactionEntry.inventoryItem = createInventoryItem(product, lotNumber, expirationDate)
      transaction.addToTransactionEntries(transactionEntry)
      transaction.save(failOnError:true, flush:true)

    }
}
