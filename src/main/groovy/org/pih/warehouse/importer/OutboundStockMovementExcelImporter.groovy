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

class OutboundStockMovementExcelImporter extends AbstractExcelImporter implements DataImporter {

    static cellReporter = new DefaultImportCellCollector()

    ExcelImportService excelImportService

    @Delegate
    OutboundStockMovementImportDataService outboundStockMovementImportDataService

    static Map columnMap = [
        sheet   : 'Sheet1',
            startRow : 1,
            columnMap : [
                    'A': 'origin', // 'Source',
                    'B': 'destination', // 'Dest Venue Code',
                    'D': 'productCode', // 'SKU Code',
                    'E': 'quantity', // 'Requested Quantity',
                    'H': 'requestedDeliveryDate', // 'Delivery Date',
                    'I': 'requestNumber', // 'Load Code',
                    'J': 'description' // 'Special Instructions'
            ]
    ]

    static Map propertyMap = [
            "origin"    : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            "destination"   : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            "productCode"       : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            "quantity": ([expectedType: ExpectedPropertyType.IntType, defaultValue: null]),
            "requestedDeliveryDate": ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            "requestNumber": ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            "description": ([expectedType: ExpectedPropertyType.StringType, defaultValue: null])
    ]

    OutboundStockMovementExcelImporter(String fileName) {
        super()
        read(fileName)
        excelImportService = Holders.grailsApplication.mainContext.getBean("excelImportService")
        outboundStockMovementImportDataService = Holders.grailsApplication.mainContext.getBean("outboundStockMovementImportDataService")
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
