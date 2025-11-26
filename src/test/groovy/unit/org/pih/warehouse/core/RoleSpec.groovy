package unit.org.pih.warehouse.core

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

import org.pih.warehouse.core.Role
import org.pih.warehouse.core.RoleType

class RoleSpec extends Specification implements DomainUnitTest<Role> {

    void setupSpec() {
        mockDomain(Role)
    }

    void 'admin() should return the admin role'() {
        given:
        Role role = new Role(roleType: RoleType.ROLE_ADMIN).save(validate: false)

        expect:
        assert Role.admin() == role
    }

    void 'manager() should return the manager role'() {
        given:
        Role role = new Role(roleType: RoleType.ROLE_MANAGER).save(validate: false)

        expect:
        assert Role.manager() == role
    }

    void 'assistant() should return the assistant role'() {
        given:
        Role role = new Role(roleType: RoleType.ROLE_ASSISTANT).save(validate: false)

        expect:
        assert Role.assistant() == role
    }

    void 'browser() should return the browser role'() {
        given:
        Role role = new Role(roleType: RoleType.ROLE_BROWSER).save(validate: false)

        expect:
        assert Role.browser() == role
    }
}
