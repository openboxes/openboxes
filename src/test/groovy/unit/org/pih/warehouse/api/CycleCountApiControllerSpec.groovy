package unit.org.pih.warehouse.api

import grails.gorm.PagedResultList
import grails.plugins.rendering.pdf.PdfRenderingService
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationException
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.apache.http.HttpStatus
import org.grails.datastore.mapping.query.Query
import org.pih.warehouse.api.CycleCountApiController
import org.pih.warehouse.core.DocumentService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.dtos.BatchCommandUtils
import org.pih.warehouse.inventory.CycleCountCandidate
import org.pih.warehouse.inventory.CycleCountCandidateFilterCommand
import org.pih.warehouse.inventory.CycleCountDto
import org.pih.warehouse.inventory.CycleCountRequest
import org.pih.warehouse.inventory.CycleCountRequestBatchCommand
import org.pih.warehouse.inventory.CycleCountRequestCommand
import org.pih.warehouse.inventory.CycleCountRequestStatus
import org.pih.warehouse.inventory.CycleCountService
import org.pih.warehouse.inventory.CycleCountStartBatchCommand
import org.pih.warehouse.inventory.CycleCountStartCommand
import org.pih.warehouse.inventory.CycleCountStartRecountBatchCommand
import org.pih.warehouse.inventory.CycleCountStartRecountCommand
import org.pih.warehouse.inventory.PendingCycleCountRequest
import org.pih.warehouse.product.Product
import org.testcontainers.shaded.com.google.common.net.HttpHeaders
import org.w3c.dom.Document
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import javax.servlet.http.HttpServletResponse

@Unroll
class CycleCountApiControllerSpec extends Specification implements DataTest, ControllerUnitTest<CycleCountApiController> {

    private static String FACILITY_ID = 'Facility Id'
    private static String JSON_FORMAT = 'application/json'
    private static String CSV_FORMAT = 'text/csv'
    private static String XLS_FORMAT = 'application/vnd.ms-excel'
    private static String PDF_FORMAT = 'application/pdf'
    private static String PRODUCT_CODE = 'productCode'
    private static String LOCATION_NAME = 'locationName'

    @Shared
    CycleCountService cycleCountService

    @Shared
    DocumentService documentService

    @Shared
    PdfRenderingService pdfRenderingService

    void setup() {
        cycleCountService = Stub(CycleCountService) {
            createRequests(_ as CycleCountRequestBatchCommand) >> { CycleCountRequestBatchCommand cycleCountRequestBatchCommand ->
                return cycleCountRequestBatchCommand.requests.collect { Mock(CycleCountRequest) }
            }
            startCycleCount(_ as CycleCountStartBatchCommand) >> { CycleCountStartBatchCommand cycleCountStartBatchCommand ->
                return cycleCountStartBatchCommand.requests.collect { Mock(CycleCountDto) }
            }
            getCountFormXls(_ as List<CycleCountDto>) >> []
        }
        documentService = Stub(DocumentService)
        pdfRenderingService = Stub(PdfRenderingService) {
            render(_ as Map, _ as HttpServletResponse) >> { Map args, HttpServletResponse response ->
                response.setContentType(PDF_FORMAT)
            }
        }
        controller.cycleCountService = cycleCountService
        controller.documentService = documentService
        controller.pdfRenderingService = pdfRenderingService
    }

    void 'getCandidates should return JSON response when format is not csv'() {
        given:
        CycleCountCandidateFilterCommand filterParams = new CycleCountCandidateFilterCommand(format: JSON_FORMAT)
        Query mockQuery = Stub(Query) {
            list() >> [Mock(CycleCountCandidate),
                       Mock(CycleCountCandidate),]
        }
        List<CycleCountCandidate> mockedCandidates = Spy(PagedResultList, constructorArgs: [mockQuery]) {
            getTotalCount() >> mockQuery.list().size()
        }
        controller.params.facilityId = FACILITY_ID
        cycleCountService.getCandidates(filterParams, FACILITY_ID) >> mockedCandidates

        when:
        controller.getCandidates(filterParams)

        then:
        response.json.data.size() == 2
        response.json.totalCount == 2
        response.contentType.contains(JSON_FORMAT)
        response.status == HttpStatus.SC_OK
    }

    void 'getCandidates should return CSV response when format is csv'() {
        given:
        CycleCountCandidateFilterCommand filterParams = new CycleCountCandidateFilterCommand(format: "csv")
        CycleCountCandidate cycleCountCandidate1 = Mock(CycleCountCandidate)
        CycleCountCandidate cycleCountCandidate2 = Mock(CycleCountCandidate)
        Query mockQuery = Stub(Query) {
            list() >> [cycleCountCandidate1,
                       cycleCountCandidate2]
        }
        List<CycleCountCandidate> mockedCandidates = Spy(PagedResultList, constructorArgs: [mockQuery]) {
            getTotalCount() >> mockQuery.list().size()
        }
        cycleCountService.getCycleCountCsv(mockedCandidates) >> {
            StringWriter sw = new StringWriter()
            CSVPrinter csvPrinter = new CSVPrinter(sw, CSVFormat.DEFAULT.withHeader("ID"))
            mockedCandidates.each { csvPrinter.printRecord(it.hashCode()) }
            csvPrinter.flush()
            return csvPrinter
        }
        controller.params.facilityId = FACILITY_ID
        cycleCountService.getCandidates(filterParams, FACILITY_ID) >> mockedCandidates

        when:
        controller.getCandidates(filterParams)

        then:
        response.contentType.contains(CSV_FORMAT)
        response.getHeader(HttpHeaders.CONTENT_DISPOSITION).startsWith("attachment; filename=\"CycleCountReport-")
        response.status == HttpStatus.SC_OK
        response.text.contains("ID")
        response.text.contains(cycleCountCandidate1.hashCode() as String)
        response.text.contains(cycleCountCandidate2.hashCode() as String)
    }

