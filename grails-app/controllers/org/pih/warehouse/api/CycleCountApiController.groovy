package org.pih.warehouse.api

import grails.converters.JSON
import org.pih.warehouse.inventory.CycleCountCandidate
import org.pih.warehouse.inventory.CycleCountCandidateFilterCommand
import org.pih.warehouse.inventory.CycleCountService

class CycleCountApiController {

    CycleCountService cycleCountService

    def getCandidates(CycleCountCandidateFilterCommand filterParams) {
        List<CycleCountCandidate> candidates = cycleCountService.getCandidates(filterParams, params.facilityId)

        render([data: candidates, totalCount: candidates.totalCount] as JSON)
    }
}
