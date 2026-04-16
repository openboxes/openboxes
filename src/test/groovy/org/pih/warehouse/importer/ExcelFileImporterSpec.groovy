package org.pih.warehouse.importer

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

    @Shared
    ExcelFileImporter excelFileImporter

    @Shared
    UploadedFile xlsFile

    @Shared
    UploadedFile xlsxFile

    void setupSpec() {
        xlsFile = new UploadedFile(file: ResourceUtil.getMultiPartFile(TEST_XLS_FILE_PATH))
        xlsxFile = new UploadedFile(file: ResourceUtil.getMultiPartFile(TEST_XLSX_FILE_PATH))
    }

    void setup() {
        excelFileImporter = new ExcelFileImporter()
    }

    void 'importFile should successfully import strings from xls file for case: #scenario'() {
        given:
        ExcelFileImporterConfig config = new ExcelFileImporterConfig(
                sheetName: "string",
                startRow: 1,
                columnToFieldMapping: ["A": "string"],
        )

        when:
        FileImportResult result = excelFileImporter.importFile(xlsFile, config)
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
    }

    @Ignore("The Grails plugin makes it hard to add the dependencies required to support XLSX files. Once we remove the plugin and upgrade POI, we can re-enable this test")
    void 'importFile should successfully import strings from xlsx file for case: #scenario'() {
        given:
        ExcelFileImporterConfig config = new ExcelFileImporterConfig(
                sheetName: "string",
                startRow: 1,
                columnToFieldMapping: ["A": "string"],
        )

        when:
        FileImportResult result = excelFileImporter.importFile(xlsxFile, config)
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
    }

    void 'importFile should successfully import numerics from xls file for case: #scenario'() {
        given:
        ExcelFileImporterConfig config = new ExcelFileImporterConfig(
                sheetName: "numeric",
                startRow: 1,
                columnToFieldMapping: ["A": "numeric"],
        )

        when:
        FileImportResult result = excelFileImporter.importFile(xlsFile, config)

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
                columnToFieldMapping: ["A": "numeric"],
        )

        when:
        FileImportResult result = excelFileImporter.importFile(xlsxFile, config)

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
                columnToFieldMapping: ["A": "boolean"],
        )

        when:
        FileImportResult result = excelFileImporter.importFile(xlsFile, config)

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
                columnToFieldMapping: ["A": "boolean"],
        )

        when:
        FileImportResult result = excelFileImporter.importFile(xlsxFile, config)

        then:
        assert result.rows[rowIndex]["boolean"] == expectedValue

        where:
        rowIndex || expectedValue | scenario
        0        || true          | "True"
        1        || false         | "False"
    }
}
