package unit.org.pih.warehouse.api

import io.restassured.path.json.JsonPath
import io.restassured.specification.RequestSpecification
import org.testng.Assert
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.*;
import io.restassured.response.Response;
class ProductApiTest{

    Map headers = [:]
    String baseURI = "http://localhost:8080/openboxes";


    @BeforeClass
    public void setup() {
        println "inside setup"
        RequestSpecification httpRequest = given().formParam("username", "admin").formParam("password", "password");
        Response response = httpRequest.post(baseURI+"/auth/handleLogin");
        System.out.println(response.statusCode());
        System.out.println(response.getBody().asString());
        response.headers().each {
            if(it.name == "Set-Cookie"){
                headers["Cookie"] = it.value.substring(0, (it.value.indexOf(";")+1))
            }
        }

    }

    @Test
    public void test() {
        RequestSpecification httpRequest = given().header("Cookie", "${headers.Cookie}");
        Response response = httpRequest.get(baseURI+"/api/products");
        // Get JSON Representation from Response Body
        JsonPath jsonPathEvaluator = response.jsonPath();
        // Get specific element from JSON document
        List data = jsonPathEvaluator.getList("data");
        int statusCode = response.getStatusCode();
        Assert.assertEquals(statusCode, 200);
        Assert.assertEquals(data, []);
    }
}