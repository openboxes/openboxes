package org.pih.warehouse.api.regression.platform.spec.auth

import spock.lang.PendingFeature



import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import org.pih.warehouse.api.regression.platform.client.auth.RegressionAuthApi
import org.pih.warehouse.api.spec.auth.*
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.http.ContentType
import io.restassured.response.Response
import org.apache.http.HttpStatus
import org.grails.web.json.JSONObject
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.regression.platform.client.auth.SessionApi
import org.pih.warehouse.api.regression.platform.base.AccessCheckApi

/**
 * The session operations a client uses right after logging in: the app context, the menu, the current location and
 * language, and logging out.
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation
class SessionApiSpec extends RegressionApiSpec {

    @Autowired
    SessionApi sessionApi

    @Autowired
    RegressionAuthApi regressionAuthApi

    void 'status answers without a login'() {
        expect:
        regressionAuthApi.status(new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('status', Matchers.equalTo('OK'))
                .expectBody('database.status', Matchers.equalTo(true))
                .build())
    }

    void 'the app context describes the user, the current location and the settings'() {
        expect:
        sessionApi.getAppContext(new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.user.username', Matchers.equalTo(username))
                .expectBody('data.location.id', Matchers.equalTo(facility.id))
                .expectBody('data.supportedLocales.code', Matchers.hasItem('en'))
                .expectBody('data.menuItems', Matchers.not(Matchers.empty()))
                .build())
    }

    void 'the menu config lists the menu sections for the current location'() {
        expect:
        sessionApi.getMenuConfig(new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.menuConfig', Matchers.not(Matchers.empty()))
                .build())
    }

    @PendingFeature(reason = 'chooseLocation does not answer JSON')
    void 'choosing a location makes it the current location'() {
        expect:
        sessionApi.chooseLocation(facility.id, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.location', Matchers.equalTo(facility.id))
                .build())
    }

    void 'choosing an unknown location answers 404'() {
        expect:
        sessionApi.chooseLocation(INVALID_ID, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    void 'choosing a language changes the language of the session'() {
        when:
        sessionApi.chooseLocale('fr', new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.activeLanguage', Matchers.equalTo('fr'))
                .expectBody('data.activeLanguageTag', Matchers.equalTo('fr'))
                .build())

        then:
        sessionApi.getAppContext(new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.activeLanguage', Matchers.equalTo('fr'))
                .build())
    }

    @PendingFeature(reason = 'chooseLocale accepts any locale code')
    void 'choosing an unsupported language answers 404'() {
        expect:
        sessionApi.chooseLocale('xx', responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    @PendingFeature(reason = 'logout does not answer JSON')
    void 'logging out ends the session'() {
        when:
        sessionApi.logout(new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.message', Matchers.equalTo('Logout was successful'))
                .build())

        then: 'the session no longer authenticates'
        sessionApi.getAppContext(new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_UNAUTHORIZED)
                .expectBody('errorMessage', Matchers.equalTo('Login required: log in with POST /api/login'))
                .build())
    }

    @PendingFeature(reason = 'location-bound operations answer 500 before a location is chosen')
    void 'before a location is chosen the app context has no location, and location-bound operations ask for one'() {
        given: 'a session logged in without a location'
        Response login = authApiWrapper.login(new JSONObject().put('username', username).put('password', password),
                HttpStatus.SC_OK)
        AccessCheckApi noLocation = new AccessCheckApi(new RequestSpecBuilder()
                .setAccept(ContentType.JSON)
                .addCookie(login.getDetailedCookie('JSESSIONID'))
                .build())

        expect:
        noLocation.call('GET', '/api/getAppContext', new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.user.username', Matchers.equalTo(username))
                .expectBody('data.location', Matchers.nullValue())
                .expectBody('data.supportedActivities', Matchers.empty())
                .build())
        noLocation.call('GET', '/api/getMenuConfig', responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
        noLocation.call('GET', '/api/products/{id}/productSummary',
                responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
        noLocation.call('GET', '/api/products/{id}/productAvailability',
                responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
        new AccessCheckApi(new RequestSpecBuilder()
                .setAccept(ContentType.JSON)
                .addCookie(login.getDetailedCookie('JSESSIONID'))
                .addQueryParam('direction', 'OUTBOUND')
                .build())
                .call('GET', '/api/locations', responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
    }
}
