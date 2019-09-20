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
import org.grails.plugins.excelimport.AbstractExcelImporter
import org.grails.plugins.excelimport.ExcelImportUtils
import org.pih.warehouse.data.CategoryDataService

class CategoryExcelImporter extends AbstractExcelImporter {

    CategoryDataService categoryDataService

    static Map columnMap = [
            sheet    : 'Sheet1',
            startRow : 1,
            columnMap: [
                    'A': 'id',
                    'B': 'name',
                    'C': 'parentCategoryId'
            ]
    ]

    static Map propertyMap = [
            id              : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            name            : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            parentCategoryId: ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null])
    ]

    CategoryExcelImporter(String fileName) {
        super(fileName)
        categoryDataService = ApplicationHolder.getApplication().getMainContext().getBean("categoryDataService")
    }

    List<Map> getData() {
        return ExcelImportUtils.convertColumnMapConfigManyRows(workbook, columnMap, null, propertyMap)
    }

    void validateData(ImportDataCommand command) {
        categoryDataService.validateData(command)
    }

    void importData(ImportDataCommand command) {
        categoryDataService.importData(command)
    }

}