package org.pih.warehouse.exporter

import org.springframework.context.ApplicationContext
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import testutil.MessageLocalizerStub

import org.pih.warehouse.core.formatter.DefaultTypeFormatter
import org.pih.warehouse.core.formatter.Formatter
import org.pih.warehouse.core.formatter.FormatterContext
import org.pih.warehouse.core.http.ContentType

@Unroll
class CsvWriterSpec extends Specification {

    private static final String STUB_STRING_FORMATTER_RESULT = "FORMATTED!"

    @Shared
    CsvWriter writer

    void setup() {
        ApplicationContext applicationContextStub = Stub(ApplicationContext) {
            getBean(StubStringFormatter) >> new StubStringFormatter()
        }

        writer = new CsvWriter(
                applicationContextStub,
                Stub(BulkDataExportComponentResolver),
                // We're not testing the DefaultTypeFormatter so let it always fail to find a formatter.
                new DefaultTypeFormatter(Optional.empty()),
                MessageLocalizerStub.MESSAGE_LOCALIZER_STUB)
    }

    void "write succeeds with default formatters and no columnIndex specified for fields"() {
        given:
        List<Map> toWrite = [
                [string: "A", integer: 1],
                [string: "B", integer: 2],
        ]

        and: "a valid config"
        CsvWriterConfig config = initConfig(true, new CsvWriterConfig(
                delimiter: ",",
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
        BulkDataWriterResult<String> result = writer.write(toWrite, ContentType.CSV, config)

        then:
        assert result.result == "﻿" +
                "String Header,Integer Header\r\n" +
                "A,1\r\n" +
                "B,2\r\n"
    }

    void "write succeeds when we exclude the header row"() {
        given:
        List<Map> toWrite = [
                [string: "A", integer: 1],
                [string: "B", integer: 2],
        ]

        and: "a valid config"
        CsvWriterConfig config = initConfig(true, new CsvWriterConfig(
                delimiter: ",",
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
        BulkDataWriterResult<String> result = writer.write(toWrite, ContentType.CSV, config)

        then:
        assert result.result == "﻿" +
                "A,1\r\n" +
                "B,2\r\n"
    }

    void "write can handle a '#delimiter' delimiter"() {
        given:
        List<Map> toWrite = [
                [string: "A", integer: 1],
                [string: "B", integer: 2],
        ]

        and: "a valid config"
        CsvWriterConfig config = initConfig(true, new CsvWriterConfig(
                delimiter: delimiter,
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
        BulkDataWriterResult<String> result = writer.write(toWrite, ContentType.CSV, config)

        then:
        assert result.result == "﻿" +
                "String Header${delimiter}Integer Header\r\n" +
                "A${delimiter}1\r\n" +
                "B${delimiter}2\r\n"

        where:
        delimiter << ["|", ";", "\t", "‍"]
    }

    void "write correctly orders fields based on config"() {
        given:
        List<Map> toWrite = [
                [string: "A", integer: 1],
                [string: "B", integer: 2],
        ]

        and: "a valid config but the fields are defined out of order"
        CsvWriterConfig config = initConfig(true, new CsvWriterConfig(
                delimiter: ",",
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
        BulkDataWriterResult<String> result = writer.write(toWrite, ContentType.CSV, config)

        then:
        assert result.result == "﻿" +
                "Integer Header,String Header\r\n" +
                "1,A\r\n" +
                "2,B\r\n"
    }

    void "write succeeds with a custom formatter"() {
        given:
        List<Map> toWrite = [
                [string: "A", integer: 1],
                [string: "B", integer: 2],
        ]

        and: "a valid config"
        CsvWriterConfig config = initConfig(true, new CsvWriterConfig(
                delimiter: ",",
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
        BulkDataWriterResult<String> result = writer.write(toWrite, ContentType.CSV, config)

        then:
        assert result.result == "﻿" +
                "String Header,Integer Header\r\n" +
                "${STUB_STRING_FORMATTER_RESULT},1\r\n" +
                "${STUB_STRING_FORMATTER_RESULT},2\r\n"
    }

    void "write succeeds for unicode Strings"() {
        given:
        List<Map> toWrite = [
                [string: "苹果"],
                [string: "jabłko"],
        ]

        and: "a valid config"
        CsvWriterConfig config = initConfig(true, new CsvWriterConfig(
                addHeaderRow: true,
                fields: [
                        new BulkDataWriterFieldConfig(
                                fieldName: "string",
                                headerPlainText: "String Header",
                        )
                ],
        ))

        when:
        BulkDataWriterResult<String> result = writer.write(toWrite, ContentType.CSV, config)

        then:
        assert result.result == "﻿" +
                "String Header\r\n" +
                "苹果\r\n" +
                "jabłko\r\n"
    }

    void "write succeeds with an empty dataset"() {
        given:
        List<Map> toWrite = []

        and: "a valid config"
        CsvWriterConfig config = initConfig(true, new CsvWriterConfig(
                addHeaderRow: true,
                fields: [
                        new BulkDataWriterFieldConfig(
                                fieldName: "string",
                                headerPlainText: "String Header",
                        ),
                ],
        ))

        when:
        BulkDataWriterResult<String> result = writer.write(toWrite, ContentType.CSV, config)

        then:
        assert result.result == "﻿" +
                "String Header\r\n"
    }

    void "write succeeds with an empty dataset and no header"() {
        given:
        List<Map> toWrite = []

        and: "a valid config"
        CsvWriterConfig config = initConfig(true, new CsvWriterConfig(
                addHeaderRow: false,
                fields: [
                        new BulkDataWriterFieldConfig(fieldName: "string"),
                ],
        ))

        when:
        BulkDataWriterResult<String> result = writer.write(toWrite, ContentType.CSV, config)

        then:
        assert result.result == "﻿"
    }

    void "write succeeds with no field config"() {
        given:
        List<Map> toWrite = [
                [string: "A", integer: 1],
                [string: "B", integer: 2],
        ]

        and: "a valid config"
        CsvWriterConfig config = initConfig(true, new CsvWriterConfig(
                delimiter: ",",
                addHeaderRow: true,
                fields: [],  // By not defining the fields, it will use the field names as the header
        ))

        when:
        BulkDataWriterResult<String> result = writer.write(toWrite, ContentType.CSV, config)

        then:
        assert result.result == "﻿" +
                "string,integer\r\n" +
                "A,1\r\n" +
                "B,2\r\n"
    }

    void "write succeeds with localized headers"() {
        given:
        List<Map> toWrite = [
                [string: "A", integer: 1],
                [string: "B", integer: 2],
        ]

        and: "a valid config"
        CsvWriterConfig config = initConfig(true, new CsvWriterConfig(
                delimiter: ",",
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
        BulkDataWriterResult<String> result = writer.write(toWrite, ContentType.CSV, config)

        then:
        assert result.result == "﻿" +
                "some.code,Integer Header\r\n" +
                "A,1\r\n" +
                "B,2\r\n"
    }

    void "write fails for an unsupported content type"() {
        given:
        List<Map> toWrite = [
                [string: "A"],
        ]

        and: "a valid config"
        CsvWriterConfig config = initConfig(true, new CsvWriterConfig(
                fields: [
                        new BulkDataWriterFieldConfig(fieldName: "string"),
                ],
        ))

        when:
        writer.write(toWrite, ContentType.PDF, config)

        then:
        thrown(IllegalArgumentException)
    }

    private CsvWriterConfig initConfig(boolean isValid, CsvWriterConfig config) {
        CsvWriterConfig configStub = Spy(config)

        // We're not testing the validator, so simply stub its result.
        configStub.validate() >> isValid

        return configStub
    }

    // For testing custom formatters
    static class StubStringFormatter extends Formatter<String, FormatterContext> {
        @Override
        protected String doFormat(String toFormat, FormatterContext context) {
            return STUB_STRING_FORMATTER_RESULT
        }
    }
}
