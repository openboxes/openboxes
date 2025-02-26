package org.pih.warehouse.api

import grails.converters.JSON
import grails.validation.ValidationException
import org.apache.commons.csv.CSVPrinter
import org.pih.warehouse.core.dtos.BatchCommandUtils
import org.pih.warehouse.inventory.CycleCountCandidate
import org.pih.warehouse.inventory.CycleCountCandidateFilterCommand
import org.pih.warehouse.inventory.CycleCountDto
import org.pih.warehouse.inventory.CycleCountItem
import org.pih.warehouse.inventory.CycleCountItemCommand
import org.pih.warehouse.inventory.CycleCountItemDto
import org.pih.warehouse.inventory.CycleCountRequest
import org.pih.warehouse.inventory.CycleCountRequestBatchCommand
import org.pih.warehouse.inventory.CycleCountService
import org.pih.warehouse.inventory.CycleCountStartBatchCommand
import org.pih.warehouse.inventory.CycleCountStartRecountBatchCommand
import org.pih.warehouse.inventory.CycleCountSubmitCountCommand
import org.pih.warehouse.inventory.CycleCountUpdateItemCommand

import org.pih.warehouse.inventory.CycleCountSubmitRecountCommand

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
        BatchCommandUtils.validateBatch(command, "requests")
        List<CycleCountRequest> cycleCountRequests = cycleCountService.createRequests(command)
        render([data: cycleCountRequests] as JSON)
    }

    def startCycleCount(CycleCountStartBatchCommand command) {
        BatchCommandUtils.validateBatch(command, "requests")
        List<CycleCountDto> cycleCounts = cycleCountService.startCycleCount(command)

        render([data: cycleCounts] as JSON)
    }

    def startRecount(CycleCountStartRecountBatchCommand command) {
        BatchCommandUtils.validateBatch(command, "requests")
        List<CycleCountDto> cycleCounts = cycleCountService.startRecount(command)

        render([data: cycleCounts] as JSON)
    }

    def list() {
        List<String> ids = params.list("id")
        List<CycleCountDto> cycleCounts = cycleCountService.getCycleCounts(ids)

        render([data: cycleCounts] as JSON)
    }

    def submitCount(CycleCountSubmitCountCommand command) {
        if (command.hasErrors()) {
            throw new ValidationException("Invalid submit count object", command.errors)
        }
        CycleCountDto cycleCount = cycleCountService.submitCount(command)

        render([data: cycleCount] as JSON)
    }

    def submitRecount(CycleCountSubmitRecountCommand command) {
        if (command.hasErrors()) {
            throw new ValidationException("Invalid submit recount object", command.errors)
        }
        CycleCountDto cycleCount = cycleCountService.submitCount(command)

        render([data: cycleCount] as JSON)
    }

    def updateCycleCountItem(CycleCountUpdateItemCommand command) {
        if (command.hasErrors()) {
            throw new ValidationException("Invalid cycle count item", command.errors)
        }
        CycleCountItemDto cycleCountItem = cycleCountService.updateCycleCountItem(command)

        render([data: cycleCountItem] as JSON)
    }

    def deleteCycleCountItem(String cycleCountItemId) {
        cycleCountService.deleteCycleCountItem(cycleCountItemId)

        render(status: 204)
    }

    def createCustomCycleCountItem(CycleCountItemCommand command) {
        if (command.hasErrors()) {
            throw new ValidationException("Invalid cycle count item", command.errors)
        }
        CycleCountItemDto cycleCountItem = cycleCountService.createCustomCycleCountItem(command)

        render([data: cycleCountItem] as JSON)
    }
}
