package org.pih.warehouse.exporter

import org.pih.warehouse.core.date.DateFieldType
import org.pih.warehouse.core.file.FileNamePart

/**
 * Wraps all export configuration options for a feature, unifying the CSV and Excel writer configuration into
 * a single config for the sake of simplicity.
 *
 * Designed to be used by implementations of {@link BulkDataExportConfigurer}. This is the export-only equivalent
 * of {@link org.pih.warehouse.importer.BulkDataImportExportConfig}.
 */
class BulkDataExportConfig {

    private List<BulkDataWriterFieldConfig> fieldConfigs = []
    private List<Object> fileNameArgs = []

    static BulkDataExportConfigBuilder builder() {
        return new BulkDataExportConfigBuilder()
    }

    /**
     * Configuration for writing each field of the object. The order of configs in the list determines
     * the order of the columns in the resulting export document.
     */
    List<BulkDataWriterFieldConfig> getFieldConfigs() {
        return fieldConfigs
    }

    /**
     * The fields to join together to construct the exported file name. Can include {@link FileNamePart} placeholders
     * (such as {@link FileNamePart#CURRENT_TIME}) for parts that change per file. Empty means use the default name.
     */
    List<Object> getFileNameArgs() {
        return fileNameArgs
    }

    static class BulkDataExportConfigBuilder {

        BulkDataExportConfig config = new BulkDataExportConfig()

        /**
         * Sets the fields to join together to construct the exported file name.
         *
         * Use {@link FileNamePart} placeholders for parts that change per file. Do NOT pass values like
         * Instant.now() directly because the config is only ever built once.
         *
         * Ex: fileName("CycleCountReport", FileNamePart.CURRENT_TIME) -> "CycleCountReport_20000101-130000.csv"
         */
        BulkDataExportConfigBuilder fileName(Object... args) {
            config.fileNameArgs.clear()
            config.fileNameArgs.addAll(args)
            return this
        }

        /**
         * Adds a column with a localized header.
         *
         * For java.util.Date fields, use {@link #dateField} instead.
         *
         * @param fieldName The name of the field in the (unbound) row being exported.
         * @param headerMessageCode The localization message code of the header cell.
         */
        BulkDataExportConfigBuilder field(String fieldName, String headerMessageCode) {
            return fieldConfig(new BulkDataWriterFieldConfig(
                    fieldName: fieldName,
                    headerMessageCode: headerMessageCode,
            ))
        }

        /**
         * Adds a column with a non-localized header.
         *
         * For java.util.Date fields, use {@link #dateFieldUnlocalized} instead.
         *
         * @param fieldName The name of the field in the (unbound) row being exported.
         * @param headerPlainText The plain text value of the header cell.
         */
        BulkDataExportConfigBuilder fieldUnlocalized(String fieldName, String headerPlainText) {
            return fieldConfig(new BulkDataWriterFieldConfig(
                    fieldName: fieldName,
                    headerPlainText: headerPlainText,
            ))
        }

        /**
         * Adds a java.util.Date column with a localized header.
         *
         * @param fieldName The name of the field in the (unbound) row being exported.
         * @param headerMessageCode The localization message code of the header cell.
         * @param dateFieldType LOCAL_DATE for date-only fields or INSTANT for datetime fields.
         */
        BulkDataExportConfigBuilder dateField(String fieldName,
                                              String headerMessageCode,
                                              DateFieldType dateFieldType) {
            return fieldConfig(new BulkDataWriterFieldConfig(
                    fieldName: fieldName,
                    headerMessageCode: headerMessageCode,
                    dateFieldType: requireDateFieldType(fieldName, dateFieldType),
            ))
        }

        /**
         * Adds a java.util.Date column with a non-localized header.
         *
         * @param fieldName The name of the field in the (unbound) row being exported.
         * @param headerPlainText The plain text value of the header cell.
         * @param dateFieldType LOCAL_DATE for date-only fields or INSTANT for datetime fields.
         */
        BulkDataExportConfigBuilder dateFieldUnlocalized(String fieldName,
                                                         String headerPlainText,
                                                         DateFieldType dateFieldType) {
            return fieldConfig(new BulkDataWriterFieldConfig(
                    fieldName: fieldName,
                    headerPlainText: headerPlainText,
                    dateFieldType: requireDateFieldType(fieldName, dateFieldType),
            ))
        }

        BulkDataExportConfig build() {
            return config
        }

        /**
         * Adds a column from a fully specified field config. Columns are written in the order that they are added.
         *
         * Prefer the other builder methods where possible. This exists for when the field config needs options that
         * those methods don't expose (such as a columnIndex, which BulkDataImportExportConfig uses).
         */
        BulkDataExportConfigBuilder fieldConfig(BulkDataWriterFieldConfig fieldConfig) {
            if (config.fieldConfigs.any { it.fieldName == fieldConfig.fieldName }) {
                throw new IllegalArgumentException("Field '${fieldConfig.fieldName}' was added more than once.")
            }
            config.fieldConfigs.add(fieldConfig)
            return this
        }

        private DateFieldType requireDateFieldType(String fieldName, DateFieldType dateFieldType) {
            if (dateFieldType == null) {
                throw new IllegalArgumentException("Date field '${fieldName}' must specify a dateFieldType.")
            }
            return dateFieldType
        }
    }
}
