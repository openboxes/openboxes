package org.pih.warehouse.exporter

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.context.ApplicationContext
import spock.lang.PendingFeature
import spock.lang.Specification
import spock.lang.Unroll
import testutil.MessageLocalizerStub

import org.pih.warehouse.core.session.SessionManager
import org.pih.warehouse.core.formatter.DefaultTypeFormatter
import org.pih.warehouse.core.formatter.Formatter
import org.pih.warehouse.core.formatter.FormatterContext
import org.pih.warehouse.core.http.ContentType
import org.pih.warehouse.core.parser.DoubleParser

@Unroll
class ExcelWriterReviewSpec extends Specification {

    private static final String STUB_NUMBER_FORMATTER_RESULT = "FORMATTED!"

    ExcelWriter writer

    void setup() {
        ApplicationContext applicationContextStub = Stub(ApplicationContext) {
            getBean(StubNumberFormatter) >> new StubNumberFormatter()
        }

        writer = new ExcelWriter(
                applicationContextStub,
                Stub(BulkDataExportComponentResolver),
                // We're not testing the DefaultTypeFormatter so let it always fail to find a formatter.
                new DefaultTypeFormatter(Optional.empty()),
                new DoubleParser(),
                MessageLocalizerStub.MESSAGE_LOCALIZER_STUB,
                Stub(SessionManager) { getTimezone() >> TimeZone.getDefault() })
    }

    void "write succeeds for content type XLS"() {
        expect:
        assertWritesWorkbook(ContentType.XLS, HSSFWorkbook)
    }

    // XLSX is not supported until we upgrade Apache POI (see the TODO in ExcelWriter.getSupportedContentTypes).
    // The bug is that ConfiguresExcelWriter advertises XLSX anyway, which BulkDataImportConfigurerSpec covers.
    // Remove @PendingFeature once the POI upgrade adds an XLSX writer.
    @PendingFeature
    void "write succeeds for content type XLSX"() {
        expect:
        assertWritesWorkbook(ContentType.XLSX, XSSFWorkbook)
    }

    void "write keeps Excel column order when the config uses columns beyond Z"() {
        given: "27 columns, A through AA, defined in their natural order"
        List<String> columnLetters = ('A'..'Z').collect { it } + ['AA']
        Map<String, Object> row = columnLetters.collectEntries { [("field${it}".toString()): it] }

        and: "a valid config"
        ExcelWriterConfig config = initConfig(new ExcelWriterConfig(
                fields: columnLetters.collect {
                    new BulkDataWriterFieldConfig(
                            fieldName: "field${it}",
                            headerPlainText: "Header ${it}",
                            columnIndex: it,
                    )
                },
        ))

        when:
        Workbook workbook = writer.write([row], ContentType.XLS, config).result

        then: "column AA stays after Z, matching the column mapping that the import uses"
        Sheet sheet = workbook.getSheetAt(0)
        assert cellsOf(sheet.getRow(0)) == columnLetters.collect { "Header ${it}".toString() }
        assert cellsOf(sheet.getRow(1)) == columnLetters

        cleanup:
        workbook?.close()
    }

    void "write gives date cells a date format so Excel displays them as dates"() {
        given:
        Date date = Date.parse("yyyy-MM-dd", "2025-03-01")
        List<Map> toWrite = [
                [date: date],
        ]

        and: "a valid config"
        ExcelWriterConfig config = initConfig(new ExcelWriterConfig(
                fields: [
                        new BulkDataWriterFieldConfig(fieldName: "date", headerPlainText: "Date Header"),
                ],
        ))

        when:
        Workbook workbook = writer.write(toWrite, ContentType.XLS, config).result

        then: "without a date format, Excel shows the cell's raw serial number (45717) instead of the date"
        Cell cell = workbook.getSheetAt(0).getRow(1).getCell(0)
        assert DateUtil.isCellDateFormatted(cell)
        assert cell.dateCellValue == date

        cleanup:
        workbook?.close()
    }

    void "write uses the field's custom formatter for #description values"() {
        given:
        List<Map> toWrite = [
                [value: value],
        ]

        and: "a valid config with a custom formatter on the field"
        ExcelWriterConfig config = initConfig(new ExcelWriterConfig(
                fields: [
                        new BulkDataWriterFieldConfig(
                                fieldName: "value",
                                headerPlainText: "Value Header",
                                formatter: StubNumberFormatter,
                                formatterContext: null,  // Context doesn't matter here since the formatter is stubbed
                        ),
                ],
        ))

        when:
        Workbook workbook = writer.write(toWrite, ContentType.XLS, config).result

        then: "the Excel export honours the formatter, the same as CsvWriter does"
        assert cellsOf(workbook.getSheetAt(0).getRow(1)) == [STUB_NUMBER_FORMATTER_RESULT]

        cleanup:
        workbook?.close()

        where:
        description  | value
        "BigDecimal" | new BigDecimal("12.50")
        "Integer"    | 7
    }

    // Separate scope from the PR's fixes: Excel stores every number as a double, so writing large values intact
    // needs a decision on whether to write them as text. Remove @PendingFeature once that is decided and fixed.
    @PendingFeature
    void "write does not lose precision on #description values"() {
        given:
        List<Map> toWrite = [
                [value: value],
        ]

        and: "a valid config"
        ExcelWriterConfig config = initConfig(new ExcelWriterConfig(
                fields: [
                        new BulkDataWriterFieldConfig(fieldName: "value", headerPlainText: "Value Header"),
                ],
        ))

        when:
        Workbook workbook = writer.write(toWrite, ContentType.XLS, config).result

        then:
        assert cellsOf(workbook.getSheetAt(0).getRow(1)) == [expected]

        cleanup:
        workbook?.close()

        where:
        description              | value                                  || expected
        "Long beyond 2^53"       | 9007199254740993L                      || "9007199254740993"
        "high-scale BigDecimal" | new BigDecimal("1234567890.123456789") || "1234567890.123456789"
    }

    /**
     * @return The cells of the given row, as Excel would display them.
     */
    private static List<String> cellsOf(Row row) {
        DataFormatter dataFormatter = new DataFormatter()
        return row.cellIterator().collect { dataFormatter.formatCellValue(it) }
    }

    private void assertWritesWorkbook(ContentType contentType, Class<? extends Workbook> expectedWorkbookType) {
        ExcelWriterConfig config = initConfig(new ExcelWriterConfig(
                fields: [
                        new BulkDataWriterFieldConfig(fieldName: "string", headerPlainText: "String Header"),
                ],
        ))

        Workbook workbook = writer.write([[string: "A"]], contentType, config).result
        try {
            assert expectedWorkbookType.isInstance(workbook)
            assert cellsOf(workbook.getSheetAt(0).getRow(1)) == ["A"]
        } finally {
            workbook?.close()
        }
    }

    private ExcelWriterConfig initConfig(ExcelWriterConfig config) {
        ExcelWriterConfig configStub = Spy(config)

        // We're not testing the validator, so simply stub its result. ExcelWriterConfigValidationSpec covers it.
        configStub.validate() >> true

        return configStub
    }

    // For testing custom formatters
    static class StubNumberFormatter extends Formatter<Number, FormatterContext> {
        @Override
        protected String doFormat(Number toFormat, FormatterContext context) {
            return STUB_NUMBER_FORMATTER_RESULT
        }
    }
}
