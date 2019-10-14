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

class InventoryItemExcelImporter extends AbstractExcelImporter {

    def dataService
    ExcelImportService excelImportService

    static Map columnMap = [
            sheet    : 'Sheet1',
            startRow : 1,
            columnMap: [
                    'A': 'status',
                    'B': 'productCode',
                    'C': 'productName',
                    'D': 'category',
                    'E': 'tags',
                    'F': 'manufacturer',
                    'G': 'manufacturerCode',
                    'H': 'vendor',
                    'I': 'vendorCode',
                    'J': 'binLocation',
                    'K': 'unitOfMeasure',
                    'L': 'package',
                    'M': 'packageUom',
                    'N': 'packageSize',
                    'O': 'pricePerPackage',
                    'P': 'pricePerUnit',
                    'Q': 'minQuantity',
                    'R': 'reorderQuantity',
                    'S': 'maxQuantity',
                    'T': 'currentQuantity'
            ]
    ]

    static Map propertyMap = [
            status          : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            productCode     : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            productName     : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            tags            : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            category        : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            manufacturer    : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            manufacturerCode: ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            vendor          : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            vendorCode      : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            binLocation     : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            unitOfMeasure   : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            package         : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            packageUom      : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            packageSize     : ([expectedType: ExpectedPropertyType.IntType, defaultValue: null]),
            pricePerPackage : ([expectedType: ExpectedPropertyType.IntType, defaultValue: null]),
            pricePerUnit    : ([expectedType: ExpectedPropertyType.IntType, defaultValue: null]),
            minQuantity     : ([expectedType: ExpectedPropertyType.IntType, defaultValue: null]),
            reorderQuantity : ([expectedType: ExpectedPropertyType.IntType, defaultValue: null]),
            maxQuantity     : ([expectedType: ExpectedPropertyType.IntType, defaultValue: null]),
            currentQuantity : ([expectedType: ExpectedPropertyType.IntType, defaultValue: null])
    ]


    InventoryItemExcelImporter(String fileName) {
        super(fileName)
        excelImportService = Holders.grailsApplication.mainContext.getBean("excelImportService")
        dataService = Holders.grailsApplication.mainContext.getBean("dataService")
    }


    List<Map> getData() {
        return excelImportService.convertColumnMapConfigManyRows(workbook, columnMap, null, null, propertyMap)
    }


    void validateData(ImportDataCommand command) {
        dataService.validateInventoryLevels(command)
    }

    void importData(ImportDataCommand command) {
        dataService.importInventoryLevels(command)
    }


}
