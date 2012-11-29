package org.pih.warehouse.pages

import geb.Page
import testutils.TestFixture

class ShowRequisitionPage extends Page {
    static url = TestFixture.baseUrl + "/requisition/show"
    static at = { title == "View requisition"}
    static content = {
        details(wait:true) { $("#tabs-details table").last() }
        detailsBody(wait:true) { $(details.find("tbody")) }
        requisitionActionButton(wait:true) { $("button", class:"action-btn") }
        processRequisitionActionButton(wait:true) { $("[name='processRequisition']")}
        firstProduct(wait:true) { $(detailsBody.find("tr").first()) }
        firstProductName(wait:true) { $(firstProduct.find("td.product")).text().trim() }
        firstProductQuantity(wait:true) { $(firstProduct.find("td.quantity")).text().trim() }
        firstProductQuantityPicked(wait:true) { $(firstProduct.find("td.quantityPicked")).text().trim() }
        secondProduct(wait:true) { $(firstProduct.next()) }
        secondProductName(wait:true) { $(secondProduct.find("td.product")).text().trim() }
        secondProductQuantity(wait:true) { $(secondProduct.find("td.quantity")).text().trim() }
        secondProductQuantityPicked(wait:true) { $(secondProduct.find("td.quantityPicked")).text().trim() }
    }
}
