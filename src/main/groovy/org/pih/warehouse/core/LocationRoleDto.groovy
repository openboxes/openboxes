package org.pih.warehouse.core

import org.pih.warehouse.core.dtos.DomainDto
import org.pih.warehouse.location.LocationSimpleDto

/**
 * The DTO representation of a {@link LocationRole}: a role that a user has at a single location.
 */
class LocationRoleDto implements DomainDto<LocationRole> {

    LocationSimpleDto location
    RoleDto role
}
