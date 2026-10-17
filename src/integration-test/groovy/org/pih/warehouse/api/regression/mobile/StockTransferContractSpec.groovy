package org.pih.warehouse.api.regression.mobile

import spock.lang.PendingFeature

import org.pih.warehouse.core.Location
import org.pih.warehouse.order.Order
import org.pih.warehouse.product.Product

/**
 * Stock transfers (Create Transfer flow, Transfers list/detail, legacy Transfer
 * screen). Each feature makes its own product and two bins: completing a
 * transfer moves stock for good, and a pending transfer hides its candidate row.
 */
class StockTransferContractSpec extends MobileContractSpec {

    Product p
    Location binA
    Location binB

    void setupData() {
        p = newProduct('ST')
        binA = newBin('ST-A')
        binB = newBin('ST-B')
        stock(p, [[binA, 10]])
    }

    Map candidateRow() {
        CallResult c = call('src/apis/createTransfer.ts#getStockTransferCandidates', [facilityId: facility.id])
        Map row = (c.data.data as List<Map>).find { it['product.id'] == p.id && it['originBinLocation.id'] == binA.id }
        assert row: "no candidate row for ${p.productCode} in ${binA.name}"
        return row
    }

    /** The Create Transfer flow's body (CreateTransferDestinationScreen), PENDING. */
    Map pendingBody(Map row, int qty = 4) {
        return [stockTransferItems: [[
                id                    : null,
                productAvailabilityId : row.productAvailabilityId,
                product               : [id: row['product.id']],
                inventoryItem         : [id: row['inventoryItem.id']],
                originBinLocation     : [id: binA.id],
                destinationBinLocation: [id: binB.id],
                quantity              : qty,
                quantityOnHand        : row.quantityOnHand,
                quantityNotPicked     : row.quantityNotPicked,
                status                : 'PENDING',
                splitItems            : [],
                sortOrder             : 0]]]
    }

    String createPending() {
        CallResult r = call('src/apis/createTransfer.ts#postStockTransfer', [:], pendingBody(candidateRow()))
        r.holds()
        return r.data.data.id
    }

    /** The app's completion body: GET /stockTransfers/{id}.data through parseResponse, plus status. */
    Map completionBody(String id) {
        CallResult get = call('src/apis/createTransfer.ts#getStockTransferById', [id: id])
        get.holds()
        return (ContractCheck.expandFlatKeys(get.data.data) as Map) + [status: 'COMPLETED']
    }

    long ordersNumbered(String number) {
        return Order.withNewTransaction { Order.countByOrderNumber(number) } as long
    }

    @Covers('src/apis/createTransfer.ts#getStockTransferCandidates')
    void 'GET /stockTransfers/candidates lists the stocked bin with the fields the item list reads'() {
        when:
        CallResult r = call('src/apis/createTransfer.ts#getStockTransferCandidates', [facilityId: facility.id])

        then:
        r.rows { it['product.id'] == p.id }.holds()
        (r.data.data as List<Map>).any { it['product.id'] == p.id && it['originBinLocation.id'] == binA.id && it.quantityOnHand == 10 }
    }

    @Covers('src/apis/createTransfer.ts#postStockTransfer')
    void 'POST /stockTransfers with the Create Transfer body makes a PENDING transfer'() {
        when:
        CallResult r = call('src/apis/createTransfer.ts#postStockTransfer', [:], pendingBody(candidateRow()))

        then:
        r.holds()
        r.data.data.status == 'PENDING'
    }

    @Covers(['src/apis/createTransfer.ts#getStockTransferById', 'src/apis/transfers.ts#fetchStockTransferSummary'])
    void 'GET /stockTransfers/{id} answers the transfer the detail screens read'() {
        given:
        String id = createPending()

        when:
        CallResult byId = call('src/apis/createTransfer.ts#getStockTransferById', [id: id])
        CallResult summary = call('src/apis/transfers.ts#fetchStockTransferSummary', [id: id])

        then:
        byId.holds()
        summary.holds()
        summary.data.data.id == id
        summary.data.data.stockTransferItems.size() == 1
    }

    @Covers('src/apis/createTransfer.ts#putStockTransfer')
    void 'PUT /stockTransfers/{id} with the parsed GET body completes the transfer in place'() {
        given:
        String id = createPending()
        Map body = completionBody(id)
        String number = body.stockTransferNumber

        when:
        CallResult r = call('src/apis/createTransfer.ts#putStockTransfer', [id: id], body)
        CallResult after = call('src/apis/transfers.ts#fetchStockTransferSummary', [id: id])

        then:
        r.holds()
        after.data.data.status == 'COMPLETED'
        ordersNumbered(number) == 1
    }

    @Covers('src/apis/transfers.ts#completeStockTransfer')
    void 'POST /stockTransfers/{id} with the parsed summary body completes the transfer in place'() {
        given:
        String id = createPending()
        Map body = completionBody(id) + [id: id]
        String number = body.stockTransferNumber

        when:
        CallResult r = call('src/apis/transfers.ts#completeStockTransfer', ['data.id': id], body)
        CallResult after = call('src/apis/transfers.ts#fetchStockTransferSummary', [id: id])

        then:
        r.holds()
        r.data.data.id == id
        after.data.data.status == 'COMPLETED'
        ordersNumbered(number) == 1
    }

    @Covers('src/apis/transfers.ts#getStockTransfers')
    void 'GET /stockTransfers/?location= lists the facility transfers with the list row fields'() {
        given:
        String id = createPending()

        when:
        CallResult r = call('src/apis/transfers.ts#getStockTransfers', [data: [params: [location: facility.id]]])

        then:
        r.rows { it.id == id }.holds()
        (r.data.data as List<Map>).any { it.id == id }
    }

    @Covers('src/apis/transfers.ts#stockTransfers')
    void 'POST /stockTransfers through the undispatched stockTransfers action accepts a pending body'() {
        when:
        CallResult r = call('src/apis/transfers.ts#stockTransfers', [:], pendingBody(candidateRow(), 2))

        then:
        r.holds()
    }

    @Covers('src/apis/transfer.ts#updateStockTransfers')
    @PendingFeature(reason = 'APP/SERVER MISMATCH: src/apis/transfer.ts updateStockTransfers (legacy Transfer screen, src/screens/Transfer/index.tsx) POSTs a new transfer with status COMPLETED; OBAF StockTransferApiController.create now calls StockTransferService.completeStockTransfer, which publishes StockTransferEvent(stockTransfer.id) while the new transfer id is still null: IllegalArgumentException "null source", the transaction rolls back, 500')
    void 'POST /stockTransfers with the legacy COMPLETED body moves the stock'() {
        given:
        Map row = candidateRow()
        Map body = [
                status             : 'COMPLETED',
                stockTransferNumber: '',
                description        : '',
                'origin.id'        : facility.id,
                'destination.id'   : facility.id,
                stockTransferItems : [[
                        product               : [id: p.id],
                        inventoryItem         : [id: row['inventoryItem.id']],
                        location              : [id: facility.id],
                        originBinLocation     : [id: binA.id],
                        destinationBinLocation: [id: binB.id],
                        quantity              : 3]]]

        when:
        CallResult r = call('src/apis/transfer.ts#updateStockTransfers', [:], body)

        then:
        r.holds()
    }
}
