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
import org.grails.plugins.excelimport.ExcelImportUtils
import org.pih.warehouse.data.ProductSupplierDataService

class ProductSupplierExcelImporter extends AbstractExcelImporter {

    ProductSupplierDataService productSupplierDataService

    static Map columnMap = [
            sheet    : 'Sheet1',
            startRow : 1,
            columnMap: [
                    'A': 'id',
                    'B': 'code',
                    'C': 'productCode',
                    'D': 'legacyProductCode',
                    'E': 'productName',
                    'F': 'description',
                    'G': 'supplierId',
                    'H': 'supplierName',
                    'I': 'supplierCode',
                    'J': 'supplierProductName',
                    'K': 'manufacturerId',
                    'L': 'manufacturerName',
                    'M': 'manufacturerCode',
                    'N': 'manufacturerProductName',
                    'O': 'defaultProductPackageUomCode',
                    'P': 'defaultProductPackageQuantity',
                    'Q': 'defaultProductPackagePrice',
                    'R': 'standardLeadTimeDays',
                    'S': 'preferenceTypeCode',
                    'T': 'ratingTypeCode',
                    'U': 'comments',
            ]
    ]

    static Map propertyMap = [
            id                           : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            code                         : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            productCode                  : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            legacyProductCode            : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            productName                  : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            description                  : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            supplierId                   : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            supplierName                 : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            supplierCode                 : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            supplierProductName          : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            manufacturerId               : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            manufacturerName             : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            manufacturerCode             : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            manufacturerProductName      : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            defaultProductPackageUomCode : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            defaultProductPackageQuantity: ([expectedType: ExcelImportUtils.PROPERTY_TYPE_INT, defaultValue: null]),
            defaultProductPackagePrice   : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            standardLeadTimeDays         : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_INT, defaultValue: null]),
            preferenceTypeCode           : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            ratingTypeCode               : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            comments                     : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null])
    ]


    ProductSupplierExcelImporter(String fileName) {
        super(fileName)
    }

    def getDataService() {
        return Holders.getGrailsApplication().getMainContext().getBean("productSupplierDataService")
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
