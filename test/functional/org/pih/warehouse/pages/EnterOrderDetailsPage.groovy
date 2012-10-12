package org.pih.warehouse.pages

import geb.Page
import testutils.Settings

class EnterOrderDetailsPage extends Page {
    static url = Settings.baseUrl + "/purchaseOrderWorkflow/purchaseOrder"
    static at = { title == "Enter order details"}
    static content ={
        purchaseOrderDescription { $("input[name='description']") }
        purchaseOrderOrigin { $("select[name='origin.id']") }
        addItemsButton(to: AddOrderItemsPage) { $("input", type:"submit")}
    }
}
