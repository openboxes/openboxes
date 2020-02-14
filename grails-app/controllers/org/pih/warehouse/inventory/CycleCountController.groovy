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

class CycleCountController {

    def dataService
    def reportService
    def inventoryService

    def exportAsCsv = {
        Location location = Location.load(session.warehouse.id)
        List binLocations = inventoryService.getQuantityByBinLocation(location)
        log.info "Returned ${binLocations.size()} bin locations for location ${location}"

        Map additionalColumns = grailsApplication.config.openboxes.cycleCount.additionalColumns

        List rows = binLocations.collect { row ->
            def latestInventoryDate = row?.product?.latestInventoryDate(location.id) ?: row?.product.earliestReceivingDate(location.id)
            Map dataRow = [
                    "Product code"       : StringEscapeUtils.escapeCsv(row?.product?.productCode),
                    "Product name"       : row?.product.name ?: "",
                    "Generic product"    : row?.genericProduct?.name ?: "",
                    "Category"           : StringEscapeUtils.escapeCsv(row?.category?.name ?: ""),
                    "Formularies"        : row?.product.productCatalogs.join(", ") ?: "",
                    "Lot number"         : StringEscapeUtils.escapeCsv(row?.inventoryItem.lotNumber ?: ""),
                    "Expiration date"    : row?.inventoryItem.expirationDate ? row?.inventoryItem.expirationDate.format("dd-MMM-yyyy") : "",
                    "ABC classification" : StringEscapeUtils.escapeCsv(row?.product.getAbcClassification(location.id) ?: ""),
                    "Bin location"       : StringEscapeUtils.escapeCsv(row?.binLocation?.name ?: ""),
                    "Bin location old"   : StringEscapeUtils.escapeCsv(row?.product?.getBinLocation(location.id) ?: ""),
                    "Status"             : g.message(code: "binLocationSummary.${row?.status}.label"),
                    "Last inventory date": latestInventoryDate ? latestInventoryDate.format("dd-MMM-yyyy") : "",
                    "Quantity on Hand"   : row?.quantity ?: 0,
            ]

            // Iterate over additional columns
            additionalColumns.each { columnKey, Closure columnExpression ->
                String columnValue = columnExpression ? columnExpression.call(row) : ""
                dataRow << ["${StringEscapeUtils.escapeCsv(columnKey)}": StringEscapeUtils.escapeCsv(columnValue)]
            }

            return dataRow

        }

        String csv = dataService.generateCsv(rows)
        response.setHeader("Content-disposition", "attachment; filename=\"CycleCountReport-${location.name}-${new Date().format("dd MMM yyyy hhmmss")}.csv\"")
        render(contentType: "text/csv", text: csv.toString(), encoding: "UTF-8")
        return
    }
}
