package org.pih.warehouse.core

import org.pih.warehouse.inventory.CycleCount
import org.springframework.context.ApplicationEvent

class CycleCountCompletedEvent extends ApplicationEvent {

    CycleCountCompletedEvent(CycleCount cycleCount) {
        super(cycleCount?.id)
    }
}
