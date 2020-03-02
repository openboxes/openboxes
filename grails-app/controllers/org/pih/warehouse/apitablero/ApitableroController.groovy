package org.pih.warehouse.apitablero


import grails.converters.JSON
import org.pih.warehouse.tableroapi.NumberDataService
import org.pih.warehouse.tableroapi.IndicatorDataService
import org.pih.warehouse.tablero.NumberData
import org.codehaus.groovy.grails.web.json.JSONObject

class ApitableroController {

IndicatorDataService indicator = new IndicatorDataService();
NumberDataService numberDataService = new NumberDataService();

def index= {
    render("You are in the api")
}

def getNumberData= {
    render  numberDataService.getListNumberData() as JSON;
}

def getExpirationSummary= {
    render indicator.getExpirationSummaryData().toJson() as JSON;
}

def getFillRate= {
    render indicator.getFillRate().toJson() as JSON
}

def getSentStockMovements= {
    render indicator.getSentStockMovements().toJson() as JSON
}


}