package org.pih.warehouse.importer

import java.nio.charset.StandardCharsets
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import org.apache.commons.lang.StringUtils

import org.pih.warehouse.core.date.EpochDate
import org.pih.warehouse.core.file.UploadedFile
import org.pih.warehouse.core.file.FileExtension

/**
 * Imports a CSV file, capturing it as a List of Map rows.
 */
class CsvFileImporter implements FileImporter<CsvFileImporterConfig> {

    @Override
    List<FileExtension> getSupportedFileExtensions() {
        return [FileExtension.CSV]
    }

    @Override
    FileImportResult importFile(UploadedFile file, CsvFileImporterConfig config) {
        try {
            // Read in the file as a collection of rows
            Iterable<CSVRecord> csvRows = CSVFormat.DEFAULT.builder()
                    .setDelimiter(config.delimiter)
                    .build()
                    .parse(new InputStreamReader(file.file.getInputStream(), StandardCharsets.UTF_8))

            Map<String, String> columnToFieldMap = config.columnToFieldMapping
            List<Map<String, Object>> importedRows = []
            for (CSVRecord csvRow : csvRows) {
                if (csvRow.recordNumber <= config.startRow) {  // recordNumber is 1-indexed, startRow is 0-indexed
                    continue
                }

                Map<String, Object> importedRow = [:]
                for (int i = 0; i < csvRow.size(); i++) {
                    // Only bother importing cells whose columns are specified in the config
                    String fieldName = columnToFieldMap.get(String.valueOf(i))
                    if (StringUtils.isBlank(fieldName)) {
                        continue
                    }

                    // Read in the cell as a String. Sanitizing and type parsing will be done in the data binding step.
                    importedRow.put(fieldName, csvRow.get(i))
                }
                importedRows.add(importedRow)
            }

            return new FileImportResult(
                    rows: importedRows,
                    // CSVs are unlikely to contain dates in the Double format that Excel uses, so the "epochDate"
                    // is unlikely to matter for CSVs. Typically CSV exports contain the stringified version of
                    // the date. For clarity, we set the value to the Unix epoch, but this won't actually be used.
                    epochDate: EpochDate.UNIX_EPOCH,
            )

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage())
        }
    }
}
