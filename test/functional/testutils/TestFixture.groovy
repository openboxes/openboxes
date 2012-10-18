package testutils

import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.Location
import org.pih.warehouse.pages.ProductPage
import org.pih.warehouse.pages.InventoryPage
import geb.Browser

class TestFixture{

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

}