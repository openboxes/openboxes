package org.pih.warehouse.api.regression.platform.spec.stockmovement


import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification

import org.pih.warehouse.product.ProductAvailability

import static io.restassured.RestAssured.given

/**
 * Server rendered pages (session cookie) around stock movements whose controller code needs a real request or real
 * database queries: deleting a stock movement, printing its picklist and the product availability migration pages.
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation(tolerate = [
        'known html PUT /chooseLocation/.*',
        'known response GET /stockMovements',
])
class StockMovementServerPagesSpec extends StockMovementSpec {

    private RequestSpecification page() {
        return given().baseUri('http://localhost').port(serverPort).basePath('/openboxes').cookie(cookie)
                .redirects().follow(false)
    }

    void 'the picklist of a picked request prints as HTML'() {
        given:
        stockUp(10)
        String id = picking([(product): 4])

        when:
        Response response = page().get("/picklist/renderHtml/${id}")

        then:
        response.statusCode == 200
        response.asString().contains(product.productCode)
    }

    void 'the product availability migration page counts rows per depot'() {
        given:
        stockUp(7)
        int stored = ProductAvailability.withNewSession { ProductAvailability.countByLocation(facility) }

        when:
        Response list = page().get('/migration/productAvailability')
        Response count = page().get("/migration/loadProductAvailability/${facility.id}")

        then:
        list.statusCode == 200
        list.asString().contains(facility.name)
        list.asString().contains("/migration/loadProductAvailability/${facility.id}")
        count.statusCode == 200
        count.asString().trim() as Integer == stored
    }
}
