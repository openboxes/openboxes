package org.pih.warehouse.inventory

enum CycleCountRequestType {

    MANUAL_REQUEST,

    /** Raised by the system when product quantity falls below zero. */
    NEGATIVE_INVENTORY

    @Override
    String toString() {
        return name()
    }
}
