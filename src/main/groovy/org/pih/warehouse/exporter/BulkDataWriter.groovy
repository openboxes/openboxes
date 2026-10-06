package org.pih.warehouse.exporter

import grails.validation.ValidationException
import org.apache.commons.lang.StringUtils
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Lazy

import org.pih.warehouse.core.formatter.DefaultTypeFormatter
import org.pih.warehouse.core.formatter.Formatter
import org.pih.warehouse.core.http.ContentType
import org.pih.warehouse.core.localization.MessageLocalizer
import org.pih.warehouse.importer.BulkDataType

/**
 * Writes a List of rows to an output document, often a file.
 */
abstract class BulkDataWriter<OutputType, Config extends BulkDataWriterConfig> {

    final ApplicationContext context
    final BulkDataExportComponentResolver componentResolver
    final DefaultTypeFormatter defaultTypeFormatter
    final MessageLocalizer messageLocalizer

    // The component resolver is annotated with @Lazy because it wires in the writers, creating a circular dependency.
    // Fortunately the writer doesn't immediately use the component resolver so we can simply delay fetching it.
    BulkDataWriter(final ApplicationContext context,
                   @Lazy final BulkDataExportComponentResolver componentResolver,
                   final DefaultTypeFormatter defaultTypeFormatter,
                   final MessageLocalizer messageLocalizer) {
        this.context = context
        this.componentResolver = componentResolver
        this.defaultTypeFormatter = defaultTypeFormatter
        this.messageLocalizer = messageLocalizer
    }

    /**
     * @return The list of content types that the writer can handle.
     */
    abstract List<ContentType> getSupportedContentTypes()

    /**
     * @return The value to put in a cell to represent a null/empty cell.
     */
    abstract Object getEmptyCellValue()

    /**
     * Writes the given rows to an output document of the given content type.
     * Contains the writer logic that is specific to the bulk data writer implementation.
     *
     * @param rowsToWrite The list of "unbound" objects to be converted to rows in the output document.
     * @param contentType The file type of the output document.
     * @param config Configuration for how the writer should process the rows.
     */
    protected abstract BulkDataWriterResult<OutputType> doWrite(List<Map<String, Object>> rowsToWrite,
                                                                ContentType contentType,
                                                                Config config)

    /**
     * Writes the given rows to an output document of the given content type.
     *
     * For use when we want to provide custom writer configuration that overrides the default.
     *
     * @param rowsToWrite The list of "unbound" objects to be converted to rows in the output document.
     * @param contentType The file type of the output document.
     * @param config Configuration for how the writer should process the rows.
     */
    BulkDataWriterResult<OutputType> write(List<Map<String, Object>> rowsToWrite,
                                           ContentType contentType,
                                           Config config) {
        validateConfig(contentType, config)
        return doWrite(rowsToWrite, contentType, config)
    }

    /**
     * Writes the given rows to an output document of the given content type.
     *
     * For use when we want to rely on the default writer configuration for a given bulk data type.
     *
     * @param rowsToWrite The list of "unbound" objects to be converted to rows in the output document.
     * @param bulkDataType Determines which configurer to use when writing the data.
     * @param contentType The file type of the output document.
     */
    BulkDataWriterResult<OutputType> write(List<Map<String, Object>> rowsToWrite,
                                           BulkDataType bulkDataType,
                                           ContentType contentType) {
        Config writerConfig = componentResolver.getBulkDataWriterConfig(bulkDataType, contentType) as Config
        return write(rowsToWrite, contentType, writerConfig)
    }


    private void validateConfig(ContentType contentType, Config config) {
        if (!config?.validate()) {
            throw new ValidationException("Config is invalid", config?.errors)
        }

        if (!supportedContentTypes.contains(contentType)) {
            throw new IllegalArgumentException("Writer does not support content-type ${contentType}. " +
                    "Only the following content-types are allowed: ${supportedContentTypes}")
        }
    }

    /**
     * @return A header row to be written to the output.
     */
    protected List<String> buildHeaderRow(List<Map<String, Object>> rowsToWrite, Config config) {
        List<BulkDataWriterFieldConfig> fieldConfigsOrdered = getOrderedFieldConfigs(rowsToWrite, config)
        List<String> headerRow = []
        for (fieldConfig in fieldConfigsOrdered) {
            if (StringUtils.isNotBlank(fieldConfig.headerMessageCode)) {
                headerRow.add(messageLocalizer.localize(fieldConfig.headerMessageCode))
                continue
            }
            if (StringUtils.isNotBlank(fieldConfig.headerPlainText)) {
                headerRow.add(fieldConfig.headerPlainText)
                continue
            }
            headerRow.add(fieldConfig.fieldName)
        }
        return headerRow
    }

    /**
     * @return An ordered list of field configs. If no config exists, will construct a basic one from the field
     *         names of one of the rows.
     */
    protected List<BulkDataWriterFieldConfig> getOrderedFieldConfigs(List<Map<String, Object>> rowsToWrite,
                                                                     Config config) {
        Map<String, Object> exampleRow = rowsToWrite?.find()
        return config.fields ? config.fieldConfigsInOrder: initFieldConfigFromRow(exampleRow)
    }

    private List<BulkDataWriterFieldConfig> initFieldConfigFromRow(Map<String, Object> exampleRow) {
        if (!exampleRow) {
            return []
        }
        return exampleRow.keySet().collect { new BulkDataWriterFieldConfig(fieldName: it) }
    }

    /**
     * @return A cell value formatted for use in the output.
     */
    protected Object formatCellValue(Object value, BulkDataWriterFieldConfig config) {
        if (value == null) {
            return emptyCellValue
        }

        // If the config contains a specific Formatter to use for the field, use it.
        if (config?.formatter != null) {
            Formatter formatter = context.getBean(config.formatter)
            return formatter.format(value, config.formatterContext)
        }

        // If a default formatter is associated with the type, use it.
        try {
            return defaultTypeFormatter.format(value)
        } catch (IllegalArgumentException ignore) {
            // There was no default formatter for the value type.
        }

        // Otherwise simply stringify the value.
        return value.toString()
    }
}
