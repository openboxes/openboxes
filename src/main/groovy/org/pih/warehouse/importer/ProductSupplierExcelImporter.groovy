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

class ProductSupplierExcelImporter extends AbstractExcelImporter implements DataImporter {

    static cellReporter = new DefaultImportCellCollector()

    ExcelImportService excelImportService

    @Delegate
    ProductSupplierImportDataService productSupplierImportDataService

    static Map columnMap = [
            sheet    : 'Sheet1',
            startRow : 1,
            columnMap: [
                    'A': 'active',
                    'B': 'id',
                    'C': 'code',
                    'D': 'name',
                    'E': 'productCode',
                    'F': 'productName',
                    'G': 'legacyProductCode',
                    'H': 'supplierName',
                    'I': 'supplierCode',
                    'J': 'manufacturerName',
                    'K': 'manufacturerCode',
                    'L': 'minOrderQuantity',
                    'M': 'contractPricePrice',
                    'N': 'contractPriceValidUntil',
                    'O': 'ratingTypeCode',
                    'P': 'globalPreferenceTypeName',
                    'Q': 'globalPreferenceTypeValidityStartDate',
                    'R': 'globalPreferenceTypeValidityEndDate',
                    'S': 'globalPreferenceTypeComments',
                    'T': 'defaultProductPackageUomCode',
                    'U': 'defaultProductPackageQuantity',
                    'V': 'defaultProductPackagePrice',
            ]
    ]

    static Map propertyMap = [
            active                                : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            id                                    : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            code                                  : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            name                                  : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            productCode                           : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            productName                           : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            legacyProductCode                     : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            supplierName                          : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            supplierCode                          : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            manufacturerName                      : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            manufacturerCode                      : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            minOrderQuantity                      : ([expectedType: ExpectedPropertyType.IntType, defaultValue: null]),
            contractPricePrice                    : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            contractPriceValidUntil               : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            ratingType                            : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            globalPreferenceTypeName              : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            globalPreferenceTypeValidityStartDate : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            globalPreferenceTypeValidityEndDate   : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            globalPreferenceTypeComments          : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            defaultProductPackageUomCode          : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            defaultProductPackageQuantity         : ([expectedType: ExpectedPropertyType.IntType, defaultValue: null]),
            defaultProductPackagePrice            : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null])
    ]


    ProductSupplierExcelImporter(String fileName) {
        super()
        read(fileName)
        excelImportService = Holders.grailsApplication.mainContext.getBean("excelImportService")
        productSupplierImportDataService = Holders.grailsApplication.mainContext.getBean("productSupplierImportDataService")
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
