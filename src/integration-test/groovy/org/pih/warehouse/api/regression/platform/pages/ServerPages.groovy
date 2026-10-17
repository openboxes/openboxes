package org.pih.warehouse.api.regression.platform.pages

import io.restassured.http.Cookie
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification

import static io.restassured.RestAssured.given

/**
 * Requests against the server pages (the GSP controllers under the /openboxes context path) as a signed-in browser
 * sends them: the session cookie, no redirects followed.
 */
class ServerPages {

    final int port
    final Cookie cookie

    ServerPages(int port, Cookie cookie) {
        this.port = port
        this.cookie = cookie
    }

    RequestSpecification request() {
        return given().baseUri('http://localhost').port(port).basePath('/openboxes').cookie(cookie)
                .redirects().follow(false)
    }

    Response get(String path, Map<String, ?> query = [:]) {
        RequestSpecification spec = request().accept('text/html')
        query.each { String name, value -> spec = spec.queryParam(name, value) }
        return spec.get(path)
    }

    Response post(String path, Map<String, ?> form = [:]) {
        RequestSpecification spec = request().accept('text/html').contentType('application/x-www-form-urlencoded')
        form.each { String name, value ->
            spec = value instanceof Collection ? spec.formParam(name, (value as Collection).toArray()) :
                    spec.formParam(name, value)
        }
        return spec.post(path)
    }

    Response postJson(String path, String body) {
        return request().accept('application/json').contentType('application/json').body(body).post(path)
    }

    /** The path (from the context root on) a redirect points at, without the query string. */
    static String redirectPath(Response response) {
        String location = response.header('Location')
        assert location, "expected a redirect, got ${response.statusCode}"
        String path = new URI(location).path
        return path.startsWith('/openboxes') ? path.substring('/openboxes'.length()) : path
    }

    /** A rendered page: status 200, returns the body text. */
    static String page(Response response) {
        assert response.statusCode == 200, "expected 200, got ${response.statusCode}: ${response.body.asString().take(2000)}"
        return response.body.asString()
    }
}
