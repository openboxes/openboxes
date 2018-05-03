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
import org.pih.warehouse.product.Product

class CycleCountController {

    def dataService
    def reportService
    def inventoryService

    def exportAsCsv = {
        Location location = Location.load(session.warehouse.id)
        List binLocations = inventoryService.getQuantityByBinLocation(location)
        log.info "Returned ${binLocations.size()} bin locations for location ${location}"
        List rows = binLocations.collect {
            [
                    "Status"             : g.message(code: "binLocationSummary.${it.status}.label"),
                    "Last Inventory Date": it.product.latestInventoryDate(location.id) ?: "",
                    "Product Code"       : StringEscapeUtils.escapeCsv(it.product.productCode),
                    "Product Name"       : it.product.name ?: "",
                    "Generic Product"    : it.genericProduct?.name ?: "",
                    "Category"           : StringEscapeUtils.escapeCsv(it.category?.name ?: ""),
                    "Lot Number"         : StringEscapeUtils.escapeCsv(it.inventoryItem.lotNumber ?: ""),
                    "Expiration Date"    : it.inventoryItem.expirationDate ?: "",
                    "Bin Location"       : StringEscapeUtils.escapeCsv(it?.binLocation?.name ?: ""),
                    "Bin Location Old"   : StringEscapeUtils.escapeCsv(it.product.getBinLocation(location.id) ?: ""),
                    "ABC Classification" : StringEscapeUtils.escapeCsv(it.product.getAbcClassification(location.id) ?: ""),
                    "Quantity on Hand"   : it.quantity ?: 0,
                    "Quantity Counted"   : "",
                    "Quantity Variance"  : ""

            ]
        }

        String csv = dataService.generateCsv(rows)
        response.setHeader("Content-disposition", "attachment; filename='CycleCountReport-${location.name}-${new Date().format("dd MMM yyyy hhmmss")}.csv'")
        render(contentType: "text/csv", text: csv.toString(), encoding: "UTF-8")
        return
    }
}
