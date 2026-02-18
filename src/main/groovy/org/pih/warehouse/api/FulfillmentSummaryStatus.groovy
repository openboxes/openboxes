package org.pih.warehouse.api

enum FulfillmentSummaryStatus {
    // Received, but not yet allocated
    CREATED,
    // Allocation has started or is in progress
    PICKING,
    // Picking is complete for all items
    PICKED,
    // All items are in the final staging location
    STAGED,
    // The movement has been fully fulfilled/shipped
    ISSUED,
    // All items in the movement were cancelled
    CANCELLED,
    // Partial fulfillment occurred, remaining quantity is unavailable
    BACK_ORDER


}
