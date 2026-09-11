package org.pih.warehouse.exporter

import org.apache.commons.lang.StringUtils

import org.pih.warehouse.core.validation.ObjectValidatable

/**
 * Configuration for writing bulk data to files.
 */
class BulkDataWriterConfig implements ObjectValidatable<BulkDataWriterConfigValidator> {

    /**
     * Will be concatenated to construct the file name of the file being output.
     * Only needed if we are writing to a file.
     */
    Collection<Object> fileNameArgs = []

    /**
     * True if we should create a header row (constructed using the field config)
     */
    boolean addHeaderRow = true

    /**
     * The configuration to use when writing each of the fields in the bulk data.
     */
    List<BulkDataWriterFieldConfig> fields = []

    /**
     * @return The field configurations, in the column order that they should be written to.
     */
    List<BulkDataWriterFieldConfig> getFieldConfigsInOrder() {
        return isFieldConfigOrdinal() ? fields : fields.sort { it.columnIndex }
    }

    private boolean isFieldConfigOrdinal() {
        BulkDataWriterFieldConfig fieldConfig = fields.find()
        return !fieldConfig || StringUtils.isBlank(fieldConfig.columnIndex)
    }
}
