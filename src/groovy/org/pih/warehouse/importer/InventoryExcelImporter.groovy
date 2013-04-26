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

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.grails.plugins.excelimport.ExcelImportUtils

class InventoryExcelImporter extends AbstractExcelImporter {

	def inventoryService

	static Map cellMap = [
		sheet:'Sheet1', startRow: 1, cellMap: [ ]]

	static Map columnMap = [
		sheet:'Sheet1',
		startRow: 1,
		columnMap: [
			'A':'category',
			'B':'productDescription',
			'C':'unitOfMeasure',
			'D':'manufacturer',
			'E':'manufacturerCode',
			'F':'upc',
			'G':'ndc',
			'H':'coldChain',
			'I':'lotNumber',
			'J':'expirationDate',
			'K':'quantity'
			
		]
	]

	static Map propertyMap = [
		parentCategory:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
		category:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
		productDescription: ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
		unitOfMeasure: ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
		manufacturer:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
		manufacturerCode:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
		upc:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
		ndc:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
		coldChain:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
		lotNumber:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
		expirationDate:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
		quantity:([expectedType: ExcelImportUtils.PROPERTY_TYPE_INT, defaultValue:null])
		
	]




	public InventoryExcelImporter(String fileName) {
		super(fileName)
		inventoryService = ApplicationHolder.getApplication().getMainContext().getBean("inventoryService")
	}


	List<Map> getData() {
		return ExcelImportUtils.convertColumnMapConfigManyRows(workbook, columnMap, null, propertyMap)
	}

	public void validateData(ImportDataCommand command) { 
		inventoryService.validateData(command)
	}

	public void importData(ImportDataCommand command) { 
		inventoryService.importData(command)
	}





}