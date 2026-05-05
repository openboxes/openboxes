package org.pih.warehouse.inventory

// Counts of in progress outbound stock movements grouped by how long ago they were created.
class OutgoingStockMovementCounts {
    Integer lessThan4DaysAgo
    Integer between4And7DaysAgo
    Integer moreThan7DaysAgo
}
