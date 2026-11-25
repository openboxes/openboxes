package org.pih.warehouse.core

import spock.lang.Specification

class RoleDtoMapperSpec extends Specification {

    RoleDtoMapper mapper

    void setup() {
        mapper = new RoleDtoMapper()
    }

    void 'map should map all fields of the role'() {
        given:
        Role role = new Role(roleType: RoleType.ROLE_ADMIN, name: 'Admin', description: 'Administrator')
        role.id = '1'

        when:
        RoleDto dto = mapper.map(role)

        then:
        assert dto.id == '1'
        assert dto.name == 'Admin'
        assert dto.roleType == RoleType.ROLE_ADMIN
        assert dto.description == 'Administrator'
    }

    void 'map should return null when the role is null'() {
        expect:
        assert mapper.map(null) == null
    }
}
