package org.pih.warehouse.jobs

import org.pih.warehouse.core.identification.BlankIdentifierResolver
class AssignIdentifierJob {

    List<BlankIdentifierResolver> blankIdentifierResolvers

    def sessionRequired = false

    static concurrent = false

    static triggers = {
        cron name: JobUtils.getCronName(AssignIdentifierJob),
            cronExpression: JobUtils.getCronExpression(AssignIdentifierJob)
    }

    void execute() {
        if (!JobUtils.shouldExecute(AssignIdentifierJob)) {
            return
        }

//        Product.withNewSession {
            for (BlankIdentifierResolver blankIdentifierResolver : blankIdentifierResolvers) {
                blankIdentifierResolver.generateForAllUnassignedIdentifiers()
            }
//        }
    }
}
