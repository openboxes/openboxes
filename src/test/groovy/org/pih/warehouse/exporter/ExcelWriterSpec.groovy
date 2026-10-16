package org.pih.warehouse.exporter

import java.time.LocalDate
import java.time.ZonedDateTime
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.springframework.context.ApplicationContext
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import testutil.MessageLocalizerStub

import org.pih.warehouse.core.ConfigService
import org.pih.warehouse.core.date.InstantParser
import org.pih.warehouse.core.date.JavaUtilDateParser
import org.pih.warehouse.core.formatter.DefaultTypeFormatter
import org.pih.warehouse.core.formatter.Formatter
import org.pih.warehouse.core.formatter.FormatterContext
import org.pih.warehouse.core.http.ContentType
import org.pih.warehouse.core.parser.DoubleParser
import org.pih.warehouse.core.session.SessionManager

@Unroll
class ExcelWriterSpec extends Specification {

    private static final String STUB_STRING_FORMATTER_RESULT = "FORMATTED!"
    private static final TimeZone USER_TIMEZONE = TimeZone.getTimeZone("Asia/Kolkata")  // +05:30

    private static final double EXCEL_2000_01_02 = 36527d
    private static final double EXCEL_2000_01_02_NOON = 36527.5d
    private static final double EXCEL_2000_07_02_NOON = 36709.5d
    private static final double EXCEL_2018_11_04 = 43408d

    @Shared
    ExcelWriter writer

    @Shared
    SessionManager sessionManagerStub

    @Shared
    ConfigService configServiceStub

    // Tests can override this to change the timezone of the requesting user.
    TimeZone userTimezone = USER_TIMEZONE

    // The server timezone at the start of the test, so that we can restore it if a test changes it.
    TimeZone originalServerTimezone = TimeZone.getDefault()

    void setup() {
        ApplicationContext applicationContextStub = Stub(ApplicationContext) {
            getBean(StubStringFormatter) >> new StubStringFormatter()
        }

        sessionManagerStub = Stub(SessionManager) {
            getTimezone() >> { userTimezone }
        }

        configServiceStub = Stub(ConfigService) {
            getProperty("openboxes.export.defaultSheetName") >> "Sheet1"
        }

        writer = new ExcelWriter(
                applicationContextStub,
                Stub(BulkDataExportComponentResolver),
                // We're not testing the DefaultTypeFormatter so let it always fail to find a formatter.
                new DefaultTypeFormatter(Optional.empty()),
                new DoubleParser(),
                MessageLocalizerStub.MESSAGE_LOCALIZER_STUB,
                sessionManagerStub,
                configServiceStub)
    }

    void cleanup() {
        TimeZone.setDefault(originalServerTimezone)
    }

    void "write succeeds with default formatters and no columnIndex specified for fields"() {
        given:
        List<Map> toWrite = [
                [string: "A", integer: 1],
                [string: "B", integer: 2],
        ]

        and: "a valid config"
        ExcelWriterConfig config = initConfig(true, new ExcelWriterConfig(
                addHeaderRow: true,
                fields: [
                        new BulkDataWriterFieldConfig(
                                fieldName: "string",
                                headerPlainText: "String Header",
                        ),
                        new BulkDataWriterFieldConfig(
                                fieldName: "integer",
                                headerPlainText: "Integer Header",
                        ),
                ],
        ))

        when:
        BulkDataWriterResult<Workbook> result = writer.write(toWrite, ContentType.XLS, config)

        then:
        assert readRows(result) == [
                ["String Header", "Integer Header"],
                ["A", 1d],
                ["B", 2d],
        ]
    }

