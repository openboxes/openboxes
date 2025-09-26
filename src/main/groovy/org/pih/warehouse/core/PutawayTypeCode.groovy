package org.pih.warehouse.core

enum PutawayTypeCode {

    STANDARD,      // default putaway to preferred bin location
    DIRECTED,      // system-directed putaway
    CROSS_DOCK,    // immediate putaway to outbound staging
    RESERVE,       // putaway to reserve location
    FORWARD_PICK,  // putaway to forward picking area
    OVERRIDE,      // putaway to location specified by user
    QUARANTINE,    // putaway to quarantine location
    UNASSIGNED     // putaway with no destination assigned yet

    final String value

    PutawayTypeCode() {
        this.value = name()
    }

    String toString() { value }
}