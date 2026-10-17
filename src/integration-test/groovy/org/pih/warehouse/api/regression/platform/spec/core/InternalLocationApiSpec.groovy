package org.pih.warehouse.api.regression.platform.spec.core

import spock.lang.PendingFeature



import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import org.pih.warehouse.api.spec.core.*
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.ResponseSpecification
import org.apache.http.HttpStatus
import org.grails.web.json.JSONObject
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Shared

import org.pih.warehouse.api.regression.platform.base.AccessCheckApi
import org.pih.warehouse.api.regression.platform.client.core.InternalLocationApi
import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.core.Location

/**
 * Bin and internal locations of the current facility. The main facility (where the user is logged in) holds one bin
 * of the spec's own; a second facility holds a bin that the current facility's lists must not show.
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation
class InternalLocationApiSpec extends RegressionApiSpec {

    @Autowired
    InternalLocationApi internalLocationApi

    @Shared
    Map<String, String> ids

    @Override
    void setupData() {
        if (ids) {
            return
        }
        ids = Location.withNewTransaction {
            String tag = randomUtil.randomStringFieldValue('IL')
            Location depot = new LocationTestBuilder().name("${tag} depot").asFacility().build(true)
            Location binA = new LocationTestBuilder().name("${tag} A-1").asBinLocation(depot).build(true)
            Location mainBin = new LocationTestBuilder().name("${tag} main").asBinLocation(Location.get(facility.id))
                    .build(true)
            [binA: binA.id, mainBin: mainBin.id, mainBinName: mainBin.name]
        }
    }

    void 'the bin locations without a facility are those of the current one'() {
        expect:
        internalLocationApi.binLocations([:], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(ids.mainBin))
                .expectBody('data.id', Matchers.not(Matchers.hasItem(ids.binA)))
                .build())
    }

    @PendingFeature(reason = 'an unknown facility on the bin location list answers 500, not 404')
    void 'a bin location by id; unknown ids and facilities are a 404'() {
        expect:
        internalLocationApi.binLocation(ids.mainBin, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.equalTo(ids.mainBin))
                .expectBody('data.name', Matchers.equalTo(ids.mainBinName))
                .build())
        internalLocationApi.binLocation('no-such-bin', error(HttpStatus.SC_NOT_FOUND))
        internalLocationApi.binLocations(['parentLocation.id': 'no-such-facility'], error(HttpStatus.SC_NOT_FOUND))
    }

    @PendingFeature(reason = 'internal locations without location.id return nothing')
    void 'the internal locations without a facility are those of the current one'() {
        expect:
        internalLocationApi.internalLocations([:], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(ids.mainBin))
                .expectBody('data.id', Matchers.not(Matchers.hasItem(ids.binA)))
                .build())
    }

    @PendingFeature(reason = 'without a facility and a current location the list answers 500')
    void 'the internal locations without a facility and without a current one are a 400'() {
        given: 'a session logged in without a location'
        Response login = authApiWrapper.login(new JSONObject().put('username', username).put('password', password),
                HttpStatus.SC_OK)

        expect:
        new AccessCheckApi(new RequestSpecBuilder()
                .setAccept(ContentType.JSON)
                .addCookie(login.getDetailedCookie('JSESSIONID'))
                .build())
                .call('GET', '/api/internalLocations', error(HttpStatus.SC_BAD_REQUEST))
    }

    @PendingFeature(reason = 'an unknown internal location is not a 404')
    void 'an internal location by id; an unknown one is a 404'() {
        expect:
        internalLocationApi.internalLocation(ids.mainBin, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.equalTo(ids.mainBin))
                .build())
        internalLocationApi.internalLocation('no-such-location', error(HttpStatus.SC_NOT_FOUND))
    }

    static ResponseSpecification error(int status) {
        return new ResponseSpecBuilder()
                .expectStatusCode(status)
                .expectBody('errorCode', Matchers.equalTo(status))
                .expectBody('errorMessage', Matchers.notNullValue())
                .build()
    }
}
