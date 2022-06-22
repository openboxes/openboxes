package org.pih.warehouse.importer

import org.junit.Test
import org.springframework.core.io.ClassPathResource

class InventoryExcelImporterTests extends GroovyTestCase {

    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    @Test
    void testSomething() {
        def resource = new ClassPathResource('resources/inventory3.xls')
        def file = resource.getFile()
        assert file.exists()

        def importer = new InventoryExcelImporter(file.absolutePath)
        def actualData = importer.data

        println "Data: ${actualData}"
        assert actualData != null

        def expectedData = [
                [productCode:"00001", product:"Advil 200mg", lotNumber:"M18", expirationDate:"15-11", binLocation:"Salle 1", quantityOnHand:252.0, quantity:2520.0, comments:"Comment 1"],
                [productCode:"00001", product:"Advil 200mg", lotNumber:"M21", expirationDate:"16-02", binLocation:"Salle 1", quantityOnHand:1654.0, quantity:16540.0, comments:"Comment 2"],
                [productCode:"00001", product:"Advil 200mg", lotNumber:"M20", expirationDate:"16-02", binLocation:"Salle 1", quantityOnHand:1736.0, quantity:17360.0, comments:"Comment 3"],
                [productCode:"00001", product:"Advil 200mg", lotNumber:"M19", expirationDate:"15-11", binLocation:"Salle 1", quantityOnHand:1232.0, quantity:12321.0, comments:"Comment 4"],
                [productCode:"00001", product:"Advil 200mg", lotNumber:"M22", expirationDate:"16-02", binLocation:"Salle 1", quantityOnHand:322.0, quantity:3220.0, comments:"Comment 5"],
                [productCode:"00001", product:"Advil 200mg", lotNumber:"M17", expirationDate:"15-06", binLocation:"Salle 1", quantityOnHand:15.0, quantity:148.0, comments:"Comment 6"],
                [productCode:"00002", product:"Tylenol 325mg", lotNumber:"CJ12002", expirationDate:"14-12", binLocation:"Salle 1", quantityOnHand:39.0, quantity:385.0, comments:"Comment 7"],
                [productCode:"00002", product:"Tylenol 325mg", lotNumber:"CJ11026", expirationDate:"14-09", binLocation:"Salle 2", quantityOnHand:30.0, quantity:300.0, comments:"Comment 8"],
                [productCode:"00003", product:"Aspirin 20mg", lotNumber:"DB11007", expirationDate:"14-07", binLocation:"Salle 2", quantityOnHand:6.0, quantity:60.0, comments:"Comment 9"],
                [productCode:"00003", product:"Aspirin 20mg", lotNumber:"DB11012", expirationDate:"14-11", binLocation:"Salle 2", quantityOnHand:604.0, quantity:6042.0, comments:"Comment 10"],
                [productCode:"00004", product:"General Pain Reliever", lotNumber:"H13", expirationDate:"16-02", binLocation:"Salle 2", quantityOnHand:2.0, quantity:18.0, comments:"Comment 11"],
                [productCode:"00004", product:"General Pain Reliever", lotNumber:"H11", expirationDate:"15-04", binLocation:"Salle 3", quantityOnHand:177.0, quantity:1766.0, comments:"Comment 12"],
                [productCode:"00004", product:"General Pain Reliever", lotNumber:"H12", expirationDate:"15-09", binLocation:"Salle 3", quantityOnHand:2028.0, quantity:20282.0, comments:"Comment 13"],
                [productCode:"00005", product:"Similac Advance low iron 400g", lotNumber:"HI12034", expirationDate:"15-09", binLocation:"Salle 3", quantityOnHand:1153.0, quantity:11525.0, comments:"Comment 14"],
                [productCode:"00006", product:"Similac Advance + iron 365g", lotNumber:"GA12083", expirationDate:"15-07", binLocation:"Salle 3", quantityOnHand:9.0, quantity:90.0, comments:"Comment 15"],
                [productCode:"00007", product:"MacBook Pro 8G", lotNumber:"CK12026", expirationDate:"15-10", binLocation:"Salle 3", quantityOnHand:760.0, quantity:7600.0, comments:"Comment 16"],
                [productCode:"00008", product:"Print Paper A4", lotNumber:"7516", expirationDate:"15-11", binLocation:"Salle 3", quantityOnHand:70.0, quantity:700.0, comments:"Comment 17"],
                [productCode:"00008", product:"Print Paper A4", lotNumber:"7518", expirationDate:"16-04", binLocation:"Salle 3", quantityOnHand:680.0, quantity:6800.0, comments:"Comment 18"]
        ]

        assert actualData.size() == 18
        for (i in 0 .. 17) {
            assert actualData[i].productCode == expectedData[i].productCode
            assert actualData[i].product == expectedData[i].product
            assert actualData[i].lotNumber == expectedData[i].lotNumber
            assert actualData[i].expirationDate == expectedData[i].expirationDate
            assert actualData[i].binLocation == expectedData[i].binLocation
            assert actualData[i].quantityOnHand == expectedData[i].quantityOnHand
            assert actualData[i].quantity == expectedData[i].quantity
            assert actualData[i].comments == expectedData[i].comments

        }
    }





}
