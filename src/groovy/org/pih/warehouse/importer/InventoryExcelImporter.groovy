package org.pih.warehouse.importer

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.grails.commons.ApplicationHolder;
import org.grails.plugins.excelimport.ExcelImportUtils;
import org.pih.warehouse.core.Constants;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.importer.ImportDataCommand;
import org.pih.warehouse.importer.AbstractExcelImporter;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.inventory.Transaction;
import org.pih.warehouse.inventory.TransactionEntry;
import org.pih.warehouse.inventory.TransactionType;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;
import org.springframework.validation.Errors

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
			'H':'lotNumber',
			'I':'expirationDate',
			'J':'quantity'
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
		lotNumber:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
		expirationDate:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
		quantity:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null])
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