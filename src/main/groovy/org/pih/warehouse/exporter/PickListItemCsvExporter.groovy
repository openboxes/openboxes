/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.exporter

import grails.util.Holders
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.pih.warehouse.importer.CSVUtils

class PickListItemCsvExporter {

    List<Map> data
    ApplicationTagLib applicationTagLib

    Map documentProperties = [
            columns: [
                    'id',
                    'code',
                    'name',
                    'lot',
                    'expiration',
                    'binLocation',
                    'quantity',
            ],
            headerLabels: [
                    'id'            : 'default.id.label',
                    'code'          : 'product.productCode.label',
                    'name'          : 'product.name.label',
                    'lot'           : 'inventoryItem.lotNumber.label',
                    'expiration'    : 'inventoryItem.expirationDate.label',
                    'binLocation'   : 'inventoryItem.binLocation.label',
                    'quantity'      : 'default.quantity.label',
            ]
    ]

    PickListItemCsvExporter(List<Map> data) {
        this.data = data
        this.applicationTagLib = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib.class)
    }

    String exportData() {
        Map<String, String> translatedHeadings = this.documentProperties.headerLabels.collectEntries { key, it ->
            [key, applicationTagLib.message(code: it, default: key)]
        }

        this.documentProperties.headerLabels = translatedHeadings

        try {
            return CSVUtils.generateCsv(this.data, this.documentProperties)
        } catch (IOException e) {
            log.error("IO exception while generating csv file")
        }
    }

}
