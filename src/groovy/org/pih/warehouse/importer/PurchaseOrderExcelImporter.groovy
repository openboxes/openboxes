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


import org.grails.plugins.excelimport.AbstractExcelImporter
import org.grails.plugins.excelimport.ExcelImportUtils

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

    def productService

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
            productCode              : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            product                  : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            manufacturer             : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            manufacturerCode         : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            vendor                   : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            vendorCode               : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            totalOrderQuantity       : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            orderNotes               : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            leadTime                 : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            packageCost              : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            unitsPerPackage          : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            unitCost                 : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            quantityUnitsCosted      : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            totalCost                : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            quoteNotes               : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            quantityToExpediteToMiami: ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            remaining                : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            miamiStatus              : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            uhmStatus                : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            receptionNotes           : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null])
    ]

    PurchaseOrderExcelImporter(String fileName) {
        super(fileName)
    }


    List<Map> getData() {
        return ExcelImportUtils.convertColumnMapConfigManyRows(workbook, columnMap, null, propertyMap)
    }


}