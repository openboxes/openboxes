package org.pih.warehouse.core.mapper

import grails.util.Holders
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
     * @param targetClass The target class type to convert the source to.
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
     * @param targetClass The target class type to convert each of the sources to.
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

    private static SmartMapper getSmartMapperStatic() {
        return Holders.applicationContext.getBean("smartMapper") as SmartMapper
    }

    /**
     * Prefer using the non-static {@link #map} method when possible. Statically accessing a component like this is an
     * anti-pattern and makes unit testing harder. This method only exists to simplify refactorings and is not meant
     * as a long term solution.
     *
     * Converts an instance of the source object into a new instance of the target object.
     * Requires that a {@link Mapper} component is defined between the source and target.
     *
     * @param source The object to be converted from.
     * @param targetClass The target class type to convert the source to.
     * @param config The configuration to use when performing the mapping.
     * @return A new instance of the target object.
     */
    static <Source, Target> Target mapStatic(Source source, Class<Target> targetClass, MapperConfig config=null) {
        return getSmartMapperStatic().map(source, targetClass, config)
    }

    /**
     * Prefer using the non-static {@link #mapCollection} method when possible. Statically accessing a component like
     * this is an anti-pattern and makes unit testing harder. This method only exists to simplify refactorings and
     * is not meant as a long term solution.
     *
     * Converts a collection of source objects into a new list of target objects.
     * Requires that a {@link Mapper} component is defined between the source and target.
     *
     * @param source The collection of object to be converted from.
     * @param targetClass The target class type to convert each of the sources to.
     * @param config The configuration to use when performing the mapping.
     * @return A new list of instances of the target object.
     */
    static <Source, Target> List<Target> mapCollectionStatic(
            Collection<Source> sourceCollection, Class<Target> targetClass, MapperConfig config=null) {
        return getSmartMapperStatic().mapCollection(sourceCollection, targetClass, config)
    }
}
