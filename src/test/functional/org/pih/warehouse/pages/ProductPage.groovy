package org.pih.warehouse.pages

import geb.Page
import testutils.TestFixture


class ProductPage extends Page{
    static url = TestFixture.baseUrl + "/product/create"
    static at = { title == "Add new product"}
    static content ={
        productDescription { $("#tabs-details form input", name:"name") }
        productCategory { $("#tabs-details form select", name:"category.id") }
        unitOfMeasure {$("#tabs-details form input", name:"unitOfMeasure")}
        manufacturer {$("#tabs-details form input", name:"manufacturer")}
        manufacturerCode {$("#tabs-details form input", name:"manufacturerCode")}
        saveButton(to: InventoryPage) { $("button", type:"submit")}

    }
}