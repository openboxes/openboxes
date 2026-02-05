package org.pih.warehouse.api;

enum InboundWorkflowState {

    CREATE_HEADER(1),
    ADD_ITEMS(2),
    SEND_SHIPMENT(6)

    Integer stepNumber;

    InboundWorkflowState(int stepNumber) {
        this.stepNumber = stepNumber;
    }
}