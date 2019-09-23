package org.pih.warehouse.core


enum RatingTypeCode {
    OUTSTANDING(1),
    GOOD(2),
    FAIR(3),
    POOR(4),
    NOT_RATED(0)


    int sortOrder

    RatingTypeCode(int sortOrder) {
        [this.sortOrder = sortOrder]
    }

    static int compare(RatingTypeCode a, RatingTypeCode b) {
        return a.sortOrder <=> b.sortOrder
    }

    static list() {
        [OUTSTANDING, GOOD, FAIR, POOR, NOT_RATED]
    }

    String getName() { return name() }

    String toString() { return name() }
}