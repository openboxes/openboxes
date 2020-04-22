package org.pih.warehouse.apitablero

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import org.pih.warehouse.core.Location
import org.pih.warehouse.tablero.NumberData

@Transactional
class ApitableroController {

    def numberDataService
    def indicatorDataService
    def inventorySnapshotService

    def getNumberData() {
        Location location = Location.get(session?.warehouse?.id)
        List<NumberData> numberData = numberDataService.getListNumberData(session.user, location)
        render (numberData as JSON)
    }

    def getExpirationSummary() {
        Location location = Location.get(session?.warehouse?.id)
        def expirationSummary = indicatorDataService.getExpirationSummaryData(location, params)["data"]
        render (expirationSummary.toJson() as JSON)
    }

    def getFillRate() {
        def fillRate = indicatorDataService.getFillRate()["data"]
        render (fillRate.toJson() as JSON)
    }

    def getInventorySummary() {
        Location location = Location.get(session?.warehouse?.id)
        def results = inventorySnapshotService.findInventorySnapshotByLocation(location)
        def inventorySummary = indicatorDataService.getInventorySummaryData(results)["data"]
        render (inventorySummary.toJson() as JSON)
    }

    def getSentStockMovements() {
        Location location = Location.get(session?.warehouse?.id)
        def sentStockMovements = indicatorDataService.getSentStockMovements(location, params)["data"]
        render (sentStockMovements.toJson() as JSON)
    }

    def getReceivedStockMovements() {
        Location location = Location.get(session?.warehouse?.id)
        def receivedStockMovements = indicatorDataService.getReceivedStockData(location, params)["data"]
        render (receivedStockMovements.toJson() as JSON)
    }

    def getOutgoingStock() {
        Location location = Location.get(session?.warehouse?.id)
        def outgoingStock = indicatorDataService.getOutgoingStock(location)
        render (outgoingStock.toJson() as JSON)
    }

    def getIncomingStock() {
        Location location = Location.get(session?.warehouse?.id)
        def incomingStock = indicatorDataService.getIncomingStock(location)
        render (incomingStock.toJson() as JSON)
    }

    def getDiscrepancy() {
        Location location = Location.get(session?.warehouse?.id)
        def discrepancy = indicatorDataService.getDiscrepancy(location, params)
        render (discrepancy as JSON)
    }
}
