package org.pih.warehouse.core

import java.time.Instant

/**
 * Represents an object containing methods of providing auditing information.
 */
interface Auditable {

    /**
     * The datetime of when the object was created.
     */
    Instant getDateCreated()

    /**
     * The datetime of when the object was most recently updated.
     */
    Instant getLastUpdated()

    /**
     * The {@link User} who created this object.
     */
    User getCreatedBy()

    /**
     * The {@link User} who most recently updated this object.
     */
    User getUpdatedBy()
}
