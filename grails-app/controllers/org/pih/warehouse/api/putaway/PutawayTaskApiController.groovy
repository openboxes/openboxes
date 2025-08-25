package org.pih.warehouse.api.putaway

import grails.converters.JSON
import org.pih.warehouse.api.StatusCategory
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product
import org.pih.warehouse.putaway.PutawayTask
import org.pih.warehouse.putaway.PutawayTaskService
import org.springframework.http.HttpStatus

class PutawayTaskApiController {

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

    def search(SearchPutawayTaskCommand command) {
        log.info "search params " + params
        // Return a not found error if request specifies a product, but the product does not exist
        if (command.product && !command.product) {
            render(status: HttpStatus.NOT_FOUND.value())
            return;
        }

        def tasks = putawayTaskService.search(command.facility, command.product, params)

        render ([data: tasks, totalCount: tasks.totalCount] as JSON)
    }

    def read(String id) {
        def task = putawayTaskService.get(id)
        if (!task) {
            return render(status: HttpStatus.NOT_FOUND.value())
        }
        render([data: task] as JSON)
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
        PutawayTask putawayTask = putawayTaskService.patch(params.id, (String) jsonBody.action, jsonBody)
        if (!putawayTask) {
            return render(status: HttpStatus.NOT_FOUND.value())
        }
        render([data: putawayTask] as JSON)
    }


}

class SearchPutawayTaskCommand {
    Location facility
    Product product
    StatusCategory statusCategory
}