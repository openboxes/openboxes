package org.pih.warehouse.core

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

import org.pih.warehouse.core.Role
import org.pih.warehouse.core.RoleType

class RoleSpec extends Specification implements DomainUnitTest<Role> {

    void setupSpec() {
        mockDomain(Role)
    }

    void 'should return the admin role'() {
        given:
        Role role = new Role(roleType: RoleType.ROLE_ADMIN).save(validate: false)

        expect:
        assert Role.admin() == role
    }

    void 'should return the manager role'() {
        given:
        Role role = new Role(roleType: RoleType.ROLE_MANAGER).save(validate: false)

        expect:
        assert Role.manager() == role
    }

    void 'should return the assistant role'() {
        given:
        Role role = new Role(roleType: RoleType.ROLE_ASSISTANT).save(validate: false)

        expect:
        assert Role.assistant() == role
    }

    void 'should return the browser role'() {
        given:
        Role role = new Role(roleType: RoleType.ROLE_BROWSER).save(validate: false)

        expect:
        assert Role.browser() == role
    }

    void 'should order roles by sortOrder, with the more privileged role first'() {
        // FIXME id is not bindable via the map constructor, so it is set directly
        given: 'an admin role and a finance role with a higher sortOrder'
        Role admin = new Role(roleType: RoleType.ROLE_ADMIN)     // sortOrder 1
        admin.id = 'a'
        Role finance = new Role(roleType: RoleType.ROLE_FINANCE) // sortOrder 100
        finance.id = 'b'

        expect: 'the role with the lower sortOrder sorts first'
        admin < finance
        finance > admin
    }

    void 'should break sortOrder ties by id so that distinct roles are never equal'() {
        // FIXME id is not bindable via the map constructor, so it is set directly
        given: 'two distinct roles that share the same sortOrder'
        Role finance = new Role(roleType: RoleType.ROLE_FINANCE) // sortOrder 100
        finance.id = 'a'
        Role invoice = new Role(roleType: RoleType.ROLE_INVOICE) // sortOrder 100
        invoice.id = 'b'

        expect: 'that they are not equal when compared'
        (finance <=> invoice) != 0
        (invoice <=> finance) != 0

        and: 'they are treated as separate elements when included in a set rather than collapsed (see OBPIH-7904)'
        (([finance, invoice] as Set) - ([finance] as Set)) == ([invoice] as Set)
        [finance, invoice].unique().size() == 2
    }
}
