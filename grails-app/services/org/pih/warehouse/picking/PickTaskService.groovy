package org.pih.warehouse.picking

import grails.gorm.transactions.Transactional
import org.pih.warehouse.api.picking.SearchPickTaskCommand
import org.pih.warehouse.core.DeliveryTypeCode
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionStatus

@Transactional
class PickTaskService {

    @Transactional(readOnly = true)
    List<PickTask> search(SearchPickTaskCommand command, Map params = [:]) {
        log.info "Searching pick tasks for facility=${command.facility?.name}, pickType=${command.pickType}, ordersNumber=${command.ordersCount}"

        Integer ordersCount = command.ordersCount ?: 1
        String pickType = command.pickType
        boolean searchForAllOrders = !pickType || pickType == "ALL"
        DeliveryTypeCode specificDeliveryType = null

        if (!searchForAllOrders) {
            try {
                specificDeliveryType = DeliveryTypeCode.valueOf(pickType)
            } catch (IllegalArgumentException e) {
                log.warn "Invalid pick type passed: ${pickType}. All orders will be searched."
                searchForAllOrders = true
            }
        }

        def criteria = Requisition.createCriteria()
        List<Requisition> allCandidates = criteria.list() {
            eq("origin", command.facility)
            eq("status", RequisitionStatus.PICKING)

            if (!searchForAllOrders && specificDeliveryType) {
                eq("deliveryTypeCode", specificDeliveryType)
                order("dateCreated", "asc")
            }
        }

        if (!allCandidates) {
            return []
        }

        List<Requisition> selectedRequisitions

        if (searchForAllOrders) {
            def sortedCandidates = allCandidates.sort { a, b ->
                Integer p1 = a.deliveryTypeCode?.priority ?: 99
                Integer p2 = b.deliveryTypeCode?.priority ?: 99

                return p1 <=> p2 ?: a.dateCreated <=> b.dateCreated
            }
            selectedRequisitions = sortedCandidates.take(ordersCount)
        } else {
            selectedRequisitions = allCandidates.take(ordersCount)
        }

        List<String> requisitionIds = selectedRequisitions.collect { it.id }

        if (!requisitionIds) return []

        List<PickTask> tasks = PickTask.createCriteria().list() {
            'in'("requisition.id", requisitionIds)
            createAlias("location", "l")
            order("l.name", "asc")
        }

        return tasks
    }
}
