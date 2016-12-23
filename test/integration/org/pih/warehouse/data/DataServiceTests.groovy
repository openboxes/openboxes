/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.data

import grails.test.mixin.integration.Integration
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.junit.Ignore
import org.junit.Test
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.product.Product
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource

@Integration
class DataServiceTests {

    def dataService

    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }



    @Test
    void findOrCreateCategory() {
        def category = dataService.findOrCreateCategory("New Category")
        assert category != null
        assert "New Category" == category.name
    }

    @Test
    void test_findOrCreateProduct() {
        def product = dataService.findOrCreateProduct([productCode: "AB12", productName: "New product", category: "New category", manufacturer: "Mfg", manufacturerCode: "Mfgcode", vendor: "Vendor", vendorCode: "Vendor code", unitOfMeasure: "each"])
        assert product != null
        assert "AB12" == product.productCode
        assert "New product" == product.name
        assert "Mfg" == product.manufacturer
        assert "Mfgcode" == product.manufacturerCode
        assert "Vendor" == product.vendor
        assert "Vendor code" == product.vendorCode
        assert "each" == product.unitOfMeasure
    }

    @Test
    void test_indOrCreateUnitOfMeasure() {
        def unitOfMeasure = dataService.findOrCreateUnitOfMeasure("EA")
        assert unitOfMeasure != null
        assert "EA" == unitOfMeasure.code
    }

    @Test
    void test_findOrCreateProductPackage() {
        def product = dataService.findOrCreateProduct([productCode: "AB12", productName: "New product", category: "New category", manufacturer: "Mfg", manufacturerCode: "Mfgcode", vendor: "Vendor", vendorCode: "Vendor code", unitOfMeasure: "each"])
        def productPackage = dataService.findOrCreateProductPackage(product, "EA", 1, 1.50)
        assert productPackage != null
        assert "EA/1" == productPackage.name
        assert "EA/1" == productPackage.description
        assert 1.50 == productPackage.price
        assert "EA" == productPackage.uom.code
        assert "EA" == productPackage.uom.name
        assert "EA" == productPackage.uom.description
    }

    @Test
    void test_findOrCreateInventoryLevel() {
        def location = Location.findByName("Boston Headquarters");
        assert location != null
        if (!location.inventory) {
            location.inventory = new Inventory();
            location.save(flush: true, failOnError: true)
        }

        def product = dataService.findOrCreateProduct([productCode: "AB12", productName: "New product", category: "New category", manufacturer: "Mfg", manufacturerCode: "Mfgcode", vendor: "Vendor", vendorCode: "Vendor code", unitOfMeasure: "each"])
        def inventoryLevel = dataService.findOrCreateInventoryLevel(product, location.inventory, "AB-12-12", 0, 10, 100, true)

        assert inventoryLevel != null
        //assertEquals "AB-12-12", inventoryLevel.binLocation
        assert 0 == inventoryLevel.minQuantity
        assert 10 == inventoryLevel.reorderQuantity
        assert 100 == inventoryLevel.maxQuantity
        assert "Boston Headquarters" == inventoryLevel.inventory.warehouse.name
        assert "AB12" == inventoryLevel.product.productCode
        assert "New product" == inventoryLevel.product.name
        assert inventoryLevel.preferred
    }



    @Ignore
    void test_importInventoryLevels() {
        def startTime = System.currentTimeMillis()

        // Get the
        def location = Location.findByName("Boston Headquarters");
        assertNotNull location
        if (!location.inventory) {
            location.inventory = new Inventory();
            location.save(flush: true, failOnError: true)
        }

        Resource resource = new ClassPathResource("resources/inventoryLevelImportData-partial.xls")
        def file = resource.getFile()
        assert file.exists()

        def inventoryLevelList = dataService.importInventoryLevels(location, file.getAbsolutePath())
        assertNotNull inventoryLevelList

        def product = Product.findByProductCode("NM89")
        assertNotNull product
        assertEquals "X- Ray Digitizer", product.name
        assertEquals "Diagnostic imaging products", product.category.name
        assertEquals "HUM All Products", product.tagsToString()
        assertEquals "Vidair", product.manufacturer
        assertEquals "3D Systems Corporation", product.vendor
        assertEquals "each", product.unitOfMeasure

        product = Product.findByProductCode("PK77")
        assertNotNull product
        assertEquals "Burette Set (micro dropper), 150ml, w/ automatic shutoff", product.name
        assertEquals "Intravenous (IV) and arterial administration products", product.category.name
        assertTrue product.tagsToString().contains("HUM McKesson Consumables")
        assertTrue product.tagsToString().contains("HUM All Products")
        assertEquals "Braun", product.manufacturer
        assertEquals "375113", product.manufacturerCode
        assertEquals "AAA Wholesale", product.vendor
        assertEquals "B-375113-EA", product.vendorCode
        assertEquals "each", product.unitOfMeasure
        assertEquals "BP2-02", product.getInventoryLevel(location.id).binLocation

        product = Product.findByProductCode("NT75")
        assertNotNull product
        assertEquals "Applicator stick", product.name
        assertEquals "Laboratory supplies", product.category.name
        assertTrue product.tagsToString().contains("HUM All Products")
        assertTrue product.tagsToString().contains("HUM Fisher Lab Order")
        assertTrue product.tagsToString().contains("HUM Lab Reagents and Accessories")
        assertEquals "Puritan", product.manufacturer
        assertEquals "807", product.manufacturerCode
        assertEquals "Fisher Scientific", product.vendor
        assertEquals "22 029 491", product.vendorCode
        assertEquals "each", product.unitOfMeasure

        assertEquals 0.08591, product.pricePerUnit, 0.0001
        assertEquals "B2-01-C2", product.getInventoryLevel(location.id).binLocation
        assertEquals 5000, product.getInventoryLevel(location.id).minQuantity
        assertEquals 10000, product.getInventoryLevel(location.id).reorderQuantity
        assertEquals 20000, product.getInventoryLevel(location.id).maxQuantity
        assertTrue product.getInventoryLevel(location.id).preferred

        assertNotNull product.getProductPackage("CS")
        assertEquals 1000, product.getProductPackage("CS").quantity
        assertEquals 85.91, product.getProductPackage("CS").price, 0.0001


        // Testing the new preferred for reorder flag
        product = Product.findByProductCode("NT75")
        assertNotNull product
        assertEquals "Applicator stick", product.name
        assertTrue product.getInventoryLevel(location.id).preferred

        product = Product.findByProductCode("VV07")
        assertNotNull product
        assertEquals "Applicator, Cotton tipped, Nonsterile", product.name
        assertFalse product.getInventoryLevel(location.id).preferred

        product = Product.findByProductCode("SD08")
        assertNotNull product
        assertEquals "Bag, Biohazard, Autoclave, 14in x 19in", product.name
        assertFalse product.getInventoryLevel(location.id).preferred


        /*
        product = Product.findByProductCode("QM56")
        assertNotNull product
        assertEquals "Carvedilol, 12.5mg, tablet ", product.name
        assertEquals "Drugs and pharmaceutical products", product.category.name
        assertTrue product.tagsToString().contains("HUM Formulary")
        assertTrue product.tagsToString().contains("HUM All Products")
        assertEquals "Holden Medical Laboratories ", product.manufacturer
        assertEquals "36075", product.manufacturerCode
        assertEquals "each", product.unitOfMeasure
        assertEquals "B13-02-B2", product.getInventoryLevel(location.id).binLocation
        assertEquals 22000, product.getInventoryLevel(location.id).minQuantity
        assertEquals 44000, product.getInventoryLevel(location.id).reorderQuantity
        assertEquals 88000, product.getInventoryLevel(location.id).maxQuantity
        */

        //println inventoryLevelList
        println "products after: " + Product.list()?.size()
        println "Time to import ${inventoryLevelList.size()} items: " + (System.currentTimeMillis() - startTime) + " ms"
     }

    @Ignore
    void test_importProductGroups() {
        def startTime = System.currentTimeMillis()

        // Get the
        def location = Location.findByName("Boston Headquarters");
        assertNotNull location
        if (!location.inventory) {
            location.inventory = new Inventory();
            location.save(flush: true, failOnError: true)
        }

        Resource resource = new ClassPathResource("resources/inventoryLevelImportData-partial.xls")
        def file = resource.getFile()
        assert file.exists()

        def inventoryLevelList = dataService.importInventoryLevels(location, file.getAbsolutePath())
        assertNotNull inventoryLevelList

        def product = Product.findByProductCode("NM89")
        assertNotNull product
        assertEquals "X- Ray Digitizer", product.name
        assertEquals "Diagnostic imaging products", product.category.name
        assertEquals "HUM All Products", product.tagsToString()
        assertEquals "Vidair", product.manufacturer
        assertEquals "3D Systems Corporation", product.vendor
        assertEquals "each", product.unitOfMeasure

        product = Product.findByProductCode("PK77")
        assertNotNull product
        assertEquals "Burette Set (micro dropper), 150ml, w/ automatic shutoff", product.name
        assertEquals "Intravenous (IV) and arterial administration products", product.category.name
        assertTrue product.tagsToString().contains("HUM McKesson Consumables")
        assertTrue product.tagsToString().contains("HUM All Products")
        assertEquals "Braun", product.manufacturer
        assertEquals "375113", product.manufacturerCode
        assertEquals "AAA Wholesale", product.vendor
        assertEquals "B-375113-EA", product.vendorCode
        assertEquals "each", product.unitOfMeasure
        assertEquals "BP2-02", product.getInventoryLevel(location.id).binLocation

        product = Product.findByProductCode("NT75")
        assertNotNull product
        assertEquals "Applicator stick", product.name
        assertEquals "Laboratory supplies", product.category.name
        assertTrue product.tagsToString().contains("HUM All Products")
        assertTrue product.tagsToString().contains("HUM Fisher Lab Order")
        assertTrue product.tagsToString().contains("HUM Lab Reagents and Accessories")
        assertEquals "Puritan", product.manufacturer
        assertEquals "807", product.manufacturerCode
        assertEquals "Fisher Scientific", product.vendor
        assertEquals "22 029 491", product.vendorCode
        assertEquals "each", product.unitOfMeasure

        assertEquals 0.08591, product.pricePerUnit, 0.0001
        assertEquals "B2-01-C2", product.getInventoryLevel(location.id).binLocation
        assertEquals 5000, product.getInventoryLevel(location.id).minQuantity
        assertEquals 10000, product.getInventoryLevel(location.id).reorderQuantity
        assertEquals 20000, product.getInventoryLevel(location.id).maxQuantity

        def productPackage = product.getProductPackage("CS")
        assertNotNull productPackage
        assertEquals 1000, productPackage.quantity
        assertEquals 85.91, productPackage.price, 0.0001

        /*
        product = Product.findByProductCode("QM56")
        assertNotNull product
        assertEquals "Carvedilol, 12.5mg, tablet ", product.name
        assertEquals "Drugs and pharmaceutical products", product.category.name
        assertTrue product.tagsToString().contains("HUM Formulary")
        assertTrue product.tagsToString().contains("HUM All Products")
        assertEquals "Holden Medical Laboratories ", product.manufacturer
        assertEquals "36075", product.manufacturerCode
        assertEquals "each", product.unitOfMeasure
        assertEquals "B13-02-B2", product.getInventoryLevel(location.id).binLocation
        assertEquals 22000, product.getInventoryLevel(location.id).minQuantity
        assertEquals 44000, product.getInventoryLevel(location.id).reorderQuantity
        assertEquals 88000, product.getInventoryLevel(location.id).maxQuantity
        */

        //println inventoryLevelList
        println "products after: " + Product.list()?.size()
        println "Time to import ${inventoryLevelList.size()} items: " + (System.currentTimeMillis() - startTime) + " ms"
    }

}
