package org.pih.warehouse.importer

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component

import org.pih.warehouse.core.date.EpochDate
import org.pih.warehouse.core.http.ContentType

/**
 * Reads in CSV a source object, capturing it as a List of Map rows.
 */
@Component
class CsvReader extends BulkDataReader<CsvReaderConfig> {

    @Override
    List<ContentType> getSupportedContentTypes() {
        return [ContentType.CSV]
    }

    @Override
    protected BulkDataReaderResult doRead(BulkDataSource source, CsvReaderConfig config) {
        InputStreamReader reader = null
        try {
            reader = new InputStreamReader(source.asInputStream(), config.charset)
            Iterable<CSVRecord> csvRows = CSVFormat.DEFAULT.builder()
                    .setDelimiter(config.delimiter)
                    .build()
                    .parse(reader)

            Map<String, String> columnToFieldMap = config.columnMapping
            List<Map<String, Object>> readRows = []
            for (CSVRecord csvRow : csvRows) {
                if (csvRow.recordNumber <= config.linesToSkip) {  // recordNumber is 1-index based
                    continue
                }

                Map<String, Object> readRow = [:]
                for (int i = 0; i < csvRow.size(); i++) {
                    // Only bother importing cells whose columns are specified in the config
                    String fieldName = columnToFieldMap.get(String.valueOf(i))
                    if (StringUtils.isBlank(fieldName)) {
                        continue
                    }

                    // Read in the cell as a String. Sanitizing and type parsing will be done in the data binding step.
                    readRow.put(fieldName, csvRow.get(i))
                }
                readRows.add(readRow)
            }

            return new BulkDataReaderResult(
                    rows: readRows,
                    // CSVs are unlikely to contain dates in the Double format that Excel uses, so the "epochDate"
                    // is unlikely to matter for CSVs. Typically CSV exports contain the stringified version of
                    // the date. For clarity, we set the value to the Unix epoch, but this probably won't be used.
                    epochDate: EpochDate.UNIX_EPOCH,
            )
        } finally {
            reader?.close()
        }
    }
}
