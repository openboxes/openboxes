package org.pih.warehouse.api.spec.inventory

import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.client.inventory.CycleCountApiWrapper
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.CycleCountRequest
import org.pih.warehouse.inventory.CycleCountRequestStatus
import org.pih.warehouse.inventory.CycleCountRequestType
import org.pih.warehouse.product.Product

class CycleCountApiSpec extends ApiSpec {

    private static final int QUANTITY_ON_HAND = 10

    @Autowired
    CycleCountApiWrapper cycleCountApiWrapper

    @Autowired
    AuthService authService

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

    void 'pending requests list every open request for a product, each with its own id'() {
        given: 'two open requests for the same product, the second saved directly since the API allows only one'
        cycleCountApiWrapper.createRequestsOK(facility.id, [product])
        CycleCountRequest.withNewTransaction {
            Location requestFacility = Location.load(facility.id)
            Product requestProduct = Product.load(product.id)
            // beforeInsert stamps createdBy and updatedBy from the current user (updated_by_id is required), so flush while it is set
            authService.currentUser = CycleCountRequest.findByFacilityAndProduct(requestFacility, requestProduct).createdBy
            try {
                new CycleCountRequest(
                        facility: requestFacility,
                        product: requestProduct,
                        status: CycleCountRequestStatus.CREATED,
                        requestType: CycleCountRequestType.MANUAL_REQUEST,
                        blindCount: false,
                ).save(failOnError: true, flush: true)
            } finally {
                authService.currentUser = null
            }
        }

        when:
        List<Map> pendingRequests = cycleCountApiWrapper.listPendingRequestsOK(facility.id, [searchTerm: product.productCode])
                .findAll { it.product.id == product.id }

        then:
        assert pendingRequests.size() == 2
        assert pendingRequests*.id.toSet().size() == 2
    }
}
