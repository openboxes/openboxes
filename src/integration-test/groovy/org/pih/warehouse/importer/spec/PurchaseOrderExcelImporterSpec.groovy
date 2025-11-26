package org.pih.warehouse.importer.spec

import org.pih.warehouse.common.util.FileResourceUtil
import org.pih.warehouse.importer.PurchaseOrderExcelImporter
import org.pih.warehouse.importer.spec.base.ImporterSpec

class PurchaseOrderExcelImporterSpec extends ImporterSpec {

    void 'PurchaseOrderExcelImporter can import successfully'() {
        given:
        File file = FileResourceUtil.getFile("import/purchaseOrders.xls")

        when:
        PurchaseOrderExcelImporter importer = new PurchaseOrderExcelImporter(file.absolutePath)
        List<Map> data = importer.data

        then:
        assert data != null
        // TODO: Do some asserts on the actual data
    }
}
