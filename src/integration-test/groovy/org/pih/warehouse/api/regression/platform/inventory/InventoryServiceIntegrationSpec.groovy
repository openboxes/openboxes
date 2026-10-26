package org.pih.warehouse.api.regression.platform.inventory

import spock.lang.PendingFeature


import org.pih.warehouse.inventory.*
import grails.util.GrailsWebMockUtil
import grails.validation.ValidationException
import grails.util.Holders
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.request.RequestContextHolder
import spock.lang.Shared

import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.common.base.IntegrationSpec
import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.common.domain.builder.product.CategoryTestBuilder
import org.pih.warehouse.common.domain.builder.product.ProductTestBuilder
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.User
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductCatalog
import org.pih.warehouse.product.ProductCatalogItem

/**
 * The inventory service against a real database: a depot with two bins holds two lots of one product (counted,
 * then partly consumed and expired) and a default lot of another product, and a second depot holds a little of the
 * first lot. Quantities on hand in the depot: lot A 7 in bin 1, lot B 4 in bin 2, the other product 8 without a bin.
 */
class InventoryServiceIntegrationSpec extends IntegrationSpec {

    InventoryService inventoryService
    ProductAvailabilityService productAvailabilityService
    AuthService authService
    def sessionFactory

    @Shared Map<String, String> ids = [:]
    @Shared Map<String, Date> dates = [:]

    private static final long DAY = 24L * 60 * 60 * 1000

    /** Whole seconds, as the database stores them. */
    private static Date daysAgo(int days) {
        long time = System.currentTimeMillis() - days * DAY
        return new Date(time - (time % 1000))
    }

    /**
     * Runs the closure in a new transaction. The current user (kept per thread) is loaded again in the new session,
     * so that the audit fields set on save refer to an instance of this session.
     */
    private <T> T tx(Closure<T> closure) {
        return Product.withNewTransaction {
            if (AuthService.currentUser) {
                authService.currentUser = AuthService.currentUser
            }
            return closure.call()
        }
    }

    private Location depot() { Location.get(ids.depot) }
    private Location otherDepot() { Location.get(ids.otherDepot) }
    private Location bin1() { Location.get(ids.bin1) }
    private Location bin2() { Location.get(ids.bin2) }
    private Product alpha() { Product.get(ids.alpha) }
    private Product beta() { Product.get(ids.beta) }
    private Product gamma() { Product.get(ids.gamma) }
    private InventoryItem lotA() { InventoryItem.get(ids.lotA) }
    private InventoryItem lotB() { InventoryItem.get(ids.lotB) }
    private InventoryItem betaDefault() { InventoryItem.get(ids.betaDefault) }

    private static Transaction transaction(Location location, String typeId, Date date, String number) {
        return new Transaction(inventory: location.inventory, transactionType: TransactionType.get(typeId),
                transactionDate: date, transactionNumber: number, disableRefresh: true)
    }

