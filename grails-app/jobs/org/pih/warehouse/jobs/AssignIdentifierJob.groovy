package org.pih.warehouse.jobs

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.TransactionIdentifierService
import org.pih.warehouse.order.OrderIdentifierService
import org.pih.warehouse.product.ProductIdentifierService
import org.pih.warehouse.receiving.ReceiptIdentifierService
import org.pih.warehouse.requisition.RequisitionIdentifierService
import org.pih.warehouse.shipping.ShipmentIdentifierService

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

        // Run as the robot user so any records created/updated are stamped with a valid current
        // user. withNewSession provides the Hibernate session needed to look up the robot user.
        User.withNewSession {
            AuthService.withRobotUser {
                // Assume that each service will manage their own transactions
                productIdentifierService.generateForAllUnassignedIdentifiers()
                shipmentIdentifierService.generateForAllUnassignedIdentifiers()
                receiptIdentifierService.generateForAllUnassignedIdentifiers()
                orderIdentifierService.generateForAllUnassignedIdentifiers()
                requisitionIdentifierService.generateForAllUnassignedIdentifiers()
                transactionIdentifierService.generateForAllUnassignedIdentifiers()
            }
        }
    }
}
