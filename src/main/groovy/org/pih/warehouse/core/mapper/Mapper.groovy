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
     * @param config The configuration to use when performing the mapping.
     * @return A new instance of the target object.
     */
    abstract Target map(Source source, MapperConfig config=null)

    /**
     * Converts a collection of instances of the source object into a List of instances of the target object.
     *
     * @param sources The objects to be converted from.
     * @param config The configuration to use when performing the mapping.
     * @return A new List of instances of the target object.
     */
    List<Target> mapCollection(Collection<Source> sources, MapperConfig config=null) {
        if (sources == null) {
            return null
        }

        List<Target> mappedList = []
        for (source in sources) {
            mappedList.add(map(source, config))
        }
        return mappedList
    }
}
