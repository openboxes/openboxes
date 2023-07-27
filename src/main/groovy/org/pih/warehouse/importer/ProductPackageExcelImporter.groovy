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

class ProductPackageExcelImporter extends AbstractExcelImporter {

    ExcelImportService excelImportService

    static Map columnMap = [
            sheet    : 'Sheet1',
            startRow : 1,
            columnMap: [
                    'A': 'id',
                    'B': 'productCode',
                    'C': 'productSupplierCode',
                    'D': 'name',
                    'E': 'description',
                    'F': 'gtin',
                    'G': 'uomCode',
                    'H': 'quantity',
                    'I': 'price'
            ]
    ]

    static Map propertyMap = [
            id                 : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            productCode        : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            productSupplierCode: ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            name               : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            description        : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            gtin               : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            uomCode            : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            quantity           : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            price              : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null])
    ]

    ProductPackageExcelImporter(String fileName) {
        super(fileName)
        excelImportService = Holders.grailsApplication.mainContext.getBean("excelImportService")
    }

    def getDataService() {
        return Holders.grailsApplication.mainContext.getBean("productPackageDataService")
    }

    List<Map> getData() {
        return excelImportService.convertColumnMapConfigManyRows(workbook, columnMap, null, null, propertyMap)
    }

    void validateData(ImportDataCommand command) {
        dataService.validate(command)
    }

    void importData(ImportDataCommand command) {
        dataService.process(command)
    }

}