    void setup() {
        if (ids) {
            return
        }
        Map<String, String> created = [:]
        Map<String, Date> when = [count: daysAgo(10), credit: daysAgo(5), otherCredit: daysAgo(4),
                                  consumption: daysAgo(3), expiry: daysAgo(2)]
        tx {
            authService.currentUser = User.findByUsername('admin')
            String suffix = UUID.randomUUID().toString().take(8)
            created.suffix = suffix
            Location depot = new LocationTestBuilder().asFacility().name("Inventory depot ${suffix}").build(true)
            Location otherDepot = new LocationTestBuilder().asFacility().name("Other depot ${suffix}").build(true)
            Location bin1 = new LocationTestBuilder().asBinLocation(depot).name("Bin 1 ${suffix}").build(true)
            Location bin2 = new LocationTestBuilder().asBinLocation(depot).name("Bin 2 ${suffix}").build(true)
            Location emptyBin = new LocationTestBuilder().asBinLocation(depot).name("Empty bin ${suffix}").build(true)
            Category parent = new CategoryTestBuilder().name("Inventory parent ${suffix}").build(true)
            Category child = new CategoryTestBuilder().name("Inventory child ${suffix}").parentCategory(parent).build(true)
            Product alpha = new ProductTestBuilder().name("Inventory alpha ${suffix}").category(parent).build(true)
            Product beta = new ProductTestBuilder().name("Inventory beta ${suffix}").category(child).build(true)
            Product gamma = new ProductTestBuilder().name("Inventory gamma ${suffix}").category(parent).build(true)
            InventoryItem lotA = InventoryItem.build(product: alpha, lotNumber: "A-${suffix}", expirationDate: new Date() + 365)
            InventoryItem lotB = InventoryItem.build(product: alpha, lotNumber: "B-${suffix}", expirationDate: new Date() + 730)
            InventoryItem betaDefault = InventoryItem.build(product: beta, lotNumber: null, expirationDate: null)

            Transaction count = transaction(depot, Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID, when.count, "CNT-${suffix}")
            count.addToTransactionEntries(new TransactionEntry(inventoryItem: lotA, binLocation: bin1, quantity: 10))
            count.addToTransactionEntries(new TransactionEntry(inventoryItem: lotB, binLocation: bin2, quantity: 5))
            count.save(failOnError: true)
            Transaction credit = transaction(depot, Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID, when.credit, "CRD-${suffix}")
            credit.addToTransactionEntries(new TransactionEntry(inventoryItem: betaDefault, quantity: 8))
            credit.save(failOnError: true)
            Transaction consumption = transaction(depot, Constants.CONSUMPTION_TRANSACTION_TYPE_ID, when.consumption, "CON-${suffix}")
            consumption.addToTransactionEntries(new TransactionEntry(inventoryItem: lotA, binLocation: bin1, quantity: 3))
            consumption.save(failOnError: true)
            Transaction expiry = transaction(depot, Constants.EXPIRATION_TRANSACTION_TYPE_ID, when.expiry, "EXP-${suffix}")
            expiry.addToTransactionEntries(new TransactionEntry(inventoryItem: lotB, binLocation: bin2, quantity: 1))
            expiry.save(failOnError: true)
            Transaction otherCredit = transaction(otherDepot, Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID, when.otherCredit,
                    "OCR-${suffix}")
            otherCredit.addToTransactionEntries(new TransactionEntry(inventoryItem: lotA, quantity: 2))
            otherCredit.save(failOnError: true)

            new InventoryLevel(inventory: depot.inventory, product: alpha, minQuantity: 20, reorderQuantity: 30,
                    maxQuantity: 100, status: InventoryStatus.SUPPORTED, preferred: true).save(failOnError: true)
            new InventoryLevel(inventory: depot.inventory, product: beta, minQuantity: 2, reorderQuantity: 10,
                    maxQuantity: 50, status: InventoryStatus.SUPPORTED).save(failOnError: true)
            new InventoryLevel(inventory: depot.inventory, product: gamma, status: InventoryStatus.NOT_SUPPORTED)
                    .save(failOnError: true)

            Tag tag = new Tag(tag: "inventory-tag-${suffix}").save(failOnError: true, flush: true)
            sessionFactory.currentSession.createSQLQuery('insert into product_tag (product_id, tag_id) values (:product, :tag)')
                    .setParameter('product', alpha.id).setParameter('tag', tag.id).executeUpdate()
            ProductCatalog catalog = new ProductCatalog(code: "CAT-${suffix}", name: "Catalog ${suffix}")
            catalog.addToProductCatalogItems(new ProductCatalogItem(product: beta, active: true))
            catalog.save(failOnError: true)

            created.putAll(depot: depot.id, otherDepot: otherDepot.id, bin1: bin1.id, bin2: bin2.id, emptyBin: emptyBin.id,
                    parent: parent.id, child: child.id, alpha: alpha.id, beta: beta.id, gamma: gamma.id, lotA: lotA.id,
                    lotB: lotB.id, betaDefault: betaDefault.id, tag: tag.id, catalog: catalog.id)
        }
        tx {
            productAvailabilityService.refreshProductsAvailability(created.depot, [created.alpha, created.beta], true)
        }
        // Only remembered once committed, so a failed setup is retried rather than silently skipped
        dates = when
        ids = created
    }

    void cleanup() {
        authService.currentUser = null
        authService.currentLocation = null
        RequestContextHolder.resetRequestAttributes()
    }

    void 'the quantity maps of the depot add up the lots of every product'() {
        expect:
        tx {
            Map<Product, Integer> byProduct = inventoryService.getQuantityByProductMap(depot())
            assert byProduct[alpha()] == 11
            assert byProduct[beta()] == 8
            assert inventoryService.getQuantityByProductMap(ids.depot as String)[alpha()] == 11
            assert inventoryService.getQuantityByProductGroup(depot())[beta()] == 8
            assert inventoryService.getQuantityByProductMap(depot(), [alpha(), gamma()]) ==
                    [(alpha()): 11, (gamma()): 0]

            Map<InventoryItem, Integer> byItem = inventoryService.getQuantityForInventory(depot().inventory)
            assert byItem[lotA()] == 7
            assert byItem[lotB()] == 4
            assert byItem[betaDefault()] == 8
            assert inventoryService.getQuantityForInventory(depot().inventory, [alpha()]) == [(lotA()): 7, (lotB()): 4]
            assert inventoryService.getQuantityByInventoryItemMap(depot(), [beta()]) == [(betaDefault()): 8]
            assert inventoryService.getQuantityByInventoryAndProduct(depot().inventory, alpha()) == [(lotA()): 7, (lotB()): 4]
            assert inventoryService.getQuantityForProducts(depot().inventory, [ids.alpha, ids.beta] as ArrayList) ==
                    [(ids.alpha): 11, (ids.beta): 8]
            assert inventoryService.getQuantityOnHand(depot(), alpha()) == 11
            assert inventoryService.getQuantityOnHand(depot(), gamma()) == 0
            assert inventoryService.getQuantity(depot().inventory, lotA()) == 7
            assert inventoryService.getQuantity(depot(), alpha(), lotB().lotNumber) == 4
            assert inventoryService.getQuantity(otherDepot().inventory, lotA()) == 2
            assert inventoryService.getQuantityFromBinLocation(depot(), bin2(), lotB()) == 4
            assert inventoryService.getQuantityFromBinLocation(depot(), bin1(), lotB()) == 0
            assert inventoryService.getQuantityAvailableToPromise(depot().inventory, lotA()) == 7
            Map<Product, List<InventoryItem>> withQuantity =
                    inventoryService.getInventoryItemsWithQuantity([alpha()], depot().inventory)
            assert withQuantity[alpha()].collect { [it.lotNumber, it.quantity] } ==
                    [[lotA().lotNumber, 7], [lotB().lotNumber, 4]]
            assert inventoryService.getProductsWithTransactions(depot())*.id as Set == [ids.alpha, ids.beta] as Set
            true
        }
    }

