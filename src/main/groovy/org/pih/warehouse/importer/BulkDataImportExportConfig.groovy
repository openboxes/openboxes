package org.pih.warehouse.importer

import java.lang.reflect.Field
import org.springframework.util.ReflectionUtils

import org.pih.warehouse.core.date.DateFieldType
import org.pih.warehouse.core.parser.Parser
import org.pih.warehouse.core.parser.ParserContext
import org.pih.warehouse.exporter.BulkDataExportConfig
import org.pih.warehouse.exporter.BulkDataWriterFieldConfig

/**
 * Wraps all import and export configuration options, unifying them into a single config for the sake of simplicity.
 *
 * Designed to be used by implementations of {@link BulkDataImportExportConfigurer}. The export half of the config
 * is a regular {@link BulkDataExportConfig}, so it is handled the same as for export-only features.
 */
class BulkDataImportExportConfig {

    private Map<String, String> columnMapping = [:]
    private Map<String, BulkDataBinderFieldConfig> binderFieldConfigs = [:]
    private BulkDataExportConfig exportConfig

    static BulkDataImportExportConfigBuilder builder(final Class importableType) {
        return new BulkDataImportExportConfigBuilder(importableType)
    }

    /**
     * Maps column indexes to a field/property name.
     *
     * In this mapping, columns can be represented as either zero-indexed numerical keys, or as letters (which is how
     * they appear in Excel). The first column can be represented as "0" or "A", the second as "1" or "B", ...
     *
     * For example: ["0": "field0", "1": "field1", ...]
     *
     * This logic assumes that fields the export and import files map to the same columns and so can share
     * a column mapping.
     */
    Map<String, String> getColumnMapping() {
        return columnMapping
    }

    /**
     * Configuration for binding each of the fields of the object. Keyed on field name.
     */
    Map<String, BulkDataBinderFieldConfig> getBinderFieldConfigs() {
        return binderFieldConfigs
    }

    /**
     * Configuration for exporting the object (the writer field configs and file name).
     */
    BulkDataExportConfig getExportConfig() {
        return exportConfig
    }

    /**
     * Configuration for writing each field of the object, in the order that they were added.
     * A shortcut for exportConfig.fieldConfigs.
     */
    List<BulkDataWriterFieldConfig> getWriterFieldConfigs() {
        return exportConfig.fieldConfigs
    }

    /**
     * The fields to join together to construct the exported file name.
     */
    List<Object> getFileNameArgs() {
        return exportConfig.fileNameArgs
    }

    static class BulkDataImportExportConfigBuilder {

        Class importableType
        BulkDataImportExportConfig config

        /**
         * Builds the export half of the config. We delegate to the export builder so that the export logic
         * (such as validating fields and setting the file name) is shared with export-only features.
         */
        private final BulkDataExportConfig.BulkDataExportConfigBuilder exportConfigBuilder =
                BulkDataExportConfig.builder()

        BulkDataImportExportConfigBuilder(final Class importableType) {
            this.importableType = importableType
            this.config = new BulkDataImportExportConfig()
        }

        /**
         * Sets the fields to join together to construct the exported file name.
         * See {@link BulkDataExportConfig.BulkDataExportConfigBuilder#fileName}.
         */
        BulkDataImportExportConfigBuilder fileName(Object... args) {
            exportConfigBuilder.fileName(args)
            return this
        }

        /**
         * Adds configuration for a field that will be automatically bound via a {@link Parser} that is either
         * explicitly given or determined based on the field type.
         *
         * For java.util.Date fields, use {@link #autoBoundDateField} instead.
         *
         * @param columnIndex The numerical or letter index of the column in the document.
         * @param fieldName The name of the field in the object.
         * @param headerMessageCode The localization message code of the header cell.
         * @param parser The Parser to use when binding the field
         * @param parserContext Defines any custom behaviour when parsing the field.
         */
        BulkDataImportExportConfigBuilder autoBoundField(String columnIndex,
                                                         String fieldName,
                                                         String headerMessageCode,
                                                         Class<Parser> parser=null,
                                                         ParserContext parserContext=null) {

            Field field = ReflectionUtils.findField(importableType, fieldName)
            rejectJavaUtilDateField(field, "autoBoundDateField")

            BulkDataBinderFieldConfig binderConfig
            if (parser) {
                binderConfig = new BulkDataBinderFieldConfig(parser: parser, parserContext: parserContext)
            }
            else {
                if (!field) {
                    throw new IllegalArgumentException(
                            "No field '${fieldName}' on ${importableType.name} or its superclasses")
                }

                // We have custom data-binding handling for when the field is an enum.
                Class fieldType = field.type
                binderConfig = fieldType.isEnum()
                        ? BulkDataBinderFieldConfigDefaults.buildDefaultEnumFieldConfig(fieldType)
                        : BulkDataBinderFieldConfigDefaults.DEFAULT_CONFIG
            }

            return addField(binderConfig, columnIndex, fieldName, headerMessageCode)
        }

