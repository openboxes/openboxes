package org.pih.warehouse.importer

import java.sql.Timestamp
import java.time.LocalDate
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.date.DateFieldType
import org.pih.warehouse.core.file.FileNamePart
import org.pih.warehouse.core.parser.StringParser

@Unroll
class BulkDataImportExportConfigSpec extends Specification {

    void "#autoBoundField rejects java.util.Date field '#fieldName'"() {
        when:
        BulkDataImportExportConfig.BulkDataImportExportConfigBuilder builder =
                BulkDataImportExportConfig.builder(DateRow)
        builder.autoBoundField(*args)

        then: "the error tells the developer which builder method to use instead"
        IllegalArgumentException e = thrown()
        assert e.message.contains(expectedSuggestion)

        where:
        fieldName   | args                                               || expectedSuggestion
        "date"      | ["A", "date", "dateRow.date.label"]                || "autoBoundDateField"
        "timestamp" | ["A", "timestamp", "dateRow.timestamp.label"]      || "autoBoundDateField"
        "date"      | ["A", "date", "dateRow.date.label", StringParser]  || "autoBoundDateField"
    }

    void "autoBoundDateField configures both binding and writing with dateFieldType #dateFieldType"() {
        when:
        BulkDataImportExportConfig config = BulkDataImportExportConfig.builder(DateRow)
                .autoBoundDateField("A", "date", "dateRow.date.label", dateFieldType)
                .build()

        then:
        assert config.binderFieldConfigs["date"].dataBindingMethod == DataBindingMethod.AUTO
        assert config.binderFieldConfigs["date"].dateFieldType == dateFieldType
        assert config.writerFieldConfigs*.dateFieldType == [dateFieldType]
        assert config.columnMapping == ["A": "date"]

        where:
        dateFieldType << DateFieldType.values().toList()
    }

    void "the builder rejects a field that is added twice"() {
        when:
        BulkDataImportExportConfig.builder(DateRow)
                .autoBoundField("A", "localDate", "dateRow.localDate.label")
                .manuallyBoundField("B", "localDate", "dateRow.localDate.label")

        then:
        IllegalArgumentException e = thrown()
        assert e.message.contains("Field 'localDate' was added more than once")
    }

    void "the builder rejects a column that is mapped to more than one field"() {
        when:
        BulkDataImportExportConfig.builder(DateRow)
                .autoBoundField("A", "localDate", "dateRow.localDate.label")
                .autoBoundDateField("A", "date", "dateRow.date.label", DateFieldType.LOCAL_DATE)

        then:
        IllegalArgumentException e = thrown()
        assert e.message.contains("Column 'A' was mapped to more than one field")
    }

    void "fileName configures the file name args"() {
        when:
        BulkDataImportExportConfig config = BulkDataImportExportConfig.builder(DateRow)
                .fileName("DateReport", FileNamePart.CURRENT_TIME)
                .build()

        then:
        assert config.fileNameArgs == ["DateReport", FileNamePart.CURRENT_TIME]
    }

    void "the file name is empty when not configured"() {
        expect:
        assert BulkDataImportExportConfig.builder(DateRow).build().fileNameArgs == []
    }

    void "autoBoundDateField rejects #description"() {
        when:
        BulkDataImportExportConfig.builder(DateRow).autoBoundDateField("A", fieldName, "label", dateFieldType)

        then:
        thrown(IllegalArgumentException)

        where:
        description                           | fieldName   | dateFieldType
        "a missing dateFieldType"             | "date"      | null
        "a field that isn't a java.util.Date" | "localDate" | DateFieldType.LOCAL_DATE
        "a field that doesn't exist"          | "missing"   | DateFieldType.LOCAL_DATE
    }

    static class DateRow implements Importable {
        Date date
        // Subclasses of Date (such as the Timestamp that Hibernate returns) are just as ambiguous.
        Timestamp timestamp
        LocalDate localDate
    }
}