    void 'getQuantity needs a location and an inventory item and an inventory'() {
        when:
        tx { inventoryService.getQuantity(null, alpha(), 'X') }

        then:
        RuntimeException noLocation = thrown()
        noLocation.message == 'Must specify location in order to calculate quantity on hand'

        when:
        tx { inventoryService.getQuantity(depot(), alpha(), 'no such lot') }

        then:
        RuntimeException noItem = thrown()
        noItem.message.startsWith("There's no inventory item for product")

        when:
        tx { inventoryService.getQuantity((Inventory) null, lotA()) }

        then:
        RuntimeException noInventory = thrown()
        noInventory.message == 'Inventory does not exist'
    }

    void 'the bin views of the depot list each lot in its bin'() {
        expect:
        tx {
            Closure<List<String>> lotAndBin = { List bins ->
                bins.collect { "${it.inventoryItem.lotNumber}@${it.binLocation?.name}=${it.quantity}".toString() }
            }
            String a = lotA().lotNumber
            String b = lotB().lotNumber
            String bin1Name = bin1().name
            String bin2Name = bin2().name

            assert lotAndBin(inventoryService.getProductQuantityByBinLocation(depot(), alpha())) ==
                    ["${a}@${bin1Name}=7", "${b}@${bin2Name}=4"]
            assert lotAndBin(inventoryService.getProductQuantityByBinLocation(depot(), alpha(), Boolean.TRUE)).size() == 2
            assert lotAndBin(inventoryService.getProductQuantityByBinLocation(depot(), [alpha(), beta()])).size() == 3
            // the counts of other bins come along; the caller (product availability) keeps only the bins it asked for
            assert lotAndBin(inventoryService.getProductQuantityByBinLocation(depot(), alpha(), [bin2()], false)
                    .findAll { it.binLocation?.id == ids.bin2 }) == ["${b}@${bin2Name}=4"]
            assert lotAndBin(inventoryService.getProductQuantityByBinLocation(depot(), beta(), [null], false)) ==
                    ['null@null=8']
            assert lotAndBin(inventoryService.getBinLocationsByInventoryItem(depot(), lotB())) == ["${b}@${bin2Name}=4"]
            assert lotAndBin(inventoryService.getBinLocationsByInventoryItems(depot(), [lotA(), betaDefault()])) ==
                    ['null@null=8', "${a}@${bin1Name}=7"]
            assert lotAndBin(inventoryService.getQuantityByBinLocation(depot(), bin1())) == ["${a}@${bin1Name}=7"]
            assert lotAndBin(inventoryService.getQuantityByBinLocation(depot())).size() == 3
            assert lotAndBin(inventoryService.getBinLocationDetails(depot())).size() == 3
            assert lotAndBin(inventoryService.getBinLocationDetails(depot(), dates.consumption)) ==
                    ['null@null=8', "${a}@${bin1Name}=10", "${b}@${bin2Name}=5"]
            assert inventoryService.getQuantityAsAvailableItems(
                    inventoryService.getTransactionEntriesByInventoryAndProduct(depot().inventory, [alpha()]))
                    .collect { it.quantityOnHand } as Set == [7, 4] as Set

            Map report = inventoryService.getBinLocationReport(depot())
            assert report.data.size() == 3
            assert report.summary.collect { [it.status, it.count] } == [['inStock', 3], ['outOfStock', 0]]
            true
        }
    }

    void 'saveLocation refuses to disable a bin that still holds stock but disables an empty one'() {
        when:
        tx {
            Location bin = bin1()
            bin.active = false
            inventoryService.saveLocation(bin)
        }

        then:
        ValidationException exception = thrown()
        exception.errors.globalErrors*.code == ['location.cannotDisableBinLocationWithStock.message']
        tx { bin1().active }

        when:
        tx {
            Location bin = Location.get(ids.emptyBin)
            bin.active = false
            inventoryService.saveLocation(bin)
        }

        then:
        tx { !Location.get(ids.emptyBin).active }
    }

    void 'getLocation finds a location, creates an empty one without an id and fails for an unknown id'() {
        expect:
        tx { inventoryService.getLocation(ids.depot).name.startsWith('Inventory depot') }
        tx { inventoryService.getLocation(null).id == null }

        when:
        tx { inventoryService.getLocation('no-such-location') }

        then:
        Exception exception = thrown()
        exception.message == 'No location found with locationId no-such-location'
    }

