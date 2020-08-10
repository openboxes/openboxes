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
import org.grails.plugins.excelimport.ExcelImportService
import org.grails.plugins.excelimport.ExpectedPropertyType

class InventoryExcelImporter extends AbstractExcelImporter {

    def inventoryService
    def excelImportService

    static Map cellMap = [sheet: 'Sheet1', startRow: 1, cellMap: []]

    static Map columnMap = [
            sheet    : 'Sheet1',
            startRow : 1,
            columnMap: [
                    'A': 'productCode',
                    'B': 'product',
                    'C': 'lotNumber',
                    'D': 'expirationDate',
                    'E': 'binLocation',
                    'F': 'quantityOnHand',  // OB QoH
                    'G': 'quantity', // Physical QoH
                    'H': 'comments'
            ]
    ]

    static Map propertyMap = [
            productCode     : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            product         : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            lotNumber       : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            expirationDate  : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            binLocation    : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            quantityOnHand: ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            quantity        : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            comments        : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null])
    ]


    InventoryExcelImporter(String fileName) {
        super(fileName)
        excelImportService = Holders.grailsApplication.mainContext.getBean("excelImportService")
        inventoryService = Holders.grailsApplication.mainContext.getBean("inventoryService")
    }


    List<Map> getData() {
        return excelImportService.convertColumnMapConfigManyRows(workbook, columnMap, null, null, propertyMap)
    }


    void validateData(ImportDataCommand command) {
        inventoryService.validateInventoryData(command)
    }


    /**
     * Import data from given inventoryMapList into database.
     *
     * @param location
     * @param inventoryMapList
     * @param errors
     */
    void importData(ImportDataCommand command) {
        inventoryService.importInventoryData(command)
    }

}
