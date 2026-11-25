package org.pih.warehouse.core.mapper

import grails.util.Holders
import org.hibernate.proxy.HibernateProxy
import org.springframework.stereotype.Component

import org.pih.warehouse.core.Identifiable

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

        Class<Source> sourceClass = determineSourceClass(source)

        Mapper mapper = mapperComponentResolver.getMapper(sourceClass, targetClass)
        if (mapper == null) {
            throw new RuntimeException("No org.pih.warehouse.core.mapper.Mapper implementation was found between " +
                    "source ${sourceClass} and target ${targetClass}.")
        }
        return mapper.map(source, config)
    }

    /**
     * A convenience method for mapping {@link Identifiable} objects when we only want to map the id field.
     * This allows us to map between objects without actually hydrating them.
     *
     * @param source The collection of object to be converted from.
     * @param targetClass The target class type to convert each of the sources to.
     * @return A new list of instances of the target object with only the id field set.
     */
    static <Source extends Identifiable, Target extends Identifiable> Target mapIdOnly(Source source,
                                                                                       Class<Target> targetClass) {
        // If the source object is a Hibernate proxy (ie a non-hydrated domain entity object),
        // accessing its "id" field will not cause the entity to be fetched/hydrated from the database.
        return targetClass.newInstance(id: source.id)
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

        if (sourceCollection.empty) {
            return []
        }

        Class<Source> sourceClass = determineSourceClass(sourceCollection.first())

        Mapper mapper = mapperComponentResolver.getMapper(sourceClass, targetClass)
        if (mapper == null) {
            throw new RuntimeException("No org.pih.warehouse.core.mapper.Mapper implementation was found between " +
                    "source ${sourceClass} and target ${targetClass}.")
        }
        return mapper.mapCollection(sourceCollection, config)
    }

    /**
     * Determine the class of the source object.
     *
     * If the source object is a Hibernate proxy, meaning it has not yet been fetched from the database,
     * we determine the class without triggering a database query.
     */
    private <Source> Class<Source> determineSourceClass(Source source) {
        if (source instanceof HibernateProxy) {
            return source.hibernateLazyInitializer.persistentClass
        }
        return source.class as Class<Source>
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
