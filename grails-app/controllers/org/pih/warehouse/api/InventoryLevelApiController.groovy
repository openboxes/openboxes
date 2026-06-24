package org.pih.warehouse.api

import grails.converters.JSON
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.pih.warehouse.core.DocumentService
import org.pih.warehouse.core.Location
import org.pih.warehouse.data.DataService
import org.pih.warehouse.importer.InventoryLevelImportDataService
import org.pih.warehouse.inventory.InventoryLevel
import org.springframework.http.HttpStatus

class InventoryLevelApiController {

    DataService dataService
    DocumentService documentService
    InventoryLevelImportDataService inventoryLevelImportDataService
    def inventoryLevelService
    def productAvailabilityService
    def inventorySnapshotService

    def list() {
        Location facility = Location.get(params.facilityId)
        if (!facility)
            throw new IllegalArgumentException("Unable to locate facility with id ${params.facilityId}")

        List inventoryLevels = InventoryLevel.createCriteria().list {
            eq("inventory", facility.inventory)
            isNull("internalLocation")
        }

        withFormat {
            "xls" {
                def data = dataService.transformObjects(inventoryLevels, InventoryLevel.PROPERTIES)
                documentService.generateExcel(response.outputStream, data)
                response.setHeader 'Content-disposition', "attachment; filename=\"inventory-levels.xls\""
                response.outputStream.flush()
                return
            }
            "csv" {
                String text = inventoryLevelImportDataService.exportInventoryLevels(inventoryLevels)
                response.contentType = "text/csv"
                response.setHeader("Content-disposition", "attachment; filename=\"inventory-levels.csv\"")
                render(text)
                return
            }

            "*" {
                render([data: inventoryLevels] as JSON)
            }
        }
    }

    def bulkUpsert() {
        Location facility = Location.get(params.facilityId)
        if (!facility) {
            throw new IllegalArgumentException("Unable to find facility with id ${params.facilityId}")
        }

        boolean deferRefresh = params.boolean('deferRefresh', false)
        def json = request.JSON
        if (!(json instanceof JSONArray)) {
            throw new IllegalArgumentException("Expected a JSON array of inventory levels")
        }

        List<UpsertResult> results = inventoryLevelService.bulkUpsert(facility, (JSONArray) json, deferRefresh)

        List<String> productIds = results.findAll { it.status == UpsertStatus.OK && it.productId }*.productId.unique()
        if (deferRefresh && productIds) {
            productAvailabilityService.triggerRefreshProductAvailability(facility.id, productIds, true)
            inventorySnapshotService.triggerRefreshInventorySnapshot(facility.id, productIds, true)
        }

        render([
            data    : results,
            metadata: [
                bulk: [
                    total  : results.size(),
                    created: results.count { it.action == UpsertAction.CREATED },
                    updated: results.count { it.action == UpsertAction.UPDATED },
                    errors : results.count { it.status == UpsertStatus.ERROR },
                    refresh: [
                        deferred         : deferRefresh,
                        refreshedFacility: facility.id,
                        refreshedProducts: deferRefresh ? productIds : null
                    ]
                ]
            ]
        ] as JSON)
    }

    def upsert() {
        Location facility = Location.get(params.facilityId)
        if (!facility) {
            throw new IllegalArgumentException("Unable to find facility with id ${params.facilityId}")
        }

        def json = request.JSON
        if (!(json instanceof JSONObject)) {
            throw new IllegalArgumentException("Expected a single inventory level object")
        }

        json.identifier = params.identifier

        UpsertResult result = inventoryLevelService.upsert(facility, json, false)

        if (result.status == UpsertStatus.ERROR) {
            response.status = HttpStatus.BAD_REQUEST.value()
        }

        render([data: result] as JSON)
    }
}
