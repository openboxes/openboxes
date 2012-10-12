package org.pih.warehouse

import geb.spock.GebReportingSpec
import org.pih.warehouse.pages.ExpiringStockPage
import testutils.DbHelper
import testutils.PageNavigator

class InventorySpec extends GebReportingSpec {
    def "should show stock expiring within one week"() {
        def product_name = "TestProd" + UUID.randomUUID().toString()[0..5]
        given:
            DbHelper.CreateProductInInventory(product_name, 5000, new Date().plus(5))
            PageNavigator.UserLoginedAsManagerForBoston()
            to ExpiringStockPage
        when:
            at ExpiringStockPage
            threshhold.value("one week")
            filter.click()
        then:
            expiredStockList.contains(product_name)
    }
}
