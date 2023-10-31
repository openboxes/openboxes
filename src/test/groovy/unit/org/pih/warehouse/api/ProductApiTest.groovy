package unit.org.pih.warehouse.api

import io.restassured.path.json.JsonPath
import io.restassured.specification.RequestSpecification
import org.testng.Assert
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test
import spock.lang.Specification;

import static io.restassured.RestAssured.*;
import io.restassured.response.Response;

class ProductApiTest  extends Specification {

    Map headers = [:]
    String baseURI = "http://localhost:8080/openboxes";


    void setup() {
        println "inside setup"
        RequestSpecification httpRequest = given().formParam("username", "admin").formParam("password", "password");
        Response response = httpRequest.post(baseURI + "/auth/handleLogin");
        System.out.println(response.statusCode());
        System.out.println(response.getBody().asString());
        response.headers().each {
            if (it.name == "Set-Cookie") {
                headers["Cookie"] = it.value.substring(0, (it.value.indexOf(";") + 1))
            }
        }
    }

    void "read product list"() {
        when: "/api/products end point called"
            RequestSpecification httpRequest = given().header("Cookie", "${headers.Cookie}");
            Response response = httpRequest.get(baseURI + "/api/products");
            // Get JSON Representation from Response Body
            JsonPath jsonPathEvaluator = response.jsonPath();
            // Get specific element from JSON document
        then: "List of products should return"
            List data = jsonPathEvaluator.getList("data");
            int statusCode = response.getStatusCode();
        and: "response status should 200"
            Assert.assertEquals(statusCode, 200);
        and: "data list size should be 1"
            Assert.assertEquals(data.size(), 1);
    }
}