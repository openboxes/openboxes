package testutils

// import grails.validation.ValidationException;

import org.pih.warehouse.product.*
import org.pih.warehouse.shipping.Container;
import org.pih.warehouse.shipping.ContainerType;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.shipping.ShipmentItem;
import org.pih.warehouse.shipping.ShipmentType;
import org.pih.warehouse.inventory.*
import org.pih.warehouse.core.*


class DbHelper {

    static Location creatLocationIfNotExist(def name, def locationType) {
        def existingOne = Location.findByName(name)
        if (existingOne) return existingOne
        def newOne = new Location(name: name, locationType: locationType)
        newOne.save(flush: true)
        newOne
    }

    static Inventory createInventory(def location) {
        def inventory = new Inventory(warehouse: location)
        location.inventory = inventory
        inventory.save(flush: true)
        location.save(flush: true)
        inventory
    }

    static Category createCategoryIfNotExists(name) {
        def category = Category.findByName(name)
        if (category) return category
        category = new Category(name: name).save();
        category
    }

	static Product createProductIfNotExists(productName){
		return DbHelper.createProductIfNotExists(productName, "Medicines")
	}

	static Product createProductIfNotExists(productName, categoryName){
		def existingOne = Product.findByName(productName)
		if(existingOne) return existingOne
		def newOne = new Product(name:productName, productCode: productName)
		newOne.category = createCategoryIfNotExists(categoryName)
		newOne.save(flush:true)
		newOne
	}

    static Product createProductWithGroups(def name, def groupNames) {
        Product product = Product.findByName(name)
        if (!product) {
            product = new Product(name: name, productCode: name, category: createCategoryIfNotExists("Integration"))
            product.save(failOnError: true, flush: true)
        }
        groupNames.each { groupName ->
            def productGroup = ProductGroup.findByName(groupName)
            if (!productGroup) {
                productGroup = new ProductGroup(name: groupName)
                productGroup.category = createCategoryIfNotExists("Integration")
                productGroup.save(failOnError: true, flush: true)
            }
            productGroup.addToProducts(product)
            productGroup.save(failOnError: true, flush: true)
            product.addToProductGroups(productGroup)
            product.save(failOnError: true, flush: true)
        }

        product
    }

    static InventoryLevel createInventoryLevel(Product product, Location location, String binLocation, InventoryStatus status, int min, int reorder, int max) {
        def inventoryLevel = new InventoryLevel()
        if (product && location.inventory) {
            inventoryLevel.product = product
            inventoryLevel.binLocation = binLocation
            inventoryLevel.inventory = location.inventory
            inventoryLevel.minQuantity = min
            inventoryLevel.reorderQuantity = reorder
            inventoryLevel.maxQuantity = max
            inventoryLevel.save(failOnError: true)
        }
        return inventoryLevel

    }

    static InventoryItem createInventoryItem(Product product, String lotNumber, expirationDate = new Date().plus(30)) {
        def existingOne = InventoryItem.findByProductAndLotNumber(product, lotNumber)
        if (existingOne) return existingOne
        InventoryItem item = new InventoryItem()
        item.product = product
        item.lotNumber = lotNumber
        item.expirationDate = expirationDate
        item.save(flush: true)
        item
    }
	
    static recordProductInventory(Product product, Location location, String lotNumber, Date expirationDate, int quantity, Date transactionDate) {
        def transactionType = TransactionType.get(Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID)
        if (location.inventory == null) {
            createInventory(location)
        }
        def transaction = new Transaction(inventory: location.inventory, transactionType: transactionType, createdBy: User.get(2), transactionDate: transactionDate)
        TransactionEntry transactionEntry = new TransactionEntry()
        transactionEntry.quantity = quantity
        transactionEntry.inventoryItem = createInventoryItem(product, lotNumber, expirationDate)
        transaction.addToTransactionEntries(transactionEntry)
        transaction.save(failOnError: true, flush: true)

    }

