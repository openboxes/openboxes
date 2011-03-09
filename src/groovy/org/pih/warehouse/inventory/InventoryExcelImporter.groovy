package org.pih.warehouse.inventory;


import org.grails.plugins.excelimport.ExcelImportUtils
import org.grails.plugins.excelimport.*

class InventoryExcelImporter extends AbstractExcelImporter {

	Map cellMap
	Map columnMap
	Map propertyMap
	
	public InventoryExcelImporter(fileName, columnMap, cellMap) {
		super(fileName)
		this.columnMap = columnMap
		this.cellMap = cellMap
	}

	public InventoryExcelImporter(fileName, columnMap, cellMap, propertyMap) {
		super(fileName)
		this.columnMap = columnMap
		this.cellMap = cellMap
		this.propertyMap = propertyMap
	}

	
	List<Map> getInventoryItems() {
		return ExcelImportUtils.convertColumnMapConfigManyRows(workbook, columnMap, null, propertyMap)
	}
	/*
	Map getOneMoreInventoryItemParams() {
		return ExcelImportUtils.convertFromCellMapToMapWithValues(workbook, cellMap, propertyMap)
	}
	*/
}