    void "write succeeds when we exclude the header row"() {
        given:
        List<Map> toWrite = [
                [string: "A", integer: 1],
                [string: "B", integer: 2],
        ]

        and: "a valid config"
        ExcelWriterConfig config = initConfig(true, new ExcelWriterConfig(
                addHeaderRow: false,
                fields: [
                        new BulkDataWriterFieldConfig(
                                fieldName: "string",
                                headerPlainText: "String Header",
                        ),
                        new BulkDataWriterFieldConfig(
                                fieldName: "integer",
                                headerPlainText: "Integer Header",
                        ),
                ],
        ))

        when:
        BulkDataWriterResult<Workbook> result = writer.write(toWrite, ContentType.XLS, config)

        then:
        assert readRows(result) == [
                ["A", 1d],
                ["B", 2d],
        ]
    }

    void "write correctly orders fields based on config"() {
        given:
        List<Map> toWrite = [
                [string: "A", integer: 1],
                [string: "B", integer: 2],
        ]

        and: "a valid config but the fields are defined out of order"
        ExcelWriterConfig config = initConfig(true, new ExcelWriterConfig(
                addHeaderRow: true,
                fields: [
                        new BulkDataWriterFieldConfig(
                                fieldName: "string",
                                headerPlainText: "String Header",
                                columnIndex: 1,  // Is first in the list but has index 1. columnIndex will be used
                        ),
                        new BulkDataWriterFieldConfig(
                                fieldName: "integer",
                                headerPlainText: "Integer Header",
                                columnIndex: 0,  // Is second in the list but has index 0. columnIndex will be used
                        ),
                ],
        ))

        when:
        BulkDataWriterResult<Workbook> result = writer.write(toWrite, ContentType.XLS, config)

        then:
        assert readRows(result) == [
                ["Integer Header", "String Header"],
                [1d, "A"],
                [2d, "B"],
        ]
    }

    void "write orders fields by columnIndex for #scenario indexes"() {
        given:
        List<Map> toWrite = [
                declaredIndexes.collectEntries { [(it): "value ${it}"] },
        ]

        and: "a valid config with the fields defined out of order"
        ExcelWriterConfig config = initConfig(true, new ExcelWriterConfig(
                addHeaderRow: true,
                fields: declaredIndexes.collect {
                    new BulkDataWriterFieldConfig(fieldName: it, headerPlainText: it, columnIndex: it)
                },
        ))

        when:
        BulkDataWriterResult<Workbook> result = writer.write(toWrite, ContentType.XLS, config)

        then: "columns are ordered by their numerical index, not alphabetically"
        assert readRows(result) == [
                expectedIndexes,
                expectedIndexes.collect { "value ${it}" },
        ]

        where:
        scenario    | declaredIndexes                  || expectedIndexes
        "letter"    | ["AA", "B", "Z", "A", "AB"]      || ["A", "B", "Z", "AA", "AB"]
        "lowercase" | ["aa", "b", "a"]                 || ["a", "b", "aa"]
        "numeric"   | ["10", "2", "0", "1", "11"]      || ["0", "1", "2", "10", "11"]
    }

    void "write succeeds with a custom formatter"() {
        given:
        List<Map> toWrite = [
                [string: "A", integer: 1],
                [string: "B", integer: 2],
        ]

        and: "a valid config"
        ExcelWriterConfig config = initConfig(true, new ExcelWriterConfig(
                addHeaderRow: true,
                fields: [
                        new BulkDataWriterFieldConfig(
                                fieldName: "string",
                                headerPlainText: "String Header",
                                formatter: StubStringFormatter,  // A custom formatter
                                formatterContext: null,  // Context doesn't matter here since the formatter is stubbed
                        ),
                        new BulkDataWriterFieldConfig(
                                fieldName: "integer",
                                headerPlainText: "Integer Header",
                        ),
                ],
        ))

        when:
        BulkDataWriterResult<Workbook> result = writer.write(toWrite, ContentType.XLS, config)

        then:
        assert readRows(result) == [
                ["String Header", "Integer Header"],
                [STUB_STRING_FORMATTER_RESULT, 1d],
                [STUB_STRING_FORMATTER_RESULT, 2d],
        ]
    }

