package org.pih.warehouse.core

import grails.test.mixin.integration.Integration
import org.junit.Test

@Integration
class RoleIntegrationTests {

    @Test
    void test_getAdmin() {
        assert Role.admin().roleType == RoleType.ROLE_ADMIN
    }

    @Test
    void test_getManager() {
        assert Role.manager().roleType == RoleType.ROLE_MANAGER
    }

    @Test
    void test_getAssistant() {
        assert Role.assistant().roleType == RoleType.ROLE_ASSISTANT
    }

    @Test
    void test_getBrowser() {
        assert Role.browser().roleType == RoleType.ROLE_BROWSER
    }

}
