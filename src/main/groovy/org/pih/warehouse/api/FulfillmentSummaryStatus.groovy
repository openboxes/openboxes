package org.pih.warehouse.api

enum FulfillmentSummaryStatus {
    // Received, but not yet allocated
    CREATED,
    // Some items have allocation but not all
    PARTIALLY_ALLOCATED,
    // Allocation complete, picking in progress
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
    BACK_ORDER
}
