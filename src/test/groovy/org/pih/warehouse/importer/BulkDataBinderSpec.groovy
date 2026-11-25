package org.pih.warehouse.importer

import java.time.Instant
import java.time.LocalDate
import org.springframework.context.ApplicationContext
import spock.lang.Specification
import spock.lang.Unroll
import testutil.MessageLocalizerStub

import org.pih.warehouse.core.date.DateFieldType
import org.pih.warehouse.core.date.JavaUtilDateParser
import org.pih.warehouse.core.parser.DefaultTypeParser
import org.pih.warehouse.core.parser.Parser
import org.pih.warehouse.core.parser.ParserContext
import org.pih.warehouse.core.session.SessionManager

@Unroll
class BulkDataBinderSpec extends Specification {

    // Chosen arbitrarily. This will be stubbed so all that matters is that we're consistent.
    private static final BulkDataType BULK_DATA_TYPE = BulkDataType.PERSON
    private static final BulkDataType UNKNOWN_BULK_DATA_TYPE = BulkDataType.CATEGORY
    private static final int VALUE_THAT_FAILS_CUSTOM_BINDING = -1
    private static final int VALUE_THAT_WARNS_CUSTOM_BINDING = -2

    BulkDataBinder bulkDataBinder

    DefaultTypeParser defaultTypeParserStub
    Parser parserStub

    void setup() {
        parserStub = Stub(Parser)
        ApplicationContext contextStub = Stub(ApplicationContext) {
            getBean(_ as Class<Parser>) >> parserStub
        }

        defaultTypeParserStub = Stub(DefaultTypeParser) {
            getDefaultParser(_ as Class<Object>) >> parserStub
        }

        BulkDataImportComponentResolver componentResolverStub = Stub(BulkDataImportComponentResolver) {
            getBulkDataBinderConfigurer(BULK_DATA_TYPE) >> new BulkDataBinderConfigurerForTest()
            getBulkDataBinderConfigurer(UNKNOWN_BULK_DATA_TYPE) >> null
        }

        JavaUtilDateParser javaUtilDateParser = new JavaUtilDateParser(sessionManager: Stub(SessionManager) {
            getTimezone() >> TimeZone.getTimeZone("Asia/Kolkata")  // +05:30
        })

        bulkDataBinder = new BulkDataBinder(
                componentResolverStub,
                defaultTypeParserStub,
                javaUtilDateParser,
                MessageLocalizerStub.MESSAGE_LOCALIZER_STUB,
                contextStub,
        )
    }

    void "bindData should successfully bind data to a strongly-typed object"() {
        given: "the raw data being bound"
        BulkDataType dataImportType = BULK_DATA_TYPE
        BulkDataReaderResult readerResult = new BulkDataReaderResult(rows: [
                [
                        "stringField": new BulkDataCell(row: 0, column: 0, fieldName: "stringField", value: "Hi"),
                        "integerField": new BulkDataCell(row: 0, column: 1, fieldName: "integerField", value: 1)
                ],
        ])

        and: "stubbed values for the parser to return"
        parserStub.parse("Hi", _ as ParserContext) >> "Hi"

        when:
        BulkDataBinderResult result = bulkDataBinder.bindData(dataImportType, readerResult)
        List<ImportableStub> rows = result.boundRows as List<ImportableStub>

        then:
        assert rows.size() == 1
        assert rows[0].stringField == "Hi"
        assert rows[0].integerField == 1

        assert result.bindErrors.size() == 0
    }

    void "bindData binds a java.util.Date date-only field"() {
        given:
        BulkDataReaderResult readerResult = new BulkDataReaderResult(rows: [
                [
                        "integerField": new BulkDataCell(row: 0, column: 1, fieldName: "integerField", value: 1),
                        "dateField": new BulkDataCell(row: 0, column: 2, fieldName: "dateField", value: "2000-01-02"),
                ],
        ])

        when:
        BulkDataBinderResult result = bulkDataBinder.bindData(BULK_DATA_TYPE, readerResult)

        then:
        assert result.bindErrors.size() == 0
        assert (result.boundRows[0] as ImportableStub).dateField ==
                JavaUtilDateParser.asDate(LocalDate.parse("2000-01-02"))
    }

