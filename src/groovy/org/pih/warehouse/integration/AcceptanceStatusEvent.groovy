package org.pih.warehouse.integration

import org.pih.warehouse.xml.acceptancestatus.AcceptanceStatus
import org.springframework.context.ApplicationEvent

class AcceptanceStatusEvent extends ApplicationEvent {

    AcceptanceStatus acceptanceStatus

    AcceptanceStatusEvent(AcceptanceStatus acceptanceStatus) {
        super(acceptanceStatus)
        this.acceptanceStatus = acceptanceStatus
    }

}
