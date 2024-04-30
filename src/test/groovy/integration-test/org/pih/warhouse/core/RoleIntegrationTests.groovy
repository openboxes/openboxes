package org.pih.warehouse.core

import grails.testing.gorm.DomainUnitTest
import org.junit.Ignore
import org.junit.Test
import org.pih.warehouse.core.Role
import spock.lang.Specification
import static org.junit.Assert.*;

//@Ignore
class RoleIntegrationTests extends Specification implements DomainUnitTest<Role> {

    public void setup() throws Exception {
//        super.setUp();
    }


    @Test
    void test_getAdmin() {
        when:
        String roleType = Role.admin().roleType
        then:
        assert Role.admin().roleType == RoleType.ROLE_ADMIN
    }

    @Test
    void test_getManager() {
        when:
        String roleType = Role.manager().roleType
        then:
        assert Role.manager().roleType == RoleType.ROLE_MANAGER
    }

    @Test
    void test_getAssistant() {
        when:
        String roleType = Role.assistant().roleType
        then:
        assert Role.assistant().roleType == RoleType.ROLE_ASSISTANT
    }

    @Test
    void test_getBrowser() {
        when:
        String roleType = Role.browser().roleType
        then:
        assert Role.browser().roleType == RoleType.ROLE_BROWSER
    }

}
