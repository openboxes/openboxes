package org.pih.warehouse.core

enum DeliveryTypeCode {
    PICK_UP(1, ActivityCode.DELIVERY_TYPE_PICKUP),
    LOCAL_DELIVERY(2, ActivityCode.DELIVERY_TYPE_LOCAL_DELIVERY),
    SERVICE(2, ActivityCode.DELIVERY_TYPE_SERVICE),
    WILL_CALL(3, ActivityCode.DELIVERY_TYPE_WILL_CALL),
    // STOCK_TRANSFER_IBT Delivery Type Code is a temporary solution until proper one is implemented
    STOCK_TRANSFER_IBT(3, ActivityCode.DELIVERY_TYPE_STOCK_TRANSFER_IBT),
    SHIP_TO(4, ActivityCode.DELIVERY_TYPE_SHIPPING),
    DEFAULT(5, null)

    final Integer priority
    final ActivityCode activityCode


    DeliveryTypeCode(Integer priority, ActivityCode activityCode) {
        this.priority = priority
        this.activityCode = activityCode
    }

    Integer getPriority() {
        return priority
    }

    @Override
    String toString() {
        return name()
    }

    static Comparator<DeliveryTypeCode> byPriority() {
        { a, b -> a.priority <=> b.priority } as Comparator<DeliveryTypeCode>
    }

    // SQL formula mapping delivery_type_code to its priority, for use as a Hibernate formula-mapped
    // property (e.g. ordering requisitions by delivery type priority at the database level).
    // A null/unrecognized delivery type is treated as the lowest priority (sorts last).
    static String getPriorityFormula() {
        return "(case delivery_type_code " +
                values().collect { "when '${it.name()}' then ${it.priority} " }.join() +
                "else ${Integer.MAX_VALUE} end)"
    }

}
