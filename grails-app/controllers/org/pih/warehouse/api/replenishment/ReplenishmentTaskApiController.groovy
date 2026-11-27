/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.api.replenishment

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.UserService
import org.pih.warehouse.picking.StagingLocationNotFoundException
import org.pih.warehouse.replenishment.ReplenishmentTask
import org.pih.warehouse.replenishment.ReplenishmentTaskService
import org.springframework.http.HttpStatus

class ReplenishmentTaskApiController extends RestfulController<ReplenishmentTask> {

    static responseFormats = ['json']
    static allowedMethods = [
            lists : 'GET',
            search: 'GET',
            read  : 'GET',
            patch : 'PATCH',
            drop  : 'PATCH'
    ]

    ReplenishmentTaskService replenishmentTaskService
    UserService userService

    ReplenishmentTaskApiController() {
        super(ReplenishmentTask)
    }

    /**
     * The replenishment lists of the facility still to pick or stage, optionally limited to the given list statuses
     * (PENDING, PICKING, PICKED).
     */
    def lists() {
        Location facility = facilityFor(RoleType.ROLE_BROWSER)
        if (!facility) {
            return
        }
        List<String> statuses = params.list('status') as List<String>

        render([data: replenishmentTaskService.listReplenishments(facility, statuses)] as JSON)
    }

    def search(SearchReplenishmentTaskCommand command) {
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

        def tasks = replenishmentTaskService.search(command, params)

        render([
                data      : tasks*.toJson(),
                totalCount: tasks.totalCount,
                max       : max,
                offset    : offset
        ] as JSON)
    }

    def read(String id) {
        Location facility = facilityFor(RoleType.ROLE_BROWSER)
        if (!facility) {
            return
        }
        ReplenishmentTask task = replenishmentTaskService.get(id, facility)
        if (!task) {
            render(status: HttpStatus.NOT_FOUND.value(), [errorCode: 404, errorMessage: "Replenishment task ${id} not found"] as JSON)
            return
        }

        render([data: task.toJson()] as JSON)
    }

    def patch() {
        Location facility = facilityFor(RoleType.ROLE_ASSISTANT)
        if (!facility) {
            return
        }
        if (!replenishmentTaskService.get(params.id as String, facility)) {
            render(status: HttpStatus.NOT_FOUND.value(), [errorCode: 404, errorMessage: "Replenishment task ${params.id} not found"] as JSON)
            return
        }
        def jsonBody = request.JSON ?: [:]

        ReplenishmentTask task = replenishmentTaskService.patch(params.id as String, jsonBody, facility)

        render([data: task?.toJson()] as JSON)
    }

    def drop() {
        Location facility = facilityFor(RoleType.ROLE_ASSISTANT)
        if (!facility) {
            return
        }
        def jsonBody = request.JSON ?: [:]
        String outboundContainerId = params.outboundContainerId
        Location outboundContainer = replenishmentTaskService.findOutboundContainer(outboundContainerId, facility)
        if (!outboundContainer) {
            render(status: HttpStatus.NOT_FOUND.value(),
                    [errorCode: 404, errorMessage: "Outbound container ${outboundContainerId} not found"] as JSON)
            return
        }
        if (jsonBody.action == 'drop' && isBlank(jsonBody.stagingLocationId)) {
            render(status: HttpStatus.BAD_REQUEST.value(),
                    [errorCode: 400, errorMessage: "Staging location is required"] as JSON)
            return
        }

        List<ReplenishmentTask> stagedTasks
        try {
            stagedTasks = replenishmentTaskService.drop(outboundContainer, jsonBody)
        } catch (StagingLocationNotFoundException e) {
            log.info "Drop of ${outboundContainer.locationNumber} at facility ${facility.id} rejected: ${e.message}"
            render(status: HttpStatus.NOT_FOUND.value(), [errorCode: 404, errorMessage: e.message] as JSON)
            return
        }

        render([data: stagedTasks*.toJson()] as JSON)
    }

    // Missing, JSON null (Grails' JSONObject holds it as a Java null) or only whitespace.
    private static boolean isBlank(Object value) {
        return value == null || !value.toString().trim()
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
