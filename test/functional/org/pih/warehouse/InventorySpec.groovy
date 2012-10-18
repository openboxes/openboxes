package org.pih.warehouse

import geb.spock.GebReportingSpec
import org.pih.warehouse.pages.BrowseInventoryPage
import org.pih.warehouse.pages.ExpiredStockPage
import org.pih.warehouse.pages.ExpiringStockPage
import org.pih.warehouse.pages.InventoryPage
import org.pih.warehouse.pages.ShowStockCardPage
import testutils.TestFixture


class InventorySpec extends GebReportingSpec {
    def "should show stock expiring within one week"() {
        def product_name = "TestProd" + UUID.randomUUID().toString()[0..5]
        given:
            TestFixture.UserLoginedAsManagerForBoston()
            TestFixture.CreateProductInInventory(product_name, 5000, new Date().plus(5))
            to ExpiringStockPage
        when:
            at ExpiringStockPage
            threshhold.value("one week")
            filter.click()
        then:
            expiringStockList.contains(product_name)
    }

    def "should show stock expiring within 6 months"() {
        def product_name = "TestProd" + UUID.randomUUID().toString()[0..5]
        given:
            TestFixture.UserLoginedAsManagerForBoston()
            TestFixture.CreateProductInInventory(product_name, 5000, new Date().plus(20))
            to ExpiringStockPage
        when:
            at ExpiringStockPage
            threshhold.value("six months")
            filter.click()
        then:
            expiringStockList.contains(product_name)
    }

    def "should be able to filter by category"() {
        def product_name = "TestProd" + UUID.randomUUID().toString()[0..5]
        given:
            TestFixture.UserLoginedAsManagerForBoston()
            TestFixture.CreateProductInInventory(product_name, 5000, new Date().plus(20))
            to ExpiringStockPage
        when:
            at ExpiringStockPage
            category.value("Supplies")
            filter.click()
        then:
            expiringStockList.contains(product_name)
    }

    def "should show expired stock"() {
        def product_name = "TestProd" + UUID.randomUUID().toString()[0..5]
        given:
            TestFixture.UserLoginedAsManagerForBoston()
            TestFixture.CreateProductInInventory(product_name, 5000, new Date().minus(1))
            to ExpiredStockPage
        when:
            at ExpiredStockPage
        then:
            expiredStockList.contains(product_name)
    }

    def "should browse existing inventory"() {
        def product_name = "TestProd" + UUID.randomUUID().toString()[0..5]
        given:
            TestFixture.UserLoginedAsManagerForBoston()
            TestFixture.CreateProductInInventory(product_name, 5000, new Date().minus(1))
            to BrowseInventoryPage
        when:
            selectProductCategory.value(2) //supplies
            searchProductName.value(product_name)
            searchButton.click()
        and:
            at BrowseInventoryPage
            productItem.click()
        then:
            at ShowStockCardPage
            stockProductName == product_name
    }

    def "should adjust inventory for existing product"() {
        given:
            TestFixture.UserLoginedAsManagerForBoston()
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
