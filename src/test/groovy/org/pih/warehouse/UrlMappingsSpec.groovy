package org.pih.warehouse

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.web.mapping.UrlMapping
import org.grails.web.mapping.ResponseCodeUrlMapping
import org.pih.warehouse.UrlMappings
import org.pih.warehouse.api.CategoryApiController
import org.pih.warehouse.api.StockMovementItemApiController
import org.pih.warehouse.api.UserRoleApiController
import org.pih.warehouse.monitoring.QuietExceptionResolver
import spock.lang.Specification

@TestFor(UrlMappings)
@Mock([CategoryApiController, StockMovementItemApiController, UserRoleApiController])
class UrlMappingsSpec extends Specification {

    // The ErrorsController actions that respond with a 4xx status instead of a 500.
    static final List<String> FOUR_XX_ERROR_ACTIONS = ['handleNotFound', 'handleValidationErrors', 'handleUnauthorized']

    void "every quiet exception is mapped to an ErrorsController action that responds with a 4xx"() {
        given:
        Map<Class, String> exceptionActions = exceptionActions()

        expect:
        QuietExceptionResolver.QUIET_EXCEPTIONS.each { Class quietType ->
            String action = exceptionActions.find { Class mappedType, String a -> mappedType.isAssignableFrom(quietType) }?.value
            assert action in FOUR_XX_ERROR_ACTIONS,
                    "${quietType.simpleName} is quiet but UrlMappings maps it to ${action ?: 'no 4xx handler'}"
        }
    }

    void "every exception mapped to a 4xx ErrorsController action is quiet"() {
        expect:
        exceptionActions().each { Class mappedType, String action ->
            if (action in FOUR_XX_ERROR_ACTIONS) {
                assert mappedType in QuietExceptionResolver.QUIET_EXCEPTIONS,
                        "${mappedType.simpleName} is mapped to ${action} but QuietExceptionResolver still logs it as an error"
            }
        }
    }

    /**
     * The "500" mappings that are specific to an exception type, keyed by exception type, in declaration order
     * (which matters because the first matching mapping wins).
     */
    private Map<Class, String> exceptionActions() {
        Map<Class, String> actions = [:]
        urlMappingsHolder.urlMappings.each { UrlMapping mapping ->
            if (mapping instanceof ResponseCodeUrlMapping && mapping.exceptionType) {
                assert mapping.controllerName == 'errors'
                actions.putIfAbsent(mapping.exceptionType, mapping.actionName as String)
            }
        }
        return actions
    }

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
        // USER ROLES
        assertForwardUrlMapping("/api/users/123/roles", controller: 'userRoleApi', action: "getUserRoles") {
            userId = "123"
        }
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
        assertForwardUrlMapping("/api/stockMovementItems/123/removeItem", controller: 'stockMovementItemApi', action: "eraseItem")
    }
}