    void 'the inventory levels show which products are low, which to reorder and which are hidden'() {
        expect:
        tx {
            Map levels = inventoryService.getProductsBelowMinimumAndReorderQuantities(depot().inventory, false)
            assert levels.minimumProductsQuantityMap == [(alpha()): 11]
            assert levels.reorderProductsQuantityMap == [(beta()): 8]
            Map withUnsupported = inventoryService.getProductsBelowMinimumAndReorderQuantities(depot().inventory, true)
            assert withUnsupported.minimumProductsQuantityMap == [(alpha()): 11, (gamma()): 0]

            assert inventoryService.getHiddenProducts(depot())*.id == [ids.gamma]
            assert inventoryService.getProductsByLocationAndStatus(depot(), InventoryStatus.SUPPORTED)*.id as Set ==
                    [ids.alpha, ids.beta] as Set
            assert inventoryService.getInventoryLevelsByInventory(depot().inventory).size() == 3
            assert inventoryService.getProductsByInventory(depot().inventory)*.id as Set == [ids.alpha, ids.beta, ids.gamma] as Set
            assert inventoryService.getInventoryLevelByProductAndInventory(alpha(), depot().inventory).minQuantity == 20
            assert inventoryService.getInventoryLevelByProductAndInventory(alpha(), otherDepot().inventory).id == null
            true
        }
    }

    void 'products can be found by terms, categories, tags and catalogs'() {
        expect:
        tx {
            String suffix = ids.suffix
            Category parent = Category.get(ids.parent)
            Category child = Category.get(ids.child)
            assert inventoryService.searchProducts(["Inventory", suffix] as String[], [parent, child])*.id ==
                    [ids.alpha, ids.beta, ids.gamma]
            assert inventoryService.searchProducts(["A-${suffix}".toString()] as String[], null)*.id == [ids.alpha]
            def page = inventoryService.getProductsByTermsAndCategories(["beta ${suffix}".toString()], [child], false,
                    depot().inventory, 10, 0)
            assert page*.id == [ids.beta]
            assert page.totalCount == 1
            assert inventoryService.getProductsByTermsAndCategories(null, [parent], false, null, 10, 0)*.id ==
                    [ids.alpha, ids.gamma]

            assert inventoryService.getProductsByTagId([ids.tag])*.id == [ids.alpha]
            assert inventoryService.getProductsByTags([ids.tag])*.id == [ids.alpha]
            assert inventoryService.getProductsByTags([ids.tag], 0, 0)*.id == [ids.alpha]
            assert inventoryService.countProductsByTags([ids.tag]) == 1
            assert inventoryService.getProductsByTag("inventory-tag-${suffix}".toString())*.id == [ids.alpha]
            assert inventoryService.getProductsByCatalogs([ids.catalog], 10, 0)*.id == [ids.beta]
            assert inventoryService.countProductsByCatalogs([ids.catalog]) == 1

            assert inventoryService.getCategoriesMatchingSearchTerms(["Inventory parent ${suffix}".toString()])*.id
                    .containsAll([ids.parent, ids.child])
            assert inventoryService.getCategoriesMatchingSearchTerms([]) == []
            assert inventoryService.getProductsByCategory(child)*.id == [ids.beta]
            assert inventoryService.getProductsByNestedCategory(parent)*.id as Set == [ids.alpha, ids.beta, ids.gamma] as Set
            assert inventoryService.getProductsByNestedCategory(null) == []
            true
        }
    }

    void 'inventory items can be found and created by product and lot'() {
        expect:
        tx {
            String suffix = ids.suffix
            assert inventoryService.findInventoryItems("A-${suffix}".toString())*.id == [ids.lotA]
            assert inventoryService.findInventoryItems("Inventory alpha ${suffix}".toString())*.id as Set ==
                    [ids.lotA, ids.lotB] as Set
            assert inventoryService.findInventoryItemsByProducts([alpha()])*.id == [ids.lotA, ids.lotB]
            assert inventoryService.findInventoryItemsByProducts([]) == []
            assert inventoryService.getInventoryItemsByProduct(alpha())*.id as Set == [ids.lotA, ids.lotB] as Set
            assert inventoryService.getInventoryItemsByProducts(depot(), [ids.beta])[beta()]*.id == [ids.betaDefault]
            assert inventoryService.getInventoryItemsByProducts(depot(), []) == [:]
            assert inventoryService.getInventoryItemsByProductAndInventory(alpha(), depot().inventory)*.id ==
                    [ids.lotA, ids.lotB]
            assert inventoryService.findInventoryItemByProductAndLotNumber(beta(), null).id == ids.betaDefault
            assert inventoryService.findInventoryItemByProductAndLotNumber(alpha(), 'missing') == null
            assert inventoryService.findInventoryItemWithEmptyLotNumber()*.id.contains(ids.betaDefault)
            assert inventoryService.isInventoryItemInOtherLocation(depot().inventory, lotA())
            assert !inventoryService.isInventoryItemInOtherLocation(depot().inventory, lotB())
            assert inventoryService.getInventoriesWithTransactionsByProduct(alpha())*.id as Set ==
                    [depot().inventory.id, otherDepot().inventory.id] as Set
            true
        }
    }

