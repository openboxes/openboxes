package org.pih.warehouse.importer

/**
 * Only needed for step 1 where we import the file and format it to a Map
 */
abstract class DataFileImporterConfig {

    /**
     * zero index based. Defaults to 1 because there is usually a header row.
     */
    int startRow = 1

    /**
     * Maps columns in the imported document to a field/property name.
     *
     * We rely on the column index instead of the name in the header row of the document so that we can localize
     * the header row in exports. The drawback of this approach is that if columns in the imported document change
     * order, it will break the importer. The typical import flow is to download a (localized) import template, so
     * the hope is that users will not need to modify the columns themselves.
     *
     * In this mapping, columns can be represented as either zero-indexed numerical keys, or as letters (which is how
     * they appear in Excel). The first column can be represented as "0" or "A", the second as "1" or "B", ...
     *
     * For example:
     * - Excel importers might have a mapping like: ["A": "field0", "B": "field1", ...]
     * - CSV importers might have a mapping like:   ["0": "field0", "1": "field1", ...]
     */
    Map<String, String> columnMapping = [:]
}
