package org.pih.warehouse.importer.spec

import org.joda.time.LocalDate

import org.pih.warehouse.common.util.FileResourceUtil
import org.pih.warehouse.importer.InventoryExcelImporter
import org.pih.warehouse.importer.spec.base.ImporterSpec

class InventoryExcelImporterSpec extends ImporterSpec {

    void 'InventoryExcelImporter can import successfully'() {
        given:
        File file = FileResourceUtil.getFile("import/inventory.xls")

        when:
        InventoryExcelImporter importer = new InventoryExcelImporter(file.absolutePath)
        List<Map> actualData = importer.data
        List<Map> expectedData = [
                [productCode: "00001", product: "Advil 200mg",   lotNumber: "M18",     expirationDate: null,                        binLocation: "Salle 1", quantityOnHand: "252",  quantity: "2520",  comments: "Comment 1"],
                [productCode: "00001", product: "Advil 200mg",   lotNumber: "M21",     expirationDate: new LocalDate(2025, 2, 16),  binLocation: "Salle 1", quantityOnHand: "1654", quantity: "16540", comments: "Comment 2"],
                [productCode: "00001", product: "Advil 200mg",   lotNumber: "M20",     expirationDate: new LocalDate(2025, 2, 16),  binLocation: "Salle 1", quantityOnHand: "1736", quantity: "17360", comments: "Comment 3"],
                [productCode: "00002", product: "Tylenol 325mg", lotNumber: "CJ12002", expirationDate: new LocalDate(2025, 12, 14), binLocation: "Salle 1", quantityOnHand: "39",   quantity: "385",   comments: "Comment 4"],
                [productCode: "00002", product: "Tylenol 325mg", lotNumber: "CJ11026", expirationDate: new LocalDate(2025, 9, 14),  binLocation: "Salle 2", quantityOnHand: "30",   quantity: "300",   comments: "Comment 5"],
                [productCode: "00003", product: "Aspirin 20mg",  lotNumber: "DB11007", expirationDate: new LocalDate(2025, 7, 14),  binLocation: "Salle 2", quantityOnHand: "6",    quantity: "60",    comments: "Comment 6"],
                [productCode: "00003", product: "Aspirin 20mg",  lotNumber: "DB11012", expirationDate: new LocalDate(2025, 11, 14), binLocation: "Salle 2", quantityOnHand: "604",  quantity: "6042",  comments: null       ],
        ]

        then:
        assert actualData != null
        assert actualData.size() == 7
        for (i in 0 .. 6) {
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
