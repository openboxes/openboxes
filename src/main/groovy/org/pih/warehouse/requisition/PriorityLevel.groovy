package org.pih.warehouse.requisition

enum PriorityLevel {

    LOW(-Integer.MAX_VALUE, -1),
    NORMAL(0, 0),
    MEDIUM(1, 49),
    HIGH(50, 99),
    CRITICAL(100, Integer.MAX_VALUE)

    final int minValue
    final int maxValue

    PriorityLevel(int minValue, int maxValue) {
        this.minValue = minValue
        this.maxValue = maxValue
    }

    static PriorityLevel fromPriority(Integer priority) {
        if (priority == null) {
            return NORMAL
        }
        return values().find { priority >= it.minValue && priority <= it.maxValue } ?: NORMAL
    }

    @Override
    String toString() {
        return name()
    }
}
