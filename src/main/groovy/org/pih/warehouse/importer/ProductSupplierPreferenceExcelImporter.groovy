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

class ProductSupplierPreferenceExcelImporter extends AbstractExcelImporter implements DataImporter {

    static cellReporter = new DefaultImportCellCollector()

    ExcelImportService excelImportService

    @Delegate
    ProductSupplierPreferenceImportDataService productSupplierPreferenceImportDataService

    ProductSupplierPreferenceExcelImporter(String fileName) {
        super()
        read(fileName)
        excelImportService = Holders.grailsApplication.mainContext.getBean("excelImportService")
        productSupplierPreferenceImportDataService = Holders.grailsApplication.mainContext.getBean("productSupplierPreferenceImportDataService")
    }

    static Map columnMap = [
            sheet    : 'Sheet1',
            startRow : 1,
            columnMap: [
                    'A': 'supplierCode',
                    'B': 'organizationCode',
                    'C': 'organizationName',
                    'D': 'preferenceTypeName',
                    'E': 'validityStartDate',
                    'F': 'validityEndDate',
                    'G': 'preferenceComments',
            ]
    ]

    static Map propertyMap = [
            supplierCode         : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            organizationCode     : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            organizationName     : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            preferenceTypeName   : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            validityStartDate    : ([expectedType: ExpectedPropertyType.DateJavaType, defaultValue: null]),
            validityEndDate      : ([expectedType: ExpectedPropertyType.DateJavaType, defaultValue: null]),
            preferenceComments   : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
    ]

    List<Map> getData() {
        excelImportService.columns(
                workbook,
                columnMap,
                cellReporter,
                propertyMap
        )
    }

}
