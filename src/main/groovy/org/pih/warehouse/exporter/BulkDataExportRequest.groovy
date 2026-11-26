package org.pih.warehouse.exporter

import org.pih.warehouse.core.http.ContentType
import org.pih.warehouse.core.validation.ObjectValidatable
import org.pih.warehouse.importer.BulkDataType

class BulkDataExportRequest implements ObjectValidatable {

    /**
     * The feature associated with the data being exported.
     * Is used to fetch the default bulk data configuration.
     */
    BulkDataType dataType

    /**
     * The desired content type that the bulk data should be formatted to.
     */
    ContentType contentType

    /**
     * The bulk data rows to be exported.
     */
    List<Object> toExport
}
