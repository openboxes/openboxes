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

class UserExcelImporter extends AbstractExcelImporter implements DataImporter {

    static cellReporter = new DefaultImportCellCollector()

    ExcelImportService excelImportService

    @Delegate
    UserImportDataService userImportDataService

    static Map columnMap = [
            sheet    : 'Sheet1',
            startRow : 1,
            columnMap: [
                    'A': 'username',
                    'B': 'firstName',
                    'C': 'lastName',
                    'D': 'email',
                    'E': 'defaultRoles'
            ]
    ]

    static Map propertyMap = [
            username    : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            firstName   : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            lastName    : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            email       : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            defaultRoles: ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
    ]

    UserExcelImporter(String fileName) {
        super()
        read(fileName)
        excelImportService = Holders.grailsApplication.mainContext.getBean("excelImportService")
        userImportDataService = Holders.grailsApplication.mainContext.getBean("userImportDataService")
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
