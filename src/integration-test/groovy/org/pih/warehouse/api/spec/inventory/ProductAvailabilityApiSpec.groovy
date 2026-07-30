package org.pih.warehouse.api.spec.inventory

import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.client.inventory.ProductAvailabilityApiWrapper
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.api.util.ResponseSpecUtil

import static io.restassured.RestAssured.given

class ProductAvailabilityApiSpec extends ApiSpec {

    @Autowired
    ProductAvailabilityApiWrapper productAvailabilityApiWrapper

    @Autowired
    ResponseSpecUtil responseSpecUtil

    void 'list rejects unauthenticated requests'() {
        expect:
        given(unauthenticatedApiContext.baseRequestSpec)
                .pathParam("facilityId", facility.id)
                .when()
                    .get("/facilities/{facilityId}/availableItems")
                .then()
                    .spec(responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_UNAUTHORIZED))
    }

    void 'list returns stock for the facility with required fields'() {
        given:
        setStock(product, null, null, 10)

        when:
        def response = productAvailabilityApiWrapper.listOK(facility.id)
        List data = response.jsonPath().getList("data")

        then:
        data.size() >= 1
        def row = data.find { it.productCode == product.productCode }
        row != null
        row["product.name"] == product.name
        row.productCode == product.productCode
        row.quantityOnHand == 10
        row.containsKey("quantityAvailable")
        row.containsKey("lotNumber")
        row.containsKey("expirationDate")
        row.containsKey("binLocation")
        row.containsKey("zone")
        !row.containsKey("zones")
    }

    void 'list excludes zero quantity on hand rows'() {
        given:
        setStock(product, null, null, 0)

        when:
        def response = productAvailabilityApiWrapper.listOK(facility.id)
        List data = response.jsonPath().getList("data")

        then:
        !data.any { it.productCode == product.productCode && it.quantityOnHand == 0 }
    }

    void 'list returns empty data when facility has no matching stock'() {
        // facility/product start with no stock after ApiSpec setup deletes transactions
        when:
        def response = productAvailabilityApiWrapper.listOK(facility.id)
        List data = response.jsonPath().getList("data")

        then:
        data.findAll { it.productCode == product.productCode }.isEmpty()
    }

    void 'list returns error for unknown facility id'() {
        expect:
        productAvailabilityApiWrapper.listExpectingStatus(INVALID_ID, HttpStatus.SC_NOT_FOUND)
    }
}
