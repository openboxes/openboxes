/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.inventory

import org.apache.commons.lang.StringEscapeUtils
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Location

class CycleCountController {

    def dataService
    def reportService
    def inventoryService

    def exportAsCsv = {
        Location location = Location.load(session.warehouse.id)
        List binLocations = inventoryService.getQuantityByBinLocation(location)
        log.info "Returned ${binLocations.size()} bin locations for location ${location}"

        List rows = binLocations.collect {
            def latestInventoryDate = it.product.latestInventoryDate(location.id)
            [
                    "Product code"                                                                                         : StringEscapeUtils.escapeCsv(it.product.productCode),
                    "Product name"                                                                                         : it.product.name ?: "",
                    "Generic product"                                                                                      : it.genericProduct?.name ?: "",
                    "Category"                                                                                             : StringEscapeUtils.escapeCsv(it.category?.name ?: ""),
                    "Formularies"                                                                                          : it.product.productCatalogs.join(", ") ?: "",
                    "Lot number"                                                                                           : StringEscapeUtils.escapeCsv(it.inventoryItem.lotNumber ?: ""),
                    "Expiration date"                                                                                      : it.inventoryItem.expirationDate ? it.inventoryItem.expirationDate.format("dd-MMM-yyyy") : "",
                    "ABC classification"                                                                                   : StringEscapeUtils.escapeCsv(it.product.getAbcClassification(location.id) ?: ""),
                    "Bin location"                                                                                         : StringEscapeUtils.escapeCsv(it?.binLocation?.name ?: ""),
                    "Bin location old"                                                                                     : StringEscapeUtils.escapeCsv(it.product.getBinLocation(location.id) ?: ""),
                    "Status"                                                                                               : g.message(code: "binLocationSummary.${it.status}.label"),
                    "Last inventory date"                                                                                  : latestInventoryDate ? latestInventoryDate.format("dd-MMM-yyyy") : "",
                    "Quantity on Hand"                                                                                     : it.quantity ?: 0,
                    "Physical lot/serial number"                                                                           : "",
                    "Physical bin location"                                                                                : "",
                    "Physical expiration date"                                                                             : "",
                    "Physical quantity"                                                                                    : "",
                    "Was bin location updated in OpenBoxes?"                                                               : "",
                    "${StringEscapeUtils.escapeCsv("Was quantity, lot/serial, and expiration date updated in OpenBoxes?")}": "",
                    "Comment"                                                                                              : "",
            ]
        }

        String csv = dataService.generateCsv(rows)
        response.setHeader("Content-disposition", "attachment; filename=\"CycleCountReport-${location.name}-${new Date().format("dd MMM yyyy hhmmss")}.csv\"")
        render(contentType: "text/csv", text: csv.toString(), encoding: "UTF-8")
        return
    }
}
