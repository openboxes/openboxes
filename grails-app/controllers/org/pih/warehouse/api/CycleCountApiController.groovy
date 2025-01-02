package org.pih.warehouse.api

import grails.converters.JSON
import grails.validation.ValidationException
import org.pih.warehouse.inventory.CycleCountCandidate
import org.pih.warehouse.inventory.CycleCountCandidateFilterCommand
import org.pih.warehouse.inventory.CycleCountRequest
import org.pih.warehouse.inventory.CycleCountRequestCommand
import org.pih.warehouse.inventory.CycleCountService

class CycleCountApiController {

    CycleCountService cycleCountService

    def getCandidates(CycleCountCandidateFilterCommand filterParams) {
        List<CycleCountCandidate> candidates = cycleCountService.getCandidates(filterParams, params.facilityId)

        render([data: candidates, totalCount: candidates.totalCount] as JSON)
    }

    def createRequest(CycleCountRequestCommand command) {
        if (command.hasErrors()) {
            throw new ValidationException("Invalid cycle count", command.errors)
        }
        List<CycleCountRequest> cycleCountRequests = cycleCountService.createRequest(command)
        render([data: cycleCountRequests] as JSON)
    }
}
