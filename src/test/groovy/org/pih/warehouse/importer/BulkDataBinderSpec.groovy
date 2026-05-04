package org.pih.warehouse.importer

import org.springframework.context.ApplicationContext
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.localization.MessageLocalizer
import org.pih.warehouse.core.parser.DefaultTypeParser
import org.pih.warehouse.core.parser.Parser
import org.pih.warehouse.core.parser.StringParser

@Unroll
class BulkDataBinderSpec extends Specification {

    BulkDataBinder bulkDataBinder

    DefaultTypeParser defaultTypeParserStub
    ConfiguresBulkDataBinder bulkDataBinderConfigurerStub
    MessageLocalizer messageLocalizerStub
    Parser parserStub

    void setup() {
        defaultTypeParserStub = Stub(DefaultTypeParser)

        bulkDataBinderConfigurerStub = Stub(ConfiguresBulkDataBinder)
        BulkDataImportComponentResolver componentResolverStub = Stub(BulkDataImportComponentResolver) {
            getBulkDataBinderConfigurer(_ as BulkDataType) >> bulkDataBinderConfigurerStub
        }

        messageLocalizerStub = Stub(MessageLocalizer) {
            localize(_ as String, _ as Object[]) >> "LOCALIZED MESSAGE"
        }

        parserStub = Stub(Parser)
        ApplicationContext contextStub = Stub(ApplicationContext) {
            getBean(_ as Class<Parser>) >> parserStub
        }

        bulkDataBinder = new BulkDataBinder(
                componentResolverStub, defaultTypeParserStub, messageLocalizerStub, contextStub)
    }

    void "bindData should successfully bind data to a strongly-typed object"() {
        given: "the raw data being bound"
        BulkDataType dataImportType = BulkDataType.PERSON
        List<LinkedHashMap<String, Object>> rawRows = [
                ["stringField": "Hi", "integerField": 1],
        ]

        and: "the config to use when data binding"
        bulkDataBinderConfigurerStub.bulkDataBinderConfig >> new BulkDataBinderConfig(
                bindTo: ImportableStub,
                fields: [
                        "stringField": new BulkDataBinderFieldConfig(),
                        "integerField": new BulkDataBinderFieldConfig(),
                ]
        )

        and: "stubbed values for the parser to return"
        defaultTypeParserStub.parse("Hi", String, null) >> "Hi"
        defaultTypeParserStub.parse(1, Integer, null) >> 1

        when:
        BulkDataBinderResult result = bulkDataBinder.bindData(dataImportType, rawRows)
        List<ImportableStub> rows = result.boundRows as List<ImportableStub>

        then:
        assert rows.size() == 1
        assert rows[0].stringField == "Hi"
        assert rows[0].integerField == 1

        assert result.errors.size() == 0
    }

    void "bindData should ignore fields that are not specified in the config"() {
        given: "the raw data being bound"
        BulkDataType dataImportType = BulkDataType.PERSON
        List<LinkedHashMap<String, Object>> rawRows = [
                ["stringField": "Hi", "integerField": 1],
        ]

        and: "the config to use when data binding"
        bulkDataBinderConfigurerStub.bulkDataBinderConfig >> new BulkDataBinderConfig(
                bindTo: ImportableStub,
                fields: [
                        "stringField": new BulkDataBinderFieldConfig(),
                        // integerField is not included!
                ]
        )

        and: "stubbed values for the parser to return"
        defaultTypeParserStub.parse("Hi", String, null) >> "Hi"
        defaultTypeParserStub.parse(1, Integer, null) >> 1

        when:
        BulkDataBinderResult result = bulkDataBinder.bindData(dataImportType, rawRows)
        List<ImportableStub> rows = result.boundRows as List<ImportableStub>

        then:
        assert rows.size() == 1
        assert rows[0].stringField == "Hi"   // Should parse normally
        assert rows[0].integerField == null  // Should be ignored

        assert result.errors.size() == 0
    }

