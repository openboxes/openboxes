package org.pih.warehouse.api.regression.platform.spec.stockmovement

import spock.lang.PendingFeature



import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.path.json.JsonPath
import org.apache.http.HttpStatus
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.hamcrest.Matchers

import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAssociation
import org.pih.warehouse.product.ProductAssociationTypeCode
import org.pih.warehouse.requisition.RequisitionItem

/**
 * The line items of a stock movement: reading them, picking them by hand, revising, cancelling, substituting and
 * removing them.
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation(tolerate = [
        'known html PUT /chooseLocation/.*',
        'known response GET /stockMovements',
])
class StockMovementItemApiSpec extends StockMovementSpec {

    private BigDecimal quantityPicked(String itemId) {
        return RequisitionItem.withNewSession { RequisitionItem.get(itemId).calculateQuantityPicked() as BigDecimal }
    }

    void 'the items of a stock movement list and read back, with details on request'() {
        given:
        String id = requested([(product): 5])
        String itemId = firstItemId(id)

        when:
        JsonPath list = stockMovementItemApi.list(['stockMovement.id': id], responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
        JsonPath item = stockMovementItemApi.read(itemId, responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
        JsonPath details = stockMovementItemApi.details(itemId, responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
        JsonPath items = stockMovementApiWrapper.api.getStockMovementItems(id, [stepNumber: 2],
                responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()

        then:
        list.getList('data.id') == [itemId]
        item.getString('data.product.id') == product.id
        item.getInt('data.quantityRequested') == 5
        details.getString('data.id') == itemId
        details.getString('data.product.id') == product.id
        items.getList('data.id') == [itemId]
    }

    void 'listing the items of a stock movement that does not exist answers 404'() {
        expect:
        stockMovementItemApi.list(['stockMovement.id': INVALID_ID], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_NOT_FOUND)
                .expectBody('errorCode', Matchers.equalTo(404))
                .build())
    }

    void 'an item can be picked automatically, cleared, picked by hand, and have its pick reverted'() {
        given:
        stockUp(20)
        String id = requested([(product): 5])
        stockMovementApiWrapper.updateStatusOK(id, 'VALIDATED')
        String itemId = firstItemId(id)
        String inventoryItemId = InventoryItem.withNewSession {
            InventoryItem.findByProductAndLotNumber(Product.get(product.id), 'SM-LOT-1').id
        }

        when: 'picked automatically'
        stockMovementItemApi.createPicklist(itemId, responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        quantityPicked(itemId) == 5

        when: 'the picklist is cleared'
        stockMovementItemApi.clearPicklist(itemId, responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        quantityPicked(itemId) == 0

        when: 'picked by hand, fewer than requested'
        JSONArray picklistItems = new JSONArray()
        picklistItems.add(new JSONObject()
                .put('inventoryItem', new JSONObject().put('id', inventoryItemId))
                .put('binLocation', new JSONObject().put('id', ''))
                // OBAF: StockMovementService.updatePicklistItem refuses a row picked above its quantityAllocated
                // (the allocation engine's safeguard), so a hand pick names the quantity it allocates.
                .put('quantityAllocated', 3)
                .put('quantityPicked', 3))
        stockMovementItemApi.updatePicklist(itemId, new JSONObject()
                .put('picklistItems', picklistItems).put('reasonCode', 'STOCKOUT').toString(),
                responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        quantityPicked(itemId) == 3

        when: 'the pick is reverted'
        stockMovementItemApi.revertPick(itemId, responseSpecUtil.NO_CONTENT_RESPONSE_SPEC)

        then:
        quantityPicked(itemId) == 0
    }

    @PendingFeature(reason = 'updatePicklist without picklistItems answers 500, not 400')
    void 'picking by hand without picklist items is refused with 400'() {
        given:
        String id = requested([(product): 5])
        stockMovementApiWrapper.updateStatusOK(id, 'VALIDATED')

        expect:
        stockMovementItemApi.updatePicklist(firstItemId(id), new JSONObject().put('reasonCode', 'STOCKOUT').toString(),
                new ResponseSpecBuilder().expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                        .expectBody('errorCode', Matchers.equalTo(400))
                        .expectBody('errorMessage', Matchers.containsString('picklistItems')).build())
    }

    void 'items can be revised, and the revision reverted'() {
        given:
        stockUp(20)
        String id = requested([(product): 5])
        stockMovementApiWrapper.updateStatusOK(id, 'VALIDATED')
        String itemId = firstItemId(id)
        JSONArray lineItems = new JSONArray()
        lineItems.add(new JSONObject().put('id', itemId).put('quantityRevised', 2).put('reasonCode', 'STOCKOUT'))

        when:
        stockMovementApiWrapper.api.reviseItems(id, new JSONObject().put('lineItems', lineItems).toString(),
                responseSpecUtil.OK_RESPONSE_SPEC)

        then: 'the revision is kept as a modification of the requested item'
        RequisitionItem.withNewSession { RequisitionItem.get(itemId).modificationItem?.quantity } == 2

        when:
        stockMovementItemApi.revertItem(itemId, responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        RequisitionItem.withNewSession { RequisitionItem.get(itemId).modificationItem } == null
    }

    @PendingFeature(reason = 'cancelling an item fails validation (400) unless a reason code was already stored')
    void 'an item can be cancelled'() {
        given:
        String id = requested([(product): 5])
        stockMovementApiWrapper.updateStatusOK(id, 'VALIDATED')
        String itemId = firstItemId(id)

        when:
        JsonPath cancelled = stockMovementItemApi.cancelItem(itemId,
                new JSONObject().put('reasonCode', 'STOCKOUT').put('comments', 'None left').toString(),
                responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()

        then:
        cancelled.getString('data.id') == itemId
        RequisitionItem.withNewSession { RequisitionItem.get(itemId).quantityCanceled } == 5
        RequisitionItem.withNewSession { RequisitionItem.get(itemId).cancelReasonCode } == 'STOCKOUT'
        RequisitionItem.withNewSession { RequisitionItem.get(itemId).cancelComments } == 'None left'
        RequisitionItem.withNewSession { RequisitionItem.get(itemId).quantityApproved } == 0
    }

    void 'an item can be substituted by a product that is set up as its substitute'() {
        given: 'a product of its own (so the shared product gets no substitutes) with a substitute in stock'
        Product original = Product.withNewTransaction { newProduct() }
        Product substitute = Product.withNewTransaction {
            Product substituteProduct = newProduct()
            new ProductAssociation(product: Product.get(original.id), code: ProductAssociationTypeCode.SUBSTITUTE,
                    associatedProduct: substituteProduct, quantity: 1).save(flush: true, failOnError: true)
            return substituteProduct
        }
        stockUp(20, substitute, 'SUB-LOT-1')
        String id = requested([(original): 5])
        stockMovementApiWrapper.updateStatusOK(id, 'VALIDATED')
        String itemId = firstItemId(id)

        when:
        List<String> offered = stockMovementApiWrapper.api.getSubstitutionItems(itemId, responseSpecUtil.OK_RESPONSE_SPEC)
                .jsonPath().getList('data.productId')

        then:
        offered == [substitute.id]

        when:
        JSONArray substitutionItems = new JSONArray()
        substitutionItems.add(new JSONObject()
                .put('newProduct', new JSONObject().put('id', substitute.id))
                .put('newQuantity', 3)
                .put('reasonCode', 'SUBSTITUTION'))
        stockMovementItemApi.substituteItem(itemId, new JSONObject()
                .put('substitutionItems', substitutionItems).put('reasonCode', 'SUBSTITUTION').toString(),
                responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        RequisitionItem.withNewSession {
            RequisitionItem.get(itemId).substitutionItems.collect { [it.product.id, it.quantity] }
        } == [[substitute.id, 3]]
    }

    void 'an item can be removed'() {
        given:
        String id = requested([(product): 5, (Product.withNewTransaction { newProduct() }): 2])
        String itemId = firstItemId(id)

        when:
        stockMovementItemApi.eraseItem(itemId, responseSpecUtil.NO_CONTENT_RESPONSE_SPEC)

        then:
        !stockMovementApiWrapper.api.getStockMovementItems(id, [:], responseSpecUtil.OK_RESPONSE_SPEC)
                .jsonPath().getList('data.id').contains(itemId)
        RequisitionItem.withNewSession { RequisitionItem.get(itemId) } == null
    }

    void 'the picklist of an adjusted product is recreated'() {
        given:
        stockUp(20)
        String id = picking([(product): 5])
        String itemId = firstItemId(id)

        when:
        JsonPath adjusted = stockMovementApiWrapper.api.updateAdjustedItems(id, [adjustedProduct: product.productCode],
                responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()

        then:
        adjusted.getString('data.id') == id
        quantityPicked(itemId) == 5
    }

    void 'inventory items are created for new lots of the items'() {
        given:
        String id = requested([(product): 5])
        JSONArray lineItems = new JSONArray()
        lineItems.add(new JSONObject()
                .put('product', jsonObjectUtil.asIdForRequestBody(product))
                .put('quantityRequested', 5)
                .put('lotNumber', 'NEW-LOT-9')
                .put('expirationDate', '12/31/2030'))

        when:
        stockMovementApiWrapper.api.updateInventoryItems(id, new JSONObject().put('lineItems', lineItems).toString(),
                responseSpecUtil.OK_RESPONSE_SPEC)

        then: 'the lot exists with its expiration date (within a day: the server and the test may differ in time zone)'
        Date expiration = InventoryItem.withNewSession {
            InventoryItem.findByProductAndLotNumber(Product.get(product.id), 'NEW-LOT-9')?.expirationDate
        }
        expiration != null
        Math.abs(expiration.time - Date.parse('yyyy-MM-dd', '2030-12-31').time) <= 24 * 60 * 60 * 1000
    }

    void 'an inbound shipment for combined shipments is created from the posted details'() {
        given:
        String body = new JSONObject()
                .put('name', '')
                .put('description', 'Combined inbound')
                .put('origin', jsonObjectUtil.asIdForRequestBody(destination))
                .put('destination', jsonObjectUtil.asIdForRequestBody(facility))
                .put('requestedBy', jsonObjectUtil.asIdForRequestBody(requestor))
                .put('dateRequested', new Date().format('MM/dd/yyyy'))
                .toString()

        when:
        JsonPath created = stockMovementApiWrapper.api.createCombinedShipments(body, responseSpecUtil.CREATED_RESPONSE_SPEC)
                .jsonPath()

        then:
        created.getString('data.id')
        created.getString('data.description') == 'Combined inbound'
        created.getString('data.origin.id') == destination.id
        created.getString('data.destination.id') == facility.id
    }
}
