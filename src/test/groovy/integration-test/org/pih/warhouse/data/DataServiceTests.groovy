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

import grails.testing.services.ServiceUnitTest
import org.junit.Ignore
import org.junit.Test
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.product.Product
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import spock.lang.Specification
import testutils.DbHelper
import static org.junit.Assert.*;

//@Ignore
class DataServiceTests extends Specification implements ServiceUnitTest<DataService> {


    @Test
    void findOrCreateCategory() {
        when:
        def category = service.findOrCreateCategory("New Category")
        then:
        assertNotNull category
        assertEquals "New Category", category.name
    }

    @Test
    void findOrCreateProduct() {
        when:
        def product = service.findOrCreateProduct([productCode: "AB12", productName: "New product", category: "New category", productTypeName: "Default", manufacturer: "Mfg", manufacturerCode: "Mfgcode", vendor: "Vendor", vendorCode: "Vendor code", unitOfMeasure: "each"])
        then:
        assertNotNull product
        assertEquals "AB12", product.productCode
        assertEquals "New product", product.name
        assertEquals "Mfg", product.manufacturer
        assertEquals "Mfgcode", product.manufacturerCode
        assertEquals "Vendor", product.vendor
        assertEquals "Vendor code", product.vendorCode
        assertEquals "each", product.unitOfMeasure
    }

    @Test
    void findOrCreateUnitOfMeasure() {
        when:
        def unitOfMeasure = service.findOrCreateUnitOfMeasure("EA")
        then:
        assertNotNull unitOfMeasure
        assertEquals "EA", unitOfMeasure.code
    }

    @Test
    void findOrCreateProductPackage() {
        when:
        def product = service.findOrCreateProduct([productCode: "AB12", productName: "New product", category: "New category", productTypeName: "Default", manufacturer: "Mfg", manufacturerCode: "Mfgcode", vendor: "Vendor", vendorCode: "Vendor code", unitOfMeasure: "each"])
        def productPackage = service.findOrCreateProductPackage(product, "EA", 1, 1.50)
        then:
        assertNotNull productPackage
        assertEquals "EA/1", productPackage.name
        assertEquals "EA/1", productPackage.description
        assertEquals 1.50, productPackage.productPrice.price
        assertEquals "EA", productPackage.uom.code
        assertEquals "Each", productPackage.uom.name
    }

    @Test
    void findOrCreateInventoryLevel() {
        when:
        def location = DbHelper.findOrCreateLocationWithInventory("Boston Headquarters");
        def product = service.findOrCreateProduct([productCode: "AB12", productName: "New product", category: "New category", productTypeName: "Default", manufacturer: "Mfg", manufacturerCode: "Mfgcode", vendor: "Vendor", vendorCode: "Vendor code", unitOfMeasure: "each"])
        def row = [minQuantity: 0, reorderQuantity: 10, maxQuantity: 100, expectedLeadTimeDays: 120, replenishmentPeriodDays: 7, preferredForReorder: true]
        def inventoryLevel = service.findOrCreateInventoryLevel(product, location.inventory, null, row)

        then:
        assertNotNull inventoryLevel
        //assertEquals "AB-12-12", inventoryLevel.binLocation
        assertEquals 0, inventoryLevel.minQuantity
        assertEquals 10, inventoryLevel.reorderQuantity
        assertEquals 100, inventoryLevel.maxQuantity
        assertEquals "Boston Headquarters", inventoryLevel.inventory.warehouse.name
        assertEquals "AB12", inventoryLevel.product.productCode
        assertEquals "New product", inventoryLevel.product.name
        assertTrue inventoryLevel.preferred
    }



//    @Ignore
    @Test
    void importInventoryLevels() {
        when:
        def startTime = System.currentTimeMillis()

        // Get the
        def location = Location.findByName("Boston Headquarters");
        then:
        assertNotNull location
        if (!location.inventory) {
            location.inventory = new Inventory();
            location.save(flush: true, failOnError: true)
        }

        when:
        Resource resource = new ClassPathResource("resources/inventoryLevelImportData-partial.xls")
        def file = resource.getFile()
        then:
        assert file.exists()

        when:
        def inventoryLevelList = service.importInventoryLevels(location, file.getAbsolutePath())
        then:
        assertNotNull inventoryLevelList

        when:
        def product = Product.findByProductCode("NM89")
        then:
        assertNotNull product
        assertEquals "X- Ray Digitizer", product.name
        assertEquals "Diagnostic imaging products", product.category.name
        assertEquals "HUM All Products", product.tagsToString()
        assertEquals "Vidair", product.manufacturer
        assertEquals "3D Systems Corporation", product.vendor
        assertEquals "each", product.unitOfMeasure

        when:
        product = Product.findByProductCode("PK77")
        then:
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

        when:
        product = Product.findByProductCode("NT75")
        then:
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
        assertEquals 85.91, product.getProductPackage("CS").productPrice.price, 0.0001


        // Testing the new preferred for reorder flag
        when:
        product = Product.findByProductCode("NT75")
        then:
        assertNotNull product
        assertEquals "Applicator stick", product.name
        assertTrue product.getInventoryLevel(location.id).preferred

        when:
        product = Product.findByProductCode("VV07")
        then:
        assertNotNull product
        assertEquals "Applicator, Cotton tipped, Nonsterile", product.name
        assertFalse product.getInventoryLevel(location.id).preferred

        when:
        product = Product.findByProductCode("SD08")
        then:
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

//    @Ignore
    @Test
    void importProductGroups() {
        when:
        def startTime = System.currentTimeMillis()

        // Get the
        def location = Location.findByName("Boston Headquarters");
        then:
        assertNotNull location
        if (!location.inventory) {
            location.inventory = new Inventory();
            location.save(flush: true, failOnError: true)
        }

        when:
        Resource resource = new ClassPathResource("resources/inventoryLevelImportData-partial.xls")
        def file = resource.getFile()
        then:
        assert file.exists()

        when:
        def inventoryLevelList = service.importInventoryLevels(location, file.getAbsolutePath())
        then:
        assertNotNull inventoryLevelList

        when:
        def product = Product.findByProductCode("NM89")
        then:
        assertNotNull product
        assertEquals "X- Ray Digitizer", product.name
        assertEquals "Diagnostic imaging products", product.category.name
        assertEquals "HUM All Products", product.tagsToString()
        assertEquals "Vidair", product.manufacturer
        assertEquals "3D Systems Corporation", product.vendor
        assertEquals "each", product.unitOfMeasure

        when:
        product = Product.findByProductCode("PK77")
        then:
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

        when:
        product = Product.findByProductCode("NT75")
        then:
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

        when:
        def productPackage = product.getProductPackage("CS")
        then:
        assertNotNull productPackage
        assertEquals 1000, productPackage.quantity
        assertEquals 85.91, productPackage.productPrice.price, 0.0001

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
