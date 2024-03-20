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

class PurchaseOrderActualReadyDateExcelImporter extends AbstractExcelImporter implements DataImporter {

    static cellReporter = new DefaultImportCellCollector()

    ExcelImportService excelImportService

    @Delegate
    PurchaseOrderActualReadyDateImportDataService purchaseOrderActualReadyDateImportDataService

    static Map columnMap = [
            sheet    : 'Sheet1',
            startRow : 1,
            columnMap: [
                    'A': 'orderItemId',
                    'B': 'orderNumber',
                    'C': 'productCode',
                    'D': 'productName',
                    'E': 'actualReadyDate',
            ]
    ]

    static Map propertyMap = [
            "orderItemId"           : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            "orderNumber"           : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            "productCode"           : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            "productName"           : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            "actualReadyDate"       : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
    ]


    PurchaseOrderActualReadyDateExcelImporter(String fileName) {
        super()
        read(fileName)
        excelImportService = Holders.grailsApplication.mainContext.getBean("excelImportService")
        purchaseOrderActualReadyDateImportDataService = Holders.grailsApplication.mainContext.getBean("purchaseOrderActualReadyDateImportDataService")
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
