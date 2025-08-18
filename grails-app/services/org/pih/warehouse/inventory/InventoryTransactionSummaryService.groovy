package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product
import org.pih.warehouse.report.CycleCountReportCommand

import javax.sql.DataSource
import java.sql.Timestamp

@Transactional
class InventoryTransactionSummaryService {

    DataSource dataSource

    List<InventoryTransactionsSummary> getInventoryTransactionsSummary(CycleCountReportCommand command) {
        List<InventoryTransactionsSummary> inventoryTransactions = InventoryTransactionsSummary.createCriteria().list(command.paginationParams) {
            eq("facility", command.facility)
            if (command.startDate) {
                gte("dateRecorded", command.startDate)
            }
            if (command.endDate) {
                lte("dateRecorded", command.endDate)
            }
            if (command.products) {
                "in"("product", command.products)
            }
            order("dateRecorded", "desc")
        }

        return inventoryTransactions
    }

    void refreshInventoryMovementSummaryView(RefreshInventoryTransactionsSummaryEvent event) {
        Sql sql = new Sql(dataSource)
        if (event.isDelete) {
            Map<String, Object> params = [
                    transactionId: event.transactionId
            ]
            String query = """
                DELETE FROM inventory_movement_summary WHERE transaction_id = :transactionId
            """
            sql.executeUpdate(params, query)
            return
        }
        event.products.each { key, entries ->
            Product product = Product.read(key)
            Timestamp transactionDate = new Timestamp(event.transactionDate.time)

            Map<String, Object> params = [
                    transactionId: event.transactionId,
                    productId: product.id,
                    transactionDate: transactionDate,
                    inventoryId: event.inventoryId,
                    productCode: product.productCode,
                    quantitySum: entries.sum { entry ->
                        event.transactionTypeId == Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID ? -entry.quantity : entry.quantity
                    }
            ]
            String query = """
                INSERT INTO inventory_movement_summary (
                    transaction_id,
                    product_id,
                    transaction_date,
                    inventory_id,
                    product_code,
                    quantity_sum
                )
                VALUES (
                    :transactionId,
                    :productId,
                    :transactionDate,
                    :inventoryId,
                    :productCode,
                    :quantitySum
                )
            """
            sql.executeInsert(params, query)
        }
    }

    void refreshProductInventorySummaryView(RefreshInventoryTransactionsSummaryEvent event) {
        Sql sql = new Sql(dataSource)
        if (event.isDelete) {
            Map<String, Object> params = [
                    transactionId: event.transactionId
            ]
            String query = """
                DELETE FROM product_inventory_summary WHERE transaction_id = :transactionId
            """
            sql.executeUpdate(params, query)
            return
        }
        Location facility = Location.findByInventory(Inventory.read(event.inventoryId))
        event.products.each { key, entries ->
            Product product = Product.read(key)
            Timestamp transactionDate = new Timestamp(event.transactionDate.time)

            Map<String, Object> params = [
                    transactionId: event.transactionId,
                    productId: product.id,
                    transactionDate: transactionDate,
                    inventoryId: event.inventoryId,
                    productCode: product.productCode,
                    quantityBalance: entries.sum { entry -> entry.quantity },
                    facilityId: facility.id,
            ]
            String query = """
                INSERT INTO product_inventory_summary (
                    transaction_id,
                    product_id,
                    transaction_date,
                    facility_id,
                    product_code,
                    quantity_balance
                )
                VALUES (
                    :transactionId,
                    :productId,
                    :transactionDate,
                    :facilityId,
                    :productCode,
                    :quantityBalance
                )
            """
            sql.executeInsert(params, query)
        }
    }
}
