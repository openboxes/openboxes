package org.pih.warehouse.api.spec.core

import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.client.core.LocationApiWrapper
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.api.util.ResponseSpecUtil

import static io.restassured.RestAssured.given

class LocationApiAvailableItemsSpec extends ApiSpec {

    @Autowired
    LocationApiWrapper locationApiWrapper

    @Autowired
    ResponseSpecUtil responseSpecUtil

    void 'availableItems rejects unauthenticated requests'() {
        expect:
        given(unauthenticatedApiContext.baseRequestSpec)
                .pathParam("id", facility.id)
                .when()
                    .get("/locations/{id}/availableItems")
                .then()
                    .spec(responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_UNAUTHORIZED))
    }

    void 'availableItems returns stock for the location with required fields'() {
        given:
        setStock(product, null, null, 10)

        when:
        def response = locationApiWrapper.getAvailableItemsOK(facility.id)
        List data = response.jsonPath().getList("data")

        then:
        data.size() >= 1
        def row = data.find { it.productCode == product.productCode }
        row != null
        row.location.id == facility.id
        row.location.name == facility.name
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

    void 'availableItems excludes zero quantity on hand rows'() {
        given:
        setStock(product, null, null, 0)

        when:
        def response = locationApiWrapper.getAvailableItemsOK(facility.id)
        List data = response.jsonPath().getList("data")

        then:
        !data.any { it.productCode == product.productCode && it.quantityOnHand == 0 }
    }

    void 'availableItems returns empty data when location has no matching stock'() {
        // facility/product start with no stock after ApiSpec setup deletes transactions
        when:
        def response = locationApiWrapper.getAvailableItemsOK(facility.id)
        List data = response.jsonPath().getList("data")

        then:
        data.findAll { it.productCode == product.productCode }.isEmpty()
    }

    void 'availableItems returns error for unknown location id'() {
        expect:
        locationApiWrapper.getAvailableItemsExpectingStatus(INVALID_ID, HttpStatus.SC_NOT_FOUND)
    }

    void 'exportAvailableItems returns JSON for the location'() {
        given:
        setStock(product, null, null, 7)

        when:
        def response = locationApiWrapper.exportAvailableItemsOK(facility.id)
        List data = response.jsonPath().getList("data")

        then:
        def row = data.find { it.productCode == product.productCode }
        row != null
        row.location.id == facility.id
        row.quantityOnHand == 7
        !row.containsKey("zones")
    }

    void 'exportAvailableItems returns error for unknown location id'() {
        expect:
        locationApiWrapper.exportAvailableItemsExpectingStatus(INVALID_ID, HttpStatus.SC_NOT_FOUND)
    }
}
