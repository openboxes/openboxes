package org.pih.warehouse.pages

import geb.Page
import testutils.TestFixture

class ShowRequisitionPage extends Page {
    static url = TestFixture.baseUrl + "/requisition/show"
    static at = { title == "View requisition"}
    static content = {
        details { $("#tabs-details table").last() }
        detailsBody { $(details.find("tbody")) }
        firstProduct { $(detailsBody.find("tr").first()) }
        firstProductName { $(firstProduct.find("td.product")).text().trim() }
        firstProductQuantity { $(firstProduct.find("td.quantity")).text().trim() }
        firstProductQuantityPicked { $(firstProduct.find("td.quantityPicked")).text().trim() }
        secondProduct { $(firstProduct.next()) }
        secondProductName { $(secondProduct.find("td.product")).text().trim() }
        secondProductQuantity { $(secondProduct.find("td.quantity")).text().trim() }
        secondProductQuantityPicked { $(secondProduct.find("td.quantityPicked")).text().trim() }
    }
}
