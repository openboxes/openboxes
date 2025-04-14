package org.pih.warehouse.inventory

/**
 * Extends CycleCountSubmitCountCommand for the sake of convenience because submitting a recount uses the exact same
 * flow as submitting a regular count. If this changes in the future, we should break this extension and have this
 * command declare all its fields explicitly.
 */
class CycleCountSubmitRecountCommand extends CycleCountSubmitCountCommand {

    static constraints = {
        cycleCount(validator: { CycleCount obj ->
            // The validation should be uncommented after coming up with
            // better solution for those cases:
            // - enter qty > 0 while counting, then remove that inventory,
            //   start recount (now cycle count has invalid status), we are
            //   not able to finish the cycle count
            // - remove inventory while recounting, reload, we are
            //   not able to finish the cycle count because of
            //   invalid cycle count status again
            // if (obj?.status != CycleCountStatus.INVESTIGATING) {
            //    return ['invalidStatus']
            // }
        })
    }
}
