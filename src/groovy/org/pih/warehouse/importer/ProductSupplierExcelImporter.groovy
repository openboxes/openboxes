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
import org.pih.warehouse.data.ProductSupplierDataService

class ProductSupplierExcelImporter extends AbstractExcelImporter {

    ProductSupplierDataService productSupplierDataService

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
            active                                : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            id                                    : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            code                                  : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            name                                  : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            productCode                           : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            productName                           : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            legacyProductCode                     : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            supplierName                          : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            supplierCode                          : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            manufacturerName                      : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            manufacturerCode                      : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            minOrderQuantity                      : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_INT, defaultValue: null]),
            contractPricePrice                    : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            contractPriceValidUntil               : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            ratingType                            : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            globalPreferenceTypeName              : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            globalPreferenceTypeValidityStartDate : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            globalPreferenceTypeValidityEndDate   : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            globalPreferenceTypeComments          : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            defaultProductPackageUomCode          : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            defaultProductPackageQuantity         : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_INT, defaultValue: null]),
            defaultProductPackagePrice            : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null])
    ]


    ProductSupplierExcelImporter(String fileName) {
        super(fileName)
    }

    def getDataService() {
        return ApplicationHolder.getApplication().getMainContext().getBean("productSupplierDataService")
    }


    List<Map> getData() {
        return ExcelImportUtils.convertColumnMapConfigManyRows(workbook, columnMap, null, propertyMap)
    }

    void validateData(ImportDataCommand command) {
        dataService.validate(command)
    }

    void importData(ImportDataCommand command) {
        dataService.process(command)
    }

}
