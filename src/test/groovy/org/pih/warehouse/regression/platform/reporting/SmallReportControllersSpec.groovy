package org.pih.warehouse.regression.platform.reporting

import spock.lang.PendingFeature

import spock.lang.Ignore
import org.pih.warehouse.reporting.*
import grails.gorm.PagedResultList
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationException
import spock.lang.Specification

import org.pih.warehouse.core.Document
import org.pih.warehouse.core.DocumentCode
import org.pih.warehouse.core.DocumentService
import org.pih.warehouse.core.DocumentType
import org.pih.warehouse.core.Doc4jController
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.http.ContentType
import org.pih.warehouse.core.http.HttpServletResponseConfigurer
import org.pih.warehouse.data.DataExportController
import org.pih.warehouse.data.DataService
import org.pih.warehouse.inventory.InventoryAuditSummaryFormatter
import org.pih.warehouse.receiving.GoodsReceiptNoteDto
import org.pih.warehouse.receiving.GoodsReceiptNoteService
import org.pih.warehouse.report.ReportService
import org.pih.warehouse.shipping.Shipment

class InventoryAuditReportControllerSpec extends Specification implements ControllerUnitTest<InventoryAuditReportController>, DataTest {

    Location facility

    void setupSpec() {
        mockDomains(Location)
    }

    void setup() {
        facility = new Location(name: "Depot").save(validate: false)
        controller.reportService = Mock(ReportService)
        controller.dataService = Mock(DataService)
        controller.inventoryAuditSummaryFormatter = Mock(InventoryAuditSummaryFormatter)
    }

    private InventoryAuditCommand command(Map props = [:]) {
        InventoryAuditCommand command = new InventoryAuditCommand([facility: facility] + props)
        command.validate()
        return command
    }

    private static PagedResultList paged(List rows, int total) {
        return new PagedResultList(null).with {
            resultList = rows
            totalCount = total
            it
        }
    }

    void "the details report renders one page with the total count"() {
        given:
        InventoryAuditCommand command = command(max: 2, offset: 4)

        when:
        controller.getInventoryAuditDetails(command)

        then:
        1 * controller.reportService.getInventoryAuditDetails(command) >> paged([[code: "A"], [code: "B"]], 9)
        response.json.count == 2
        response.json.max == 2
        response.json.offset == 4
        response.json.totalCount == 9
        response.json.data*.code == ["A", "B"]
    }

    @PendingFeature(reason = 'OBAF passes a day-only end date through at midnight, so the range misses the end day')
    void "an end date without a time covers the whole day"() {
        given:
        InventoryAuditCommand command = command(startDate: Date.parse("yyyy-MM-dd", "2024-01-01"),
                endDate: Date.parse("yyyy-MM-dd", "2024-01-31"))

        when:
        controller.getInventoryAuditSummary(command)

        then:
        1 * controller.reportService.getInventoryAuditSummary({ it.endDate.format("yyyy-MM-dd HH:mm") == "2024-01-31 23:59" }) >> paged([], 0)
        response.json.count == 0
    }

    void "the summary can be downloaded as CSV with every row"() {
        given:
        InventoryAuditCommand command = command(max: 10)
        params.format = "csv"

        when:
        controller.getInventoryAuditSummary(command)

        then:
        1 * controller.reportService.getInventoryAuditSummary({ it.max == -1 }) >> paged([], 0)
        1 * controller.inventoryAuditSummaryFormatter.toCsv(_) >> [[code: "A"]]
        1 * controller.dataService.generateCsv([[code: "A"]]) >> "code\nA"
        response.getHeader("Content-disposition") == 'attachment; filename="inventory-audit-summary.csv"'
        response.text == "code\nA"
    }
}

class GoodsReceiptNoteControllerSpec extends Specification implements ControllerUnitTest<GoodsReceiptNoteController>, DataTest {

    // The note itself is built by GoodsReceiptNoteService (see GoodsReceiptNoteServiceSpec): the controller only
    // hands the shipment id over and renders the result.
    GoodsReceiptNoteService goodsReceiptNoteService = Mock(GoodsReceiptNoteService)

    void setup() {
        controller.goodsReceiptNoteService = goodsReceiptNoteService
    }

    void "print renders the goods receipt note of the shipment"() {
        given:
        GoodsReceiptNoteDto goodsReceiptNote = new GoodsReceiptNoteDto(shipmentNumber: "S1")

        when:
        params.id = "shipment-1"
        def model = controller.print()

        then:
        1 * goodsReceiptNoteService.buildGoodsReceiptNote("shipment-1") >> goodsReceiptNote
        model.goodsReceiptNote == goodsReceiptNote
    }

    void "print fails for an unknown shipment"() {
        when:
        params.id = "missing"
        controller.print()

        then:
        1 * goodsReceiptNoteService.buildGoodsReceiptNote("missing") >> {
            throw new IllegalStateException("Unable to locate shipment missing")
        }
        IllegalStateException e = thrown()
        e.message == "Unable to locate shipment missing"
    }
}

class DataExportControllerSpec extends Specification implements ControllerUnitTest<DataExportController>, DataTest {

    Document export

    void setupSpec() {
        mockDomains(Document, DocumentType)
    }

    void setup() {
        DocumentType exportType = new DocumentType(name: "Export", documentCode: DocumentCode.DATA_EXPORT).save(validate: false)
        export = new Document(name: "Products", fileContents: "select 1".bytes, documentType: exportType).save(validate: false)
        controller.dataService = Mock(DataService)
    }

    void "index lists the data export documents"() {
        when:
        def model = controller.index()

        then:
        model.documents == [export]
    }

    void "render runs the export query and renders JSON"() {
        when:
        params.id = export.id
        controller.render()

        then:
        2 * controller.dataService.executeQuery("select 1") >> [[one: 1]]
        response.json[0].one == 1
    }

    void "render downloads the export as CSV"() {
        when:
        params.id = export.id
        params.format = "csv"
        controller.render()

        then:
        1 * controller.dataService.executeQuery("select 1") >> [[one: 1]]
        1 * controller.dataService.generateCsv([[one: 1]]) >> "one\n1"
        response.getHeader("Content-disposition") == 'attachment; filename="Products.csv"'
        response.text == "one\n1"
    }
}

class Doc4jControllerSpec extends Specification implements ControllerUnitTest<Doc4jController>, DataTest {

    Shipment shipment

    void setupSpec() {
        mockDomains(Shipment)
    }

    void setup() {
        shipment = new Shipment(name: "Relief", shipmentNumber: "SH-7").save(validate: false)
        controller.documentService = Mock(DocumentService)
        controller.httpServletResponseConfigurer = Mock(HttpServletResponseConfigurer)
    }

    void "downloadPackingList writes the packing list spreadsheet"() {
        when:
        params.id = shipment.id
        controller.downloadPackingList()

        then:
        1 * controller.httpServletResponseConfigurer.withFile(_, ContentType.XLS, ["Packing List", "Relief"])
        1 * controller.documentService.generatePackingList(_, shipment) >> { args -> args[0].write("XLS".bytes) }
        response.contentAsByteArray == "XLS".bytes
    }

    void "the packing list requires a shipment"() {
        when:
        params.id = "missing"
        controller.downloadPackingList()

        then:
        Exception e = thrown()
        e.message == "Unable to locate shipment with ID missing"
    }
}
