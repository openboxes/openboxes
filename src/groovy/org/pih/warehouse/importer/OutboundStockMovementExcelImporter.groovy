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
                    'A': 'origin', // 'Source',
                    'B': 'destination', // 'Dest Venue Code',
                    'D': 'productCode', // 'SKU Code',
                    'E': 'quantity', // 'Requested Quantity',
                    'H': 'requestedDeliveryDate', // 'Delivery Date',
                    'I': 'requestNumber', // 'Load Code',
                    'J': 'description' // 'Special Instructions'
            ]
    ]

    static Map propertyMap = [
            "origin"    : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            "destination"   : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            "productCode"       : ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            "quantity": ([expectedType: ExcelImportUtils.PROPERTY_TYPE_INT, defaultValue: null]),
            "requestedDeliveryDate": ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            "requestNumber": ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null]),
            "description": ([expectedType: ExcelImportUtils.PROPERTY_TYPE_STRING, defaultValue: null])
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
