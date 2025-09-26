package org.pih.warehouse.core

enum DeliveryTypeCode {
    PICK_UP(1, ActivityCode.DELIVERY_TYPE_PICKUP),
    LOCAL_DELIVERY(2, ActivityCode.DELIVERY_TYPE_LOCAL_DELIVERY),
    SERVICE(2, ActivityCode.DELIVERY_TYPE_SERVICE),
    WILL_CALL(3, ActivityCode.DELIVERY_TYPE_WILL_CALL),
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

}