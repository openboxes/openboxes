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

import org.grails.plugins.excelimport.ExcelImportService
import org.grails.plugins.excelimport.ExpectedPropertyType

// import java.text.ParseException;
// import java.text.SimpleDateFormat;

class ProductGroupExcelImporter extends AbstractExcelImporter {

	static Map cellMap = [ sheet:'Sheet1', startRow: 1, cellMap: [] ]

	static Map columnMap = [
		sheet:'Sheet1',
		startRow: 1,
		columnMap: [
                'A':'productCode',
                'B':'productName',
                'C':'genericProduct'
		]
	]

    static Map propertyMap = [
            productCode:([expectedType: ExpectedPropertyType.StringType, defaultValue:null]),
            productName: ([expectedType: ExpectedPropertyType.StringType, defaultValue:null]),
            genericProduct: ([expectedType: ExpectedPropertyType.StringType, defaultValue:null])
	]

	def getExcelImportService() {
		ExcelImportService.getService()
	}

	ProductGroupExcelImporter(String fileName) {
		super(fileName)
	}

	List<Map> getData() {
		return excelImportService.columns(workbook, columnMap, null, propertyMap)
	}

	void validateData(ImportDataCommand command) {
		//inventoryService.validateData(command)
	}

	void importData(ImportDataCommand command) {
		//inventoryService.importData(command)
	}






}