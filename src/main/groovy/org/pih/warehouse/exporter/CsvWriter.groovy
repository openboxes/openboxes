package org.pih.warehouse.exporter

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.lang.StringEscapeUtils
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

import org.pih.warehouse.core.formatter.DefaultTypeFormatter
import org.pih.warehouse.core.http.ContentType
import org.pih.warehouse.core.localization.MessageLocalizer
import org.pih.warehouse.importer.CSVUtils

/**
 * For converting objects into a CSV formatted String.
 */
@Component
class CsvWriter extends BulkDataWriter<String, CsvWriterConfig> {

    // The component resolver is annotated with @Lazy because it wires in the writers, creating a circular dependency.
    // Fortunately the writer doesn't immediately use the component resolver so we can simply delay fetching it.
    CsvWriter(final ApplicationContext context,
              @Lazy final BulkDataExportComponentResolver componentResolver,
              final DefaultTypeFormatter defaultTypeFormatter,
              final MessageLocalizer messageLocalizer) {
        super(context, componentResolver, defaultTypeFormatter, messageLocalizer)
    }

    @Override
    List<ContentType> getSupportedContentTypes() {
        return [ContentType.CSV]
    }

    @Override
    def getEmptyCellValue() {
        // CSVs don't handle nulls gracefully so we need to set a non-null cell value to create a blank field.
        return ''
    }

    @Override
    BulkDataWriterResult<String> doWrite(List<Map<String, Object>> rowsToWrite,
                                         ContentType contentType,
                                         CsvWriterConfig config) {
        // CSVPrinter writes data row by row (instead of keeping the whole thing in memory) and so does not have
        // memory issues with large data sets.
        CSVPrinter printer = null
        try {
            StringWriter stringWriter = new StringWriter()
            printer = new CSVPrinter(stringWriter, CSVFormat.DEFAULT.builder()
                    .setDelimiter(config.delimiter)
                    .build())

            if (config.addHeaderRow) {
                printer.printRecord(buildHeaderRow(rowsToWrite, config))
            }

            List<BulkDataWriterFieldConfig> fieldConfigsOrdered = getOrderedFieldConfigs(rowsToWrite, config)
            for (rowToWrite in rowsToWrite) {
                List<String> formattedRow = []
                for (fieldConfig in fieldConfigsOrdered) {
                    Object cellValue = rowToWrite.get(fieldConfig.fieldName)
                    String cellValueFormatted = formatCellValue(cellValue, fieldConfig)
                    formattedRow.add(StringEscapeUtils.escapeCsv(cellValueFormatted))
                }
                printer.printRecord(formattedRow)
            }

            // BOM == Byte Order Mark. A "Zero-Width No-Break Space" (ZWNBSP) unicode character that tells programs
            // like Excel that the CSV is UTF-8 encoded.
            return new CsvWriterResult(result: CSVUtils.prependBomToCsvString(stringWriter.toString()))
        } finally {
            // The StringWriter doesn't need to be closed but the CSVPrinter does.
            printer?.close()
        }
    }
}
