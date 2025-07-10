package org.pih.warehouse.core

enum VarianceTypeCode {

    EQUAL(0),
    MORE(1),
    LESS(-1)

    final Integer sortOrder

    VarianceTypeCode(Integer sortOrder) {
        this.sortOrder = sortOrder
    }

    String toString() {
        return name()
    }

}