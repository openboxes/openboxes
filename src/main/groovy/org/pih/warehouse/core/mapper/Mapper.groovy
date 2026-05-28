package org.pih.warehouse.core.mapper

/**
 * Converter from a source object into a target object.
 *
 * To support mapping in both directions, implement BidirectionalMapper instead.
 *
 * @param <Source> The object to be converted from
 * @param <Target> The object to be converted to
 */
interface Mapper<Source, Target> {

    /**
     * Converts an instance of the source object into a new instance of the target object.
     *
     * @param source The object to be converted from.
     * @return A new instance of the target object.
     */
    Target map(Source source)
}