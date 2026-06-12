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
        def body = request.JSON
        if (!(body instanceof JSONArray)) {
            throw new IllegalArgumentException("Expected a JSON array of inventory levels")
        }

        List<Map> results = inventoryLevelService.bulkUpsert(facility, (JSONArray) body, deferRefresh)

        List<String> productIds = results.findAll { it.status == 'ok' && it.productId }*.productId.unique()
        if (deferRefresh && productIds) {
            productAvailabilityService.triggerRefreshProductAvailability(facility.id, productIds, true)
            inventorySnapshotService.triggerRefreshInventorySnapshot(facility.id, productIds, true)
        }

        List<String> savedIds = results.findAll { it.status == 'ok' }*.inventoryLevelId
        List<InventoryLevel> savedLevels = savedIds ? InventoryLevel.getAll(savedIds) : []

        render([
            data    : savedLevels.collect { toJson(it) },
            metadata: [
                bulk  : [
                    total  : results.size(),
                    created: results.count { it.action == 'created' },
                    updated: results.count { it.action == 'updated' },
                    errors : results.count { it.status == 'error' },
                    refresh: [
                        deferred         : deferRefresh,
                        refreshedFacility: facility.id,
                        refreshedProducts: deferRefresh ? productIds : null
                    ]
                ],
                errors: results.findAll { it.status == 'error' }.collect {
                    [index: it.index, errorMessage: it.errorMessage, errors: it.errors]
                        .findAll { k, v -> v != null }
                }
            ]
        ] as JSON)
    }

    def upsert() {
        Location facility = Location.get(params.facilityId)
        if (!facility) {
            throw new IllegalArgumentException("Unable to find facility with id ${params.facilityId}")
        }

        def body = request.JSON
        if (!(body instanceof JSONObject)) {
            throw new IllegalArgumentException("Expected a single inventory level object")
        }

        body.identifier = params.identifier

        Map result = inventoryLevelService.bulkUpsert(facility, [body], false).first()

        if (result.status == 'error') {
            response.status = HttpStatus.BAD_REQUEST.value()
            render([errorCode: 400, errorMessage: result.errorMessage, errors: result.errors].findAll { k, v -> v != null } as JSON)
            return
        }

        render([data: toJson(InventoryLevel.get(result.inventoryLevelId))] as JSON)
    }

    private Map toJson(InventoryLevel inventoryLevel) {
        return [
            id                   : inventoryLevel.id,
            identifier           : inventoryLevel.identifier,
            scope                : inventoryLevel.inventoryLevelScope?.name(),
            product              : [id: inventoryLevel.product?.id, productCode: inventoryLevel.product?.productCode],
            status               : inventoryLevel.status?.name(),
            abcClass             : inventoryLevel.abcClass,
            cycleCountFrequencyDays: inventoryLevel.cycleCountFrequencyDays,
            internalLocation     : inventoryLevel.internalLocation?.id,
            preferredBinLocation : inventoryLevel.preferredBinLocation?.id,
            replenishmentLocation: inventoryLevel.replenishmentLocation?.id,
            sortOrder            : inventoryLevel.sortOrder,
            minQuantity          : inventoryLevel.minQuantity,
            reorderQuantity      : inventoryLevel.reorderQuantity,
            maxQuantity          : inventoryLevel.maxQuantity
        ]
    }
}
