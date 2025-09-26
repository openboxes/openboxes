package org.pih.warehouse.inboundSortation

import grails.gorm.transactions.Transactional
import org.hibernate.FetchMode
import org.pih.warehouse.api.PutawayTaskStatus
import org.pih.warehouse.api.StatusCategory
import org.pih.warehouse.core.DeliveryTypeCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product
import org.pih.warehouse.putaway.PutawayTask
import org.pih.warehouse.putaway.PutawayTaskService
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionStatus

@Transactional
class DemandService {

    PutawayTaskService putawayTaskService

    def calculateUnmetDemand(Location facility, Product product) {
        def allocations = getAllocations(facility, product)
        def demands = getDemands(facility, product)

        // Get all keys
        def allKeys = (allocations.keySet() + demands.keySet()) as Set

        def unmetDemand = allKeys
                // Tranform to map of unmet quantity demand based on delivery type code
                .collectEntries { deliveryTypeCode ->
                    Integer quantityDemanded = demands.get(deliveryTypeCode) ?: 0
                    Integer quantityAllocated = allocations.get(deliveryTypeCode) ?: 0
                    Integer quantityBackordered = Math.max((quantityDemanded - quantityAllocated), 0)
                    [deliveryTypeCode, quantityBackordered]
                }
                // Sort by delivery type code priority (ascending)
                .sort { a, b ->
                    a.key.priority <=> b.key.priority
                }

        return unmetDemand
    }

    def getAllocations(Location facility, Product product) {

        // Get open putaway tasks
        // FIXME Eventually this should be based on actual allocations
        //List<PutawayTask> putawayTasks = putawayTaskService.search(facility, product, null, StatusCategory.OPEN, [:])

        List<PutawayTask> putawayTasks = PutawayTask.createCriteria().list {
            eq("facility", facility)
            eq("product", product)
            'in'("status", PutawayTaskStatus.toSet(StatusCategory.OPEN))
        }

        def allocations = putawayTasks
                // Group putaway tasks into a map and sum quantity based on delivery type
                .groupBy { PutawayTask task -> deliveryTypeCode : task.deliveryTypeCode }

                // Filter out groups where delivery type code is null
                .findAll { deliveryTypeCode, tasks ->
                    deliveryTypeCode
                }

                // Tranform grouped entries into delivery type and quantity
                .collectEntries {deliveryTypeCode, tasks ->
                    [deliveryTypeCode, tasks.sum { it.quantity }]
                }

                // Sort the results by the priority assigned to each delivery type code
                .sort { a, b ->
                    a.key.priority <=> b.key.priority
                }

        return allocations
    }

    Map<DeliveryTypeCode, List<OutboundDemand>> getDemands(Location facility, Product product) {

        // FIXME This might need to be a specific status because we might not want to allocate / cross dock
        //  an inbound item to an outbound demand too early. However, this is fine for now.
        def statuses = RequisitionStatus.values().findAll {
            it.sortOrder < RequisitionStatus.PICKING.sortOrder
        }

        List<RequisitionItem> pendingRequestLines = RequisitionItem.createCriteria().list {
            requisition {
                'in'('status', statuses)
            }
            fetchMode 'product', FetchMode.JOIN
            fetchMode 'requisition', FetchMode.JOIN
            requisition {
                eq("origin", facility)
            }
            eq("product", product)
        } as List<RequisitionItem>

        log.info "pendingRequestLines " + pendingRequestLines

        if (!pendingRequestLines) {
            return [:]
        }

        def outboundDemandLines = pendingRequestLines
                // Group requisition items by delivery type code
                .groupBy { RequisitionItem item ->
                    // FIXME It would be better if we didn't have to specify the default value here
                    item.requisition.deliveryTypeCode?:DeliveryTypeCode.DEFAULT
                }

                // Return only records with a non-null delivery type code
                .findAll { deliveryTypeCode, lines ->
                    deliveryTypeCode
                }

                // Transform to a map of deliveryTypeCode and total quantity
                .collectEntries { deliveryTypeCode, lines ->
                    [deliveryTypeCode, lines.sum { it.quantity ?: 0 }]
                }

                // Sort by deliveryTypeCode priority
                .sort { a, b ->
                    a.key.priority <=> b.key.priority
                }

        return outboundDemandLines
    }
}