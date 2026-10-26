package org.pih.warehouse.api.regression.platform.client.generic

/**
 * Resource names for the generic API (/api/generic/{resource}). Upstream's GenericResource is an enum with only
 * PRODUCT, and an enum cannot be extended from test code; this adds ORGANIZATION, SHIPMENT_TYPE and TAG.
 * Port: `GenericResource.X` -> `RegressionGenericResource.X` (plain strings, so any other domain name works too).
 */
final class RegressionGenericResource {
    static final String ORGANIZATION = 'organization'
    static final String PRODUCT = 'product'
    static final String SHIPMENT_TYPE = 'shipmentType'
    static final String TAG = 'tag'

    private RegressionGenericResource() {}
}
