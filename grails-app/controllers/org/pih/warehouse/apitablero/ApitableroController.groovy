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

    def getInProgressPutaways = {
        Location location = Location.get(session?.warehouse?.id)
        NumberData numberData = numberDataService.getInProgressPutaways(session.user, location)
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

     def getExpiredProductsInStock = {
        Location location = Location.get(session?.warehouse?.id)
        NumberData numberData = numberDataService.getExpiredProductsInStock(location)
        render (numberData as JSON)
    }

    def getExpirationSummary = {
        Location location = Location.get(session?.warehouse?.id)
        def expirationSummary = indicatorDataService.getExpirationSummaryData(location, params)
        render (expirationSummary.toJson() as JSON)
    }

    def getFillRate = {
        def fillRate = indicatorDataService.getFillRate()
        render (fillRate.toJson() as JSON)
    }

    def getInventorySummary = {
        Location location = Location.get(session?.warehouse?.id)
        def inventorySummary = indicatorDataService.getInventorySummaryData(location)
        render (inventorySummary.toJson() as JSON)
    }

    def getSentStockMovements = {
        Location location = Location.get(session?.warehouse?.id)
        def sentStockMovements = indicatorDataService.getSentStockMovements(location, params)
        render (sentStockMovements.toJson() as JSON)
    }

    def getReceivedStockMovements = {
        Location location = Location.get(session?.warehouse?.id)
        def receivedStockMovements = indicatorDataService.getReceivedStockData(location, params)
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

    def getProductWithNegativeInventory = {
        Location location = Location.get(session?.warehouse?.id)
        def productsWithNegativeInventory = numberDataService.getProductWithNegativeInventory(location)
        render (productsWithNegativeInventory as JSON)
    }
}
