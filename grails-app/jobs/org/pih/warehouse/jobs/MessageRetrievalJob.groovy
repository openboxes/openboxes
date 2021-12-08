package org.pih.warehouse.jobs

import net.schmizz.sshj.connection.ConnectionException
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import org.quartz.DisallowConcurrentExecution

import java.nio.charset.StandardCharsets

@DisallowConcurrentExecution
class MessageRetrievalJob {

    def grailsApplication
    def tmsIntegrationService

    // cron job needs to be triggered
    static triggers = {
        cron name: 'messageRetrievalCronTrigger',
                cronExpression: CH.config.openboxes.jobs.messageRetrievalJob.cronExpression?:"0 0/5 * * * ?"
    }

    def execute(context) {
        log.info "Executing message retrieval job "
        Boolean enabled = CH.config.openboxes.jobs.messageRetrievalJob.enabled?:true
        if (!enabled) {
            return
        }
        def startTime = System.currentTimeMillis()
        try {
            log.info "Starting message retrieval job at ${new Date()}"
            tmsIntegrationService.handleMessages()
        } catch(ConnectionException e) {
            log.error "Unable to retrieve message due to exception: " + e.message, e
        }
        log.info "Finished message retrieval job in " + (System.currentTimeMillis() - startTime) + " ms"
    }


}
