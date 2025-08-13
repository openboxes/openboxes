package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional
import org.pih.warehouse.data.DataService

@Transactional
class InventoryCountService {

    DataService dataService

    void refreshAdjustmentCandidatesView(Inventory inventory, List<String> productIds, String transactionId, Date transactionDate) {
        productIds.each {
            String dateString = transactionDate.format("yyyy-MM-dd HH:mm:ss")
            String query = """
                INSERT INTO adjustments_candidates (
                    transaction_id,
                    product_id,
                    transaction_date,
                    inventory_id,
                    facility_id
                )
                VALUES (
                    '${transactionId}',
                    '${it}',
                    '${dateString}',
                    '${inventory.id}',
                    '${inventory.warehouse.id}'
                )
            """

            dataService.executeStatement(query, true)
        }
    }

    void refreshInventoryBaselineCandidatesView(Inventory inventory, List<String> productIds, String transactionId, Date transactionDate) {
        productIds.each {
            String dateString = transactionDate.format("yyyy-MM-dd HH:mm:ss")
            String query = """
                INSERT INTO inventory_baseline_candidates (
                    transaction_id,
                    product_id,
                    transaction_date,
                    inventory_id,
                    facility_id
                )
                VALUES (
                    '${transactionId}',
                    '${it}',
                    '${dateString}',
                    '${inventory.id}',
                    '${inventory.warehouse.id}'
                )
            """

            dataService.executeStatement(query, true)
        }
    }
}
