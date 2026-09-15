package org.pih.warehouse.exporter

/**
 * The result of writing to a CSV String.
 */
class CsvWriterResult extends BulkDataWriterResult<String> {

    @Override
    void close() {
        // The CSV writer outputs a String and the CSVPrinter is closed automatically by the writer
        // so there's nothing left to do.
    }
}
