package org.pih.warehouse.inventory

import java.time.LocalDate

import grails.testing.gorm.DataTest
import spock.lang.Shared
import spock.lang.Specification

import org.pih.warehouse.core.date.JavaUtilDateParser
import org.pih.warehouse.product.Product

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
