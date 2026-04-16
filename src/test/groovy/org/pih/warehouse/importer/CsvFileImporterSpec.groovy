package org.pih.warehouse.importer

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import testutil.ResourceUtil

import org.pih.warehouse.core.file.UploadedFile

@Unroll
class CsvFileImporterSpec extends Specification {

    private static final String TEST_CSV_FILE_PATH = "/testfiles/importer/csv-file-importer-spec.csv"

    @Shared
    CsvFileImporter csvFileImporter

    @Shared
    UploadedFile csvFile

    void setupSpec() {
        csvFile = new UploadedFile(file: ResourceUtil.getMultiPartFile(TEST_CSV_FILE_PATH))
    }

    void setup() {
        csvFileImporter = new CsvFileImporter()
    }

    void 'importFile should successfully import from csv file for case: #scenario'() {
        given:
        CsvFileImporterConfig config = new CsvFileImporterConfig(
                delimiter: ",",
                startRow: 1,
                columnToFieldMapping: [
                        "0": "field1",
                        "1": "field2",
                ],
        )

        when:
        FileImportResult result = csvFileImporter.importFile(csvFile, config)
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
        6        || "jabłko"            | "苹果"               | "special characters"
        7        || "1"                 | " 1 "               | "integers"
        8        || "1.1"               | " 1.1 "             | "decimals"
        9        || "2000-01-01"        | "2000-01-01T00:00Z" | "dates"
    }
}
