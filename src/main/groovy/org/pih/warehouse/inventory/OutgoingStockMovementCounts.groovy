package org.pih.warehouse.inventory

// Counts of in progress outbound stock movements grouped by how long ago they were created.
class OutgoingStockMovementCounts {
    Integer createdLessThan4DaysAgo
    Integer createdBetween4And7DaysAgo
    Integer createdMoreThan7DaysAgo
}
