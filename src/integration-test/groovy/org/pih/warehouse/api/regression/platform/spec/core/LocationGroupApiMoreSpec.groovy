package org.pih.warehouse.api.regression.platform.spec.core

import spock.lang.PendingFeature



import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import org.pih.warehouse.api.spec.core.*
import io.restassured.builder.ResponseSpecBuilder
import org.apache.http.HttpStatus
import org.grails.web.json.JSONObject
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Shared

import org.pih.warehouse.api.client.core.LocationGroupApiWrapper
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.common.domain.builder.core.LocationGroupTestBuilder
import org.pih.warehouse.core.LocationGroup

// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation
class LocationGroupApiMoreSpec extends RegressionApiSpec {

    @Autowired
    LocationGroupApiWrapper locationGroupApiWrapper

    @Shared
    LocationGroup locationGroup

    @Override
    void setupData() {
        locationGroup = new LocationGroupTestBuilder().build(true)
    }

    @Override
    void cleanupData() {
        LocationGroup.get(locationGroup.id)?.delete()
    }

    @PendingFeature(reason = 'a stale location group version answers 500, not 409')
    void 'update location group answers 409 when the version sent is older than the stored one'() {
        given: 'someone else updated the group after we read version 0'
        locationGroupApiWrapper.updateOK(locationGroup.id, new JSONObject()
                .put('name', "First ${locationGroup.name}".toString())
                .put('version', 0))

        expect:
        locationGroupApiWrapper.api.update(locationGroup.id, new JSONObject()
                .put('name', "Second ${locationGroup.name}".toString())
                .put('version', 0)
                .toString(), responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_CONFLICT))
    }

}
