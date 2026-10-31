package org.pih.warehouse.exporter

import org.pih.warehouse.core.formatter.Formatter
import org.pih.warehouse.core.formatter.FormatterContext
import org.pih.warehouse.importer.BulkDataColumnIndex

/**
 * Field-specific configuration for writing bulk data to files.
 */
class BulkDataWriterFieldConfig {

    /**
     * The name of the field in the source object.
     */
    String fieldName

    /**
     * The index of the column in the output document. Can be a zero-indexed numerical (ex: "0", "1", "10")
     * or a letter (ex: "A", "B", "AA"). This should match the column indexes defined in
     * {@link org.pih.warehouse.importer.BulkDataReaderConfig}.
     */
    String columnIndex

    /**
     * A non-localizable, plain text String value to use as the column header in the output document.
     * Overridden by headerMessageCode if it is specified.
     */
    String headerPlainText

    /**
     * A localizable message code to use as the column header in the output document.
     * If specified, will override headerPlainText
     */
    String headerMessageCode

    /**
     * Overrides the formatter class to use when binding the field.
     *
     * This is only required if you don't want to use the default formatter associated with the field.
     */
    Class<Formatter> formatter

    /**
     * Defines any custom behaviour when formatting the field.
     *
     * If null, will use the default context for the formatter.
     */
    FormatterContext formatterContext

    /**
     * @return The zero-indexed numerical representation of the column, or null if no columnIndex is specified.
     * @throws IllegalArgumentException if the columnIndex is neither all digits nor all letters.
     */
    Integer resolveColumnIndex() {
        return BulkDataColumnIndex.toInt(columnIndex)
    }
}
