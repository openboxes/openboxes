package unit.org.pih.warehouse.api

import io.restassured.path.json.JsonPath
import io.restassured.specification.RequestSpecification
import org.testng.Assert
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test
import spock.lang.Shared
import spock.lang.Specification;

import static io.restassured.RestAssured.*;
import io.restassured.response.Response;

class ProductApiTest extends Specification {

    @Shared
    String baseURI = "http://localhost:8080/openboxes";

    @Shared
    Map headers = [:]

    @Shared
    boolean setupInitialized = false

    @Shared
    String productId

    @Shared
    String locationId = 1

    void setup() {
        if (!setupInitialized) {
            RequestSpecification httpRequest = given().formParam("username", "admin").formParam("password", "password");
            Response response = httpRequest.post(baseURI + "/auth/handleLogin");
            response.headers().each {
                if (it.name == "Set-Cookie") {
                    headers["Cookie"] = it.value.substring(0, (it.value.indexOf(";")))
                }
            }
            httpRequest = given().header("Cookie", "${headers.Cookie}");
            response = httpRequest.get(baseURI + "/dashboard/chooseLocation/${locationId}");
            setupInitialized = true
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
        and:
        Assert.assertEquals(statusCode, 200, "response status should 200");
        and:
        Assert.assertEquals(data.size(), 1, "data list size should be 1");
        when:
        def product = data.get(0)
        productId = product.id
        then:
        Assert.assertNotNull(productId, "Product id should not be null")
    }

    void "get demand of product"() {
        when:
        "api/products/${productId}/demand called"
        RequestSpecification httpRequest = given().header("Cookie", "${headers.Cookie}");
        Response response = httpRequest.get(baseURI + "/api/products/${productId}/demand");
        // Get JSON Representation from Response Body
        JsonPath jsonPathEvaluator = response.jsonPath();
        then:
        def data = jsonPathEvaluator.get("data")
        Assert.assertNotNull(data)
        and:
        Assert.assertNotNull(data.product, "Response should have product object")
        and:
        Assert.assertEquals(data.product.id, productId, "Product id should match with requested id:${productId}")
        and:
        Assert.assertNotNull(data.demand, "Demand Object should not be null")
    }

    void "get demand summary of product"() {
        when:
        "api/products/${productId}/demandSummary called"
        RequestSpecification httpRequest = given().header("Cookie", "${headers.Cookie}");
        Response response = httpRequest.get(baseURI + "/api/products/${productId}/demandSummary");
        // Get JSON Representation from Response Body
        JsonPath jsonPathEvaluator = response.jsonPath();
        then:
        def data = jsonPathEvaluator.getList("data")
        Assert.assertNotNull(data)
        and:
        Assert.assertEquals(data.size(), 0, "Demand summary should be empty")
    }

    void "get summary of product"() {
        when:
        "api/products/${productId}/productSummary called"
        RequestSpecification httpRequest = given().header("Cookie", "${headers.Cookie}");
        Response response = httpRequest.get(baseURI + "/api/products/${productId}/productSummary");
        // Get JSON Representation from Response Body
        JsonPath jsonPathEvaluator = response.jsonPath();
        then:
        def data = jsonPathEvaluator.get("data")
        Assert.assertEquals(data.product.id, productId, "Product id should be ${productId}")
        and:
        Assert.assertEquals(data.location.id, locationId, "Location id should be ${locationId}")
        and:
        Integer quantityOnHand = data.quantityOnHand ?: 0
        Assert.assertEquals(quantityOnHand, 0, "QuantityOnHand should be 0")
    }

}