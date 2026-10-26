/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.allocation

import grails.gorm.transactions.Transactional
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem

/**
 * Single place that decides which inbound quantity covers which outbound demand line.
 */
@Transactional
class BackorderMatchingService {

    List<BackorderMatch> match(Requisition backorder, Collection<ShipmentItem> inboundItems) {
        List<BackorderMatch> matches = []
        Map<String, Integer> quantityRemainingByDemand = [:]
        demandItems(backorder).each { RequisitionItem demand ->
            quantityRemainingByDemand[demand.id] = remainingDemand(demand)
        }

        for (ShipmentItem inboundItem : inboundItems) {
            int quantityToMatch = inboundItem.quantity ?: 0
            for (RequisitionItem demand : candidatesFor(backorder, inboundItem.product, quantityRemainingByDemand)) {
                if (quantityToMatch <= 0) {
                    break
                }
                int quantityMatched = Math.min(quantityToMatch, quantityRemainingByDemand[demand.id])
                matches << new BackorderMatch(
                        inboundItem: inboundItem,
                        demand: demand,
                        quantityMatched: quantityMatched)
                quantityRemainingByDemand[demand.id] -= quantityMatched
                quantityToMatch -= quantityMatched
            }
        }
        return matches
    }

    /**
     * Resolves how much of the quantity on hand should be cross-docked, summed across every line of the
     * order for the product. Returns null when there is no demand left to cover, in which case the quantity
     * belongs in storage.
     */
    BackorderMatch resolveCrossDockMatch(String backorderReference, RequisitionItem backorderItem,
                                        Product product, Integer quantityAvailable) {
        int quantity = quantityAvailable ?: 0
        if (quantity <= 0) {
            return null
        }

        List<RequisitionItem> demands
        if (backorderItem) {
            demands = [backorderItem]
        } else {
            Requisition backorder = backorderReference ? Requisition.findByRequestNumber(backorderReference) : null
            if (!backorder) {
                return null
            }
            demands = demandItems(backorder).findAll { it.product == product }
        }

        List<RequisitionItem> candidates = demands.findAll { remainingDemand(it) > 0 }
        if (!candidates) {
            return null
        }

        int quantityMatched = Math.min(quantity, candidates.sum { remainingDemand(it) } as int)
        return new BackorderMatch(demand: candidates.first(), quantityMatched: quantityMatched)
    }

    /**
     * Quantity a cross-dock delivery may still cover on a demand line. Only a backordered line is
     * cross-docked, and it is measured against the whole ordered quantity less everything allocated.
     */
    Integer remainingDemand(RequisitionItem demand) {
        if (!demand.isBackordered()) {
            return 0
        }
        Integer quantityOrdered = (demand.quantity ?: 0) - (demand.quantityCanceled ?: 0)
        return Math.max(0, quantityOrdered - (demand.calculateQuantityAllocated() ?: 0))
    }

    /**
     * Inbound items covering the given backorder through either link: the soft reference used before
     * the demand line is resolved, or the direct one once it is.
     */
    Collection<ShipmentItem> findInboundItemsForRequisition(Shipment shipment, Requisition backorder) {
        return shipment.shipmentItems.findAll {
            it.backorderReference == backorder.requestNumber ||
                    it.backorderItem?.requisition?.id == backorder.id
        }
    }

    /**
     * Demand lines for the product that still need cover
     */
    private List<RequisitionItem> candidatesFor(Requisition backorder, Product product,
                                                Map<String, Integer> quantityRemainingByDemand) {
        return demandItems(backorder)
                .findAll { it.product == product && quantityRemainingByDemand[it.id] > 0 }
                .sort { quantityRemainingByDemand[it.id] }
    }

    /**
     * Items that carry demand in their own right. A revised or substituted line keeps its original row
     * on the requisition alongside the row that replaced it, and both report the same quantity, so
     * matching against the original as well would cover the same demand twice and would record the
     * cross-dock allocation against the row that is never picked. Walk down from the original lines
     * and take only the item at the end of each chain.
     */
    private static List<RequisitionItem> demandItems(Requisition backorder) {
        return backorder.initialRequisitionItems.collectMany { RequisitionItem item ->
            item.getEffectiveItems()
        }
    }
}
