/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.data

import grails.converters.JSON
import grails.validation.ValidationException
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.PartyRole
import org.pih.warehouse.core.PartyType
import org.pih.warehouse.core.PreferenceTypeCode
import org.pih.warehouse.core.RatingTypeCode
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductSupplier
import org.springframework.validation.Errors

class MigrationController {

    def migrationService
    def inventoryService

    def index = {
        def location = Location.get(session.warehouse.id)

        TransactionType inventoryTransactionType = TransactionType.load(Constants.INVENTORY_TRANSACTION_TYPE_ID)
        def inventoryTransactionCount = Transaction.countByTransactionTypeAndInventory(inventoryTransactionType, location.inventory)

        [inventoryTransactionCount:inventoryTransactionCount]
    }


    def nextInventoryTransaction = {
        def location = Location.get(session.warehouse.id)

        params.max = params.max ? params.int('max') : 1

        // FIXME This might be an expensive query just to get a single product
        def transactionEntries = migrationService.getInventoryTransactionEntries(location, 1)
//        transactionEntries = transactionEntries.collect { [
//                transactionNumber: it.transaction.transactionNumber,
//                transactionDate: it.transaction.transactionDate,
//                transactionType: it.transaction.transactionType.name,
//                transactionCode: it.transaction.transactionType.transactionCode.name(),
//                productCode: it.inventoryItem.product.productCode,
//                productName: it.inventoryItem.product.name,
//                lotNumber: it.inventoryItem.lotNumber,
//                quantity: it.quantity
//            ]
//        }

//        render ([transactionEntries: transactionEntries] as JSON)
        def product = transactionEntries[0]?.inventoryItem?.product
        if (product) {
            redirect(controller: "inventoryItem", action: "showStockCard", id: product.id)
        }
        else {
            render "No inventory transactions"
        }
    }


    def migrateInventoryTransactions = {
        def startTime = System.currentTimeMillis()
        def location = Location.get(session.warehouse.id)

        params.max = params.max ? params.int('max') : 1

        boolean performMigration = params.boolean("performMigration") ?: false
        def results = migrationService.migrateInventoryTransactions(location, params.max, performMigration)

        log.info "Migrated in ${(System.currentTimeMillis() - startTime)} ms"
        render ([results:results] as JSON)

    }


//    def migrateInventoryTransactions = {
//
//        def startTime = System.currentTimeMillis()
//        def location = Location.get(session.warehouse.id)
//
//        def results = migrationService.migrateInventoryTransactions(location)
//
////        String csv = dataService.generateCsv(results)
////        response.setHeader("Content-disposition", "attachment; filename='InventoryTransactions-${location.name}.csv'")
////        render(contentType:"text/csv", text: csv.toString(), encoding:"UTF-8")
////
//        log.info "Migrated in ${(System.currentTimeMillis() - startTime)} ms"
//        render ([results:results] as JSON)
//    }


    def migrateProductSuppliers = { MigrationCommand command ->
        def startTime = System.currentTimeMillis()
        try {
//            def products = migrationService.getProductsForMigration()
//            if (params.preview) {
//                def message = "You will migrate ${products.size()} products"
//                render(template: "status", model: [message: message])
//                return
//            }
            def migratedList = migrationService.migrateProductSuppliersInParallel()
            render(template: "status", model: [message: "Migrated ${migratedList.size()} products in ${System.currentTimeMillis()-startTime} ms"])

        } catch (Exception e) {
            command.errors.reject("productSupplier.error.message", e.message)
            render(template: "status", model: [command: command])

        } catch (ValidationException e) {
            command.errors = e.errors
            render(template: "status", model: [command: command])
        }
    }


    def migrateOrganizations = { MigrationCommand command ->
        def startTime = System.currentTimeMillis()
        try {
//            def suppliers = migrationService.getSuppliersForMigration()
//            if (params.preview) {
//                render(template: "status", model: [message: "You will migrate ${suppliers.size()} organizations"])
//                return
//            }
//
            def migratedList = migrationService.migrationOrganizationsInParallel()
            render(template: "status", model: [message: "Migrated ${migratedList.size()} organizations in ${System.currentTimeMillis()-startTime} ms"])


        } catch (ValidationException e) {
            command.errors = e.errors
            render(template: "status", model: [command: command])
            return
        }
    }






    def deleteOrganizations = {
        def startTime = System.currentTimeMillis()
        def orgCount = migrationService.deleteOrganizations()
        render(template: "status", model: [message: "Deleted ${orgCount} organizations in ${System.currentTimeMillis()-startTime} ms"])
    }

    def deleteProductSuppliers = {
        def startTime = System.currentTimeMillis()
        def productSupplierCount = migrationService.deleteProductSuppliers()
        render(template: "status", model: [message: "Deleted ${productSupplierCount} product suppliers in ${System.currentTimeMillis()-startTime} ms"])

    }



}

class MigrationCommand {

}