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

/**
 * Product code
 * Product
 * Manufacturer
 * Manufacturer code
 * Vendor
 * Vendor code
 * Total Order Quantity Round Up
 * Order notes
 * Lead time
 * Package cost
 * Units per package
 * Unit cost
 * Quantity of units quoted
 * Total cost
 * Quote notes
 * Quantity to expedite to Miami
 * Remaining
 * Miami Status
 * UHM Status
 * Reception notes

 */
class PurchaseOrderExcelImporter extends AbstractExcelImporter {

    def excelImportService

    static Map cellMap = [
            sheet: 'Sheet1', startRow: 1, cellMap: []]

    static Map columnMap = [
            sheet    : 'Sheet1',
            startRow : 1,
            columnMap: [
                    'A': 'productCode',
                    'B': 'product',
                    'C': 'manufacturer',
                    'D': 'manufacturerCode',
                    'E': 'vendor',
                    'F': 'vendorCode',
                    'G': 'totalOrderQuantity',
                    'H': 'orderNotes',
                    'I': 'leadTime',
                    'J': 'packageCost',
                    'K': 'unitsPerPackage',
                    'L': 'unitCost',
                    'M': 'quantityUnitsCosted',
                    'N': 'totalCost',
                    'O': 'quoteNotes',
                    'P': 'quantityToExpediteToMiami',
                    'Q': 'remaining',
                    'R': 'miamiStatus',
                    'S': 'uhmStatus',
                    'T': 'receptionNotes'
            ]
    ]

    static Map propertyMap = [
            productCode              : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            product                  : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            manufacturer             : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            manufacturerCode         : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            vendor                   : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            vendorCode               : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            totalOrderQuantity       : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            orderNotes               : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            leadTime                 : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            packageCost              : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            unitsPerPackage          : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            unitCost                 : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            quantityUnitsCosted      : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            totalCost                : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            quoteNotes               : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            quantityToExpediteToMiami: ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            remaining                : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            miamiStatus              : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            uhmStatus                : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            receptionNotes           : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null])
    ]

    PurchaseOrderExcelImporter(String fileName) {
        super(fileName)
        excelImportService = Holders.grailsApplication.mainContext.getBean("excelImportService")
    }


    List<Map> getData() {
        return excelImportService.convertColumnMapConfigManyRows(workbook, columnMap, null, null, propertyMap)
    }

    void validateData(ImportDataCommand command) {
        throw new UnsupportedOperationException()
    }

    void importData(ImportDataCommand command) {
        throw new UnsupportedOperationException()
    }
}
