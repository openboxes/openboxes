package org.pih.warehouse.api

enum FulfillmentStatusCode {
    // Received, but not yet allocated
    CREATED,
    // Some items have allocation but not all
    PARTIALLY_ALLOCATED,
    // Allocation complete, waiting for picking to start
    ALLOCATED,
    // Picking in progress
    PICKING,
    // Picking is complete for all items
    PICKED,
    // Some items are staged but not all
    PARTIALLY_STAGED,
    // All items are in the final staging location
    STAGED,
    // Some items are shipped but not all
    PARTIALLY_ISSUED,
    // The movement has been fully fulfilled/shipped
    ISSUED,
    // All items in the movement were cancelled
    CANCELLED,
    // Partial fulfillment occurred, remaining quantity is unavailable
    BACKORDERED
}
