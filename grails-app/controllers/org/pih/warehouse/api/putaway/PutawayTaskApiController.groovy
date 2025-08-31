package org.pih.warehouse.api.putaway

import grails.converters.JSON
import grails.rest.RestfulController
import org.pih.warehouse.api.StatusCategory
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product
import org.pih.warehouse.putaway.PutawayTask
import org.pih.warehouse.putaway.PutawayTaskService
import org.springframework.http.HttpStatus

class PutawayTaskApiController extends RestfulController<PutawayTask> {

    static responseFormats = ['json']
    static allowedMethods = [
            index: 'GET',
            show: 'GET',
            save: 'POST',
            update: 'PUT',
            patch: ['PATCH', 'POST'],
            delete: 'DELETE',
            generateForOrder: 'POST'
    ]

    PutawayTaskService putawayTaskService

    PutawayTaskApiController() {
        super(PutawayTask)
    }

    def search(SearchPutawayTaskCommand command) {
        log.info "search params " + params
        // Return a not found error if request specifies a product, but the product does not exist
        if (command.product && !command.product) {
            render(status: HttpStatus.NOT_FOUND.value())
            return;
        }

        def tasks = putawayTaskService.search(command.facility, command.product, params)

        render ([data: tasks, totalCount: tasks.totalCount?:0] as JSON)
    }

    // FIXME I think I was experimenting with the data binding mechanism to see if this would work
    //  It does but I wanted to keep the original code in case we wanted to rollback.
    def read(PutawayTask putawayTask) {
//        def task = putawayTaskService.get(id)
//        if (!task) {
//            return render(status: HttpStatus.NOT_FOUND.value())
//        }
//        render([data: task] as JSON)
        respond ([data: putawayTask])
    }

    def save() {
        throw new UnsupportedOperationException("Unable to create a putaway task at this time")
    }

    def update(String id) {
        throw new UnsupportedOperationException("Unable to update a putaway task at this time")
    }

    def delete(String id) {
        throw new UnsupportedOperationException("Unable to delete a putaway task at this time")
    }

    /**
     * Generic action endpoint for state transitions and side-effects
     * PATCH /api/putaway-tasks/{id}
     * {
     *   "action": "scanToContainer", "data": {"containerId":"TOTE-123","override":true}
     * }
     * Common actions: start, scanToContainer, complete, cancel
     */
    def patch() {
        def jsonBody = request.JSON ?: [:]
        // FIXME Improve error handling
        PutawayTask task = putawayTaskService.patch(params.id, jsonBody)
        if (!task) {
            return render(status: HttpStatus.NOT_FOUND.value())
        }
        render([data: task] as JSON)
    }
}

class SearchPutawayTaskCommand {
    Location facility
    Product product
    StatusCategory statusCategory
}