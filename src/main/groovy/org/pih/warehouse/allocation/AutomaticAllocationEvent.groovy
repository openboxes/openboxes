package org.pih.warehouse.allocation

import org.springframework.context.ApplicationEvent

class AutomaticAllocationEvent extends ApplicationEvent {

    AutomaticAllocationEvent(String requisitionId) {
        super(requisitionId)
    }
}
