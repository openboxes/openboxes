package org.pih.warehouse.core

import grails.validation.Validateable

/**
 * A POJO representing a reference to some source object, such as a database entity.
 * The goal is to standardize how we provide references to entities, which is mostly useful for the frontend.
 */
class ReferenceDocument implements Validateable {
    /**
     * The suggested text for a frontend to display when referencing the document.
     */
    String label

    /**
     * A link to the resource that provides further details about the document.
     */
    String url

    /**
     * The unique id of the document
     */
    String id

    /**
     * A user-friendly identifier used internally within the system.
     * Typically this is the identifier generated via an {@link IdentifierService} implementation.
     */
    String identifier

    String description

    String name

    static constraints = {
        url(nullable: true)
        description(nullable: true)
        name(nullable: true)
    }
}
