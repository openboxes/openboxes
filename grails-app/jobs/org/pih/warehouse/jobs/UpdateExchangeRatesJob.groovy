package org.pih.warehouse.jobs

class UpdateExchangeRatesJob extends SessionlessJob {

    def currencyService

    static concurrent = false

    static triggers = {
        cron name: JobUtils.getCronName(UpdateExchangeRatesJob),
            cronExpression: JobUtils.getCronExpression(UpdateExchangeRatesJob)
    }

    void execute() {
        if (JobUtils.shouldExecute(UpdateExchangeRatesJob)) {
            log.info "Starting job at ${new Date()}"
            def startTime = System.currentTimeMillis()
            currencyService.updateExchangeRates()
            log.info "Finished running job in " + (System.currentTimeMillis() - startTime) + " ms"
        }
    }
}
