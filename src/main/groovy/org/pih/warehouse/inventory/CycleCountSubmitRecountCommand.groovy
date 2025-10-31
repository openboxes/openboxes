package org.pih.warehouse.inventory

/**
 * Extends CycleCountSubmitCountCommand for the sake of convenience because submitting a recount uses the exact same
 * flow as submitting a regular count. If this changes in the future, we should break this extension and have this
 * command declare all its fields explicitly.
 */
class CycleCountSubmitRecountCommand extends CycleCountSubmitCountCommand {

    static constraints = {
        cycleCount(validator: { CycleCount obj ->
            if (obj?.status != CycleCountStatus.INVESTIGATING) {
                return ['invalidStatus']
            }
        })
    }
}
