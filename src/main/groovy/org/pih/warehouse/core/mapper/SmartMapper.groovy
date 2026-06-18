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
     * @return A new instance of the target object.
     */
    def <Source, Target> Target map(Source source, Class<Target> targetClass) {
        if (source == null) {
            return null
        }

        Mapper mapper = mapperComponentResolver.getMapper(source.class, targetClass)
        if (mapper == null) {
            throw new RuntimeException("No mapper was found between source ${source.class} and target ${targetClass}.")
        }
        return mapper.map(source)
    }

    /**
     * Converts a collection of source objects into a new list of target objects.
     * Requires that a {@link Mapper} component is defined between the source and target.
     *
     * @param source The collection of object to be converted from.
     * @param target The target class type to convert each of the sources to.
     * @return A new list of instances of the target object.
     */
    def <Source, Target> List<Target> mapCollection(Collection<Source> sourceCollection, Class<Target> targetClass) {
        if (sourceCollection == null) {
            return null
        }
        if (sourceCollection.isEmpty()) {
            return []
        }

        Class sourceClass = sourceCollection[0].class
        Mapper mapper = mapperComponentResolver.getMapper(sourceClass, targetClass)
        if (mapper == null) {
            throw new RuntimeException("No mapper was found between source ${sourceClass} and target ${targetClass}.")
        }

        List<Target> mappedList = []
        for (source in sourceCollection) {
            mappedList.add(mapper.map(source))
        }
        return mappedList
    }
}
