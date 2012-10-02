package org.pih.warehouse

import geb.spock.GebReportingSpec
import testutils.PageNavigator
import org.pih.warehouse.pages.ProductPage
import org.pih.warehouse.pages.InventoryPage



class ProductSpec extends GebReportingSpec{
    def "should create a new product"(){
        given:
            PageNavigator.UserLoginedAsManagerForBoston()
        and:
            to ProductPage
        when:
            productDescription.value(product_name)
            productCategory.value("2") //supplies
            unitOfMeasure.value("pill")
            manufacturer.value("Xemon")
            manufacturerCode.value("ABC")

        and:
            saveButton.click()
        then:
            at InventoryPage
            productName == product_name
            productCategory.contains("Supplies")
            unitOfMeasure == "pill"
            manufacturer == "Xemon"
            manufacturerCode == "ABC"

    }

     def product_name = "TestProd" + UUID.randomUUID().toString()[0..5]
}
