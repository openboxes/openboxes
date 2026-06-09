package org.pih.warehouse.importer

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import testutil.ResourceUtil

import org.pih.warehouse.core.http.ContentType

@Unroll
class CsvReaderSpec extends Specification {

    private static final String TEST_CSV_FILE_PATH = "/testfiles/importer/csv-file-importer-spec.csv"

    @Shared
    CsvReader reader

    @Shared
    MultipartFileSource csvFile

    void setupSpec() {
        csvFile = new MultipartFileSource(source: ResourceUtil.getMultipartFile(TEST_CSV_FILE_PATH))
    }

    void setup() {
        reader = new CsvReader()
    }

    void "read should successfully read from csv file for case: #scenario"() {
        given:
        CsvReaderConfig config = new CsvReaderConfig(
                delimiter: ",",
                linesToSkip: 1,
                columnMapping: [
                        "0": "field1",
                        "1": "field2",
                ],
        )

        when:
        BulkDataReaderResult result = reader.read(csvFile, config)
        List<Map<String, BulkDataCell>> rows = result.rows

        then:
        BulkDataCell field1Cell = rows[resultListIndex]["field1"]
        assert field1Cell.value == field1ExpectedValue
        assert field1Cell.row == rowIndex
        assert field1Cell.column == 0
        assert field1Cell.fieldName == "field1"

        BulkDataCell field2Cell = rows[resultListIndex]["field2"]
        assert field2Cell.value == field2ExpectedValue
        assert field2Cell.row == rowIndex
        assert field2Cell.column == 1
        assert field2Cell.fieldName == "field2"

        where:
        rowIndex | resultListIndex || field1ExpectedValue | field2ExpectedValue | scenario
        1        | 0               || "ABC"               | "ABC"               | "plain text"
        2        | 1               || "ABC"               | ""                  | "blank after delimiter"
        3        | 2               || ""                  | "ABC"               | "blank before delimiter"
        4        | 3               || ""                  | ""                  | "all fields blank"
        5        | 4               || " ABC "             | " ABC "             | "spaces around strings"
        6        | 5               || "ABC,123,"          | "ABC,"              | "delimiters within cells"
        7        | 6               || "\"ABC\""           | "\"ABC"             | "double quotes within cells"
        8        | 7               || "ABC\\n"            | "\\nABC"            | "new line characters within cells"
        9        | 8               || "'ABC"              | "ABC'"              | "single quotes within cells"
        10       | 9               || "\\\\ABC"           | "\\\\\\\\ABC"       | "backslash within cells"
        11       | 10              || "jabłko"            | "苹果"               | "special characters"
        12       | 11              || "1"                 | " 1 "               | "integers"
        13       | 12              || "1.1"               | " 1.1 "             | "decimals"
        14       | 13              || "2000-01-01"        | "2000-01-01T00:00Z" | "dates"
    }

    void "read should successfully read from csv String for case: #scenario"() {
        given:
        StringSource csvString = new StringSource(
                source: "field1,field2\n" +
                        "ABC,ABC\n" +
                        "ABC,\n" +
                        ",ABC\n" +
                        ",\n" +
                        " ABC , ABC \n" +
                        "\"ABC,123,\",\"ABC,\"\n" +
                        // To represent double quotes, CSV encoding requires the whole cell to be wrapped in quotes,
                        // then the quote character being stringified must be escaped by ANOTHER quote.
                        // So """ABC""" becomes "ABC" and """ABC" becomes "ABC.
                        "\"\"\"ABC\"\"\",\"\"\"ABC\"\n" +
                        "\"ABC\\n\",\"\\nABC\"\n" +
                        "'ABC,ABC'\n" +
                        "\\\\ABC,\\\\\\\\ABC\n" +
                        "jabłko,苹果\n" +
                        "1, 1 \n" +
                        "1.1, 1.1 \n" +
                        "2000-01-01,2000-01-01T00:00Z",
                contentType: ContentType.CSV,
        )

        and:
        CsvReaderConfig config = new CsvReaderConfig(
                delimiter: ",",
                linesToSkip: 1,
                columnMapping: [
                        "0": "field1",
                        "1": "field2",
                ],
        )

        when:
        BulkDataReaderResult result = reader.read(csvString, config)
        List<Map<String, BulkDataCell>> rows = result.rows

        then:
        BulkDataCell field1Cell = rows[resultListIndex]["field1"]
        assert field1Cell.value == field1ExpectedValue
        assert field1Cell.row == rowIndex
        assert field1Cell.column == 0
        assert field1Cell.fieldName == "field1"

        BulkDataCell field2Cell = rows[resultListIndex]["field2"]
        assert field2Cell.value == field2ExpectedValue
        assert field2Cell.row == rowIndex
        assert field2Cell.column == 1
        assert field2Cell.fieldName == "field2"

        where:
        rowIndex | resultListIndex || field1ExpectedValue | field2ExpectedValue | scenario
        1        | 0               || "ABC"               | "ABC"               | "plain text"
        2        | 1               || "ABC"               | ""                  | "blank after delimiter"
        3        | 2               || ""                  | "ABC"               | "blank before delimiter"
        4        | 3               || ""                  | ""                  | "all fields blank"
        5        | 4               || " ABC "             | " ABC "             | "spaces around strings"
        6        | 5               || "ABC,123,"          | "ABC,"              | "delimiters within cells"
        7        | 6               || "\"ABC\""           | "\"ABC"             | "double quotes within cells"
        8        | 7               || "ABC\\n"            | "\\nABC"            | "new line characters within cells"
        9        | 8               || "'ABC"              | "ABC'"              | "single quotes within cells"
        10       | 9               || "\\\\ABC"           | "\\\\\\\\ABC"       | "backslash within cells"
        11       | 10              || "jabłko"            | "苹果"               | "special characters"
        12       | 11              || "1"                 | " 1 "               | "integers"
        13       | 12              || "1.1"               | " 1.1 "             | "decimals"
        14       | 13              || "2000-01-01"        | "2000-01-01T00:00Z" | "dates"
    }

