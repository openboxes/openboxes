package org.pih.warehouse.importer

import java.lang.reflect.Field
import org.springframework.util.ReflectionUtils

import org.pih.warehouse.core.date.DateFieldType
import org.pih.warehouse.core.parser.Parser
import org.pih.warehouse.core.parser.ParserContext
import org.pih.warehouse.exporter.BulkDataWriterFieldConfig

/**
 * Wraps all import and export configuration options, unifying them into a single config for the sake of simplicity.
 *
 * Designed to be used by implementations of {@link BulkDataImportConfigurer}.
 */
class BulkDataImportExportConfig {

    private Map<String, String> columnMapping = [:]
    private Map<String, BulkDataBinderFieldConfig> binderFieldConfigs = [:]
    private List<BulkDataWriterFieldConfig> writerFieldConfigs = []

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
     * Configuration for writing each field of the object. Keyed on field name.
     */
    List<BulkDataWriterFieldConfig> getWriterFieldConfigs() {
        return writerFieldConfigs
    }

    static class BulkDataImportExportConfigBuilder {

        Class importableType
        BulkDataImportExportConfig config

        BulkDataImportExportConfigBuilder(final Class importableType) {
            this.importableType = importableType
            this.config = new BulkDataImportExportConfig()
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

            config.columnMapping.put(columnIndex, fieldName)
            config.binderFieldConfigs.put(fieldName, binderConfig)
            config.writerFieldConfigs.add(new BulkDataWriterFieldConfig(
                    fieldName: fieldName,
                    columnIndex: columnIndex,
                    headerMessageCode: headerMessageCode,
                    dateFieldType: dateFieldType,
            ))

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
            return config
        }
    }
}
