package org.pih.warehouse.core

import org.hibernate.Hibernate

import org.pih.warehouse.core.dtos.DomainDto

/**
 * A simple, general purpose DTO representing a Person.
 */
class PersonDto implements DomainDto<Person> {

    String name
    String email
    String phoneNumber
    Date dateCreated
    Date lastUpdated
    Boolean active = true

    static PersonDto from(Person person) {
        // A lazy Person association is a Hibernate proxy of Person even when the row is a User
        person = Hibernate.unproxy(person) as Person
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
