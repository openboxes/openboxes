package org.pih.warehouse.api

enum OutboundWorkflowState {

    CREATE_HEADER(1),
    ADD_ITEMS(2),
    REVISE_ITEMS(3),
    PICK_ITEMS(4),
    PACK_ITEMS(5),
    SEND_SHIPMENT(6)

    Integer stepNumber

    OutboundWorkflowState(int stepNumber) {
        this.stepNumber = stepNumber
    }

    static OutboundWorkflowState fromStepNumber(Integer stepNumber) {
        if (stepNumber == null) {
            return null
        }
        values().find { it.stepNumber == stepNumber }
    }

    static OutboundWorkflowState fromStepNumber(String stepNumber) {
        if (!stepNumber?.isNumber()) {
            return null
        }
        fromStepNumber(stepNumber?.toInteger())
    }
}
