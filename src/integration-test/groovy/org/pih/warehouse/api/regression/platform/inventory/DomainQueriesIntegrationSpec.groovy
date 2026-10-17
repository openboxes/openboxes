package org.pih.warehouse.api.regression.platform.inventory

import org.pih.warehouse.inventory.*
import spock.lang.Shared

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.common.base.IntegrationSpec
import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.common.domain.builder.product.CategoryTestBuilder
import org.pih.warehouse.common.domain.builder.product.ProductTestBuilder
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.User
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderType
import org.pih.warehouse.order.OrderTypeCode
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAvailability
import org.pih.warehouse.product.ProductCatalog
import org.pih.warehouse.product.ProductCatalogItem
import org.pih.warehouse.reporting.DateDimension
import org.pih.warehouse.reporting.LocationDimension
import org.pih.warehouse.reporting.LotDimension
import org.pih.warehouse.reporting.ProductDimension
import org.pih.warehouse.reporting.TransactionFact
import org.pih.warehouse.reporting.TransactionTypeDimension

/**
 * Domain criteria, named queries and HQL that only a real database can answer: transaction dates and counts per
 * location and product, bin locations of a depot and the transaction fact summary of the transaction report.
 */
class DomainQueriesIntegrationSpec extends IntegrationSpec {

    AuthService authService

    @Shared Map<String, Object> ids = [:]

    /** Noon on the given day of 2025, far from day boundaries. */
    private static Date day(int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance()
        calendar.clear()
        calendar.set(2025, month - 1, dayOfMonth, 12, 0, 0)
        return calendar.time
    }

    void setup() {
        if (ids) {
            return
        }
        Map<String, Object> created = [:]
        Product.withNewTransaction {
            String suffix = UUID.randomUUID().toString().take(8)
            Location depot = new LocationTestBuilder().asFacility().name("Query depot ${suffix}").build(true)
            Location bin = new LocationTestBuilder().asBinLocation(depot).name("Bin ${suffix}").build(true)
            Category category = new CategoryTestBuilder().name("Query category ${suffix}").build(true)
            Product product = new ProductTestBuilder().randomizedName().category(category).build(true)
            Product other = new ProductTestBuilder().randomizedName().category(category).build(true)
            InventoryItem lot = InventoryItem.build(product: product, lotNumber: "Q-${suffix}", expirationDate: null)

            created.depot = depot.id
            created.bin = bin.id
            created.product = product.id
            created.other = other.id
            created.lot = lot.id
            created.transactions = [
                    transaction(depot, Constants.TRANSFER_IN_TRANSACTION_TYPE_ID, day(3, 1), lot, product, 10, suffix),
                    transaction(depot, Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID, day(3, 10), lot, product, 8, suffix),
                    transaction(depot, Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID, day(3, 20), lot, product, 7, suffix),
                    transaction(depot, Constants.TRANSFER_IN_TRANSACTION_TYPE_ID, day(4, 5), lot, product, 5, suffix),
            ]
            // availability of a product without transactions, so that no refresh touches it
            InventoryItem otherLot = InventoryItem.build(product: other, lotNumber: "R-${suffix}", expirationDate: null)
            ProductAvailability.build(location: depot, product: other, inventoryItem: otherLot, binLocation: null,
                    productCode: other.productCode, lotNumber: otherLot.lotNumber, binLocationName: 'DEFAULT',
                    quantityOnHand: 12, quantityAvailableToPromise: 12)
            ProductAvailability.build(location: depot, product: other, inventoryItem: otherLot, binLocation: bin,
                    productCode: other.productCode, lotNumber: otherLot.lotNumber, binLocationName: bin.name,
                    quantityOnHand: 3, quantityAvailableToPromise: 3)

            LocationDimension locationKey = LocationDimension.build(locationId: depot.id, locationName: depot.name,
                    locationTypeCode: 'DEPOT', locationTypeName: 'Depot')
            ProductDimension productKey = ProductDimension.build(productId: product.id, productCode: product.productCode,
                    productName: product.name, active: true)
            ProductDimension otherKey = ProductDimension.build(productId: other.id, productCode: other.productCode,
                    productName: other.name, active: true)
            LotDimension lotKey = LotDimension.build(inventoryItemId: lot.id, productCode: product.productCode)
            TransactionTypeDimension typeKey = TransactionTypeDimension.build(
                    transactionTypeId: Constants.TRANSFER_IN_TRANSACTION_TYPE_ID, transactionCode: 'CREDIT',
                    transactionTypeName: 'Transfer in')
            DateDimension march = DateDimension.build(date: day(3, 2))
            created.facts = [
                    TransactionFact.build(locationKey: locationKey, productKey: productKey, lotKey: lotKey,
                            transactionDateKey: march, transactionTypeKey: typeKey, transactionDate: day(3, 2),
                            transactionNumber: "F1-${suffix}", quantity: 1).id,
                    TransactionFact.build(locationKey: locationKey, productKey: otherKey, lotKey: lotKey,
                            transactionDateKey: march, transactionTypeKey: typeKey, transactionDate: day(3, 25),
                            transactionNumber: "F2-${suffix}", quantity: 2).id,
                    TransactionFact.build(locationKey: locationKey, productKey: productKey, lotKey: lotKey,
                            transactionDateKey: march, transactionTypeKey: typeKey, transactionDate: day(3, 12),
                            transactionNumber: "F3-${suffix}", quantity: 3).id,
            ]
            created.dimensions = [location: locationKey.id, product: productKey.id, other: otherKey.id,
                                  lot: lotKey.id, type: typeKey.id, date: march.id]
        }
        ids = created
    }

