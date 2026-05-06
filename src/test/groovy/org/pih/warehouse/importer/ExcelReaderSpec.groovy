package org.pih.warehouse.importer

import grails.validation.ValidationException
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import testutil.ResourceUtil

@Unroll
class ExcelReaderSpec extends Specification {

    private static final String TEST_XLS_FILE_PATH = "/testfiles/importer/excel-file-importer-spec.xls"
    private static final String TEST_XLSX_FILE_PATH = "/testfiles/importer/excel-file-importer-spec.xlsx"
    private static final String TEST_TXT_FILE_PATH = "/testfiles/importer/excel-file-importer-spec.txt"

    @Shared
    ExcelReader reader

    @Shared
    MultipartFileSource xlsFile

    @Shared
    MultipartFileSource xlsxFile

    @Shared
    MultipartFileSource txtFile

    void setupSpec() {
        xlsFile = new MultipartFileSource(source: ResourceUtil.getMultipartFile(TEST_XLS_FILE_PATH))
        xlsxFile = new MultipartFileSource(source: ResourceUtil.getMultipartFile(TEST_XLSX_FILE_PATH))
        txtFile = new MultipartFileSource(source: ResourceUtil.getMultipartFile(TEST_TXT_FILE_PATH))
    }

    void setup() {
        reader = new ExcelReader()
    }

    void 'read should successfully import strings from xls file for case: #scenario'() {
        given:
        ExcelReaderConfig config = new ExcelReaderConfig(
                sheetName: "string",
                linesToSkip: 1,
                columnMapping: ["A": "string"],
        )

        when:
        BulkDataReaderResult result = reader.read(xlsFile, config)

        then:
        BulkDataCell cell = result.rows[rowIndex]["string"]
        assert cell.value == expectedValue
        assert cell.row == rowIndex + 1  // +1 because of the header row
        assert cell.column == 0
        assert cell.fieldName == "string"

        where:
        rowIndex || expectedValue | scenario
        0        || "ABC"         | "Plain string"
        1        || "A1"          | "String ends in number"
        2        || "1A"          | "String starts with number"
        3        || " A"          | "String starts with space"
        4        || "A "          | "String ends with space"
        5        || "苹果"         | "Special characters"
    }

    @Ignore("The Grails plugin makes it hard to add the dependencies required to support XLSX files. Once we remove the plugin and upgrade POI, we can re-enable this test")
    void 'read should successfully import strings from xlsx file for case: #scenario'() {
        given:
        ExcelReaderConfig config = new ExcelReaderConfig(
                sheetName: "string",
                linesToSkip: 1,
                columnMapping: ["A": "string"],
        )

        when:
        BulkDataReaderResult result = reader.read(xlsxFile, config)

        then:
        BulkDataCell cell = result.rows[rowIndex]["string"]
        assert cell.value == expectedValue
        assert cell.row == rowIndex + 1  // +1 because of the header row
        assert cell.column == 0
        assert cell.fieldName == "string"

        where:
        rowIndex || expectedValue | scenario
        0        || "ABC"         | "Plain string"
        1        || "A1"          | "String ends in number"
        2        || "1A"          | "String starts with number"
        3        || " A"          | "String starts with space"
        4        || "A "          | "String ends with space"
        5        || "苹果"         | "Special characters"
    }

    void 'read should successfully import numerics from xls file for case: #scenario'() {
        given:
        ExcelReaderConfig config = new ExcelReaderConfig(
                sheetName: "numeric",
                linesToSkip: 1,
                columnMapping: ["A": "numeric"],
        )

        when:
        BulkDataReaderResult result = reader.read(xlsFile, config)

        then:
        BulkDataCell cell = result.rows[rowIndex]["numeric"]
        assert cell.value == expectedValue
        assert cell.row == rowIndex + 1  // +1 because of the header row
        assert cell.column == 0
        assert cell.fieldName == "numeric"

        where:
        rowIndex || expectedValue | scenario
        0        || 1.0           | "Integer"
        1        || 1.2           | "Double"
        2        || 0.0           | "Epoch Date"   // "1899-12-30" in the file, which is Excel epoch
        3        || 36526.0       | "Modern Date"  // "2000-01-01" in the file, which is 36526 days after Excel epoch
    }

