// Companion spec: extra features for upstream's PersonServiceSpec (the upstream file stays authoritative).
package org.pih.warehouse.regression.platform.person

import org.pih.warehouse.person.*
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import grails.validation.ValidationException
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.Person
import org.pih.warehouse.data.PersonService

@Unroll
class PersonServiceMoreSpec extends Specification implements ServiceUnitTest<PersonService>, DataTest {
    void setupSpec() {
        mockDomain Person
    }

    void 'getOrCreatePersonByRecipient puts everything after the first name into the last name'() {
        when:
        Person person = service.getOrCreatePersonByRecipient('Mary Ann Smith <mary@example.org>')

        then:
        person.firstName == 'Mary'
        person.lastName == 'Ann Smith'
        person.email == 'mary@example.org'
        Person.count() == 1
    }

    void 'getOrCreatePersonByRecipient returns an existing person without creating a duplicate'() {
        given:
        new Person(id: '1', firstName: 'a', lastName: 'b', active: false).save(validate: false)

        when:
        Person person = service.getOrCreatePersonByRecipient('a b')

        then:
        person.id == '1'  // even though they are inactive
        Person.count() == 1
    }

    void 'getOrCreatePersonByRecipient fails with a validation error when the new person is invalid: #recipient'() {
        when:
        service.getOrCreatePersonByRecipient(recipient)

        then:
        thrown(ValidationException)
        Person.count() == 0

        where:
        recipient << ['a ' + 'b' * 256, 'a ' + 'b' * 256 + ' <long@example.org>']
    }

    void 'getPerson ignores surrounding whitespace and treats blank identifiers as unknown'() {
        given:
        new Person(id: '1', firstName: 'Jane', lastName: 'Doe', email: 'jane.doe@example.org', active: true).save(validate: false)

        expect:
        service.getPerson(identifier)?.id == expectedId

        where:
        identifier                   || expectedId
        '  jane.doe@example.org  '   || '1'
        ' Jane Doe '                 || '1'
        '   '                        || null
    }
}
