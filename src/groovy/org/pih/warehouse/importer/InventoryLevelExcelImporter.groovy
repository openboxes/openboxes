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

import org.grails.plugins.excelimport.*
import static org.grails.plugins.excelimport.ExpectedPropertyType.*
// import java.text.ParseException;
// import java.text.SimpleDateFormat;
class InventoryLevelExcelImporter extends AbstractExcelImporter {

	def dataService
	def grailsApplication

	static Map cellMap = [ sheet:'Sheet1', startRow: 1, cellMap: [] ]

	static Map columnMap = [
		sheet:'Sheet1',
		startRow: 1,
		columnMap: [
                'A':'status',
                'B':'productCode',
                'C':'productName',
                'D':'category',
                'E':'tags',
                'F':'manufacturer',
                'G':'manufacturerCode',
                'H':'vendor',
                'I':'vendorCode',
                'J':'binLocation',
                'K':'unitOfMeasure',
                'L':'package',
                'M':'packageUom',
                'N':'packageSize',
                'O':'pricePerPackage',
                'P':'pricePerUnit',
                'Q':'minQuantity',
                'R':'reorderQuantity',
                'S':'maxQuantity',
                'T':'currentQuantity',
                'U':'preferredForReorder'
		]
	]

    static Map propertyMap = [
            status:([expectedType: StringType, defaultValue:null]),
            productCode:([expectedType: StringType, defaultValue:null]),
            productName: ([expectedType: StringType, defaultValue:null]),
            tags: ([expectedType: StringType, defaultValue:null]),
            category: ([expectedType: StringType, defaultValue:null]),
            manufacturer:([expectedType: StringType, defaultValue:null]),
            manufacturerCode:([expectedType: StringType, defaultValue:null]),
            vendor:([expectedType: StringType, defaultValue:null]),
            vendorCode:([expectedType: StringType, defaultValue:null]),
            binLocation:([expectedType: StringType, defaultValue:null]),
            unitOfMeasure:([expectedType: StringType, defaultValue:null]),
            package:([expectedType: StringType, defaultValue:null]),
            packageUom:([expectedType: StringType, defaultValue:null]),
            packageSize:([expectedType: IntType, defaultValue:null]),
            pricePerPackage:([expectedType: IntType, defaultValue:null]),
            pricePerUnit:([expectedType: IntType, defaultValue:null]),
            //pricePerUnitStatic:([expectedType: IntType, defaultValue:null]),
            minQuantity:([expectedType: IntType, defaultValue:null]),
            reorderQuantity:([expectedType: IntType, defaultValue:null]),
            maxQuantity:([expectedType: IntType, defaultValue:null]),
            currentQuantity:([expectedType: IntType, defaultValue:null]),
            preferredForReorder:([expectedType: StringType, defaultValue:null])

	]


	def getExcelImportService() {
		ExcelImportService.getService()
	}


	InventoryLevelExcelImporter(String fileName) {
		super(fileName)
		dataService = grailsApplication.mainContext.getBean("dataService")
	}


	List<Map> getData() {
		return excelImportService.columns(workbook, columnMap, null, propertyMap)
	}



	void validateData(ImportDataCommand command) {
        dataService.validateInventoryLevels(command)

    }

	void importData(ImportDataCommand command) {
        dataService.importInventoryLevels(command)
	}






}