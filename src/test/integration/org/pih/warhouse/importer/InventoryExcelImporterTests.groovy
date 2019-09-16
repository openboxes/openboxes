package org.pih.warehouse.importer

import org.apache.commons.lang.StringUtils
import org.junit.Test
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.product.Product
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource

import java.text.SimpleDateFormat

class InventoryExcelImporterTests extends GroovyTestCase {

    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    @Test
    void testSomething() {
        // FIXME Used just for testing
        def location = Location.list()[0]
        def resource = new ClassPathResource("resources/inventory2.xls")
        def file = resource.getFile()
        assert file.exists()

        def importer = new InventoryExcelImporter(file.absolutePath)
        def actualData = importer.data

        println "Data: ${actualData}"
        assert actualData != null

        def expectedData = [
                [productCode:"00001", product:"Advil 200mg", lotNumber:"M18", expirationDate:"15-11", manufacturer:"Mepro Pharmaceuticals", manufacturerCode:"G/1663", quantity:2520.0, binLocation:"Salle 1", comments:"Comment 1"],
                [productCode:"00001", product:"Advil 200mg", lotNumber:"M21", expirationDate:"16-02", manufacturer:"Mepro Pharmaceuticals", manufacturerCode:"G/1663", quantity:16540.0, binLocation:"Salle 1", comments:"Comment 2"],
                [productCode:"00001", product:"Advil 200mg", lotNumber:"M20", expirationDate:"16-02", manufacturer:"Mepro Pharmaceuticals", manufacturerCode:"G/1663", quantity:17360.0, binLocation:"Salle 1", comments:"Comment 3"],
                [productCode:"00001", product:"Advil 200mg", lotNumber:"M19", expirationDate:"15-11", manufacturer:"Mepro Pharmaceuticals", manufacturerCode:"G/1663", quantity:12321.0, binLocation:"Salle 1", comments:"Comment 4"],
                [productCode:"00001", product:"Advil 200mg", lotNumber:"M22", expirationDate:"16-02", manufacturer:"Mepro Pharmaceuticals", manufacturerCode:"G/1663", quantity:3220.0, binLocation:"Salle 1", comments:"Comment 5"],
                [productCode:"00001", product:"Advil 200mg", lotNumber:"M17", expirationDate:"15-06", manufacturer:"Mepro Pharmaceuticals", manufacturerCode:"G/1663", quantity:148.0, binLocation:"Salle 1", comments:"Comment 6"],
                [productCode:"00002", product:"Tylenol 325mg", lotNumber:"CJ12002", expirationDate:"14-12", manufacturer:"Galentic Pharma Pvt, Ltd", manufacturerCode:"KD-214", quantity:385.0, binLocation:"Salle 1", comments:"Comment 7"],
                [productCode:"00002", product:"Tylenol 325mg", lotNumber:"CJ11026", expirationDate:"14-09", manufacturer:"Galentic Pharma Pvt, Ltd", manufacturerCode:"KD-214", quantity:300.0, binLocation:"Salle 2", comments:"Comment 8"],
                [productCode:"00003", product:"Aspirin 20mg", lotNumber:"DB11007", expirationDate:"14-07", manufacturer:"Galentic Pharma Pvt, Ltd", manufacturerCode:"KD-180", quantity:60.0, binLocation:"Salle 2", comments:"Comment 9"],
                [productCode:"00003", product:"Aspirin 20mg", lotNumber:"DB11012", expirationDate:"14-11", manufacturer:"Galentic Pharma Pvt, Ltd", manufacturerCode:"KD-180", quantity:6042.0, binLocation:"Salle 2", comments:"Comment 10"],
                [productCode:"00004", product:"General Pain Reliever", lotNumber:"H13", expirationDate:"16-02", manufacturer:"Mepro Pharmaceuticals", manufacturerCode:"G/1663", quantity:18.0, binLocation:"Salle 2", comments:"Comment 11"],
                [productCode:"00004", product:"General Pain Reliever", lotNumber:"H11", expirationDate:"15-04", manufacturer:"Mepro Pharmaceuticals", manufacturerCode:"G/1663", quantity:1766.0, binLocation:"Salle 3", comments:"Comment 12"],
                [productCode:"00004", product:"General Pain Reliever", lotNumber:"H12", expirationDate:"15-09", manufacturer:"Mepro Pharmaceuticals", manufacturerCode:"G/1663", quantity:20282.0, binLocation:"Salle 3", comments:"Comment 13"],
                [productCode:"00005", product:"Similac Advance low iron 400g", lotNumber:"HI12034", expirationDate:"15-09", manufacturer:"Galentic Pharma Pvt,Ltd", manufacturerCode:"KD-180", quantity:11525.0, binLocation:"Salle 3", comments:"Comment 14"],
                [productCode:"00006", product:"Similac Advance + iron 365g", lotNumber:"GA12083", expirationDate:"15-07", manufacturer:"Galentic Pharma Pvt, Ltd", manufacturerCode:"KD-214", quantity:90.0, binLocation:"Salle 3", comments:"Comment 15"],
                [productCode:"00007", product:"MacBook Pro 8G", lotNumber:"CK12026", expirationDate:"15-10", manufacturer:"Galentic Pharma Pvt, Ltd", manufacturerCode:"KD-214", quantity:7600.0, binLocation:"Salle 3", comments:"Comment 16"],
                [productCode:"00008", product:"Print Paper A4", lotNumber:"7516", expirationDate:"15-11", manufacturer:"Agio Pharmaceuticals", manufacturerCode:"PD-163", quantity:700.0, binLocation:"Salle 3", comments:"Comment 17"],
                [productCode:"00008", product:"Print Paper A4", lotNumber:"7518", expirationDate:"16-04", manufacturer:"Agio Pharmaceuticals", manufacturerCode:"PD-163", quantity:6800.0, binLocation:"Salle 3", comments:"Comment 18"]
        ]

        assert actualData.size() == 18
        for (i in 0 .. 17) {
            assert actualData[i].productCode == expectedData[i].productCode
            assert actualData[i].product == expectedData[i].product
            assert actualData[i].lotNumber == expectedData[i].lotNumber
            assert actualData[i].expirationDate == expectedData[i].expirationDate
            assert actualData[i].manufacturer == expectedData[i].manufacturer
            assert actualData[i].manufacturerCode == expectedData[i].manufacturerCode
            assert actualData[i].quantity == expectedData[i].quantity
            assert actualData[i].binLocation == expectedData[i].binLocation
            assert actualData[i].comments == expectedData[i].comments

        }
    }





}
