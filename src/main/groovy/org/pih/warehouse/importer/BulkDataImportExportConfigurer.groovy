package org.pih.warehouse.importer

import org.springframework.core.GenericTypeResolver

import org.pih.warehouse.core.http.ContentType
import org.pih.warehouse.exporter.BulkDataExportConfig
import org.pih.warehouse.exporter.ConfiguresAllBulkDataWriters

/**
 * A convenience base class for configuring the full import and export flows for a feature.
 *
 * Primarily exists to remove boilerplate code and standardize the import/export approach. If you need different
 * behaviour, either override the methods defined here or implement the Configures* traits directly.
 *
 * If you need to perform custom import/export behaviour, the following hook-in methods can be overwritten:
 * - customBindDataAcrossRows: custom data binding that spans across multiple rows
 * - customBindDataRow: custom data binding on each row individually
 * - customValidateAcrossRows: custom validation that spans across multiple rows
 * - customValidateRow: custom validation on each row individually
 *
 * @param <I> The DTO being imported
 */
abstract class BulkDataImportExportConfigurer<I extends Importable> implements
        // import configurers
        ConfiguresCsvReader,
        ConfiguresExcelReader,
        ConfiguresBulkDataBinder<I>,
        ConfiguresBulkDataValidator<I>,
        // export configurers
        ConfiguresAllBulkDataWriters {

    /**
     * A unified configuration for both import and export.
     *
     * We wrap getImportExportConfig with a lazy initialized field to avoid recomputing the config each time, which
     * is safe to do because the config itself is functionally static.
     *
     * NOTE: The field must not be named "importExportConfig". @Lazy generates a getter for the field, which would
     *       collide with getImportExportConfig() and so the config would be rebuilt on every access.
     */
    @Lazy
    private BulkDataImportExportConfig lazyImportExportConfig = getImportExportConfig()

    /**
     * A unified configuration for both import and export.
     * It is likely simplest to construct the config via the builder, which is accessible via {@link #configBuilder()}
     */
    abstract protected BulkDataImportExportConfig getImportExportConfig()

    protected BulkDataImportExportConfig.BulkDataImportExportConfigBuilder configBuilder() {
        return BulkDataImportExportConfig.builder(getImportableType())
    }

    @Override
    BulkDataExportConfig getCachedExportConfig() {
        return lazyImportExportConfig.exportConfig
    }

    /**
     * We're configuring both the CSV and Excel readers/writers so we support the content types of both.
     *
     * NOTE: This override is required. ConfiguresAllBulkDataWriters is the last trait implemented, so without it,
     *       its writer-only implementation would be used and the readers would be ignored.
     */
    @Override
    Set<ContentType> getSupportedContentTypes() {
        // Error if the readers support different content types from the writers because that is a misconfiguration.
        Set<ContentType> readerContentTypes = readerContentTypes
        if (readerContentTypes != writerContentTypes) {
            throw new RuntimeException("Readers and writers must support the same content types. The readers " +
                    "support ${readerContentTypes} but the writers support ${writerContentTypes}")
        }
        return readerContentTypes
    }

    /**
     * @return The content types supported by all of the readers.
     */
    Set<ContentType> getReaderContentTypes() {
        return ConfiguresCsvReader.super.getSupportedContentTypes() +
                ConfiguresExcelReader.super.getSupportedContentTypes()
    }

    @Override
    CsvReaderConfig getCsvReaderConfig() {
        return new CsvReaderConfig(
                columnMapping: lazyImportExportConfig.columnMapping,
        )
    }

    @Override
    ExcelReaderConfig getExcelReaderConfig() {
        return new ExcelReaderConfig(
                columnMapping: lazyImportExportConfig.columnMapping,
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
                bindTo: getImportableType(),
                bulkDataType: bulkDataType,
                fields: lazyImportExportConfig.binderFieldConfigs,
                columnByFieldName: getColumnByFieldName(),
        )
    }

    @Override
    BulkDataValidatorConfig getBulkDataValidatorConfig() {
        return new BulkDataValidatorConfig(
                columnByFieldName: getColumnByFieldName(),
        )
    }

    private Map<String, String> getColumnByFieldName() {
        // Column index keyed on field name. The inverse of the column mapping that the readers use.
        return lazyImportExportConfig.columnMapping.collectEntries { k, v -> [v, k] } as Map<String, String>
    }

    private Class<I> getImportableType() {
        return (Class<I>) GenericTypeResolver.resolveTypeArguments(getClass(), BulkDataImportExportConfigurer)[0]
    }
}
