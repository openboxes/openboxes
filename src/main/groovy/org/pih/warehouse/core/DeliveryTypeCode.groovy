package org.pih.warehouse.core

enum DeliveryTypeCode {
    PICK_UP(1),
    LOCAL_DELIVERY(2),
    SERVICE(2),
    WILL_CALL(3),
    SHIP_TO(4),
    DEFAULT(5)

    final Integer priority

    DeliveryTypeCode(Integer priority) {
        this.priority = priority
    }

    Integer getPriority() {
        return priority
    }

    @Override
    String toString() {
        return name()
    }
}