package org.pih.warehouse.api.regression

import groovy.sql.Sql
import io.restassured.builder.ResponseSpecBuilder
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired

import javax.sql.DataSource

import org.pih.warehouse.api.client.core.LocationGroupApiWrapper
import org.pih.warehouse.api.spec.base.ApiSpec

/**
 * Smoke check for the regression integration suite: living under
 * org.pih.warehouse.api.* (the package IntegrationSpecConfig scans), it boots the whole app on a Testcontainers
 * database (TEST_DATABASE), lets Liquibase build the schema, logs in through
 * the real API (ApiSpec.setup) and makes one API call.
 */
class RegressionIntegrationSmokeSpec extends ApiSpec {

    @Autowired
    LocationGroupApiWrapper locationGroupApiWrapper

    @Autowired
    DataSource dataSource

    void 'Liquibase applied the changelog to the Testcontainers database'() {
        when:
        Sql sql = new Sql(dataSource)
        String version = sql.firstRow('SELECT VERSION() AS v').v
        long changeSets = sql.firstRow('SELECT COUNT(*) AS c FROM DATABASECHANGELOG').c as long
        long locks = sql.firstRow('SELECT COUNT(*) AS c FROM DATABASECHANGELOGLOCK WHERE LOCKED = 1').c as long
        println "[regression] database ${version}: ${changeSets} changesets applied, ${locks} held lock(s)"

        then:
        changeSets > 0
        locks == 0
    }

    void 'an authenticated API call succeeds'() {
        expect:
        locationGroupApiWrapper.api.list(new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data', Matchers.notNullValue())
                .build())
    }
}