    void "bindData binds a java.util.Date datetime without a timezone in the user's timezone"() {
        given:
        BulkDataReaderResult readerResult = new BulkDataReaderResult(rows: [
                [
                        "integerField": new BulkDataCell(row: 0, column: 1, fieldName: "integerField", value: 1),
                        "dateTimeField": new BulkDataCell(
                                row: 0, column: 3, fieldName: "dateTimeField", value: "2000-01-02 12:00:00"),
                ],
        ])

        when:
        BulkDataBinderResult result = bulkDataBinder.bindData(BULK_DATA_TYPE, readerResult)

        then:
        assert result.bindErrors.size() == 0
        assert (result.boundRows[0] as ImportableStub).dateTimeField == Date.from(Instant.parse("2000-01-02T06:30:00Z"))
    }

    void "bindData errors for a java.util.Date field with no dateFieldType"() {
        given:
        BulkDataReaderResult readerResult = new BulkDataReaderResult(rows: [
                [
                        "integerField": new BulkDataCell(row: 0, column: 1, fieldName: "integerField", value: 1),
                        "dateFieldWithoutStyle": new BulkDataCell(
                                row: 0, column: 4, fieldName: "dateFieldWithoutStyle", value: "2000-01-02"),
                ],
        ])

        when:
        BulkDataBinderResult result = bulkDataBinder.bindData(BULK_DATA_TYPE, readerResult)

        then: "the field is not bound"
        assert (result.boundRows[0] as ImportableStub).dateFieldWithoutStyle == null

        and: "the error explains that the config is missing the style"
        assert result.bindErrors.size() == 1
        assert result.bindErrors[0].exception instanceof IllegalStateException
        assert result.bindErrors[0].exception.message.contains("dateFieldType")
    }

    void "bindData should ignore fields that are not specified in the config"() {
        given: "the raw data being bound"
        BulkDataType dataImportType = BULK_DATA_TYPE
        BulkDataReaderResult readerResult = new BulkDataReaderResult(rows: [
                [
                        "stringField": new BulkDataCell(row: 0, column: 0, fieldName: "stringField", value: "Hi"),
                        "integerField": new BulkDataCell(row: 0, column: 1, fieldName: "integerField", value: 1),
                        // Not in the config!
                        "otherField": new BulkDataCell(row: 0, column: 2, fieldName: "otherField", value: "Ignored"),
                ],
        ])

        and: "stubbed values for the parser to return"
        parserStub.parse("Hi", _ as ParserContext) >> "Hi"

        when:
        BulkDataBinderResult result = bulkDataBinder.bindData(dataImportType, readerResult)
        List<ImportableStub> rows = result.boundRows as List<ImportableStub>

        then: "The row is bound without errors and the non-configured field is ignored"
        assert rows.size() == 1
        assert rows[0].stringField == "Hi"
        assert rows[0].integerField == 1
        assert !rows[0].hasProperty("otherField")

        assert result.bindErrors.size() == 0
    }

    void "bindData should capture parser errors"() {
        given: "the raw data being bound"
        BulkDataType dataImportType = BULK_DATA_TYPE
        BulkDataReaderResult readerResult = new BulkDataReaderResult(rows: [
                [
                        "stringField": new BulkDataCell(row: 0, column: 0, fieldName: "stringField", value: "Hi"),
                        "integerField": new BulkDataCell(row: 0, column: 1, fieldName: "integerField", value: 1)
                ],
        ])

        and: "parsing the stringField will fail"
        parserStub.parse("Hi", _ as ParserContext) >> { throw new RuntimeException("PARSER ERROR") }

        when:
        BulkDataBinderResult result = bulkDataBinder.bindData(dataImportType, readerResult)
        List<ImportableStub> rows = result.boundRows as List<ImportableStub>

        then: "the stringField fails to be bound"
        assert rows.size() == 1
        assert rows[0].stringField == null
        assert rows[0].integerField == 1

        and: "the error is returned as expected"
        assert result.bindErrors.size() == 1
        assert result.bindErrors[0].row == 0
        assert result.bindErrors[0].column == "0"
        assert result.bindErrors[0].fieldName == "stringField"
        assert result.bindErrors[0].severity == BulkDataErrorSeverity.ERROR
        assert result.bindErrors[0].localizedMessage == "import.binder.error"
        assert result.bindErrors[0].exception.message == "PARSER ERROR"
    }

