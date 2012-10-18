package org.pih.warehouse

import geb.spock.GebReportingSpec
import testutils.TestFixture
import testutils.PageNavigator
import org.pih.warehouse.pages.EnterShipmentDetailsPage
import org.pih.warehouse.pages.EnterTrackingDetailsPage
import org.pih.warehouse.pages.EditPackingListPage
import org.pih.warehouse.pages.SendShipmentPage
import org.pih.warehouse.pages.ViewShipmentPage


class ShipmentSpec extends GebReportingSpec{

    def "should send a sea shipment from Boston to Miami"(){
        def product_name = "TestProd" + UUID.randomUUID().toString()[0..5]
        def shipment_name = product_name + "_shipment"

        given:
            PageNavigator.UserLoginedAsManagerForBoston()
            TestFixture.CreateProductInInventory(product_name, 5000)
            to EnterShipmentDetailsPage
        when:
            at EnterShipmentDetailsPage
            shipmentType.value("Sea")
            name.value(shipment_name)
            origin.value("Boston Headquarters [Depot]")
            destination.value("Miami Warehouse [Depot]")
            expectedShippingDate.click()
            datePicker.today.click()
            expectedArrivalDate.click()
            datePicker.tomorrow.click()
            nextButton.click()
        and:
            at EnterTrackingDetailsPage
            nextButton.click()
        and:
            at EditPackingListPage
            addItemToUnpackedItems()
            addItemToShipment.searchInventoryItem.searchCriteral.value(product_name)
            addItemToShipment.searchInventoryItem.firstSuggestion.click()
            addItemToShipment.quantity.value(200)
            addItemToShipment.saveButton.click()
            nextButton.click()
        and:
            at SendShipmentPage
            actualShippingDate.click()
            datePicker.today.click()
            nextButton.click()
        then:
            at ViewShipmentPage
            shipmentName ==  shipment_name
            status == "Shipped"
            type == "Sea"
            shipmentOrigin == "Boston Headquarters"
            shipmentDestination == "Miami Warehouse"
            product == product_name

            quantity == "200"
    }


}
