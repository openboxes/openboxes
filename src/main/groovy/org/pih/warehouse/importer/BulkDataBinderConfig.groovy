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

    /**
     * Maps field/property name to the column in the source object.
     *
     * In this mapping, columns can be represented as either zero-indexed numerical keys, or as letters (which is how
     * they appear in Excel). The first column can be represented as "0" or "A", the second as "1" or "B", ...
     *
     * For example:
     * - Excel importers might have a mapping like: ["field0": "A", "field1": "B", ...]
     * - CSV importers might have a mapping like:   ["field0": "0", "field1": "1", ...]
     */
    Map<String, String> columnByFieldName = [:]
}