    @Ignore("The Grails plugin makes it hard to add the dependencies required to support XLSX files. Once we remove the plugin and upgrade POI, we can re-enable this test")
    void 'read should successfully import numerics from xlsx file for case: #scenario'() {
        given:
        ExcelReaderConfig config = new ExcelReaderConfig(
                sheetName: "numeric",
                linesToSkip: 1,
                columnMapping: ["A": "numeric"],
        )

        when:
        BulkDataReaderResult result = reader.read(xlsxFile, config)

        then:
        BulkDataCell cell = result.rows[rowIndex]["numeric"]
        assert cell.value == expectedValue
        assert cell.row == rowIndex + 1  // +1 because of the header row
        assert cell.column == 0
        assert cell.fieldName == "string"

        where:
        rowIndex || expectedValue | scenario
        0        || 1.0           | "Integer"
        1        || 1.2           | "Double"
        2        || 0.0           | "Epoch Date"   // "1899-12-30" in the file, which is Excel epoch
        3        || 36526.0       | "Modern Date"  // "2000-01-01" in the file, which is 36526 days after Excel epoch
    }

    void 'read should successfully import booleans from xls file for case: #scenario'() {
        given:
        ExcelReaderConfig config = new ExcelReaderConfig(
                sheetName: "boolean",
                linesToSkip: 1,
                columnMapping: ["A": "boolean"],
        )

        when:
        BulkDataReaderResult result = reader.read(xlsFile, config)

        then:
        BulkDataCell cell = result.rows[rowIndex]["boolean"]
        assert cell.value == expectedValue
        assert cell.row == rowIndex + 1  // +1 because of the header row
        assert cell.column == 0
        assert cell.fieldName == "boolean"

        where:
        rowIndex || expectedValue | scenario
        0        || true          | "True"
        1        || false         | "False"
    }

    @Ignore("The Grails plugin makes it hard to add the dependencies required to support XLSX files. Once we remove the plugin and upgrade POI, we can re-enable this test")
    void 'read should successfully import booleans from xlsx file for case: #scenario'() {
        given:
        ExcelReaderConfig config = new ExcelReaderConfig(
                sheetName: "boolean",
                linesToSkip: 1,
                columnMapping: ["A": "boolean"],
        )

        when:
        BulkDataReaderResult result = reader.read(xlsxFile, config)

        then:
        BulkDataCell cell = result.rows[rowIndex]["boolean"]
        assert cell.value == expectedValue
        assert cell.row == rowIndex + 1  // +1 because of the header row
        assert cell.column == 0
        assert cell.fieldName == "boolean"

        where:
        rowIndex || expectedValue | scenario
        0        || true          | "True"
        1        || false         | "False"
    }

    void 'read should only import rows after startRow'() {
        given: "we are starting the import on the last row of the file"
        int numRowsInFile = 7
        ExcelReaderConfig config = new ExcelReaderConfig(
                sheetName: "string",
                linesToSkip: numRowsInFile - 1,  // -1 because it is zero-indexed
                columnMapping: ["A": "string"],
        )

        expect: "only one row to be imported"
        assert reader.read(xlsFile, config).rows.size() == 1
    }

    void 'read should fail if given an empty file'() {
        given:
        ExcelReaderConfig config = new ExcelReaderConfig(
                sheetName: "string",
                linesToSkip: 1,
                columnMapping: ["A": "string"],
        )

        when:
        reader.read(new MultipartFileSource(), config)

        then:
        thrown(ValidationException)
    }

    void 'read should fail if given a file type that is not supported by the importer'() {
        given:
        ExcelReaderConfig config = new ExcelReaderConfig(
                sheetName: "string",
                linesToSkip: 1,
                columnMapping: ["A": "string"],
        )

        when:
        reader.read(txtFile, config)

        then:
        thrown(IllegalArgumentException)
    }
}
