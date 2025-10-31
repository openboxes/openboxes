package org.pih.warehouse.inventory

/**
 * Enumerates the possible statuses of a cycle count candidate.
 *
 * Because a candidate can have all the statuses of the cycle count request and of a cycle count, this is simply
 * a copy of both those enums values.
 */
enum CycleCountCandidateStatus {
    // Cycle Count Request statuses
    CREATED,

    // Cycle Count statuses (some are the same as Cycle Count Request statuses)
    REQUESTED,
    COUNTING,
    COUNTED,
    INVESTIGATING,
    READY_TO_REVIEW,
    REVIEWED,
    COMPLETED,
    CANCELED

    String toString() {
        name()
    }
}
