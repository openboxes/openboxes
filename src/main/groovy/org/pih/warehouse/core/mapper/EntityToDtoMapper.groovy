package org.pih.warehouse.core.mapper

import org.grails.datastore.gorm.GormEntity
import org.hibernate.Hibernate

import org.pih.warehouse.core.dtos.DomainDto

/**
 * Defines how to convert a domain entity into a DTO.
 *
 * @param <E> The domain entity to be converted from
 * @param <D> The DTO to be converted into
 */
trait EntityToDtoMapper<E extends GormEntity, D extends DomainDto> implements Mapper<E, D> {

    /**
     * Defines the actual logic of converting a domain entity into a DTO.
     *
     * At this point, we've determined that we want to fully "hydrate" the DTO, so this method should populate all
     * fields of the DTO with the relevant values from the source entity.
     *
     * @param source The domain entity to be converted from.
     * @param config The configuration to use when performing the mapping.
     * @return A new instance of the DTO, full hydrated by the entity.
     */
    abstract D doMap(E source, MapperConfig config)

    @Override
    D map(E source, MapperConfig config=null) {
        if (!source) {
            return null
        }

        /*
         * Determine if we should fully hydrate the DTO or only set its id field.
         *
         * Fully hydrating the object typically requires an additional database call, whereas only mapping the id field
         * does not.
         *
         * Even though we pass around the actual entity object as the source, it isn't actually queried for from the
         * database until we access its fields. This is because Hibernate lazy fetches relationship fields by default.
         *
         * Say we have an entity "Product" that has a reference to another entity "Category". After fetching the
         * Product "p" from the database, "p.category" will be a Hibernate proxy instance. Calling "p.category.id"
         * won't cause the Category to be fetched from the database, but calling "p.category.name" will.
         *
         * If we have a ProductDtoMapper and a CategoryDtoMapper that both implement EntityToDtoMapper, and the
         * productMapper internally invokes the categoryMapper (because product has a category field):
         *
         * If we call: categoryMapper.map(p.category, new MapperConfig(hydrationLevel: ID_ONLY)
         * then the category won't actually be fetched from the database because we only ever access its "id" field.
         *
         * If we call: categoryMapper.map(p.category, new MapperConfig(hydrationLevel: FULL)
         * then we'd fully populate the category dto, requiring an additional database call to fetch the category.
         */
        switch (config?.hydrationLevel) {
            case HydrationLevel.ID_ONLY:
                return mapIdOnly(source)
            default:
                return doMap(source, config)
        }
    }

    @Override
    List<D> mapCollection(Collection<E> sources, MapperConfig config=null) {
        if (sources == null) {
            return null
        }

        /*
         * If we don't rely on the entity fields (because we're only including the id), or if the entities have already
         * been fetched from the database, we have no database queries to optimize so simply proceed as normal.
         *
         * Note that the HibernateProxy will never be initialized for domain entity "hasMany" relationships. Accessing
         * any element of the hasMany collection at any point in any way will fully load the collection. Fortunately,
         * this is done via a single query, so there's no N+1 risk.
         */
        if (!sources || config?.hydrationLevel == HydrationLevel.ID_ONLY || Hibernate.isInitialized(sources.first())) {
            return super.mapCollection(sources, config)
        }

        // The sources are not initialized (they are still Hibernate proxies). To avoid N+1 queries when looping
        // the collection to map the individual sources, we pre-fetch them all now in a single query.
        Collection<E> sourcesFetched = sourceType.findAllByIdInList(sources.id)
        return super.mapCollection(sourcesFetched, config)
    }

    private D mapIdOnly(E source) {
        // Accessing the "id" field of a Hibernate proxy entity does not cause it to be fetched from the database.
        return targetType.newInstance(id: source.id)
    }
}
