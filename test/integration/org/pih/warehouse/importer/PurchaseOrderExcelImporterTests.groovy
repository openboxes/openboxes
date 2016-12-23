package org.pih.warehouse.importer

import grails.test.mixin.integration.Integration
import org.apache.commons.lang.StringUtils
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.pih.warehouse.product.Product
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource

@Integration
class PurchaseOrderExcelImporterTests {

    @Before
    void setUp() { }

    @After
    void tearDown() { }

    @Test
    void testSomething() {
        Resource resource = new ClassPathResource("resources/purchaseOrders.xls")
        def file = resource.getFile()
        assert file.exists()

        def importer = new PurchaseOrderExcelImporter(file.absolutePath)
        def data = importer.data
        assert data != null

        println data
        /*
        data.eachWithIndex { row, index ->
            println "${index}: ${row}"

            def product = Product.findByProductCode(row.productCode)
            assert product
            //assert product.name == row.product
            println "Levenshtein distnace: " + StringUtils.getLevenshteinDistance(product.name, row.product)
        }
        */




    }
}
