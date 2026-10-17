package org.pih.warehouse.importer

import org.springframework.context.ApplicationContext
import spock.lang.Specification
import spock.lang.Unroll
import testutil.MessageLocalizerStub

import org.pih.warehouse.core.session.SessionManager
import org.pih.warehouse.core.formatter.DefaultTypeFormatter
import org.pih.warehouse.core.http.ContentType
import org.pih.warehouse.core.parser.DoubleParser
import org.pih.warehouse.core.parser.EnumParser
import org.pih.warehouse.exporter.BulkDataExportComponentResolver
import org.pih.warehouse.exporter.BulkDataWriter
import org.pih.warehouse.exporter.CsvWriter
import org.pih.warehouse.exporter.ExcelWriter

@Unroll
class BulkDataImportConfigurerSpec extends Specification {

    void "every content type the configurer supports resolves to a writer, including #contentType"() {
        given: "the resolver wired with the configurer and the real writers"
        BulkDataExportComponentResolver resolver = new BulkDataExportComponentResolver(
                Optional.of([new StatusConfigurer()]),
                Optional.of(buildWriters()))

        expect: "the configurer advertises the content type"
        assert new StatusConfigurer().supportedContentTypes.contains(contentType)

        and: "a config is found for it"
        assert resolver.getBulkDataWriterConfig(BulkDataType.INVENTORY, contentType) != null

        and: "so a writer must be found for it too, otherwise the export fails with a null writer"
        assert resolver.getBulkDataWriter(contentType) != null

        where:
        contentType << [ContentType.CSV, ContentType.XLS, ContentType.XLSX]
    }

    void "the binder config is built from the importable type when the export type declares the field differently"() {
        given: "the import DTO declares 'status' as a String but the export DTO declares it as an enum"
        StatusConfigurer configurer = new StatusConfigurer()

        when:
        BulkDataBinderConfig binderConfig = configurer.bulkDataBinderConfig

        then: "binding targets the import DTO, so its String field must not get the enum parser"
        assert binderConfig.bindTo == StatusImportRow
        assert binderConfig.fields["status"].parser != EnumParser
    }

    void "the binder config can be built for a field that only the importable type declares"() {
        given:
        ImportOnlyFieldConfigurer configurer = new ImportOnlyFieldConfigurer()

        when:
        BulkDataBinderConfig binderConfig = configurer.bulkDataBinderConfig

        then:
        notThrown(NoSuchFieldException)
        assert binderConfig.fields.containsKey("notes")
    }

    void "autoBoundField supports fields inherited from a superclass"() {
        when:
        BulkDataImportExportConfig config = BulkDataImportExportConfig.builder(InheritingExportRow)
                .autoBoundField("A", "code", "inheritingExportRow.code.label")
                .build()

        then:
        notThrown(NoSuchFieldException)
        assert config.binderFieldConfigs["code"] == BulkDataBinderFieldConfigDefaults.DEFAULT_CONFIG
    }

    private List<BulkDataWriter> buildWriters() {
        ApplicationContext context = Stub(ApplicationContext)
        BulkDataExportComponentResolver resolver = Stub(BulkDataExportComponentResolver)
        DefaultTypeFormatter defaultTypeFormatter = new DefaultTypeFormatter(Optional.empty())
        return [
                new CsvWriter(context, resolver, defaultTypeFormatter, MessageLocalizerStub.MESSAGE_LOCALIZER_STUB),
                new ExcelWriter(context, resolver, defaultTypeFormatter, new DoubleParser(),
                        MessageLocalizerStub.MESSAGE_LOCALIZER_STUB, Stub(SessionManager)),
        ]
    }

    static enum Status { ACTIVE, INACTIVE }

    static class StatusImportRow implements Importable {
        String status
    }

    static class StatusExportRow {
        Status status
    }

    static class StatusConfigurer extends BulkDataImportConfigurer<StatusImportRow, StatusExportRow> {
        @Override
        BulkDataType getBulkDataType() {
            return BulkDataType.INVENTORY
        }

        @Override
        protected BulkDataImportExportConfig getImportExportConfig() {
            return configBuilder()
                    .autoBoundField("A", "status", "statusRow.status.label")
                    .build()
        }
    }

    static class NotesImportRow implements Importable {
        String notes
    }

    static class NotesExportRow {
    }

    static class ImportOnlyFieldConfigurer extends BulkDataImportConfigurer<NotesImportRow, NotesExportRow> {
        @Override
        BulkDataType getBulkDataType() {
            return BulkDataType.INVENTORY
        }

        @Override
        protected BulkDataImportExportConfig getImportExportConfig() {
            return configBuilder()
                    .autoBoundField("A", "notes", "notesRow.notes.label")
                    .build()
        }
    }

    static class BaseExportRow {
        String code
    }

    static class InheritingExportRow extends BaseExportRow {
    }
}