    void "bindData should capture parser errors"() {
        given: "the raw data being bound"
        BulkDataType dataImportType = BulkDataType.PERSON
        List<LinkedHashMap<String, Object>> rawRows = [
                ["stringField": "Hi", "integerField": 1],
        ]

        and: "the config to use when data binding"
        bulkDataBinderConfigurerStub.bulkDataBinderConfig >> new BulkDataBinderConfig(
                bindTo: ImportableStub,
                fields: [
                        "stringField": new BulkDataBinderFieldConfig(parser: StringParser),
                        // Wrong type! In reality this wouldn't error because the String parser can handle integers,
                        // but it doesn't matter because we force an error below to simulate the behaviour.
                        "integerField": new BulkDataBinderFieldConfig(parser: StringParser),
                ]
        )

        and: "stubbed values for the parser to return"
        parserStub.parse("Hi", null) >> "Hi"
        parserStub.parse(1, null) >> { throw new RuntimeException("PARSER ERROR") }

        when:
        BulkDataBinderResult result = bulkDataBinder.bindData(dataImportType, rawRows)
        List<ImportableStub> rows = result.boundRows as List<ImportableStub>

        then:
        assert rows.size() == 1
        assert rows[0].stringField == "Hi"
        assert rows[0].integerField == null

        assert result.errors.size() == 1
        assert result.errors[0].row == 0
        assert result.errors[0].column == 1
        assert result.errors[0].severity == BulkDataErrorSeverity.ERROR
        assert result.errors[0].localizedMessage == "LOCALIZED MESSAGE"
        assert result.errors[0].exception.message == "PARSER ERROR"
    }

    void "bindData should successfully custom bind data"() {
        given: "the raw data being bound"
        BulkDataType dataImportType = BulkDataType.PERSON
        List<LinkedHashMap<String, Object>> rawRows = [
                ["stringField": "Hi"],
        ]

        and: "the config to use when data binding"
        bulkDataBinderConfigurerStub.bulkDataBinderConfig >> new BulkDataBinderConfig(
                bindTo: ImportableStub,
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
        BulkDataBinderResult result = bulkDataBinder.bindData(dataImportType, rawRows)
        List<ImportableStub> rows = result.boundRows as List<ImportableStub>

        then:
        assert rows.size() == 1
        assert rows[0].stringField == "CUSTOM VALUE"

        assert result.errors.size() == 0
    }

    void "bindData should handle errors when custom bind data"() {
        given: "the raw data being bound"
        BulkDataType dataImportType = BulkDataType.PERSON
        List<LinkedHashMap<String, Object>> rawRows = [
                ["stringField": "Hi"],
        ]

        and: "the config to use when data binding"
        bulkDataBinderConfigurerStub.bulkDataBinderConfig >> new BulkDataBinderConfig(
                bindTo: ImportableStub,
                fields: [
                        "stringField": new BulkDataBinderFieldConfig(dataBindingMethod: DataBindingMethod.MANUAL),
                ]
        )

        and: "the custom binding logic"
        bulkDataBinderConfigurerStub.customBindData(_ as List, _ as BulkDataBinderResult) >> {
            List rawRowsList, BulkDataBinderResult<ImportableStub> result ->

                result.addError(new BulkDataError(
                        row: 0,
                        column: 0,
                        localizedMessage: "CUSTOM BINDING ERROR"
                ))
        }

        when:
        BulkDataBinderResult result = bulkDataBinder.bindData(dataImportType, rawRows)
        List<ImportableStub> rows = result.boundRows as List<ImportableStub>

        then:
        assert rows.size() == 1
        assert rows[0].stringField == null

        assert result.errors.size() == 1
        assert result.errors[0].row == 0
        assert result.errors[0].column == 0
        assert result.errors[0].localizedMessage == "CUSTOM BINDING ERROR"
    }

    class ImportableStub implements Importable {
        String stringField
        Integer integerField
    }
}