    void "write succeeds for unicode Strings"() {
        given:
        List<Map> toWrite = [
                [string: "苹果"],
                [string: "jabłko"],
        ]

        and: "a valid config"
        ExcelWriterConfig config = initConfig(true, new ExcelWriterConfig(
                addHeaderRow: true,
                fields: [
                        new BulkDataWriterFieldConfig(
                                fieldName: "string",
                                headerPlainText: "String Header",
                        )
                ],
        ))

        when:
        BulkDataWriterResult<Workbook> result = writer.write(toWrite, ContentType.XLS, config)

        then:
        assert readRows(result) == [
                ["String Header"],
                ["苹果"],
                ["jabłko"],
        ]
    }

    void "write maps #scenario values to the correct Excel cell type"() {
        given:
        List<Map> toWrite = [
                [value: value],
        ]

        and: "a valid config"
        ExcelWriterConfig config = initConfig(true, new ExcelWriterConfig(
                addHeaderRow: false,
                fields: [
                        new BulkDataWriterFieldConfig(fieldName: "value"),
                ],
        ))

        when:
        BulkDataWriterResult<Workbook> result = writer.write(toWrite, ContentType.XLS, config)

        then:
        Workbook workbook = reopenWorkbook(result)
        assert getCellValue(workbook.getSheetAt(0).getRow(0).getCell(0)) == expectedValue

        cleanup:
        workbook?.close()

        where:
        scenario         | value        || expectedValue
        "null"           | null         || ""
        "String"         | "A"          || "A"
        "Integer"        | 1            || 1d
        "Long"           | 10000000000L || 10000000000d
        "BigDecimal"     | 1.25G        || 1.25d
        "Double"         | -1.5d        || -1.5d
        "Boolean"        | true         || true
    }

    void "write maps #scenario values to the correct Excel cell type and format"() {
        given:
        List<Map> toWrite = [
                [value: value],
        ]

        and: "a valid config"
        ExcelWriterConfig config = initConfig(true, new ExcelWriterConfig(
                addHeaderRow: false,
                fields: [
                        new BulkDataWriterFieldConfig(fieldName: "value"),
                ],
        ))

        when:
        BulkDataWriterResult<Workbook> result = writer.write(toWrite, ContentType.XLS, config)

        then:
        Workbook workbook = reopenWorkbook(result)
        Cell cell = workbook.getSheetAt(0).getRow(0).getCell(0)
        assert getCellValue(cell) == expectedValue

        and: "Excel uses the date formats that we set in ExcelCellStyles"
        assert cell.cellStyle.dataFormatString == expectedFormat

        and: "Excel recognizes the cell as a date"
        assert DateUtil.isCellDateFormatted(cell)

        cleanup:
        workbook?.close()

        where:
        scenario           | value                                                  || expectedValue         | expectedFormat
        // Date-only fields. No timezone conversion required.
        "LocalDate"        | LocalDate.parse("2000-01-02")                          || EXCEL_2000_01_02      | "yyyy-mm-dd"
        // Date + time fields. Are converted to the requesting user's timezone (which is +05:30)
        "Instant Z"        | InstantParser.asInstant("2000-01-02T06:30:00Z")        || EXCEL_2000_01_02_NOON | "yyyy-mm-dd hh:mm:ss"
        "Instant +1"       | InstantParser.asInstant("2000-01-02T07:30:00+01:00")   || EXCEL_2000_01_02_NOON | "yyyy-mm-dd hh:mm:ss"
        "Instant -1"       | InstantParser.asInstant("2000-01-02T05:30:00-01:00")   || EXCEL_2000_01_02_NOON | "yyyy-mm-dd hh:mm:ss"
        "ZonedDateTime Z"  | ZonedDateTime.parse("2000-01-02T06:30:00Z")            || EXCEL_2000_01_02_NOON | "yyyy-mm-dd hh:mm:ss"
        "ZonedDateTime +1" | ZonedDateTime.parse("2000-01-02T07:30:00+01:00")       || EXCEL_2000_01_02_NOON | "yyyy-mm-dd hh:mm:ss"
        "ZonedDateTime -1" | ZonedDateTime.parse("2000-01-02T05:30:00-01:00")       || EXCEL_2000_01_02_NOON | "yyyy-mm-dd hh:mm:ss"
        "Date Z"           | JavaUtilDateParser.asDate("2000-01-02T06:30:00Z")      || EXCEL_2000_01_02_NOON | "yyyy-mm-dd hh:mm:ss"
        "Date +1"          | JavaUtilDateParser.asDate("2000-01-02T07:30:00+01:00") || EXCEL_2000_01_02_NOON | "yyyy-mm-dd hh:mm:ss"
        "Date -1"          | JavaUtilDateParser.asDate("2000-01-02T05:30:00-01:00") || EXCEL_2000_01_02_NOON | "yyyy-mm-dd hh:mm:ss"
    }

