package org.pih.warehouse.exporter

/**
 * A convenience base class for configuring the export flow for a feature that does not also support import.
 *
 * For features that support both importing and exporting, use
 * {@link org.pih.warehouse.importer.BulkDataImportExportConfigurer} instead.
 *
 * Primarily exists to remove boilerplate code and standardize the export approach. If you need different
 * behaviour, either override the methods defined here or implement the Configures*Writer traits directly.
 */
abstract class BulkDataExportConfigurer implements ConfiguresAllBulkDataWriters {

    /**
     * We cache the result of getExportConfig with a lazy initialized field to avoid recomputing the config each
     * time, which is safe to do because the config itself is functionally static.
     *
     * NOTE: The field must not be named "exportConfig". @Lazy generates a getter for the field, which would
     *       collide with getExportConfig() and so the config would be rebuilt on every access.
     */
    @Lazy
    private BulkDataExportConfig lazyExportConfig = getExportConfig()

    /**
     * Returns the configuration to use when exporting.
     * It is likely simplest to construct the config via the builder, which is accessible via {@link #configBuilder()}
     */
    abstract protected BulkDataExportConfig getExportConfig()

    protected BulkDataExportConfig.BulkDataExportConfigBuilder configBuilder() {
        return BulkDataExportConfig.builder()
    }

    @Override
    BulkDataExportConfig getCachedExportConfig() {
        return lazyExportConfig
    }
}
