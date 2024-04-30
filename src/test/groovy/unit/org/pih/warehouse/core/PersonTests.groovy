package org.pih.warehouse.core

import grails.testing.gorm.DomainUnitTest
import org.junit.Ignore

// import grails.test.GrailsUnitTestCase
import org.junit.Test
import org.pih.warehouse.core.Person
import spock.lang.Specification
import static org.junit.Assert.*;

//@Ignore
class PersonTests extends Specification implements DomainUnitTest<Person> {

    @Test
    void sort_shouldSortByLastNameFirstName() {

        when:
        def person1 = new Person(id: 1, firstName: "Rene", lastName: "Merida", email: "rene@openboxes.com")
        def person2 = new Person(id: 2, firstName: "Levi", lastName: "Bermudez", email: "levi@openboxes.com")
        def person3 = new Person(id: 3, firstName: "Kendra", lastName: "Nishioka", email: "kendra@openboxes.com")
        def person4 = new Person(id: 4, firstName: "Terese", lastName: "Decato", email: "terese@openboxes.com")
        def person5 = new Person(id: 5, firstName: "Tanisha", lastName: "Cadorette", email: "tanisha@openboxes.com")
        def person6 = new Person(id: 6, firstName: "Kim", lastName: "Romine", email: "kim@openboxes.com")
        def person7 = new Person(id: 7, firstName: "Justin", lastName: "Miranda", email: "justin@openboxes.com")
        def person8 = new Person(id: 8, firstName: "Justin", lastName: "Miranda", email: "jmiranda@openboxes.com")

        mockDomain(Person, [person1, person2, person3, person4, person5, person6, person7, person8])

        def list = Person.list().sort()
        then:
        assert list == [person2, person5, person4, person1, person8, person7, person3, person6]

    }


    @Test
    void validate_shouldRequireValidEmail() {

        mockDomain(Person)

        when:
        def person1 = new Person(id: 1, firstName: "Rene", lastName: "Merida", email: "")
        then:
        assert person1.validate()

        when:
        def person2 = new Person(id: 2, firstName: "Levi", lastName: "Bermudez", email: null)
        then:
        assert person2.validate()

        when:
        def person3 = new Person(id: 2, firstName: "Kendra", lastName: "Nishioka", email: "email@something")
        then:
        assert !person3.validate()

        when:
        def person4 = new Person(id: 2, firstName: "Terese", lastName: "Decato", email: "email@something.com")
        then:
        assert person4.validate()

    }

}