    static recordInventory(Product product, Location location, String lotNumber, Date expirationDate, int quantity, Date transactionDate) {
        def transactionType = TransactionType.get(Constants.INVENTORY_TRANSACTION_TYPE_ID)
        if (location.inventory == null) {
            createInventory(location)
        }
        def transaction = new Transaction(inventory: location.inventory, transactionType: transactionType, createdBy: User.get(2), transactionDate: transactionDate)
        TransactionEntry transactionEntry = new TransactionEntry()
        transactionEntry.quantity = quantity
        transactionEntry.inventoryItem = createInventoryItem(product, lotNumber, expirationDate)
        transaction.addToTransactionEntries(transactionEntry)
        transaction.save(failOnError: true, flush: true)

    }



    static transferStock(Product product, Location fromLocation, String lotNumber, int quantity, Date transactionDate, Location toLocation) {
        def transactionType = TransactionType.get(Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID)
        if (fromLocation.inventory == null) {
            createInventory(fromLocation)
        }
        def transaction = new Transaction(inventory: fromLocation.inventory, transactionType: transactionType, createdBy: User.get(2), transactionDate: transactionDate, destination: toLocation)
        TransactionEntry transactionEntry = new TransactionEntry()
        transactionEntry.quantity = quantity
        transactionEntry.inventoryItem = getInventoryItem(product, lotNumber)
        transaction.addToTransactionEntries(transactionEntry)
        transaction.save(failOnError: true, flush: true)

    }

    static getInventoryItem(Product product, String lotNumber) {
        return InventoryItem.findByProductAndLotNumber(product, lotNumber)
    }
	
	static Product createProductWithTags(name, tags){
		Product product = Product.findByName(name)
		if(!product){
			product = new Product(name: name, productCode: name, category: createCategoryIfNotExists("Integration"))
			product.save(failOnError:true,flush:true)
		}
		tags.each{ tagName ->
			def tag = Tag.findByTag(tagName)
			if(!tag){
				tag = new Tag(tag: tagName)				
				//tag.save(failOnError:true,flush:true)
			}
			product.addToTags(tag)
			product.save(failOnError:true,flush:true)
		}
		return product
	}
	
	static Tag createTag(tagName) { 
		def tag = Tag.findByTag(tagName)
		if (!tag) { 
			tag = new Tag(tag: tagName)
			tag.save(failOnError:true,flush:true)
		}
		return tag		
	}
	
	static Person createPerson(firstName, lastName, email) { 
		Person person = Person.findByEmail(email)
		if (!person) {
			person = new Person(firstName: firstName, lastName: lastName, email: email)
			person.save(failOnError:true, flush:true)
		}
		return person
	}
	
	
	static User createAdmin(firstName, lastName, email, username, password, active) { 
		User user = User.findByUsernameOrEmail(username, email)
		if (!user) {
			user = new User(firstName: firstName, lastName: lastName, email: email, username: username, password: password, active: active)
			
			Role admin = Role.findByRoleType(RoleType.ROLE_ADMIN)
			assert admin
			user.addToRoles(admin)
			
			user.save(failOnError:true, flush:true)
		}
		return user
	}
	
	static Shipment createShipment(name, shipmentType, origin, destination) { 
		Shipment shipment = Shipment.findByName(name)
		if (!shipment) {  
			shipment = new Shipment()
			shipment.name = name
			shipment.expectedDeliveryDate = new Date();
			shipment.expectedShippingDate = new Date();
			shipment.shipmentType = ShipmentType.findByName(shipmentType);
			shipment.origin = Location.findByName(origin)
			shipment.destination = Location.findByName(destination)
			shipment.save(failOnError:true, flush: true)
		}
		return shipment	
	}
	
	
	static Container createPallet(shipment, name) { 
		ContainerType pallet = ContainerType.findByName("Pallet")
		Container container = new Container(name: name, containerType: pallet)		
		shipment.addToContainers(container)
		shipment.save(flush:true)
		return container
	}
	
	
	static ShipmentItem createShipmentItem(shipment, pallet, product, lotNumber, quantity) { 
		InventoryItem inventoryItem = InventoryItem.findByProductAndLotNumber(product, lotNumber)
		ShipmentItem shipmentItem = new ShipmentItem()
		shipmentItem.container = pallet
		shipmentItem.inventoryItem = inventoryItem
		shipmentItem.quantity = quantity
		shipment.addToShipmentItems(shipmentItem)
		shipment.save(flush:true)
		return shipmentItem
	}
	
}