    void 'getPendingCycleCountRequests should return valid JSON'() {
        given:
        Query mockQuery = Stub(Query) {
            list() >> [Mock(PendingCycleCountRequest),
                       Mock(PendingCycleCountRequest)]
        }
        List<PendingCycleCountRequest> mockedPendingRequests = Spy(PagedResultList, constructorArgs: [mockQuery]) {
            getTotalCount() >> mockQuery.list().size()
        }
        CycleCountCandidateFilterCommand cycleCountCandidateFilterCommand = Mock(CycleCountCandidateFilterCommand)
        controller.params.facilityId = FACILITY_ID
        cycleCountService.getPendingCycleCountRequests(cycleCountCandidateFilterCommand, FACILITY_ID) >> mockedPendingRequests

        when:
        controller.getPendingCycleCountRequests(cycleCountCandidateFilterCommand)

        then:
        response.contentType.contains(JSON_FORMAT)
        response.status == HttpStatus.SC_OK
        response.json.data.size() == 2
        response.json.totalCount == 2
    }

    void 'createRequests should fail when batch has duplicated product'() {
        given: "the following commands"
        CycleCountRequestCommand cycleCountRequestCommand = new CycleCountRequestCommand(
                product: new Product(productCode: PRODUCT_CODE),
                blindCount: true,
        )
        CycleCountRequestBatchCommand cycleCountRequestBatchCommand = new CycleCountRequestBatchCommand(requests: [cycleCountRequestCommand])

        and: "the following facility id is passed as a param"
        controller.params.facilityId = FACILITY_ID

        and: "the following GORM methods are mocked"
        CycleCountRequest.metaClass.static.findByProductAndFacilityAndStatusNotInList = {
            Product product, Location location, List<CycleCountRequestStatus> statuses ->
            return Mock(CycleCountRequest)
        }
        Location.metaClass.static.findById = {
            String id -> Mock(Location)
        }

        when: "the command is invalid"
        cycleCountRequestBatchCommand.validate()

        and: "the request is sent"
        controller.createRequests(cycleCountRequestBatchCommand)

        then: "the exception should be thrown"
        thrown(ValidationException)
    }

    void 'createRequests should return valid JSON'() {
        given:
        BatchCommandUtils.metaClass.static.validateBatch = { Object command, String batchPropertyName ->
            return null
        }
        List<CycleCountRequestCommand> cycleCountRequests = [
                Mock(CycleCountRequestCommand),
                Mock(CycleCountRequestCommand),
        ]
        CycleCountRequestBatchCommand cycleCountRequestBatchCommand = new CycleCountRequestBatchCommand(
                requests: cycleCountRequests,
        )

        when:
        controller.createRequests(cycleCountRequestBatchCommand)

        then:
        response.status == HttpStatus.SC_OK
        response.json.data.size() == 2
    }

    void 'startCycleCount should return cycleCounts in #contentType contentType when the #outputFormat format is used'() {
        given:
        BatchCommandUtils.metaClass.static.validateBatch = { Object command, String batchPropertyName ->
            return null
        }
        List<CycleCountStartCommand> cycleCountStartCommands = [
                Mock(CycleCountStartCommand),
                Mock(CycleCountStartCommand),
        ]
        controller.params.facilityId = FACILITY_ID
        controller.params.format = outputFormat
        Location.metaClass.static.findById = {
            String id -> Mock(Location)
        }
        Location mockedLocation = Stub(Location)
        mockedLocation.name = LOCATION_NAME
        CycleCountStartBatchCommand cycleCountStartBatchCommand = new CycleCountStartBatchCommand(
                requests: cycleCountStartCommands,
                facility: mockedLocation
        )

        when:
        controller.startCycleCount(cycleCountStartBatchCommand)

        then:
        response.status == HttpStatus.SC_OK
        response.contentType.contains(contentType)

        where:
        outputFormat || contentType
        'json'       || JSON_FORMAT
        'xls'        || XLS_FORMAT
        'pdf'        || PDF_FORMAT
    }

    void 'startRecount should return cycleCounts in #contentType contentType when the #outputFormat format is used'() {
        given:
        BatchCommandUtils.metaClass.static.validateBatch = { Object command, String batchPropertyName ->
            return null
        }
        List<CycleCountStartRecountCommand> cycleCountStartRecountCommands = [
                Mock(CycleCountStartRecountCommand),
                Mock(CycleCountStartRecountCommand),
        ]
        controller.params.facilityId = FACILITY_ID
        controller.params.format = outputFormat
        Location.metaClass.static.findById = {
            String id -> Mock(Location)
        }
        Location mockedLocation = Stub(Location)
        mockedLocation.name = LOCATION_NAME
        CycleCountStartRecountBatchCommand cycleCountStarRecountBatchCommand = new CycleCountStartRecountBatchCommand(
                requests: cycleCountStartRecountCommands,
                facility: mockedLocation
        )

        when:
        controller.startRecount(cycleCountStarRecountBatchCommand)

        then:
        response.status == HttpStatus.SC_OK
        response.contentType.contains(contentType)

        where:
        outputFormat || contentType
        'json'       || JSON_FORMAT
        'xls'        || XLS_FORMAT
        'pdf'        || PDF_FORMAT
    }
}
