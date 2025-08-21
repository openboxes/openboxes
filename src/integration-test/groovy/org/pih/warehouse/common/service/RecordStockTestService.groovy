package org.pih.warehouse.common.service

import grails.gorm.transactions.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.inventory.RecordStockApiWrapper
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.inventory.RecordInventoryCommand
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionIdentifierService
import org.pih.warehouse.product.Product

@Transactional
@TestComponent
class RecordStockTestService implements BaseTestService {

    @Autowired
    TransactionIdentifierService transactionIdentifierService

    @Autowired
    InventoryService inventoryService

    @Autowired
    ProductAvailabilityService productAvailabilityService

    @Autowired
    RecordStockApiWrapper recordStockApiWrapper

    /**
     * Performs a Record Stock operation, setting the quantity of a given item.
     */
    void recordStock(Location facility, RecordInventoryCommand command) {
        recordStockApiWrapper.saveRecordStockOK(facility, command)
    }

    /**
     * Deletes all transactions and their entries for the given product and facility that occur on the given
     * datetime or after.
     */
    void deleteTransactionsOnOrAfterDate(Location facility, Product product, Date date) {
        List<TransactionEntry> transactionEntries = getAllTransactionEntriesOnOrAfterDate(facility, product, date)
        transactionEntries.transaction.unique().each { it.delete() }
    }

    /**
     * Deletes all transactions and their entries for the given product and facility.
     */
    void deleteAllTransactions(Location facility, Product product) {
        deleteTransactionsOnOrAfterDate(facility, product, null)
    }

    private List<TransactionEntry> getAllTransactionEntriesOnOrAfterDate(Location facility, Product product, Date date){
        return TransactionEntry.createCriteria().list() {
            inventoryItem {
                // Needs to be product.id (instead of product) because we run into "unsaved transient instance"
                // Hibernate exceptions. Is it because the Product is created in a separate transaction? Unclear.
                eq("product.id", product.id)
            }
            transaction {
                eq("inventory", facility.inventory)
                // If date is null, delete ALL transactions for the given product and facility.
                if (date) {
                    ge("transactionDate", date)
                }
            }
        } as List<TransactionEntry>
    }
}
