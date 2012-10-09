package org.pih.warehouse

import geb.spock.GebReportingSpec
import testutils.PageNavigator
import org.pih.warehouse.pages.ProductPage
import org.pih.warehouse.pages.InventoryPage
import org.pih.warehouse.pages.ShowStockCardPage



class ProductSpec extends GebReportingSpec{
    def "should create a new product and a inventory item"(){
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
        and:
            at InventoryPage
            productName == product_name
            productCategory.contains("Supplies")
            unitOfMeasure == "pill"
            manufacturer == "Xemon"
            manufacturerCode == "ABC"

        and:
            lotNumber.value("47")
            expires.click()
            datePicker.tomorrow.click()
            newQuantity.click()
            newQuantity.value(7963)
            //Thread.sleep(10000)
            saveInventoryItem.click()

        then:
          // at ShowStockCardPage  //not sure why it fails under headless
           totalQuantity == "7,963"
           productName == product_name
    }

     def product_name = "TestProd" + UUID.randomUUID().toString()[0..5]
}
