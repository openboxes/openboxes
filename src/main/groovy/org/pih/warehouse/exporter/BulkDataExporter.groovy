package org.pih.warehouse.exporter

import org.springframework.stereotype.Component

import org.pih.warehouse.core.http.ContentType
import org.pih.warehouse.importer.BulkDataType

/**
 * Exports bulk data to an output source (typically a file).
 * Manages the full flow through an export.
 */
@Component
class BulkDataExporter {

    final BulkDataUnbinder bulkDataUnbinder
    final BulkDataExportComponentResolver bulkDataExportComponentResolver

    BulkDataExporter(final BulkDataUnbinder bulkDataUnbinder,
                     final BulkDataExportComponentResolver bulkDataExportComponentResolver) {
        this.bulkDataUnbinder = bulkDataUnbinder
        this.bulkDataExportComponentResolver = bulkDataExportComponentResolver
    }

    /**
     * Takes in a List of objects and writes them to an output document of the given content type.
     */
    BulkDataWriterResult exportData(BulkDataExportRequest request) {
        List<Map<String, Object>> unboundRows = bulkDataUnbinder.unbindData(request.toExport ?: [])
        return writeFile(unboundRows, request.contentType, request.dataType)
    }

    private BulkDataWriterResult writeFile(List<Map<String, Object>> rows,
                                           ContentType contentType,
                                           BulkDataType bulkDataType) {

        BulkDataWriter writer = bulkDataExportComponentResolver.getBulkDataWriter(contentType)
        if (!writer) {
            throw new RuntimeException("No bulk data writer was found for content type ${contentType}")
        }

        // Without a data type there's no config to fetch, so the writer uses its default config.
        BulkDataWriterConfig config = bulkDataType ?
                bulkDataExportComponentResolver.getBulkDataWriterConfig(bulkDataType, contentType) :
                null

        return writer.write(rows, contentType, config)
    }
}
