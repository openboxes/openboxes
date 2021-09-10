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
import org.grails.plugins.excelimport.ExcelImportUtils

class InboundStockMovementExcelImporter extends AbstractExcelImporter {
    static Map columnMap = [
            sheet   : 'Sheet1',
            startRow : 1,
            columnMap : [
                    'A': 'origin', // 'Source',
                    'B': 'destination', // 'Dest Venue Code',
                    'C': 'productName',
                    'D': 'productCode', // 'SKU Code',
                    'E': 'quantity', // 'Requested Quantity',
                    'H': 'deliveryDate', // 'Delivery Date',
                    'I': 'shipmentNumber', // 'Load Code',
                    'J': 'specialInstructions' // 'Special Instructions'
            ]
    ]

    static Map propertyMap = [
            "origin"    : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            "destination"   : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            "productCode"       : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            "quantity": ([expectedType: ExcelImportUtils.PROPERTY_TYPE_INT, defaultValue: null]),
            "deliveryDate": ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            "shipmentNumber": ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            "specialInstructions": ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null])
    ]

    InboundStockMovementExcelImporter(String fileName) {
        super(fileName)
    }

    InboundStockMovementExcelImporter(String fileName, InputStream inputStream) {
        super(fileName, inputStream)
    }

    def getDataService() {
        return ApplicationHolder.getApplication().getMainContext().getBean("inboundStockMovementDataService")
    }

    /**
     * Validate Inbound Stock Movement constraints
     * @param command
     */
    void validateData(ImportDataCommand command) {
        dataService.validateData(command)
    }

    List<Map> getData() {
        return ExcelImportUtils.convertColumnMapConfigManyRows(workbook, columnMap, null, propertyMap)
    }

    /**
     * Import data from given map into database.
     * @param command
     */
    void importData(ImportDataCommand command) {
        dataService.importData(command)
    }
}
