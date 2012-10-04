package org.pih.warehouse.pages

import geb.Page
import testutils.Settings


class ViewShipmentPage extends Page{
    static url = Settings.baseUrl + "/shipment/showDetails"
    static at = { title == "View shipment"}
    static content ={
       shipmentName{$("#shipmentSummary span.title").text().trim()}
       status{$("#shipmentSummary span.status label").text().trim()}
       type{$("#shipmentSummary span.shipmentType label").text().trim()}
       shipmentOrigin{$("#shipmentOrigin").text().trim()}
       shipmentDestination{$("#shipmentDestination").text().trim()}
       firstShipmentItem{$(".shipmentItem").first()}
       product{firstShipmentItem.find(".product").text().trim()}
       lotNumber{firstShipmentItem.find(".lotNumber").text().trim()}
       quantity{firstShipmentItem.find(".quantity").text().trim()}
    }
}
