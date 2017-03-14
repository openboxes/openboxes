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
import org.apache.commons.lang.NotImplementedException
import static org.grails.plugins.excelimport.ExpectedPropertyType.*
import org.apache.commons.lang.StringUtils
import org.grails.plugins.excelimport.AbstractExcelImporter
import org.grails.plugins.excelimport.ExcelImportService

// import java.text.ParseException;
// import java.text.SimpleDateFormat;

import org.pih.warehouse.core.Constants
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.product.Product

import java.text.SimpleDateFormat

class InventoryExcelImporter extends AbstractExcelImporter {

    def inventoryService

	static Map cellMap = [ sheet:'Sheet1', startRow: 1, cellMap: [] ]

	static Map columnMap = [
		sheet:'Sheet1',
		startRow: 1,
		columnMap: [
                'A':'productCode',
                'B':'product',
                'C':'lotNumber',
                'D':'expirationDate',
                'E':'manufacturer',
                'F':'manufacturerCode',
                'G':'quantity',
                'H':'binLocation',
                'I':'comments'
		]
	]

	static Map propertyMap = [
            productCode:([expectedType: StringType, defaultValue:null]),
            product:([expectedType: StringType, defaultValue:null]),
            lotNumber:([expectedType: StringType, defaultValue:null]),
            expirationDate:([expectedType: DateType, defaultValue:null]),
            manufacturer:([expectedType: StringType, defaultValue:null]),
            manufacturerCode:([expectedType: StringType, defaultValue:null]),
            quantity:([expectedType: IntType, defaultValue:null]),
            binLocation:([expectedType: StringType, defaultValue:null]),
            comments:([expectedType: StringType, defaultValue:null])
	]

	def getExcelImportService() {
		ExcelImportService.getService()
	}

	InventoryExcelImporter(String fileName) {
		super(fileName)
        inventoryService = Holders.grailsApplication.getMainContext().getBean("inventoryService")
	}


	List<Map> getData() {
		return excelImportService.columns(workbook, columnMap, null, propertyMap)
    }


    void validateData(ImportDataCommand command) {
        inventoryService.validateInventoryData(command)
    }


    /**
     * Import data from given inventoryMapList into database.
     *
     * @param location
     * @param inventoryMapList
     * @param errors
     */
    void importData(ImportDataCommand command) {
        inventoryService.importInventoryData(command)

    }

}