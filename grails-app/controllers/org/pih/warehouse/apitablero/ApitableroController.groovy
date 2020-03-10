package org.pih.warehouse.apitablero

import grails.converters.JSON
import org.pih.warehouse.tableroapi.NumberDataService
import org.pih.warehouse.tableroapi.IndicatorDataService
import org.pih.warehouse.tablero.NumberData
import org.codehaus.groovy.grails.web.json.JSONObject
import org.pih.warehouse.core.Location

class ApitableroController {

def dashboardService
def inventorySnapshotService

Location location = Location.get(session.warehouse?.id)
NumberDataService numberDataService = new NumberDataService();
IndicatorDataService indicatorDataService = new IndicatorDataService();

def getNumberData = {
    render  numberDataService.getListNumberData(session.user, location) as JSON;
}

def getExpirationSummary = {
    def expirationData = dashboardService.getExpirationSummary(location);
    render indicatorDataService.getExpirationSummaryData(expirationData)["data"].toJson() as JSON;
}

def getFillRate = {
    render indicatorDataService.getFillRate()["data"].toJson() as JSON
}

def getInventorySummary = {
    def results = inventorySnapshotService.findInventorySnapshotByLocation(location)
    render indicatorDataService.getInventorySummaryData(results)["data"].toJson() as JSON;
}

def getSentStockMovements = {
    render indicatorDataService.getSentStockMovements(location)["data"].toJson() as JSON
}

def getReceivedStockMovements = {
    render indicatorDataService.getReceivedStockData(location)["data"].toJson() as JSON;
}

def getOutgoingStock = {
    render indicatorDataService.getOutgoingStock(location).toJson() as JSON;
}

def getInComingStock = {
    render indicatorDataService.getInComingStock(location).toJson() as JSON;
}
}
