/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.report

import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Product

class InventoryReportEntryCommand {
    // Entries are index by product or by inventory item
    Product product
    List<InventoryItem> inventoryItems = []
    Map<InventoryItem, InventoryReportEntryCommand> entries = [:]

    // Entries are index by product or by inventory item
    InventoryItem inventoryItem
    List<ProductReportEntryCommand> transactionEntries = []

    // Running counts of each aggregate
    Integer quantityInitial = 0
    Integer quantityRunning = 0
    Integer quantityFinal = 0

    Integer quantityTransferredIn = 0
    Integer quantityFound = 0
    Integer quantityTotalIn = 0

    Integer quantityTransferredOut = 0
    Integer quantityConsumed = 0
    Integer quantityDamaged = 0
    Integer quantityExpired = 0
    Integer quantityLost = 0
    Integer quantityTotalOut = 0

    Integer quantityAdjusted = 0

    Map<Location, Integer> quantityTransferredInByLocation = [:]
    Map<Location, Integer> quantityTransferredOutByLocation = [:]

    static constraints = {
    }


    Integer getQuantityTotalAdjusted() {
        return quantityFound - quantityLost
    }

    Integer getQuantityEnding() {
        return quantityTotalIn - quantityTotalOut + getQuantityTotalAdjusted()
    }


    InventoryReportEntryCommand getTotals() {
        def totals = new InventoryReportEntryCommand()
        entries.values().each {
            totals.quantityAdjusted += it.quantityAdjusted
            totals.quantityTransferredIn += it.quantityTransferredIn
            totals.quantityTransferredOut += it.quantityTransferredOut
            totals.quantityConsumed += it.quantityConsumed
            totals.quantityDamaged += it.quantityDamaged
            totals.quantityExpired += it.quantityExpired
            totals.quantityFinal += it.quantityFinal
            totals.quantityFound += it.quantityFound
            totals.quantityInitial += it.quantityInitial
            totals.quantityLost += it.quantityLost
            totals.quantityRunning += it.quantityRunning
            totals.quantityTotalIn += it.quantityTotalIn
            totals.quantityTotalOut += it.quantityTotalOut

            it?.quantityTransferredInByLocation?.each { key, value ->
                if (!totals.quantityTransferredInByLocation[key]) {
                    totals.quantityTransferredInByLocation[key] = 0
                }
                totals.quantityTransferredInByLocation[key] += value
            }
            it?.quantityTransferredOutByLocation?.each { key, value ->
                if (!totals.quantityTransferredOutByLocation[key]) {
                    totals.quantityTransferredOutByLocation[key] = 0
                }
                totals.quantityTransferredOutByLocation[key] += value
            }

        }
        return totals
    }


}