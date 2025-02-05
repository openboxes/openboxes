package org.pih.warehouse.inventory

/**
 * The different ways that we support grouping of CycleCountStatus. Used for unifying behaviour and for allowing
 * the client to specify groups without needing to understand the inner workings of cycle count status.
 */
enum CycleCountStatusGroup {

    /**
     * Meaning there is no active count.
     */
    NOT_YET_REQUESTED([]),

    /**
     * Meaning the count has been requested but is not yet submitted.
     */
    TO_COUNT([
            CycleCountStatus.REQUESTED,
            CycleCountStatus.COUNTING,
    ]),

    /**
     * Meaning a count has occurred and now there's a discrepancy to resolve.
     */
    TO_RESOLVE([
            CycleCountStatus.COUNTED,
            CycleCountStatus.INVESTIGATING,
    ]),

    /**
     * Meaning the count has been completed and now must be reviewed.
     */
    TO_REVIEW([
            CycleCountStatus.READY_TO_REVIEW,
    ])

    List<CycleCountStatus> statuses

    private CycleCountStatusGroup(List<CycleCountStatus> statuses) {
        this.statuses = statuses
    }
}