    void 'getInventoryItemsByProduct needs a product'() {
        when:
        tx { inventoryService.getInventoryItemsByProduct(null) }

        then:
        Exception exception = thrown()
        exception.message == 'ProductNotFoundException'
    }

    void 'findOrCreateInventoryItem returns the existing lot or creates a new one'() {
        given:
        String newLot = "N-${ids.suffix}"
        Date expiry = new Date() + 100

        expect:
        tx { inventoryService.findOrCreateInventoryItem(lotA()).id == ids.lotA }
        tx { inventoryService.findOrCreateDefaultInventoryItem(beta()).id == ids.betaDefault }

        when:
        String createdId = tx { inventoryService.findOrCreateInventoryItem(gamma(), newLot, expiry).id }
        String updatedId = tx { inventoryService.findAndUpdateOrCreateInventoryItem(gamma(), newLot, expiry + 1).id }
        String otherId = tx { inventoryService.findAndUpdateOrCreateInventoryItem(gamma(), "M-${ids.suffix}", null).id }

        then:
        createdId == updatedId
        tx { InventoryItem.get(createdId).expirationDate.format('yyyy-MM-dd') } == (expiry + 1).format('yyyy-MM-dd')
        tx { InventoryItem.get(otherId).lotNumber } == "M-${ids.suffix}"
    }

    void 'the transaction entry queries filter by product, period, category and tag'() {
        expect:
        tx {
            Location depot = depot()
            Closure<List<String>> numbers = { List entries -> entries*.transaction*.transactionNumber.unique() }
            String s = ids.suffix

            assert numbers(inventoryService.getTransactionEntries(alpha(), depot, null, null)) ==
                    ["CNT-${s}", "CON-${s}", "EXP-${s}"]*.toString()
            assert numbers(inventoryService.getTransactionEntries(alpha(), depot, dates.consumption, dates.expiry)) ==
                    ["CON-${s}", "EXP-${s}"]*.toString()
            assert numbers(inventoryService.getTransactionEntries(alpha(), depot, dates.consumption, null)) ==
                    ["CON-${s}", "EXP-${s}"]*.toString()
            assert numbers(inventoryService.getTransactionEntries(alpha(), depot, null, dates.count)) == ["CNT-${s}"]*.toString()
            assert numbers(inventoryService.getTransactionEntriesBeforeDate(alpha(), depot, dates.expiry)) ==
                    ["CNT-${s}", "CON-${s}"]*.toString()
            assert inventoryService.getTransactionEntriesBeforeDate(alpha(), depot, null) == []
            assert numbers(inventoryService.getTransactionEntries(depot, Category.get(ids.child), null, null)) == ["CRD-${s}"]*.toString()
            assert numbers(inventoryService.getTransactionEntries(depot, [Category.get(ids.parent)], dates.count, dates.credit)) ==
                    ["CNT-${s}"]*.toString()
            assert numbers(inventoryService.getTransactionEntries(depot, [], dates.expiry, null)) == ["EXP-${s}"]*.toString()
            assert numbers(inventoryService.getTransactionEntries(depot, [], null, dates.count)) == ["CNT-${s}"]*.toString()
            assert numbers(inventoryService.getTransactionEntriesBeforeDate(depot, dates.consumption)) ==
                    ["CNT-${s}", "CRD-${s}"]*.toString()
            assert numbers(inventoryService.getTransactionEntriesBeforeDate(depot, dates.consumption, Tag.get(ids.tag))) ==
                    ["CNT-${s}"]*.toString()
            assert inventoryService.getTransactionEntriesBeforeDate(depot, [alpha()], null) == []
            assert numbers(inventoryService.getStockHistory(depot.inventory, beta())) == ["CRD-${s}"]*.toString()
            assert numbers(inventoryService.getTransactionEntriesByInventory(depot.inventory)) ==
                    ["CNT-${s}", "CRD-${s}", "CON-${s}", "EXP-${s}"]*.toString()
            assert numbers(inventoryService.getTransactionEntriesByInventoryAndProductForStockHistory(depot.inventory, [alpha()])) ==
                    ["CNT-${s}", "CON-${s}", "EXP-${s}"]*.toString()
            assert inventoryService.getTransactionEntriesByLocation(depot).size() == 5

            assert inventoryService.getQuantityOnHandAsOfDate(depot, dates.consumption) == [(alpha()): 15, (beta()): 8]
            assert inventoryService.getQuantityOnHandAsOfDate(depot, dates.consumption, [ids.tag]) == [(alpha()): 15]
            assert inventoryService.hasTransactionEntriesOnDate(depot, dates.credit, [beta()])
            assert !inventoryService.hasTransactionEntriesOnDate(depot, dates.credit, [alpha()])
            assert !inventoryService.hasTransactionEntriesOnDate(depot, dates.credit, [])
            true
        }
    }

    void 'getTransactionEntriesByLocation needs a location with an inventory'() {
        when:
        inventoryService.getTransactionEntriesByLocation(new Location())

        then:
        RuntimeException exception = thrown()
        exception.message == 'Location must have an inventory'
    }

