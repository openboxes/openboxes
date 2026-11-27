package org.pih.warehouse.exporter

import org.springframework.context.ApplicationContext
import spock.lang.Specification
import spock.lang.Unroll
import testutil.MessageLocalizerStub

import org.pih.warehouse.core.ConfigService
import org.pih.warehouse.core.date.DateFieldType
import org.pih.warehouse.core.file.FileNamePart
import org.pih.warehouse.core.formatter.DefaultTypeFormatter
import org.pih.warehouse.core.http.ContentType
import org.pih.warehouse.core.parser.DoubleParser
import org.pih.warehouse.core.session.SessionManager
import org.pih.warehouse.importer.BulkDataType

@Unroll
class BulkDataExportConfigurerSpec extends Specification {

    void "every content type the configurer supports resolves to a config and a writer, including #contentType"() {
        given: "the resolver wired with the configurer and the real writers"
        BulkDataExportComponentResolver resolver = new BulkDataExportComponentResolver(
                Optional.of([new TestExportConfigurer()]),
                Optional.of(buildWriters()))

        expect:
        assert resolver.getBulkDataWriterConfig(BulkDataType.INVENTORY, contentType) != null
        assert resolver.getBulkDataWriter(contentType) != null

        where:
        contentType << new TestExportConfigurer().supportedContentTypes.toList()
    }

    void "getBulkDataWriterConfig returns the config type that matches the content type: #contentType"() {
        expect:
        assert expectedType.isInstance(new TestExportConfigurer().getBulkDataWriterConfig(contentType))

        where:
        contentType     || expectedType
        ContentType.CSV || CsvWriterConfig
        ContentType.XLS || ExcelWriterConfig
    }

    void "getBulkDataWriterConfig fails for an unsupported content type"() {
        when:
        new TestExportConfigurer().getBulkDataWriterConfig(ContentType.PDF)

        then:
        thrown(IllegalArgumentException)
    }

    void "the Excel config is styled as a table"() {
        expect:
        assert new TestExportConfigurer().excelWriterConfig.excelStyle == ExcelStyle.TABLE
    }

    void "fields are configured in the order they are added, with their headers and date styles"() {
        when:
        TestExportConfigurer configurer = new TestExportConfigurer()

        then: "CSV and Excel use the same field config"
        for (BulkDataWriterConfig config in [configurer.csvWriterConfig, configurer.excelWriterConfig]) {
            assert config.fields*.fieldName == ["code", "name", "dateCreated", "lastCounted"]
            assert config.fields*.headerMessageCode == ["test.code.label", null, "test.dateCreated.label", null]
            assert config.fields*.headerPlainText == [null, "Name", null, "Last Counted"]
            assert config.fields*.dateFieldType == [null, null, DateFieldType.INSTANT, DateFieldType.LOCAL_DATE]
        }
    }

    void "#builderMethod rejects a missing dateFieldType"() {
        when:
        builder(BulkDataExportConfig.builder())

        then:
        thrown(IllegalArgumentException)

        where:
        builderMethod          | builder
        "dateField"            | { b -> b.dateField("date", "test.date.label", null) }
        "dateFieldUnlocalized" | { b -> b.dateFieldUnlocalized("date", "Date", null) }
    }

    void "the configured file name is resolvable for every supported content type, including #contentType"() {
        given:
        BulkDataExportComponentResolver resolver = new BulkDataExportComponentResolver(
                Optional.of([new TestExportConfigurer()]),
                Optional.of(buildWriters()))

        expect:
        assert resolver.getFileNameArgs(BulkDataType.INVENTORY, contentType) ==
                ["TestReport", FileNamePart.CURRENT_TIME]

        where:
        contentType << new TestExportConfigurer().supportedContentTypes.toList()
    }

    void "the file name is empty when not configured"() {
        expect:
        assert BulkDataExportConfig.builder().build().fileNameArgs == []
    }

    void "the builder rejects a field that is added twice"() {
        when:
        BulkDataExportConfig.builder()
                .field("code", "test.code.label")
                .fieldUnlocalized("code", "Code")

        then:
        thrown(IllegalArgumentException)
    }

    private List<BulkDataWriter> buildWriters() {
        ApplicationContext context = Stub(ApplicationContext)
        BulkDataExportComponentResolver resolver = Stub(BulkDataExportComponentResolver)
        DefaultTypeFormatter defaultTypeFormatter = new DefaultTypeFormatter(Optional.empty())
        return [
                new CsvWriter(context, resolver, defaultTypeFormatter, MessageLocalizerStub.MESSAGE_LOCALIZER_STUB),
                new ExcelWriter(context, resolver, defaultTypeFormatter, new DoubleParser(),
                        MessageLocalizerStub.MESSAGE_LOCALIZER_STUB, Stub(SessionManager), Stub(ConfigService)),
        ]
    }

    static class TestExportConfigurer extends BulkDataExportConfigurer {

        @Override
        BulkDataType getBulkDataType() {
            return BulkDataType.INVENTORY
        }

        @Override
        protected BulkDataExportConfig getExportConfig() {
            return configBuilder()
                    .fileName("TestReport", FileNamePart.CURRENT_TIME)
                    .field("code", "test.code.label")
                    .fieldUnlocalized("name", "Name")
                    .dateField("dateCreated", "test.dateCreated.label", DateFieldType.INSTANT)
                    .dateFieldUnlocalized("lastCounted", "Last Counted", DateFieldType.LOCAL_DATE)
                    .build()
        }
    }
}
