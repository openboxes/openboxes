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
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.hibernate.criterion.CriteriaSpecification
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionCode
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.jobs.DataMigrationJob
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAvailability
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
    def locationService
    def productAvailabilityService

    def index = {

    }


    def dataQuality = {

    }

    def dataMigration = {

        def organizations = migrationService.getSuppliersForMigration()
        def productSuppliers = migrationService.getProductsForMigration()
        TransactionType inventoryTransactionType = TransactionType.load(Constants.INVENTORY_TRANSACTION_TYPE_ID)
        def inventoryTransactionCount = Transaction.countByTransactionType(inventoryTransactionType)

        [
                organizationCount        : organizations.size(),
                inventoryTransactionCount: inventoryTransactionCount,
                productSupplierCount     : productSuppliers.size(),

        ]
    }

    def dimensionTables = {
        [
                locationDimensionCount: LocationDimension.count(),
                productDimensionCount : ProductDimension.count(),
                lotDimensionCount     : LotDimension.count(),
                dateDimensionCount    : DateDimension.count(),
        ]
    }

    def factTables = {
        def stockoutFactCount = dataService.executeQuery("select count(*) as count from stockout_fact")[0]?.count ?: 0
        [
                transactionFactCount     : TransactionFact.count(),
                consumptionFactCount     : ConsumptionFact.count(),
                stockoutFactCount        : stockoutFactCount,
        ]
    }

    def materializedViews = {
        def productDemandCount = dataService.executeQuery("select count(*) as count from product_demand_details")[0]?.count ?: 0
        def productAvailabilityCount = dataService.executeQuery("select count(*) as count from product_availability")[0]?.count ?: 0

        [
                productDemandCount       : productDemandCount,
                productAvailabilityCount : productAvailabilityCount
        ]
    }

    def stockMovementsWithoutShipmentItems = {
        def g = ApplicationHolder.application.mainContext.getBean( 'org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib' )
        def data = migrationService.stockMovementsWithoutShipmentItems
        // id, status, request_number, date_created, origin, requested, picked, shipped
        if ("count".equals(params.format)) {
            render data.size()
            return
        }
        else {
            data = data.collect {
                def href = g.createLink(controller: "stockMovement", action: "show", id: it?.id)
                [
                        identifier : "<a target='_blank' href='${href}'>${it?.request_number}</a>",
                        status     : it.status,
                        dateCreated: it.date_created,
                        origin     : it.origin,
                        requested  : it.requested,
                        picked     : it.picked,
                        shipped    : it.shipped,
                        issued    : it.issued,
                ]
            }.sort { it?.dateCreated }
            render(template: "/common/dataTable", model: [data: data])
        }
    }

    def loadProductAvailability = {
        Location location = Location.get(params.id)
        def results = ProductAvailability.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                count("id", "count")
            }
            eq("location", location)
        }
        def count = results ? results[0].count : null
        render (status: 200, text: count)
    }


    def calculateProductAvailability = {
        Location location = Location.get(params.id)
        def binLocations = productAvailabilityService.calculateBinLocations(location)
        render (status: 200, text: binLocations.size())
    }

    def productAvailability = {

        def countByLocation = ProductAvailability.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                count("id", "count")
                groupProperty("location", "location")
            }
        }

        def data = locationService.depots.collect { Location location ->
            def count = countByLocation.find { it.location == location }?.count?:null
            [
                    "Location" : location.name,
                    "Product Availability":
                            "<div data-url=\"${request.contextPath}/migration/loadProductAvailability/${location.id}\" class=\"fetch-indicator\">${count}</div>",
                    "Calculated":
                            "<div data-url=\"${request.contextPath}/migration/calculateProductAvailability/${location.id}\" class=\"fetch-indicator\">Fetch</div>",
                    "Actions":
                            "<a data-title=\"${location.name}\" class=\"button btn-show-dialog\" data-url=\"${request.contextPath}/migration/compareProductAvailability?location.id=${location.id}\">show diff</a>" +
                            "<a data-title=\"${location.name}\" class=\"button btn-show-dialog\" data-url=\"${request.contextPath}/migration/compareProductAvailability?location.id=${location.id}&showAll=true\">show all</a>" +
                            "<a class=\"button btn-post-data\" data-url=\"${request.contextPath}/migration/refreshProductAvailability?location.id=${location.id}\">refresh</a>"
            ]
        }.sort { it.count }

        [data:data]
    }

    def refreshProductAvailability = {
        Location location = params?.location ?
                Location.get(params.location?.id) :
                Location.get(session.warehouse.id)

        productAvailabilityService.refreshProductAvailability(location, true)
        render (status: 200, text: "Refreshed product availability for location ${location.name}")
    }

    def compareProductAvailability = {

        log.info "params " + params

        Location location = params?.location ?
                Location.get(params.location?.id) :
                Location.get(session.warehouse.id)


        if ("count".equals(params.format)) {
            render ProductAvailability.count()
            return
        }
        else {
            def showAll = params.showAll?:false
            def startTime = System.currentTimeMillis()
            def binLocations = productAvailabilityService.calculateBinLocations(location)
            log.info "calculate time: " + (System.currentTimeMillis()-startTime)

            startTime = System.currentTimeMillis()
            def data = ProductAvailability.findAllByLocation(location)
            data = data.collect { ProductAvailability pa ->
                def binLocation = binLocations.find {
                    it.product?.id == pa.product?.id &&
                            it.inventoryItem?.id == pa.inventoryItem?.id &&
                            it.binLocation?.id == pa.binLocation?.id
                }
                log.info "bin location " + binLocation
                binLocations.remove(binLocation)
                return [
                        productCode : pa?.productCode,
                        lotNumber : pa?.lotNumber,
                        binLocation : pa?.binLocationName,
                        quantityFromProductAvailability: pa.quantityOnHand?:0,
                        quantityFromTransactions: binLocation?.quantity,
                        includedInProductAvailability: true
                ]
            }.findAll { it.quantityFromProductAvailability != it.quantityFromTransactions || showAll }

            log.info "binLocations " + binLocations

            def binLocationsRemaining = binLocations.collect {
                [
                        productCode: it?.product?.productCode,
                        lotNumber : it?.inventoryItem?.lotNumber,
                        binLocation : it?.binLocation?.name,
                        quantityFromProductAvailability : null,
                        quantityFromTransactions : it.quantity,
                        includedInProductAvailability: false
                ]
            }

            data.addAll(binLocationsRemaining)
            log.info "time: " + (System.currentTimeMillis()-startTime)
            render(template: "/common/dataTable", model: [data: data])
        }
    }


    def receiptsWithoutTransaction = {
        def g = ApplicationHolder.application.mainContext.getBean( 'org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib' )
        def data = migrationService.getReceiptsWithoutTransaction()
        if ("count".equals(params.format)) {
            render data.size()
            return
        }
        else {
            data = data.collect {
                def href = g.createLink(controller: "stockMovement", action: "show", id: it?.shipment?.id)
                [
                        shipmentNumber: "<a target='_blank' href='${href}'>${it.shipment?.shipmentNumber}</a>",
                        shipmentStatus: it.shipment?.currentStatus,
                        shipmentName  : it?.shipment?.name,
                        receiptNumber : it.receiptNumber,
                        receiptStatus : it.receiptStatusCode.name(),
                ]
            }.sort { it?.shipmentNumber }
            render(template: "/common/dataTable", model: [data: data])
        }
    }

    def shipmentsWithoutTransactions = {
        def data = migrationService.shipmentsWithoutTransactions
        if ("count".equals(params.format)) {
            render data.size()
            return
        }
        else {
            data = data.collect {
                [
                        shipmentNumber: "<a target='_blank' href='${href}'>${it?.shipmentNumber}</a>",
                        shipmentStatus: it.currentStatus.name(),
                        origin        : it.origin.name,
                        destination   : it.destination.name
                ]
            }
            render(template: "/common/dataTable", model: [data: data])
        }
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
