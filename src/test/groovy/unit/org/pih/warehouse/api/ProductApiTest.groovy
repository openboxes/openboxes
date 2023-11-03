package unit.org.pih.warehouse.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import groovy.transform.builder.Builder
import io.restassured.RestAssured
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import io.restassured.http.Cookie
import io.restassured.path.json.JsonPath
import io.restassured.specification.RequestSpecification
import lombok.AllArgsConstructor
import lombok.Data
import lombok.NoArgsConstructor
import org.grails.web.json.JSONObject
import spock.lang.Shared
import spock.lang.Specification
import static io.restassured.RestAssured.*
import io.restassured.response.Response
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.notNullValue
import static org.hamcrest.Matchers.lessThanOrEqualTo

class ProductApiTest extends Specification {

    @Shared
    String baseURI = System.getProperty("server.host")?:"http://localhost"

    @Shared
    String basePath = System.getProperty("server.path")?:"/openboxes"

    @Shared
    int port = System.getProperty("server.port")?Integer.valueOf(System.getProperty("server.port")):8080

    @Shared
    String username = System.getProperty("username")?:"admin"

    @Shared
    String password = System.getProperty("password")?:"password"

    @Shared
    String productId

    @Shared
    RequestSpecification requestSpecification

    // Executed before the first spock test is executed
    void setupSpec() {

        RestAssured.basePath = basePath
        RestAssured.baseURI = baseURI
        RestAssured.port = port

        JSONObject jsonObject = new JSONObject().
                put("username", username).
                put("password", password).
                put("location", 1)

        Response response = given().log().all().
                contentType(ContentType.JSON).
                body(jsonObject.toString()).
                post("/api/login")

        // Make sure we were successfully authenticated
        response.then().assertThat().statusCode(200)

        // We should have a cookie at this point
        Cookie cookie = response.getDetailedCookie("JSESSIONID")

        // FIXME For some reason ReqestSpecBuilder doesn't respect the config settings above
        // so we need to set the baseURI, basePath, port again
        requestSpecification = new RequestSpecBuilder().
                setBaseUri(baseURI).
                setBasePath(basePath).
                setPort(port).
                addCookie(cookie).
                setAccept(ContentType.JSON).build()
    }

    void "should return a list of products"() {
        given: "an authenticated user"
        RequestSpecification request = given().spec(requestSpecification).log().all()

        when: "we get products from the API"
        // FIXME figure out better way to set up baseURI using RestAssured API
        Response response = request.when().get("/api/products?max=10")

        // FIXME Should be replaced by Product domain class once we
        List<ProductResponse> products = response.then().log().all().extract().body().jsonPath().
                getList("data", ProductResponse.class)

        then: "expected a 200 response with at most 10 products"
        response.then().assertThat().statusCode(200)
        assertThat(products, is(notNullValue()))
        assertThat(products.size(), is(lessThanOrEqualTo(10)))

        // Get a single product to pass to the next test
        when: "we attempt to share a product with the next test"
        ProductResponse product = response.then().body().extract().jsonPath().getObject("data[0]", ProductResponse)
        productId = product.id

        then: "product should not be null"
        assertThat(productId, is(notNullValue()))

    }

    void "get demand of product"() {
        given: "api/products/${productId}/demand called"
        RequestSpecification httpRequest = given().spec(requestSpecification).
                pathParams("productId", productId)

        when: "we request product demand resource"
        Response response = httpRequest.get("/api/products/{productId}/demand")
        JsonPath jsonPathEvaluator = response.jsonPath()
        def data = jsonPathEvaluator.get("data")

        then:
        response.then().assertThat().statusCode(200)

//        then: "data should not be null"
//        Assert.assertNotNull(data)
//        and: "product should not be null"
//        Assert.assertNotNull(data.product, "Response should have product object")
//        and: "product ID should not be null"
//        Assert.assertEquals(data.product.id, productId, "Product id should match with requested id:${productId}")
//        and: "demand should not be null"
//        Assert.assertNotNull(data.demand, "Demand Object should not be null")
    }

    void "get demand summary of product"() {
        given:
        RequestSpecification httpRequest = given().spec(requestSpecification).
                pathParams("productId", productId)

        when:
        Response response = httpRequest.get("/api/products/{productId}/demandSummary")
        // Get JSON Representation from Response Body
        JsonPath jsonPathEvaluator = response.jsonPath()
        def data = jsonPathEvaluator.getList("data")

        then:
        response.then().assertThat().statusCode(200)
//        Assert.that(is(notNullValue(data)))
//        and:
//        Assert.that(is(data.size(), equalTo(0)))
    }

    void "get summary of product"() {
        given:
        RequestSpecification httpRequest = given().spec(requestSpecification).pathParam("productId", productId)

        when: "api/products/${productId}/productSummary called"
        Response response = httpRequest.get("/api/products/{productId}/productSummary")
        // Get JSON Representation from Response Body
        JsonPath jsonPathEvaluator = response.jsonPath()
        def data = jsonPathEvaluator.get("data")

        then:
        response.then().assertThat().statusCode(200)
//        Assert.assertEquals(data.product.id, productId, "Product id should be ${productId}")
//        and:
//        Assert.assertEquals(data.location.id, locationId, "Location id should be ${locationId}")
//        and:
//        Integer quantityOnHand = data.quantityOnHand ?: 0
//        Assert.assertEquals(quantityOnHand, 0, "QuantityOnHand should be 0")
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
class ProductResponse {
    String id
    String productCode
    String name
}
