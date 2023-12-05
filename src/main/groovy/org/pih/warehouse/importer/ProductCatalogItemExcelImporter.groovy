/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.importer

import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.grails.plugins.excelimport.AbstractExcelImporter
import org.grails.plugins.excelimport.DefaultImportCellCollector
import org.grails.plugins.excelimport.ExcelImportService
import org.grails.plugins.excelimport.ExpectedPropertyType
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductCatalog
import org.pih.warehouse.product.ProductCatalogItem
import org.springframework.validation.BeanPropertyBindingResult

@Transactional
class ProductCatalogItemExcelImporter extends AbstractExcelImporter implements DataImporter {

    static cellReporter = new DefaultImportCellCollector()

    ExcelImportService excelImportService

    @Delegate
    ProductCatalogItemImportDataService productCatalogItemImportDataService

    static Map columnMap = [
            sheet    : 'Sheet1',
            startRow : 1,
            columnMap: [
                    'A': 'productCatalogCode',
                    'B': 'productCode',
                    'C': 'productName'
            ]
    ]

    static Map propertyMap = [
            productCatalogCode: ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            productCode       : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            productName       : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null])
    ]


    ProductCatalogItemExcelImporter(String fileName) {
        super()
        read(fileName)
        excelImportService = Holders.grailsApplication.mainContext.getBean("excelImportService")
        productCatalogItemImportDataService = Holders.grailsApplication.mainContext.getBean("productCatalogItemImportDataService")
    }


    List<Map> getData() {
        excelImportService.columns(
                workbook,
                columnMap,
                cellReporter,
                propertyMap
        )
    }
}
