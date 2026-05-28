package org.pih.warehouse.api

import grails.orm.PagedResultList
import grails.validation.ValidationException
import java.time.Instant
import org.apache.commons.csv.CSVPrinter

import org.pih.warehouse.core.BaseController
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.DocumentService
import org.pih.warehouse.core.UploadService
import org.pih.warehouse.core.dtos.BatchCommandUtils
import org.pih.warehouse.core.http.HttpResponseContext
import org.pih.warehouse.importer.CycleCountItemsExcelImporter
import org.pih.warehouse.importer.CycleCountRecountItemsExcelImporter
import org.pih.warehouse.importer.DataImporter
import org.pih.warehouse.importer.ImportDataCommand
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
import org.pih.warehouse.inventory.InventoryTransactionsSummary
import org.pih.warehouse.inventory.PendingCycleCountRequest
import org.pih.warehouse.report.CycleCountReportCommand
import org.springframework.web.multipart.MultipartFile

class CycleCountApiController extends BaseController {

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
        renderResponse(cycleCounts)
    }

    def getPendingCycleCountRequests(CycleCountCandidateFilterCommand filterParams) {
        List<PendingCycleCountRequest> pendingCycleCountRequests = cycleCountService.getPendingCycleCountRequests(filterParams, params.facilityId)

        renderResponse(pendingCycleCountRequests)
    }

    def createRequests(CycleCountRequestBatchCommand command) {
        BatchCommandUtils.validateBatch(command, "requests")
        List<CycleCountRequest> cycleCountRequests = cycleCountService.createRequests(command)
        renderResponse(cycleCountRequests)
    }

    def updateRequests(CycleCountRequestUpdateBulkCommand command) {
        BatchCommandUtils.validateBatch(command)
        List<CycleCountRequest> cycleCountRequests = cycleCountService.updateRequests(command)
        renderResponse(cycleCountRequests)
    }

    def deleteRequests() {
        List<String> ids = params.list("id")
        cycleCountService.deleteCycleCountRequests(ids)
        renderNoContentResponse()
    }

    def startCycleCount(CycleCountStartBatchCommand command) {
        BatchCommandUtils.validateBatch(command, "requests")
        List<CycleCountDto> cycleCounts = cycleCountService.startCycleCount(command)

        withFormat {
            json {
                renderResponse(cycleCounts)
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
                renderResponse(cycleCounts)
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
                renderResponse(cycleCounts)
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
        renderResponse(HttpResponseContext.builder()
                .forTemplateFile(
                        "/cycleCount/printCount",
                        [cycleCounts: cycleCounts, facilityName: facilityName, datePrinted: new Date()],
                        ["Count form", facilityName, Instant.now()],
                ))
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
        renderResponse(HttpResponseContext.builder()
                .forTemplateFile(
                        "/cycleCount/printRecount",
                        [cycleCounts: cycleCounts, facilityName: facilityName, datePrinted: new Date()],
                        ["Recount form", facilityName, Instant.now()],
                ))
    }

    def submitCount(CycleCountSubmitCountCommand command) {
        if (command.hasErrors()) {
            throw new ValidationException("Invalid submit count object", command.errors)
        }
        CycleCountDto cycleCount = cycleCountService.submitCount(command)

        renderResponse(cycleCount)
    }

    def submitRecount(CycleCountSubmitRecountCommand command) {
        if (command.hasErrors()) {
            throw new ValidationException("Invalid submit recount object", command.errors)
        }
        CycleCountDto cycleCount = cycleCountService.submitCount(command)

        renderResponse(cycleCount)
    }

    def updateCycleCountItem(CycleCountUpdateItemCommand command) {
        if (command.hasErrors()) {
            throw new ValidationException("Invalid cycle count item", command.errors)
        }
        CycleCountItemDto cycleCountItem = cycleCountService.updateCycleCountItem(command)

        renderResponse(cycleCountItem)
    }

    def deleteCycleCountItem(String cycleCountItemId) {
        cycleCountService.deleteCycleCountItem(cycleCountItemId)

        renderNoContentResponse()
    }

    def createCycleCountItem(CycleCountItemCommand command) {
        if (command.hasErrors()) {
            throw new ValidationException("Invalid cycle count item", command.errors)
        }
        CycleCountItemDto cycleCountItem = cycleCountService.createCycleCountItem(command)

        renderResponse(cycleCountItem)
    }

    def refreshCycleCount(String cycleCountId) {
        boolean removeOutOfStockItemsImplicitly = params.boolean("removeOutOfStockItemsImplicitly", false)
        CycleCountDto cycleCount = cycleCountService.refreshCycleCount(cycleCountId, removeOutOfStockItemsImplicitly, params.int("countIndex"))

        renderResponse(cycleCount)
    }

    def createCycleCountItemBatch(CycleCountItemBatchCommand command) {
        BatchCommandUtils.validateBatch(command, "itemsToCreate")

        List<CycleCountItemDto> cycleCountItems = cycleCountService.createCycleCountItems(command.itemsToCreate)

        renderResponse(cycleCountItems)
    }

    def updateCycleCountItemBatch(CycleCountUpdateItemBatchCommand command) {
        BatchCommandUtils.validateBatch(command, "itemsToUpdate")

        List<CycleCountItemDto> cycleCountItems = cycleCountService.updateCycleCountItems(command.itemsToUpdate)

        renderResponse(cycleCountItems)
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
        renderResponse(data: command.data, additionalFields: [errors: errors])
    }

    def uploadCycleCountRecountItems(ImportDataCommand command) {
        MultipartFile importFile = command.importFile
        File localFile = uploadService.createLocalFile(importFile.originalFilename)
        importFile.transferTo(localFile)
        DataImporter cycleCountRecountItemsExcelImporter = new CycleCountRecountItemsExcelImporter(localFile.absolutePath)
        // After importer takes care of parsing the data, assign it to the import data command that is further validated
        command.data = cycleCountRecountItemsExcelImporter.data
        cycleCountRecountItemsExcelImporter.validateData(command)
        // Collect the errors after validating the data to readable state
        List<String> errors = cycleCountImportService.buildErrors(command)
        renderResponse(data: command.data, additionalFields: [errors: errors])
    }

    def getCycleCountDetails(CycleCountReportCommand command) {
        PagedResultList data = cycleCountService.getCycleCountDetailsReport(command)
        renderResponse(data: data, additionalFields: [
                max       : command.max,
                offset    : command.offset,
        ])
    }

    def getCycleCountSummary(CycleCountReportCommand command) {
        PagedResultList data = cycleCountService.getCycleCountSummaryReport(command)
        renderResponse(data: data, additionalFields: [
                max       : command.max,
                offset    : command.offset,
        ])
    }

    def getInventoryTransactionsSummary(CycleCountReportCommand command) {
        if (!command.validate()) {
            throw new ValidationException("Invalid params", command.errors)
        }
        List<InventoryTransactionsSummary> inventoryTransactions = cycleCountService.getInventoryTransactionsSummary(command)

        renderResponse(inventoryTransactions)
    }
}
