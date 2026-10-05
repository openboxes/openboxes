package org.pih.warehouse.core

import org.pih.warehouse.core.dtos.DomainDto

/**
 * The DTO representation of a {@link Role}.
 */
class RoleDto implements DomainDto<Role> {

    String name
    RoleType roleType
    String description
}
