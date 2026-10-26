package org.pih.warehouse.regression.platform.util

import org.pih.warehouse.util.*
import org.grails.testing.GrailsUnitTest
import spock.lang.Specification
import util.ConfigHelper
import util.InventoryUtil

import org.pih.warehouse.core.RoleType
import org.pih.warehouse.inventory.InventoryStatus

class InventoryUtilAndConfigHelperSpec extends Specification implements GrailsUnitTest {

    void 'the stock status is #expected for status #status, levels #min/#reorder/#max and quantity #quantity'() {
        expect:
        InventoryUtil.getStatusMessage(status, min, reorder, max, quantity) == expected

        where:
        status                                  | min  | reorder | max  | quantity || expected
        null                                    | 10   | 20      | 100  | 0        || 'STOCK_OUT'
        InventoryStatus.SUPPORTED               | 10   | 20      | 100  | -1       || 'STOCK_OUT'
        InventoryStatus.SUPPORTED               | 10   | 20      | 100  | 10       || 'LOW_STOCK'
        InventoryStatus.SUPPORTED               | 10   | 20      | 100  | 15       || 'REORDER'
        InventoryStatus.SUPPORTED               | 10   | 20      | 100  | 101      || 'OVERSTOCK'
        InventoryStatus.SUPPORTED               | 10   | 20      | 100  | 50       || 'IN_STOCK'
        InventoryStatus.SUPPORTED               | null | null    | null | 5        || 'IN_STOCK'
        InventoryStatus.SUPPORTED               | 0    | 0       | 0    | 5        || 'IN_STOCK'
        InventoryStatus.NOT_SUPPORTED           | 10   | 20      | 100  | 50       || 'NOT_SUPPORTED'
        InventoryStatus.SUPPORTED_NON_INVENTORY | 10   | 20      | 100  | 50       || 'SUPPORTED_NON_INVENTORY'
        InventoryStatus.INACTIVE                | 10   | 20      | 100  | 50       || 'UNAVAILABLE'
    }

    void 'boolean and list config values accept strings'() {
        expect:
        ConfigHelper.booleanValue(true) == true
        ConfigHelper.booleanValue(false) == false
        ConfigHelper.booleanValue('true') == true
        ConfigHelper.booleanValue('no') == false
        ConfigHelper.listValue('a,b,c') as List == ['a', 'b', 'c']
        ConfigHelper.listValue(['x']) == ['x']
        ConfigHelper.listValue(null) == null
    }

    void 'the context path is empty for the root context'() {
        when:
        config.server.contextPath = contextPath

        then:
        ConfigHelper.contextPath == expected

        where:
        contextPath  || expected
        '/'          || ''
        '/openboxes' || '/openboxes'
    }

    void 'access rules prefer the controller and action rule, then the controller wildcard, then the global wildcard'() {
        given:
        config.openboxes.security.rbac.rules = [
                [controller: '*', actions: ['*'], accessRules: [minimumRequiredRole: RoleType.ROLE_BROWSER]],
                [controller: 'order', actions: ['*'], accessRules: [minimumRequiredRole: RoleType.ROLE_ASSISTANT]],
                [controller: 'order', actions: ['remove'], accessRules: [minimumRequiredRole: RoleType.ROLE_MANAGER]],
                [controller: 'order', actions: ['remove', 'purge'], accessRules: [minimumRequiredRole: RoleType.ROLE_ADMIN]],
        ]

        expect: 'the highest role wins among rules for the same action'
        ConfigHelper.findAccessRule('order', 'remove').accessRules.minimumRequiredRole == RoleType.ROLE_ADMIN
        ConfigHelper.findAccessRule('order', 'purge').accessRules.minimumRequiredRole == RoleType.ROLE_ADMIN
        ConfigHelper.findAccessRule('order', 'show').accessRules.minimumRequiredRole == RoleType.ROLE_ASSISTANT
        ConfigHelper.findAccessRule('product', 'show').accessRules.minimumRequiredRole == RoleType.ROLE_BROWSER
    }

    void 'without matching rules there is no access rule'() {
        given:
        config.openboxes.security.rbac.rules = [
                [controller: 'order', actions: ['remove'], accessRules: [minimumRequiredRole: RoleType.ROLE_MANAGER]]]

        expect:
        ConfigHelper.findAccessRule('product', 'show') == null
    }
}
