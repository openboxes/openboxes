package org.pih.warehouse.api

import javax.validation.Valid

import org.pih.warehouse.core.http.ContentType
import org.pih.warehouse.core.http.HttpResponseContext
import org.pih.warehouse.exporter.BulkDataExportTemplateRequest

class ExportApiController extends BaseApiController {

    /**
     * The content types that bulk data can be exported to.
     */
    private static final List<ContentType> EXPORTABLE_CONTENT_TYPES = [
            ContentType.CSV,
            ContentType.XLS,
            ContentType.XLSX,
    ]

    /**
     * Generic API for exporting blank, bulk data templates.
     */
    def exportTemplate(@Valid BulkDataExportTemplateRequest command) {
        if (!(contentType in EXPORTABLE_CONTENT_TYPES)) {
            throw new UnsupportedOperationException("This endpoint is designed for data exports. Please specify an " +
                    "exportable content type (such as 'csv' or 'xls') via the format=* query parameter.")
        }

        // It's strange to not call into a service here, but the exporter associated with the given dataType
        // will automatically be invoked as a part of rendering the response.
        renderResponse(HttpResponseContext.builder()
                // Templates have no data, only headers, so we pass in an empty dataset.
                .forExport([], command.dataType)
                .build())
    }
}