    void 'credits and debits between dates can be narrowed by locations and types'() {
        expect:
        tx {
            String s = ids.suffix
            Date from = dates.count - 1
            Date to = new Date()
            Closure<List<String>> numbers = { List transactions ->
                transactions*.transactionNumber.findAll { it?.endsWith(s) }.sort()
            }

            assert numbers(inventoryService.getCreditsBetweenDates(null, [depot()], from, to)) == ["CRD-${s}"]*.toString()
            assert numbers(inventoryService.getCreditsBetweenDates(null, [depot(), otherDepot()], from, to,
                    [TransactionType.get(Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID)])) == ["CRD-${s}", "OCR-${s}"]*.toString()
            assert numbers(inventoryService.getCreditsBetweenDates([depot()], null, from, to)) == []
            assert numbers(inventoryService.getDebitsBetweenDates([depot()], null, from, to)) == ["CON-${s}", "EXP-${s}"]*.toString()
            assert numbers(inventoryService.getDebitsBetweenDates([depot()], [otherDepot()], from, to,
                    [TransactionType.get(Constants.EXPIRATION_TRANSACTION_TYPE_ID)])) == ["EXP-${s}"]*.toString()
            true
        }
    }

    void 'the most recent counts and the changes since them are summed per product'() {
        expect:
        tx {
            Map mostRecent = inventoryService.getMostRecentQuantityOnHand(depot())
            assert mostRecent[ids.alpha].quantityOnHand == 15
            assert mostRecent[ids.alpha].stockCountDate.time == dates.count.time
            List mostRecentForProduct = inventoryService.getMostRecentQuantityOnHand(alpha(), depot())
            assert mostRecentForProduct[0][1] == 15

            assert inventoryService.getQuantityChangeSince(alpha(), depot(), TransactionCode.DEBIT, dates.expiry) == [1]
            assert inventoryService.getQuantityChangeSince(alpha(), depot(), TransactionCode.DEBIT, null) == [4]
            assert inventoryService.calculateQuantityOnHand(alpha(), depot(), 15,
                    new java.sql.Timestamp(dates.count.time)) == 11
            assert inventoryService.getQuantityMap(depot())[ids.alpha].quantityOnHand == 15

            List latest = inventoryService.getMostRecentTransactionTypeForProductsInInventory(depot().inventory, [alpha(), beta()])
            assert latest.collectEntries { [(it.product.id): it.transactionTypeId] } ==
                    [(ids.alpha): Constants.EXPIRATION_TRANSACTION_TYPE_ID, (ids.beta): Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID]
            true
        }
    }

    void 'the stock card brings together levels, lots, history and availability'() {
        expect:
        tx {
            StockCardCommand command = inventoryService.getStockCardCommand(new StockCardCommand(warehouse: depot()),
                    [id: ids.alpha])
            assert command.product.id == ids.alpha
            assert command.inventoryLevel.minQuantity == 20
            assert command.inventoryItemList*.id == [ids.lotA, ids.lotB]
            assert command.totalQuantity == 11
            assert command.transactionEntryList.size() == 4
            assert command.transactionEntriesByTransactionMap.size() == 3
            assert command.quantityByInventoryItemMap == [(lotA()): 7, (lotB()): 4]
            assert command.availableItems.collect { [it.inventoryItem.id, it.quantityOnHand, it.quantityAvailable] } ==
                    [[ids.lotA, 7, 7], [ids.lotB, 4, 4]]
            assert command.totalQuantityAvailableToPromise == 11
            assert inventoryService.getQuantityAvailableToPromise(alpha(), depot()) == 11
            assert inventoryService.getQuantityAvailableByProductAndInventoryItemMap(alpha(), depot())[alpha()][lotA()][ids.bin1] == 7
            true
        }
    }

    void 'the stock card needs a known product'() {
        when:
        tx { inventoryService.getStockCardCommand(new StockCardCommand(warehouse: depot()), [product: [id: 'unknown']]) }

        then:
        org.pih.warehouse.product.ProductException exception = thrown()
        exception.message == "Product with identifier 'unknown' could not be found"
    }

    void 'populateRecordInventoryCommand prefills a row for every lot in every bin'() {
        expect:
        tx {
            RecordInventoryCommand command = new RecordInventoryCommand(product: alpha(), inventory: depot().inventory)
            inventoryService.populateRecordInventoryCommand(command, [:])
            assert command.recordInventoryRows.collect { [it.lotNumber, it.binLocation.id, it.oldQuantity, it.newQuantity] } ==
                    [[lotA().lotNumber, ids.bin1, 7, 7], [lotB().lotNumber, ids.bin2, 4, 4]]
            assert command.transactionDate

            RecordInventoryCommand noProduct = new RecordInventoryCommand()
            inventoryService.populateRecordInventoryCommand(noProduct, [:])
            assert noProduct.errors.globalErrors*.code == ['error.product.invalid']
            true
        }
    }

