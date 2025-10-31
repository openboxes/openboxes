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

class InventoryLevelExcelImporter extends AbstractExcelImporter implements DataImporter {

    static cellReporter = new DefaultImportCellCollector()

    ExcelImportService excelImportService

    @Delegate
    InventoryLevelImportDataService inventoryLevelImportDataService

    static Map cellMap = [sheet: 'Sheet1', startRow: 1, cellMap: []]

    static Map columnMap = [
            sheet    : 'Sheet1',
            startRow : 1,
            columnMap: [
                    'A': 'productCode',
                    'B': 'productName',
                    'C': 'facility',
                    'D': 'status',
                    'E': 'internalLocation',
                    'F': 'preferredBinLocation',
                    'G': 'replenishmentLocation',
                    'H': 'abcClass',
                    'I': 'minQuantity',
                    'J': 'reorderQuantity',
                    'K': 'maxQuantity'
            ]
    ]

    static Map propertyMap = [
            productCode            : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            productName            : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            facility               : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            status                 : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            internalLocation       : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            preferredBinLocation   : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            replenishmentLocation  : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            abcClass               : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            minQuantity            : ([expectedType: ExpectedPropertyType.IntType, defaultValue: null]),
            reorderQuantity        : ([expectedType: ExpectedPropertyType.IntType, defaultValue: null]),
            maxQuantity            : ([expectedType: ExpectedPropertyType.IntType, defaultValue: null]),
    ]

    InventoryLevelExcelImporter(String fileName) {
        super()
        read(fileName)
        excelImportService = Holders.grailsApplication.mainContext.getBean("excelImportService")
        inventoryLevelImportDataService = Holders.grailsApplication.mainContext.getBean("inventoryLevelImportDataService")
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
