package util

import org.apache.commons.lang3.RandomStringUtils
import org.pih.warehouse.core.Person
import spock.lang.Specification

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.*

@Integration
@Rollback
class DatabaseContainerSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    def setupData() {
        Person.withNewTransaction { tranasction ->
            Person person = new Person(firstName: RandomStringUtils.random(20), lastName: RandomStringUtils.random(20)).save(flush:true)
            log.info "person " + person.errors
        }
    }


    void "test something"() {
        expect:"fix me"
        Person.count() == 1
        println "Persons " + Person.list()
        Person.count() == 2
    }

    void "create a person record in testcontainer mysql instance"() {
        when: "when this"
        Person person = new Person(firstName: "firstName", lastName: "lastName").save(flush: true)
        println person.errors

        then: "then that "
        Person.count == old(Person.count) + 1
        Person.exists(person.id)
    }
}
