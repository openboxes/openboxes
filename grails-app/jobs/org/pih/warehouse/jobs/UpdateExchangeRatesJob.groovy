package org.pih.warehouse.jobs

import org.quartz.DisallowConcurrentExecution

@DisallowConcurrentExecution
class UpdateExchangeRatesJob {

    def concurrent = false  // make `static` in Grails 3
    def currencyService

    static triggers = {
        cron name: JobUtils.getCronName(UpdateExchangeRatesJob),
            cronExpression: JobUtils.getCronExpression(UpdateExchangeRatesJob)
    }

    def execute() {
        if (JobUtils.shouldExecute(UpdateExchangeRatesJob)) {
            log.info "Starting job at ${new Date()}"
            def startTime = System.currentTimeMillis()
            currencyService.updateExchangeRates()
            log.info "Finished running job in " + (System.currentTimeMillis() - startTime) + " ms"
        }
    }
}
