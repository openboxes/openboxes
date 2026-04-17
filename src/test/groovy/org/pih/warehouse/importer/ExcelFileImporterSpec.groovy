package org.pih.warehouse.importer

import grails.validation.ValidationException
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import testutil.ResourceUtil

import org.pih.warehouse.core.file.UploadedFile

@Unroll
class ExcelFileImporterSpec extends Specification {

    private static final String TEST_XLS_FILE_PATH = "/testfiles/importer/excel-file-importer-spec.xls"
    private static final String TEST_XLSX_FILE_PATH = "/testfiles/importer/excel-file-importer-spec.xlsx"
    private static final String TEST_TXT_FILE_PATH = "/testfiles/importer/excel-file-importer-spec.txt"

    @Shared
    ExcelFileImporter excelFileImporter

    @Shared
    UploadedFile xlsFile

    @Shared
    UploadedFile xlsxFile

    @Shared
    UploadedFile txtFile

    void setupSpec() {
        xlsFile = new UploadedFile(file: ResourceUtil.getMultiPartFile(TEST_XLS_FILE_PATH))
        xlsxFile = new UploadedFile(file: ResourceUtil.getMultiPartFile(TEST_XLSX_FILE_PATH))
        txtFile = new UploadedFile(file: ResourceUtil.getMultiPartFile(TEST_TXT_FILE_PATH))
    }

    void setup() {
        excelFileImporter = new ExcelFileImporter()
    }

    void 'importFile should successfully import strings from xls file for case: #scenario'() {
        given:
        ExcelFileImporterConfig config = new ExcelFileImporterConfig(
                sheetName: "string",
                startRow: 1,
                columnMapping: ["A": "string"],
        )

        when:
        FileImporterResult result = excelFileImporter.importFile(xlsFile, config)
        List<Map<String, Object>> rows = result.rows

        then:
        assert rows[rowIndex]["string"] == expectedValue

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
    void 'importFile should successfully import strings from xlsx file for case: #scenario'() {
        given:
        ExcelFileImporterConfig config = new ExcelFileImporterConfig(
                sheetName: "string",
                startRow: 1,
                columnMapping: ["A": "string"],
        )

        when:
        FileImporterResult result = excelFileImporter.importFile(xlsxFile, config)
        List<Map<String, Object>> rows = result.rows

        then:
        assert rows[rowIndex]["string"] == expectedValue

        where:
        rowIndex || expectedValue | scenario
        0        || "ABC"         | "Plain string"
        1        || "A1"          | "String ends in number"
        2        || "1A"          | "String starts with number"
        3        || " A"          | "String starts with space"
        4        || "A "          | "String ends with space"
        5        || "苹果"         | "Special characters"
    }

    void 'importFile should successfully import numerics from xls file for case: #scenario'() {
        given:
        ExcelFileImporterConfig config = new ExcelFileImporterConfig(
                sheetName: "numeric",
                startRow: 1,
                columnMapping: ["A": "numeric"],
        )

        when:
        FileImporterResult result = excelFileImporter.importFile(xlsFile, config)

        then:
        assert result.rows[rowIndex]["numeric"] == expectedValue

        where:
        rowIndex || expectedValue | scenario
        0        || 1.0           | "Integer"
        1        || 1.2           | "Double"
        2        || 0.0           | "Epoch Date"   // "1899-12-30" in the file, which is Excel epoch
        3        || 36526.0       | "Modern Date"  // "2000-01-01" in the file, which is 36526 days after Excel epoch
    }

    @Ignore("The Grails plugin makes it hard to add the dependencies required to support XLSX files. Once we remove the plugin and upgrade POI, we can re-enable this test")
    void 'importFile should successfully import numerics from xlsx file for case: #scenario'() {
        given:
        ExcelFileImporterConfig config = new ExcelFileImporterConfig(
                sheetName: "numeric",
                startRow: 1,
                columnMapping: ["A": "numeric"],
        )

        when:
        FileImporterResult result = excelFileImporter.importFile(xlsxFile, config)

        then:
        assert result.rows[rowIndex]["numeric"] == expectedValue

        where:
        rowIndex || expectedValue | scenario
        0        || 1.0           | "Integer"
        1        || 1.2           | "Double"
        2        || 0.0           | "Epoch Date"   // "1899-12-30" in the file, which is Excel epoch
        3        || 36526.0       | "Modern Date"  // "2000-01-01" in the file, which is 36526 days after Excel epoch
    }

    void 'importFile should successfully import booleans from xls file for case: #scenario'() {
        given:
        ExcelFileImporterConfig config = new ExcelFileImporterConfig(
                sheetName: "boolean",
                startRow: 1,
                columnMapping: ["A": "boolean"],
        )

        when:
        FileImporterResult result = excelFileImporter.importFile(xlsFile, config)

        then:
        assert result.rows[rowIndex]["boolean"] == expectedValue

        where:
        rowIndex || expectedValue | scenario
        0        || true          | "True"
        1        || false         | "False"
    }

    @Ignore("The Grails plugin makes it hard to add the dependencies required to support XLSX files. Once we remove the plugin and upgrade POI, we can re-enable this test")
    void 'importFile should successfully import booleans from xlsx file for case: #scenario'() {
        given:
        ExcelFileImporterConfig config = new ExcelFileImporterConfig(
                sheetName: "boolean",
                startRow: 1,
                columnMapping: ["A": "boolean"],
        )

        when:
        FileImporterResult result = excelFileImporter.importFile(xlsxFile, config)

        then:
        assert result.rows[rowIndex]["boolean"] == expectedValue

        where:
        rowIndex || expectedValue | scenario
        0        || true          | "True"
        1        || false         | "False"
    }

    void 'importFile should only import rows after startRow'() {
        given: "we are starting the import on the last row of the file"
        int numRowsInFile = 7
        ExcelFileImporterConfig config = new ExcelFileImporterConfig(
                sheetName: "string",
                startRow: numRowsInFile - 1,  // -1 because it is zero-indexed
                columnMapping: ["A": "string"],
        )

        expect: "only one row to be imported"
        assert excelFileImporter.importFile(xlsFile, config).rows.size() == 1
    }

    void 'importFile should fail if given an empty file'() {
        given:
        ExcelFileImporterConfig config = new ExcelFileImporterConfig(
                sheetName: "string",
                startRow: 1,
                columnMapping: ["A": "string"],
        )

        when:
        excelFileImporter.importFile(new UploadedFile(), config)

        then:
        thrown(ValidationException)
    }

    void 'importFile should fail if given a file type that is not supported by the importer'() {
        given:
        ExcelFileImporterConfig config = new ExcelFileImporterConfig(
                sheetName: "string",
                startRow: 1,
                columnMapping: ["A": "string"],
        )

        when:
        excelFileImporter.importFile(txtFile, config)

        then:
        thrown(IllegalArgumentException)
    }
}
