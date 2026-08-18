package org.pih.warehouse.importer

import org.springframework.core.GenericTypeResolver

import org.pih.warehouse.core.http.ContentType

/**
 * A convenience base class for configuring the full import flow for a feature.
 *
 * Primarily exists to remove boilerplate code and standardize the import approach. If you need different
 * behaviour, either override the methods defined here or implement the Configures* traits directly.
 */
abstract class BulkDataImportConfigurer<T extends Importable> implements
        ConfiguresCsvReader,
        ConfiguresExcelReader,
        ConfiguresBulkDataBinder<T>,
        ConfiguresBulkDataValidator<T>{

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
    abstract Map<String, String> getColumnMapping()

    /**
     * Configuration for binding each of the fields of the object.
     * Keyed on field name. Make sure that the keys in this map match the values in getColumnMapping().
     */
    abstract Map<String, BulkDataBinderFieldConfig> getDataBinderFieldConfig()

    @Override
    List<ContentType> getSupportedContentTypes() {
        // We're configuring both the CSV and Excel readers so we support the content types of both.
        return ConfiguresExcelReader.super.getSupportedContentTypes() +
                ConfiguresCsvReader.super.getSupportedContentTypes()
    }

    @Override
    CsvReaderConfig getCsvReaderConfig() {
        return new CsvReaderConfig(
                columnMapping: columnMapping,
        )
    }

    @Override
    ExcelReaderConfig getExcelReaderConfig() {
        return new ExcelReaderConfig(
                columnMapping: columnMapping,
        )
    }

    @Override
    BulkDataReaderConfig getBulkDataReaderConfig(ContentType contentType) {
        if (ConfiguresExcelReader.super.getSupportedContentTypes().contains(contentType)) {
            return excelReaderConfig
        }
        if (ConfiguresCsvReader.super.getSupportedContentTypes().contains(contentType)) {
            return csvReaderConfig
        }
        throw new RuntimeException("Content type ${contentType} is not supported.")
    }

    @Override
    BulkDataBinderConfig getBulkDataBinderConfig() {
        return new BulkDataBinderConfig(
                bindTo: getSourceType(),
                bulkDataType: bulkDataType,
                fields: dataBinderFieldConfig,
                columnByFieldName: getColumnByFieldName(),
        )
    }

    @Override
    BulkDataValidatorConfig getBulkDataValidatorConfig() {
        return new BulkDataValidatorConfig(
                columnByFieldName: columnByFieldName,
        )
    }

    private Map<String, String> getColumnByFieldName() {
        // Column index keyed on field name. The inverse of the column mapping that the readers use.
        return columnMapping.collectEntries { k, v -> [v, k] } as Map<String, String>
    }

    private Class<T> getSourceType() {
        return (Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(), BulkDataImportConfigurer.class)
    }
}
