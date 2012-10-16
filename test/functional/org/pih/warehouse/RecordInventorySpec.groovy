package org.pih.warehouse

import geb.spock.GebReportingSpec
import org.pih.warehouse.pages.BrowseInventoryPage
import testutils.PageNavigator

class RecordInventorySpec extends GebReportingSpec {

    def "should create new inventory item for existing product"() {
        given:
            PageNavigator.UserLoginedAsManagerForBoston()
        and:
            to BrowseInventoryPage
        and:
            selectProductCategory.value(2) //supplies
        and:
            searchButton.click()
        and:
            at BrowseInventoryPage

    }
}
