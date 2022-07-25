package org.pih.warehouse.api

import grails.testing.spock.OnceBefore
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.util.GrailsWebMockUtil
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.exceptions.HttpClientResponseException
import org.pih.warehouse.Application
import org.pih.warehouse.core.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.context.WebApplicationContext
import spock.lang.Shared
import spock.lang.Specification

@Rollback
@Integration(applicationClass = Application.class)
class ApiControllerFunctionalSpec extends Specification {

    @Shared
    HttpClient client

    @Shared
    Location location

    @Shared
    String cookie

    @Autowired
    WebApplicationContext ctx

    private String authenticate(String username, String password, Location location) {
        def postBody = [username: "admin", password: "password", location: location?.id]
        HttpRequest request = HttpRequest.POST('/openboxes/api/login', postBody).
                accept(MediaType.APPLICATION_JSON_TYPE)
        HttpResponse<Map> response = client.toBlocking().exchange(request, Map)
        return response.header("Set-Cookie")
    }

    @OnceBefore
    void init() {
        String baseUrl = "http://localhost:$serverPort"
        client = HttpClient.create(baseUrl.toURL())
    }

    def setup() {
        GrailsWebMockUtil.bindMockWebRequest(ctx)

        // Find or create login location
        PartyType orgPartyType = PartyType.findByPartyTypeCode(PartyTypeCode.ORGANIZATION)
        Organization organization = Organization.findOrCreateWhere([code: "TEST", name: "Test Corporation", partyType: orgPartyType]).save()
        LocationType defaultLocationType = LocationType.findByLocationTypeCode(LocationTypeCode.DEPOT)
        location = Location.findOrCreateWhere([locationNumber: "TEST", name: "Test Warehouse", locationType: defaultLocationType, organization: organization]).save()
    }

    void "list products"() {
        given:
        String cookie = authenticate("admin", "password", location)

        when:
        HttpRequest request = HttpRequest.GET('/openboxes/api/products?name=ibuprofen').
                header(HttpHeaders.COOKIE, cookie)
        HttpResponse response = client.toBlocking().exchange(request, String.class)

        then:
        response.status == HttpStatus.OK
    }

    void "login successful"() {
        when:
        HttpRequest request = HttpRequest.POST('/openboxes/api/login', [username: "admin", password: "password"])
        HttpResponse<Map> response = client.toBlocking().exchange(request, Map)

        then:
        response.status == HttpStatus.OK
    }

    void "login failed"() {
        when:
        HttpRequest request = HttpRequest.POST('/openboxes/api/login', [username: "admin", password: "invalidpassword"])
        HttpResponse<Map> response = client.toBlocking().exchange(request, Map)

        then:
        def e = thrown(HttpClientResponseException)
        e.response.status == HttpStatus.UNAUTHORIZED
    }

    void "get status"() {
        when:
        HttpRequest request = HttpRequest.GET("/openboxes/api/status").accept(MediaType.APPLICATION_JSON_TYPE)
        HttpResponse<Map> response = client.toBlocking().exchange(request, Map)

        then:
        response.status == HttpStatus.OK
        response.body()
        response.body().status == "OK"
    }
}
