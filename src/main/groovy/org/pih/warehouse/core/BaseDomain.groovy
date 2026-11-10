package org.pih.warehouse.core

import java.time.Instant

import org.pih.warehouse.auth.AuthService

/**
 * A base class containing common logic that should be shared across all domain entities.
 *
 * It is important to note that because BaseDomain is under /src and not /grails-app/domain, it can never itself be
 * recognized as a domain entity. This is what allows BaseDomain to be a source for common domain logic / fields
 * without it becoming a 'base_domain' table in the database. This is true for any value of the `tablePerHierarchy`
 * GORM mapping property.
 *
 * Another important note is that the fields of this base class do not contribute to the dirty check of the Hibernate
 * instance because we don't annotate the class with `@DirtyCheck`. This means that if you change the value of
 * a field in the BaseDomain, but don't change any field values in the implementing class, it won't actually persist
 * to the database. This is fine because this base class only specifies fields that should not be directly modified
 * since they are automatically set by the system. As such, we don't need these fields to dirty the instance.
 */
abstract class BaseDomain implements Serializable, Identifiable, Auditable {

    /**
     * The UUID unique identifier of the domain entity.
     */
    String id

    /**
     * The datetime of when the domain was created / persisted to the database.
     */
    Instant dateCreated

    /**
     * The datetime of when the object was most recently updated.
     */
    Instant lastUpdated

    /**
     * The {@link User} who created this domain instance.
     */
    User createdBy

    /**
     * The {@link User} who most recently updated this domain instance.
     */
    User updatedBy

    def beforeInsert() {
        createdBy = AuthService.currentUser
        updatedBy = AuthService.currentUser
    }

    def beforeUpdate() {
        updatedBy = AuthService.currentUser
    }

    static mapping = {
        id generator: 'uuid'
    }
}
