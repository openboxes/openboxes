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
        List rows = binLocations.collect {
            [
                    status: StringEscapeUtils.escapeCsv(it.status),
                    latestInventoryDate: it.product.latestInventoryDate(location.id)?:"",
                    productCode: StringEscapeUtils.escapeCsv(it.product.productCode),
                    productName: StringEscapeUtils.escapeCsv(it.product.name?:""),
                    genericProduct: StringEscapeUtils.escapeCsv(it.genericProduct?.name?:""),
                    category: StringEscapeUtils.escapeCsv(it.category.name?:""),
                    lotNumber: StringEscapeUtils.escapeCsv(it.inventoryItem.lotNumber?:""),
                    expirationDate: it.inventoryItem.expirationDate?:"",
                    binLocation: StringEscapeUtils.escapeCsv(it?.binLocation?.name?:""),
                    binLocationOld: StringEscapeUtils.escapeCsv(it.product.getBinLocation(location.id)?:""),
                    quantity: it.quantity?:0

            ]

        }

//        Map quantityMap = inventoryService.getQuantityByProductMap(location.inventory)
//        def rows = quantityMap.collect { Product product, Integer quantity ->
//            [
//                    status: product.getStatus(location.id, quantity),
//                    latestInventoryDate: product.latestInventoryDate(location.id)?:"",
//                    productCode: StringEscapeUtils.escapeCsv(product.productCode),
//                    productName: StringEscapeUtils.escapeCsv(product.name),
//                    genericProduct: StringEscapeUtils.escapeCsv(product.genericProduct?.name),
//
//            ]
//        }

        String csv = dataService.generateCsv(rows)
        response.setHeader("Content-disposition", "attachment; filename='CycleCountReport-${location.name}-${new Date().format("dd MMM yyyy hhmmss")}.csv'")
        render(contentType: "text/csv", text: csv.toString(), encoding: "UTF-8")
        return
    }
}
