package org.pih.warehouse.core

import org.pih.warehouse.core.dtos.IdentifiableDto

/**
 * A simple, general purpose DTO representing a Person.
 */
class PersonDto implements IdentifiableDto {

    String name
    String email
    String phoneNumber
    Date dateCreated
    Date lastUpdated
    Boolean active = true

    static PersonDto from(Person person) {
        return !person ? null : new PersonDto(
                id: person.id,
                name: person.name,
                email: person.email,
                phoneNumber: person.phoneNumber,
                dateCreated: person.dateCreated,
                lastUpdated: person.lastUpdated,
                active: person.active,
        )
    }
}
