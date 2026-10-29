package org.pih.warehouse.api.spec.core

import io.restassured.builder.ResponseSpecBuilder
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Shared

import org.pih.warehouse.api.client.core.SelectOptionsApiWrapper
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.common.domain.builder.core.GlAccountTestBuilder
import org.pih.warehouse.common.domain.builder.core.GlAccountTypeTestBuilder
import org.pih.warehouse.core.GlAccount
import org.pih.warehouse.core.GlAccountType

class SelectOptionsApiGlAccountSpec extends ApiSpec {

    @Autowired
    SelectOptionsApiWrapper selectOptionsApiWrapper

    @Shared
    GlAccountType glAccountType

    @Shared
    GlAccount activeGlAccount

    @Shared
    GlAccount inactiveGlAccount

    @Override
    void setupData() {
        glAccountType = new GlAccountTypeTestBuilder().build(true)
        activeGlAccount = new GlAccountTestBuilder().glAccountType(glAccountType).build(true)
        inactiveGlAccount = new GlAccountTestBuilder().glAccountType(glAccountType).active(false).build(true)
    }

    @Override
    void cleanupData() {
        activeGlAccount?.delete()
        inactiveGlAccount?.delete()
        glAccountType?.delete()
    }

    void 'get gl account options should return both active and inactive gl accounts when active is not provided'() {
        expect:
        selectOptionsApiWrapper.api.glAccountOptions([:], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItems(activeGlAccount.id, inactiveGlAccount.id))
                .build())
    }

    void 'get gl account options should return label built from gl account code and name'() {
        expect:
        selectOptionsApiWrapper.api.glAccountOptions([:], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.label', Matchers.hasItem("${activeGlAccount.code} - ${activeGlAccount.name}".toString()))
                .build())
    }

    void 'get gl account options should return only active gl accounts when active is true'() {
        expect:
        selectOptionsApiWrapper.api.glAccountOptions([active: true], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(activeGlAccount.id))
                .expectBody('data.id', Matchers.not(Matchers.hasItem(inactiveGlAccount.id)))
                .build())
    }

    void 'get gl account options should return only inactive gl accounts when active is false'() {
        expect:
        selectOptionsApiWrapper.api.glAccountOptions([active: false], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(inactiveGlAccount.id))
                .expectBody('data.id', Matchers.not(Matchers.hasItem(activeGlAccount.id)))
                .build())
    }
}