    void "write converts #scenario to the user's timezone, not the server's"() {
        given: "a server timezone that differs from the user's timezone"
        TimeZone.setDefault(TimeZone.getTimeZone(serverTimezone))
        userTimezone = TimeZone.getTimeZone(userTimezoneId)

        and:
        List<Map> toWrite = [
                [value: value],
        ]

        and: "a valid config"
        ExcelWriterConfig config = initConfig(true, new ExcelWriterConfig(
                addHeaderRow: false,
                fields: [
                        new BulkDataWriterFieldConfig(fieldName: "value"),
                ],
        ))

        when:
        BulkDataWriterResult<Workbook> result = writer.write(toWrite, ContentType.XLS, config)

        then:
        assert readRows(result) == [
                [expectedValue],
        ]

        where:
        scenario                                    | serverTimezone        | userTimezoneId     | value                                             || expectedValue
        // The server timezone should never impact the result. It should always be in the user's timezone.
        "an Instant with a server in UTC-08"        | "America/Los_Angeles" | "Asia/Kolkata"     | InstantParser.asInstant("2000-01-02T06:30:00Z")   || EXCEL_2000_01_02_NOON
        "a ZonedDateTime with a server in UTC-08"   | "America/Los_Angeles" | "Asia/Kolkata"     | ZonedDateTime.parse("2000-01-02T06:30:00Z")       || EXCEL_2000_01_02_NOON
        "a Date with a server in UTC-08"            | "America/Los_Angeles" | "Asia/Kolkata"     | JavaUtilDateParser.asDate("2000-01-02T06:30:00Z") || EXCEL_2000_01_02_NOON
        "a LocalDate with a server in UTC-08"       | "America/Los_Angeles" | "Asia/Kolkata"     | LocalDate.parse("2000-01-02")                     || EXCEL_2000_01_02
        "a LocalDate with a server in UTC+14"       | "Pacific/Kiritimati"  | "Pacific/Pago_Pago"| LocalDate.parse("2000-01-02")                     || EXCEL_2000_01_02
        // A datetime that falls on a different day in UTC than it does for the user.
        "an Instant on the previous day in UTC"     | "UTC"                 | "Asia/Kolkata"     | InstantParser.asInstant("2000-01-01T18:30:00Z")   || EXCEL_2000_01_02
        "a ZonedDateTime on the previous day in -1" | "UTC"                 | "Asia/Kolkata"     | ZonedDateTime.parse("2000-01-01T17:30:00-01:00")  || EXCEL_2000_01_02
        "a Date on the next day in UTC"             | "UTC"                 | "Etc/GMT+12"       | JavaUtilDateParser.asDate("2000-01-03T00:00:00Z") || EXCEL_2000_01_02_NOON
        // A user in a timezone that observes daylight savings should get the offset that applied at that datetime.
        "an Instant in winter (EST, -05:00)"        | "UTC"                 | "America/New_York" | InstantParser.asInstant("2000-01-02T17:00:00Z")   || EXCEL_2000_01_02_NOON
        "an Instant in summer (EDT, -04:00)"        | "UTC"                 | "America/New_York" | InstantParser.asInstant("2000-07-02T16:00:00Z")   || EXCEL_2000_07_02_NOON
        "a Date in summer (EDT, -04:00)"            | "UTC"                 | "America/New_York" | JavaUtilDateParser.asDate("2000-07-02T16:00:00Z") || EXCEL_2000_07_02_NOON
        // Sao Paulo's DST started at midnight on 2018-11-04, so midnight didn't exist that day. LocalDates should still be written as exactly midnight, and not as 01:00.
        "a LocalDate that skips midnight due to DST" | "America/Sao_Paulo"  | "America/Sao_Paulo"| LocalDate.parse("2018-11-04")                     || EXCEL_2018_11_04
    }

