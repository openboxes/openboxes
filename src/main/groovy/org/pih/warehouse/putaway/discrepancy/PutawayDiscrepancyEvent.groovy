package org.pih.warehouse.putaway.discrepancy

import org.pih.warehouse.putaway.PutawayTask
import org.springframework.context.ApplicationEvent

class PutawayDiscrepancyEvent extends ApplicationEvent {

    PutawayDiscrepancyEvent(PutawayTask putawayTask) {
        super(putawayTask)
    }
}
