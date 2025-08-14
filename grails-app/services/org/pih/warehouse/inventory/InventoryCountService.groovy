package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import org.pih.warehouse.core.Constants

import javax.sql.DataSource

@Transactional
class InventoryCountService {

    DataSource dataSource

    void refreshAdjustmentCandidatesView(Inventory inventory, List<String> productIds, String transactionId, Date transactionDate) {
        productIds.each {
            String dateString = transactionDate.format(Constants.ISO_DATE_TIME_FORMAT)
            Sql sql = new Sql(dataSource)
            Map<String, Object> params = [
                    transactionId: transactionId,
                    productId: it,
                    transactionDate: dateString,
                    inventoryId: inventory.id,
                    facilityId: inventory.warehouse.id
            ]
            String query = """
                INSERT INTO adjustment_candidate (
                    transaction_id,
                    product_id,
                    transaction_date,
                    inventory_id,
                    facility_id
                )
                VALUES (
                    :transactionId,
                    :productId,
                    :transactionDate,
                    :inventoryId,
                    :facilityId
                )
            """
            sql.executeInsert(params, query)
        }
    }

    void refreshInventoryBaselineCandidatesView(Inventory inventory, List<String> productIds, String transactionId, Date transactionDate) {
        productIds.each {
            String dateString = transactionDate.format(Constants.ISO_DATE_TIME_FORMAT)
            Sql sql = new Sql(dataSource)
            Map<String, Object> params = [
                    transactionId: transactionId,
                    productId: it,
                    transactionDate: dateString,
                    inventoryId: inventory.id,
                    facilityId: inventory.warehouse.id
            ]
            String query = """
                INSERT INTO inventory_baseline_candidate (
                    transaction_id,
                    product_id,
                    transaction_date,
                    inventory_id,
                    facility_id
                )
                VALUES (
                    :transactionId,
                    :productId,
                    :transactionDate,
                    :inventoryId,
                    :facilityId
                )
            """
            sql.executeInsert(params, query)
        }
    }
}
