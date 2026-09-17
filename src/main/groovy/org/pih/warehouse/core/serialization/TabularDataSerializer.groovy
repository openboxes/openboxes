package org.pih.warehouse.core.serialization

/**
 * Converter between an object and a flat/tabular representation of that object's data that is ready to be serialized.
 *
 * @param <S> The class of the object being serialized.
 */
interface TabularDataSerializer<S extends TabularSerializable> {

    /**
     * Converts an object to a Map of flat/tabular data that is ready to be serialized.
     *
     * Used for: CSV, XLS, XLSX
     *
     * Tabular data formats differ from their hierarchal counterparts in that they must be flat.
     * This is because tabular data formats cannot easily represent nested data structures.
     *
     * As such, if you want an object to be able to be serialized as tabular data (such as for bulk data exporting) we
     * require that you explicitly define the mapping behaviour.
     *
     * @param serializable The object to serialize
     * @return a Map of values keyed on field name
     */
    Map<String, Object> serializeTabular(S serializable)
}
