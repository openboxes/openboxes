package org.pih.warehouse.api

import grails.converters.JSON
import grails.orm.PagedResultList
import grails.validation.ValidationException
import org.apache.commons.csv.CSVPrinter
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.DocumentService
import org.pih.warehouse.core.UploadService
import org.pih.warehouse.core.dtos.BatchCommandUtils
import org.pih.warehouse.importer.CycleCountItemsExcelImporter
import org.pih.warehouse.importer.DataImporter
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.importer.PackingListExcelImporter
import org.pih.warehouse.inventory.CycleCountCandidate
import org.pih.warehouse.inventory.CycleCountCandidateFilterCommand
import org.pih.warehouse.inventory.CycleCountDto
import org.pih.warehouse.inventory.CycleCountImportService
import org.pih.warehouse.inventory.CycleCountItemBatchCommand
import org.pih.warehouse.inventory.CycleCountItemCommand
import org.pih.warehouse.inventory.CycleCountItemDto
import org.pih.warehouse.inventory.CycleCountRequest
import org.pih.warehouse.inventory.CycleCountRequestBatchCommand
import org.pih.warehouse.inventory.CycleCountRequestUpdateBulkCommand
import org.pih.warehouse.inventory.CycleCountService
import org.pih.warehouse.inventory.CycleCountStartBatchCommand
import org.pih.warehouse.inventory.CycleCountStartRecountBatchCommand
import org.pih.warehouse.inventory.CycleCountStatus
import org.pih.warehouse.inventory.CycleCountSubmitCountCommand
import org.pih.warehouse.inventory.CycleCountSubmitRecountCommand
import org.pih.warehouse.inventory.CycleCountUpdateItemBatchCommand
import org.pih.warehouse.inventory.CycleCountUpdateItemCommand
import org.pih.warehouse.inventory.PendingCycleCountRequest
import org.pih.warehouse.report.CycleCountReportCommand
import org.springframework.web.multipart.MultipartFile

class CycleCountApiController {

    CycleCountService cycleCountService
    DocumentService documentService
    UploadService uploadService
    CycleCountImportService cycleCountImportService

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

    def getPendingCycleCountRequests(CycleCountCandidateFilterCommand filterParams) {
        List<PendingCycleCountRequest> pendingCycleCountRequests = cycleCountService.getPendingCycleCountRequests(filterParams, params.facilityId)

        render([data: pendingCycleCountRequests, totalCount: pendingCycleCountRequests.totalCount] as JSON)
    }

    def createRequests(CycleCountRequestBatchCommand command) {
        BatchCommandUtils.validateBatch(command, "requests")
        List<CycleCountRequest> cycleCountRequests = cycleCountService.createRequests(command)
        render([data: cycleCountRequests] as JSON)
    }

    def updateRequests(CycleCountRequestUpdateBulkCommand command) {
        BatchCommandUtils.validateBatch(command)
        List<CycleCountRequest> cycleCountRequests = cycleCountService.updateRequests(command)
        render([data: cycleCountRequests] as JSON)
    }

    def deleteRequests() {
        List<String> ids = params.list("id")
        cycleCountService.deleteCycleCountRequests(ids)
        render(status: 204)
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
        BatchCommandUtils.validateBatch(command, "requests")
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
        String sortBy = params.sortBy
        List<CycleCountDto> cycleCounts = cycleCountService.getCycleCounts(ids, sortBy)

        boolean isRecount = cycleCounts?.any { (it.status as CycleCountStatus).isRecounting() }
        String facilityName = cycleCounts?.find()?.cycleCountItems?.find()?.facility?.name  ?: ""

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

    def refreshCycleCount(String cycleCountId) {
        boolean removeOutOfStockItemsImplicitly = params.boolean("removeOutOfStockItemsImplicitly", false)
        CycleCountDto cycleCount = cycleCountService.refreshCycleCount(cycleCountId, removeOutOfStockItemsImplicitly, params.int("countIndex"))

        render([data: cycleCount] as JSON)
    }

    def createCycleCountItemBatch(CycleCountItemBatchCommand command) {
        BatchCommandUtils.validateBatch(command, "itemsToCreate")

        List<CycleCountItemDto> cycleCountItems = cycleCountService.createCycleCountItems(command.itemsToCreate)

        render([data: cycleCountItems] as JSON)
    }

    def updateCycleCountItemBatch(CycleCountUpdateItemBatchCommand command) {
        BatchCommandUtils.validateBatch(command, "itemsToUpdate")

        List<CycleCountItemDto> cycleCountItems = cycleCountService.updateCycleCountItems(command.itemsToUpdate)

        render([data: cycleCountItems] as JSON)
    }

    def uploadCycleCountItems(ImportDataCommand command) {
        MultipartFile importFile = command.importFile
        File localFile = uploadService.createLocalFile(importFile.originalFilename)
        importFile.transferTo(localFile)
        DataImporter cycleCountItemsExcelImporter = new CycleCountItemsExcelImporter(localFile.absolutePath)
        // After importer takes care of parsing the data, assign it to the import data command that is further validated
        command.data = cycleCountItemsExcelImporter.data
        cycleCountItemsExcelImporter.validateData(command)
        // Collect the errors after validating the data to readable state
        List<String> errors = cycleCountImportService.buildErrors(command)
        render([data: command.data, errors: errors] as JSON)
    }

    def getCycleCountDetails(CycleCountReportCommand command) {
        PagedResultList data = cycleCountService.getCycleCountDetailsReport(command)
        render([
                data      : data,
                count     : data?.size() ?: 0,
                max       : command.max,
                offset    : command.offset,
                totalCount: data.totalCount,
        ] as JSON)
    }

    def getCycleCountSummary(CycleCountReportCommand command) {
        PagedResultList data = cycleCountService.getCycleCountSummaryReport(command)
        render([
                data      : data,
                count     : data?.size() ?: 0,
                max       : command.max,
                offset    : command.offset,
                totalCount: data.totalCount,
        ] as JSON)
    }
}
