package org.pih.warehouse.shipping

import org.pih.warehouse.core.dtos.IdentifiableDto

/**
 * A simplified representation of a Container, containing only the fields that are required
 * to display the container in its most basic form.
 */
class ContainerSimpleDto implements IdentifiableDto {

    String name
    ContainerSimpleDto parentContainer

    static ContainerSimpleDto from(Container container) {
        return !container ? null : new ContainerSimpleDto(
                id: container.id,
                name: container.name,
                parentContainer: from(container.parentContainer),
        )
    }
}
