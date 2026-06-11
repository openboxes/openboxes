package org.pih.warehouse.core.mapper

/**
 * Converter between two different source objects that supports converting in both directions (A -> B and B -> A).
 */
abstract class BidirectionalMapper<Source, OtherSource> implements Mapper<Source, OtherSource> {

    /**
     * Converts an instance of the "other" source object into a new instance of the target object.
     *
     * @param source The object to be converted from.
     * @return A new instance of the target object.
     */
    abstract Source map(OtherSource source)
}
