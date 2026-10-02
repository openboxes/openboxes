package org.pih.warehouse.core

import java.time.Instant

import org.pih.warehouse.auth.AuthService

/**
 * A base class containing common logic that should be shared across all domain entities.
 */
abstract class BaseDomain<T extends BaseDomain> implements Comparable<T>, Serializable, Identifiable {

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

    /**
     * Compares two instances of the domain for the purpose of determining order.
     *
     * We expect this method to be overwritten by child implementations that need to compare against additional fields.
     *
     * @return a negative integer, zero, or a positive integer if this object is less than, equal to, or greater than
     *         the other object respectively.
     */
    @Override
    int compareTo(T o) {
        return id <=> o?.id
    }
}
