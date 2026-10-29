package org.pih.warehouse.importer

/**
 * Configuration for validating bulk data.
 */
class BulkDataValidatorConfig {
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
