package org.pih.warehouse.shipping

/**
 * A simplified representation of a Container, containing only the fields that are required
 * to display the container in its most basic form.
 */
class ContainerSimpleDto {
    String id
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
