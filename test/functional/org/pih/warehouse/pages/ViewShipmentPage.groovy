package org.pih.warehouse.pages

import geb.Page


class ViewShipmentPage extends Page{
    static url = { settings.baseUrl + "/shipment/showDetails"}
    static at = { title == "View shipment"}
    static content ={

    }
}
