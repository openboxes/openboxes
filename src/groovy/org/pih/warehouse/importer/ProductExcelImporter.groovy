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
import org.grails.plugins.excelimport.ExcelImportService;

import static org.grails.plugins.excelimport.ExpectedPropertyType.*


class ProductExcelImporter extends AbstractExcelImporter {

	def productService 
	def grailsApplication

	static Map cellMap = [
		sheet:'Sheet1', startRow: 1, cellMap: []]
	
	static Map columnMap = [
		sheet:'Sheet1',
		startRow: 1,
		columnMap: [
			'A':'idaCode',
			'B':'openBoxesId',
			'C':'productDescription',
			'D':'french',
			'E':'search1',
			'F':'search2',
			'G':'packaging',
			'H':'unit',
			'I':'manufacturer',
			'J':'comment',
			'K':'code'
		]
	]

	static Map propertyMap = [
		idaCode:([expectedType: StringType, defaultValue:null]),
		openBoxesId:([expectedType: StringType, defaultValue:null]),
		productDescription: ([expectedType: StringType, defaultValue:null]),
		french: ([expectedType: StringType, defaultValue:null]),
		search1:([expectedType: StringType, defaultValue:null]),
		search2:([expectedType: StringType, defaultValue:null]),
		packaging:([expectedType: StringType, defaultValue:null]),
		unit:([expectedType: StringType, defaultValue:null]),
		manufacturer:([expectedType: StringType, defaultValue:null]),
		comment:([expectedType: StringType, defaultValue:null]),
		code:([expectedType: StringType, defaultValue:null])
	]

	def getExcelImportService() {
		ExcelImportService.getService()
	}

	ProductExcelImporter(String fileName) {
		super(fileName)
		productService = grailsApplication.getMainContext().getBean("productService")
	}


	List<Map> getData() {
		return excelImportService.columns(workbook, columnMap, null, propertyMap)
	}


	void validateData(ImportDataCommand command) {
		productService.validateData(command)
	}
	

	/**
	 * Import data from given inventoryMapList into database.
	 *
	 * @param location
	 * @param inventoryMapList
	 * @param errors
	 */
	void importData(ImportDataCommand command) {
		productService.importData(command)

	}



}