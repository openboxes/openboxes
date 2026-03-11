package org.pih.warehouse.api.picking

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import org.pih.warehouse.picking.PickTask
import org.pih.warehouse.picking.PickTaskService
import org.springframework.http.HttpStatus

class PickTaskApiController extends RestfulController<PickTask> {

    static responseFormats = ['json']
    static allowedMethods = [
            search: 'GET',
            read: 'GET',
            patch: 'PATCH',
            drop: 'PATCH',
            reallocate: 'POST'
    ]

    PickTaskService pickTaskService

    PickTaskApiController() {
        super(PickTask)
    }

    def search(SearchPickTaskCommand command) {
        log.info "Pick tasks search params " + params

        if (command.hasErrors()) {
            throw new ValidationException("Validation errors", command.errors)
        }

        Integer max = params.int('max') ?: 100
        Integer offset = params.int('offset') ?: 0

        def tasks = pickTaskService.search(command, params)

        render ([
                data: tasks,
                totalCount: tasks.totalCount,
                max: max,
                offset: offset
        ] as JSON)
    }

    def read(String id) {
        PickTask task = pickTaskService.get(id)
        if (!task) {
            render (status: HttpStatus.NOT_FOUND.value(), [errorCode: 404, message: "Pick task not found"] as JSON)
            return
        }

        render ([data: task.toJson()] as JSON)
    }

    def patch() {
        def jsonBody = request.JSON ?: [:]

        PickTask task = pickTaskService.patch(params.id, jsonBody)
        if (!task) {
            return render(status: HttpStatus.NOT_FOUND.value())
        }

        render([data: task.toJson()] as JSON)
    }

    def reallocate() {
        def jsonBody = request.JSON ?: [:]

        PickTask task = pickTaskService.get(params.id)
        if (!task) {
            render (status: HttpStatus.NOT_FOUND.value(), [errorCode: 404, message: "Pick task not found"] as JSON)
            return
        }

        List<PickTask> newTasks = pickTaskService.reallocate(task, jsonBody.picklistItems as List)

        render([data: newTasks.collect { it.toJson() }] as JSON)
    }

    def drop() {
        def jsonBody = request.JSON ?: [:]
        String outboundContainerId = params.outboundContainerId
        try {
            pickTaskService.drop(outboundContainerId, jsonBody)
        } catch (Exception e) {
            response.status = 500
            render([errorCode: 500, errorMessage: e?.message ?: "Error occurred"] as JSON)
            return
        }

        render status: 200
    }
}
