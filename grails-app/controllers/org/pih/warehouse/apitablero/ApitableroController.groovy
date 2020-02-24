package org.pih.warehouse.apitablero


import grails.converters.JSON
import org.pih.warehouse.tableroapi.NumberDataService
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


}