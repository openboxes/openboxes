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
     * The feature/data structure associated with this config.
     * Can be null if the binding is not directly tied to a configurer. In this case, no custom binding will occur.
     */
    BulkDataType bulkDataType

    /**
     * Configuration for binding each of the fields of the object.
     *
     * Keyed on the field name as it is in the object being bound to. For data imports, make sure that this
     * name matches the name in the BulkDataReaderConfig.columnMapping
     */
    Map<String, BulkDataBinderFieldConfig> fields
}
