package org.pih.warehouse.regression.platform.inventory

import org.pih.warehouse.inventory.*
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product

class TransactionEntryControllerSpec extends Specification implements ControllerUnitTest<TransactionEntryController>, DataTest {

    void setupSpec() {
        mockDomains(Transaction, TransactionEntry, TransactionType, InventoryItem, Product, Inventory, Location)
    }

    void setup() {
        controller.metaClass.warehouse = [message: { Map attrs -> attrs.code }]
    }

    void 'delete removes the entry from its transaction'() {
        given:
        Product product = new Product(name: 'Paracetamol').save(validate: false)
        InventoryItem lot = new InventoryItem(product: product, lotNumber: 'L1').save(validate: false)
        Transaction transaction = new Transaction(transactionDate: new Date())
        TransactionEntry entry = new TransactionEntry(inventoryItem: lot, product: product, quantity: 2)
        transaction.addToTransactionEntries(entry)
        transaction.save(validate: false)
        entry.save(validate: false)
        params.id = entry.id

        when:
        controller.delete()

        then:
        flash.message == 'default.deleted.message'
        transaction.transactionEntries.isEmpty()
        response.redirectedUrl == '/transactionEntry/index'
    }

    void 'delete reports a missing entry'() {
        given:
        params.id = 'missing'

        when:
        controller.delete()

        then:
        flash.message == 'default.not.found.message'
        response.redirectedUrl == '/transactionEntry/index'
    }
}
