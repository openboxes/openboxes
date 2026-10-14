package org.pih.warehouse.core

import org.springframework.stereotype.Component

import org.pih.warehouse.core.mapper.EntityToDtoMapper
import org.pih.warehouse.core.mapper.MapperConfig

@Component
class RoleDtoMapper implements EntityToDtoMapper<Role, RoleDto> {

    @Override
    RoleDto doMap(Role source, MapperConfig config) {
        return new RoleDto(
                id: source.id,
                name: source.name,
                roleType: source.roleType,
                description: source.description,
        )
    }
}
