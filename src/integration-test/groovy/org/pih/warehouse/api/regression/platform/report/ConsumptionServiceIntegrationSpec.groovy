package org.pih.warehouse.api.regression.platform.report

import org.pih.warehouse.report.*
import spock.lang.Shared

import org.pih.warehouse.common.base.IntegrationSpec
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.reporting.ConsumptionFact
import org.pih.warehouse.reporting.DateDimension
import org.pih.warehouse.reporting.LocationDimension
import org.pih.warehouse.reporting.LotDimension
import org.pih.warehouse.reporting.ProductDimension

/**
 * The consumption queries join the fact table to its dimensions, which the in-memory datastore cannot do, so they
 * run against the database here. The dimensions reference real locations, products and inventory items.
 */
class ConsumptionServiceIntegrationSpec extends IntegrationSpec {

    ConsumptionService consumptionService

    @Shared Map<String, Long> factIds = [:]
    @Shared Map<String, List<String>> createdIds = [:]

    /** Noon on the given day, so that day boundaries are never close. */
    private static Date day(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance()
        calendar.clear()
        calendar.set(year, month - 1, dayOfMonth, 12, 0, 0)
        return calendar.time
    }

    void setup() {
        if (factIds) {
            return
        }
        Map<String, Long> ids = [:]
        Map<String, List<String>> created = [locations: [], products: [], inventoryItems: []]
        ConsumptionFact.withNewTransaction {
            LocationDimension depot = location(created)
            LocationDimension clinic = location(created)
            ProductDimension bDrug = product('B drug', 'Drugs', created)
            ProductDimension aDrug = product('A drug', 'Drugs', created)
            ProductDimension gauze = product('Gauze', 'Supplies', created)
            InventoryItem inventoryItem = InventoryItem.build(product: Product.get(created.products[0]), lotNumber: 'L1')
            created.inventoryItems << inventoryItem.id
            LotDimension lot = LotDimension.build(inventoryItemId: inventoryItem.id, productCode: 'P')
            DateDimension june = DateDimension.build(date: day(2026, 6, 15))
            DateDimension july = DateDimension.build(date: day(2026, 7, 15))

            ids.bDrugInJune = fact(depot, bDrug, lot, june).id
            ids.aDrugInJune = fact(depot, aDrug, lot, june).id
            ids.gauzeInJune = fact(depot, gauze, lot, june).id
            ids.bDrugInJuly = fact(depot, bDrug, lot, july).id
            ids.clinicInJune = fact(clinic, bDrug, lot, june).id
        }
        // Only remembered once committed, so a failed setup is retried rather than silently skipped
        createdIds = created
        factIds = ids
    }

    void cleanupSpec() {
        if (!createdIds) {
            return
        }
        ConsumptionFact.withNewTransaction {
            if (factIds) {
                ConsumptionFact.findAllByIdInList(factIds.values() as List)*.delete()
            }
            LocationDimension.findAllByLocationIdInList(createdIds.locations)*.delete()
            ProductDimension.findAllByProductIdInList(createdIds.products)*.delete()
            LotDimension.findAllByInventoryItemIdInList(createdIds.inventoryItems)*.delete()
            DateDimension.findAllByDateInList([day(2026, 6, 15), day(2026, 7, 15)])*.delete()
        }
        // Bulk deletes, because deleting inventory items or products one by one publishes a product availability
        // refresh for every depot
        ConsumptionFact.withNewTransaction {
            InventoryItem.executeUpdate('delete InventoryItem i where i.id in (:ids)', [ids: createdIds.inventoryItems])
            Product.executeUpdate('delete Product p where p.id in (:ids)', [ids: createdIds.products])
        }
        // the test locations stay: deleting them fails on rows that still reference them, and the test database is
        // thrown away after the run
    }

    private static LocationDimension location(Map<String, List<String>> created) {
        Location location = Location.build()
        created.locations << location.id
        return LocationDimension.build(locationId: location.id, locationName: location.name,
                locationTypeCode: 'DEPOT', locationTypeName: 'Depot')
    }

    private static ProductDimension product(String name, String categoryName, Map<String, List<String>> created) {
        Product product = Product.build(name: name)
        created.products << product.id
        return ProductDimension.build(productId: product.id, productCode: product.productCode ?: name,
                productName: name, categoryName: categoryName, active: true)
    }

    private static ConsumptionFact fact(LocationDimension location, ProductDimension product, LotDimension lot,
                                        DateDimension date) {
        return ConsumptionFact.build(locationKey: location, productKey: product, lotKey: lot, transactionDateKey: date,
                transactionNumber: 'T-1', transactionCode: 'DEBIT', transactionType: 'Consumption', quantity: 1)
    }

    private Location depot() {
        Location location = new Location()
        location.id = createdIds.locations[0]
        return location
    }

    void 'listConsumption returns the facts of the location within the dates ordered by product name'() {
        when:
        List<Long> ids = ConsumptionFact.withNewTransaction {
            consumptionService.listConsumption(depot(), null, day(2026, 6, 1), day(2026, 6, 30))*.id
        }

        then:
        ids == [factIds.aDrugInJune, factIds.bDrugInJune, factIds.gauzeInJune]
    }

    void 'listConsumption narrows the facts down to one category'() {
        when:
        List<Long> ids = ConsumptionFact.withNewTransaction {
            consumptionService.listConsumption(depot(), new Category(name: 'Drugs'), day(2026, 6, 1), day(2026, 6, 30))*.id
        }

        then:
        ids == [factIds.aDrugInJune, factIds.bDrugInJune]
    }

    void 'listConsumption does not limit the period unless both dates are given'() {
        when:
        List<Long> ids = ConsumptionFact.withNewTransaction {
            consumptionService.listConsumption(depot(), null, day(2026, 6, 1), null)*.id
        }

        then:
        ids as Set == [factIds.aDrugInJune, factIds.bDrugInJune, factIds.bDrugInJuly, factIds.gauzeInJune] as Set
    }

    void 'deleteConsumptionRecords removes every fact and reports how many'() {
        given:
        long before = ConsumptionFact.withNewTransaction { ConsumptionFact.count() }

        when:
        Integer deleted = ConsumptionFact.withNewTransaction { consumptionService.deleteConsumptionRecords() }

        then:
        before >= 5
        deleted == before
        ConsumptionFact.withNewTransaction { ConsumptionFact.count() } == 0

        cleanup: 'the facts are gone: a feature after this one builds them again'
        factIds = [:]
    }
}
