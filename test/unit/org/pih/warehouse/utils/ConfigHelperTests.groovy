package org.pih.warehouse.utils

import grails.test.GrailsUnitTestCase
import org.junit.Test
import org.pih.warehouse.core.RoleType
import util.ConfigHelper

class ConfigHelperTests extends GrailsUnitTestCase {
    @Test
    void findAccessRule_shouldReturnCorrectRule() {
        def rules = [
            [controller: 'order', actions: ['remove'], accessRules: [ minimumRequiredRole: RoleType.ROLE_MANAGER ]],
            [controller: 'order', actions: ['removeOrderItem'], accessRules: [ minimumRequiredRole: RoleType.ROLE_MANAGER ]],
            [controller: 'stockMovement', actions: ['remove'], accessRules: [ minimumRequiredRole: RoleType.ROLE_ASSISTANT ]],
            [controller: 'stockRequest', actions: ['remove'], accessRules: [minimumRequiredRole: RoleType.ROLE_ADMIN]]
        ]
        def expectedResult = [controller: 'stockRequest', actions: ['remove'], accessRules: [minimumRequiredRole: RoleType.ROLE_ADMIN]]
        assertEquals expectedResult, ConfigHelper.findAccessRule("stockRequest", "remove", rules)
    }

    @Test
    void findAccessRule_shouldReturnExceptionWhenTwoRulesForTheSameControllerAndAction() {
        def rules = [
                [controller: '*', actions: ['remove'], accessRules: [ minimumRequiredRole: RoleType.ROLE_SUPERUSER ]],
                [controller: 'order', actions: ['removeOrderItem'], accessRules: [ minimumRequiredRole: RoleType.ROLE_MANAGER ]],
                [controller: 'stockRequest', actions: ['remove'], accessRules: [minimumRequiredRole: RoleType.ROLE_ADMIN]],
                [controller: 'stockRequest', actions: ['remove'], accessRules: [minimumRequiredRole: RoleType.ROLE_MANAGER]]
        ]
        def message = shouldFail(Exception) {
            ConfigHelper.findAccessRule("stockRequest", "remove", rules)
        }
        assertTrue message == "There can't be more than one rule specified for this controller and action!"
    }

    @Test
    void findAccessRule_shouldReturnCorrectRuleWhenTwoActionsContainingTheSameStringFirstScenario() {
        def rules = [
                [controller: '*', actions: ['remove'], accessRules: [ minimumRequiredRole: RoleType.ROLE_SUPERUSER ]],
                [controller: 'order', actions: ['removeOrderItem'], accessRules: [ minimumRequiredRole: RoleType.ROLE_MANAGER ]],
                [controller: 'stockRequest', actions: ['remove'], accessRules: [minimumRequiredRole: RoleType.ROLE_ADMIN]],
                [controller: 'stockRequest', actions: ['removeItem'], accessRules: [minimumRequiredRole: RoleType.ROLE_MANAGER]]
        ]
        def expectedResult = [controller: 'stockRequest', actions: ['remove'], accessRules: [minimumRequiredRole: RoleType.ROLE_ADMIN]]
        assertEquals expectedResult, ConfigHelper.findAccessRule("stockRequest", "remove", rules)
    }

    @Test
    void findAccessRule_shouldReturnCorrectRuleWhenTwoActionsContainingTheSameStringSecondScenario() {
        def rules = [
                [controller: '*', actions: ['remove'], accessRules: [ minimumRequiredRole: RoleType.ROLE_SUPERUSER ]],
                [controller: 'order', actions: ['removeOrderItem'], accessRules: [ minimumRequiredRole: RoleType.ROLE_MANAGER ]],
                [controller: 'stockRequest', actions: ['remove'], accessRules: [minimumRequiredRole: RoleType.ROLE_ADMIN]],
                [controller: 'stockRequest', actions: ['removeItem'], accessRules: [minimumRequiredRole: RoleType.ROLE_MANAGER]]
        ]
        def expectedResult = [controller: 'stockRequest', actions: ['removeItem'], accessRules: [minimumRequiredRole: RoleType.ROLE_MANAGER]]
        assertEquals expectedResult, ConfigHelper.findAccessRule("stockRequest", "removeItem", rules)
    }

    @Test
    void findAccessRule_shouldReturnNullIfNotExistingRule() {
        def rules = [
                [controller: '*', actions: ['remove'], accessRules: [ minimumRequiredRole: RoleType.ROLE_SUPERUSER ]],
                [controller: 'order', actions: ['removeOrderItem'], accessRules: [ minimumRequiredRole: RoleType.ROLE_MANAGER ]],
        ]
        assertNull ConfigHelper.findAccessRule("stockRequest", "remove", rules)
    }
}
