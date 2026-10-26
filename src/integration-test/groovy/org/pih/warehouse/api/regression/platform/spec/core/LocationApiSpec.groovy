package org.pih.warehouse.api.regression.platform.spec.core

import spock.lang.PendingFeature



import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import org.pih.warehouse.api.spec.core.*
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.response.Response
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.regression.platform.client.core.LocationApi
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.core.Location

// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation
class LocationApiSpec extends RegressionApiSpec {

    @Autowired
    LocationApi locationApi

    void 'list locations includes the current facility'() {
        expect:
        locationApi.list([:], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(facility.id))
                .build())
    }

    void 'list locations filters by name'() {
        expect:
        locationApi.list([name: facility.name], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(facility.id))
                .build())
    }

    @PendingFeature(reason = 'a short location search answers a non-JSON body')
    void 'list locations answers an empty list for a name shorter than three characters'() {
        expect:
        locationApi.list([name: 'ab'], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data', Matchers.empty())
                .build())
    }

    void 'list locations in the compact form'() {
        expect:
        locationApi.list([presentation: 'toBaseJson'], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(facility.id))
                .build())
    }

    @PendingFeature(reason = 'withOrganization=false acts as true')
    void 'list locations with withOrganization only lists locations that have one; false does not filter'() {
        given: 'a location without an organization (the facility has one)'
        Location zone = Location.withNewTransaction {
            new LocationTestBuilder().name("Docs Location ${UUID.randomUUID().toString().take(8)}").build(true)
        }

        expect:
        locationApi.list([withOrganization: true], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(facility.id))
                .expectBody('data.id', Matchers.not(Matchers.hasItem(zone.id)))
                .build())
        locationApi.list([withOrganization: false], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(zone.id))
                .build())
    }

    void 'list locations for a return order leaves out depots and suppliers'() {
        expect:
        locationApi.list([isReturnOrder: true], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.not(Matchers.hasItem(facility.id)))
                .build())
    }

    void 'list locations with applyUserFilter keeps the locations where the user has a role'() {
        expect: 'the admin test user has its role at the main facility'
        locationApi.list([applyUserFilter: true], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(facility.id))
                .build())
    }

    void 'list locations OUTBOUND lists where the current location can send to'() {
        expect:
        locationApi.list([direction: 'OUTBOUND'], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(facility.id))
                .build())
    }

    void 'read location by id returns the location'() {
        expect:
        locationApi.read(facility.id, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.equalTo(facility.id))
                .expectBody('data.name', Matchers.equalTo(facility.name))
                .build())
    }

    void 'the import template is a CSV download with the header row'() {
        when:
        Response response = locationApi.downloadTemplate(new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectContentType('text/csv')
                .expectHeader('Content-Disposition', Matchers.containsString('Location_template.csv'))
                .build())

        then:
        response.asString().contains('id,name,active,locationNumber,locationType')
    }

    void 'importing a locations CSV creates the locations'() {
        given:
        String name = "Imported Depot ${UUID.randomUUID().toString().take(8)}"
        String csv = 'id,name,active,locationNumber,locationType,locationGroup,parentLocation,organization,' +
                'streetAddress,streetAddress2,city,stateOrProvince,postalCode,country,description\n' +
                ",${name},true,,Depot,,,${name} Organization,,,,,,,Imported by a test\n"

        when:
        locationApi.importCsv('locations.csv', 'text/csv', csv.getBytes('UTF-8'), responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        Location.withNewTransaction { Location.findByName(name) } != null
    }

    @PendingFeature(reason = 'a location import with an empty file answers 500')
    void 'importing an empty file answers 400'() {
        expect:
        locationApi.importCsv('locations.csv', 'text/csv', new byte[0],
                responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
    }

    @PendingFeature(reason = 'a location import with a non-CSV file answers 500')
    void 'importing a file that is not CSV answers 400'() {
        expect:
        locationApi.importCsv('locations.txt', 'text/plain', 'name\nx\n'.getBytes('UTF-8'),
                responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
    }
}
