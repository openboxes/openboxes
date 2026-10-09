package org.pih.warehouse.performance.spec.base

import grails.gorm.transactions.Transactional

import org.pih.warehouse.common.base.IntegrationSpec

/**
 * Base class for performance/benchmark integration specs. Mirrors ApiSpec's setupData()/cleanupData()
 * pattern: setup() and cleanup() are wrapped in their own @Transactional methods (so GORM calls in
 * subclasses have a bound session, and setup()'s transaction commits before the feature method runs -
 * no need for individual specs to use withNewTransaction{}). Subclasses override setupData()/cleanupData()
 * to create and tear down whatever fixtures they need.
 *
 * Grouping performance specs under this base and under org.pih.warehouse.performance.spec also gives us a
 * single place to later add conditional execution (e.g. skipping these in normal CI runs) without having
 * to touch every individual spec - deliberately not implemented yet.
 */
abstract class PerformanceSpec extends IntegrationSpec {

    @Transactional
    void setup() {
        setupData()
    }

    @Transactional
    void cleanup() {
        cleanupData()
    }

    void setupData() {
        // Blank default implementation; subclasses override to set up the data they need.
    }

    void cleanupData() {
        // Blank default implementation; subclasses override to tear down what they created.
    }
}
