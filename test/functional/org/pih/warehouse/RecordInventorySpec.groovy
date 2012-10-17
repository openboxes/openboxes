package org.pih.warehouse

import geb.spock.GebReportingSpec
import org.pih.warehouse.pages.BrowseInventoryPage
import org.pih.warehouse.pages.InventoryPage
import org.pih.warehouse.pages.ShowStockCardPage
import testutils.PageNavigator

class RecordInventorySpec extends GebReportingSpec {

    def "should create new inventory item for existing product"() {
        given:
            PageNavigator.UserLoginedAsManagerForBoston()
        and:
            to BrowseInventoryPage
        when:
            selectProductCategory.value(2) //supplies
        and:
            searchButton.click()
        and:
            at BrowseInventoryPage
            productItem.click()
        and:
            at ShowStockCardPage
            actionButton.click()
            recordInventoryButton.click()
        and:
            at InventoryPage
            def val = Math.floor(Math.random() * 10000 + 1)
            newQuantity.value(val)
        and:
            saveInventoryItem.click()
        then:
            at ShowStockCardPage
            totalQuantity == String.format("%,d", (int)val)
    }
}
