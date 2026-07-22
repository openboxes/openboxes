package org.pih.warehouse.core

import org.springframework.context.ApplicationEvent

class TransactionCreatedEvent extends ApplicationEvent {

    TransactionCreatedEvent(String transactionId) {
        super(transactionId)
    }
}
