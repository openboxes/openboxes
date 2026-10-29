package org.pih.warehouse.core

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.hibernate.ObjectNotFoundException
import spock.lang.Specification

import org.pih.warehouse.core.mapper.MapperComponentResolver
import org.pih.warehouse.core.mapper.SmartMapper

class UserRoleServiceSpec extends Specification implements ServiceUnitTest<UserRoleService>, DataTest {

    Role adminRole
    Role managerRole
    Role browserRole
    Role invoiceRole

    Location facilityA
    Location facilityB

    User user

    void setupSpec() {
        mockDomain(Location)
        mockDomain(LocationRole)
        mockDomain(Role)
        mockDomain(User)
    }

    void setup() {
        // Use the real mappers so that we test the full mapping from domains to DTOs.
        RoleDtoMapper roleDtoMapper = new RoleDtoMapper()
        LocationRoleDtoMapper locationRoleDtoMapper = new LocationRoleDtoMapper(roleDtoMapper: roleDtoMapper)
        service.smartMapper = new SmartMapper(new MapperComponentResolver(
                Optional.empty(),
                Optional.of([roleDtoMapper, locationRoleDtoMapper])))

        adminRole = new Role(roleType: RoleType.ROLE_ADMIN, name: 'Admin').save(validate: false)
        managerRole = new Role(roleType: RoleType.ROLE_MANAGER, name: 'Manager').save(validate: false)
        browserRole = new Role(roleType: RoleType.ROLE_BROWSER, name: 'Browser').save(validate: false)
        invoiceRole = new Role(roleType: RoleType.ROLE_INVOICE, name: 'Invoice user').save(validate: false)

        facilityA = new Location(name: 'Facility A').save(validate: false)
        facilityB = new Location(name: 'Facility B').save(validate: false)

        user = new User(username: 'user', password: 'password', firstName: 'Test', lastName: 'User')
        // Added out of order to check that roles are returned sorted.
        user.addToRoles(invoiceRole)
        user.addToRoles(adminRole)
        user.addToLocationRoles(new LocationRole(location: facilityB, role: managerRole))
        user.addToLocationRoles(new LocationRole(location: facilityA, role: browserRole))
        user.addToLocationRoles(new LocationRole(location: facilityA, role: managerRole))
        user.save(validate: false)
    }

    void 'getUserRoles should return all global and location roles when no facility is given'() {
        when:
        UserRolesDto userRoles = service.getUserRoles(user.id, null)

        then:
        assert userRoles.roles*.roleType == [RoleType.ROLE_ADMIN, RoleType.ROLE_INVOICE]
        assert userRoles.locationRoles*.location*.id == [facilityA.id, facilityA.id, facilityB.id]
        assert userRoles.locationRoles*.role*.roleType ==
                [RoleType.ROLE_MANAGER, RoleType.ROLE_BROWSER, RoleType.ROLE_MANAGER]
    }

    void 'getUserRoles should return all global roles but only the location roles at the given facility'() {
        when:
        UserRolesDto userRoles = service.getUserRoles(user.id, facilityB.id)

        then:
        assert userRoles.roles*.roleType == [RoleType.ROLE_ADMIN, RoleType.ROLE_INVOICE]
        assert userRoles.locationRoles.size() == 1
        assert userRoles.locationRoles[0].location.id == facilityB.id
        assert userRoles.locationRoles[0].location.name == 'Facility B'
        assert userRoles.locationRoles[0].role.id == managerRole.id
    }

    void 'getUserRoles should return no location roles when the user has none at the given facility'() {
        given:
        Location otherFacility = new Location(name: 'Other Facility').save(validate: false)

        when:
        UserRolesDto userRoles = service.getUserRoles(user.id, otherFacility.id)

        then:
        assert userRoles.roles*.roleType == [RoleType.ROLE_ADMIN, RoleType.ROLE_INVOICE]
        assert userRoles.locationRoles == []
    }

    void 'getUserRoles should return empty lists for a user without any roles'() {
        given:
        User userWithoutRoles = new User(username: 'noroles', password: 'password', firstName: 'No', lastName: 'Roles')
                .save(validate: false)

        when:
        UserRolesDto userRoles = service.getUserRoles(userWithoutRoles.id, null)

        then:
        assert userRoles.roles == []
        assert userRoles.locationRoles == []
    }

    void 'getUserRoles should map all role fields'() {
        given:
        adminRole.description = 'Administrator'
        adminRole.save(validate: false)

        when:
        UserRolesDto userRoles = service.getUserRoles(user.id, null)

        then:
        RoleDto roleDto = userRoles.roles[0]
        assert roleDto.id == adminRole.id
        assert roleDto.name == 'Admin'
        assert roleDto.roleType == RoleType.ROLE_ADMIN
        assert roleDto.description == 'Administrator'
    }

    void 'getUserRoles should fail when the user does not exist'() {
        when:
        service.getUserRoles('nonexistent', null)

        then:
        thrown(ObjectNotFoundException)
    }

    void 'getUserRoles should fail when the facility does not exist'() {
        when:
        service.getUserRoles(user.id, 'nonexistent')

        then:
        thrown(ObjectNotFoundException)
    }
}
