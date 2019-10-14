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

class UserLocationExcelImporter extends AbstractExcelImporter {

    ExcelImportService excelImportService

    static Map columnMap = [
            sheet    : 'Sheet1',
            startRow : 1,
            columnMap: [
                    'A': 'username',
                    'B': 'locationName',
                    'C': 'roleName'
            ]
    ]

    static Map propertyMap = [
            username    : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            locationName: ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            roleName    : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null])
    ]


    UserLocationExcelImporter(String fileName) {
        super(fileName)
        excelImportService = Holders.grailsApplication.mainContext.getBean("excelImportService")
    }

    def getDataService() {
        return Holders.grailsApplication.mainContext.getBean("userLocationDataService")
    }

    List<Map> getData() {
        return excelImportService.convertColumnMapConfigManyRows(workbook, columnMap, null, null, null, propertyMap)
    }


    void validateData(ImportDataCommand command) {
        dataService.validateData(command)
    }


    /**
     * Import data from given map into database.
     *
     * @param location
     * @param inventoryMapList
     * @param errors
     */
    void importData(ImportDataCommand command) {
        dataService.importData(command)
    }

}
