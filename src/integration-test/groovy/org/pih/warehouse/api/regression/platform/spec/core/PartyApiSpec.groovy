package org.pih.warehouse.api.regression.platform.spec.core

import spock.lang.PendingFeature



import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import org.pih.warehouse.api.spec.core.*
import groovy.json.JsonOutput
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.regression.platform.client.core.PartyApi
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.PartyType
import org.pih.warehouse.core.Person

/**
 * People and organizations created, read, changed and deleted through their resource routes (people through the
 * generic domain API behind /api/persons, organizations through OrganizationApiController and the generic API).
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation
class PartyApiSpec extends RegressionApiSpec {

    @Autowired
    PartyApi partyApi

    private String uniqueName(String prefix) {
        return randomUtil.randomStringFieldValue(prefix)
    }

    private static Map personRow(String id) {
        return Person.withNewSession {
            Person person = Person.get(id)
            person ? [firstName: person.firstName, lastName: person.lastName, email: person.email] : null
        }
    }

    private String newPerson() {
        return Person.withNewTransaction {
            new Person(firstName: uniqueName('First'), lastName: uniqueName('Last'), email: 'person@example.com')
                    .save(failOnError: true, flush: true).id
        }
    }

    void 'create a person: 201 with the person, and it is stored'() {
        given:
        String lastName = uniqueName('Created')

        when:
        Response response = partyApi.createPerson(JsonOutput.toJson([firstName: 'Ada', lastName: lastName,
                                                                      email: 'ada@example.com']), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_CREATED)
                .expectBody('data.firstName', Matchers.equalTo('Ada'))
                .expectBody('data.lastName', Matchers.equalTo(lastName))
                .build())

        then:
        personRow(response.jsonPath().getString('data.id')) == [firstName: 'Ada', lastName: lastName, email: 'ada@example.com']
    }

    void 'read a person'() {
        given:
        String id = newPerson()
        Map row = personRow(id)

        expect:
        partyApi.person(id, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.equalTo(id))
                .expectBody('data.firstName', Matchers.equalTo(row.firstName))
                .expectBody('data.lastName', Matchers.equalTo(row.lastName))
                .build())
    }

    void 'update a person with #method: the given fields change, the others stay'() {
        given:
        String id = newPerson()
        Map before = personRow(id)

        when:
        partyApi.updatePerson(id, JsonOutput.toJson([email: 'changed@example.com']), method, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.email', Matchers.equalTo('changed@example.com'))
                .build())

        then:
        personRow(id) == before + [email: 'changed@example.com']

        where:
        method << [Method.PUT, Method.POST]
    }

    void 'update a person with an invalid value: 400, unchanged'() {
        given:
        String id = newPerson()
        Map before = personRow(id)

        expect:
        partyApi.updatePerson(id, JsonOutput.toJson([firstName: '']), Method.PUT, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .build())
        personRow(id) == before
    }

    void 'delete a person: 204 and gone'() {
        given:
        String id = newPerson()

        when:
        partyApi.deletePerson(id, responseSpecUtil.NO_CONTENT_RESPONSE_SPEC)

        then:
        personRow(id) == null
    }

    private static Map organizationRow(String id) {
        return Organization.withNewSession {
            Organization organization = Organization.get(id)
            organization ? [name: organization.name, code: organization.code, partyType: organization.partyType?.code,
                            description: organization.description] : null
        }
    }

    private String newOrganization() {
        return Organization.withNewTransaction {
            new Organization(name: uniqueName('Org'), code: uniqueName('ORG').take(30),
                    partyType: PartyType.findByCode(Constants.DEFAULT_ORGANIZATION_CODE))
                    .save(failOnError: true, flush: true).id
        }
    }

    void 'create an organization: a code and the default party type are filled in'() {
        given:
        String name = uniqueName('Created Org')

        when:
        Response response = partyApi.createOrganization(JsonOutput.toJson([name: name]), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.not(Matchers.isEmptyOrNullString()))
                .build())
        Map row = organizationRow(response.jsonPath().getString('data.id'))

        then:
        row.name == name
        row.code
        row.partyType == Constants.DEFAULT_ORGANIZATION_CODE
    }

    @PendingFeature(reason = 'an organization without a name answers 500')
    void 'create an organization without a name: 400'() {
        expect:
        partyApi.createOrganization(JsonOutput.toJson([description: 'no name']), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody('errorCode', Matchers.equalTo(HttpStatus.SC_BAD_REQUEST))
                .build())
    }

    void 'update an organization with #method'() {
        given:
        String id = newOrganization()
        Map before = organizationRow(id)

        when:
        partyApi.updateOrganization(id, JsonOutput.toJson([description: 'Changed']), method, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.equalTo(id))
                .build())

        then:
        organizationRow(id) == before + [description: 'Changed']

        where:
        method << [Method.PUT, Method.POST]
    }

    void 'delete an organization: 204 and gone; an unknown one: 404'() {
        given:
        String id = newOrganization()

        when:
        partyApi.deleteOrganization(id, responseSpecUtil.NO_CONTENT_RESPONSE_SPEC)

        then:
        organizationRow(id) == null
        partyApi.deleteOrganization(INVALID_ID, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
        partyApi.updateOrganization(INVALID_ID, JsonOutput.toJson([description: 'x']), Method.PUT,
                responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }
}
