package org.pih.warehouse.apitablero


import grails.converters.JSON
import org.pih.warehouse.tableroapi.NumberDataService
import org.pih.warehouse.tableroapi.IndicatorDataService
import org.pih.warehouse.tablero.NumberData
import org.codehaus.groovy.grails.web.json.JSONObject
import org.pih.warehouse.core.Location

import org.pih.warehouse.inventory.InventoryStatus


class ApitableroController {

def dashboardService
def inventoryService
def inventorySnapshotService

Location location = Location.get(session.warehouse?.id)
NumberDataService numberDataService = new NumberDataService();
IndicatorDataService indicator = new IndicatorDataService();

def index = { render("You are in the api") }

def getNumberData = {
    def binLocations = inventorySnapshotService.getQuantityOnHandByBinLocation(location);
    def binLocationData = inventoryService.getBinLocationSummary(binLocations);
    render  numberDataService.getListNumberData(binLocationData) as JSON;
}

def getExpirationSummary = {
    def expirationData = dashboardService.getExpirationSummary(location);
    render indicator.getExpirationSummaryData(expirationData)["data"].toJson() as JSON;
}

def getFillRate = {
    render indicator.getFillRate()["data"].toJson() as JSON
}

def getSentStockMovements = {
    render indicator.getSentStockMovements()["data"].toJson() as JSON
}

def getInventorySummary = {
    def results = inventorySnapshotService.findInventorySnapshotByLocation(location)

    def inStockCount = results.findAll {
        it.quantityOnHand > 0 && it.status == InventoryStatus.SUPPORTED
    }.size()
    def lowStockCount = results.findAll {
        it.quantityOnHand > 0 && it.quantityOnHand <= it.minQuantity && it.status == InventoryStatus.SUPPORTED
    }.size()
    def reoderStockCount = results.findAll {
        it.quantityOnHand > it.minQuantity && it.quantityOnHand <= it.reorderQuantity && it.status == InventoryStatus.SUPPORTED
        }.size()
    def overStockCount = results.findAll {
        it.quantityOnHand > it.reorderQuantity && it.quantityOnHand <= it.maxQuantity && it.status == InventoryStatus.SUPPORTED
    }.size()
    def stockOutCount = results.findAll {
        it.quantityOnHand <= 0 && it.status == InventoryStatus.SUPPORTED
    }.size()
    def totalCount = results.size()
    
    def res = [
                totalCount      : totalCount,
                inStockCount    : inStockCount,
                lowStockCount   : lowStockCount,
                reoderStockCount: reoderStockCount,
                overStockCount  : overStockCount,
                stockOutCount   : stockOutCount
            ];
    
    render indicator.getInventorySummaryData(res)["data"].toJson() as JSON;
}

def getReceivedStockMovements = {
    render indicator.getReceivedStockData()["data"].toJson() as JSON;
}

def getOutgoingStock = {
    render indicator.getOutgoingStock().toJson() as JSON;
}
}
