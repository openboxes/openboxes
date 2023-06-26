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

import grails.util.Holders
import org.grails.plugins.excelimport.AbstractExcelImporter
import org.grails.plugins.excelimport.ExpectedPropertyType

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
            "origin"    : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            "destination"   : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            "productCode"       : ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            "quantity": ([expectedType: ExpectedPropertyType.IntType, defaultValue: null]),
            "requestedDeliveryDate": ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            "requestNumber": ([expectedType: ExpectedPropertyType.StringType, defaultValue: null]),
            "description": ([expectedType: ExpectedPropertyType.StringType, defaultValue: null])
    ]

    OutboundStockMovementExcelImporter(String fileName) {
        super(fileName)
    }

    def getDataService() {
        return Holders.grailsApplication.mainContext.getBean("outboundStockMovementDataService")
    }

    /**
     * Validate Outbound Stock Movement constraints
     * @param command
     */
    void validateData(ImportDataCommand command) {
        dataService.validateData(command)
    }

    List<Map> getData() {
        Holders.grailsApplication.mainContext.getBean("excelImportService")
                .convertColumnMapConfigManyRows(workbook, columnMap, null, null, propertyMap)
    }

    /**
     * Import data from given map into database.
     * @param command
     */
    void importData(ImportDataCommand command) {
        dataService.importData(command)
    }
}
