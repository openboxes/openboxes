package org.pih.warehouse.inventory

import java.time.LocalDate

import grails.testing.gorm.DataTest
import spock.lang.Shared
import spock.lang.Specification

import org.pih.warehouse.core.date.JavaUtilDateParser
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.lot.ProductLot

class InventoryItemManagerSpec extends Specification implements DataTest {

    @Shared
    InventoryItemManager inventoryItemManager

    @Shared
    Date expirationDate = JavaUtilDateParser.asDate(LocalDate.of(2028, 3, 1))

    void setupSpec() {
        mockDomains(Product, InventoryItem)
    }

    void setup() {
        inventoryItemManager = new InventoryItemManager()
    }

    private InventoryItem buildInventoryItem(Date lotExpirationDate) {
        Product product = new Product(name: 'Test product').save(validate: false)
        return new InventoryItem(
                product: product,
                lotNumber: 'LOT-1',
                expirationDate: lotExpirationDate,
        ).save(validate: false, flush: true)
    }

    private InventoryItem buildLot(Product product, String lotNumber) {
        return new InventoryItem(product: product, lotNumber: lotNumber).save(validate: false, flush: true)
    }

    void 'getInventoryItems should find the lot of every given product lot'() {
        given: 'two products, each with its own lot'
        Product product = new Product(name: 'Test product').save(validate: false)
        Product otherProduct = new Product(name: 'Other product').save(validate: false)
        InventoryItem lot = buildLot(product, 'LOT-1')
        InventoryItem otherLot = buildLot(otherProduct, 'LOT-2')

        and: 'a lot of one of the products that nobody asked about'
        buildLot(otherProduct, 'LOT-3')

        when:
        List<InventoryItem> result = inventoryItemManager.getInventoryItems([
                new ProductLot(product: product, lotNumber: 'LOT-1'),
                new ProductLot(product: otherProduct, lotNumber: 'LOT-2'),
        ])

        then: 'only the lots that were asked for are returned'
        assert result as Set == [lot, otherLot] as Set
    }

    void 'getInventoryItems should find the default lot of a product lot without a lot number'() {
        given: 'a product whose default lot is stored with an empty lot number'
        Product product = new Product(name: 'Test product').save(validate: false)
        InventoryItem defaultLot = buildLot(product, '')

        when:
        List<InventoryItem> result = inventoryItemManager.getInventoryItems([
                new ProductLot(product: product, lotNumber: null),
        ])

        then:
        assert result == [defaultLot]
    }

    void 'getInventoryItems should match the lot number as given, without sanitizing it'() {
        given:
        Product product = new Product(name: 'Test product').save(validate: false)
        buildLot(product, 'LOT-1')

        when: 'the lot number is asked for with whitespace around it'
        List<InventoryItem> result = inventoryItemManager.getInventoryItems([
                new ProductLot(product: product, lotNumber: ' LOT-1 '),
        ])

        then: 'the lot stored without the whitespace is not reported'
        assert result.empty
    }

    void 'getInventoryItems should leave out a product lot that is not in inventory'() {
        given:
        Product product = new Product(name: 'Test product').save(validate: false)
        InventoryItem lot = buildLot(product, 'LOT-1')

        when:
        List<InventoryItem> result = inventoryItemManager.getInventoryItems([
                new ProductLot(product: product, lotNumber: 'LOT-1'),
                new ProductLot(product: product, lotNumber: 'LOT-UNKNOWN'),
        ])

        then: 'nothing is created for the missing lot'
        assert result == [lot]
        assert InventoryItem.count() == 1
    }

    void 'updateExpirationDate should save the new date on the lot'() {
        given: 'the following db data'
        InventoryItem inventoryItem = buildInventoryItem(JavaUtilDateParser.asDate(LocalDate.of(2026, 9, 9)))

        when:
        inventoryItemManager.updateExpirationDate(inventoryItem, expirationDate)

        then: 'the lot carries the new date'
        assert InventoryItem.get(inventoryItem.id).expirationDate == expirationDate
    }

    void 'updateExpirationDate should clear the date of the lot when none is given'() {
        given: 'the following db data'
        InventoryItem inventoryItem = buildInventoryItem(expirationDate)

        when:
        inventoryItemManager.updateExpirationDate(inventoryItem, null)

        then: 'the lot is left without a date instead of keeping the old one'
        assert InventoryItem.get(inventoryItem.id).expirationDate == null
    }

    void 'updateExpirationDate should leave the lot untouched when the date has not changed'() {
        given: 'the following db data'
        InventoryItem inventoryItem = buildInventoryItem(expirationDate)

        when:
        inventoryItemManager.updateExpirationDate(inventoryItem, expirationDate)

        then: 'nothing is written'
        assert !inventoryItem.isDirty()
        assert InventoryItem.get(inventoryItem.id).expirationDate == expirationDate
    }
}
