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

class LocationExcelImporter extends AbstractExcelImporter implements DataImporter {

    static cellReporter = new DefaultImportCellCollector()

    ExcelImportService excelImportService

    @Delegate
    LocationImportDataService locationImportDataService

    static Map columnMap = [
            sheet    : 'Sheet1',
            startRow : 1,
            columnMap: [
                    'A': 'id',
                    'B': 'name',
                    'C': 'active',
                    'D': 'locationNumber',
                    'E': 'locationType',
                    'F': 'locationGroup',
                    'G': 'parentLocation',
                    'H': 'organization',
                    'I': 'streetAddress',
                    'J': 'streetAddress2',
                    'K': 'city',
                    'L': 'stateOrProvince',
                    'M': 'postalCode',
                    'N': 'country',
                    'O': 'description',
            ]
    ]

    static Map propertyMap = [
            id              : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            name            : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            active          : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            locationNumber  : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            locationType    : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            locationGroup   : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            parentLocation  : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            organization    : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            streetAddress   : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            streetAddress2  : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            city            : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            stateOrProvince : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            postalCode      : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            country         : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            description     : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
    ]

    LocationExcelImporter(String fileName) {
        super()
        read(fileName)
        excelImportService = Holders.grailsApplication.mainContext.getBean("excelImportService")
        locationImportDataService = Holders.grailsApplication.mainContext.getBean("locationImportDataService")
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
