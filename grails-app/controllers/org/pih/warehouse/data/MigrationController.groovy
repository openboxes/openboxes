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
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionCode
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.jobs.DataMigrationJob
import org.pih.warehouse.product.Product
import org.pih.warehouse.reporting.ConsumptionFact
import org.pih.warehouse.reporting.DateDimension
import org.pih.warehouse.reporting.LocationDimension
import org.pih.warehouse.reporting.LotDimension
import org.pih.warehouse.reporting.ProductDimension
import org.pih.warehouse.reporting.TransactionFact

class MigrationController {

    def dataService
    def migrationService
    def inventoryService

    def index = {

        def organizations = migrationService.getSuppliersForMigration()

        def productSuppliers = migrationService.getProductsForMigration()

        def receiptsWithoutTransaction = migrationService.getReceiptsWithoutTransaction()

        def shipmentsWithoutTransaction = migrationService.getShipmentsWithoutTransaction()

        TransactionType inventoryTransactionType = TransactionType.load(Constants.INVENTORY_TRANSACTION_TYPE_ID)
        def inventoryTransactionCount = Transaction.countByTransactionType(inventoryTransactionType)
        [
                organizationCount               : organizations.size(),
                receiptsWithoutTransactionCount : receiptsWithoutTransaction.size(),
                shipmentsWithoutTransactionCount: shipmentsWithoutTransaction.size(),
                inventoryTransactionCount       : inventoryTransactionCount,
                productSupplierCount            : productSuppliers.size(),
                transactionFactCount            : TransactionFact.count(),
                consumptionFactCount            : ConsumptionFact.count(),
                locationDimensionCount          : LocationDimension.count(),
                productDimensionCount           : ProductDimension.count(),
                lotDimensionCount               : LotDimension.count(),
                dateDimensionCount              : DateDimension.count(),
        ]
    }

    def receiptsWithoutTransaction = {
        def receiptsWithoutTransaction = migrationService.getReceiptsWithoutTransaction()
        receiptsWithoutTransaction = receiptsWithoutTransaction.collect {
            [status: it.receiptStatusCode.name(), receiptNumber: it.receiptNumber, shipmentNumber: it.shipment?.shipmentNumber]
        }.sort { it?.shipmentNumber }
        render(receiptsWithoutTransaction as JSON)
    }

    def shipmentsWithoutTransaction = {
        def shipmentsWithoutTransaction = migrationService.getShipmentsWithoutTransaction()
        shipmentsWithoutTransaction = shipmentsWithoutTransaction.collect {
            [status: it.currentStatus.name(), shipmentNumber: it.shipmentNumber, origin: it.origin.name, destination: it.destination.name]
        }
        render(shipmentsWithoutTransaction as JSON)
    }

    def downloadCurrentInventory = {
        def startTime = System.currentTimeMillis()
        def location = Location.get(session.warehouse.id)

        def data = migrationService.getCurrentInventory([location])
        if (params.format == "csv") {
            def csv = dataService.generateCsv(data)
            response.setHeader("Content-disposition", "attachment; filename='CurrentInventory_${location.name}.csv'")
            render(contentType: "text/csv", text: csv)
            return
        }
        render([responseTime: (System.currentTimeMillis() - startTime), count: data.size(), results: data] as JSON)
    }

    def locationsWithInventoryTransactions = {
        def locations = migrationService.getLocationsWithTransactions([TransactionCode.INVENTORY])
        render([count: locations.size(), locations: locations] as JSON)
    }


    def productsWithInventoryTransactions = {
        def location = Location.get(session.warehouse.id)
        def products = migrationService.getProductsWithTransactions(location, [TransactionCode.INVENTORY])
        products = products.collect { [productCode: it.productCode] }
        render([products: products] as JSON)
    }

    def nextInventoryTransaction = {
        def location = Location.get(session.warehouse.id)
        def products = migrationService.getProductsWithTransactions(location, [TransactionCode.INVENTORY])
        def product = products[0]
        if (product) {
            redirect(controller: "inventoryItem", action: "showStockCard", id: product.id)
        } else {
            render "No inventory transactions"
        }
    }

    def migrateProduct = {
        def location = Location.get(session.warehouse.id)
        Product product = Product.get(params.id)
        try {
            def results = migrationService.migrateInventoryTransactions(location, product, true)
            flash.message = "Migrated product ${product.productCode}"
            log.info("Results: " + results)
        } catch (Exception e) {
            log.error("Unable to migrate product ${product.productCode} due to error: " + e.message, e)
            flash.message = "Unable to migrate product ${product.productCode} due to error: " + e.message
        }
        redirect(controller: "inventoryItem", action: "showStockCard", id: params.id)
    }

    def migrateAllInventoryTransactions = {
        DataMigrationJob.triggerNow([:])
        flash.message = "Triggered data migration job in background"
        redirect(controller: "migration")
    }

    def migrateInventoryTransactions = {
        def startTime = System.currentTimeMillis()
        def location = Location.get(session.warehouse.id)

        params.max = params.max ? params.int('max') : 1

        boolean performMigration = params.boolean("performMigration") ?: false
        def results = migrationService.migrateInventoryTransactions(location, performMigration)

        def responseTime = System.currentTimeMillis() - startTime
        log.info "Migrated in ${(responseTime)} ms"

        if (params.format == "csv") {
            results.remove("stockHistory")
            def data = dataService.generateCsv(results)
            response.setHeader("Content-disposition", "attachment; filename='MigrateInventoryTransactions.csv'")
            render(contentType: "text/csv", text: data)
            return
        }

        render([responseTime: responseTime, count: results.size(), results: results] as JSON)

    }


    def migrateProductSuppliers = { MigrationCommand command ->
        def startTime = System.currentTimeMillis()
        try {
            def migratedList = migrationService.migrateProductSuppliersInParallel()
            render(template: "status", model: [message: "Migrated ${migratedList.size()} products in ${System.currentTimeMillis() - startTime} ms"])
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
            def migratedList = migrationService.migrationOrganizationsInParallel()
            render(template: "status", model: [message: "Migrated ${migratedList.size()} organizations in ${System.currentTimeMillis() - startTime} ms"])
        } catch (ValidationException e) {
            command.errors = e.errors
            render(template: "status", model: [command: command])
            return
        }
    }


    def deleteOrganizations = {
        def startTime = System.currentTimeMillis()
        def orgCount = migrationService.deleteOrganizations()
        render(template: "status", model: [message: "Deleted ${orgCount} organizations in ${System.currentTimeMillis() - startTime} ms"])
    }

    def deleteProductSuppliers = {
        def startTime = System.currentTimeMillis()
        def productSupplierCount = migrationService.deleteProductSuppliers()
        render(template: "status", model: [message: "Deleted ${productSupplierCount} product suppliers in ${System.currentTimeMillis() - startTime} ms"])

    }


}

class MigrationCommand {

}
