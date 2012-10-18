package testutils

import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.Location
import org.pih.warehouse.pages.ProductPage
import org.pih.warehouse.pages.InventoryPage
import geb.Browser
import org.pih.warehouse.pages.LocationPage
import org.pih.warehouse.pages.LoginPage
import org.pih.warehouse.pages.CreateEnterShipmentDetailsPage
import org.pih.warehouse.pages.EnterTrackingDetailsPage
import org.pih.warehouse.pages.EditPackingListPage
import org.pih.warehouse.pages.ViewShipmentPage

class TestFixture{

    static  baseUrl = "/openboxes"

      static def UserLoginedAsManagerForBoston(){
          Browser.drive {
            to LoginPage
            loginForm.with{
                username = "manager"
                password = "password"
            }
            submitButton.click()

            at LocationPage

            boston.click()
          }
    }

    static Location CreateSupplierIfRequired() {
        def loc = Location.findByName("Test Supplier")
        if(!loc)
            loc = new Location()
        loc.version = 1
        loc.dateCreated = new Date()
        loc.lastUpdated = new Date()
        loc.name = "Test Supplier"
        loc.locationType = LocationType.findByDescription("Supplier") // Supplier
        loc.save(flush: true)
        loc
    }

    static void CreateProductInInventory(productName, quantity, expirationDate = new Date().plus(30)) {
        Browser.drive {
            to ProductPage

            productDescription.value(productName)
            productCategory.value("2") //supplies
            unitOfMeasure.value("pill")
            manufacturer.value("Xemon")
            manufacturerCode.value("ABC")

            saveButton.click()

            at InventoryPage
            assert productName == productName

            lotNumber.value("47")
            expires.click()
            datePicker.pickDate(expirationDate)
            newQuantity.click()
            newQuantity.value(7963)

            saveInventoryItem.click()

        }
    }

      static void CreatePendingShipment(product_name, shipment_name, quantity) {
        Browser.drive {
            to CreateEnterShipmentDetailsPage
            shipmentType.value("Sea")
            shipmentName.value(shipment_name)
            origin.value("Boston Headquarters [Depot]")
            destination.value("Miami Warehouse [Depot]")
            expectedShippingDate.click()
            datePicker.today.click()
            expectedArrivalDate.click()
            datePicker.tomorrow.click()
            nextButton.click()

            at EnterTrackingDetailsPage
            nextButton.click()
        and:
            at EditPackingListPage
            addItemToUnpackedItems()
            addItemToShipment.searchInventoryItem.searchCriteral.value(product_name)
            addItemToShipment.searchInventoryItem.firstSuggestion.click()
            addItemToShipment.quantity.value(quantity)
            addItemToShipment.saveButton.click()
            saveAndExitButton.click()
            at ViewShipmentPage

        }
    }

}