package org.pih.warehouse

import geb.spock.GebReportingSpec
import testutils.TestFixture

import org.pih.warehouse.pages.CreateEnterShipmentDetailsPage
import org.pih.warehouse.pages.EnterTrackingDetailsPage
import org.pih.warehouse.pages.EditPackingListPage
import org.pih.warehouse.pages.SendShipmentPage
import org.pih.warehouse.pages.ViewShipmentPage


class ShipmentSpec extends GebReportingSpec{

    def "should send a sea shipment from Boston to Miami"(){
        def product_name = "TestProd" + UUID.randomUUID().toString()[0..5]
        def shipment_name = product_name + "_shipment"

        given:
            TestFixture.UserLoginedAsManagerForBoston()
            TestFixture.CreateProductInInventory(product_name, 5000)
            to CreateEnterShipmentDetailsPage
        when:
            at CreateEnterShipmentDetailsPage
            shipmentType.value("Sea")
            shipmentName.value(shipment_name)
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


    def "should be able to send a suitcase from Boston to Miami"() {
        def product_name = "TestProd" + UUID.randomUUID().toString()[0..5]
        def shipment_name = product_name + "_shipment"

        given:
            TestFixture.UserLoginedAsManagerForBoston()
            TestFixture.CreateProductInInventory(product_name, 5000)
            to CreateEnterShipmentDetailsPage
        when:
            at CreateEnterShipmentDetailsPage
            shipmentType.value("Suitcase")
            shipmentName.value(shipment_name)
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
            addSuitcaseToShipment()
            addSuitcaseToShipment.packingUnit.value("suitcase")
            addSuitcaseToShipment.weight.value(30)
            addSuitcaseToShipment.caseHeight.value(1)
            addSuitcaseToShipment.caseWidth.value(1.5)
            addSuitcaseToShipment.caseLength.value(2)
            addSuitcaseToShipment.addItemButton.click()


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
            shipmentName == shipment_name
            status == "Shipped"
            type == "Suitcase"
            shipmentOrigin == "Boston Headquarters"
            shipmentDestination == "Miami Warehouse"
            product == product_name
            quantity == "200"
    }

    def "view pending shipments"(){
        def product_name = "TestProd" + UUID.randomUUID().toString()[0..5]
        def shipment_name = product_name + "_shipment"
        given:
            TestFixture.UserLoginedAsManagerForBoston()
            TestFixture.CreateProductInInventory(product_name, 5000)
            TestFixture.CreatePendingShipment(product_name, shipment_name, 20)
        when:
            at ViewShipmentPage
        then:
            shipmentName == shipment_name
            status == "Pending"
            product == product_name
            quantity == "20"
    }
}