        /**
         * Adds configuration for a java.util.Date field that will be automatically bound.
         *
         * @param columnIndex The numerical or letter index of the column in the document.
         * @param fieldName The name of the java.util.Date field in the object.
         * @param headerMessageCode The localization message code of the header cell.
         * @param dateFieldType LOCAL_DATE for date-only fields or INSTANT for datetime fields.
         */
        BulkDataImportExportConfigBuilder autoBoundDateField(String columnIndex,
                                                             String fieldName,
                                                             String headerMessageCode,
                                                             DateFieldType dateFieldType) {

            requireJavaUtilDateField(fieldName, dateFieldType)
            BulkDataBinderFieldConfig binderConfig = new BulkDataBinderFieldConfig(dateFieldType: dateFieldType)
            return addField(binderConfig, columnIndex, fieldName, headerMessageCode, dateFieldType)
        }

        /**
         * Adds configuration for a field that will be manually bound. By definition, manually bound fields will
         * not be bound automatically, so we expect a binding for them to be defined in customBindDataAcrossRows,
         * or customBindDataRow.
         *
         * @param columnIndex The numerical or letter index of the column in the document.
         * @param fieldName The name of the field in the object.
         * @param headerMessageCode The localization message code of the header cell.
         */
        BulkDataImportExportConfigBuilder manuallyBoundField(String columnIndex,
                                                             String fieldName,
                                                             String headerMessageCode) {

            return addField(BulkDataBinderFieldConfigDefaults.MANUALLY_BOUND, columnIndex, fieldName, headerMessageCode)
        }

        private BulkDataImportExportConfigBuilder addField(BulkDataBinderFieldConfig binderConfig,
                                                           String columnIndex,
                                                           String fieldName,
                                                           String headerMessageCode,
                                                           DateFieldType dateFieldType=null) {

            // Add the writer config first because it rejects fields that were already added.
            exportConfigBuilder.fieldConfig(new BulkDataWriterFieldConfig(
                    fieldName: fieldName,
                    columnIndex: columnIndex,
                    headerMessageCode: headerMessageCode,
                    dateFieldType: dateFieldType,
            ))
            if (config.columnMapping.containsKey(columnIndex)) {
                throw new IllegalArgumentException("Column '${columnIndex}' was mapped to more than one field.")
            }
            config.columnMapping.put(columnIndex, fieldName)
            config.binderFieldConfigs.put(fieldName, binderConfig)

            return this
        }

        private static void rejectJavaUtilDateField(Field field, String builderMethodToUse) {
            if (field != null && Date.isAssignableFrom(field.type)) {
                throw new IllegalArgumentException("Field '${field.name}' is a java.util.Date, which is used for " +
                        "both date-only and datetime fields. Use ${builderMethodToUse} to specify which it is.")
            }
        }

        private void requireJavaUtilDateField(String fieldName, DateFieldType dateFieldType) {
            Field field = ReflectionUtils.findField(importableType, fieldName)
            if (!field) {
                throw new IllegalArgumentException(
                        "No field '${fieldName}' on ${importableType.name} or its superclasses")
            }
            if (!Date.isAssignableFrom(field.type)) {
                throw new IllegalArgumentException("Field '${fieldName}' is a ${field.type.simpleName}, not a " +
                        "java.util.Date. Date fields of other types (such as LocalDate or Instant) are unambiguous " +
                        "and so don't need a dateFieldType.")
            }
            if (dateFieldType == null) {
                throw new IllegalArgumentException("Date field '${fieldName}' must specify a dateFieldType.")
            }
        }

        BulkDataImportExportConfig build() {
            config.@exportConfig = exportConfigBuilder.build()
            return config
        }
    }
}
