package org.pih.warehouse.integration

import org.pih.warehouse.integration.xml.execution.Execution
import org.springframework.context.ApplicationEvent

class TripExecutionEvent extends ApplicationEvent {

    Execution execution

    TripExecutionEvent(Execution execution) {
        super(execution)
        this.execution = execution
    }

}
