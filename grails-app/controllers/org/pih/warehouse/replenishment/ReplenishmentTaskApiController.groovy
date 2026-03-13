package org.pih.warehouse.replenishment

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import org.pih.warehouse.replenishment.ReplenishmentTask
import org.springframework.http.HttpStatus

class ReplenishmentTaskApiController extends RestfulController<ReplenishmentTask> {

    static responseFormats = ['json']
    static allowedMethods = [
            search: 'GET',
            read: 'GET',
            patch: 'PATCH',
            drop: 'PATCH'
    ]

    ReplenishmentTaskService replenishmentTaskService

    ReplenishmentTaskApiController() {
        super(ReplenishmentTask)
    }

    def search(SearchReplenishmentTaskCommand command) {
        log.info "Replenishment tasks search params " + params

        if (command.hasErrors()) {
            throw new ValidationException("Validation errors", command.errors)
        }

        Integer max = params.int('max') ?: 100
        Integer offset = params.int('offset') ?: 0

        def tasks = replenishmentTaskService.search(command, params)

        render ([
                data: tasks,
                totalCount: tasks.totalCount,
                max: max,
                offset: offset
        ] as JSON)
    }

    def read(String id) {
        ReplenishmentTask task = replenishmentTaskService.get(id)
        if (!task) {
            render (status: HttpStatus.NOT_FOUND.value(), [errorCode: 404, message: "Replenishment task not found"] as JSON)
            return
        }

        render ([data: task.toJson()] as JSON)
    }

    def patch() {
        def jsonBody = request.JSON ?: [:]

        ReplenishmentTask task = replenishmentTaskService.patch(params.id, jsonBody)
        if (!task) {
            return render(status: HttpStatus.NOT_FOUND.value())
        }

        render([data: task.toJson()] as JSON)
    }

    def drop() {
        def jsonBody = request.JSON ?: [:]
        String outboundContainerId = params.outboundContainerId
        try {
            replenishmentTaskService.drop(outboundContainerId, jsonBody)
        } catch (Exception e) {
            response.status = 500
            render([errorCode: 500, errorMessage: e?.message ?: "Error occurred"] as JSON)
            return
        }

        render status: 200
    }
}
