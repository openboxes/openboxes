package org.pih.warehouse.importer

import org.apache.commons.lang.StringUtils
import org.junit.Ignore
import org.junit.Test
import org.pih.warehouse.product.Product
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import spock.lang.Specification
import static org.junit.Assert.*;

//@Ignore
class PurchaseOrderExcelImporterTests extends Specification {
    protected void setup() {
//        super.setUp()
    }

    protected void tearDown() {
//        super.tearDown()
    }

    @Test
    void testSomething() {
        when:
        Resource resource = new ClassPathResource("resources/purchaseOrders.xls")
        def file = resource.getFile()
        then:
        assert file.exists()

        when:
        def importer = new PurchaseOrderExcelImporter(file.absolutePath)
        def data = importer.data
        then:
        assert data != null

        println data
    }
}
