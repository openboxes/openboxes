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
import org.grails.plugins.excelimport.AbstractExcelImporter
import org.grails.plugins.excelimport.ExcelImportUtils
import org.pih.warehouse.data.ProductSupplierDataService

class ProductSupplierExcelImporter extends AbstractExcelImporter {

	ProductSupplierDataService productSupplierDataService

	static Map columnMap = [
		sheet:'Sheet1',
		startRow: 1,
		columnMap: [
                'A':'code',
                'B':'product.productCode',
                'C':'name',
                'D':'description',
                'E':'supplier.id',
                'F':'supplier.name',
                'G':'supplierCode',
                'H':'supplierName',
				'I':'manufacturer.id',
				'J':'manufacturer.name',
				'K':'manufacturerCode',
				'L':'manufacturerName',
				'M':'unitPrice',
				'N':'standardLeadTimeDays',
				'O':'preferenceTypeCode',
				'P':'ratingTypeCode',
				'Q':'comments',
		]
	]

	static Map propertyMap = [
            code:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
			productCode:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
            name:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
            description:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
			supplierId:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
			supplierName:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
			supplierCode:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
			supplierProductName:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
			manufacturerId:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
			manufacturerName:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
			manufacturerCode:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
			manufacturerProductName:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
			unitPrice:([expectedType: ExcelImportUtils.PROPERTY_TYPE_INT, defaultValue:null]),
			standardLeadTimeDays:([expectedType: ExcelImportUtils.PROPERTY_TYPE_INT, defaultValue:null]),
			preferenceTypeCode:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
			ratingTypeCode:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null]),
            comments:([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue:null])
	]


	ProductSupplierExcelImporter(String fileName) {
		super(fileName)
	}

	def getDataService() {
		return ApplicationHolder.getApplication().getMainContext().getBean("productSupplierDataService")
	}


	List<Map> getData() {
		return ExcelImportUtils.convertColumnMapConfigManyRows(workbook, columnMap, null, propertyMap)
    }


    void validateData(ImportDataCommand command) {
		dataService.validate(command)
    }

    /**
     * Import data from given inventoryMapList into database.
     *
     * @param location
     * @param inventoryMapList
     * @param errors
     */
    void importData(ImportDataCommand command) {
		dataService.process(command)
    }

}