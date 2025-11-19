package org.pih.warehouse.api

enum PickTaskStatus {
    PENDING,
    PICKING,
    PICKED

    static final Map<PickTaskStatus, Set<PickTaskStatus>> ALLOWED_STATE_TRANSITIONS = [
            (PENDING): [PICKING, PICKED] as Set,
            (PICKING): [PICKED] as Set,
            (PICKED): [] as Set
    ]

    static final Map<PickTaskStatus, Set<PickTaskStatus>> ROLLBACK_STATE_TRANSITIONS = [
            (PENDING): [] as Set,
            (PICKING): [PENDING] as Set,
            (PICKED): [PICKING, PENDING] as Set
    ]

    static Boolean validateTransition(PickTaskStatus from, PickTaskStatus to) {
        return ALLOWED_STATE_TRANSITIONS.get(from, Collections.emptySet()).contains(to)
    }

    static Boolean validateRollback(PickTaskStatus from, PickTaskStatus to) {
        return ROLLBACK_STATE_TRANSITIONS.get(from, Collections.emptySet()).contains(to)
    }

    static List<PickTaskStatus> list() {
        return values().toList()
    }
}
