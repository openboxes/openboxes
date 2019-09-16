package testutils

import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.PartyType
import org.pih.warehouse.core.Person
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
import org.pih.warehouse.product.ProductType
import org.pih.warehouse.product.ProductTypeCode
import org.pih.warehouse.shipping.Container
import org.pih.warehouse.shipping.ContainerType
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentType

class DbHelper {

    static Category findOrCreateCategory(String name) {
        Category.findByName(name) ?: new Category(name: name).save(failOnError: true, flush: true)
    }

    static Location findOrCreateLocation(String name, LocationType locationType = null, String organizationName = null) {
        Location.findByName(name) ?: new Location(
            locationType: locationType ?: LocationType.get(Constants.WAREHOUSE_LOCATION_TYPE_ID),
            name: name,
            organization: findOrCreateOrganization(organizationName ?: "DbHelper's Dummy Organization")
        ).save(failOnError: true, flush: true)
    }

    static Location findOrCreateLocationWithInventory(String name) {
        Location warehouse = findOrCreateLocation(name)
        createInventory(warehouse)
        return warehouse
    }

    static LocationType findOrCreateLocationType(String name) {
        LocationType.findByName(name) ?: new LocationType(name: name).save(failOnError: true, flush: true)
    }

    static InventoryItem findOrCreateInventoryItem(Product product, String lotNumber, Date expirationDate = new Date().plus(30)) {
        InventoryItem.findByProductAndLotNumber(product, lotNumber) ?: new InventoryItem(
            expirationDate: expirationDate,
            lotNumber: lotNumber,
            product: product,
        ).save(failOnError: true, flush: true)
    }

    static Organization findOrCreateOrganization(String name) {
        Organization.findByName(name) ?: new Organization(
            code: name[0..3],
            name: name,
            partyType: PartyType.findByCode(Constants.DEFAULT_ORGANIZATION_CODE)
        ).save(failOnError: true, flush: true)
    }

    static ProductType findOrCreateProductType(String name) {
        ProductType.findByName(name) ?: new ProductType(
            name: name,
            productTypeCode: ProductTypeCode.GOOD
        ).save(failOnError: true, flush: true)
    }

    static Product findOrCreateProduct(String productName, String categoryName = 'Medicines') {
        Product.findByName(productName) ?: new Product(
            category: findOrCreateCategory(categoryName),
            name: productName,
            productCode: productName,
            productType: findOrCreateProductType("Default"),
        ).save(failOnError: true, flush: true)
    }

    static ProductGroup findOrCreateProductGroup(String groupName, String categoryName) {
        ProductGroup.findByName(groupName) ?: new ProductGroup(
            category: findOrCreateCategory(categoryName),
            name: groupName,
        ).save(failOnError: true, flush: true)
    }

    static Product findOrCreateProductWithGroups(String name, List<String> groupNames) {
        Product product = findOrCreateProduct(name, 'Integration')
        groupNames.each {
            ProductGroup productGroup = findOrCreateProductGroup(it, 'Integration')
            product.addToProductGroups(productGroup)
            productGroup.addToProducts(product).save(failOnError: true, flush: true)
        }

        product.save(failOnError: true, flush: true)
    }

    static Product findOrCreateProductWithTags(String name, List<String> tagNames) {
        Product product = findOrCreateProduct(name, 'Integration')
        tagNames.each {
            product.addToTags(findOrCreateTag(it)).save(failOnError: true, flush: true)
        }
        return product
    }

    static Tag findOrCreateTag(String tagName) {
        Tag.findByTag(tagName) ?: new Tag(tag: tagName).save(failOnError: true, flush: true)
    }

    static User findOrCreateUser(firstName, lastName, email, username, password, active) {
        User.findByUsernameOrEmail(username, email) ?: new User(
            active: active,
            email: email,
            firstName: firstName,
            lastName: lastName,
            password: password,
            username: username
        ).save(failOnError: true, flush: true)
    }

    static User findOrCreateAdminUser(firstName, lastName, email, username, password, active) {
        User user = findOrCreateUser(firstName, lastName, email, username, password, active)
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
        transactionEntry.inventoryItem = findOrCreateInventoryItem(product, lotNumber, expirationDate)
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
