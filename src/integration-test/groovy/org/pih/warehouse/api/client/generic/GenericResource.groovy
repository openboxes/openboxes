package org.pih.warehouse.api.client.generic

/**
 * Enumerates the different resource types supported by the generic API.
 */
enum GenericResource {
    PRODUCT('product'),

    /**
     * The name of the resource. Used in the URI of the request.
     */
    final String name

    /**
     * The name of the field representing the id of the resource.
     */
    final String idField

    private GenericResource(String name, String idField='id') {
        this.name = name
        this.idField = idField
    }

    String toString() {
        return name
    }
}
