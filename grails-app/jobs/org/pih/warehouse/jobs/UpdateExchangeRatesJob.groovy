package org.pih.warehouse.jobs

import org.pih.warehouse.core.UnitOfMeasureConversion

class UpdateExchangeRatesJob {

    def currencyService

    static concurrent = false

    def sessionRequired = false

    static triggers = {
        cron name: JobUtils.getCronName(UpdateExchangeRatesJob),
            cronExpression: JobUtils.getCronExpression(UpdateExchangeRatesJob)
    }

    void execute() {
        if (JobUtils.shouldExecute(UpdateExchangeRatesJob)) {
            UnitOfMeasureConversion.withNewSession {
                log.info "Starting job at ${new Date()}"
                def startTime = System.currentTimeMillis()
                currencyService.updateExchangeRates()
                log.info "Finished running job in " + (System.currentTimeMillis() - startTime) + " ms"
            }
        }
    }
}
