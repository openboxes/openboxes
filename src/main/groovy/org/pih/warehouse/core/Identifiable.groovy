package org.pih.warehouse.core

/**
 * Represents an object that is identifiable via some identifier field.
 *
 * In practice, this likely means the object is either a domain entity or a DTO representation of one.
 */
interface Identifiable {

    /**
     * Fetch the identifier of the object.
     */
    String getId()
}
