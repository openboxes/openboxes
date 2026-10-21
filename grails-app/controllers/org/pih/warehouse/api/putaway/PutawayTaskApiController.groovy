package org.pih.warehouse.api.putaway

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.UserService
import org.pih.warehouse.inboundSortation.DemandService
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

    DemandService demandService
    PutawayTaskService putawayTaskService
    UserService userService

    PutawayTaskApiController() {
        super(PutawayTask)
    }

    def search(SearchPutawayTaskCommand command) {
        log.info "search params " + params

        Location facility = facilityFor(RoleType.ROLE_BROWSER)
        if (!facility) {
            return
        }
        command.facility = facility
        if (command.hasErrors()) {
            throw new ValidationException("validation errors", command.errors)
        }

        def tasks = putawayTaskService.search(command, params)

        render ([data: tasks, totalCount: tasks.totalCount?:0] as JSON)
    }

    def read(String id) {
        Location facility = facilityFor(RoleType.ROLE_BROWSER)
        if (!facility) {
            return
        }
        PutawayTask putawayTask = putawayTaskService.read(id, facility)
        if (!putawayTask) {
            render(status: HttpStatus.NOT_FOUND.value(), [errorCode: 404, errorMessage: "Putaway task ${id} not found"] as JSON)
            return
        }
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
        Location facility = facilityFor(RoleType.ROLE_ASSISTANT)
        if (!facility) {
            return
        }
        def jsonBody = request.JSON ?: [:]
        // FIXME Improve error handling
        PutawayTask task = putawayTaskService.patch(params.id, jsonBody, facility)
        if (!task) {
            return render(status: HttpStatus.NOT_FOUND.value())
        }
        render([data: task] as JSON)
    }

    def suggestions(SuggestInboundRoutingCommand command) {
        Location facility = facilityFor(RoleType.ROLE_BROWSER)
        if (!facility) {
            return
        }
        command.facility = facility
        if (command.hasErrors()) {
            throw new ValidationException("validation errors", command.errors)
        }

        def allocations = demandService.getAllocations(command.facility, command.product)
        def demands = demandService.getDemands(command.facility, command.product)
        def unmetDemands = demandService.calculateUnmetDemand(command.facility, command.product)

        render([data: [allocations: allocations, demands: demands, unmetDemands: unmetDemands]] as JSON)
    }

    def alternateDestinations(String id) {
        Location facility = facilityFor(RoleType.ROLE_BROWSER)
        if (!facility) {
            return
        }
        PutawayTask task = putawayTaskService.read(id, facility)
        if (!task) {
            render(status: HttpStatus.NOT_FOUND.value(), [errorCode: 404, errorMessage: "Putaway task ${id} not found"] as JSON)
            return
        }
        List<Location> locations = putawayTaskService.getAlternateDestinations(task)
        render ([data:locations] as JSON)
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
