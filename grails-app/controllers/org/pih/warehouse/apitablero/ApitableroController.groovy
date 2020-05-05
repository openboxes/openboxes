package org.pih.warehouse.apitablero

import grails.converters.JSON
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventorySnapshot
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.order.Order
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.tablero.NumberData
import org.pih.warehouse.tableroapi.NumberDataService
import org.codehaus.groovy.grails.web.json.JSONObject
import org.pih.warehouse.tableroapi.IndicatorDataService

class ApitableroController {

    def numberDataService
    def indicatorDataService
    def inventorySnapshotService

    def getInventoryByLotAndBin = {
        Location location = Location.get(session?.warehouse?.id)
        NumberData numberData = numberDataService.getInventoryByLotAndBin(location)
        render (numberData as JSON)
    }

    def getInProgressShipments = {
        Location location = Location.get(session?.warehouse?.id)
        NumberData numberData = numberDataService.getInProgressShipments(session.user, location)
        render (numberData as JSON)
    }

    def getInProgresPutaways = {
        Location location = Location.get(session?.warehouse?.id)
        NumberData numberData = numberDataService.getInProgresPutaways(session.user, location)
        render (numberData as JSON)
    }

    def getReceivingBin = {
        Location location = Location.get(session?.warehouse?.id)
        NumberData numberData = numberDataService.getReceivingBin(location)
        render (numberData as JSON)
    }

    def getItemsInventoried = {
        Location location = Location.get(session?.warehouse?.id)
        NumberData numberData = numberDataService.getItemsInventoried(location)
        render (numberData as JSON)
    }

    def getDefaultBin = {
        Location location = Location.get(session?.warehouse?.id)
        NumberData numberData = numberDataService.getDefaultBin(location)
        render (numberData as JSON)
    }

    def getExpirationSummary = {
        Location location = Location.get(session?.warehouse?.id)
        def expirationSummary = indicatorDataService.getExpirationSummaryData(location, params)["data"]
        render (expirationSummary.toJson() as JSON)
    }

    def getFillRate = {
        def fillRate = indicatorDataService.getFillRate()["data"]
        render (fillRate.toJson() as JSON)
    }

    def getInventorySummary = {
        Location location = Location.get(session?.warehouse?.id)
        def results = inventorySnapshotService.findInventorySnapshotByLocation(location)
        def inventorySummary = indicatorDataService.getInventorySummaryData(results)["data"]
        render (inventorySummary.toJson() as JSON)
    }

    def getSentStockMovements = {
        Location location = Location.get(session?.warehouse?.id)
        def sentStockMovements = indicatorDataService.getSentStockMovements(location, params)["data"]
        render (sentStockMovements.toJson() as JSON)
    }

    def getReceivedStockMovements = {
        Location location = Location.get(session?.warehouse?.id)
        def receivedStockMovements = indicatorDataService.getReceivedStockData(location, params)["data"]
        render (receivedStockMovements.toJson() as JSON)
    }

    def getOutgoingStock = {
        Location location = Location.get(session?.warehouse?.id)
        def outgoingStock = indicatorDataService.getOutgoingStock(location)
        render (outgoingStock.toJson() as JSON)
    }

    def getIncomingStock = {
        Location location = Location.get(session?.warehouse?.id)
        def incomingStock = indicatorDataService.getIncomingStock(location)
        render (incomingStock.toJson() as JSON)
    }

    def getDiscrepancy = {
        Location location = Location.get(session?.warehouse?.id)
        def discrepancy = indicatorDataService.getDiscrepancy(location, params)
        render (discrepancy as JSON)
    }

    def getDelayedShipments = {
        Location location = Location.get(session?.warehouse?.id)
        def delayedShipments = indicatorDataService.getDelayedShipments(location)
        render (delayedShipments as JSON)
    }
}
