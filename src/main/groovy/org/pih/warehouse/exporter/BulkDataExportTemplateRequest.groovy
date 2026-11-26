package org.pih.warehouse.exporter

import org.pih.warehouse.core.validation.ObjectValidatable
import org.pih.warehouse.importer.BulkDataType

class BulkDataExportTemplateRequest implements ObjectValidatable {

    /**
     * The feature associated with the data being exported.
     * Is used to fetch the default bulk data configuration.
     */
    BulkDataType dataType
}
