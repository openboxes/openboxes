package org.pih.warehouse.importer

import org.springframework.context.ApplicationContext
import spock.lang.Specification
import spock.lang.Unroll
import testutil.MessageLocalizerStub

import org.pih.warehouse.core.parser.DefaultTypeParser
import org.pih.warehouse.core.parser.Parser
import org.pih.warehouse.core.parser.ParserContext

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

        bulkDataBinder = new BulkDataBinder(
                componentResolverStub,
                defaultTypeParserStub,
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
                    ],
                    columnByFieldName: [
                            "stringField": "0",
                            "integerField": "1",
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
