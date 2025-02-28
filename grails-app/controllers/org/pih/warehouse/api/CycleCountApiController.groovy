package org.pih.warehouse.api

import grails.converters.JSON
import grails.validation.ValidationException
import org.apache.commons.csv.CSVPrinter
import org.pih.warehouse.core.DocumentService
import org.pih.warehouse.core.dtos.BatchCommandUtils
import org.pih.warehouse.inventory.CycleCountCandidate
import org.pih.warehouse.inventory.CycleCountCandidateFilterCommand
import org.pih.warehouse.inventory.CycleCountDto
import org.pih.warehouse.inventory.CycleCountItemCommand
import org.pih.warehouse.inventory.CycleCountItemDto
import org.pih.warehouse.inventory.CycleCountRequest
import org.pih.warehouse.inventory.CycleCountRequestBatchCommand
import org.pih.warehouse.inventory.CycleCountService
import org.pih.warehouse.inventory.CycleCountStartBatchCommand
import org.pih.warehouse.inventory.CycleCountStartRecountBatchCommand
import org.pih.warehouse.inventory.CycleCountStatus
import org.pih.warehouse.inventory.CycleCountSubmitCountCommand
import org.pih.warehouse.inventory.CycleCountSubmitRecountCommand
import org.pih.warehouse.inventory.CycleCountUpdateItemCommand


class CycleCountApiController {

    CycleCountService cycleCountService
    DocumentService documentService

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

        if (!params.format) {
            params.format = "json"
        }

        withFormat {
            xls {
                exportCountXls(cycleCounts)
            }
            pdf {
                renderCountPdf(cycleCounts, command.facility.name)
            }
            json {
                render([data: cycleCounts] as JSON)
            }
        }
    }

    def startRecount(CycleCountStartRecountBatchCommand command) {
        BatchCommandUtils.validateBatch(command, "requests")
        List<CycleCountDto> cycleCounts = cycleCountService.startRecount(command)

        if (!params.format) {
            params.format = "json"
        }

        withFormat {
            xls {
                exportRecountXls(cycleCounts)
            }
            pdf {
                // TODO: To be implemented in OBPIH-7016
            }
            json {
                render([data: cycleCounts] as JSON)
            }
        }
    }

    def list() {
        List<String> ids = params.list("id")
        List<CycleCountDto> cycleCounts = cycleCountService.getCycleCounts(ids)

        if (!params.format) {
            params.format = "json"
        }

        boolean isRecount = cycleCounts?.any { it.status == CycleCountStatus.INVESTIGATING.name() }

        withFormat {
            xls {
                isRecount ? exportRecountXls(cycleCounts) : exportCountXls(cycleCounts)
            }
            pdf {
                String facilityName = cycleCounts?.first()?.cycleCountItems?.first()?.facility?.name  ?: ""
                renderCountPdf(cycleCounts, facilityName)
            }
            json {
                render([data: cycleCounts] as JSON)
            }
        }
    }

    def exportCountXls(List<CycleCountDto> cycleCounts) {
        List<Map> data = cycleCountService.getCountFormXls(cycleCounts)
        response.setHeader("Content-disposition", "attachment; filename=Count form.xls")
        response.contentType = "application/vnd.ms-excel"
        documentService.generateExcel(response.outputStream, data)
        response.outputStream.flush()
    }

    def renderCountPdf(List<CycleCountDto> cycleCounts, String facilityName) {
        renderPdf(
                template: "/cycleCount/printCount",
                model: [cycleCounts: cycleCounts, facilityName: facilityName, datePrinted: new Date()],
                filename: "Count form.pdf"
        )
    }

    def exportRecountXls(List<CycleCountDto> cycleCounts) {
        List<Map> data = cycleCountService.getRecountFormXls(cycleCounts)
        response.setHeader("Content-disposition", "attachment; filename=Recount form.xls")
        response.contentType = "application/vnd.ms-excel"
        documentService.generateExcel(response.outputStream, data)
        response.outputStream.flush()
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

    def createCycleCountItem(CycleCountItemCommand command) {
        if (command.hasErrors()) {
            throw new ValidationException("Invalid cycle count item", command.errors)
        }
        CycleCountItemDto cycleCountItem = cycleCountService.createCycleCountItem(command)

        render([data: cycleCountItem] as JSON)
    }
}
