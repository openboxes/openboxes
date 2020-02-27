package org.pih.warehouse.apitablero


import grails.converters.JSON
import org.pih.warehouse.tableroapi.NumberDataService
import org.pih.warehouse.tableroapi.IndicatorDataService
import org.pih.warehouse.tablero.NumberData
import org.codehaus.groovy.grails.web.json.JSONObject

class ApitableroController {

def index= {
    
    render("You are in the api")
}

def getNumberData= {
    NumberDataService numberDataService = new NumberDataService();

    render  numberDataService.getListNumberData() as JSON;
}

def getExpirationSummary= {
    IndicatorDataService indicator = new IndicatorDataService();

    render indicator.getExpirationSummaryData().toJson() as JSON;
}


}