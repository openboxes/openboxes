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

import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.grails.plugins.excelimport.ExcelImportUtils

class LocationExcelImporter extends AbstractExcelImporter {

    static Map columnMap = [
            sheet    : 'Sheet1',
            startRow : 1,
            columnMap: [
                    'A': 'name',
                    'B': 'locationNumber',
                    'C': 'locationType',
                    'D': 'locationGroup',
                    'E': 'parentLocation',
                    'F': 'address',
                    'G': 'address2',
                    'H': 'city',
                    'I': 'stateOrProvince',
                    'J': 'postalCode',
                    'K': 'country',
                    'L': 'description',
            ]
    ]

    static Map propertyMap = [
            name           : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            locationNumber : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            locationType   : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            locationGroup  : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            parentLocation : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            address        : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            address2       : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            city           : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            stateOrProvince: ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            postalCode     : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            country        : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            description    : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
    ]

    LocationExcelImporter(String fileName) {
        super(fileName)
    }

    LocationExcelImporter(String fileName, InputStream inputStream) {
        super(fileName, inputStream)
    }

    def getDataService() {
        return ApplicationHolder.getApplication().getMainContext().getBean("locationDataService")
    }

    List<Map> getData() {
        return ExcelImportUtils.convertColumnMapConfigManyRows(workbook, columnMap, null, propertyMap)
    }

    void validateData(ImportDataCommand command) {
        dataService.validateData(command)
    }

    void importData(ImportDataCommand command) {
        dataService.importData(command)
    }

}
