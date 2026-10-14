package org.pih.warehouse.api.regression.platform.spec.stockmovement

import spock.lang.PendingFeature




import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.path.json.JsonPath
import io.restassured.response.Response
import io.restassured.specification.ResponseSpecification
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.hamcrest.Matchers

import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionStatus

import static io.restassured.RestAssured.given

/**
 * The outbound stock movement wizard's first three steps (create, add items, edit) as the web app calls them, and the
 * line items CSV it exports and imports on the add items step.
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation(tolerate = [
        'known html PUT /chooseLocation/.*',
        'known response GET /stockMovements',
])
class OutboundWizardApiSpec extends StockMovementSpec {

    private static final String CSV_HEADER = 'Requisition item id,Product code (required),Product name,Pack level 1,' +
            'Pack level 2,Lot number,Expiration date (MM/dd/yyyy),Quantity (required),Recipient id'

    private ResponseSpecification error(int status, String message) {
        return new ResponseSpecBuilder()
                .expectStatusCode(status)
                .expectBody('errorCode', Matchers.equalTo(status))
                .expectBody('errorMessage', Matchers.containsString(message))
                .build()
    }

    /** The requested quantities of a stock movement's items, by product code. */
    private Map<String, Integer> requested(String stockMovementId) {
        return Requisition.withNewSession {
            Requisition.get(stockMovementId).requisitionItems.collectEntries {
                [(it.product.productCode): it.quantity]
            }
        }
    }

    void 'a movement is created, gets its items, and has them revised, step by step as the wizard does'() {
        given:
        stockUp(20)
        Product other = Product.withNewTransaction { newProduct() }

        when: 'create'
        String id = stockMovementApiWrapper.createOutboundOK(facility, destination, requestor, 'Wizard run')
        JsonPath created = stockMovementApiWrapper.api.read(id, [:], responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()

        then:
        created.getString('data.statusCode') == 'CREATED'
        created.getString('data.origin.id') == facility.id

        when: 'add items: the first save, then an autosave that adds a line'
        stockMovementApiWrapper.addItemsOK(id, [(product): 5])
        JSONArray added = new JSONArray()
        added.add(new JSONObject()
                .put('product', new JSONObject().put('id', other.id))
                .put('quantityRequested', 3)
                .put('sortOrder', 200))
        stockMovementApiWrapper.api.updateItems(id, new JSONObject().put('id', id).put('lineItems', added).toString(),
                responseSpecUtil.OK_RESPONSE_SPEC)
        JsonPath stepTwo = stockMovementApiWrapper.api.getStockMovementItems(id, [stepNumber: '2'],
                responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()

        then:
        stepTwo.getList('data.product.id') as Set == [product.id, other.id] as Set

        when: 'on to the edit step'
        stockMovementApiWrapper.updateStatusOK(id, 'REQUESTED')
        JsonPath stepThree = stockMovementApiWrapper.api.getStockMovementItems(id, [stepNumber: '3'],
                responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
        String itemId = stepThree.getString("data.find { it.product.id == '${product.id}' }.requisitionItemId")

        then:
        stepThree.getList('data').size() == 2
        itemId

        when: 'edit: revise a quantity with a reason'
        JSONArray revised = new JSONArray()
        revised.add(new JSONObject().put('id', itemId).put('quantityRevised', 2).put('reasonCode', 'STOCKOUT'))
        stockMovementApiWrapper.api.reviseItems(id, new JSONObject().put('lineItems', revised).toString(),
                responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        RequisitionItem.withNewSession { RequisitionItem.get(itemId).modificationItem?.quantity } == 2
        stockMovementApiWrapper.api.read(id, [:], responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
                .getString('data.statusCode') == 'REQUESTED'
    }

    /** The server page import (/stockMovement/importCsv/{id}, the inbound and request wizards), with the session. */
    private Response serverPageImport(String id, String csv) {
        return given().baseUri('http://localhost').port(serverPort).basePath('/openboxes').cookie(cookie)
                .accept('application/json').redirects().follow(false)
                .multiPart('importFile', 'items.csv', csv.getBytes('UTF-8'), 'text/csv')
                .post("/stockMovement/importCsv/${id}")
    }

    @PendingFeature(reason = 'importing items into a canceled movement through the server page is accepted (200), not refused with 409')
    void 'the server page import reads the same CSV through the same rules'() {
        given:
        String id = requested([(product): 5])
        String itemId = firstItemId(id)

        when:
        Response imported = serverPageImport(id, "${CSV_HEADER}\n${itemId},${product.productCode},,,,,,9,")

        then:
        imported.statusCode == 200
        requested(id) == [(product.productCode): 9]

        when: 'the movement is canceled'
        Requisition.withNewTransaction {
            Requisition requisition = Requisition.get(id)
            requisition.status = RequisitionStatus.CANCELED
            requisition.save(flush: true, failOnError: true)
        }
        Response refused = serverPageImport(id, "${CSV_HEADER}\n${itemId},${product.productCode},,,,,,4,")

        then:
        refused.statusCode == 409
        refused.jsonPath().getString('errorMessage').contains('can no longer change')
        requested(id) == [(product.productCode): 9]
    }

}
