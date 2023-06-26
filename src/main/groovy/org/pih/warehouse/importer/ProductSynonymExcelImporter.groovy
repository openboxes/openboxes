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
import org.grails.plugins.excelimport.ExpectedPropertyType

class ProductSynonymExcelImporter extends AbstractExcelImporter {

    static Map columnMap = [
            sheet    : 'Sheet1',
            startRow : 1,
            columnMap: [
                    'A': 'product.productCode',
                    'B': 'synonymTypeCode',
                    'C': 'locale',
                    'D': 'name',
            ]
    ]

    static Map propertyMap = [
            "product.productCode"       : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            "synonymTypeCode"           : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            "locale"                    : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            "name"                      : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
    ]


    ProductSynonymExcelImporter(String fileName) {
        super(fileName)
    }

    def getDataService() {
        return Holders.grailsApplication.mainContext.getBean("productSynonymDataService")
    }

    List<Map> getData() {
        return Holders.grailsApplication.mainContext.getBean("excelImportService")
                .convertColumnMapConfigManyRows(workbook, columnMap, null, null, propertyMap)
    }

    void validateData(ImportDataCommand command) {
        dataService.validateData(command)
    }

    void importData(ImportDataCommand command) {
        dataService.importData(command)
    }

}
