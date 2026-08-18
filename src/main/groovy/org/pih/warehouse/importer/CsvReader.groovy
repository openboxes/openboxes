package org.pih.warehouse.importer

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import org.apache.commons.lang3.StringUtils
import org.apache.poi.ss.util.CellReference
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

import org.pih.warehouse.core.http.ContentType
import org.pih.warehouse.core.localization.MessageLocalizer

/**
 * Reads in CSV a source object, capturing it as a List of Map rows.
 */
@Component
class CsvReader extends BulkDataReader<CsvReaderConfig> {

    final private MessageLocalizer messageLocalizer

    CsvReader(@Lazy final BulkDataImportComponentResolver componentResolver,
              final MessageLocalizer messageLocalizer) {
        super(componentResolver)
        this.messageLocalizer = messageLocalizer
    }

    @Override
    List<ContentType> getSupportedContentTypes() {
        return [ContentType.CSV]
    }

    @Override
    protected BulkDataReaderResult doRead(BulkDataSource source, CsvReaderConfig config) {
        InputStreamReader reader = null
        try {
            BulkDataReaderResult result = new BulkDataReaderResult()

            reader = new InputStreamReader(source.asInputStream(), config.charset)
            Iterable<CSVRecord> csvRows = CSVFormat.DEFAULT.builder()
                    .setDelimiter(config.delimiter)
                    .build()
                    .parse(reader)

            Map<String, String> columnToFieldMap = config.columnMapping
            List<Map<String, BulkDataCell>> readRows = []
            for (CSVRecord csvRow : csvRows) {
                if (csvRow.recordNumber <= config.linesToSkip) {  // recordNumber is 1-indexed
                    continue
                }

                int rowIndex = (int) (csvRow.recordNumber - 1)  // recordNumber is 1-indexed

                // If a row has fewer columns than we expect, error.
                if (csvRow.size() < columnToFieldMap.size()) {
                    result.readErrors.add(new BulkDataError(
                            row: rowIndex,
                            severity: BulkDataErrorSeverity.ERROR,
                            localizedMessage: messageLocalizer.localize(
                                    "import.reader.unexpectedNumberCells", [columnToFieldMap.size(), csvRow.size()]),
                    ))
                }

                Map<String, BulkDataCell> readRow = [:]
                for (int i = 0; i < csvRow.size(); i++) {
                    // Only bother importing cells whose columns are specified in the config
                    String fieldName = getFieldName(i, columnToFieldMap)
                    if (StringUtils.isBlank(fieldName)) {
                        continue
                    }

                    // Read in the cell as a String. Sanitizing and type parsing will be done in the data binding step.
                    readRow.put(fieldName, new BulkDataCell(
                            row: rowIndex,
                            column: i,
                            fieldName: fieldName,
                            value: csvRow.get(i)
                    ))
                }
                readRows.add(readRow)
            }
            result.rows = readRows
            return result
        } finally {
            reader?.close()
        }
    }

    /**
     * Extract the field name from the column mapping config for the given column index.
     *
     * We allow our columns in our mapping config to be represented as either zero-indexed numerical keys,
     * or as letters (as they appear in Excel). Ex: The first column can be represented as "0" or "A".
     */
    private String getFieldName(int columnIndex, Map<String, String> columnMapping) {
        return columnMapping.get(String.valueOf(columnIndex)) ?:
                columnMapping.get(CellReference.convertNumToColString(columnIndex))
    }
}
