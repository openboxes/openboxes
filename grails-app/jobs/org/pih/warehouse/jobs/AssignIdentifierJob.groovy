package org.pih.warehouse.jobs

import grails.gorm.transactions.Transactional

import org.pih.warehouse.inventory.TransactionIdentifierService
import org.pih.warehouse.order.OrderIdentifierService
import org.pih.warehouse.product.ProductIdentifierService
import org.pih.warehouse.receiving.ReceiptIdentifierService
import org.pih.warehouse.requisition.RequisitionIdentifierService
import org.pih.warehouse.shipping.ShipmentIdentifierService

@Transactional
class AssignIdentifierJob {

    // Every identifier service that implements BlankIdentifierResolver
    ProductIdentifierService productIdentifierService
    ShipmentIdentifierService shipmentIdentifierService
    ReceiptIdentifierService receiptIdentifierService
    OrderIdentifierService orderIdentifierService
    RequisitionIdentifierService requisitionIdentifierService
    TransactionIdentifierService transactionIdentifierService

    def sessionRequired = false

    static concurrent = false

    static triggers = {
        cron name: JobUtils.getCronName(AssignIdentifierJob),
            cronExpression: JobUtils.getCronExpression(AssignIdentifierJob)
    }

    void execute() {
        if (!JobUtils.shouldExecute(AssignIdentifierJob)) {
            return
        }

        productIdentifierService.generateForAllUnassignedIdentifiers()
        shipmentIdentifierService.generateForAllUnassignedIdentifiers()
        receiptIdentifierService.generateForAllUnassignedIdentifiers()
        orderIdentifierService.generateForAllUnassignedIdentifiers()
        requisitionIdentifierService.generateForAllUnassignedIdentifiers()
        transactionIdentifierService.generateForAllUnassignedIdentifiers()
    }
}
