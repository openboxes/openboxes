package org.pih.warehouse.core.serialization

/**
 * Converter between object and flat, tabular data format that is ready to be serialized.
 * @param <S>
 */
interface TabularDataSerializer<S extends TabularSerializable> {

    /**
     * Converts an object to a Map for use bulk data export APIs, such as CSV or XLS/XLSX.
     *
     * A "bulk data" row differs from a response body representation of the data in that it must be flat.
     * This is because tabular data files (such as .csv) cannot easily represent nested data structures.
     *
     * As such, exporting will fail (with a loud, helpful message) if any values in the returned map are not directly
     * serializable.
     *
     * @param serializable The object to convert
     * @return a Map of values keyed on field name
     */
    Map<String, Object> serializeTabular(S serializable)
}