package org.pih.warehouse.core.mapper

/**
 * Enumerates the different approaches to populating the fields of an object when initializing it.
 */
enum HydrationLevel {

    /**
     * Only the identifier field (as specified via {@link org.pih.warehouse.core.Identifiable}) should be populated.
     *
     * This indicates that the object is essentially a proxy object. If you want more information about it, you need
     * to fetch it in another request.
     */
    ID_ONLY,

    /**
     * The object should be fully populated, setting values for all of its fields.
     */
    FULL,
}