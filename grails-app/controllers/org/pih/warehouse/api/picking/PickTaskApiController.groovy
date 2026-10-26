package org.pih.warehouse.api.picking

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.UserService
import org.pih.warehouse.picking.PickTask
import org.pih.warehouse.picking.PickTaskService
import org.springframework.http.HttpStatus

class PickTaskApiController extends RestfulController<PickTask> {

    static responseFormats = ['json']
    static allowedMethods = [
            search: 'GET',
            counts: 'GET',
            read: 'GET',
            patch: 'PATCH',
            drop: 'PATCH',
            reallocate: 'POST'
    ]

    PickTaskService pickTaskService
    UserService userService

    PickTaskApiController() {
        super(PickTask)
    }

    def search(SearchPickTaskCommand command) {
        log.info "Pick tasks search params " + params

        Location facility = facilityFor(RoleType.ROLE_BROWSER)
        if (!facility) {
            return
        }
        command.facility = facility
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

    def counts(SearchPickTaskCommand command) {
        Location facility = facilityFor(RoleType.ROLE_BROWSER)
        if (!facility) {
            return
        }
        command.facility = facility
        if (command.hasErrors()) {
            throw new ValidationException("Validation errors", command.errors)
        }

        def data = pickTaskService.countOrdersByDeliveryType(command)

        render([data: data] as JSON)
    }

    def read(String id) {
        Location facility = facilityFor(RoleType.ROLE_BROWSER)
        if (!facility) {
            return
        }
        PickTask task = pickTaskService.get(id, facility)
        if (!task) {
            render (status: HttpStatus.NOT_FOUND.value(), [errorCode: 404, message: "Pick task not found"] as JSON)
            return
        }

        render ([data: task.toJson()] as JSON)
    }

    def patch() {
        Location facility = facilityFor(RoleType.ROLE_ASSISTANT)
        if (!facility) {
            return
        }
        def jsonBody = request.JSON ?: [:]

        PickTask task = pickTaskService.patch(params.id, jsonBody, facility)
        if (!task) {
            return render(status: HttpStatus.NOT_FOUND.value())
        }

        render([data: task.toJson()] as JSON)
    }

    def reallocate() {
        Location facility = facilityFor(RoleType.ROLE_ASSISTANT)
        if (!facility) {
            return
        }
        def jsonBody = request.JSON ?: [:]

        PickTask task = pickTaskService.get(params.id, facility)
        if (!task) {
            render (status: HttpStatus.NOT_FOUND.value(), [errorCode: 404, message: "Pick task not found"] as JSON)
            return
        }

        List<PickTask> newTasks = pickTaskService.reallocate(task, jsonBody.picklistItems as List)

        render([data: newTasks.collect { it.toJson() }] as JSON)
    }

    def drop() {
        Location facility = facilityFor(RoleType.ROLE_ASSISTANT)
        if (!facility) {
            return
        }
        def jsonBody = request.JSON ?: [:]
        String outboundContainerId = params.outboundContainerId
        Location outboundContainer = pickTaskService.findOutboundContainer(outboundContainerId, facility)
        if (!outboundContainer) {
            render(status: HttpStatus.NOT_FOUND.value(),
                    [errorCode: 404, errorMessage: "Outbound container ${outboundContainerId} not found"] as JSON)
            return
        }
        pickTaskService.drop(outboundContainer, jsonBody)
        render status: 200
    }

    /**
     * The facility in the request URL, when the signed-in user holds at least the given role there (globally or at
     * that facility). Otherwise renders 404 (unknown facility) or 403 and returns null.
     */
    private Location facilityFor(RoleType minimumRole) {
        Location facility = params.facility ? Location.get(params.facility as String) : null
        if (!facility) {
            render(status: HttpStatus.NOT_FOUND.value(), [errorCode: 404, errorMessage: "Facility ${params.facility} not found"] as JSON)
            return null
        }
        if (!userService.isUserInRoleAtLocation(AuthService.currentUser, facility, minimumRole)) {
            render(status: HttpStatus.FORBIDDEN.value(), [errorCode: 403, errorMessage: "Access denied"] as JSON)
            return null
        }
        return facility
    }
}
