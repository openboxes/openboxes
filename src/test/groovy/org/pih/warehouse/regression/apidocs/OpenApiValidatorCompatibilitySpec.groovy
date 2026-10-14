package org.pih.warehouse.regression.apidocs

import com.atlassian.oai.validator.OpenApiInteractionValidator
import com.atlassian.oai.validator.model.Request
import com.atlassian.oai.validator.model.SimpleRequest
import com.atlassian.oai.validator.model.SimpleResponse
import com.atlassian.oai.validator.restassured.OpenApiValidationFilter
import com.sun.net.httpserver.HttpServer
import io.restassured.RestAssured
import spock.lang.Shared
import spock.lang.Specification

/**
 * Pins down that the OpenAPI request validator (swagger-request-validator 2.8.x, the last line built
 * on Jackson 2.9) works on this classpath: multi-file $refs, request and response validation, and
 * the RestAssured filter with the RestAssured version the API tests use.
 */
class OpenApiValidatorCompatibilitySpec extends Specification {

    @Shared OpenApiInteractionValidator validator = OpenApiInteractionValidator
            .createForSpecificationUrl(OpenApiValidatorCompatibilitySpec.getResource('/regression-apidocs/validator-probe/openapi.yaml').toString())
            .build()

    void "valid request and response pass"() {
        expect:
        !validator.validate(SimpleRequest.Builder.post('/api/things/1').withContentType('application/json').withBody('{"name":"a"}').build(),
                SimpleResponse.Builder.ok().withContentType('application/json').withBody('{"data":{"name":"a","quantity":1}}').build()).hasErrors()
    }

    void "invalid request body fails"() {
        expect:
        validator.validateRequest(SimpleRequest.Builder.post('/api/things/1').withContentType('application/json').withBody('{"quantity":"x"}').build()).hasErrors()
    }

    void "invalid response body fails"() {
        expect:
        validator.validateResponse('/api/things/1', Request.Method.GET,
                SimpleResponse.Builder.ok().withContentType('application/json').withBody('{"data":{"quantity":1}}').build()).hasErrors()
    }

    void "rest assured filter validates live traffic"() {
        given:
        HttpServer server = HttpServer.create(new InetSocketAddress('127.0.0.1', 0), 0)
        server.createContext('/api/things/') { ex ->
            byte[] body = (ex.requestURI.path.endsWith('/bad') ? '{"data":{}}' : '{"data":{"name":"a"}}').bytes
            ex.responseHeaders.add('Content-Type', 'application/json')
            ex.sendResponseHeaders(200, body.length); ex.responseBody.write(body); ex.close()
        }
        server.start()
        String base = "http://127.0.0.1:${server.address.port}"

        when:
        RestAssured.given().filter(new OpenApiValidationFilter(validator)).get("$base/api/things/1").then().statusCode(200)

        then:
        noExceptionThrown()

        when:
        RestAssured.given().filter(new OpenApiValidationFilter(validator)).get("$base/api/things/bad")

        then:
        thrown(OpenApiValidationFilter.OpenApiValidationException)

        cleanup:
        server?.stop(0)
    }
}
