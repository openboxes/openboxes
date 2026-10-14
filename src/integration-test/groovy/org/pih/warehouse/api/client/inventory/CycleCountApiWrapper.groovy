package org.pih.warehouse.api.client.inventory

import groovy.transform.InheritConstructors
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.ApiWrapper
import org.pih.warehouse.product.Product

@TestComponent
@InheritConstructors
class CycleCountApiWrapper extends ApiWrapper<CycleCountApi> {

    List<Map> listCandidatesOK(String facilityId, Map<String, ?> queryParams = [:]) {
        return api.listCandidates(facilityId, queryParams, responseSpecUtil.OK_RESPONSE_SPEC)
                .jsonPath()
                .getList("data", Map)
    }

    List<Map> listPendingRequestsOK(String facilityId, Map<String, ?> queryParams = [:]) {
        return api.listPendingRequests(facilityId, queryParams, responseSpecUtil.OK_RESPONSE_SPEC)
                .jsonPath()
                .getList("data", Map)
    }

    List<Map> createRequestsOK(String facilityId, List<Product> products) {
        JSONArray requests = new JSONArray()
        for (Product product in products) {
            requests.add(new JSONObject()
                    .put('product', jsonObjectUtil.asIdForRequestBody(product))
                    .put('blindCount', false))
        }
        String body = new JSONObject()
                .put('requests', requests)
                .toString()

        return api.createRequests(facilityId, body, responseSpecUtil.OK_RESPONSE_SPEC)
                .jsonPath()
                .getList("data", Map)
    }
}
