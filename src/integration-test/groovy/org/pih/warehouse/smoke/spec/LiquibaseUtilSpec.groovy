package org.pih.warehouse.smoke.spec

import util.LiquibaseUtil

import org.pih.warehouse.smoke.spec.base.SmokeSpec

/**
 * Tests liquibase schema changes.
 */
class LiquibaseUtilSpec extends SmokeSpec {

    void "should return valid upgrade changelog versions"() {
        when:
        Set<String> changeLogVersions = LiquibaseUtil.upgradeChangeLogVersions

        then:
        changeLogVersions.containsAll(["0.5.x", "0.6.x", "0.7.x", "0.8.x"])
    }
}
