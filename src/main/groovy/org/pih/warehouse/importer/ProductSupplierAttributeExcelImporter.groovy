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

import grails.util.Holders
import org.grails.plugins.excelimport.AbstractExcelImporter
import org.grails.plugins.excelimport.DefaultImportCellCollector
import org.grails.plugins.excelimport.ExcelImportService
import org.grails.plugins.excelimport.ExpectedPropertyType

class ProductSupplierAttributeExcelImporter extends AbstractExcelImporter implements DataImporter {

    static cellReporter = new DefaultImportCellCollector()

    ExcelImportService excelImportService

    @Delegate
    ProductSupplierAttributeImportDataService productSupplierAttributeImportDataService

    static Map columnMap = [
            sheet    : 'Sheet1',
            startRow : 1,
            columnMap: [
                    'A': 'productCode',
                    'B': 'productSupplierCode',
                    'C': 'attributeCode',
                    'D': 'attributeValue',
                    'E': 'unitOfMeasure',
            ]
    ]

    static Map propertyMap = [
            productCode         : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            productSupplierCode : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            attributeCode       : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            attributeValue      : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            unitOfMeasure       : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
    ]


    ProductSupplierAttributeExcelImporter(String fileName) {
        super()
        read(fileName)
        excelImportService = Holders.grailsApplication.mainContext.getBean("excelImportService")
        productSupplierAttributeImportDataService = Holders.grailsApplication.mainContext.getBean("productSupplierAttributeImportDataService")
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
