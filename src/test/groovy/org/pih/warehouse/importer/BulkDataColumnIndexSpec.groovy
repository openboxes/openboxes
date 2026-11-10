package org.pih.warehouse.importer

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class BulkDataColumnIndexSpec extends Specification {

    void "toInt converts '#columnIndex' to #expected"() {
        expect:
        assert BulkDataColumnIndex.toInt(columnIndex) == expected

        where:
        columnIndex || expected
        null        || null
        ""          || null
        "  "        || null
        "0"         || 0
        "9"         || 9
        "10"        || 10
        " 10 "      || 10
        "A"         || 0
        "Z"         || 25
        "AA"        || 26
        "AB"        || 27
        "aa"        || 26
        " B "       || 1
    }

    void "toInt fails for invalid column index '#columnIndex'"() {
        when:
        BulkDataColumnIndex.toInt(columnIndex)

        then:
        thrown(IllegalArgumentException)

        where:
        columnIndex << [
                "A1",
                "1A",
                "-1",
                "1.5",
                "\$A",
                "A B",
        ]
    }

    void "toIntKeys converts letter and numerical keys"() {
        given:
        Map columnMapping = ["A": "field0", "1": "field1", "aa": "field26", "27": "field27"]

        expect:
        assert BulkDataColumnIndex.toIntKeys(columnMapping) == [0: "field0", 1: "field1", 26: "field26", 27: "field27"]
    }

    void "toIntKeys succeeds for #scenario mapping"() {
        expect:
        assert BulkDataColumnIndex.toIntKeys(columnMapping as Map<String, String>) == [:]

        where:
        scenario | columnMapping
        "null"   | null
        "empty"  | [:]
    }

    void "toIntKeys fails when keys '#key1' and '#key2' refer to the same column"() {
        when:
        BulkDataColumnIndex.toIntKeys([(key1): "field1", (key2): "field2"])

        then:
        IllegalArgumentException e = thrown()
        assert e.message.contains("'${key1}' and '${key2}' that refer to the same column index '${index}'")

        where:
        key1 | key2 | index
        "A"  | "0"  | "0"
        "a"  | "A"  | "0"
        "1"  | " 1" | "1"
        "AA" | "26" | "26"
    }

    void "toIntKeys fails for a #scenario key"() {
        when:
        BulkDataColumnIndex.toIntKeys([(key): "field1"])

        then:
        thrown(IllegalArgumentException)

        where:
        scenario | key
        "null"   | null
        "empty"  | ""
        "blank"  | " "
    }
}