    void 'the available items and products of a location are the lots with stock'() {
        expect:
        tx {
            assert (inventoryService.getAvailableItems(depot(), alpha()).collect { [it.inventoryItem.id, it.quantityAvailable] }
                    as Set) == ([[ids.lotA, 7], [ids.lotB, 4]] as Set)
            assert inventoryService.getAvailableItems(otherDepot(), [alpha()], false).size() == 2
            assert inventoryService.getAvailableItems(otherDepot(), [alpha()]).collect { it.inventoryItem.id } == [ids.lotA]
            assert inventoryService.getAvailableProducts(depot(), beta()) ==
                    [['inventoryItem.id': ids.betaDefault, lotNumber: null, expirationDate: null, quantity: 8]]
            assert inventoryService.getAvailableBinLocations(depot(), alpha()).collect { [it.inventoryItem.id, it.quantityOnHand] } ==
                    [[ids.lotA, 7], [ids.lotB, 4]]
            assert inventoryService.getMostRecentInventoryItemSnapshot(depot()) == [:]
            assert inventoryService.calculatePendingQuantity(alpha(), depot()) == [null, null]
            true
        }
    }

    /** A new product with the given quantity counted in bin 1 of the depot yesterday. */
    private Map stockedProduct(int quantity) {
        return tx {
            authService.currentUser = User.findByUsername('admin')
            Product product = new ProductTestBuilder().randomizedName().category(Category.get(ids.parent)).build(true)
            InventoryItem lot = InventoryItem.build(product: product, lotNumber: "S-${UUID.randomUUID().toString().take(6)}",
                    expirationDate: null)
            Transaction count = transaction(depot(), Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID, daysAgo(1),
                    "SP-${UUID.randomUUID().toString().take(12)}")
            count.addToTransactionEntries(new TransactionEntry(inventoryItem: lot, binLocation: bin1(), quantity: quantity))
            count.save(failOnError: true)
            return [product: product.id, lot: lot.id]
        }
    }

    @PendingFeature(reason = 'an adjustment to the same quantity is not refused (validate() clears the rejection)')
    void 'adjustStock records the difference to the new quantity and refuses an unchanged quantity'() {
        given:
        Map stock = stockedProduct(6)

        when:
        AdjustStockCommand unchanged = tx {
            AdjustStockCommand command = new AdjustStockCommand(location: depot(), binLocation: bin1(),
                    inventoryItem: InventoryItem.get(stock.lot), currentQuantity: 6, newQuantity: 6)
            inventoryService.adjustStock(command)
            return command
        }

        then:
        unchanged.errors.getFieldError('newQuantity').code == 'adjustStock.invalid.quantity.message'

        when:
        tx {
            AdjustStockCommand command = new AdjustStockCommand(location: depot(), binLocation: bin1(),
                    inventoryItem: InventoryItem.get(stock.lot), currentQuantity: 6, newQuantity: 9, comment: 'found more',
                    reasonCode: org.pih.warehouse.core.ReasonCode.FOUND)
            inventoryService.adjustStock(command)
            assert !command.hasErrors()
        }

        then:
        tx { inventoryService.getQuantityFromBinLocation(depot(), bin1(), InventoryItem.get(stock.lot)) } == 9
        tx {
            TransactionEntry entry = TransactionEntry.findAllByInventoryItem(InventoryItem.get(stock.lot))
                    .find { it.transaction.transactionType.id == Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID }
            [entry.quantity, entry.reasonCode, entry.transaction.comment]
        } == [3, 'FOUND', 'found more']
    }

    void 'transferStock moves stock to another depot through a pair of mirrored transactions'() {
        given:
        Map stock = stockedProduct(5)

        when:
        String transferId
        String transactionId = tx {
            TransferStockCommand command = new TransferStockCommand(location: depot(), binLocation: bin1(),
                    inventoryItem: InventoryItem.get(stock.lot), otherLocation: otherDepot(), quantity: 2, transferOut: true)
            Transaction transaction = inventoryService.transferStock(command)
            assert !transaction.hasErrors()
            return transaction.id
        }
        transferId = tx { inventoryService.getLocalTransfer(Transaction.get(transactionId)).id }

        then:
        tx { inventoryService.getQuantity(depot().inventory, InventoryItem.get(stock.lot)) } == 3
        tx { inventoryService.getQuantity(otherDepot().inventory, InventoryItem.get(stock.lot)) } == 2
        tx {
            Transaction transaction = Transaction.get(transactionId)
            LocalTransfer transfer = inventoryService.getLocalTransfer(transaction)
            assert inventoryService.isLocalTransfer(transaction)
            assert inventoryService.isLocalTransfer(transfer.destinationTransaction)
            [transfer.sourceTransaction.id, transfer.destinationTransaction.transactionType.id,
             transfer.destinationTransaction.source.id]
        } == [transactionId, Constants.TRANSFER_IN_TRANSACTION_TYPE_ID, ids.depot]

        when: 'the transfer is deleted'
        tx { inventoryService.deleteTransaction(Transaction.get(transactionId)) }

        then:
        tx { LocalTransfer.get(transferId) } == null
    }

