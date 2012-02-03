package org.pih.warehouse.importer

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.product.ProductService;
import org.springframework.validation.Errors;

import org.codehaus.groovy.grails.commons.ApplicationHolder;
import org.grails.plugins.excelimport.AbstractExcelImporter;
import org.grails.plugins.excelimport.ExcelImportUtils;

class ProductExcelImporter extends AbstractExcelImporter {

	def productService 

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
		idaCode:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
		openBoxesId:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
		productDescription: ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
		french: ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
		search1:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
		search2:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
		packaging:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
		unit:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
		manufacturer:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
		comment:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
		code:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null])
	]



	public ProductExcelImporter(String fileName) {
		super(fileName)
		productService = ApplicationHolder.getApplication().getMainContext().getBean("productService")
	}


	List<Map> getData() {
		return ExcelImportUtils.convertColumnMapConfigManyRows(workbook, columnMap, null, propertyMap)
	}


	public void validateData(ImportDataCommand command) {
		productService.validateData(command)
	}
	

	/**
	 * Import data from given inventoryMapList into database.
	 *
	 * @param location
	 * @param inventoryMapList
	 * @param errors
	 */
	public void importData(ImportDataCommand command) {
		productService.importData(command)

	}



}