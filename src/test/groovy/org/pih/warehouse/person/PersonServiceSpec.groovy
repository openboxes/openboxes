package org.pih.warehouse.person

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.Person
import org.pih.warehouse.data.PersonService

@Unroll
class PersonServiceSpec extends Specification implements ServiceUnitTest<PersonService>, DataTest {
    void setupSpec() {
        mockDomain Person
    }

    void 'getOrCreatePersonByRecipient expect person #expectedPerson to be returned when given recipient #recipient'() {
        given:
        new Person(id: '1', firstName: 'a', lastName: 'b', email: '1@1.com', active: false).save(validate: false)
        new Person(id: '2', firstName: 'a', lastName: 'b', email: '1@1.com', active: true).save(validate: false)
        new Person(id: '3', firstName: 'c', lastName: 'd', email: '2@2.com', active: false).save(validate: false)

        expect:
        service.getOrCreatePersonByRecipient(recipient)?.id == expectedPerson

        where:
        recipient       || expectedPerson
        null            || null
        ''              || null
        'a b'           || '2'
        '<1@1.com>'     || '2'
        'e f <2@2.com>' || '3'  // email takes priority, even if name doesn't match and user is inactive.
    }

    void 'getOrCreatePersonByRecipient creates a new person when given non-existing recipient #recipient'() {
        when:
        Person person = service.getOrCreatePersonByRecipient(recipient)

        then:
        person?.firstName == expectedFirstName
        person?.lastName == expectedLastName
        person?.email == expectedEmail

        where:
        recipient       || expectedFirstName | expectedLastName | expectedEmail
        'a b'           || 'a'               | 'b'              | null
        'a b <1@1.com>' || 'a'               | 'b'              | '1@1.com'
    }

    void 'getOrCreatePersonByRecipient fails when given only a single word'() {
        when:
        service.getOrCreatePersonByRecipient('oneword')

        then:
        thrown(RuntimeException)
    }

    void 'getOrCreatePersonByRecipient fails a name is not provided'() {
        when:
        service.getOrCreatePersonByRecipient('<no@name.com>')

        then:
        thrown(RuntimeException)
    }

    void 'getPerson returns person or null without throwing exceptions'() {
        given:
        new Person(id: '1', firstName: 'Justin', lastName: 'Miranda', email: 'justin@openboxes.com', active: true).save(validate: false)
        new Person(id: '2', firstName: 'Justin', lastName: 'Inactive', email: 'justin@openboxes.com', active: false).save(validate: false)

        expect:
        service.getPerson(recipient)?.id == expectedId

        where:
        recipient                                || expectedId
        'Justin'                                 || null
        'Justin Miranda'                         || '1'
        'Justin Justy Miranda'                   || null
        'justin@openboxes.com'                   || '1'
        'unknown@openboxes.com'                  || null
        'Justin Miranda <justin@openboxes.com>'  || '1'
        'Justin Miranda <unknown@openboxes.com>' || null
        'Unknown Name <justin@openboxes.com>'    || '1'
        'Justin Inactive'                        || '2'
        null                                     || null
    }

    void 'getActivePerson returns person only if they are active'() {
        given:
        new Person(id: '1', firstName: 'Justin', lastName: 'Miranda', email: 'justin@openboxes.com', active: true).save(validate: false)
        new Person(id: '2', firstName: 'Justin', lastName: 'Inactive', email: 'justin@openboxes.com', active: false).save(validate: false)

        expect:
        service.getActivePerson(recipient)?.id == expectedId

        where:
        recipient                                || expectedId
        'Justin'                                 || null
        'Justin Miranda'                         || '1'
        'Justin Justy Miranda'                   || null
        'justin@openboxes.com'                   || '1'
        'unknown@openboxes.com'                  || null
        'Justin Miranda <justin@openboxes.com>'  || '1'
        'Justin Miranda <unknown@openboxes.com>' || null
        'Unknown Name <justin@openboxes.com>'    || '1'
        'Justin Inactive'                        || null
        null                                     || null
    }
}
