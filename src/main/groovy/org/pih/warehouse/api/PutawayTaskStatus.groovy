package org.pih.warehouse.api

enum PutawayTaskStatus {

    PENDING(StatusCategory.OPEN),
    IN_PROGRESS(StatusCategory.OPEN),
    IN_TRANSIT(StatusCategory.OPEN),
    COMPLETED(StatusCategory.CLOSED),
    CANCELED(StatusCategory.CLOSED)

    // from -> allowed next states
    private static final Map<String, Set<String>> ALLOWED_STATE_TRANSITIONS = [
            (PENDING)    : [IN_PROGRESS, COMPLETED, CANCELED] as Set,
            (IN_PROGRESS): [IN_TRANSIT, COMPLETED, CANCELED] as Set,
            (IN_TRANSIT) : [COMPLETED, CANCELED] as Set,
            (COMPLETED)  : [] as Set,
            (CANCELED)   : [] as Set,
    ]



    final StatusCategory statusCategory

    PutawayTaskStatus(StatusCategory c){
        this.statusCategory = c
    }

    StatusCategory getStatusCategory() {
        return this.statusCategory;
    }

    boolean isOpen()   {
        statusCategory == StatusCategory.OPEN
    }

    boolean isClosed() {
        statusCategory == StatusCategory.CLOSED
    }

    static Boolean validateTransition(PutawayTaskStatus from, PutawayTaskStatus to) {
        return ALLOWED_STATE_TRANSITIONS.get(from, Collections.emptySet()).contains(to)
    }


    /**
     * Convert the putaway task status to putaway status.
     *
     * FIXME We don't currently have a way of going in the opposite direction, so we should probably combine the two
     *  statuses into one, but I haven't figured out the right way to do that yet.
     * @return
     */
    PutawayStatus toPutawayStatus() {
        switch (this) {
            case PENDING:
                return PutawayStatus.READY
                break
            case IN_PROGRESS:
                return PutawayStatus.PENDING
                break
            case IN_TRANSIT:
                return PutawayStatus.PENDING
                break
            case COMPLETED:
                return PutawayStatus.COMPLETED
                break
            case CANCELED:
                return PutawayStatus.CANCELED
                break
        }
    }

    static toSet(StatusCategory statusCategory) {
        return values().findAll { it.statusCategory == statusCategory }.toSet() as List<PutawayTaskStatus>
    }

}