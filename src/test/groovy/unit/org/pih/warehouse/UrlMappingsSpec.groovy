package unit.org.pih.warehouse

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.pih.warehouse.UrlMappings
import org.pih.warehouse.api.CategoryApiController
import org.pih.warehouse.api.StockMovementItemApiController
import spock.lang.Specification

@TestFor(UrlMappings)
@Mock([CategoryApiController, StockMovementItemApiController])
class UrlMappingsSpec extends Specification {
    void "test forward mapping for GETs"() {
        when:
        request.method = 'GET'

        then:
        // CATEGORIES
        assertForwardUrlMapping("/api/categories", controller: 'categoryApi', action: "list")
        assertForwardUrlMapping("/api/categories/123", controller: 'categoryApi', action: "read")
        // STOCK MOVEMENT ITEMS
        assertForwardUrlMapping("/api/stockMovementItems", controller: 'stockMovementItemApi', action: "list")
        assertForwardUrlMapping("/api/stockMovementItems/123", controller: 'stockMovementItemApi', action: "read")
    }

    void "test forward mapping for POSTs"() {
        when:
        request.method = 'POST'

        then:
        // CATEGORIES
        assertForwardUrlMapping("/api/categories", controller: 'categoryApi', action: "save")
        // STOCK MOVEMENT ITEMS
        assertForwardUrlMapping("/api/stockMovementItems/123/updatePicklist", controller: 'stockMovementItemApi', action: "updatePicklist")
        assertForwardUrlMapping("/api/stockMovementItems/123/createPicklist", controller: 'stockMovementItemApi', action: "createPicklist")
        assertForwardUrlMapping("/api/stockMovementItems/123/clearPicklist", controller: 'stockMovementItemApi', action: "clearPicklist")
        assertForwardUrlMapping("/api/stockMovementItems/123/substituteItem", controller: 'stockMovementItemApi', action: "substituteItem")
        assertForwardUrlMapping("/api/stockMovementItems/123/revertItem", controller: 'stockMovementItemApi', action: "revertItem")
        assertForwardUrlMapping("/api/stockMovementItems/123/cancelItem", controller: 'stockMovementItemApi', action: "cancelItem")
    }

    void "test forward mapping for PUTs"() {
        when:
        request.method = 'PUT'

        then:
        // CATEGORIES
        assertForwardUrlMapping("/api/categories/123", controller: 'categoryApi', action: "save")
    }

    void "test forward mapping for DELETEs"() {
        when:
        request.method = 'DELETE'

        then:
        // CATEGORIES
        assertForwardUrlMapping("/api/categories/123", controller: 'categoryApi', action: "delete")
        // STOCK MOVEMENT ITEMS
        assertForwardUrlMapping("/api/stockMovementItems/123/removeItem", controller: 'stockMovementItemApi', action: "removeItem")
    }
}
