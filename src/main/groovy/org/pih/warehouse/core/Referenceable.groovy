package org.pih.warehouse.core

/**
 * Defines the ability for an object to be referenced to in a standardized format.
 */
interface Referenceable {

    /**
     * @return A reference to the object in a standardized format.
     */
    abstract ReferenceDocument getReferenceDocument()
}
