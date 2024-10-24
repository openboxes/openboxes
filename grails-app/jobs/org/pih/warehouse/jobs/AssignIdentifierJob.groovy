package org.pih.warehouse.jobs

import org.pih.warehouse.core.identification.BlankIdentifierResolver
import org.pih.warehouse.product.Product

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

        // TODO: investigate alternatives to wrapping the whole job in a session on a single entity. Can we do
        //       one session per BlankIdentifierResolver?
        Product.withNewSession {
            for (BlankIdentifierResolver blankIdentifierResolver : blankIdentifierResolvers) {
                blankIdentifierResolver.generateForAllUnassignedIdentifiers()
            }
        }
    }
}
