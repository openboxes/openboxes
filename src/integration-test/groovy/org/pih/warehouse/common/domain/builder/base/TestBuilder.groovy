package org.pih.warehouse.common.domain.builder.base

import grails.buildtestdata.TestDataBuilder
import org.grails.datastore.gorm.GormEntity
import org.springframework.core.GenericTypeResolver

import org.pih.warehouse.common.util.RandomUtil

/**
 * Classes for building instances of Domain objects and (optionally) inserting them to the database for use by tests.
 * Adds an extra layer of convenience on top of the defaults provided by TestDataConfig.
 *
 * @param <T> The domain object that this builder constructs.
 */
abstract class TestBuilder<T extends GormEntity> implements TestDataBuilder {

    protected RandomUtil randomUtil = new RandomUtil()

    private Class<T> domainClazz

    /**
     * Holds the key value pairs that will be used to construct the domain instance. Any key that is not in the domain
     * will be ignored.
     *
     * You could expose methods that directly add or set this map, but a cleaner solution would be to expose builder
     * methods for the individual fields that tests to use so that we can control the behaviour of setting each field
     * individually and unify it in a single place.
     */
    protected Map<String, Object> args = [:]

    /**
     * If true, will fall back to the default values defined in getDefaults when a field is not specified, otherwise
     * will leave the field as null.
     */
    protected boolean useDefaults

    TestBuilder(useDefaults=true) {
        this.randomUtil = new RandomUtil()

        // The Spring *magic* way of extracting the type from a class.
        this.domainClazz = (Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(), TestBuilder.class)

        this.useDefaults = useDefaults
    }

    /**
     * Overridable method that returns a map of default values for the fields of the domain to use in case
     * they aren't specified in the "args" map. Will only be used if "useDefaults" is true.
     */
    protected Map<String, Object> getDefaults() {
        return Collections.emptyMap()
    }

    /**
     * Returns an instance of the domain with the given args if it exists, otherwise creates it and inserts it to
     * the database. Fields that are not provided to the builder will optionally default to the values defined in
     * the "defaults" map. If a field is still missing, the value defined in TestDataConfig will be used.
     */
    T findOrBuild() {
        Map<String, Object> argsCombined = useDefaults ? defaults + args : args
        return findOrBuild(domainClazz, argsCombined)
    }

    /**
     * Creates and optionally inserts an instance of the domain to the database with the given args. Fields that are not
     * provided to the builder will optionally default to the values defined in the "defaults" map. If a field is still
     * missing, the value defined in TestDataConfig will be used.
     *
     * @param save if true, will persist the entity to the database, otherwise will just initialize the class.
     */
    T build(boolean save=false) {
        Map<String, Object> argsCombined = useDefaults ? defaults + args : args
        return build([save: save], domainClazz, argsCombined)
    }
}
