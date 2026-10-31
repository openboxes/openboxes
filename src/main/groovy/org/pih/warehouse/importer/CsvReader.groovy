package org.pih.warehouse.importer

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import org.apache.commons.lang3.StringUtils
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

            Map<Integer, String> columnToFieldMap = BulkDataColumnIndex.toIntKeys(config.columnMapping)

            // Every defined column must be present in the row, so a row needs at least as many cells as the highest
            // index (+1 because integer column indexes are zero-indexed).
            int expectedNumberOfCells = columnToFieldMap ? columnToFieldMap.keySet().max() + 1 : 0

            List<Map<String, BulkDataCell>> readRows = []
            for (CSVRecord csvRow : csvRows) {
                if (csvRow.recordNumber <= config.linesToSkip) {  // recordNumber is 1-indexed
                    continue
                }

                int rowIndex = (int) (csvRow.recordNumber - 1)  // recordNumber is 1-indexed

                // If a row has fewer columns than we expect, error.
                if (csvRow.size() < expectedNumberOfCells) {
                    result.readErrors.add(new BulkDataError(
                            row: rowIndex,
                            severity: BulkDataErrorSeverity.ERROR,
                            localizedMessage: messageLocalizer.localize(
                                    "import.reader.unexpectedNumberCells", [expectedNumberOfCells, csvRow.size()]),
                    ))
                }

                Map<String, BulkDataCell> readRow = [:]
                for (int i = 0; i < csvRow.size(); i++) {
                    // Only bother importing cells whose columns are specified in the config
                    String fieldName = columnToFieldMap.get(i)
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
}
