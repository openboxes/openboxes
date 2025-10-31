package org.pih.warehouse.api.spec.base

import grails.buildtestdata.TestDataConfigurationHolder
import io.restassured.RestAssured
import io.restassured.builder.RequestSpecBuilder
import io.restassured.config.LogConfig
import io.restassured.filter.log.LogDetail
import io.restassured.http.ContentType
import io.restassured.http.Cookie
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import grails.gorm.transactions.Transactional
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Shared

import org.pih.warehouse.api.client.base.AuthenticatedApiContext
import org.pih.warehouse.api.client.base.UnauthenticatedApiContext
import org.pih.warehouse.api.client.inventory.RecordStockApiWrapper
import org.pih.warehouse.api.client.product.CategoryApiWrapper
import org.pih.warehouse.api.client.product.ProductApiWrapper
import org.pih.warehouse.api.util.JsonObjectUtil
import org.pih.warehouse.common.base.IntegrationSpec
import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.common.domain.builder.inventory.RecordInventoryCommandTestBuilder
import org.pih.warehouse.common.domain.builder.product.CategoryTestBuilder
import org.pih.warehouse.common.domain.builder.product.ProductTestBuilder
import org.pih.warehouse.common.service.TransactionTestService
import org.pih.warehouse.core.Location
import org.pih.warehouse.api.client.auth.AuthApiWrapper
import org.pih.warehouse.api.util.JsonPathUtil
import org.pih.warehouse.common.util.RandomUtil
import org.pih.warehouse.api.util.ResponseSpecUtil
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.RecordInventoryCommand
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product

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
 * As such, it's important to note that tests do *not* automatically clean up their data. While we should try to add a
 * clean up step to our test suites, it's not enforceable that this actually happens, and so tests should always assume
 * the database is in a dirty state before running.
 */
abstract class ApiSpec extends IntegrationSpec {

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
    Location facility

    @Shared
    Product product

    @Shared
    Category rootCategory

    @Autowired
    TransactionTestService transactionTestService

    @Autowired
    RecordStockApiWrapper recordStockApiWrapper

    @Autowired
    ProductApiWrapper productApiWrapper

    @Autowired
    CategoryApiWrapper categoryApiWrapper

    @Autowired
    AuthApiWrapper authApiWrapper

    @Shared
    RandomUtil randomUtil

    @Autowired
    ResponseSpecUtil responseSpecUtil

    @Autowired
    JsonObjectUtil jsonObjectUtil

    @Autowired
    JsonPathUtil jsonPathUtil

    /**
     * Create all the data that your tests need here. You'll find the findOrBuild(<Domain>) method incredibly helpful
     * for creating Domain objects when you just need some instance of a domain to exist in the db for you to use.
     * All domains that we work with should be defined in TestDataConfig.groovy so that the findOrBuild method knows
     * what to do if a field isn't provided. More info: https://longwa.github.io/build-test-data/index#findorbuild
     */
    void setupData() {
        // Providing a blank default implementation. Tests will override this to set up the data that they need.
    }

    /**
     * Removes all data created in the setupData step, likely via the Domain.delete() method.
     */
    void cleanupData() {
        // Providing a blank default implementation. Our tests should assume a dirty DB state and so it isn't
        // strictly required for us to do any database cleanup, but tests can override this method if they want to.
        // It's also often difficult to delete db data because we don't always have cascade delete rules and so we fail
        // on foreign key constraints if child objects aren't deleted first.
    }

    void setupSpec() {
        // Enables logging the request and response content when the asserts on an API call fail.
        RestAssured.config = RestAssured.config()
                .logConfig(LogConfig.logConfig()
                        .enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL)
                        .enablePrettyPrinting(true))
    }

    /**
     * Child classes shouldn't need to override this. Override setupData() instead.
     */
    @Transactional
    void setup() {
        setupRestAssuredGlobalConfig()

        randomUtil = new RandomUtil()

        // Initialize the API Contexts
        RequestSpecification baseUnauthenticatedRequestSpec = buildDefaultUnauthenticatedRequestSpec()
        unauthenticatedApiContext.loadContext(baseUnauthenticatedRequestSpec)

        // Create the facility if it doesn't exist yet (it should) and log the user into it.
        facility = createMainFacility()
        cookie = logInTestUser(facility)
        RequestSpecification baseRequestSpec = buildDefaultRequestSpec(cookie)
        authenticatedApiContext.loadContext(baseRequestSpec)

        // Set up some convenience data for tests. We shouldn't go overboard with setting stuff here. Only set
        // entities that a majority of tests will need to use. Test-specific setup should go in setupData().
        rootCategory = createRootCategory()
        product = createMainProduct()

        // In case the product already existed, wipe out any transactions on it so that we start from a fresh product.
        transactionTestService.deleteAllTransactions(facility, product)

        setupData()
    }

    /**
     * Child classes shouldn't need to override this. Override cleanupData() instead.
     */
    @Transactional
    void cleanup() {
        // Note that we don't attempt to clean up the convenience data that we created during setup because if any
        // test creates data that references them, such as adding stock to the product, and forgets to clean that up,
        // deleting the product will fail due to foreign key validation errors. So for simplicity we leave them in
        // the database.

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
    private Cookie logInTestUser(Location location) {
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

    private Location createMainFacility() {
        return new LocationTestBuilder().findOrBuildMainFacility()
    }

    private Product createMainProduct() {
        return productApiWrapper.saveOK(new ProductTestBuilder()
                .name("Test Product A")
                .category(rootCategory)
                .build())
    }

    private Category createRootCategory() {
        return categoryApiWrapper.createOK(new CategoryTestBuilder()
                .name("Test Root Category")
                .rootCategory()
                .build())
    }

    /**
     * Performs a record stock operation on the default test facility.
     * RecordInventoryCommandTestBuilder will likely be useful for constructing the command object.
     */
    void setStock(RecordInventoryCommand command) {
        recordStockApiWrapper.saveRecordStockOK(facility, command)
    }

    /**
     * Performs a record stock operation on the default test facility where you only need to add stock to a single
     * lot + bin. If you need to set the stock for multiple bins/lots, use RecordInventoryCommandTestBuilder instead.
     *
     * Transaction date will be the current timestamp, and any other transactions existing at the current time
     * will be deleted.
     */
    void setStock(Product product, InventoryItem item, Location binLocation, int quantity) {
        // We opt to do this via record stock since it sets a baseline and can be done in a single API call.
        RecordInventoryCommand command = new RecordInventoryCommandTestBuilder()
                .product(product)
                .inventory(facility.inventory)
                .transactionDateNow()
                .row(item?.lotNumber, item?.expirationDate, binLocation, quantity)
                .build()

        // We're trying to set the current stock for a product, so we need to delete any pre-existing transactions
        // that were created this second (which is very likely to happen during tests since they run really fast).
        // If we don't do this, duplicate transaction exceptions will be thrown or we'll end up with unexpected
        // QoH due to multiple baselines or adjustments existing at the same exact time.
        transactionTestService.deleteTransactionsOnOrAfterDate(facility, product, command.transactionDate)

        setStock(command)
    }
}
