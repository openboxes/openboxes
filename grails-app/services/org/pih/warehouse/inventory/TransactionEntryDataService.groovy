package org.pih.warehouse.inventory

import grails.gorm.services.Query
import grails.gorm.services.Service
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.Category


@Service(TransactionEntry)
interface TransactionEntryDataService {
    @Query("""
        from ${TransactionEntry transactionEntry}
        join fetch ${Transaction transaction = transactionEntry.transaction}
        join fetch ${InventoryItem inventoryItem = transactionEntry.inventoryItem}
        join fetch ${TransactionType transactionType = transaction.transactionType}
        join fetch ${Product product = inventoryItem.product}
        join fetch ${Category category = product.category}
        left join fetch ${Product basicProduct = transactionEntry.product}
        left join fetch ${Location binLocation = transactionEntry.binLocation}
        where $transaction.inventory = $location.inventory 
        order by $transaction.transactionDate asc, $transaction.dateCreated asc
    """)
    List<TransactionEntry> findAllByLocation(Location location)
}