    void "read should successfully handle blank rows for case: #scenario"() {
        given:
        StringSource csvString = new StringSource(
                source: "\n" +
                        "ABC,DEF\n" +
                        "\n" +
                        "GHI,JKL\n" +
                        "\n",
                contentType: ContentType.CSV,
        )

        and:
        CsvReaderConfig config = new CsvReaderConfig(
                delimiter: ",",
                linesToSkip: 0,
                columnMapping: [
                        "0": "field1",
                        "1": "field2",
                ],
        )

        when:
        BulkDataReaderResult result = reader.read(csvString, config)
        List<Map<String, BulkDataCell>> rows = result.rows

        then:
        assert rows.size() == 2  // The blank rows are left out entirely

        BulkDataCell field1Cell = rows[resultListIndex]["field1"]
        assert field1Cell.value == field1ExpectedValue
        assert field1Cell.row == rowIndex
        assert field1Cell.column == 0
        assert field1Cell.fieldName == "field1"

        BulkDataCell field2Cell = rows[resultListIndex]["field2"]
        assert field2Cell.value == field2ExpectedValue
        assert field2Cell.row == rowIndex
        assert field2Cell.column == 1
        assert field2Cell.fieldName == "field2"

        // It's worth noting that the apache commons CSV reader automatically trims out blank rows from the CSV
        // prior to us looping through them, so even though there are blanks in the file, the indexes stay in sync.
        where:
        rowIndex | resultListIndex || field1ExpectedValue | field2ExpectedValue | scenario
        0        | 0               || "ABC"               | "DEF"               | "too many columns"
        1        | 1               || "GHI"               | "JKL"               | "too many columns with blank"
    }

    void "read should successfully handle too many columns for case: #scenario"() {
        given:
        StringSource csvString = new StringSource(
                source: "ABC,DEF,GHI\n" +
                        "ABC,,GHI",
                contentType: ContentType.CSV,
        )

        and:
        CsvReaderConfig config = new CsvReaderConfig(
                delimiter: ",",
                linesToSkip: 0,
                columnMapping: [
                        "0": "field1",
                        "1": "field2",
                ],
        )

        when:
        BulkDataReaderResult result = reader.read(csvString, config)
        List<Map<String, BulkDataCell>> rows = result.rows

        then:
        assert rows.size() == 2

        BulkDataCell field1Cell = rows[resultListIndex]["field1"]
        assert field1Cell.value == field1ExpectedValue
        assert field1Cell.row == rowIndex
        assert field1Cell.column == 0
        assert field1Cell.fieldName == "field1"

        BulkDataCell field2Cell = rows[resultListIndex]["field2"]
        assert field2Cell.value == field2ExpectedValue
        assert field2Cell.row == rowIndex
        assert field2Cell.column == 1
        assert field2Cell.fieldName == "field2"

        where:
        rowIndex | resultListIndex || field1ExpectedValue | field2ExpectedValue | scenario
        0        | 0               || "ABC"               | "DEF"               | "too many columns no blanks"
        1        | 1               || "ABC"               | ""                  | "too many columns with blank"
    }

    void "read should error when there are too few columns"() {
        given:
        StringSource csvString = new StringSource(
                source: "ABC",
                contentType: ContentType.CSV,
        )

        and:
        CsvReaderConfig config = new CsvReaderConfig(
                delimiter: ",",
                linesToSkip: 0,
                columnMapping: [
                        "0": "field1",
                        "1": "field2",
                ],
        )

        when:
        reader.read(csvString, config)

        then:
        thrown(RuntimeException)
    }

    void "read should successfully work with delimiter: #delimiter"() {
        given:
        StringSource csvString = new StringSource(
                source: "ABC${delimiter}DEF\nGHI${delimiter}JKL",
                contentType: ContentType.CSV,
        )

        and:
        CsvReaderConfig config = new CsvReaderConfig(
                delimiter: delimiter,
                linesToSkip: 0,
                columnMapping: [
                        "0": "field1",
                        "1": "field2",
                ],
        )

        when:
        BulkDataReaderResult result = reader.read(csvString, config)
        List<Map<String, BulkDataCell>> rows = result.rows

        then:
        assert rows[0]["field1"].value == "ABC"
        assert rows[0]["field2"].value == "DEF"
        assert rows[1]["field1"].value == "GHI"
        assert rows[1]["field2"].value == "JKL"

        where:
        delimiter << ["|", ";", "\t", "‍"]
    }
}
