package org.pih.warehouse.jobs

import org.quartz.DisallowConcurrentExecution

@DisallowConcurrentExecution
class MessageHandlingJob {

    def grailsApplication
    def tmsIntegrationService
    def fileTransferService

    // cron job needs to be triggered
    static triggers = { }

    def execute(context) {
        log.info "Executing message handling job " + context
        def startTime = System.currentTimeMillis()
        try {
            String messagePath = context.mergedJobDataMap.get("messagePath")
            tmsIntegrationService.handleMessage(messagePath)
        } catch(Exception e) {
            log.error "Unable to handle message due to error: " + e.message, e
        }
        log.info "Completed message handling job in " + (System.currentTimeMillis() - startTime) + " ms"
    }
}
