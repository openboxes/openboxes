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
        List<Map<String, Object>> rows = result.rows

        then:
        assert rows[rowIndex]["field1"] == field1ExpectedValue
        assert rows[rowIndex]["field2"] == field2ExpectedValue

        where:
        rowIndex || field1ExpectedValue | field2ExpectedValue | scenario
        0        || "ABC"               | "ABC"               | "plain text"
        1        || "ABC"               | ""                  | "blank after delimiter"
        2        || ""                  | "ABC"               | "blank before delimiter"
        3        || ""                  | ""                  | "all fields blank"
        4        || " ABC "             | " ABC "             | "spaces around strings"
        5        || "ABC,123,"          | "ABC,"              | "delimiters within cells"
        6        || "\"ABC\""           | "\"ABC"             | "double quotes within cells"
        7        || "ABC\\n"            | "\\nABC"            | "new line characters within cells"
        8        || "'ABC"              | "ABC'"              | "single quotes within cells"
        9        || "\\\\ABC"           | "\\\\\\\\ABC"       | "backslash within cells"
        10       || "jabłko"            | "苹果"               | "special characters"
        11       || "1"                 | " 1 "               | "integers"
        12       || "1.1"               | " 1.1 "             | "decimals"
        13       || "2000-01-01"        | "2000-01-01T00:00Z" | "dates"
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
        List<Map<String, Object>> rows = result.rows

        then:
        assert rows[rowIndex]["field1"] == field1ExpectedValue
        assert rows[rowIndex]["field2"] == field2ExpectedValue

        where:
        rowIndex || field1ExpectedValue | field2ExpectedValue | scenario
        0        || "ABC"               | "ABC"               | "plain text"
        1        || "ABC"               | ""                  | "blank after delimiter"
        2        || ""                  | "ABC"               | "blank before delimiter"
        3        || ""                  | ""                  | "all fields blank"
        4        || " ABC "             | " ABC "             | "spaces around strings"
        5        || "ABC,123,"          | "ABC,"              | "delimiters within cells"
        6        || "\"ABC\""           | "\"ABC"             | "double quotes within cells"
        7        || "ABC\\n"            | "\\nABC"            | "new line characters within cells"
        8        || "'ABC"              | "ABC'"              | "single quotes within cells"
        9        || "\\\\ABC"           | "\\\\\\\\ABC"       | "backslash within cells"
        10       || "jabłko"            | "苹果"               | "special characters"
        11       || "1"                 | " 1 "               | "integers"
        12       || "1.1"               | " 1.1 "             | "decimals"
        13       || "2000-01-01"        | "2000-01-01T00:00Z" | "dates"
    }

    void "read should successfully handle incorrect number of columns for case: #scenario"() {
        given:
        StringSource csvString = new StringSource(
                source: "ABC,DEF,GHI\n" +
                        "ABC,,GHI\n" +
                        "\n" +
                        "ABC\n" +
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
        List<Map<String, Object>> rows = result.rows

        then:
        assert rows.size() == 3  // The blank rows are left out entirely
        assert rows[rowIndex]["field1"] == field1ExpectedValue
        assert rows[rowIndex]["field2"] == field2ExpectedValue

        where:
        rowIndex || field1ExpectedValue | field2ExpectedValue | scenario
        0        || "ABC"               | "DEF"               | "too many columns"
        1        || "ABC"               | ""                  | "too many columns with blank"
        2        || "ABC"               | null                | "too few columns"
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
        List<Map<String, Object>> rows = result.rows

        then:
        assert rows[0]["field1"] == "ABC"
        assert rows[0]["field2"] == "DEF"
        assert rows[1]["field1"] == "GHI"
        assert rows[1]["field2"] == "JKL"

        where:
        delimiter << ["|", ";", "\t", "‍"]
    }
}
