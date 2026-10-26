package org.pih.warehouse.api.regression.platform.client.inventory

import org.pih.warehouse.api.client.inventory.*
import groovy.transform.InheritConstructors
import io.restassured.builder.RequestSpecBuilder
import io.restassured.config.EncoderConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.ResponseSpecification
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.AuthenticatedApi

/**
 * The facility inventory API: inventory levels (/inventory-levels...), the reorder report and the inventory import
 * (/inventories...).
 */
@TestComponent
@InheritConstructors
class InventoryLevelApi extends AuthenticatedApi {

    /** @param format null for JSON, or "csv"/"xls" for a download */
    Response list(String facilityId, String format, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder().addPathParam('facilityId', facilityId)
        if (format) {
            builder.addQueryParam('format', format)
        }
        if (format == 'csv') {
            builder.setAccept('*/*')
        }
        return request(builder.build(), responseSpec, Method.GET, '/facilities/{facilityId}/inventory-levels')
    }

    Response preferredBinLocations(String facilityId, Map<String, ?> query, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder().addPathParam('facilityId', facilityId)
        query?.each { String name, value -> builder.addQueryParam(name, value) }
        return request(builder.build(), responseSpec, Method.GET,
                '/facilities/{facilityId}/inventory-levels/preferred-bin-locations')
    }

    Response reorderReport(String facilityId, Map<String, ?> query, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder().addPathParam('facilityId', facilityId)
        query?.each { String name, value -> builder.addQueryParam(name, value) }
        if (query?.format == 'csv') {
            builder.setAccept('*/*')
        }
        return request(builder.build(), responseSpec, Method.GET, '/facilities/{facilityId}/inventories/reorderReport')
    }

    /** Imports an inventory CSV sent as the raw request body (as the inventory import page does). */
    Response importInventory(String facilityId, String csv, String contentType, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
                .addPathParam('facilityId', facilityId)
                .setContentType(contentType)
                .setBody(csv)
                // OBAF: InventoryApiController.importCsv compares the content type exactly ("text/csv"); RestAssured
                // appends "; charset=ISO-8859-1" unless told not to.
                .setConfig(RestAssuredConfig.config().encoderConfig(EncoderConfig.encoderConfig()
                        .appendDefaultContentCharsetToContentTypeIfUndefined(false)))
        return request(builder.build(), responseSpec, Method.POST, '/facilities/{facilityId}/inventories/import')
    }

    /** The same list through the resource route: GET /api/inventoryLevels?facilityId=... */
    Response listByQuery(Map<String, ?> query, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
        query?.each { String name, value -> builder.addQueryParam(name, value) }
        return request(builder.build(), responseSpec, Method.GET, '/inventoryLevels')
    }
}
