package org.pih.warehouse.picking

import org.springframework.context.ApplicationEvent

class PickTaskUpdateEvent extends ApplicationEvent {

    Boolean forceRefresh = Boolean.FALSE

    PickTaskUpdateEvent(PickTask pickTask) {
        super(pickTask)
    }
}