    void "write succeeds with an empty dataset"() {
        given:
        List<Map> toWrite = []

        and: "a valid config"
        ExcelWriterConfig config = initConfig(true, new ExcelWriterConfig(
                addHeaderRow: true,
                fields: [
                        new BulkDataWriterFieldConfig(
                                fieldName: "string",
                                headerPlainText: "String Header",
                        ),
                ],
        ))

        when:
        BulkDataWriterResult<Workbook> result = writer.write(toWrite, ContentType.XLS, config)

        then:
        assert readRows(result) == [
                ["String Header"],
        ]
    }

    void "write sets a custom sheet name"() {
        given:
        List<Map> toWrite = []

        and: "a valid config"
        ExcelWriterConfig config = initConfig(true, new ExcelWriterConfig(
                addHeaderRow: false,
                fields: [],
                sheetName: "OVERRIDE"
        ))

        when:
        BulkDataWriterResult<Workbook> result = writer.write(toWrite, ContentType.XLS, config)

        then:
        assert result.result.getSheet("OVERRIDE") != null
    }

    void "write succeeds with an empty dataset and no header"() {
        given:
        List<Map> toWrite = []

        and: "a valid config"
        ExcelWriterConfig config = initConfig(true, new ExcelWriterConfig(
                addHeaderRow: false,
                fields: [
                        new BulkDataWriterFieldConfig(fieldName: "string"),
                ],
        ))

        when:
        BulkDataWriterResult<Workbook> result = writer.write(toWrite, ContentType.XLS, config)

        then:
        assert readRows(result) == []
    }

    void "write succeeds with no field config"() {
        given:
        List<Map> toWrite = [
                [string: "A", integer: 1],
                [string: "B", integer: 2],
        ]

        and: "a valid config"
        ExcelWriterConfig config = initConfig(true, new ExcelWriterConfig(
                addHeaderRow: true,
                fields: [],  // By not defining the fields, it will use the field names as the header
        ))

        when:
        BulkDataWriterResult<Workbook> result = writer.write(toWrite, ContentType.XLS, config)

        then:
        assert readRows(result) == [
                ["string", "integer"],
                ["A", 1d],
                ["B", 2d],
        ]
    }

    void "write succeeds with localized headers"() {
        given:
        List<Map> toWrite = [
                [string: "A", integer: 1],
                [string: "B", integer: 2],
        ]

        and: "a valid config"
        ExcelWriterConfig config = initConfig(true, new ExcelWriterConfig(
                addHeaderRow: true,
                fields: [
                        new BulkDataWriterFieldConfig(
                                fieldName: "string",
                                // The exact value doesn't matter since the localizer is stubbed.
                                headerMessageCode: "some.code",
                        ),
                        new BulkDataWriterFieldConfig(
                                fieldName: "integer",
                                headerPlainText: "Integer Header",
                        ),
                ],
        ))

        when:
        BulkDataWriterResult<Workbook> result = writer.write(toWrite, ContentType.XLS, config)

        then:
        assert readRows(result) == [
                ["some.code", "Integer Header"],
                ["A", 1d],
                ["B", 2d],
        ]
    }

    void "write uses the configured sheet name"() {
        given:
        List<Map> toWrite = [
                [string: "A"],
        ]

        and: "a valid config"
        ExcelWriterConfig config = initConfig(true, new ExcelWriterConfig(
                sheetName: "My Sheet",
                addHeaderRow: false,
                fields: [
                        new BulkDataWriterFieldConfig(fieldName: "string"),
                ],
        ))

        when:
        BulkDataWriterResult<Workbook> result = writer.write(toWrite, ContentType.XLS, config)

        then:
        assert readRows(result, "My Sheet") == [
                ["A"],
        ]
    }

