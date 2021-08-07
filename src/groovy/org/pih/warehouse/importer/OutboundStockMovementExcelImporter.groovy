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

class OutboundStockMovementExcelImporter extends AbstractExcelImporter {
    static Map columnMap = [
        sheet   : 'Sheet1',
            startRow : 1,
            columnMap : [
                    'A': 'Source',
                    'B': 'Dest Venue Code',
                    'C': 'Item Description',
                    'D': 'SKU Code',
                    'E': 'Requested Quantity',
                    'F': 'Pallet Quantity',
                    'G': 'Pallet Spaces',
                    'H': 'Delivery Date',
                    'I': 'Load Code',
                    'J': 'Special Instructions'
            ]
    ]

    static Map propertyMap = [
            "Source"    : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            "Dest Venue Code"   : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            "Item Description"    : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            "SKU Code"       : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            "Requested Quantity": ([expectedType: ExcelImportUtils.PROPERTY_TYPE_INT, defaultValue: null]),
            "Pallet Quantity": ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            "Pallet Spaces": ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            "Delivery Date": ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            "Load Code": ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            "Special Instructions": ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null])
    ]

    OutboundStockMovementExcelImporter(String fileName) {
        super(fileName)
    }

    def getDataService() {
        return ApplicationHolder.getApplication().getMainContext().getBean("outboundStockMovementDataService")
    }

    /**
     * Validate Outbound Stock Movement constraints
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
