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
import org.grails.plugins.excelimport.ExpectedPropertyType

class ProductExcelImporter extends AbstractExcelImporter {

    def productService
    def excelImportService

    static Map cellMap = [
            sheet: 'Sheet1', startRow: 1, cellMap: []]

    static Map columnMap = [
            sheet    : 'Sheet1',
            startRow : 1,
            columnMap: [
                    'A': 'idaCode',
                    'B': 'openBoxesId',
                    'C': 'productDescription',
                    'D': 'french',
                    'E': 'search1',
                    'F': 'search2',
                    'G': 'packaging',
                    'H': 'unit',
                    'I': 'manufacturer',
                    'J': 'comment',
                    'K': 'code'
            ]
    ]

    static Map propertyMap = [
            idaCode           : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            openBoxesId       : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            productDescription: ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            french            : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            search1           : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            search2           : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            packaging         : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            unit              : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            manufacturer      : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            comment           : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            code              : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null])
    ]


    ProductExcelImporter(String fileName) {
        super(fileName)
        productService = Holders.getGrailsApplication().getMainContext().getBean("productService")
    }


    List<Map> getData() {
        return excelImportService.convertColumnMapConfigManyRows(workbook, columnMap, null, propertyMap)
    }


    void validateData(ImportDataCommand command) {
        productService.validateData(command)
    }


    /**
     * Import data from given inventoryMapList into database.
     *
     * @param location
     * @param inventoryMapList
     * @param errors
     */
    void importData(ImportDataCommand command) {
        productService.importData(command)

    }


}