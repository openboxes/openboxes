package org.pih.warehouse.core

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import org.pih.warehouse.core.mapper.EntityToDtoMapper
import org.pih.warehouse.core.mapper.MapperConfig
import org.pih.warehouse.location.LocationSimpleDto

@Component
class LocationRoleDtoMapper implements EntityToDtoMapper<LocationRole, LocationRoleDto> {

    @Autowired
    RoleDtoMapper roleDtoMapper

    @Override
    LocationRoleDto doMap(LocationRole source, MapperConfig config) {
        return new LocationRoleDto(
                id: source.id,
                location: LocationSimpleDto.from(source.location),
                role: roleDtoMapper.map(source.role),
        )
    }
}
