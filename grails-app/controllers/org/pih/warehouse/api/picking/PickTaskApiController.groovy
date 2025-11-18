package org.pih.warehouse.api.picking

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import org.pih.warehouse.picking.PickTask
import org.pih.warehouse.picking.PickTaskService

class PickTaskApiController extends RestfulController<PickTask> {

    static responseFormats = ['json']
    static allowedMethods = [
            index: 'GET',
            search: 'GET'
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

        def tasks = pickTaskService.search(command, params)

        render ([data: tasks, totalCount: tasks.size() ?: 0] as JSON)
    }
}
