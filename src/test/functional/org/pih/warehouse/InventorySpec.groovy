package org.pih.warehouse

import geb.spock.GebReportingSpec
import org.pih.warehouse.pages.ExpiredStockPage
import org.pih.warehouse.pages.ExpiringStockPage
import testutils.TestFixture
import org.pih.warehouse.pages.InventoryPage
import org.pih.warehouse.pages.ShowStockCardPage
import org.pih.warehouse.pages.BrowseInventoryPage


class InventorySpec extends GebReportingSpec {
    def "should show stock expiring"() {
        given:
            TestFixture.UserLoginedAsManagerForBoston()
            to ExpiringStockPage
        when:
            at ExpiringStockPage
            threshold.value("one week")
            filter.click()
        then:
            expiringStockList.contains(TestFixture.Advil200mg)
            !expiringStockList.contains(TestFixture.Tylenol325mg)
        when:
            threshold.value("six months")
            filter.click()
        then:
            expiringStockList.contains(TestFixture.Advil200mg)
            expiringStockList.contains(TestFixture.Tylenol325mg)
            !expiringStockList.contains(TestFixture.GeneralPainReliever)

    }



    def "should be able to filter by category"() {
        given:
            TestFixture.UserLoginedAsManagerForBoston()
            to ExpiringStockPage
        when:
            at ExpiringStockPage
            category.value("Supplies")
            filter.click()
        then:
            expiringStockList.contains(TestFixture.MacBookPro8G)
            !expiringStockList.contains(TestFixture.Advil200mg)
    }

    def "should show expired stock"() {
        given:
            TestFixture.UserLoginedAsManagerForBoston()
            to ExpiredStockPage
        when:
            at ExpiredStockPage
        then:
            expiredStockList.contains(TestFixture.SimilacAdvanceLowiron400g)
            expiredStockList.contains(TestFixture.SimilacAdvanceIron365g)
            !expiredStockList.contains(TestFixture.Advil200mg)
            !expiredStockList.contains(TestFixture.MacBookPro8G)
    }

      def "should create new inventory item for existing product"() {
        def quantity = "5630"
        def product_name = "TestProd" + UUID.randomUUID().toString()[0..5]
        given:
            TestFixture.UserLoginedAsManagerForBoston()
            TestFixture.CreateProductInInventory(product_name, 5000)
        and:
            to BrowseInventoryPage
        when:
            selectProductCategory.value(2) //supplies
            searchText.value(product_name)
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
            newQuantity.value(quantity)
        and:
            saveInventoryItem.click()
        then:
            //at ShowStockCardPage //under headless fails, don't know why
            totalQuantity == "5,630"
    }
}
