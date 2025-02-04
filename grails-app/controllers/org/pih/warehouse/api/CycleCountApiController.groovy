package org.pih.warehouse.api

import grails.converters.JSON
import grails.validation.ValidationException
import org.apache.commons.csv.CSVPrinter
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.CycleCount
import org.pih.warehouse.inventory.CycleCountCandidate
import org.pih.warehouse.inventory.CycleCountCandidateFilterCommand
import org.pih.warehouse.inventory.CycleCountDto
import org.pih.warehouse.inventory.CycleCountRequest
import org.pih.warehouse.inventory.CycleCountRequestBatchCommand
import org.pih.warehouse.inventory.CycleCountRequestCommand
import org.pih.warehouse.inventory.CycleCountService
import org.pih.warehouse.inventory.CycleCountStartBatchCommand
import org.pih.warehouse.inventory.CycleCountStartCommand
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.Errors

class CycleCountApiController {

    CycleCountService cycleCountService

    def getCandidates(CycleCountCandidateFilterCommand filterParams) {
        List<CycleCountCandidate> cycleCounts = cycleCountService.getCandidates(filterParams, params.facilityId)

        if (filterParams.format == "csv") {
            CSVPrinter csv = cycleCountService.getCycleCountCsv(cycleCounts)
            response.setHeader("Content-disposition", "attachment; filename=\"CycleCountReport-${new Date().format("yyyyMMdd-hhmmss")}.csv\"")
            render(contentType: "text/csv", text: csv.out.toString())
            return
        }
        render([data: cycleCounts, totalCount: cycleCounts.totalCount] as JSON)
    }

    def createRequests(CycleCountRequestBatchCommand command) {
        if (command.hasErrors()) {
            // Build errors manually to be able to include errors both for the batch command instance
            // and errors for every element of the requests list
            Errors errors = new BeanPropertyBindingResult(command, "requests")
            // Iterate every element of requests and add its error to the errors instance.
            command.requests.each { CycleCountRequestCommand request ->
                request.errors.allErrors.each { error ->
                    errors.addError(error)
                }
            }
            // If there are not any errors in the elements, it means that there must be errors in the batch command instance
            // e.g. missing required arguments
            if (!errors.hasErrors()) {
                command.errors.allErrors.each { error ->
                    errors.addError(error)
                }
            }
            throw new ValidationException("Invalid cycle count request", errors)
        }
        List<CycleCountRequest> cycleCountRequests = cycleCountService.createRequests(command)
        render([data: cycleCountRequests] as JSON)
    }

    def startCycleCount(CycleCountStartBatchCommand command) {
        if (command.hasErrors()) {
            // Build errors manually to be able to include errors both for the batch command instance
            // and errors for every element of the requests list
            Errors errors = new BeanPropertyBindingResult(command, "requests")
            // Iterate every element of requests and add its error to the errors instance.
            command.requests.each { CycleCountStartCommand request ->
                request.errors.allErrors.each { error ->
                    errors.addError(error)
                }
            }
            // If there are not any errors in the elements, it means that there must be errors in the batch command instance
            // e.g. missing required arguments
            if (!errors.hasErrors()) {
                command.errors.allErrors.each { error ->
                    errors.addError(error)
                }
            }
            throw new ValidationException("Invalid cycle count", errors)
        }
        List<CycleCountDto> cycleCounts = cycleCountService.startCycleCount(command)

        render([data: cycleCounts] as JSON)
    }

    def list() {
        List<String> ids = params.list("id")
        List<CycleCountDto> cycleCounts = cycleCountService.getCycleCounts(ids)

        render([data: cycleCounts] as JSON)
    }
}
