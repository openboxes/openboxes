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
        backorder.requisitionItems.each { RequisitionItem demand ->
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
     * Resolves how much of the quantity on hand should be cross-docked, and against which demand line.
     * Returns null when there is no demand left to cover, in which case the quantity belongs in storage.
     */
    BackorderMatch resolveCrossDockMatch(String backorderReference, RequisitionItem backorderItem,
                                        Product product, Integer quantityAvailable) {
        int quantity = quantityAvailable ?: 0
        if (quantity <= 0) {
            return null
        }

        RequisitionItem demand = backorderItem
        if (!demand) {
            Requisition backorder = backorderReference ? Requisition.findByRequestNumber(backorderReference) : null
            if (!backorder) {
                return null
            }
            demand = backorder.requisitionItems
                    .findAll { it.product == product && remainingDemand(it) > 0 }
                    .sort { remainingDemand(it) }
                    .find()
        }
        if (!demand) {
            return null
        }

        int quantityMatched = Math.min(quantity, remainingDemand(demand))
        if (quantityMatched <= 0) {
            return null
        }
        return new BackorderMatch(demand: demand, quantityMatched: quantityMatched)
    }

    /**
     * Quantity still to be covered on a demand line
     */
    Integer remainingDemand(RequisitionItem demand) {
        if (demand.isBackordered()) {
            return Math.max(0, demand.quantityBackordered ?: 0)
        }
        Integer quantityRequired = demand.calculateQuantityRequired() ?: 0
        Integer quantityAllocated = demand.calculateQuantityAllocated() ?: 0
        return Math.max(0, quantityRequired - quantityAllocated)
    }

    /**
     * Inbound items covering the given backorder through either link: the soft reference used before
     * the demand line is resolved, or the direct one once it is. findInboundItems only sees the soft
     * form, which is what the receipt validation needs but would silently skip a hard linked item.
     */
    Collection<ShipmentItem> findInboundItemsForRequisition(Shipment shipment, Requisition backorder) {
        return shipment.shipmentItems.findAll {
            it.backorderReference == backorder.requestNumber ||
                    it.backorderItem?.requisition?.id == backorder.id
        }
    }

    Collection<ShipmentItem> findInboundItems(Shipment shipment, String requisitionNumber) {
        return shipment.shipmentItems.findAll {
            it.backorderReference == requisitionNumber && !it.backorderItem
        }
    }

    /**
     * Demand lines for the product that still need cover
     */
    private List<RequisitionItem> candidatesFor(Requisition backorder, Product product,
                                                Map<String, Integer> quantityRemainingByDemand) {
        return backorder.requisitionItems
                .findAll { it.product == product && quantityRemainingByDemand[it.id] > 0 }
                .sort { quantityRemainingByDemand[it.id] }
    }
}