    void "bindData should handle #expectedSeverity when custom bind data"() {
        given: "the raw data being bound"
        BulkDataType dataImportType = BULK_DATA_TYPE
        BulkDataReaderResult readerResult = new BulkDataReaderResult(rows: [
                [
                        "stringField": new BulkDataCell(row: 0, column: 0, fieldName: "stringField", value: "Hi"),
                        "integerField": new BulkDataCell(row: 0, column: 1, fieldName: "integerField", value: integerFieldValue),
                ],
        ])

        and: "stubbed values for the parser to return"
        parserStub.parse("Hi", _ as ParserContext) >> "Hi"

        when:
        BulkDataBinderResult result = bulkDataBinder.bindData(dataImportType, readerResult)
        List<ImportableStub> rows = result.boundRows as List<ImportableStub>

        then: "the fields are still bound, despite the error"
        assert rows.size() == 1
        assert rows[0].stringField == "Hi"
        assert rows[0].integerField == integerFieldValue

        and: "the error is returned as expected"
        assert result.bindErrors.size() == 1
        assert result.bindErrors[0].row == 0
        assert result.bindErrors[0].column == "1"
        assert result.bindErrors[0].fieldName == "integerField"
        assert result.bindErrors[0].localizedMessage == expectedMessage
        assert result.bindErrors[0].severity == expectedSeverity

        where:
        integerFieldValue               || expectedMessage | expectedSeverity
        VALUE_THAT_FAILS_CUSTOM_BINDING || "error.code"    | BulkDataErrorSeverity.ERROR
        VALUE_THAT_WARNS_CUSTOM_BINDING || "warn.code"     | BulkDataErrorSeverity.WARNING
    }

    void "bindData should fail if there is no configurer associated with the given bulk data type"() {
        given: "the raw data being bound"
        BulkDataReaderResult readerResult = new BulkDataReaderResult(rows: [
                [
                        "stringField": new BulkDataCell(row: 0, column: 0, fieldName: "stringField", value: "Hi"),
                        "integerField": new BulkDataCell(row: 0, column: 1, fieldName: "integerField", value: 1)
                ],
        ])

        when: "we try to bind data for an unknown bulk data type (and no custom config override is specified)"
        bulkDataBinder.bindData(UNKNOWN_BULK_DATA_TYPE, readerResult)

        then:
        RuntimeException e = thrown(RuntimeException)
        assert e.message.contains("No bulk data binder config was found for type CATEGORY")
    }

    class ImportableStub implements Importable {
        String stringField
        Integer integerField
        Date dateField
        Date dateTimeField
        Date dateFieldWithoutStyle
    }

    /**
     * A simple configurer of custom data binding for use in tests.
     * The data binding itself is arbitrary. We just define something so that we can make assertions on it.
     */
    class BulkDataBinderConfigurerForTest implements ConfiguresBulkDataBinder<ImportableStub> {

        @Override
        BulkDataType getBulkDataType() {
            return BULK_DATA_TYPE
        }

        @Override
        BulkDataBinderConfig getBulkDataBinderConfig() {
            return new BulkDataBinderConfig(
                    bindTo: ImportableStub,
                    bulkDataType: bulkDataType,
                    fields: [
                            "stringField": BulkDataBinderFieldConfigDefaults.DEFAULT_CONFIG,
                            "integerField": BulkDataBinderFieldConfigDefaults.MANUALLY_BOUND,
                            "dateField": new BulkDataBinderFieldConfig(dateFieldType: DateFieldType.LOCAL_DATE),
                            "dateTimeField": new BulkDataBinderFieldConfig(dateFieldType: DateFieldType.INSTANT),

                            // This is an invalid config (dateFieldType must always be specified for Date fields).
                            "dateFieldWithoutStyle": BulkDataBinderFieldConfigDefaults.DEFAULT_CONFIG,
                    ],
                    columnByFieldName: [
                            "stringField": "0",
                            "integerField": "1",
                            "dateField": "2",
                            "dateTimeField": "3",
                            "dateFieldWithoutStyle": "4",
                    ]
            )
        }

        @Override
        CustomBulkDataErrors customBindDataRow(Map<String, BulkDataCell> rawRow, ImportableStub boundRow) {
            CustomBulkDataErrors errors = new CustomBulkDataErrors()

            boundRow.integerField = rawRow["integerField"].value as Integer

            if (boundRow.integerField == VALUE_THAT_FAILS_CUSTOM_BINDING) {
                errors.addFieldError("integerField", "error.code")
            }
            if (boundRow.integerField == VALUE_THAT_WARNS_CUSTOM_BINDING) {
                errors.addFieldError("integerField", "warn.code", null, BulkDataErrorSeverity.WARNING)
            }
            return errors
        }
    }
}
