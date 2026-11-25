package org.pih.warehouse.inventory

import java.time.LocalDate

import grails.testing.gorm.DataTest
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.date.JavaUtilDateParser
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.lot.ProductLot

@Unroll
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

    void 'upsertInventoryItems should return an empty map when given no product lots'() {
        when:
        InventoryItemByProductLot result = inventoryItemManager.upsertInventoryItems([])

        then:
        assert result.isEmpty()
        assert InventoryItem.count() == 0
    }

    void 'upsertInventoryItems should create the lots that do not exist yet'() {
        given:
        Product product = new Product(name: 'Test product').save(validate: false)
        Product otherProduct = new Product(name: 'Other product').save(validate: false)

        when:
        InventoryItemByProductLot result = inventoryItemManager.upsertInventoryItems([
                new ProductLot(product: product, lotNumber: 'LOT-1', expirationDate: expirationDate),
                new ProductLot(product: otherProduct, lotNumber: 'LOT-2'),
        ])

        then: 'a lot is created for each product lot, carrying the given expiration date'
        assert InventoryItem.count() == 2

        InventoryItem lot = result.get(product, 'LOT-1')
        assert lot.id != null
        assert lot.product == product
        assert lot.lotNumber == 'LOT-1'
        assert lot.expirationDate == expirationDate

        InventoryItem otherLot = result.get(otherProduct, 'LOT-2')
        assert otherLot.id != null
        assert otherLot.product == otherProduct
        assert otherLot.lotNumber == 'LOT-2'
        assert otherLot.expirationDate == null
    }

    void 'upsertInventoryItems should sanitize the lot number of the lots it creates'() {
        given:
        Product product = new Product(name: 'Test product').save(validate: false)

        when:
        InventoryItemByProductLot result = inventoryItemManager.upsertInventoryItems([
                new ProductLot(product: product, lotNumber: ' LOT-1 '),
        ])

        then: 'the lot is stored trimmed, but is still keyed on the lot number as it was given'
        assert result.get(product, ' LOT-1 ').lotNumber == 'LOT-1'
    }

    void 'upsertInventoryItems should create new lots with disableRefresh set to #disableRefresh'() {
        given:
        Product product = new Product(name: 'Test product').save(validate: false)

        when:
        InventoryItemByProductLot result = inventoryItemManager.upsertInventoryItems(
                [new ProductLot(product: product, lotNumber: 'LOT-1')],
                disableRefresh,
        )

        then:
        assert result.get(product, 'LOT-1').disableRefresh == disableRefresh

        where:
        disableRefresh << [true, false]
    }

    void 'upsertInventoryItems should return the existing lot instead of creating a new one'() {
        given:
        InventoryItem existingLot = buildInventoryItem(expirationDate)

        when:
        InventoryItemByProductLot result = inventoryItemManager.upsertInventoryItems([
                new ProductLot(product: existingLot.product, lotNumber: 'LOT-1', expirationDate: expirationDate),
        ])

        then: 'nothing is created and the lot is left untouched'
        assert result.get(existingLot.product, 'LOT-1') == existingLot
        assert InventoryItem.count() == 1
        assert !existingLot.isDirty()
    }

    void 'upsertInventoryItems should find an existing lot through its sanitized lot number'() {
        given:
        InventoryItem existingLot = buildInventoryItem(expirationDate)

        when: 'we upsert with the same lot number but with whitespace around it'
        String lotNumber = " ${existingLot.lotNumber} "
        InventoryItemByProductLot result = inventoryItemManager.upsertInventoryItems([
                new ProductLot(product: existingLot.product, lotNumber: lotNumber, expirationDate: expirationDate),
        ])

        then: 'the result is keyed on the lot with whitespace but the returned item is the existing one with no spaces'
        InventoryItem returnedItem = result.get(existingLot.product, lotNumber)
        assert returnedItem == existingLot
        assert returnedItem.lotNumber == existingLot.lotNumber
        assert InventoryItem.count() == 1
    }

    void 'upsertInventoryItems should find existing blank lots when given a null lot'() {
        given: 'a product whose default lot is stored with an empty lot number'
        Product product = new Product(name: 'Test product').save(validate: false)
        InventoryItem defaultLot = buildLot(product, '')

        when:
        InventoryItemByProductLot result = inventoryItemManager.upsertInventoryItems([
                new ProductLot(product: product, lotNumber: null),
        ])

        then:
        assert result.get(product, null) == defaultLot
        assert InventoryItem.count() == 1
    }

    void 'upsertInventoryItems should update the expiration date of an existing lot when it has changed'() {
        given:
        InventoryItem existingLot = buildInventoryItem(JavaUtilDateParser.asDate(LocalDate.of(2026, 9, 9)))

        when:
        InventoryItemByProductLot result = inventoryItemManager.upsertInventoryItems([
                new ProductLot(product: existingLot.product, lotNumber: 'LOT-1', expirationDate: expirationDate),
        ])

        then: 'the date is updated'
        assert result.get(existingLot.product, 'LOT-1') == existingLot
        assert existingLot.expirationDate == expirationDate
        assert InventoryItem.count() == 1
    }

    void 'upsertInventoryItems should clear the expiration date of an existing lot when none is given'() {
        given:
        InventoryItem existingLot = buildInventoryItem(expirationDate)

        when:
        InventoryItemByProductLot result = inventoryItemManager.upsertInventoryItems([
                new ProductLot(product: existingLot.product, lotNumber: 'LOT-1', expirationDate: null),
        ])

        then: 'the date is removed'
        assert result.get(existingLot.product, 'LOT-1') == existingLot
        assert existingLot.expirationDate == null
    }

    void 'upsertInventoryItems should only process the first of a repeated product lot'() {
        given:
        Product product = new Product(name: 'Test product').save(validate: false)
        Date otherExpirationDate = JavaUtilDateParser.asDate(LocalDate.of(2029, 1, 1))

        when: 'the same new lot is given twice, with different expiration dates'
        InventoryItemByProductLot result = inventoryItemManager.upsertInventoryItems([
                new ProductLot(product: product, lotNumber: 'LOT-1', expirationDate: expirationDate),
                new ProductLot(product: product, lotNumber: 'LOT-1', expirationDate: otherExpirationDate),
        ])

        then: 'a single lot is created, and the different date in the second item is ignored'
        assert result.size() == 1
        assert InventoryItem.count() == 1
        assert result.get(product, 'LOT-1').expirationDate == expirationDate
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
