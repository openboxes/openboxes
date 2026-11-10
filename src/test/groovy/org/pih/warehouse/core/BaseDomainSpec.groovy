package org.pih.warehouse.core

import grails.gorm.annotation.Entity
import grails.testing.gorm.DataTest
import java.time.Instant
import spock.lang.Specification

import org.pih.warehouse.auth.AuthService

class BaseDomainSpec extends Specification implements DataTest {

    User userA
    User userB

    void setupSpec() {
        mockDomains(BaseDomainTestEntity, User)
    }

    void setup() {
        GroovySpy(AuthService, global: true)

        userA = new User(email: 'userA@mail.com')
        userB = new User(email: 'userB@mail.com')
    }

    void 'saving a new entity should generate an id and populate the audit fields'() {
        given:
        AuthService.getCurrentUser() >> userA
        BaseDomainTestEntity entity = new BaseDomainTestEntity(name: 'test')

        when:
        entity.save(flush: true, failOnError: true)

        then:
        assert entity.id != null
        assert entity.dateCreated != null
        assert entity.lastUpdated != null
        assert entity.createdBy == userA
        assert entity.updatedBy == userA
    }

    void 'updating an existing entity should change updatedBy and lastUpdated but not createdBy or dateCreated'() {
        given: 'the entity is created by some user'
        User currentUser = userA
        AuthService.getCurrentUser() >> { currentUser }

        BaseDomainTestEntity entity = new BaseDomainTestEntity(name: 'test')
        entity.save(flush: true, failOnError: true)

        Instant originalDateCreated = entity.dateCreated
        Instant originalLastUpdated = entity.lastUpdated

        and: 'a new user becomes active'
        sleep(5)  // Ensure the lastUpdated timestamp moves forward
        currentUser = userB

        when:
        entity.name = 'changed'
        entity.save(flush: true, failOnError: true)

        then:
        assert entity.createdBy == userA
        assert entity.updatedBy == userB
        assert entity.dateCreated == originalDateCreated
        assert entity.lastUpdated.isAfter(originalLastUpdated)
    }

    void 'saving a new entity when there is no authenticated user succeeds'() {
        given:
        AuthService.getCurrentUser() >> null
        BaseDomainTestEntity entity = new BaseDomainTestEntity(name: 'test')

        when:
        entity.save(flush: true, failOnError: true)

        then:
        assert entity.id != null
        assert entity.dateCreated != null
        assert entity.lastUpdated != null
        assert entity.createdBy == null
        assert entity.updatedBy == null
    }
}

@Entity
class BaseDomainTestEntity extends BaseDomain {
    String name
}
