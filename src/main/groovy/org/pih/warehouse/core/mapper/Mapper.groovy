package org.pih.warehouse.core.mapper

import org.springframework.core.GenericTypeResolver

/**
 * Converter from a source object into a target object.
 *
 * To support mapping in both directions, implement BidirectionalMapper instead.
 *
 * @param <Source> The object to be converted from
 * @param <Target> The object to be converted to
 */
trait Mapper<Source, Target> {

    Class<Source> getSourceType() {
        return (Class<Source>) GenericTypeResolver.resolveTypeArguments(getClass(), Mapper.class)[0]
    }

    Class<Target> getTargetType() {
        return (Class<Target>) GenericTypeResolver.resolveTypeArguments(getClass(), Mapper.class)[1]
    }

    /**
     * Converts an instance of the source object into a new instance of the target object.
     *
     * @param source The object to be converted from.
     * @return A new instance of the target object.
     */
    abstract Target map(Source source)
}
