package org.pih.warehouse.api.regression.platform.client.stockmovement

import groovy.transform.InheritConstructors
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.AuthenticatedApi

/**
 * The stock movement API (/api/stockMovements...): one method per operation.
 */
@TestComponent
@InheritConstructors
class StockMovementApi extends AuthenticatedApi {

    /** A request with an optional id path parameter, JSON body, query parameters and accepted media type. */
    private Response call(Method method, String path, String id, String body, Map<String, ?> query,
                          ResponseSpecification responseSpec, String accept = null) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
        if (accept != null) {
            builder.setAccept(accept)
        }
        if (id != null) {
            builder.addPathParam('id', id)
        }
        if (body != null) {
            builder.setBody(body)
        }
        query?.each { String name, value -> builder.addQueryParam(name, value) }
        RequestSpecification requestSpec = builder.build()
        return request(requestSpec, responseSpec, method, path)
    }

    Response list(Map<String, ?> query, ResponseSpecification responseSpec) {
        // format=csv answers a CSV download
        return call(Method.GET, '/stockMovements', null, null, query, responseSpec,
                query?.format == 'csv' ? 'text/csv' : null)
    }

    Response create(String body, ResponseSpecification responseSpec) {
        return call(Method.POST, '/stockMovements', null, body, null, responseSpec)
    }

    Response createCombinedShipments(String body, ResponseSpecification responseSpec) {
        return call(Method.POST, '/stockMovements/createCombinedShipments', null, body, null, responseSpec)
    }

    Response read(String id, Map<String, ?> query, ResponseSpecification responseSpec) {
        return call(Method.GET, '/stockMovements/{id}', id, null, query, responseSpec)
    }

    Response delete(String id, ResponseSpecification responseSpec) {
        return call(Method.DELETE, '/stockMovements/{id}', id, null, null, responseSpec)
    }

    Response status(String id, ResponseSpecification responseSpec) {
        return call(Method.GET, '/stockMovements/{id}/status', id, null, null, responseSpec)
    }

    Response updateStatus(String id, String body, ResponseSpecification responseSpec) {
        return call(Method.POST, '/stockMovements/{id}/status', id, body, null, responseSpec)
    }

    Response deleteStatus(String id, ResponseSpecification responseSpec) {
        return call(Method.DELETE, '/stockMovements/{id}/status', id, null, null, responseSpec)
    }

    Response updateRequisition(String id, String body, ResponseSpecification responseSpec) {
        return call(Method.POST, '/stockMovements/{id}/updateRequisition', id, body, null, responseSpec)
    }

    Response updateShipment(String id, String body, ResponseSpecification responseSpec) {
        return call(Method.POST, '/stockMovements/{id}/updateShipment', id, body, null, responseSpec)
    }

    Response updateItems(String id, String body, ResponseSpecification responseSpec) {
        return call(Method.POST, '/stockMovements/{id}/updateItems', id, body, null, responseSpec)
    }

    Response reviseItems(String id, String body, ResponseSpecification responseSpec) {
        return call(Method.POST, '/stockMovements/{id}/reviseItems', id, body, null, responseSpec)
    }

    Response updateInventoryItems(String id, String body, ResponseSpecification responseSpec) {
        return call(Method.POST, '/stockMovements/{id}/updateInventoryItems', id, body, null, responseSpec)
    }

    Response updateShipmentItems(String id, String body, ResponseSpecification responseSpec) {
        return call(Method.POST, '/stockMovements/{id}/updateShipmentItems', id, body, null, responseSpec)
    }

    Response updateAdjustedItems(String id, Map<String, ?> query, ResponseSpecification responseSpec) {
        return call(Method.POST, '/stockMovements/{id}/updateAdjustedItems', id, null, query, responseSpec)
    }

    Response removeAllItems(String id, ResponseSpecification responseSpec) {
        return call(Method.DELETE, '/stockMovements/{id}/removeAllItems', id, null, null, responseSpec)
    }

    Response rollbackApproval(String id, ResponseSpecification responseSpec) {
        return call(Method.PUT, '/stockMovements/{id}/rollbackApproval', id, null, null, responseSpec)
    }

    Response createPickList(String id, ResponseSpecification responseSpec) {
        return call(Method.GET, '/stockMovements/createPickList/{id}', id, null, null, responseSpec)
    }

    Response validatePicklist(String id, ResponseSpecification responseSpec) {
        return call(Method.GET, '/stockMovements/{id}/validatePicklist', id, null, null, responseSpec)
    }

    Response exportItemsCsv(String id, ResponseSpecification responseSpec) {
        return call(Method.GET, '/stockMovements/{id}/itemsCsv', id, null, null, responseSpec, 'text/csv')
    }

    /** Uploads a line items CSV as the multipart field "importFile". */
    Response updateItemsFromCsv(String id, String csv, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .setContentType('multipart/form-data')
                .addPathParam('id', id)
                .addMultiPart('importFile', 'items.csv', csv.getBytes('UTF-8'), 'text/csv')
                .build()
        return request(requestSpec, responseSpec, Method.POST, '/stockMovements/{id}/itemsCsv')
    }

    Response getDocuments(String id, ResponseSpecification responseSpec) {
        return call(Method.GET, '/stockMovements/{id}/documents', id, null, null, responseSpec)
    }

    /** Uploads files (name -> content) to the stock movement's documents, one multipart "files" part each. */
    Response uploadDocuments(String id, Map<String, byte[]> files, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setContentType('multipart/form-data')
                .addPathParam('id', id)
        files.each { String name, byte[] content ->
            builder.addMultiPart('files', name, content, name.endsWith('.pdf') ? 'application/pdf' : 'application/octet-stream')
        }
        return request(builder.build(), responseSpec, Method.POST, '/stockMovements/{id}/documents')
    }

    /** A multipart upload with only a form field: no files. */
    Response uploadNoDocuments(String id, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .setContentType('multipart/form-data')
                .addPathParam('id', id)
                .addMultiPart('comment', 'no files')
                .build()
        return request(requestSpec, responseSpec, Method.POST, '/stockMovements/{id}/documents')
    }

    Response getPendingRequisitionDetails(Map<String, ?> query, ResponseSpecification responseSpec) {
        return call(Method.GET, '/stockMovements/pendingRequisitionDetails', null, null, query, responseSpec)
    }

    Response pendingRequisitionItems(Map<String, ?> query, ResponseSpecification responseSpec) {
        return call(Method.GET, '/stockMovements/pendingRequisitionItems', null, null, query, responseSpec, 'text/csv')
    }

    Response shippedItems(Map<String, ?> query, ResponseSpecification responseSpec) {
        return call(Method.GET, '/stockMovements/shippedItems', null, null, query, responseSpec, 'text/csv')
    }

    Response requisitionStatusCodes(Map<String, ?> query, ResponseSpecification responseSpec) {
        return call(Method.GET, '/stockMovements/requisitionsStatusCodes', null, null, query, responseSpec)
    }

    Response downloadPackingListTemplate(ResponseSpecification responseSpec) {
        return call(Method.GET, '/stockMovements/packingList/template', null, null, null, responseSpec,
                'application/vnd.ms-excel')
    }

    Response getStockMovementItems(String id, Map<String, ?> query, ResponseSpecification responseSpec) {
        return call(Method.GET, '/stockMovements/{id}/stockMovementItems', id, null, query, responseSpec)
    }

    Response getSubstitutionItems(String id, ResponseSpecification responseSpec) {
        return call(Method.GET, '/stockMovements/{id}/substitutionItems', id, null, null, responseSpec)
    }
}
