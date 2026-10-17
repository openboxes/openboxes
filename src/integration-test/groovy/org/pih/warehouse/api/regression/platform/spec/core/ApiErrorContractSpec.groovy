package org.pih.warehouse.api.regression.platform.spec.core

import spock.lang.PendingFeature




import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import org.pih.warehouse.api.regression.platform.client.auth.RegressionAuthApi
import org.pih.warehouse.api.spec.core.*
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import org.apache.http.HttpStatus
import org.grails.web.json.JSONObject
import org.hamcrest.Matchers

import org.pih.warehouse.api.client.core.LocationGroupApiWrapper
import org.pih.warehouse.common.domain.builder.core.LocationGroupTestBuilder
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationGroup
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.LocationTypeCode
import org.springframework.beans.factory.annotation.Autowired

import static io.restassured.RestAssured.given

/**
 * The API's error contract: every error is JSON in one shape ({errorCode, errorMessage, errorMessages?}) with the
 * matching HTTP status, whatever the client asks for.
 *
 * Calls through the API client are also checked against the OpenAPI spec (the Error schema forbids other fields);
 * raw requests cover what the client cannot express (no Accept header, undocumented paths, pages).
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation
class ApiErrorContractSpec extends RegressionApiSpec {

    @Autowired
    RegressionAuthApi regressionAuthApi

    @Autowired
    LocationGroupApiWrapper locationGroupApiWrapper

    LocationGroup usedGroup

    Location zone

    @Override
    void setupData() {
        usedGroup = new LocationGroupTestBuilder().build(true)
        // A location that belongs to the group: the group cannot be deleted while it does.
        build(Location, [name: randomUtil.randomStringFieldValue('Location'), locationGroup: usedGroup])
        // A zone with a bin location in it: the zone cannot be deleted while it has one.
        LocationType zoneType = build(LocationType, [name: randomUtil.randomStringFieldValue('Zone type'),
                                                     locationTypeCode: LocationTypeCode.ZONE])
        zone = build(Location, [name: randomUtil.randomStringFieldValue('Zone'), locationType: zoneType])
        build(Location, [name: randomUtil.randomStringFieldValue('Bin'), zone: zone])
    }

    /** A request authenticated with the session cookie, against the context path. */
    RequestSpecification authenticated() {
        // spec() first: the authenticated spec carries RestAssured's global base path; ours must win.
        return given().spec(authenticatedRequestSpec).baseUri('http://localhost').port(serverPort)
                .basePath('/openboxes').redirects().follow(false)
    }

    /** A request with no session and no Accept header, like a script or a non-browser client would send. */
    RequestSpecification bare() {
        // a raw call as a non-browser client sends it, NOT marked AJAX (ObafAjaxHeaderFilter skips a request that sets
        // X-Requested-With itself; OBAF's RequestUtil.isAjax only accepts the value XMLHttpRequest)
        return given().baseUri('http://localhost').port(serverPort).basePath('/openboxes').redirects().follow(false)
                .header('X-Requested-With', 'none')
    }

    void 'a page without a session still redirects to the login page'() {
        when:
        Response response = bare().get('/dashboard/index')

        then:
        response.statusCode == HttpStatus.SC_MOVED_TEMPORARILY
        response.header('Location').contains('/auth/login')
    }

    @PendingFeature(reason = 'the dead CycleCountApiController.getInventoryTransactionsSummary is still reachable')
    void 'the removed cycle count copy of the inventory transactions summary is not reachable through the generic route'() {
        // The report lives at /api/reports/inventory-transactions-summary (inventoryTransactionSummaryApi); the old
        // cycleCountApi action called a service method that does not exist
        expect:
        authenticated().get('/cycleCountApi/getInventoryTransactionsSummary').statusCode == HttpStatus.SC_NOT_FOUND
    }

    @PendingFeature(reason = 'deleting a location group in use answers 500')
    void 'deleting something still in use answers 409 in the error shape'() {
        expect:
        locationGroupApiWrapper.api.delete(usedGroup.id, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_CONFLICT)
                .expectBody('errorCode', Matchers.equalTo(409))
                .expectBody('errorMessage', Matchers.not(Matchers.isEmptyOrNullString()))
                .build())
    }

    void 'a missing resource answers 404 in the error shape'() {
        expect:
        locationGroupApiWrapper.api.read(INVALID_ID, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_NOT_FOUND)
                .expectBody('errorCode', Matchers.equalTo(404))
                .build())
    }

    @PendingFeature(reason = 'POST /api/login answers text/html')
    void 'login answers JSON with the user and location'() {
        when:
        Response response = regressionAuthApi.login(new JSONObject()
                .put('username', username).put('password', password).put('location', facility.id).toString(),
                new ResponseSpecBuilder().expectStatusCode(HttpStatus.SC_OK).build())

        then:
        response.jsonPath().getString('data.username') == username
        response.jsonPath().getString('data.location') == facility.id
    }

    @PendingFeature(reason = 'a failed login is not a 401 in the error shape')
    void 'a failed login answers 401 in the error shape'() {
        expect:
        regressionAuthApi.login(new JSONObject().put('username', username).put('password', 'wrong').toString(),
                new ResponseSpecBuilder()
                        .expectStatusCode(HttpStatus.SC_UNAUTHORIZED)
                        .expectBody('errorMessage', Matchers.equalTo('Authentication failed'))
                        .build())
    }

    @PendingFeature(reason = 'chooseLocation does not answer JSON')
    void 'choosing a location answers JSON, and an unknown location 404'() {
        // The current location is part of the session: a session-mode test.
        when:
        Response chosen = bare().cookie(cookie).accept('application/json').put("/api/chooseLocation/${facility.id}")
        Response unknown = bare().cookie(cookie).accept('application/json').put("/api/chooseLocation/${INVALID_ID}")

        then:
        chosen.statusCode == HttpStatus.SC_OK
        chosen.jsonPath().getString('data.location') == facility.id
        unknown.statusCode == HttpStatus.SC_NOT_FOUND
        unknown.jsonPath().getInt('errorCode') == 404
    }

    @PendingFeature(reason = 'deleting an unknown or in-use location answers 500')
    void 'deleting a zone that still has bin locations answers 409; an unknown location 404'() {
        when:
        Response inUse = authenticated().delete("/api/locations/${zone.id}")
        Response unknown = authenticated().delete("/api/locations/${INVALID_ID}")

        then:
        inUse.statusCode == HttpStatus.SC_CONFLICT
        inUse.jsonPath().getInt('errorCode') == 409
        unknown.statusCode == HttpStatus.SC_NOT_FOUND
        unknown.jsonPath().getInt('errorCode') == 404
    }

    @PendingFeature(reason = 'logout does not answer JSON')
    void 'logout answers JSON and ends the session'() {
        given: 'a session of its own, so the shared one stays valid'
        Response login = regressionAuthApi.login(new JSONObject()
                .put('username', username).put('password', password).toString(),
                new ResponseSpecBuilder().expectStatusCode(HttpStatus.SC_OK).build())
        def session = login.getDetailedCookie('JSESSIONID')

        when:
        Response logout = bare().cookie(session).accept('application/json').post('/api/logout')

        then:
        logout.statusCode == HttpStatus.SC_OK
        logout.jsonPath().getString('data.message') == 'Logout was successful'
        bare().cookie(session).get('/api/locationGroups').statusCode == HttpStatus.SC_UNAUTHORIZED
    }

}