    void "write styles the header row when using the #excelStyle style"() {
        given:
        List<Map> toWrite = [
                [string: "A"],
        ]

        and: "a valid config"
        ExcelWriterConfig config = initConfig(true, new ExcelWriterConfig(
                excelStyle: excelStyle,
                addHeaderRow: true,
                fields: [
                        new BulkDataWriterFieldConfig(fieldName: "string"),
                ],
        ))

        when:
        BulkDataWriterResult<Workbook> result = writer.write(toWrite, ContentType.XLS, config)

        then:
        Workbook workbook = reopenWorkbook(result)
        Sheet sheet = workbook.getSheetAt(0)
        assert isBold(workbook, sheet.getRow(0).getCell(0)) == expectHeaderStyled
        assert !isBold(workbook, sheet.getRow(1).getCell(0))

        cleanup:
        workbook?.close()

        where:
        excelStyle        || expectHeaderStyled
        ExcelStyle.PLAIN  || false
        ExcelStyle.TABLE  || true
    }

    void "write fails for unsupported content type #contentType"() {
        given:
        List<Map> toWrite = [
                [string: "A"],
        ]

        and: "a valid config"
        ExcelWriterConfig config = initConfig(true, new ExcelWriterConfig(
                fields: [
                        new BulkDataWriterFieldConfig(fieldName: "string"),
                ],
        ))

        when:
        writer.write(toWrite, ContentType.PDF, config)

        then:
        thrown(IllegalArgumentException)
    }

    private ExcelWriterConfig initConfig(boolean isValid, ExcelWriterConfig config) {
        ExcelWriterConfig configStub = Spy(config)

        // We're not testing the validator, so simply stub its result.
        configStub.validate() >> isValid

        return configStub
    }

    /**
     * Writes the resulting workbook to bytes and reads it back in. This guarantees that we test on the actual file
     * contents and not just what POI handles in memory.
     *
     * Closes the original workbook so the caller is responsible for closing the new, returned one.
     */
    private static Workbook reopenWorkbook(BulkDataWriterResult<Workbook> result) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        try {
            result.result.write(outputStream)
        } finally {
            result.close()
        }
        return new HSSFWorkbook(new ByteArrayInputStream(outputStream.toByteArray()))
    }

    /**
     * @return The contents of the given sheet as a list of rows, where each row is a list of cell values.
     *         Numeric cells (including dates) are returned as doubles, mirroring how Excel stores them.
     */
    private static List<List<Object>> readRows(BulkDataWriterResult<Workbook> result, String sheetName="Sheet1") {
        Workbook workbook = reopenWorkbook(result)
        try {
            Sheet sheet = workbook.getSheet(sheetName)
            assert sheet != null
            return sheet.collect { Row row -> row.collect { Cell cell -> getCellValue(cell) } }
        } finally {
            workbook.close()
        }
    }

    private static Object getCellValue(Cell cell) {
        switch (cell.cellTypeEnum) {
            case CellType.STRING:
                return cell.stringCellValue
            case CellType.NUMERIC:
                return cell.numericCellValue
            case CellType.BOOLEAN:
                return cell.booleanCellValue
            case CellType.BLANK:
                return null
            default:
                throw new IllegalStateException("Unexpected cell type ${cell.cellTypeEnum}")
        }
    }

    private static boolean isBold(Workbook workbook, Cell cell) {
        return workbook.getFontAt(cell.cellStyle.fontIndex).bold
    }

    // For testing custom formatters
    static class StubStringFormatter extends Formatter<String, FormatterContext> {
        @Override
        protected String doFormat(String toFormat, FormatterContext context) {
            return STUB_STRING_FORMATTER_RESULT
        }
    }
}
