package org.pih.warehouse.api.regression.platform.spec.core

import spock.lang.PendingFeature




import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import spock.lang.Ignore
import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import org.pih.warehouse.api.regression.platform.client.generic.RegressionGenericApi
import org.pih.warehouse.api.regression.platform.client.generic.RegressionGenericApiWrapper
import org.pih.warehouse.api.regression.platform.client.generic.RegressionGenericResource
import org.pih.warehouse.api.spec.core.*
import io.restassured.builder.ResponseSpecBuilder
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.regression.platform.client.core.DirectoryApi
import org.pih.warehouse.api.regression.platform.client.core.SelectOptionsApi
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.PartyType
import org.pih.warehouse.core.User

/**
 * The lookups behind the stock movement and purchase order list filters: people, users, organizations, payment
 * terms, shipment status codes and shipment types, called the way those pages call them.
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation
class DirectoryApiSpec extends RegressionApiSpec {

    @Autowired
    DirectoryApi directoryApi

    @Autowired
    SelectOptionsApi regressionSelectOptionsApi

    @Autowired
    RegressionGenericApi regressionGenericApi

    void 'people are found by name, active only (the "requested by" filter)'() {
        given: 'the signed-in user, who is a person too'
        Map user = User.withNewSession { User found = User.findByUsername(username); [id: found.id, name: found.firstName] }

        expect:
        directoryApi.persons([name: user.name, status: true], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(user.id))
                .build())
    }

    void 'users are listed, and limited to a role at the current location on request (the "created by" filter)'() {
        expect:
        directoryApi.users([:], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.username', Matchers.hasItem(username))
                .build())
        directoryApi.users([roleTypes: 'ROLE_MANAGER'], responseSpecUtil.OK_RESPONSE_SPEC)
    }

    void 'empty search terms are ignored: a term that matches nobody still finds nobody'() {
        expect:
        directoryApi.users([searchTerm: 'zzNoSuchUserQq, ,'], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data', Matchers.empty())
                .build())
    }

    void 'organizations are listed by name and role (the "supplier" filter), and read by id'() {
        given:
        String organizationId = facility.organization.id

        expect:
        directoryApi.organizations([q: facility.organization.name, active: true], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(organizationId))
                .build())
        directoryApi.organizations([roleType: 'ROLE_SUPPLIER', active: true], responseSpecUtil.OK_RESPONSE_SPEC)
        directoryApi.organization(organizationId, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.equalTo(organizationId))
                .build())
    }

    @PendingFeature(reason = 'active=false still lists only active organizations')
    void 'active=true lists active organizations only; active=false (what the supplier selects send) lists them all'() {
        given: 'an inactive supplier'
        String code = randomUtil.randomStringFieldValue('INACT')
        String inactiveId = Organization.withNewTransaction {
            new Organization(code: code, name: "Inactive supplier ${code}", active: false,
                    partyType: PartyType.findByCode('ORG')).save(failOnError: true).id
        }

        expect:
        directoryApi.organizations([q: code, active: false], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(inactiveId))
                .build())
        directoryApi.organizations([q: code, active: true], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.not(Matchers.hasItem(inactiveId)))
                .build())
    }

    @PendingFeature(reason = 'an unknown organization answers 500, not 404')
    void 'an unknown organization is a 404'() {
        expect:
        directoryApi.organization(INVALID_ID, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    void 'payment terms are listed as options (the purchase order filter)'() {
        expect:
        regressionSelectOptionsApi.options('/paymentTermOptions', [:], responseSpecUtil.OK_RESPONSE_SPEC)
    }

    void 'shipment status codes are listed, without the excluded ones (the inbound list filter)'() {
        expect:
        regressionSelectOptionsApi.options('/stockMovements/shipmentStatusCodes', [:], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data', Matchers.not(Matchers.empty()))
                .build())
        regressionSelectOptionsApi.options('/stockMovements/shipmentStatusCodes', [excludedStatuses: 'PENDING'],
                new ResponseSpecBuilder()
                        .expectStatusCode(HttpStatus.SC_OK)
                        .expectBody('data.id', Matchers.not(Matchers.hasItem('PENDING')))
                        .build())
    }

    void 'shipment types are listed through the generic API (the shipment type filter)'() {
        expect:
        regressionGenericApi.list(RegressionGenericResource.SHIPMENT_TYPE, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data', Matchers.not(Matchers.empty()))
                .build())
    }
}
