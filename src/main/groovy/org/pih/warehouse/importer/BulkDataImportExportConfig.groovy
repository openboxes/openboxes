package org.pih.warehouse.importer

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

    static BulkDataImportExportConfigBuilder builder(final Class exportableType) {
        return new BulkDataImportExportConfigBuilder(exportableType)
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

        Class exportableType
        BulkDataImportExportConfig config

        BulkDataImportExportConfigBuilder(final Class exportableType) {
            this.exportableType = exportableType
            this.config = new BulkDataImportExportConfig()
        }

        /**
         * Adds configuration for a field that will be automatically bound via a {@link Parser} that is either
         * explicitly given or determined based on the field type.
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

            BulkDataBinderFieldConfig binderConfig
            if (parser) {
                binderConfig = new BulkDataBinderFieldConfig(parser: parser, parserContext: parserContext)
            }
            else {
                // We have custom data-binding handling for when the field is an enum.
                Class fieldType = exportableType.getDeclaredField(fieldName).type
                binderConfig = fieldType.isEnum()
                        ? BulkDataBinderFieldConfigDefaults.buildDefaultEnumFieldConfig(fieldType)
                        : BulkDataBinderFieldConfigDefaults.DEFAULT_CONFIG
            }

            return addField(binderConfig, columnIndex, fieldName, headerMessageCode)
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
                                                           String headerMessageCode) {

            config.columnMapping.put(columnIndex, fieldName)
            config.binderFieldConfigs.put(fieldName, binderConfig)
            config.writerFieldConfigs.add(new BulkDataWriterFieldConfig(
                    fieldName: fieldName,
                    columnIndex: columnIndex,
                    headerMessageCode: headerMessageCode,
            ))

            return this
        }

        BulkDataImportExportConfig build() {
            return config
        }
    }
}
