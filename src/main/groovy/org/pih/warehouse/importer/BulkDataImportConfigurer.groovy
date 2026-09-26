package org.pih.warehouse.importer

import org.springframework.core.GenericTypeResolver

import org.pih.warehouse.core.http.ContentType
import org.pih.warehouse.exporter.BulkDataWriterConfig
import org.pih.warehouse.exporter.ConfiguresCsvWriter
import org.pih.warehouse.exporter.ConfiguresExcelWriter
import org.pih.warehouse.exporter.CsvWriterConfig
import org.pih.warehouse.exporter.ExcelStyle
import org.pih.warehouse.exporter.ExcelWriterConfig

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
 * @param <E> The DTO being exported
 */
abstract class BulkDataImportConfigurer<I extends Importable, E> implements
        // import configurers
        ConfiguresCsvReader,
        ConfiguresExcelReader,
        ConfiguresBulkDataBinder<I>,
        ConfiguresBulkDataValidator<I>,
        // export configurers
        ConfiguresCsvWriter,
        ConfiguresExcelWriter {

    /**
     * A unified configuration for both import and export.
     * It is likely simplest to construct the config via the builder, which is accessible via {@link #configBuilder()}
     */
    abstract protected BulkDataImportExportConfig getImportExportConfig()

    protected BulkDataImportExportConfig.BulkDataImportExportConfigBuilder configBuilder() {
        return BulkDataImportExportConfig.builder(getExportableType())
    }

    @Override
    Set<ContentType> getSupportedContentTypes() {
        // We're configuring both the CSV and Excel readers/writers so we support the content types of both.
        // Error if the readers support different content types from the writers because that is a misconfiguration.
        Set<ContentType> excelReaderContentTypes = ConfiguresExcelReader.super.getSupportedContentTypes()
        Set<ContentType> excelWriterContentTypes = ConfiguresExcelWriter.super.getSupportedContentTypes()
        if (excelReaderContentTypes != excelWriterContentTypes) {
            throw new RuntimeException("Excel reader and writer configurers must have the same supported content " +
                    "types. The reader has ${excelReaderContentTypes} but the writer has ${excelWriterContentTypes}")
        }
        Set<ContentType> csvReaderContentTypes = ConfiguresCsvReader.super.getSupportedContentTypes()
        Set<ContentType> csvWriterContentTypes = ConfiguresCsvWriter.super.getSupportedContentTypes()
        if (csvReaderContentTypes != csvWriterContentTypes) {
            throw new RuntimeException("CSV reader and writer configurers must have the same supported content " +
                    "types. The reader has ${csvReaderContentTypes} but the writer has ${csvWriterContentTypes}")
        }

        return excelReaderContentTypes + csvReaderContentTypes
    }

    @Override
    CsvReaderConfig getCsvReaderConfig() {
        return new CsvReaderConfig(
                columnMapping: importExportConfig.columnMapping,
        )
    }

    @Override
    ExcelReaderConfig getExcelReaderConfig() {
        return new ExcelReaderConfig(
                columnMapping: importExportConfig.columnMapping,
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
                fields: importExportConfig.binderFieldConfigs,
                columnByFieldName: getColumnByFieldName(),
        )
    }

    @Override
    BulkDataValidatorConfig getBulkDataValidatorConfig() {
        return new BulkDataValidatorConfig(
                columnByFieldName: columnByFieldName,
        )
    }

    @Override
    CsvWriterConfig getCsvWriterConfig() {
        return new CsvWriterConfig(
                fields: importExportConfig.writerFieldConfigs,
        )
    }

    @Override
    ExcelWriterConfig getExcelWriterConfig() {
        return new ExcelWriterConfig(
                fields: importExportConfig.writerFieldConfigs,
                // We know that our bulk data exports are always going to be structured like a table
                // (header row + data) so we opt to add some quality of life styling to the Excel file.
                excelStyle: ExcelStyle.TABLE,
        )
    }

    @Override
    BulkDataWriterConfig getBulkDataWriterConfig(ContentType contentType) {
        if (ConfiguresExcelWriter.super.getSupportedContentTypes().contains(contentType)) {
            return excelWriterConfig
        }
        if (ConfiguresCsvWriter.super.getSupportedContentTypes().contains(contentType)) {
            return csvWriterConfig
        }
        throw new RuntimeException("Content type ${contentType} is not supported.")
    }

    private Map<String, String> getColumnByFieldName() {
        // Column index keyed on field name. The inverse of the column mapping that the readers use.
        return importExportConfig.columnMapping.collectEntries { k, v -> [v, k] } as Map<String, String>
    }

    private Class<I> getImportableType() {
        return (Class<I>) GenericTypeResolver.resolveTypeArguments(getClass(), BulkDataImportConfigurer)[0]
    }

    private Class<E> getExportableType() {
        return (Class<E>) GenericTypeResolver.resolveTypeArguments(getClass(), BulkDataImportConfigurer)[1]
    }
}
