package org.pih.warehouse.core.mapper

import org.springframework.stereotype.Component

/**
 * A wrapper on all {@link Mapper} components that allows converting any source object into any target object
 * as long as there is a Mapper defined between the two.
 */
@Component
class SmartMapper {

    private final MapperComponentResolver mapperComponentResolver

    SmartMapper(final MapperComponentResolver mapperComponentResolver) {
        this.mapperComponentResolver = mapperComponentResolver
    }

    /**
     * Converts an instance of the source object into a new instance of the target object.
     * Requires that a {@link Mapper} component is defined between the source and target.
     *
     * @param source The object to be converted from.
     * @param target The target class type to convert the source to.
     * @param config The configuration to use when performing the mapping.
     * @return A new instance of the target object.
     */
    def <Source, Target> Target map(Source source, Class<Target> targetClass, MapperConfig config=null) {
        if (source == null) {
            return null
        }

        Mapper mapper = mapperComponentResolver.getMapper(source.class, targetClass)
        if (mapper == null) {
            throw new RuntimeException("No mapper was found between source ${source.class} and target ${targetClass}.")
        }
        return mapper.map(source, config)
    }

    /**
     * Converts a collection of source objects into a new list of target objects.
     * Requires that a {@link Mapper} component is defined between the source and target.
     *
     * @param source The collection of object to be converted from.
     * @param target The target class type to convert each of the sources to.
     * @param config The configuration to use when performing the mapping.
     * @return A new list of instances of the target object.
     */
    def <Source, Target> List<Target> mapCollection(
            Collection<Source> sourceCollection, Class<Target> targetClass, MapperConfig config=null) {
        if (sourceCollection == null) {
            return null
        }

        List<Target> mappedList = []
        for (source in sourceCollection) {
            mappedList.add(map(source, targetClass, config))
        }
        return mappedList
    }
}
