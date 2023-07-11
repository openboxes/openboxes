package org.pih.warehouse.jobs

class UpdateExchangeRatesJob {

    def currencyService

    static concurrent = false

    // By default this is true on QuartzDisplayJob, which invokes execute()
    // and if sessionRequired is true, then QuartzDisplayJob tries to do session flush
    // even if there is no session
    static sessionRequired = false

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
