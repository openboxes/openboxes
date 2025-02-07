package org.pih.warehouse.inventory

// TODO: document!
enum CycleCountCandidateStatus {
    CREATED('CREATED'),

    REQUESTED('REQUESTED'),
    COUNTING('COUNTING'),
    COUNTED('COUNTED'),
    INVESTIGATING('INVESTIGATING'),
    READY_TO_REVIEW('READY_TO_REVIEW'),
    REVIEWED('REVIEWED'),
    COMPLETED('COMPLETED'),
    CANCELED('CANCELED')

    String name

    CycleCountCandidateStatus(String name) {
        this.name = name
    }

    String toString() {
        name
    }
}
