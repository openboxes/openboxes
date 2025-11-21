package org.pih.warehouse.picking

import org.springframework.context.ApplicationEvent

class PickTaskPickedEvent extends ApplicationEvent {

    Boolean forceRefresh = Boolean.FALSE

    PickTaskPickedEvent(PickTask pickTask) {
        super(pickTask)
    }
}