    private static String transaction(Location depot, String typeId, Date date, InventoryItem lot, Product product,
                                      int quantity, String suffix) {
        Transaction transaction = new Transaction(
                inventory: depot.inventory,
                transactionType: TransactionType.get(typeId),
                transactionDate: date,
                transactionNumber: "Q-${suffix}-${date.time}",
                source: typeId == Constants.TRANSFER_IN_TRANSACTION_TYPE_ID ? Location.get('1') : null,
                disableRefresh: true,
        )
        transaction.addToTransactionEntries(new TransactionEntry(product: product, inventoryItem: lot, quantity: quantity))
        transaction.save(failOnError: true, flush: true)
        return transaction.id
    }

    void cleanupSpec() {
        if (!ids) {
            return
        }
        try {
            Product.withNewTransaction {
                TransactionFact.findAllByIdInList(ids.facts)*.delete()
                LocationDimension.get(ids.dimensions.location)?.delete()
                ProductDimension.findAllByIdInList([ids.dimensions.product, ids.dimensions.other])*.delete()
                LotDimension.get(ids.dimensions.lot)?.delete()
                TransactionTypeDimension.get(ids.dimensions.type)?.delete()
                DateDimension.get(ids.dimensions.date)?.delete()
                ProductAvailability.findAllByProduct(Product.get(ids.other))*.delete()
            }
            Product.withNewTransaction {
                // deleting the transactions publishes the refresh events of a deletion
                Transaction.findAllByIdInList(ids.transactions)*.delete(flush: true)
            }
        } catch (Exception ignored) {
            // The refresh jobs of the application may still reference the rows; leftovers are harmless test data
        }
    }

    void 'transaction named queries span all transactions and count the recent ones of a location'() {
        when:
        Map result = Transaction.withNewTransaction {
            Location depot = Location.get(ids.depot)
            [min        : Transaction.minTransactionDate().get(),
             max        : Transaction.maxTransactionDate().get(),
             sinceEpoch : Transaction.countByLocationAsOf(depot, new Date(0)).get(),
             sinceFuture: Transaction.countByLocationAsOf(depot, new Date() + 1).get()]
        }

        then:
        result.min.time <= day(3, 1).time
        result.max.time >= day(4, 5).time
        result.sinceEpoch == 4
        result.sinceFuture == 0
    }

    void 'transaction entry named queries count transactions by type and find the last count'() {
        when:
        Map result = TransactionEntry.withNewTransaction {
            Location depot = Location.get(ids.depot)
            Product product = Product.get(ids.product)
            List<TransactionType> receipts = [TransactionType.get(Constants.TRANSFER_IN_TRANSACTION_TYPE_ID)]
            [receiptsInMarch: TransactionEntry.countByTransactionTypes(depot, product, receipts, day(3, 1) - 1, day(3, 31)).get(),
             receiptsInSpring: TransactionEntry.countByTransactionTypes(depot, product, receipts, day(3, 1) - 1, day(5, 1)).get(),
             lastCounted: TransactionEntry.dateLastCounted(depot, product).get()]
        }

        then:
        result.receiptsInMarch == 1
        result.receiptsInSpring == 2
        result.lastCounted.time == day(3, 20).time
    }

    void 'a product knows its last inventory date, first receipt and available quantity at a location'() {
        when:
        Map result = Product.withNewTransaction {
            Product product = Product.get(ids.product)
            [latestInventory: product.latestInventoryDate(ids.depot),
             earliestReceipt: product.earliestReceivingDate(ids.depot),
             available      : Product.get(ids.other).getQuantityAvailableToPromise(ids.depot)]
        }

        then:
        result.latestInventory.time == day(3, 20).time
        result.earliestReceipt.time == day(3, 1).time
        result.available == 15
    }

    void 'a depot counts its bin locations and appears among the physical and non internal locations'() {
        when:
        Map result = Location.withNewTransaction {
            Location depot = Location.get(ids.depot)
            [bins       : depot.countBinLocations(depot),
             physical   : Location.AllDepotWardAndPharmacy()*.id,
             nonInternal: Location.listNonInternalLocations()*.id]
        }

        then:
        result.bins == 1
        ids.depot in result.physical
        !(ids.bin in result.physical)
        ids.depot in result.nonInternal
        !(ids.bin in result.nonInternal)
    }

