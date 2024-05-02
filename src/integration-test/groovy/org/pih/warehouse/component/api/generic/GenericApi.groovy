package org.pih.warehouse.component.api.generic

import groovy.transform.InheritConstructors
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification

import org.pih.warehouse.component.api.base.Api
import org.pih.warehouse.util.generic.GenericResource

@InheritConstructors
class GenericApi extends Api {

    Response list(GenericResource resource, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addPathParam("resource", resource)
                .build()

        return request(requestSpec, responseSpec, Method.GET, "/generic/{resource}")
    }

    Response get(GenericResource resource, String id, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addPathParam("resource", resource)
                .addPathParam("id", id)
                .build()

        return request(requestSpec, responseSpec, Method.GET, "/generic/{resource}/{id}")
    }

    Response create(GenericResource resource, String body, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addPathParam("resource", resource)
                .setBody(body)
                .build()

        return request(requestSpec, responseSpec, Method.POST, "/generic/{resource}")
    }

    Response update(GenericResource resource, String id, String body, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addPathParam("resource", resource)
                .addPathParam("id", id)
                .setBody(body)
                .build()

        return request(requestSpec, responseSpec, Method.PUT, "/generic/{resource}/{id}")
    }

    Response delete(GenericResource resource, String id, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addPathParam("resource", resource)
                .addPathParam("id", id)
                .build()

        return request(requestSpec, responseSpec, Method.DELETE, "/generic/{resource}/{id}")
    }
}
