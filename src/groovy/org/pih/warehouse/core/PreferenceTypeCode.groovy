package org.pih.warehouse.core


enum PreferenceTypeCode {
    PRIMARY(1),
    SECONDARY(2),
    TERTIARY(3),
    ALTERNATIVE(4),
    NOT_PREFERRED(0)


    int sortOrder

    PreferenceTypeCode(int sortOrder) { [this.sortOrder = sortOrder] }

    static int compare(PreferenceTypeCode a, PreferenceTypeCode b) {
        return a.sortOrder <=> b.sortOrder
    }

    static list() {
        [PRIMARY, SECONDARY, TERTIARY, ALTERNATIVE, NOT_PREFERRED]
    }

    String getName() { return name() }

    String toString() { return name() }
}