package org.pih.warehouse.api.regression.platform.client.core

import org.pih.warehouse.api.client.core.*
import groovy.transform.InheritConstructors
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.AuthenticatedApi

/**
 * Creating, reading, changing and deleting people and organizations (POST/PUT/DELETE /api/persons and
 * /api/organizations). The lookups the list pages make are in DirectoryApi.
 */
@TestComponent
@InheritConstructors
class PartyApi extends AuthenticatedApi {

    Response createPerson(String body, ResponseSpecification responseSpec) {
        return request(withBody(body), responseSpec, Method.POST, '/persons')
    }

    Response person(String id, ResponseSpecification responseSpec) {
        return request(withId(id), responseSpec, Method.GET, '/persons/{id}')
    }

    /** @param method PUT or POST: both update */
    Response updatePerson(String id, String body, Method method, ResponseSpecification responseSpec) {
        return request(withIdAndBody(id, body), responseSpec, method, '/persons/{id}')
    }

    Response deletePerson(String id, ResponseSpecification responseSpec) {
        return request(withId(id), responseSpec, Method.DELETE, '/persons/{id}')
    }

    Response createOrganization(String body, ResponseSpecification responseSpec) {
        return request(withBody(body), responseSpec, Method.POST, '/organizations')
    }

    /** @param method PUT or POST: both update */
    Response updateOrganization(String id, String body, Method method, ResponseSpecification responseSpec) {
        return request(withIdAndBody(id, body), responseSpec, method, '/organizations/{id}')
    }

    Response deleteOrganization(String id, ResponseSpecification responseSpec) {
        return request(withId(id), responseSpec, Method.DELETE, '/organizations/{id}')
    }

    private static RequestSpecification withBody(String body) {
        return new RequestSpecBuilder().setBody(body).build()
    }

    private static RequestSpecification withId(String id) {
        return new RequestSpecBuilder().addPathParam('id', id).build()
    }

    private static RequestSpecification withIdAndBody(String id, String body) {
        return new RequestSpecBuilder().addPathParam('id', id).setBody(body).build()
    }
}
