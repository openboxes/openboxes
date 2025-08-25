package org.pih.warehouse.putaway

import grails.gorm.transactions.Transactional
import org.pih.warehouse.api.PutawayTaskStatus
import org.pih.warehouse.api.StatusCategory
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product

@Transactional
class PutawayTaskService {

    PutawayService putawayService

    List search(Location facility, Product product, Map params) {
        log.info "search putaway tasks " + params

        Integer max = Math.min((params.int('max') ?: 50), 500) as Integer
        Integer off = params.int('offset') ?: 0 as Integer
        String sort = params.sort ?: 'dateCreated'
        String order = (params.order ?: 'desc').toLowerCase() in ['asc', 'desc'] ? params.order : 'desc' as Integer
        String q = params.q as String

        // Get user-provided statuses
        List<PutawayTaskStatus> statuses = params.list("status")

        // Resolve the status category to a set of statuses and added to user-provided
        StatusCategory statusCategory = params.statusCategory as StatusCategory
        List<PutawayTaskStatus> statusesByStatusCategory = PutawayTaskStatus.toSet(statusCategory)
        statuses += statusesByStatusCategory

        List<PutawayTask> tasks = PutawayTask.where {
            if (statuses) {
                status in statuses
            }
            if (product) {
                product == product
            }
            if (facility) {
                facility == facility
            }

        }.list(max: max, offset: off, sort: sort, order: order)

        return tasks
    }

    PutawayTask get(String id) {
        if (!id) return null
        PutawayTask.get(id)
    }

    PutawayTask create(Map params) {
        throw new UnsupportedOperationException("Unable to create a putaway task at this time")
    }

    PutawayTask update(String id, Map body) {
        throw new UnsupportedOperationException("Unable to update a putaway task at this time")
    }

    boolean delete(String id) {
        throw new UnsupportedOperationException("Unable to delete a putaway task at this time")
    }

    /**
     * Action router for state transitions and side-effects (transaction).
     * Supported actions include: start, scanToContainer, complete, cancel, rollback
     * FIXME - this will eventually be replaced with a Spring Statemachine implementation.
     */
    PutawayTask patch(String id, String action, Map data = [:]) {
        PutawayTask task = get(id)
        if (!task) return null

        switch (action) {
            case 'start':
                executeStatusChange(task, PutawayTaskStatus.IN_PROGRESS)
                task.dateStarted = new Date();
                putawayService.savePutaway(task.toPutaway())
                break

            case 'complete':
                if (!task.destination) throw new IllegalStateException("Destination is required")
                executeStatusChange(task, PutawayTaskStatus.COMPLETED)
                task.discard()
                putawayService.completePutaway(task.toPutaway())
                break

            case 'cancel':
                executeStatusChange(task, PutawayTaskStatus.CANCELED)
                putawayService.savePutaway(task.toPutaway())
                break

            default:
                throw new UnsupportedOperationException("Unsupported action: ${action}")
        }

        log.info "task.toPutaway " + task.toPutaway().toJson()

        // FIXME The putaway task is a view-backed domain, but Grails doesn't know that so any changes to the
        //  task trigger a persistence event. I need to figure out how to make it a read-only domain class.
        task.discard()

        // Eventually, we want to save any changes made to the putaway task as a putaway.
        // FIXME  For now the PutawayTask will be a wrapper around Putaway. However, we may want to use just the
        //  Putaway object instead of creating a new PutawayTask. I had forgotten about the Putaway abstraction.
        return task
    }


    private void executeStatusChange(PutawayTask task, PutawayTaskStatus to, boolean validate = true) {
        PutawayTaskStatus from = task.status ?: PutawayTaskStatus.PENDING
        if (validate && !PutawayTaskStatus.validateTransition(from, to)) {
            throw new IllegalStateException("Invalid transition ${from} -> ${to}")
        }
        task.status = to
    }

}