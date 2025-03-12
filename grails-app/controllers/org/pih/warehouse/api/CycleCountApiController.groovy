package org.pih.warehouse.api

import grails.converters.JSON
import grails.validation.ValidationException
import org.apache.commons.csv.CSVPrinter
import org.pih.warehouse.core.Constants
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

        withFormat {
            json {
                render([data: cycleCounts] as JSON)
            }
            xls {
                exportCountXls(cycleCounts, command.facility.name)
            }
            pdf {
                renderCountPdf(cycleCounts, command.facility.name)
            }
        }
    }

    def startRecount(CycleCountStartRecountBatchCommand command) {
        BatchCommandUtils.validateBatch(command, "cycleCounts")
        List<CycleCountDto> cycleCounts = cycleCountService.startRecount(command)

        withFormat {
            json {
                render([data: cycleCounts] as JSON)
            }
            xls {
                exportRecountXls(cycleCounts, command.facility.name)
            }
            pdf {
                renderRecountPdf(cycleCounts, command.facility.name)
            }
        }
    }

    def list() {
        List<String> ids = params.list("id")
        List<CycleCountDto> cycleCounts = cycleCountService.getCycleCounts(ids)

        boolean isRecount = cycleCounts?.any { (it.status as CycleCountStatus) in CycleCountStatus.listRecounting() }
        String facilityName = cycleCounts?.first()?.cycleCountItems?.first()?.facility?.name  ?: ""

        withFormat {
            json {
                render([data: cycleCounts] as JSON)
            }
            xls {
                isRecount ? exportRecountXls(cycleCounts, facilityName) : exportCountXls(cycleCounts, facilityName)
            }
            pdf {
                isRecount ? renderRecountPdf(cycleCounts, facilityName) : renderCountPdf(cycleCounts, facilityName)
            }
        }
    }

    def exportCountXls(List<CycleCountDto> cycleCounts, String facilityName) {
        List<Map> data = cycleCountService.getCountFormXls(cycleCounts)
        String fileName = "Count form  - ${facilityName} - ${Constants.DISPLAY_DATE_FORMATTER.format(new Date())}.xls"
        response.setHeader("Content-disposition", "attachment; filename=\"${fileName}\"")
        response.contentType = "application/vnd.ms-excel"
        documentService.generateExcel(response.outputStream, data)
        response.outputStream.flush()
    }

    def renderCountPdf(List<CycleCountDto> cycleCounts, String facilityName) {
        renderPdf(
                template: "/cycleCount/printCount",
                model: [cycleCounts: cycleCounts, facilityName: facilityName, datePrinted: new Date()],
                filename: "Count form - ${facilityName} - ${Constants.DISPLAY_DATE_FORMATTER.format(new Date())}.pdf"
        )
    }

    def exportRecountXls(List<CycleCountDto> cycleCounts, String facilityName) {
        List<Map> data = cycleCountService.getRecountFormXls(cycleCounts)
        String fileName = "Recount form  - ${facilityName} - ${Constants.DISPLAY_DATE_FORMATTER.format(new Date())}.xls"
        response.setHeader("Content-disposition", "attachment; filename=\"${fileName}\"")
        response.contentType = "application/vnd.ms-excel"
        documentService.generateExcel(response.outputStream, data)
        response.outputStream.flush()
    }

    def renderRecountPdf(List<CycleCountDto> cycleCounts, String facilityName) {
        renderPdf(
                template: "/cycleCount/printRecount",
                model: [cycleCounts: cycleCounts, facilityName: facilityName, datePrinted: new Date()],
                filename: "Recount form - ${facilityName} - ${Constants.DISPLAY_DATE_FORMATTER.format(new Date())}.pdf"
        )
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
