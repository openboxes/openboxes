package org.pih.warehouse.common.domains.builders.location.base

import grails.buildtestdata.TestDataBuilder
import org.grails.datastore.gorm.GormEntity

/**
 * Classes for building instances of Domain objects and inserting them to the database for use by tests.
 *
 * Adds an extra layer of convenience on top of the defaults provided by TestDataConfig. These builder classes are
 * useful when we have multiple standard ways of constructing a domain that we want to provide default behaviour for.
 * For example, creating a default for a warehouse/facility Location vs a default for a bin location Location.
 *
 * @param <T> The domain object that this builder constructs.
 */
abstract class TestBuilder<T extends GormEntity> implements TestDataBuilder {

    protected Class<T> domainClazz
    protected Map<String, Object> args

    protected TestBuilder(Class<T> domainClazz, Map<String, Object> args) {
        this.domainClazz = domainClazz
        this.args = args
    }

    /**
     * Returns an instance of the domain with the given args if it exists, otherwise creates it and inserts it to
     * the database. Fields that are not provided to the builder will default to the values defined in TestDataConfig.
     */
    T findOrBuild() {
        return findOrBuild(domainClazz, args)
    }

    /**
     * Creates and inserts an instance of the domain to the database with the given args. Fields that are not
     * provided to the builder will default to the values defined in TestDataConfig.
     */
    T build() {
        return build(domainClazz, args)
    }
}
