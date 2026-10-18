package org.pih.warehouse.api.regression.platform.common.base

import org.pih.warehouse.common.base.*
import grails.util.GrailsWebMockUtil
import grails.util.Holders
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.request.RequestContextHolder

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.common.domain.builder.product.ProductTestBuilder
import org.pih.warehouse.common.util.RandomUtil
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionCode
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.common.domain.builder.product.CategoryTestBuilder
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product

/**
 * Base class for specs that call services directly against the database (no HTTP): helpers that create a depot,
 * products and stock in their own transactions, and that run service calls as the admin user at a location (the
 * services read the current user and location from AuthService).
 */
abstract class ServiceIntegrationSpec extends IntegrationSpec {

    public static final String BIN_LOCATION_TYPE_ID = 'cab2b4f35ba2d867015ba2e17e390001'

    AuthService authService
    ProductAvailabilityService productAvailabilityService

    RandomUtil randomUtil = new RandomUtil()

    /**
     * Runs the work in a transaction as the admin user, at the given location (none when null), within a mock web
     * request when none is bound (some services read the locale from the session).
     */
    protected <T> T asAdminAt(Location location, Closure<T> work) {
        boolean boundRequest = false
        if (RequestContextHolder.getRequestAttributes() == null) {
            GrailsWebMockUtil.bindMockWebRequest((WebApplicationContext) Holders.grailsApplication.mainContext)
            boundRequest = true
        }
        try {
            // The current user stays set until the transaction has committed: entities flushed at commit read it
            return (T) Location.withTransaction {
                authService.setCurrentUser(User.findByUsername('admin'))
                authService.setCurrentLocation(location?.id ? Location.get(location.id) : null)
                return work.call()
            }
        } finally {
            authService.setCurrentUser(null)
            authService.setCurrentLocation(null)
            if (boundRequest) {
                RequestContextHolder.resetRequestAttributes()
            }
        }
    }

    /** Runs the work in a new transaction that commits when it returns. */
    protected <T> T committed(Closure<T> work) {
        return (T) Location.withNewTransaction { work.call() }
    }

    protected String uniqueName(String prefix) {
        return randomUtil.randomStringFieldValue(prefix)
    }

    /** A depot of the main organization, committed. */
    protected Location newDepot(String prefix = 'Depot') {
        return committed {
            new LocationTestBuilder().name(uniqueName(prefix)).asFacility().build(true)
        }
    }

    /**
     * A top-level location of the given type, with an inventory (saving a location refreshes the product availability
     * of its inventory), committed.
     */
    protected Location newLocation(String prefix, String locationTypeId, List<String> activities = null, Location parent = null) {
        return committed {
            String name = uniqueName(prefix)
            Location location = new Location(name: name, locationType: LocationType.get(locationTypeId), active: true,
                    organization: Location.get('1').organization, parentLocation: parent?.id ? Location.get(parent.id) : null)
            location.inventory = new Inventory(warehouse: location)
            activities?.each { location.addToSupportedActivities(it) }
            location.save(failOnError: true, flush: true)
        }
    }

    /** A bin location of the given depot, committed. */
    protected Location newBin(Location depot, String name, List<String> activities = null) {
        return committed {
            Location bin = new Location(name: name, locationNumber: name, parentLocation: Location.get(depot.id),
                    locationType: LocationType.get(BIN_LOCATION_TYPE_ID), active: true)
            if (activities != null) {
                activities.each { bin.addToSupportedActivities(it) }
            }
            bin.save(failOnError: true, flush: true)
        }
    }

    /** A product in the root category (created when the database has none yet), committed. */
    protected Product newProduct(Map overrides = [:]) {
        committed {
            Category.getRootCategory() ?: new CategoryTestBuilder().name('Test Root Category').rootCategory().build(true)
        }
        return committed {
            ProductTestBuilder builder = new ProductTestBuilder().randomizedName()
            if (overrides.name) {
                builder.name(overrides.name as String)
            }
            Product product = builder.build(false)
            for (Map.Entry entry : overrides.entrySet()) {
                product.setProperty(entry.key as String, entry.value instanceof GString ? entry.value.toString() : entry.value)
            }
            product.save(failOnError: true, flush: true)
        }
    }

    // The default baseline date of stock(): one value for the whole feature, so its baselines can share a transaction
    protected final Date defaultStockDate = new Date() - 1

    // Baseline transactions made by stock(), by depot, product and date
    protected final Map<String, String> baselineTransactionIds = [:]

    /**
     * Records the quantity of a lot of the product in the depot (an inventory baseline entry, dated the day before
     * by default) and refreshes the product availability of the depot.
     *
     * A baseline (any PRODUCT_INVENTORY transaction) is a product-wide count: the latest one replaces every earlier count of the product. So the lots of a
     * product stocked as baselines at the same date go into one transaction, as in a real count. Separate baselines
     * with the same date would count all lots or only the last one, depending on whether they were saved in the same
     * second.
     */
    protected InventoryItem stock(Location depot, Product product, String lotNumber, Date expirationDate, int quantity,
                                  Location bin = null, Date transactionDate = null,
                                  String transactionTypeId = Constants.INVENTORY_BASELINE_TRANSACTION_TYPE_ID) {
        Date date = transactionDate ?: defaultStockDate
        String baselineKey = "${depot.id}:${product.id}:${date.time}"
        InventoryItem inventoryItem = committed {
            boolean baseline = TransactionType.get(transactionTypeId).transactionCode == TransactionCode.PRODUCT_INVENTORY
            InventoryItem item = InventoryItem.findByProductAndLotNumber(Product.get(product.id), lotNumber) ?:
                    new InventoryItem(product: Product.get(product.id), lotNumber: lotNumber, expirationDate: expirationDate)
                            .save(failOnError: true, flush: true)
            Transaction transaction = baseline && baselineTransactionIds[baselineKey] ?
                    Transaction.get(baselineTransactionIds[baselineKey]) :
                    new Transaction(
                            inventory: Location.get(depot.id).inventory,
                            transactionType: TransactionType.get(transactionTypeId),
                            transactionDate: date,
                            transactionNumber: uniqueName('T'))
            transaction.disableRefresh = true
            transaction.addToTransactionEntries(new TransactionEntry(product: item.product, inventoryItem: item, quantity: quantity,
                    binLocation: bin?.id ? Location.get(bin.id) : null))
            transaction.save(failOnError: true, flush: true)
            if (baseline) {
                baselineTransactionIds[baselineKey] = transaction.id
            }
            return item
        }
        committed {
            productAvailabilityService.refreshProductAvailability(Location.get(depot.id), Product.get(product.id), false)
        }
        return inventoryItem
    }
}
