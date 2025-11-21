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
            patch: 'PATCH'
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
                totalCount: tasks.size() ?: 0,
                max: max,
                offset: offset
        ] as JSON)
    }

    def read(String id) {
        PickTask task = pickTaskService.get(id)
        if (!task) {
            render ([errorCode: 404, message: "Pick task not found"] as JSON)
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

        render([data: task] as JSON)
    }
}
