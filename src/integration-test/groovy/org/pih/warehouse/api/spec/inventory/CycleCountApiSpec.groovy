package org.pih.warehouse.api.spec.inventory

import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.client.inventory.CycleCountApiWrapper
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.inventory.CycleCountRequest

class CycleCountApiSpec extends ApiSpec {

    private static final int QUANTITY_ON_HAND = 10

    @Autowired
    CycleCountApiWrapper cycleCountApiWrapper

    void setupData() {
        setStock(product, null, null, QUANTITY_ON_HAND)
    }

    void cleanupData() {
        CycleCountRequest.createCriteria().list {
            // Compare ids because the API-created product is a transient instance in this session
            eq("facility.id", facility.id)
            eq("product.id", product.id)
        }*.delete()
    }

    void 'candidates can be listed for a product with stock'() {
        when:
        List<Map> candidates = cycleCountApiWrapper.listCandidatesOK(facility.id, [searchTerm: product.productCode])
        Map candidate = candidates.find { it.product.id == product.id }

        then:
        assert candidate != null
        assert candidate.quantityOnHand == QUANTITY_ON_HAND
    }

    void 'pending requests can be listed for a product requested for counting'() {
        given:
        cycleCountApiWrapper.createRequestsOK(facility.id, [product])

        when:
        List<Map> pendingRequests = cycleCountApiWrapper.listPendingRequestsOK(facility.id, [searchTerm: product.productCode])
        Map pendingRequest = pendingRequests.find { it.product.id == product.id }

        then:
        assert pendingRequest != null
        assert pendingRequest.quantityOnHand == QUANTITY_ON_HAND
    }
}
