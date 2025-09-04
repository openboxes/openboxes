package org.pih.warehouse.requisition

enum DemandTypeCode {
    BACK_COUNTER('BC', 0),
    LOCAL_DELIVERY('LD', 1),
    WILL_CALL('WC', 2),
    SHIPPING('SH', 3),
    DEFAULT('DF', 4)

    String code
    int priority

    DemandTypeCode(String code, int priority) {
        this.code = code
        this.priority = priority
    }

    static DemandTypeCode findByCode(String code ) {
        if (!code) {
            return null
        }
        values().find { it.code.equalsIgnoreCase(code) }
    }
}