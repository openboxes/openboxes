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
IndicatorDataService indicator = new IndicatorDataService();

def index = { render("You are in the api") }

def getNumberData = {
    render  numberDataService.getListNumberData(location) as JSON;
}

def getExpirationSummary = {
    def expirationData = dashboardService.getExpirationSummary(location);
    render indicator.getExpirationSummaryData(expirationData)["data"].toJson() as JSON;
}

def getFillRate = {
    render indicator.getFillRate()["data"].toJson() as JSON
}

def getInventorySummary = {
    def results = inventorySnapshotService.findInventorySnapshotByLocation(location)
    render indicator.getInventorySummaryData(results)["data"].toJson() as JSON;
}

def getSentStockMovements = {
    render indicator.getSentStockMovements(location)["data"].toJson() as JSON
}

def getReceivedStockMovements = {
    render indicator.getReceivedStockData(location)["data"].toJson() as JSON;
}

def getOutgoingStock = {
    render indicator.getOutgoingStock(location).toJson() as JSON;
}

def getInComingStock = {
    render indicator.getInComingStock(location).toJson() as JSON;
}
}
