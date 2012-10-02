package org.pih.warehouse.pages

import geb.Page
import testutils.Settings

class ProductPage extends Page{
    static url = Settings.baseUrl + "/product/create"
    static at = { title == "Add new product"}
    static content ={
        productDescription { $("#tabs-details form input", name:"name") }
        productCategory { $("#tabs-details form select", name:"category.id") }
        saveButton(to: InventoryPage) { $("button", type:"submit")}
    }
}