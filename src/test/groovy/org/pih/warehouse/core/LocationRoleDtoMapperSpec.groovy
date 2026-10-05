package org.pih.warehouse.core

import spock.lang.Specification

class LocationRoleDtoMapperSpec extends Specification {

    LocationRoleDtoMapper mapper

    void setup() {
        mapper = new LocationRoleDtoMapper(roleDtoMapper: new RoleDtoMapper())
    }

    void 'map should map the location role with its location and role'() {
        given:
        Location location = new Location(name: 'Main Warehouse', locationNumber: 'MW', active: true)
        location.id = 'location1'

        Role role = new Role(roleType: RoleType.ROLE_MANAGER, name: 'Manager')
        role.id = 'role1'

        LocationRole locationRole = new LocationRole(location: location, role: role)
        locationRole.id = 'locationRole1'

        when:
        LocationRoleDto dto = mapper.map(locationRole)

        then:
        assert dto.id == 'locationRole1'
        assert dto.location.id == 'location1'
        assert dto.location.name == 'Main Warehouse'
        assert dto.location.locationNumber == 'MW'
        assert dto.location.active
        assert dto.role.id == 'role1'
        assert dto.role.name == 'Manager'
        assert dto.role.roleType == RoleType.ROLE_MANAGER
    }

    void 'map should return null when the location role is null'() {
        expect:
        assert mapper.map(null) == null
    }
}