    void 'transferStock reports what is wrong with a transfer and saves nothing'() {
        given:
        Map stock = stockedProduct(1)

        when:
        List<String> messages = tx {
            TransferStockCommand command = new TransferStockCommand(location: depot(), binLocation: bin1(),
                    inventoryItem: InventoryItem.get(stock.lot), otherLocation: depot(), otherBinLocation: bin1(),
                    quantity: 3, transferOut: true)
            inventoryService.transferStock(command).errors.globalErrors*.code
        }

        then:
        // OBAF: InventoryService.transferStock names the bin and the quantities in both messages
        messages.size() == 2
        messages[0].startsWith('Cannot transfer to the same location')
        messages[1].startsWith('Quantity to transfer (3) cannot exceed available quantity (1)')
        tx { inventoryService.getQuantity(depot().inventory, InventoryItem.get(stock.lot)) } == 1
    }

    void 'saveLocalTransfer turns a transfer in into a transfer out at its source'() {
        given:
        Map stock = stockedProduct(4)

        when:
        String transferId = tx {
            Transaction transferIn = transaction(otherDepot(), Constants.TRANSFER_IN_TRANSACTION_TYPE_ID, new Date() - 1,
                    "TIN-${UUID.randomUUID().toString().take(12)}")
            transferIn.source = depot()
            transferIn.addToTransactionEntries(new TransactionEntry(inventoryItem: InventoryItem.get(stock.lot), quantity: 1))
            assert inventoryService.saveLocalTransfer(transferIn)
            return transferIn.id
        }
        transferId = tx { inventoryService.getLocalTransfer(Transaction.get(transferId)).id }

        then:
        tx {
            LocalTransfer transfer = LocalTransfer.get(transferId)
            [transfer.sourceTransaction.transactionType.id, transfer.sourceTransaction.inventory.id,
             transfer.sourceTransaction.destination.id]
        } == [Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID, tx { depot().inventory.id }, ids.otherDepot]
    }

    void 'recallInventoryItem marks a lot as recalled and revertRecallInventoryItem clears it'() {
        given:
        Map stock = stockedProduct(1)

        when:
        tx { inventoryService.recallInventoryItem(InventoryItem.get(stock.lot)) }

        then:
        tx { InventoryItem.get(stock.lot).lotStatus } == LotStatusCode.RECALLED

        when:
        tx { inventoryService.revertRecallInventoryItem(InventoryItem.get(stock.lot)) }

        then:
        tx { InventoryItem.get(stock.lot).lotStatus } == null
    }

    void 'importData adds the lots of the rows to a known product and counts them in an inventory transaction'() {
        given:
        String suffix = UUID.randomUUID().toString().take(8)
        String productName = tx { gamma().name }
        List<Map> rows = [
                [productDescription: productName, category: "Imported category ${suffix}", lotNumber: "IL-${suffix}",
                 expirationDate: '2031-01-15', quantity: 12, unitOfMeasure: 'EA', manufacturer: 'Acme', upc: 'U1', ndc: 'N1'],
                [productDescription: productName, category: "Imported category ${suffix}", lotNumber: "IM-${suffix}",
                 quantity: null, unitOfMeasure: 'EA'],
        ]

        when: 'the rows are validated'
        ImportDataCommand validated = tx {
            ImportDataCommand command = new ImportDataCommand(location: depot(), data: rows)
            inventoryService.validateData(command)
            command
        }

        then: 'nothing is saved yet but the new lots are known'
        !validated.hasErrors()
        validated.inventoryItems*.lotNumber == ["IL-${suffix}", "IM-${suffix}"]*.toString()
        validated.inventoryItems[0].expirationDate.format('yyyy-MM-dd') == '2031-01-15'
        validated.transaction.transactionEntries*.quantity == [12]
        tx { InventoryItem.findAllByLotNumber("IL-${suffix}") } == []

        when: 'the rows are imported'
        tx {
            ImportDataCommand command = new ImportDataCommand(location: depot(), data: rows)
            inventoryService.importData(command)
            assert !command.hasErrors()
        }

        then:
        tx {
            Product product = Product.findByName(productName)
            [InventoryItem.findAllByProduct(product)*.lotNumber.findAll { it?.endsWith(suffix) }.sort(),
             inventoryService.getQuantity(depot().inventory, InventoryItem.findByLotNumber("IL-${suffix}"))]
        } == [["IL-${suffix}", "IM-${suffix}"]*.toString(), 12]
    }

    void 'the expiration history report lists the expired stock of the current location with totals'() {
        given:
        // the rows show localized product names, which need a request
        GrailsWebMockUtil.bindMockWebRequest(Holders.grailsApplication.mainContext as WebApplicationContext)
        ExpirationHistoryReportFilterCommand command = new ExpirationHistoryReportFilterCommand(
                startDate: dates.count, endDate: new Date(), searchTerm: "EXP-${ids.suffix}")
        command.paginationParams = new org.pih.warehouse.core.PaginationParams(max: 10, offset: 0)

        when:
        Map result = tx {
            authService.currentLocation = depot()
            def report = inventoryService.getExpirationHistoryReport(command)
            [rows: report.rows.list.collect { [it.transactionNumber, it.quantityLostToExpiry] },
             quantity: report.totalQuantityLostToExpiry, value: report.totalValueLostToExpiry]
        }

        then:
        result.rows == [["EXP-${ids.suffix}".toString(), 1]]
        result.quantity == 1
        result.value == 1
    }
}
