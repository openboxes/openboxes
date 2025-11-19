package org.pih.warehouse.picking

import grails.gorm.transactions.Transactional
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.api.PickTaskStatus
import org.pih.warehouse.api.picking.SearchPickTaskCommand
import org.pih.warehouse.core.DeliveryTypeCode
import org.pih.warehouse.core.Person
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionStatus

@Transactional
class PickTaskService {

    @Transactional(readOnly = true)
    List<PickTask> search(SearchPickTaskCommand command, Map params = [:]) {
        log.info "Searching pick tasks for facility=${command.facility?.name}, deliveryTypeCode=${command.deliveryTypeCode}, ordersCount=${command.ordersCount}"

        Integer max = params.int('max') ?: 100
        Integer offset = params.int('offset') ?: 0

        List<String> requisitionIds = findRequisitionIdsForPicking(command)

        List<PickTask> tasks = PickTask.createCriteria().list(max: max, offset: offset) {
            eq("facility", command.facility)

            if (!requisitionIds.isEmpty()) {
                'in'("requisition.id", requisitionIds)
            }

            if (command.deliveryTypeCode) {
                eq("deliveryTypeCode", command.deliveryTypeCode)
            }

            if (command.assigneeId) {
                eq("assignee.id", command.assigneeId)
            }

            if (command.status) {
                eq("status", command.status)
            }

            if (command.priority) {
                eq("priority", command.priority)
            }

            createAlias("location", "l")
            order("l.name", "asc")
        }

        return tasks
    }

    @Transactional(readOnly = true)
    PickTask get(String id) {
        if (!id) {
            return null
        }

        return PickTask.get(id)
    }

    PickTask patch(String id, Map data = [:]) {
        PickTask task = get(id)

        if (!task) {
            throw new ObjectNotFoundException(id, "Pick task ${id} was not found")
        }

        switch (data?.action) {
            case 'start':
                start(task, data.assigneeId as String)
                break

            default:
                throw new UnsupportedOperationException("Unsupported action: ${data?.action}")
        }

        if (task) {
            task.discard()
        }

        return task
    }

    void start(PickTask task, String assigneeId) {
        task.dateStarted = new Date()
        task.assignee = Person.get(assigneeId)
        executeStateTransition(task, PickTaskStatus.PICKING)
        save(task)
    }

    void save(PickTask task) {
        PicklistItem existingPickItem = PicklistItem.get(task.id)
        existingPickItem.status = task.status.name()
        Requisition requisition = existingPickItem?.requisitionItem?.requisition
        requisition.approvedBy = task.assignee
        requisition.dateApproved = task.dateStarted

        if (task.status == PickTaskStatus.PICKED) {
            def allOrderTasks = requisition.picklist.picklistItems
            boolean allTasksPicked = allOrderTasks.every {
                it.status == PickTaskStatus.PICKED.name()
            }

            if (allTasksPicked) {
                requisition.status = RequisitionStatus.PICKED
            }
        }

        requisition.save(cascade: true, failOnError: true)
        task.discard()
    }

    void executeStateTransition(PickTask task, PickTaskStatus to, boolean validate = true) {
        PickTaskStatus from = task.status ?: PickTaskStatus.PENDING
        if (validate && !PickTaskStatus.validateTransition(from, to)) {
            throw new IllegalStateException("Invalid transition ${from} -> ${to}")
        }
        task.status = to
    }

    private List<String> findRequisitionIdsForPicking(SearchPickTaskCommand command) {
        Integer ordersCount = command.ordersCount
        DeliveryTypeCode deliveryTypeCode = command.deliveryTypeCode
        if (ordersCount) {
            boolean searchForAllOrders = deliveryTypeCode == null

            def criteria = Requisition.createCriteria()
            List<Requisition> allCandidates = criteria.list() {
                eq("origin", command.facility)
                eq("status", RequisitionStatus.PICKING)

                if (!searchForAllOrders) {
                    eq("deliveryTypeCode", deliveryTypeCode)
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

            return selectedRequisitions.collect { it.id }
        }

        return []
    }
}
