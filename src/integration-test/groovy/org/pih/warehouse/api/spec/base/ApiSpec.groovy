package org.pih.warehouse.api.spec.base

import grails.buildtestdata.TestDataBuilder
import grails.buildtestdata.TestDataConfigurationHolder
import io.restassured.RestAssured
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import io.restassured.http.Cookie
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import grails.gorm.transactions.Transactional
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Shared

import org.pih.warehouse.api.client.base.AuthenticatedApiContext
import org.pih.warehouse.api.client.base.UnauthenticatedApiContext
import org.pih.warehouse.common.base.IntegrationSpec
import org.pih.warehouse.common.domains.builders.location.TestLocationBuilder
import org.pih.warehouse.core.Location
import org.pih.warehouse.api.client.auth.AuthApiWrapper
import org.pih.warehouse.api.util.JsonPathUtil
import org.pih.warehouse.common.util.RandomUtil
import org.pih.warehouse.api.util.ResponseSpecUtil

/**
 * Base class for all of our API/component tests.
 *
 * Note that even though we use the @Integration annotation, these tests are more like API tests because we're making
 * restful API calls into our server (hence the name ApiSpec). Confusingly, Grails refers to this type of API testing
 * as "functional testing", but we're going to ignore that term for our purposes because Grails "functional tests"
 * expect us to extend from GebSpec, which isn't supported for APIs that return JSON. Instead we use RestAssured.
 *
 * To be able to make Domain.save() calls from within a method, you must add the @Transactional annotation to it or
 * wrap the save with Domain.withNewTransaction{}. Remember that the transaction will only be submitted once the method
 * completes, and so don't annotate individual tests because then that data won't ever be visible. This is why
 * we didn't put the annotation at the class level. Use the setupData() method for creating domain data when possible.
 *
 * Note that we cannot use the @Rollback annotation here to automatically clean up data created by Domain.save() calls
 * because test HTTP clients and the server run in two separate processes and thus two separate transactions. If we
 * used the annotation, the Domain.save() data would get rolled back successfully, but would be totally inaccessible
 * by the server during the tests.
 *
 * As such, it's important to note that tests do *not* automatically cleanup their data. While we should try to add a
 * clean up step to our test suites, it's not enforceable that this actually happens, and so tests should always assume
 * the database is in a dirty state before running.
 */
abstract class ApiSpec extends IntegrationSpec implements TestDataBuilder {

    static final String INVALID_ID = "-1"

    @Shared
    String username = System.getProperty("username")?:"admin"

    @Shared
    String password = System.getProperty("password")?:"password"

    @Shared
    Cookie cookie

    @Autowired
    AuthenticatedApiContext authenticatedApiContext

    @Autowired
    UnauthenticatedApiContext unauthenticatedApiContext

    @Shared
    Location location

    @Shared
    Location otherLocation

    @Autowired
    AuthApiWrapper authApiWrapper

    @Shared
    RandomUtil randomUtil

    @Autowired
    ResponseSpecUtil responseSpecUtil

    @Autowired
    JsonPathUtil jsonPathUtil

    /**
     * Create all the data that your tests need here. You'll find the findOrBuild(<Domain>) method incredibly helpful
     * for creating Domain objects when you just need some instance of a domain to exist in the db for you to use.
     * All domains that we work with should be defined in TestDataConfig.groovy so that the findOrBuild method knows
     * what to do if a field isn't provided. More info: https://longwa.github.io/build-test-data/index#findorbuild
     *
     * Marked with @Transactional since we're working directly with Domain objects, which need a transaction.
     *
     * Make sure to annotate child class implementations with @Transactional as well since unfortunately the annotation
     * doesn't automatically apply to child classes.
     */
    @Transactional
    abstract void setupData()

    /**
     * Removes all data created in the setupData step, likely via the Domain.delete() method.
     *
     * Marked with @Transactional since we're working directly with Domain objects, which need a transaction.
     *
     * Make sure to annotate child class implementations with @Transactional as well since unfortunately the annotation
     * doesn't automatically apply to child classes.
     */
    @Transactional
    void cleanupData() {
        // Providing a blank default implementation. Our tests should assume a dirty DB state and so it isn't
        // strictly required for us to do any database cleanup, but tests can override this method if they want to.
        // It's also often difficult to delete db data because we don't always have cascade delete rules and so we fail
        // on foreign key constraints if child objects aren't deleted first.
    }

    @Transactional
    void setup() {
        setupRestAssuredGlobalConfig()

        randomUtil = new RandomUtil()

        // Initialize the API Contexts
        RequestSpecification baseUnauthenticatedRequestSpec = buildDefaultUnauthenticatedRequestSpec()
        unauthenticatedApiContext.loadContext(baseUnauthenticatedRequestSpec)

        // Create two locations so that we can test cross-facility flows.
        // Note that we don't attempt to clean up these locations on test teardown because if any test that uses them
        // fails to clean up their own data, we'll fail to delete the location due to foreign key validation errors.
        location = new TestLocationBuilder(name: "TEST LOCATION")
                .facilityLocation()
                .findOrBuild()
        otherLocation = new TestLocationBuilder(name: "OTHER TEST LOCATION")
                .facilityLocation()
                .findOrBuild()

        cookie = createTestUser(location)
        RequestSpecification baseRequestSpec = buildDefaultRequestSpec(cookie)
        authenticatedApiContext.loadContext(baseRequestSpec)

        setupData()
    }

    @Transactional
    void cleanup() {
        // Restores the generators in TestDataConfig in case any tests overrode the behaviour. This will also cause any
        // variables in TestDataConfig to be reset to their initial values and any randoms to be regenerated.
        TestDataConfigurationHolder.reset()

        cleanupData()
    }

    /**
     * Set up some static, global config for all API requests that go through Rest Assured.
     */
    private void setupRestAssuredGlobalConfig() {
        RestAssured.baseURI = "http://localhost"  // This is the default but defining here for clarity.
        RestAssured.port = serverPort  // @Integration assigns a random port for the server, accessible by this field.
        RestAssured.basePath = "/openboxes/api"
    }

    /**
     * Authenticate a user and extract their session id for use by all subsequent requests.
     */
    private Cookie createTestUser(Location location) {
        Response authResponse = authApiWrapper.loginOK(username, password, location.id)
        return authResponse.getDetailedCookie("JSESSIONID")
    }

    /**
     * Builds a default RequestSpecification for use by all authenticated APIs in the test. This way we don't need
     * to re-specify all these details for each individual API request. Using this, all a request needs to add are
     * its request specific fields such as request params and query params.
     */
    private RequestSpecification buildDefaultRequestSpec(Cookie cookie) {
        return new RequestSpecBuilder()
                .addCookie(cookie)
                .setAccept(ContentType.JSON)
                .setContentType(ContentType.JSON)
                .build()
    }

    /**
     * Builds a default RequestSpecification for use by all unauthenticated APIs in the test. This way we don't need
     * to re-specify all these details for each individual API request. Using this, all a request needs to add are
     * its request specific fields such as request params and query params.
     */
    private RequestSpecification buildDefaultUnauthenticatedRequestSpec() {
        return new RequestSpecBuilder()
                .setAccept(ContentType.JSON)
                .setContentType(ContentType.JSON)
                .build()
    }
}
