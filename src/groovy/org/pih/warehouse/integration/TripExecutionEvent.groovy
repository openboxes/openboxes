package org.pih.warehouse.integration

import org.pih.warehouse.xml.acceptancestatus.AcceptanceStatus
import org.pih.warehouse.xml.execution.Execution
import org.springframework.context.ApplicationEvent

class TripExecutionEvent extends ApplicationEvent {

    Execution execution

    TripExecutionEvent(Execution execution) {
        super(execution)
        this.execution = execution
    }

}