    void 'transaction facts summarize the products and dates of a location'() {
        when:
        Map result = TransactionFact.withNewTransaction {
            [products: TransactionFact.countDistinctProducts(ids.depot).get(),
             min     : TransactionFact.minTransactionDate(ids.depot).get(),
             max     : TransactionFact.maxTransactionDate(ids.depot).get()]
        }

        then:
        result.products == 2
        result.min.time == day(3, 2).time
        result.max.time == day(3, 25).time
    }

    void 'catalogs that include a product are found by name'() {
        when:
        List<String> names = ProductCatalog.withNewTransaction {
            Product product = Product.get(ids.product)
            String suffix = UUID.randomUUID().toString().take(8)
            ['B', 'A'].each { String prefix ->
                ProductCatalog catalog = new ProductCatalog(name: "${prefix} catalog ${suffix}", code: "${prefix}-${suffix}")
                catalog.addToProductCatalogItems(new ProductCatalogItem(product: product))
                catalog.save(failOnError: true, flush: true)
            }
            new ProductCatalog(name: "C catalog ${suffix}", code: "C-${suffix}").save(failOnError: true, flush: true)
            ProductCatalog.includesProduct(product).list()*.name.findAll { it.endsWith(suffix) }
        }

        then:
        names.size() == 2
        names[0].startsWith('A catalog')
        names[1].startsWith('B catalog')
    }

    void 'internal location types are the bin and internal location types'() {
        when:
        List<LocationTypeCode> codes = LocationType.withNewTransaction { LocationType.internalLocationTypes*.locationTypeCode }

        then:
        !codes.empty
        codes.every { it in [LocationTypeCode.BIN_LOCATION, LocationTypeCode.INTERNAL] }
    }

    void 'an organization knows whether it has purchase orders and its highest order number'() {
        when:
        Map result = Order.withNewTransaction {
            String suffix = UUID.randomUUID().toString().take(8)
            def partyType = Location.get('1').organization.partyType
            Organization buyer = Organization.build(name: "Buyer ${suffix}", code: "B${suffix}", partyType: partyType)
            Organization idle = Organization.build(name: "Idle ${suffix}", code: "I${suffix}", partyType: partyType)
            ['PO-1', 'PO-3', 'PO-2'].each { String number ->
                Order.build(orderType: OrderType.findByCode(OrderTypeCode.PURCHASE_ORDER.name()),
                        name: 'Purchase order', orderNumber: "${number}-${suffix}",
                        origin: Location.get('1'), destination: Location.get(ids.depot), destinationParty: buyer,
                        orderedBy: Person.list(max: 1).first())
            }
            Order.withSession { it.flush() }
            [has    : buyer.hasPurchaseOrders(), max: buyer.maxPurchaseOrderNumber(),
             idleHas: idle.hasPurchaseOrders(), idleMax: idle.maxPurchaseOrderNumber(), suffix: suffix]
        }

        then:
        result.has
        result.max == "PO-3-${result.suffix}"
        !result.idleHas
        result.idleMax == null
    }

    void 'completed cycle counts of a product are counted and dated'() {
        when:
        Map result = CycleCount.withNewTransaction {
            Location depot = Location.get(ids.depot)
            Product product = Product.get(ids.product)
            InventoryItem lot = InventoryItem.get(ids.lot)
            User admin = User.list(max: 1).first()
            // the audit fields of cycle counts come from the current user
            authService.currentUser = admin
            CycleCount completed = new CycleCount(facility: depot, status: CycleCountStatus.COMPLETED,
                    dateLastRefreshed: new Date(), createdBy: admin, updatedBy: admin)
            CycleCount counting = new CycleCount(facility: depot, status: CycleCountStatus.COUNTING,
                    dateLastRefreshed: new Date(), createdBy: admin, updatedBy: admin)
            [[completed, day(5, 2), CycleCountItemStatus.APPROVED], [completed, day(5, 9), CycleCountItemStatus.APPROVED],
             [counting, day(5, 20), CycleCountItemStatus.COUNTING]].each { List row ->
                CycleCount cycleCount = row[0]
                cycleCount.addToCycleCountItems(new CycleCountItem(facility: depot, product: product, inventoryItem: lot,
                        location: depot, countIndex: 0, status: row[2], quantityOnHand: 7,
                        quantityCounted: 7, custom: false, dateCounted: row[1], createdBy: admin, updatedBy: admin))
            }
            completed.save(failOnError: true, flush: true)
            counting.save(failOnError: true, flush: true)
            [count: CycleCountItem.countCompletedCycleCounts(depot, product, day(5, 1), day(5, 31)).get(),
             outside: CycleCountItem.countCompletedCycleCounts(depot, product, day(6, 1), day(6, 30)).get(),
             last : CycleCountItem.dateLastCounted(depot, product).get()]
        }
        authService.currentUser = null

        then:
        result.count == 1
        result.outside == 0
        result.last.time == day(5, 9).time
    }
}
