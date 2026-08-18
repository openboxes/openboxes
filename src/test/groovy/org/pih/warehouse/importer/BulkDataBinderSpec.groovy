package org.pih.warehouse.importer

import org.springframework.context.ApplicationContext
import spock.lang.Specification
import spock.lang.Unroll
import testutil.MessageLocalizerStub

import org.pih.warehouse.core.parser.DefaultTypeParser
import org.pih.warehouse.core.parser.Parser
import org.pih.warehouse.core.parser.ParserContext
import org.pih.warehouse.core.parser.StringParser

@Unroll
class BulkDataBinderSpec extends Specification {

    // Chosen arbitrarily. This will be stubbed so all that matters is that we're consistent.
    private static final BulkDataType BULK_DATA_TYPE = BulkDataType.PERSON
    private static final BulkDataType UNKNOWN_BULK_DATA_TYPE = BulkDataType.CATEGORY

    BulkDataBinder bulkDataBinder

    DefaultTypeParser defaultTypeParserStub
    ConfiguresBulkDataBinder bulkDataBinderConfigurerStub
    Parser parserStub

    void setup() {
        parserStub = Stub(Parser)
        ApplicationContext contextStub = Stub(ApplicationContext) {
            getBean(_ as Class<Parser>) >> parserStub
        }

        defaultTypeParserStub = Stub(DefaultTypeParser) {
            getDefaultParser(_ as Class<Object>) >> parserStub
        }

        bulkDataBinderConfigurerStub = Stub(ConfiguresBulkDataBinder)
        BulkDataImportComponentResolver componentResolverStub = Stub(BulkDataImportComponentResolver) {
            getBulkDataBinderConfigurer(BULK_DATA_TYPE) >> bulkDataBinderConfigurerStub
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

        and: "the config to use when data binding"
        bulkDataBinderConfigurerStub.bulkDataBinderConfig >> new BulkDataBinderConfig(
                bindTo: ImportableStub,
                bulkDataType: dataImportType,
                fields: [
                        "stringField": new BulkDataBinderFieldConfig(),
                        "integerField": new BulkDataBinderFieldConfig(),
                ]
        )

        and: "stubbed values for the parser to return"
        parserStub.parse("Hi", _ as ParserContext) >> "Hi"
        parserStub.parse(1, _ as ParserContext) >> 1

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
                        "integerField": new BulkDataCell(row: 0, column: 1, fieldName: "integerField", value: 1)
                ],
        ])

        and: "the config to use when data binding"
        bulkDataBinderConfigurerStub.bulkDataBinderConfig >> new BulkDataBinderConfig(
                bindTo: ImportableStub,
                bulkDataType: dataImportType,
                fields: [
                        "stringField": new BulkDataBinderFieldConfig(),
                        // integerField is not included!
                ]
        )

        and: "stubbed values for the parser to return"
        parserStub.parse("Hi", _ as ParserContext) >> "Hi"
        parserStub.parse(1, _ as ParserContext) >> 1

        when:
        BulkDataBinderResult result = bulkDataBinder.bindData(dataImportType, readerResult)
        List<ImportableStub> rows = result.boundRows as List<ImportableStub>

        then:
        assert rows.size() == 1
        assert rows[0].stringField == "Hi"   // Should parse normally
        assert rows[0].integerField == null  // Should be ignored

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

        and: "the config to use when data binding"
        bulkDataBinderConfigurerStub.bulkDataBinderConfig >> new BulkDataBinderConfig(
                bindTo: ImportableStub,
                bulkDataType: dataImportType,
                fields: [
                        "stringField": new BulkDataBinderFieldConfig(parser: StringParser),
                        // Wrong type! In reality this wouldn't error because the String parser can handle integers,
                        // but it doesn't matter because we force an error below to simulate the behaviour.
                        "integerField": new BulkDataBinderFieldConfig(parser: StringParser),
                ]
        )

        and: "stubbed values for the parser to return"
        parserStub.parse("Hi", _ as ParserContext) >> "Hi"
        parserStub.parse(1, _ as ParserContext) >> { throw new RuntimeException("PARSER ERROR") }

        when:
        BulkDataBinderResult result = bulkDataBinder.bindData(dataImportType, readerResult)
        List<ImportableStub> rows = result.boundRows as List<ImportableStub>

        then:
        assert rows.size() == 1
        assert rows[0].stringField == "Hi"
        assert rows[0].integerField == null

        assert result.bindErrors.size() == 1
        assert result.bindErrors[0].row == 0
        assert result.bindErrors[0].column == "1"
        assert result.bindErrors[0].fieldName == "integerField"
        assert result.bindErrors[0].severity == BulkDataErrorSeverity.ERROR
        assert result.bindErrors[0].localizedMessage == "import.binder.error"
        assert result.bindErrors[0].exception.message == "PARSER ERROR"
    }

    void "bindData should successfully custom bind data"() {
        given: "the raw data being bound"
        BulkDataType dataImportType = BULK_DATA_TYPE
        BulkDataReaderResult readerResult = new BulkDataReaderResult(rows: [
                [
                        "stringField": new BulkDataCell(row: 0, column: 0, fieldName: "stringField", value: "Hi"),
                ],
        ])

        and: "the config to use when data binding"
        bulkDataBinderConfigurerStub.bulkDataBinderConfig >> new BulkDataBinderConfig(
                bindTo: ImportableStub,
                bulkDataType: dataImportType,
                fields: [
                        "stringField": new BulkDataBinderFieldConfig(dataBindingMethod: DataBindingMethod.MANUAL),
                ]
        )

        and: "the custom binding logic"
        bulkDataBinderConfigurerStub.customBindData(_ as List, _ as BulkDataBinderResult) >> {
            List rawRowsList, BulkDataBinderResult<ImportableStub> result ->

                result.boundRows[0].stringField = "CUSTOM VALUE"
        }

        when:
        BulkDataBinderResult result = bulkDataBinder.bindData(dataImportType, readerResult)
        List<ImportableStub> rows = result.boundRows as List<ImportableStub>

        then:
        assert rows.size() == 1
        assert rows[0].stringField == "CUSTOM VALUE"

        assert result.bindErrors.size() == 0
    }

    void "bindData should handle errors when custom bind data"() {
        given: "the raw data being bound"
        BulkDataType dataImportType = BULK_DATA_TYPE
        BulkDataReaderResult readerResult = new BulkDataReaderResult(rows: [
                [
                        "stringField": new BulkDataCell(row: 0, column: 0, fieldName: "stringField", value: "Hi"),
                ],
        ])

        and: "the config to use when data binding"
        bulkDataBinderConfigurerStub.bulkDataBinderConfig >> new BulkDataBinderConfig(
                bindTo: ImportableStub,
                bulkDataType: dataImportType,
                fields: [
                        "stringField": new BulkDataBinderFieldConfig(dataBindingMethod: DataBindingMethod.MANUAL),
                ]
        )

        and: "the custom binding logic"
        bulkDataBinderConfigurerStub.customBindData(_ as List, _ as BulkDataBinderResult) >> {
            List rawRowsList, BulkDataBinderResult<ImportableStub> result ->

                result.bindErrors.add(new BulkDataError(
                        row: 0,
                        column: 0,
                        fieldName: "stringField",
                        localizedMessage: "CUSTOM BINDING ERROR",
                ))
        }

        when:
        BulkDataBinderResult result = bulkDataBinder.bindData(dataImportType, readerResult)
        List<ImportableStub> rows = result.boundRows as List<ImportableStub>

        then:
        assert rows.size() == 1
        assert rows[0].stringField == null

        assert result.bindErrors.size() == 1
        assert result.bindErrors[0].row == 0
        assert result.bindErrors[0].column == "0"
        assert result.bindErrors[0].fieldName == "stringField"
        assert result.bindErrors[0].localizedMessage == "CUSTOM BINDING ERROR"
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
}
