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
import static org.grails.plugins.excelimport.ExpectedPropertyType.*


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
	def grailsApplication
	def excepImportService

    static Map cellMap = [
		sheet:'Sheet1', startRow: 1, cellMap: []]

	static Map columnMap = [
		sheet:'Sheet1',
		startRow: 1,
		columnMap: [
			'A':'productCode',
			'B':'product',
			'C':'manufacturer',
			'D':'manufacturerCode',
            'E':'vendor',
            'F':'vendorCode',
            'G':'totalOrderQuantity',
            'H':'orderNotes',
            'I':'leadTime',
            'J':'packageCost',
            'K':'unitsPerPackage',
            'L':'unitCost',
            'M':'quantityUnitsCosted',
            'N':'totalCost',
            'O':'quoteNotes',
            'P':'quantityToExpediteToMiami',
            'Q':'remaining',
            'R':'miamiStatus',
            'S':'uhmStatus',
            'T':'receptionNotes'
		]
	]

	static Map propertyMap = [
            productCode:([expectedType: StringType, defaultValue:null]),
            product:([expectedType: StringType, defaultValue:null]),
            manufacturer: ([expectedType: StringType, defaultValue:null]),
            manufacturerCode: ([expectedType: StringType, defaultValue:null]),
            vendor: ([expectedType: StringType, defaultValue:null]),
            vendorCode: ([expectedType: StringType, defaultValue:null]),
            totalOrderQuantity: ([expectedType: StringType, defaultValue:null]),
            orderNotes: ([expectedType: StringType, defaultValue:null]),
            leadTime: ([expectedType: StringType, defaultValue:null]),
            packageCost: ([expectedType: StringType, defaultValue:null]),
            unitsPerPackage: ([expectedType: StringType, defaultValue:null]),
            unitCost: ([expectedType: StringType, defaultValue:null]),
            quantityUnitsCosted: ([expectedType: StringType, defaultValue:null]),
            totalCost: ([expectedType: StringType, defaultValue:null]),
            quoteNotes: ([expectedType: StringType, defaultValue:null]),
            quantityToExpediteToMiami: ([expectedType: StringType, defaultValue:null]),
            remaining: ([expectedType: StringType, defaultValue:null]),
            miamiStatus: ([expectedType: StringType, defaultValue:null]),
            uhmStatus: ([expectedType: StringType, defaultValue:null]),
            receptionNotes: ([expectedType: StringType, defaultValue:null])
	]

    public PurchaseOrderExcelImporter(String fileName) {
        super(fileName)
    }



    List<Map> getData() {
		return excepImportService.convertColumnMapConfigManyRows(workbook, columnMap, null, propertyMap)
	}


}