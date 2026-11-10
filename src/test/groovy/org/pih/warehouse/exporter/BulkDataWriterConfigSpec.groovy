package org.pih.warehouse.exporter

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class BulkDataWriterConfigSpec extends Specification {

    void "getFieldConfigsInOrder orders #description column indexes the way the columns appear in the file"() {
        given: "fields defined in the reverse of their column order"
        BulkDataWriterConfig config = new CsvWriterConfig(
                fields: expectedOrder.reverse().collect { new BulkDataWriterFieldConfig(fieldName: it, columnIndex: it) },
        )

        expect:
        assert config.fieldConfigsInOrder*.columnIndex == expectedOrder

        where:
        description | expectedOrder
        "letter"    | ["A", "B", "Z", "AA", "AB", "BA"]
        "numeric"   | ["0", "1", "2", "9", "10", "26"]
    }

    void "getFieldConfigsInOrder does not reorder the config's own fields"() {
        given: "fields defined out of column order"
        BulkDataWriterConfig config = new CsvWriterConfig(
                fields: [
                        new BulkDataWriterFieldConfig(fieldName: "second", columnIndex: "B"),
                        new BulkDataWriterFieldConfig(fieldName: "first", columnIndex: "A"),
                ],
        )

        when:
        List<BulkDataWriterFieldConfig> ordered = config.fieldConfigsInOrder

        then:
        assert ordered*.fieldName == ["first", "second"]

        and: "fetching the ordered view has no side effect on the config"
        assert config.fields*.fieldName == ["second", "first"]
    }
}
