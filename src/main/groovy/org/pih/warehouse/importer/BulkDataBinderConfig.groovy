package org.pih.warehouse.importer

/**
 * Configuration for binding data to some concrete object.
 */
class BulkDataBinderConfig {
    /**
     * The class type of the concrete object to bind the data to.
     */
    Class<Importable> bindTo

    /**
     * Configuration for binding each of the fields of the object.
     *
     * Keyed on the field name as it is in the object being bound to. For data imports, make sure that this
     * name matches the name in the BulkDataReaderConfig.columnMapping
     */
    Map<String, BulkDataBinderFieldConfig> fields
}
