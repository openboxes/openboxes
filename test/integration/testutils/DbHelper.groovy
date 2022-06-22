package testutils

import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.PartyType
import org.pih.warehouse.core.Role
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.inventory.InventoryStatus
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductGroup

class DbHelper {

    static Category getOrCreateCategory(String name) {
        Category.findByName(name) ?: new Category(name: name).save(failOnError: true, flush: true)
    }

    static Location getOrCreateLocation(String name, LocationType locationType = null, String organizationName = null) {
        Location.findByName(name) ?: new Location(
            locationType: locationType ?: LocationType.get(Constants.WAREHOUSE_LOCATION_TYPE_ID),
            name: name,
            organization: getOrCreateOrganization(organizationName ?: "DbHelper's Dummy Organization")
        ).save(failOnError: true, flush: true)
    }

    static Location getOrCreateLocationWithInventory(String name) {
        Location warehouse = getOrCreateLocation(name)
        createInventory(warehouse)
        return warehouse
    }

    static LocationType getOrCreateLocationType(String name) {
        LocationType.findByName(name) ?: new LocationType(name: name).save(failOnError: true, flush: true)
    }

    static InventoryItem getOrCreateInventoryItem(Product product, String lotNumber, Date expirationDate = new Date().plus(30)) {
        InventoryItem.findByProductAndLotNumber(product, lotNumber) ?: new InventoryItem(
            expirationDate: expirationDate,
            lotNumber: lotNumber,
            product: product,
        ).save(failOnError: true, flush: true)
    }

    static Organization getOrCreateOrganization(String name) {
        Organization.findByName(name) ?: new Organization(
            code: name[0..3],
            name: name,
            partyType: PartyType.findByCode(Constants.DEFAULT_ORGANIZATION_CODE)
        ).save(failOnError: true, flush: true)
    }

    static Product getOrCreateProduct(String productName, String categoryName = 'Medicines') {
        Product.findByName(productName) ?: new Product(
            category: getOrCreateCategory(categoryName),
            name: productName,
            productCode: productName,
        ).save(failOnError: true, flush: true)
    }

    static ProductGroup getOrCreateProductGroup(String groupName, String categoryName) {
        ProductGroup.findByName(groupName) ?: new ProductGroup(
            category: getOrCreateCategory(categoryName),
            name: groupName,
        ).save(failOnError: true, flush: true)
    }

    static Product getOrCreateProductWithGroups(String name, List<String> groupNames) {
        Product product = getOrCreateProduct(name, 'Integration')
        groupNames.each {
            ProductGroup productGroup = getOrCreateProductGroup(it, 'Integration')
            product.addToProductGroups(productGroup)
            productGroup.addToProducts(product).save(failOnError: true, flush: true)
        }

        product.save(failOnError: true, flush: true)
    }

    static Product getOrCreateProductWithTags(String name, List<String> tagNames) {
        Product product = getOrCreateProduct(name, 'Integration')
        tagNames.each {
            product.addToTags(getOrCreateTag(it)).save(failOnError: true, flush: true)
        }
        return product
    }

    static Tag getOrCreateTag(String tagName) {
        Tag.findByTag(tagName) ?: new Tag(tag: tagName).save(failOnError: true, flush: true)
    }

    static User getOrCreateUser(firstName, lastName, email, username, password, active) {
        User.findByUsernameOrEmail(username, email) ?: new User(
            active: active,
            email: email,
            firstName: firstName,
            lastName: lastName,
            password: password,
            username: username
        ).save(failOnError: true, flush: true)
    }

    static User getOrCreateAdminUser(firstName, lastName, email, username, password, active) {
        User user = getOrCreateUser(firstName, lastName, email, username, password, active)
        Role admin = Role.findByRoleType(RoleType.ROLE_ADMIN)
        assert admin
        user.addToRoles(admin).save(failOnError: true, flush: true)
    }

    static Inventory createInventory(Location location) {
        def inventory = new Inventory(warehouse: location)
        location.inventory = inventory
        inventory.save(failOnError: true, flush: true)
        location.save(failOnError: true, flush: true)
        inventory
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

    static recordProductInventory(Product product, Location location, String lotNumber, Date expirationDate, int quantity, Date transactionDate) {
        def transactionType = TransactionType.get(Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID)
        if (location.inventory == null) {
            createInventory(location)
        }
        def transaction = new Transaction(inventory: location.inventory, transactionType: transactionType, createdBy: User.get(2), transactionDate: transactionDate)
        TransactionEntry transactionEntry = new TransactionEntry()
        transactionEntry.quantity = quantity
        transactionEntry.inventoryItem = getOrCreateInventoryItem(product, lotNumber, expirationDate)
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
}
