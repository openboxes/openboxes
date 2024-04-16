package unit.org.pih.warehouse.core

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.Person

@Unroll
class PersonSpec extends Specification implements DomainUnitTest<Person> {

    void 'Sort order'() {
        given:
        Person person1 = new Person(lastName: "Amiranda", firstName: "Austin", email: "amiranda@openboxes.com", id: 1)
        Person person2 = new Person(lastName: "Miranda", firstName: "Austin", email: "amiranda@openboxes.com", id: 2)
        Person person3 = new Person(lastName: "Miranda", firstName: "Justin", email: "amiranda@openboxes.com", id: 3)
        Person person4 = new Person(lastName: "Miranda", firstName: "Justin", email: "jmiranda@openboxes.com", id: 4)

        // Mock them in a random order to make sure sort actually does something
        mockDomain(Person, [person2, person4, person3, person1])

        expect:
        Person.list().sort() == [person1, person2, person3, person4]
    }

    void 'Person.validate() with email: #value should return #expected with errorCode: #expectedErrorCode'() {
        when:
        domain.email = value

        then:
        domain.validate(['email']) == expected
        domain.errors['email']?.code == expectedErrorCode

        where:
        value                 || expected | expectedErrorCode
        null                  ||  true    | null
        ''                    ||  true    | null
        'email@something.com' ||  true    | null
        'email@something'     ||  false   | 'email.invalid'
    }

    void 'Person.getLastInitial() should return: #expectedLastInitial for lastName #lastName'() {
        when:
        domain.lastName = lastName

        then:
        expectedLastInitial == domain.getLastInitial()

        where:
        lastName   || expectedLastInitial
        null       ||  null
        'Lastname' ||  'L'
        'lastname' ||  'l'
    }

    void 'Person.getName() should return correct for #firstName #lastName'() {
        when:
        domain.firstName = firstName
        domain.lastName = lastName

        config.openboxes.anonymize.enabled = anonymize

        then:
        domain.getName() == expectedName

        where:
        firstName | lastName | anonymize || expectedName
        'fname'   |  'lname' | false     || 'fname lname'
        'fname'   |  'lname' | true      || 'fname l'
    }

    void 'Person.toString() should return correct for #firstName #lastName'() {
        when:
        domain.firstName = firstName
        domain.lastName = lastName

        config.openboxes.anonymize.enabled = anonymize

        then:
        domain.toString() == expectedToString

        where:
        firstName | lastName | anonymize || expectedToString
        'fname'   |  'lname' | false     || 'fname lname'
        'fname'   |  'lname' | true      || 'fname l'
    }

    void 'Person.toJson() should return as expected'(){
        given:
        Person person = new Person(id: id, firstName: firstName, lastName: lastName, email: email).save(validate: false)

        config.openboxes.anonymize.enabled = anonymize

        when:
        Map json = person.toJson()

        then:
        json == [
                id: expectedId,
                name: expectedName,
                firstName: expectedFirstName,
                lastName: expectedLastName,
                email: expectedEmail,
                username: expectedUsername]

        where:
        id  | firstName | lastName | email     | anonymize || expectedId | expectedName  | expectedFirstName | expectedLastName | expectedEmail | expectedUsername
        1   | 'fname'   | 'lname'  | 'a@a.com' | false     || '1'        | 'fname lname' | 'fname'           | 'lname'          | 'a@a.com'     | null
        1   | 'fname'   | 'lname'  | 'a@a.com' | true      || '1'        | 'fname l'     | 'fname'           | 'l'              | '*******'     | null
    }
}
